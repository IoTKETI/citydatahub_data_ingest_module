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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;
import org.json.JSONObject;

import com.cityhub.dto.LogVO;
import com.cityhub.environment.Constants;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNormalSource  implements ReflectNormalSystem {
  protected ChannelProcessor channelProcessor = null;
  protected ObjectMapper objectMapper;
  protected JSONObject ConfItem = new JSONObject();
  protected JSONObject templateItem;
  protected final static int bufferCount = 100;

  protected int count = 0;

  @Override
  public void init(ChannelProcessor channelProcessor, JSONObject ConfItem) {
    this.channelProcessor = channelProcessor;
    this.ConfItem = ConfItem;
    this.templateItem = ConfItem.getJSONObject("MODEL_TEMPLATE");

    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
  }

  @Override
  public void setup() {
    // TODO : 추가로 구현해야할 경우 오버라이드 해서 이용
  }

  @Override
  public String doit() {
    return null;
  }

  @Override
  public String doit(BasicDataSource ds) {
    return null;
  }

  @Override
  public void sendEvent(List<Map<String, Object>> bodyMap, String datasetId) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("entities", bodyMap);
      body.put("datasetId", datasetId);

      Event event = EventBuilder.withBody(objectMapper.writeValueAsString(body).getBytes(Charset.forName("UTF-8")));
      channelProcessor.processEvent(event);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


  protected void toLogger(SocketCode sc) {
    toLogger(sc, "", "".getBytes());
  }

  protected void toLogger(SocketCode sc, String id) {
    toLogger(sc, id, "".getBytes());
  }
  protected void toLogger(SocketCode sc, String id,String errMsg) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.toMessage(), id, errMsg.getBytes().length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));

    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(ConfItem.getString("sourceName"));
    l.append("`").append(ConfItem.getString("modelId"));
    l.append("`").append(sc.toMessage() + " - " + errMsg);
    l.append("`").append(id);
    l.append("`").append(errMsg.getBytes());
    l.append("`").append(ConfItem.getString("adapterType"));
    l.append("`").append(ConfItem.getString("invokeClass"));

    LogVO logVo = new LogVO();
    logVo.setSourceName(ConfItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(ConfItem.getString("modelId"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage() + " - " + errMsg );
    logVo.setId(id);
    logVo.setLength(String.valueOf(errMsg.getBytes().length));
    logVo.setAdapterType(ConfItem.getString("adapterType"));
    logVo.setInvokeClass(ConfItem.getString("invokeClass"));

    LogWriterToDb.logToDaemonApi(ConfItem, logVo);
  }

  protected void toLogger(SocketCode sc, String id, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("modelId"), sc.toMessage(), id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));

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

}
