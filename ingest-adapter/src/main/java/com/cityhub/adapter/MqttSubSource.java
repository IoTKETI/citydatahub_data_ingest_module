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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttSubSource extends AbstractPollSource {

  private String addr = "";
  private JsonUtil sub = null;
  JSONObject dis = null;
  String url_addr = "";
  String topic = "";

  @Override
  public void setup(Context context) {
    url_addr = context.getString("URL_ADDR");
    String aeName = context.getString(DefaultConstants.AE_NAME, "Mobius/sync_parking_raw");
    addr = url_addr + "/" + aeName;
    topic = context.getString("TOPIC");
  }

  @Override
  public void execFirst() {
    sub = new JsonUtil();
    sub.setFileObject("openapi/subParkingJson.template");

  }

  @Override
  public void processing() {
    log.info("::::::::::::::::::{} - Processing :::::::::::::::::", this.getName());
    log.info("`{}`{}`{}`{}`{}", this.getName(), "", "1000;Start Subscriptions", "", "");
    try {
      Map<String, String> Subheaders = new LinkedHashMap<>();
      Subheaders.put(HttpHeaders.ACCEPT, "application/json");
      Subheaders.put("X-M2M-Origin", "SW001");
      Subheaders.put("X-M2M-RI", "cityhub");

      HttpResponse res = OkUrlUtil.get(addr + "?fu=1&ty=3", Subheaders);
      if (res.getStatusCode() == 200) {
        // log.debug(res.getPayload());
        dis = new JSONObject(res.getPayload());
        crtsub();
        JSONObject jo = new JSONObject();
        jo.put("content", "Finished Subscriptions");
        sendEvent(createSendJson(jo));
        log.info("`{}`{}`{}`{}`{}", this.getName(), "", "10000;Finished Subscriptions", "", "");
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

  }

  public void crtsub() {
    if (dis != null) {
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put(HttpHeaders.ACCEPT, "application/json");
      headers.put(HttpHeaders.CONTENT_TYPE, "application/json;ty=23");
      headers.put("X-M2M-Origin", "SW001");
      headers.put("X-M2M-RI", "cityhub");
      String ip = url_addr.substring(url_addr.indexOf("//") + 2).split(":", -1)[0];
      String nu = "mqtt://" + ip + ":1883/" + topic + "?ct=json";
      JSONArray ja = dis.getJSONArray("m2m:uril");
      for (Object obj : ja) {
        String line = (String) obj;
        String[] l = line.split("/", -1);
        try {
          if (l.length == 3) {
            if (!(line.indexOf("keepalive") > 0 || line.indexOf("meta") > 0)) {
              sub.put("m2m:sub.rn", l[2] + "_subc");
            }
          } else if (l.length == 4) {
            if (!(line.indexOf("keepalive") > 0 || line.indexOf("meta") > 0)) {
              sub.put("m2m:sub.rn", l[3] + "_subc");
            }
          }

          sub.put("m2m:sub.nu", new JSONArray().put(nu));

          String mqttAddr = url_addr + "/" + line + "?rcn=1";
          // log.info("headers:{}", headers);
          // log.info("mqttAddr:{}", mqttAddr);
          // log.info("body:{}", sub.toString());

          HttpPost httpPost = new HttpPost(mqttAddr);
          httpPost.setHeader("Accept", "application/json");
          httpPost.setHeader("Accept-Charset", "utf-8");
          httpPost.setHeader("Content-Type", "application/json;ty=23");
          httpPost.setHeader("X-M2M-Origin", "SW001");
          httpPost.setHeader("X-M2M-RI", "cityhub");
          httpPost.setEntity(new StringEntity(sub.toString(), StandardCharsets.UTF_8));

          RequestConfig.Builder requestBuilder = RequestConfig.custom();
          requestBuilder.setConnectTimeout(5 * 1000);
          requestBuilder.setConnectionRequestTimeout(5 * 1000);
          HttpClientBuilder builder = HttpClientBuilder.create();
          builder.setDefaultRequestConfig(requestBuilder.build());

          try (CloseableHttpClient httpclient = builder.build()) {
            String payload = null;
            int StatusCode = -1;
            String StatusName = null;
            CloseableHttpResponse httpRes = httpclient.execute(httpPost);
            // log.info(">>>{}>>>{}>>>{}", httpPost.getMethod(),
            // httpRes.getStatusLine().toString(), httpPost.getURI() );
            HttpEntity entity = httpRes.getEntity();
            if (entity != null) {
              payload = EntityUtils.toString(httpRes.getEntity());
              // log.info("<<<{}", payload);
            }
            StatusCode = httpRes.getStatusLine().getStatusCode();
            StatusName = httpRes.getStatusLine().getReasonPhrase();
            // log.info("{}",StatusCode + " , " + StatusName + " , " +
            // httpPost.getMethod());
            // log.info("{}",httpPost.getLastHeader("Content-Type"));
            // log.info("{}",httpPost.getURI());
            log.info("{}", payload);
            Thread.sleep(500);
          } catch (Exception e) {
            log.error("Exception : " + ExceptionUtils.getStackTrace(e));
          }

          // httpConnection(mqttAddr,sub.toString());
          /*
           * HttpResponse hr = OkUrlUtil.post(mqttAddr, headers, sub.toString());
           * log.info("`{}`{}`{}`{}`{}",this.getName() ,"", hr.getStatusCode() + ";" +
           * hr.getPayload() , "", "");
           *
           * if (log.isDebugEnabled()) { log.debug("status :{} , addr: {} , result: {} ",
           * hr.getStatusCode(), mqttAddr, hr.getPayload()); }
           */
          Thread.sleep(100);
        } catch (Exception e) {
          log.error("Exception : " + ExceptionUtils.getStackTrace(e));
        }
      }
    }
  }

  public void delsub() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put(HttpHeaders.ACCEPT, "application/json");
    headers.put("X-M2M-Origin", "SW001");
    headers.put("X-M2M-RI", "cityhub");

    String ContNm = "";
    JSONArray ja = dis.getJSONArray("m2m:uril");
    for (Object obj : ja) {
      String line = (String) obj;
      String[] l = line.split("/", -1);

      try {

        if (l.length == 3 && (!(line.indexOf("keepalive") > 0) && !(line.indexOf("meta") > 0))) {
          ContNm = line.split(",", -1)[2] + "_sub";
        } else if (l.length == 4 && (!(line.indexOf("keepalive") > 0) && !(line.indexOf("meta") > 0))) {
          ContNm = line.split(",", -1)[2] + "/" + line.split(",", -1)[3] + "_sub";
        }
        String mqttAddr = addr + ContNm + "?rcn=0";
        HttpResponse hr = OkUrlUtil.delete(mqttAddr, headers);
        log.debug("status :{} , addr: {} ", hr.getStatusCode(), mqttAddr);
        Thread.sleep(500);
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
  }

  public String httpConnection(String targetUrl, String jsonBody) {
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
