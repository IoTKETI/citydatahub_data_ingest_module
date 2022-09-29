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
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpSource extends AbstractPollSource {

  private String modelId;
  private int port;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String adapterType;
  private String savePathRawData;
  private String datasetId;

  @Override
  public void setup(Context context) {
    modelId = context.getString("MODEL_ID", "");

    String confFile = context.getString("CONF_FILE", "");
    port = context.getInteger("port");
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
    ConfItem.put("datasetId", context.getString("DATASET_ID", ""));
  }

  @Override
  public void execFirst() {
    // 유효성 부분 JSON 가져오기
    templateItem = new JSONObject();
    templateItem.put(modelId, new JsonUtil().getFileJsonObject("openapi/" + modelId + ".template"));
  }

  @Override
  public void processing() {
    log.info("Processing - {},{}", this.getName(), modelId);
    try {
      MulticastSocket socket = new MulticastSocket(port);
      InetAddress ia = InetAddress.getByName("230.0.0.1");
      socket.joinGroup(ia);
      //DatagramSocket socket = new DatagramSocket(port);
      byte[] buffer = new byte[42];
      while (true) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          new Exception(e.getMessage());
        }
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        socket.receive(request);
        buffer = request.getData();

        String rawdata = byteArrayToBinaryString(buffer);
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

        log.info("{}",jTemplate);

        sendEventEx(jTemplate.toMap() , datasetId);

        createFileRawData(rawdata, savePathRawData, modelId, port);

      }

    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.NORMAL_ERROR.toMessage(), "", 0, adapterType);
    }
  }

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
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }
  public String byteArrayToBinaryString(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < b.length; ++i) {
        sb.append(byteToBinaryString(b[i]));
    }
    return sb.toString();
  }
  public String byteToBinaryString(byte n) {
    StringBuilder sb = new StringBuilder("00000000");
    for (int bit = 0; bit < 8; bit++) {
        if (((n >> bit) & 1) > 0) {
            sb.setCharAt(7 - bit, '1');
        }
    }
    return sb.toString();
  }

  public byte[] binaryStringToByteArray(String s) {
      int count = s.length() / 8;
      byte[] b = new byte[count];
      for (int i = 1; i < count; ++i) {
          String t = s.substring((i - 1) * 8, i * 8);
          b[i - 1] = binaryStringToByte(t);
      }
      return b;
  }

  public byte binaryStringToByte(String s) {
      byte ret = 0, total = 0;
      for (int i = 0; i < 8; ++i) {
          ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0;
          total = (byte) (ret | total);
      }
      return total;
  }



} // end of class
