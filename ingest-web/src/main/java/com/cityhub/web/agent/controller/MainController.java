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
package com.cityhub.web.agent.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.UrlUtil;
import com.cityhub.web.agent.service.MainService;
import com.cityhub.web.config.ConfigEnv;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
public class MainController {

  public static final String FIELD_HEX = (char) 0xD1 + "";

  @Autowired
  MainService svc;

  @Autowired
  ConfigEnv configEnv;

  private ObjectMapper objectMapper;

  @GetMapping("favicon.ico")
  @ResponseBody
  public void returnNoFavicon() {
  }



  @PostMapping(value = { "/logger/tail", "/agents/{agentId}/adaptors/{adaptorId}/logs" })
  public ResponseEntity<String> tail(@RequestBody Map param) {

    HttpResponse resp = null;
    String payload = "";

    try {
      JSONObject body = new JSONObject();
      body.put("sourceName", param.get("sourceName"));
      body.put("preEndPoint", param.get("preEndPoint"));

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getLogUrl() + "", headers, body.toString());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @PostMapping({ "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/logs" })
  public String create2(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @RequestBody Map param, HttpServletRequest request) {
    log.debug("----- AgentController.agents -----");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/logs", "POST", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping(value = { "/agent/start", "/restApi/agent/start" })
  public ResponseEntity<Map> start(@RequestBody Map param) {

    HttpResponse resp = null;
    try {
      JSONObject body = new JSONObject();
      body.put("id", param.get("id"));

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getAgentUrl() + "/start", headers, body.toString());
      param.put("result", resp.getPayload());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(param, HttpStatus.OK);
  }

  @PostMapping(value = { "/agent/stop", "/restApi/agent/stop" })
  public ResponseEntity<Map> stop(@RequestBody Map param) {

    HttpResponse resp = null;
    try {
      JSONObject body = new JSONObject();
      body.put("id", param.get("id"));

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getAgentUrl() + "/stop", headers, body.toString());
      param.put("result", resp.getPayload());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(param, HttpStatus.OK);
  }

  @PostMapping(value = { "/agent/restart", "/restApi/agent/restart" })
  public ResponseEntity<Map> restart(@RequestBody Map param) {

    HttpResponse resp = null;
    try {
      JSONObject body = new JSONObject();
      body.put("id", param.get("id"));

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getAgentUrl() + "/restart", headers, body.toString());
      param.put("result", resp.getPayload());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(param, HttpStatus.OK);
  }

  @PostMapping(value = { "/agent/status", "/restApi/agent/status" })
  public ResponseEntity<Map> status(@RequestBody Map param) {

    HttpResponse resp = null;
    try {
      JSONObject body = new JSONObject();
      body.put("id", param.get("id"));
      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getAgentUrl() + "/status", headers, body.toString());
      param.put("result", resp.getPayload());
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(param, HttpStatus.OK);
  }

  public String httpConnection(HttpServletRequest request, String targetUrl, String method, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    String result = "";
    JSONObject json = null;
    String jsonData = "";
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    HttpSession session = request.getSession();

    try {

      String stoken = (String) session.getAttribute("token");
      if (stoken != null) {
        if (stoken.length() > 0) {
          if ("{".equals(stoken.substring(0, 1))) {
            json = new JSONObject(stoken);
          }
        }
      }

      String token = (String) json.get("access_token");
      log.debug(jsonBody);
      String gatewayUrl = "";
      url = new URL(gatewayUrl + targetUrl);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod(method);
      conn.setRequestProperty("Authorization", "Bearer " + token);

      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonBody);
        wr.flush();
      }

      br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

      sb = new StringBuffer();

      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }
      returnText = sb.toString();

    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }
    return returnText;
  }

}
