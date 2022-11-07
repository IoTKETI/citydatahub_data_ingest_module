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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.FlumeException;
import org.apache.flume.Source;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.ReflectExecuter;
import com.cityhub.core.ReflectExecuterManager;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.model.DataModel;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * A netcat-like source that listens on a given port and turns each line of text
 * into an event.
 * </p>
 * <p>
 * This source, primarily built for testing and exceedingly simple systems, acts
 * like <tt>nc -k -l [host] [port]</tt>. In other words, it opens a specified
 * port and listens for data. The expectation is that the supplied data is
 * newline separated text. Each line of text is turned into a Flume event and
 * sent via the connected channel.
 * </p>
 * <p>
 * Most testing has been done by using the <tt>nc</tt> client but other,
 * similarly implemented, clients should work just fine.
 * </p>
 * <p>
 * <b>Configuration options</b>
 * </p>
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * <th>Unit / Type</th>
 * <th>Default</th>
 * </tr>
 * <tr>
 * <td><tt>bind</tt></td>
 * <td>The hostname or IP to which the source will bind.</td>
 * <td>Hostname or IP / String</td>
 * <td>none (required)</td>
 * </tr>
 * <tr>
 * <td><tt>port</tt></td>
 * <td>The port to which the source will bind and listen for events.</td>
 * <td>TCP port / int</td>
 * <td>none (required)</td>
 * </tr>
 * <tr>
 * <td><tt>max-line-length</tt></td>
 * <td>The maximum # of chars a line can be per event (including newline).</td>
 * <td>Number of UTF-8 characters / int</td>
 * <td>512</td>
 * </tr>
 * </table>
 * <p>
 * <b>Metrics</b>
 * </p>
 * <p>
 * </p>
 */
@Slf4j
public class NetcatSource extends AbstractSource implements Configurable, EventDrivenSource {

  private String hostName;
  private int port;
  private int maxLineLength;
  private boolean ackEveryEvent;
  private String sourceEncoding;

  private CounterGroup counterGroup;
  private ServerSocketChannel serverSocket;
  private AtomicBoolean acceptThreadShouldStop;
  private Thread acceptThread;
  private ExecutorService handlerService;

  private String invokeClass;
  private String modelId;
  private String DATAMODEL_API_URL;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;

  public NetcatSource() {
    super();

    port = 0;
    counterGroup = new CounterGroup();
    acceptThreadShouldStop = new AtomicBoolean(false);
  }

  @Override
  public void configure(Context context) {
    String hostKey = DefaultConstants.CONFIG_HOSTNAME;
    String portKey = DefaultConstants.CONFIG_PORT;
    String ackEventKey = DefaultConstants.CONFIG_ACKEVENT;

    Configurables.ensureRequiredNonNull(context, hostKey, portKey);

    hostName = context.getString(hostKey);
    port = context.getInteger(portKey);
    ackEveryEvent = context.getBoolean(ackEventKey, true);
    maxLineLength = context.getInteger(DefaultConstants.CONFIG_MAX_LINE_LENGTH, DefaultConstants.DEFAULT_MAX_LINE_LENGTH);
    sourceEncoding = context.getString(DefaultConstants.CONFIG_SOURCE_ENCODING, DefaultConstants.DEFAULT_ENCODING);

    invokeClass = context.getString(DefaultConstants.INVOKE_CLASS, "");
    modelId = context.getString("MODEL_ID", "");
    String confFile = context.getString("CONF_FILE", "");

    DATAMODEL_API_URL = context.getString("DATAMODEL_API_URL");
    ArrModel = StrUtil.strToArray(modelId, ",");

    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    ConfItem.put("modelId", modelId);
    ConfItem.put("DATAMODEL_API_URL", DATAMODEL_API_URL);
  }


