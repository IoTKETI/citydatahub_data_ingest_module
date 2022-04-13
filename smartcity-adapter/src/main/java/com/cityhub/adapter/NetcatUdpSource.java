/**
 *
 * Copyright 2021 PINE C&I CO., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cityhub.adapter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.google.common.annotations.VisibleForTesting;

public class NetcatUdpSource extends AbstractSource implements EventDrivenSource, Configurable {

  private int port;
  private int maxsize = 1 << 16; // 64k
  private String host = null;
  private Channel nettyChannel;
  private String remoteHostHeader = "REMOTE_ADDRESS";

  private String modelId;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String adapterType;
  private String savePathRawData;


  private static final Logger logger = LoggerFactory.getLogger(NetcatUdpSource.class);

  private CounterGroup counterGroup = new CounterGroup();

  // Default Min size
  private static final int DEFAULT_MIN_SIZE = 2048;
  private static final int DEFAULT_INITIAL_SIZE = DEFAULT_MIN_SIZE;
  private static final String REMOTE_ADDRESS_HEADER = "remoteAddress";
  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_HOST = "bind";

  public class NetcatHandler extends SimpleChannelHandler {

    // extract line for building Flume event
    private byte[] extractEvent(ChannelBuffer in) {
      byte[] resbody;
      byte b = 0;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Event e = null;
      boolean doneReading = false;
      try {
        while (!doneReading && in.readable()) {
          b = in.readByte();
          // Entries are separated by '\n'
          if (b == '\n') {
            doneReading = true;
          } else {
            baos.write(b);
          }
        }
        resbody = baos.toByteArray();
        ///e = EventBuilder.withBody(baos.toByteArray(), headers);
      } finally {
        // no-op
      }
      return resbody;
    } // end extractEvent

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent mEvent) {
      try {
        byte[] rawbyte = extractEvent((ChannelBuffer) mEvent.getMessage());
        if (rawbyte == null) {
          return;
        }
        String rawdata = byteArrayToBinaryString(rawbyte);
        String id = "urn:datahub:" + modelId + ":" + port;

        JSONObject jTemplate = new JSONObject(templateItem.getJSONObject(modelId).toString());
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/vdata.jsonld"));
        jsonEx.put("id", id);
        jsonEx.put("type", modelId);
        JSONArray jloc = new JSONArray();
        jloc.put(0.0).put(0.0);
        jsonEx.put("location.value.coordinates", jloc);
        jsonEx.put("port.value", JsonUtil.nvl(port, DataType.INTEGER));
        jsonEx.put("observedAt.value", DateUtil.getTime());
        jsonEx.put("rawData.value", rawdata);

        jsonEx.remove("address");

        logger.info("{}",jTemplate);
        createFileRawData(rawdata, savePathRawData, modelId, port);
        byte[] cont = createSendJson(jTemplate);
        sendEvent(cont, mEvent.getRemoteAddress());
        counterGroup.incrementAndGet("events.success");
      } catch (ChannelException ex) {
        counterGroup.incrementAndGet("events.dropped");
        logger.error("Error writing to channel", ex);
      } catch (RuntimeException ex) {
        counterGroup.incrementAndGet("events.dropped");
        logger.error("Error retrieving event from udp stream, event dropped", ex);
      }
    } // end messageReceived
  } // end class NetcatHandler
  public void createFileRawData(String rawdata,String savePathRawData, String  modelId, int port) {
    File d = new File(savePathRawData);
    if(!d.exists()) {
      d.mkdirs();
    }
    File file = new File(savePathRawData + modelId + "_" + port + "_" + DateUtil.getDate("yyyyMMdd") + ".txt");
    try (FileWriter filewriter = new FileWriter(file, true);
        BufferedWriter bufwriter = new BufferedWriter(filewriter);
        ) {
      bufwriter.write(rawdata);
      bufwriter.newLine();
      bufwriter.flush();
    } catch (Exception e) {
      logger.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }
  public void sendEvent(byte[] bodyBytes, SocketAddress remoteAddress) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put(remoteHostHeader, remoteAddress.toString());
    ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
    byte version = 0x10;//4bit: Major version, 4bit: minor version
    Integer bodyLength = bodyBytes.length;//length = 1234
    byteBuffer.put(version);
    byteBuffer.putInt(bodyLength.byteValue());
    byteBuffer.put(bodyBytes);
    Event event = EventBuilder.withBody(byteBuffer.array(), headers);
    getChannelProcessor().processEvent(event);
  } // end sendEvent

  public byte[] createSendJson(JSONObject content) {
    String Uuid = "DATAINGEST_" + UUID.randomUUID().toString().replaceAll("-","");;
    JSONObject body = new JSONObject();
    body.put("requestId", Uuid);
    body.put("e2eRequestId", Uuid);
    body.put("owner", "dataingest");
    body.put("operation", "FULL_UPSERT");
    body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
    body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : "") );
    body.put("queryString", "");
    body.put("eventTime", DateUtil.getTime());
    body.put("content", content);
    return body.toString().getBytes(Charset.forName("UTF-8"));
  } // end createSendJson


  public String byteArrayToBinaryString(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < b.length; ++i) {
      sb.append(byteToBinaryString(b[i]));
    }
    return sb.toString();
  } // end byteArrayToBinaryString

  public String byteToBinaryString(byte n) {
    StringBuilder sb = new StringBuilder("00000000");
    for (int bit = 0; bit < 8; bit++) {
      if (((n >> bit) & 1) > 0) {
        sb.setCharAt(7 - bit, '1');
      }
    }
    return sb.toString();
  } // end byteToBinaryString

  public byte[] binaryStringToByteArray(String s) {
    int count = s.length() / 8;
    byte[] b = new byte[count];
    for (int i = 1; i < count; ++i) {
      String t = s.substring((i - 1) * 8, i * 8);
      b[i - 1] = binaryStringToByte(t);
    }
    return b;
  } // end binaryStringToByteArray

  public byte binaryStringToByte(String s) {
    byte ret = 0, total = 0;
    for (int i = 0; i < 8; ++i) {
      ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0;
      total = (byte) (ret | total);
    }
    return total;
  } // end binaryStringToByte

  @Override
  public void start() {
    templateItem = new JSONObject();
    templateItem.put(modelId, new JsonUtil().getFileJsonObject("openapi/" + modelId + ".template"));

    // setup Netty server
    ConnectionlessBootstrap serverBootstrap = new ConnectionlessBootstrap(new OioDatagramChannelFactory(Executors.newCachedThreadPool()));
    final NetcatHandler handler = new NetcatHandler();
    serverBootstrap.setOption("receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory(DEFAULT_MIN_SIZE, DEFAULT_INITIAL_SIZE, maxsize));
    serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() {
        return Channels.pipeline(handler);
      }
    });

    if (host == null) {
      nettyChannel = serverBootstrap.bind(new InetSocketAddress(port));
    } else {
      nettyChannel = serverBootstrap.bind(new InetSocketAddress(host, port));
    }
    super.start();
  } // end start




  @Override
  public void stop() {
    logger.info("Netcat UDP Source stopping...");
    logger.info("Metrics:{}", counterGroup);
    if (nettyChannel != null) {
      nettyChannel.close();
      try {
        nettyChannel.getCloseFuture().await(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.warn("netty server stop interrupted", e);
      } finally {
        nettyChannel = null;
      }
    }

    super.stop();
  } // end stop


  @Override
  public void configure(Context context) {
    Configurables.ensureRequiredNonNull(context, CONFIG_PORT);
    port = context.getInteger(CONFIG_PORT);
    host = context.getString(CONFIG_HOST);
    remoteHostHeader = context.getString(REMOTE_ADDRESS_HEADER);
    modelId = context.getString("MODEL_ID", "");
    String confFile = context.getString("CONF_FILE", "");
    adapterType = context.getString("type", "");
    savePathRawData = context.getString("savePathRawData", "");
    savePathRawData = savePathRawData.lastIndexOf("/") !=  (savePathRawData.length() - 1) ? savePathRawData+= "/" : savePathRawData;
    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("modelId", modelId);
  } // end configure

  @VisibleForTesting
  public int getSourcePort() {
    SocketAddress localAddress = nettyChannel.getLocalAddress();
    if (localAddress instanceof InetSocketAddress) {
      InetSocketAddress addr = (InetSocketAddress) localAddress;
      return addr.getPort();
    }
    return 0;
  } // end getSourcePort


}
