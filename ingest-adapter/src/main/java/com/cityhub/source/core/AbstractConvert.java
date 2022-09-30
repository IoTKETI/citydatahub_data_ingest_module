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
package com.cityhub.source.core;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;
import org.json.JSONObject;

import com.cityhub.dto.LogVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractConvert implements ReflectExecuter {
  protected ChannelProcessor channelProcessor = null;
  public String id = "";
  public JSONObject ConfItem = null;
  public JSONObject templateItem = null;
  public ObjectMapper objectMapper;

  @Override
  public void init(ChannelProcessor channelProcessor, JSONObject ConfItem, JSONObject templateItem) {
    this.channelProcessor = channelProcessor;
    this.ConfItem = ConfItem;
    this.templateItem = templateItem;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
    setup();
  }

  @Override
  public void setup() {
    // TODO : 추가로 구현해야할 경우 오버라이드 해서 이용
  }

  @Override
  public String doit(byte[] t2) throws CoreException {
    // MQTT
    return null;
  }

  @Override
  public String doit() throws CoreException {
    // OPEN API
    return null;
  }

  public void log(SocketCode sc, String id) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.getCode() + ";" + sc.getMessage() + "", id, 0, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage());
    l.append("`").append(id);
    l.append("`").append(0);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));
    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(ConfItem.getString("modelId"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength("0");
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }

  public void log(SocketCode sc, String id, String msg) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.toMessage() + "-" + msg, id, 0, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage() + "-" + msg);
    l.append("`").append(id);
    l.append("`").append(0);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));
    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(ConfItem.getString("modelId"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage()+ "-" + msg);
    logVo.setId(id);
    logVo.setLength("0");
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);

  }

  public void log(SocketCode sc, String id, String msg, String modelId) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.toMessage() + "-" + msg, id, 0, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(modelId);
    l.append("`").append(sc.toMessage() + "-" + msg);
    l.append("`").append(id);
    l.append("`").append(0);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage()+ "-" + msg);
    logVo.setId(id);
    logVo.setLength("0");
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }

  public void log(SocketCode sc, String id, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.toMessage() + "", id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(ConfItem.getString("modelId"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);

  }

  public void log(SocketCode sc, String id, byte[] byteBody, String modelId) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.toMessage() , id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);

  }

  public void log(SocketCode sc, String id, String msg, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.toMessage() + "-" + msg, id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage() + "-" + msg);
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));
    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(ConfItem.getString("modelId"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage() + "-" + msg);
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }

  public void logger(SocketCode sc, String modelId, String id, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.toMessage() + "", id, byteBody.length, ConfItem.getString("adapterType"),ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(modelId);
    l.append("`").append(sc.toMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }

  public void logger(SocketCode sc, String modelId, String id, byte[] byteBody, String msg) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.toMessage() + "-" + msg, id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(modelId);
    l.append("`").append(sc.toMessage() + "-" + msg);
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage() + "-" + msg);
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));
    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }
  public void sendEventEx(List<Map<String, Object>> entities, String datasetId) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("entities", entities);
      body.put("datasetId", datasetId);
      Event event = EventBuilder.withBody(objectMapper.writeValueAsString(body).getBytes(Charset.forName("UTF-8")));
      channelProcessor.processEvent(event);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

} // end of class