  @Override
  public void start() {

    log.info("Source starting");

    counterGroup.incrementAndGet("open.attempts");

    templateItem = new JSONObject();
    if (ArrModel != null) {
      HttpResponse resp = OkUrlUtil.get(DATAMODEL_API_URL, "Content-type", "application/json");
      log.info("DATAMODEL_API_URL connected: {}", resp.getStatusCode());
      if (resp.getStatusCode() == 200) {
        DataModel dm = new DataModel(new JSONArray(resp.getPayload()));
        for (String model : ArrModel) {
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createTamplate(model));
          } else {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          }
        }
      } else {
        for (String model : ArrModel) {
          templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
        }
      }
    } else {
      log.error("{} : SCHEMA MODEL-ID NOT FOUND ", modelId);
    }
    if (log.isDebugEnabled()) {
      log.debug("templateItem:{} -- {}", getName(), templateItem);
    }

    try {
      log.debug("socket info : {}, {}", hostName, port);
      SocketAddress bindPoint = new InetSocketAddress(hostName, port);

      serverSocket = ServerSocketChannel.open();
      serverSocket.socket().setReuseAddress(true);
      serverSocket.socket().bind(bindPoint);

      log.info("Created serverSocket:{}", serverSocket);

    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      counterGroup.incrementAndGet("open.errors");
      log.error("Unable to bind to socket. Exception follows.", e);
      stop();
      throw new FlumeException(e);
    }

    handlerService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("netcat-handler-%d").build());

    AcceptHandler acceptRunnable = new AcceptHandler(maxLineLength);
    acceptThreadShouldStop.set(false);
    acceptRunnable.counterGroup = counterGroup;
    acceptRunnable.handlerService = handlerService;
    acceptRunnable.shouldStop = acceptThreadShouldStop;
    acceptRunnable.ackEveryEvent = ackEveryEvent;
    acceptRunnable.source = this;
    acceptRunnable.serverSocket = serverSocket;
    acceptRunnable.sourceEncoding = sourceEncoding;

    acceptRunnable.invokeClass = invokeClass;
    acceptRunnable.ConfItem = ConfItem;
    acceptRunnable.templateItem = templateItem;
    acceptRunnable.modelId = modelId;
    acceptRunnable.name = this.getName();
    acceptRunnable.ArrModel = ArrModel;

    acceptThread = new Thread(acceptRunnable);

    acceptThread.start();

    log.debug("Source started");
    super.start();
  }

  @Override
  public void stop() {
    log.info("Source stopping");

    acceptThreadShouldStop.set(true);

    if (acceptThread != null) {
      log.debug("Stopping accept handler thread");

      while (acceptThread.isAlive()) {
        try {
          log.debug("Waiting for accept handler to finish");
          acceptThread.interrupt();
          acceptThread.join(500);
        } catch (InterruptedException e) {
          log.debug("Interrupted while waiting for accept handler to finish");
          Thread.currentThread().interrupt();
        }
      }

      log.debug("Stopped accept handler thread");
    }

    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        log.error("Unable to close socket. Exception follows.", e);
        return;
      }
    }

    if (handlerService != null) {
      handlerService.shutdown();

      log.debug("Waiting for handler service to stop");

      // wait 500ms for threads to stop
      try {
        handlerService.awaitTermination(500, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        log.debug("Interrupted while waiting for netcat handler service to stop");
        Thread.currentThread().interrupt();
      }

      if (!handlerService.isShutdown()) {
        handlerService.shutdownNow();
      }

      log.debug("Handler service stopped");
    }

    log.debug("Source stopped. Event metrics:{}", counterGroup);
    super.stop();
  }

  private static class AcceptHandler implements Runnable {

    private ServerSocketChannel serverSocket;
    private CounterGroup counterGroup;
    private ExecutorService handlerService;
    private EventDrivenSource source;
    private AtomicBoolean shouldStop;
    private boolean ackEveryEvent;
    private String sourceEncoding;

    private String invokeClass;
    private JSONObject ConfItem;
    private JSONObject templateItem;
    private String modelId;
    private String name;
    private String[] ArrModel = null;
    private JSONObject init = null;

    private final int maxLineLength;

    public AcceptHandler(int maxLineLength) {
      this.maxLineLength = maxLineLength;
    }

    @Override
    public void run() {
      log.debug("Starting accept handler");

      while (!shouldStop.get()) {
        try {
          SocketChannel socketChannel = serverSocket.accept();

          NetcatSocketHandler request = new NetcatSocketHandler(maxLineLength);

          request.socketChannel = socketChannel;
          request.counterGroup = counterGroup;
          request.source = source;
          request.ackEveryEvent = ackEveryEvent;
          request.sourceEncoding = sourceEncoding;

          request.invokeClass = invokeClass;
          request.ConfItem = ConfItem;
          request.templateItem = templateItem;
          request.modelId = modelId;
          request.name = name;
          request.ArrModel = ArrModel;

          handlerService.submit(request);

          counterGroup.incrementAndGet("accept.succeeded");
        } catch (ClosedByInterruptException e) {
          // Parent is canceling us.
        } catch (IOException e) {
          log.error("Unable to accept connection. Exception follows.", e);
          counterGroup.incrementAndGet("accept.failed");
        }
      }

      log.debug("Accept handler exiting");
    }
  }

  private static class NetcatSocketHandler implements Runnable {

    private Source source;
    private CounterGroup counterGroup;
    private SocketChannel socketChannel;
    private boolean ackEveryEvent;
    private String sourceEncoding;
    private final int maxLineLength;

    private String invokeClass;
    private JSONObject ConfItem;
    private JSONObject templateItem;
    private String modelId;
    private String name;
    private String[] ArrModel = null;

    public NetcatSocketHandler(int maxLineLength) {
      this.maxLineLength = maxLineLength;
    }

    @Override
    public void run() {
      log.debug("Starting connection handler");

      try {
        Reader reader = Channels.newReader(socketChannel, sourceEncoding);
        Writer writer = Channels.newWriter(socketChannel, sourceEncoding);

        CharBuffer buffer = CharBuffer.allocate(maxLineLength);
        buffer.flip(); // flip() so fill() sees buffer as initially empty
        sendClient(writer, SocketCode.SOCKET_CONNECT, "" + socketChannel.getRemoteAddress());
        while (true) {
          // this method blocks until new data is available in the socket
          int charsRead = fill(buffer, reader);
          log.debug("Chars read = {}", charsRead);
          sendClient(writer, SocketCode.DATA_RECEIVE);

          // attempt to process all the events in the buffer
          int eventsProcessed = processEvents(buffer, writer);
          log.debug("Events processed = {}", eventsProcessed);

          if (charsRead == -1) {
            // if we received EOF before last event processing attempt, then we
            // have done everything we can
            break;
          } else if (charsRead == 0 && eventsProcessed == 0) {
            if (buffer.remaining() == buffer.capacity()) {
              // If we get here it means:
              // 1. Last time we called fill(), no new chars were buffered
              // 2. After that, we failed to process any events => no newlines
              // 3. The unread data in the buffer == the size of the buffer
              // Therefore, we are stuck because the client sent a line longer
              // than the size of the buffer. Response: Drop the connection.
              log.warn("Client sent event exceeding the maximum length");
              counterGroup.incrementAndGet("events.failed");
              sendClient(writer, SocketCode.SYSTEM_ERROR, "" + "FAILED: Event exceeds the maximum length (" + buffer.capacity() + " chars, including newline)\n");
              break;
            }
          }
        }

        socketChannel.close();

        counterGroup.incrementAndGet("sessions.completed");
      } catch (IOException e) {
        counterGroup.incrementAndGet("sessions.broken");
        try {
          socketChannel.close();
        } catch (IOException ex) {
          log.error("Unable to close socket channel. Exception follows.", ex);
        }
      }

      log.debug("Connection handler exiting");
    }

    public void sendClient(Writer writer, SocketCode sc) {
      try {
        writer.write(getStr(sc));
        writer.flush();
        log.debug("{}", getStr(sc));
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }

    public void sendClient(Writer writer, SocketCode sc, String msg) {
      try {
        writer.write(getStr(sc, msg));
        writer.flush();
        log.debug("{}", getStr(sc, msg));
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }

    public String getStr(SocketCode sc) {
      return sc.getCode() + ";" + sc.getMessage() + "\n";
    }

    public String getStr(SocketCode sc, String msg) {
      return sc.getCode() + ";" + sc.getMessage() + " [" + msg + "]\n";
    }

    /**
     * <p>
     * Consume some number of events from the buffer into the system.
     * </p>
     *
     * Invariants (pre- and post-conditions): <br/>
     * buffer should have position @ beginning of unprocessed data. <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * @param buffer The buffer containing data to process
     * @param writer The channel back to the client
     * @return number of events successfully processed
     * @throws IOException
     */
    private int processEvents(CharBuffer buffer, Writer writer) throws IOException {

      int numProcessed = 0;

      boolean foundNewLine = true;
      while (foundNewLine) {
        foundNewLine = false;

        int limit = buffer.limit();
        for (int pos = buffer.position(); pos < limit; pos++) {
          if (buffer.get(pos) == '\n') {

            // parse event body bytes out of CharBuffer
            buffer.limit(pos); // temporary limit
            ByteBuffer bytes = Charsets.UTF_8.encode(buffer);
            buffer.limit(limit); // restore limit

            // build event object
            byte[] body = new byte[bytes.remaining()];
            bytes.get(body);

            ChannelException ex = null;
            log.debug("받은 데이터 : {}", new String(body));
            // if(!new String(body).startsWith("{"))

            if (ArrModel != null) {

              log.info("::::::::::::::::::{} - Processing - {}:::::::::::::::::", this.name, this.modelId);
              sendClient(writer, SocketCode.DATA_CONVERT_REQ);

              String sb = "";
              try {
                ReflectExecuter reflectExecuter = ReflectExecuterManager.getInstance(this.invokeClass);
                reflectExecuter.init(source.getChannelProcessor(), ConfItem, templateItem);
                sb = reflectExecuter.doit(body);
                sendClient(writer, SocketCode.DATA_CONVERT_SUCCESS);
              } catch (Exception e) {
                log.error("Exception : " + ExceptionUtils.getStackTrace(e));
                sendClient(writer, SocketCode.DATA_CONVERT_FAIL, e.getMessage());
                sendClient(writer, SocketCode.SOCKET_END);
              }

              if (sb != null && sb.lastIndexOf(",") > 0) {
                JSONArray JSendArr = new JSONArray("[" + sb.substring(0, sb.length() - 1) + "]");
                for (Object itm : JSendArr) {
                  byte[] bodyBytes = createSendJson((JSONObject) itm);
                  ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
                  byte version = 0x10;// 4bit: Major version, 4bit: minor version
                  Integer bodyLength = bodyBytes.length;// length = 1234
                  byteBuffer.put(version);
                  byteBuffer.putInt(bodyLength.byteValue());
                  byteBuffer.put(bodyBytes);

                  try {
                    Event event = EventBuilder.withBody(byteBuffer.array());
                    source.getChannelProcessor().processEvent(event);
                    sendClient(writer, SocketCode.DATA_SAVE);
                  } catch (ChannelException chEx) {
                    ex = chEx;
                  }
                  if (ex == null) {
                    counterGroup.incrementAndGet("events.processed");
                    numProcessed++;
                    if (true == ackEveryEvent) {
                      sendClient(writer, SocketCode.SOCKET_END);
                    }
                  } else {
                    sendClient(writer, SocketCode.SYSTEM_ERROR, ex.getMessage());
                    sendClient(writer, SocketCode.SOCKET_END);
                    counterGroup.incrementAndGet("events.failed");
                    log.warn("Error processing event. Exception follows.", ex);
                  }

                }
              }
            } else {
              sendClient(writer, SocketCode.DATA_NOT_EXIST_MODEL, modelId);
              sendClient(writer, SocketCode.SOCKET_END);
            }

            // advance position after data is consumed
            buffer.position(pos + 1); // skip newline
            foundNewLine = true;

            break;
          }
        }

      }

      return numProcessed;
    }

    public byte[] createSendJson(JSONObject content) {
      String Uuid = "DATAINGEST_" + UUID.randomUUID().toString().replaceAll("-", "");
      JSONObject body = new JSONObject();
      body.put("requestId", Uuid);
      body.put("e2eRequestId", Uuid);
      body.put("owner", "dataingest");
      body.put("operation", "FULL_UPSERT");
      body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
      body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : ""));
      body.put("queryString", "");
      body.put("eventTime", DateUtil.getTime());
      body.put("content", content);

      return body.toString().getBytes(Charset.forName("UTF-8"));
    }

    /**
     * <p>
     * Refill the buffer read from the socket.
     * </p>
     *
     * Preconditions: <br/>
     * buffer should have position @ beginning of unprocessed data. <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * Postconditions: <br/>
     * buffer should have position @ beginning of buffer (pos=0). <br/>
     * buffer should have limit @ end of unprocessed data. <br/>
     *
     * Note: this method blocks on new data arriving.
     *
     * @param buffer The buffer to fill
     * @param reader The Reader to read the data from
     * @return number of characters read
     * @throws IOException
     */
    private int fill(CharBuffer buffer, Reader reader) throws IOException {

      // move existing data to the front of the buffer
      buffer.compact();

      // pull in as much data as we can from the socket
      int charsRead = reader.read(buffer);
      counterGroup.addAndGet("characters.received", Long.valueOf(charsRead));

      // flip so the data can be consumed
      buffer.flip();

      return charsRead;
    }

  }
}
