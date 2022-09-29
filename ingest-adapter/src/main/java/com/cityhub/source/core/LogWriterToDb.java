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
import java.net.URL;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import com.cityhub.dto.LogVO;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogWriterToDb {
  private final static String logServer = "http://localhost:8888/logToDbApi";

  public static void logApi(String sourceName, String modelId, SocketCode sc, String id, byte[] byteBody, String adapterType, String invokeClass) {
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(sourceName);
    l.append("`").append(modelId);
    l.append("`").append(sc.getCode() + ";" + sc.getMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(adapterType);
    l.append("`").append(invokeClass);

    LogVO logVo = new LogVO();
    logVo.setSourceName(sourceName);
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(invokeClass);
    logToDaemonApi(logVo);
  }

  public static void logApi(JSONObject ConfItem, String sourceName, String modelId, SocketCode sc, String id, byte[] byteBody, String adapterType, String invokeClass) {
    StringBuilder l = new StringBuilder();
    l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    l.append("`").append(sourceName);
    l.append("`").append(modelId);
    l.append("`").append(sc.getCode() + ";" + sc.getMessage());
    l.append("`").append(id);
    l.append("`").append(byteBody.length);
    l.append("`").append(adapterType);
    l.append("`").append(invokeClass);

    LogVO logVo = new LogVO();
    logVo.setSourceName(sourceName);
    logVo.setPayload(l.toString());
    logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
    logVo.setType(modelId);
    logVo.setStep(sc.getCode());
    logVo.setDesc(sc.getMessage());
    logVo.setId(id);
    logVo.setLength(String.valueOf(byteBody.length));
    logVo.setAdapterType(invokeClass);
    logToDaemonApi(ConfItem, logVo);
  }

  public static void logToDaemonApi(LogVO logVo) {
    JSONObject logJson = new JSONObject(logVo);
    String resultcode = httpConnection(logServer, logJson.toString());
    log.info("daemonServerLogApi:{}", resultcode);
  }

  public static void logToDaemonApi(JSONObject confItem, LogVO logVo) {
    JSONObject logJson = new JSONObject(logVo);
    if (confItem.has("daemonServerLogApi")) {
      if (confItem.getString("daemonServerLogApi") != null && !"".equals(confItem.getString("daemonServerLogApi")) && logJson != null) {
        String resultcode = httpConnection(confItem.getString("daemonServerLogApi"), logJson.toString());
        log.info("daemonServerLogApi:{}", resultcode);
      }
    } else {
      // default daemon Url
      String resultcode = httpConnection(logServer, logJson.toString());
      log.info("daemonServerLogApi:{}", resultcode);
    }
  }

  public static String httpConnection(String targetUrl, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    String jsonData = "";
    int responseCode;

    try {
      url = new URL(targetUrl);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod("POST");

      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonBody);
        wr.flush();
      }
      responseCode = conn.getResponseCode();
      log.debug("responseCode : {}", responseCode);

      if (responseCode < 400) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
        br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }

      sb = new StringBuffer();

      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }
      returnText = sb.toString();

      log.debug("returnText:{}", returnText);
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      try {
        br.close();
        conn.disconnect();
      } catch (Exception e2) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e2));
      }
    }
    return returnText;
  }

} // end of class
