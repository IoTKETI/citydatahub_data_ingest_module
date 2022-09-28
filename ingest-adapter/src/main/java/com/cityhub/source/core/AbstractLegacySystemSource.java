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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.cityhub.environment.Constants;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLegacySystemSource implements ReflectLegacySystem {
  protected ChannelProcessor channelProcessor = null;
  protected JSONObject ConfItem = new JSONObject();
  protected ObjectMapper objectMapper;

  @Override
  public void init(ChannelProcessor channelProcessor, JSONObject ConfItem) {
    this.channelProcessor = channelProcessor;
    this.ConfItem = ConfItem;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
  }

  @Override
  public String doit(HikariDataSource datasource) {
    return null;
  }

  @Override
  public void sendEvent(List<Map<String, Object>> bodyMap, String DATASET_ID) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("entities", bodyMap);
      body.put("datasetId", DATASET_ID);

      Event event = EventBuilder.withBody(objectMapper.writeValueAsString(body).getBytes(Charset.forName("UTF-8")));
      channelProcessor.processEvent(event);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  protected void toLogger(SocketCode sc, String id, byte[] byteBody) {
    JSONObject confItem = new JSONObject(ConfItem);

    log.info("`{}`{}`{}`{}`{}`{}`{}", confItem.getString("sourceName"), confItem.getString("model_id"), sc.toMessage(), id, byteBody.length, confItem.getString("adapterType"), confItem.getString("invokeClass"));

    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(confItem.getString("sourceName"));
    l.append("`").append(confItem.getString("model_id"));
    l.append("`").append(sc.getMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(confItem.getString("adapterType"));
    l.append("`").append(confItem.getString("invokeClass"));

    LoggerVO logVo = new LoggerVO();
    logVo.setSourceName(confItem.getString("sourceName"));
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(confItem.getString("model_id"));
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(confItem.getString("invokeClass"));
    logToDaemonApi(confItem, logVo);
  }

  private void logToDaemonApi(JSONObject confItem, LoggerVO logVo) {
    try {
      if (confItem.has("daemonServerLogApi") && !"".equals(confItem.getString("daemonServerLogApi"))) {
        String resultcode = httpConnection(confItem.getString("daemonServerLogApi"), new JSONObject(logVo).toString());
        log.info("####logJson####{}", resultcode);
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  private String httpConnection(String targetUrl, String jsonBody) throws MalformedURLException, java.io.IOException {
    String returnText = "";

    URL url = new URL(targetUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestMethod("POST");

    if (!"".equals(jsonBody)) {
      conn.setDoOutput(true);
      try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());) {
        wr.write(jsonBody);
        wr.flush();
      } catch (IOException e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }

    int responseCode = conn.getResponseCode();
    StringBuffer sb = null;
    String jsonData = "";
    try (BufferedReader br = new BufferedReader(new InputStreamReader(responseCode < 400 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"));) {
      sb = new StringBuffer();
      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }
      returnText = sb.toString();
      log.debug("returnText:{}", returnText);
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return returnText;
  }

}
