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
package com.cityhub.adapter.convex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.source.PollableSourceConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.environment.Constants;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelfQuarantineEventSource extends AbstractSource implements Configurable, PollableSource {
  private ObjectMapper objectMapper;
  private Integer connTerm; // second
  private String urladdr;
  private String urladdr2;
  @Override
  public void configure(Context context) {
    connTerm = context.getInteger(DefaultConstants.CONN_TERM, 5);
    urladdr = context.getString("url_addr", "");
    urladdr2 = context.getString("url_addr2", "");

    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));


  }

  public void processing() {
    log.info("Processing - {},{}", this.getName());
    try {
      String requestId = "SQ-";
      if (!"".equals(urladdr)){
        log.info("지역별 자가격리자 현황:{}", urladdr);
        String resultStr = httpConnection(urladdr,"GET", "");
        if (!resultStr.startsWith("ERROR") && resultStr.startsWith("{")) {
          ResponseType responseType = ResponseType.OK;
          requestId += DateUtil.getDate("yyyyMMddHHmmss");
          SelfQuarantineEventVO vo = new SelfQuarantineEventVO();
          vo.setRequestId(requestId);
          vo.setType(responseType.getDetail());
          vo.setTitle(responseType.getTitle());
          JSONObject content = new JSONObject(resultStr);

          if (!"".equals(urladdr2)) {
            urladdr2 = urladdr2 + "?srch_quarantine_month=" + DateUtil.getTime("yyyyMM");
            log.info("일별 자가격리자 현황:{}", urladdr2);
            String resultStr2 = httpConnection(urladdr2,"GET", "");
            JSONObject content2 = new JSONObject(resultStr2);
            JSONArray ja = content2.optJSONArray("data");
            if(ja != null ) {
              for(int i=0; i< ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                String pastOneday = DateUtil.addDate(DateUtil.getTime("yyyyMMdd"),ChronoUnit.DAYS,-1 ,"yyyyMMdd");
                if (pastOneday.equals(jo.getString("quarantine_date")) ) {
                  JSONObject cont = content.getJSONObject("data");
                  cont.put("quarantine_date", jo.getString("quarantine_date"));
                  cont.put("quarantine_cnt", jo.getInt("quarantine_cnt"));
                  break;
                }
              }

            }
          }
          if (content.optJSONObject("data") != null ) {
            vo.setDetail(content.getJSONObject("data").toMap());
          }

          log.info("sendKafka:{}",objectMapper.writeValueAsString(vo));
          sendEvent(vo);
        } else {
          ResponseType responseType = ResponseType.SERVICE_UNAVAILABLE;
          requestId += DateUtil.getDate("yyyyMMddHHmmss");

          SelfQuarantineEventVO vo = new SelfQuarantineEventVO();
          vo.setRequestId(requestId);
          vo.setType(responseType.getDetail());
          vo.setTitle(responseType.getTitle());
          log.info("sendKafka:{}",objectMapper.writeValueAsString(vo));
          sendEvent(vo);
        }
      }
      Thread.sleep(connTerm * 1000); // second * 1000
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendEvent(SelfQuarantineEventVO vo ) {
    try {
      byte[] bodyBytes = objectMapper.writeValueAsString(vo).getBytes(Charset.forName("UTF-8"));
      ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
      byte version = 0x10;//4bit: Major version, 4bit: minor version
      Integer bodyLength = bodyBytes.length;//length = 1234
      byteBuffer.put(version);
      byteBuffer.putInt(bodyLength.byteValue());
      byteBuffer.put(bodyBytes);
      Event event = EventBuilder.withBody(byteBuffer.array());
      getChannelProcessor().processEvent(event);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public String httpConnection(String targetUrl,String method,  String jsonBody)  {
    String returnText = "";
    HttpURLConnection conn = null;
    try  {
      String jsonData = "";
      int responseCode;
      StringBuffer sb = null;
      URL url = new URL(targetUrl);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod(method);
      conn.setConnectTimeout(1000*10); // 10초 커낵션 시도
      conn.setReadTimeout(1000*60*1); // 1분 READ 대기

      /*
      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());) {
          wr.write(jsonBody);
          wr.flush();
        } catch (Exception e) {
          log.error("Exception : " + ExceptionUtils.getStackTrace(e));
        }
      }
       */

      responseCode = conn.getResponseCode();
      if (responseCode < 400) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); ) {
          sb = new StringBuffer();

          while ((jsonData = br.readLine()) != null) {
            sb.append(jsonData);
          }
        } catch (Exception e) {
          log.error("Exception : " + ExceptionUtils.getStackTrace(e));
        }
      } else {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8")); ) {
          sb = new StringBuffer();
          while ((jsonData = br.readLine()) != null) {
            sb.append(jsonData);
          }
          log.error("ERROR: {}", sb.toString());
          sb = new StringBuffer();
          sb.append("ERROR:");
          sb.append(responseCode);
        } catch (Exception e) {
          log.error("Exception : " + ExceptionUtils.getStackTrace(e));
        }
      }
      returnText = sb.toString();
      log.info("responseData: {},{}", responseCode, returnText);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return returnText;
  }

  public String getInitFile(String fileName) {
    StringBuffer result = new StringBuffer("");
    File file = new File(fileName);
    if (file.exists()) {
      try (Scanner scanner = new Scanner(file)) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          result.append(line);
        }
        scanner.close();
      } catch (IOException e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }
    return result.toString();
  }


  @Override
  public void start() {
    super.start();
    log.debug("source {} starting", this.getName());
  }

  @Override
  public void stop() {
    super.stop();
    log.debug("source {} stopping", this.getName());

  }

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    try {
      processing();
      status = Status.READY;
    } catch (Exception e) {
      status = Status.BACKOFF;
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return status;
  }


  @Override
  public long getBackOffSleepIncrement() {
    return PollableSourceConstants.DEFAULT_BACKOFF_SLEEP_INCREMENT;
  }

  @Override
  public long getMaxBackOffSleepInterval() {
    return PollableSourceConstants.DEFAULT_MAX_BACKOFF_SLEEP;
  }
} // end of class
