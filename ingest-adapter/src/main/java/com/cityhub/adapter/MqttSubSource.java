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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
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
import com.cityhub.utils.DateUtil;
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

  public void sendEvent(byte[] bodyBytes) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
    byte version = 0x10;// 4bit: Major version, 4bit: minor version
    Integer bodyLength = bodyBytes.length;// length = 1234
    byteBuffer.put(version);
    byteBuffer.putInt(bodyLength.byteValue());
    byteBuffer.put(bodyBytes);
    Event event = EventBuilder.withBody(byteBuffer.array());
    getChannelProcessor().processEvent(event);
  }

  public byte[] createSendJson(JSONObject content) {
    String Uuid = "DATAINGEST_" + getUuid();
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
            HttpEntity entity = httpRes.getEntity();
            if (entity != null) {
              payload = EntityUtils.toString(httpRes.getEntity());
            }
            StatusCode = httpRes.getStatusLine().getStatusCode();
            StatusName = httpRes.getStatusLine().getReasonPhrase();

            log.info("{}", payload);
            Thread.sleep(500);
          } catch (Exception e) {
            log.error("Exception : " + ExceptionUtils.getStackTrace(e));
          }

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

} // end of class
