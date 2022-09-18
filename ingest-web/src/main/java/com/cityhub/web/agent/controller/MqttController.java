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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@RestController
public class MqttController {

  @GetMapping({"/mqtt/subscribe/view"})
  public ModelAndView mqttSubscribeView(HttpServletRequest request,HttpServletResponse response) {
    log.debug("----- MqttController.mqttSubscribeView() -----");

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("mqtt_subscribe");

    return modelAndView;
  }

  @PostMapping({"/mqtt/subscribe/list"})
  public ResponseEntity<List>  mqttSubscribeList(@RequestBody Map param, HttpServletRequest request,HttpServletResponse response) {
    log.debug("----- MqttController.mqttSubscribeList() -----");
    List<Map> result = new ArrayList<>();
    try {
      String http_url = (String)param.get("http_url");
      String http_resource = (String)param.get("http_resource");
      String http_parameter = (String)param.get("http_parameter");
      String header_key1 = (String)param.get("header_key1");
      String header_key2 = (String)param.get("header_key2");
      String header_key3 = (String)param.get("header_key3");
      String header_val1 = (String)param.get("header_val1");
      String header_val2 = (String)param.get("header_val2");
      String header_val3 = (String)param.get("header_val3");
      Map<String,String> Subheaders = new HashMap<>();
      Subheaders.put(header_key1, header_val1);
      Subheaders.put(header_key2, header_val2);
      Subheaders.put(header_key3, header_val3);
      StringBuffer sb = new StringBuffer();
      if (http_url.lastIndexOf("/") ==  http_url.length()) {
        sb.append(http_url);
        if (http_resource.indexOf("/") == 0) {
          sb.append(http_resource);
        } else {
          sb.append("/" + http_resource);
        }
      } else {
        sb.append(http_url + "/");
        if (http_resource.indexOf("/") == 0) {
          sb.append(http_resource.substring(1,http_resource.length()));
        } else {
          sb.append(http_resource);
        }
      }


      if (http_parameter.indexOf("&") == 0) {
        sb.append("?1=1" + http_parameter);
      } else {
        sb.append("?" + http_parameter);
      }
      HttpResponse res = OkUrlUtil.get(sb.toString(), Subheaders);
      if (res.getStatusCode() == 200) {
        JSONObject discovery = new JSONObject(res.getPayload());
        JSONArray ja = discovery.getJSONArray("m2m:uril");
        Map m = null;
        for (Object obj : ja) {
          String line = (String) obj;
          m = new HashMap();
          m.put("item", line);
          result.add(m);
        }
      }
      log.debug("result : {}", result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Value("${daemon.configUrl}")
  private String daemonConfigUrl;

  @PostMapping({"/mqtt/subscribe/save"})
  public ResponseEntity<List<Map>> mqttSubscribeSave(@RequestBody Map param, HttpServletRequest request,HttpServletResponse response) {
    log.debug("----- MqttController.mqttSubscribeSave() -----");
    List<Map> list = new ArrayList<>();

    try {
      String http_url = (String)param.get("http_url");
      String http_resource = (String)param.get("http_resource");
      String http_parameter = (String)param.get("http_parameter");
      String mqtt_ip = (String)param.get("mqtt_ip");
      String mqtt_originator = (String)param.get("mqtt_originator");
      String mqtt_receive = (String)param.get("mqtt_receive");

      String header_key1 = (String)param.get("header_key1");
      String header_key2 = (String)param.get("header_key2");
      String header_key3 = (String)param.get("header_key3");
      String header_val1 = (String)param.get("header_val1");
      String header_val2 = (String)param.get("header_val2");
      String header_val3 = (String)param.get("header_val3");

      String[] t = http_url.substring(7,http_url.length()).split(":",-1);
      String ip = t[0];
      String port = t[1].split("/",-1)[0];
      param.put("ip",ip);
      param.put("port",port);

      log.debug("{}", param);

      Map<String,String> headers = new HashMap<>();
      headers.put(header_key1, header_val1);
      headers.put(header_key2, header_val2);
      headers.put(header_key3, header_val3);
      headers.put(HttpHeaders.CONTENT_TYPE, "application/json;ty=23");
      String nu = "mqtt://" + mqtt_ip + "/" + mqtt_originator + "/" + mqtt_receive + "?ct=json";
      JsonUtil sub = new JsonUtil("{\"m2m:sub\": {\"rn\": \"\",\"enc\": {\"net\": [3]},\"nu\": []}}");


      List<Map> items = (List)param.get("items");
      Map<String,String> result = new HashMap<>();
      for(Map<String,String> m : items) {
        String line = m.get("item");
        String[] l = line.split("/",-1);
        result = new HashMap<>();
          if (l.length == 3 && (!(line.indexOf("keepalive") > 0) && !(line.indexOf("meta") > 0)) ) {
            sub.put("m2m:sub.rn", l[2] + "_subc");
          } else if (l.length == 4 && (!(line.indexOf("keepalive") > 0) && !(line.indexOf("meta") > 0)) ) {
            sub.put("m2m:sub.rn", l[3] + "_subc");

          }
          sub.put("m2m:sub.nu", new JSONArray().put(nu));
          String mqttAddr = "http://" + ip + ":" + port + "/" + line + "?rcn=1";
          int StatusCode = -1;
          String payload = httpConnection(mqttAddr,sub.toString(), StatusCode);

          result.put("addr", line);
          result.put("status", StatusCode + "");
          result.put("message", payload);

          list.add(result);
          Thread.sleep(50);
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(list, HttpStatus.CREATED);
  }
  public String httpConnection(String targetUrl, String jsonBody,int rtnCode ) {
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
      conn.setRequestProperty("Accept-Charset", "utf-8");
      conn.setRequestProperty("Content-Type", "application/json;charset-UTF-8;ty=23");
      conn.setRequestProperty("X-M2M-Origin", "SW001");
      conn.setRequestProperty("X-M2M-RI", "cityhub");
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

      log.debug("returnText:{}",returnText);
    } catch (IOException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } finally {
      try {
        br.close();
        conn.disconnect();
      } catch (Exception e2) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e2));
      }
    }
    return returnText;
  }

}
