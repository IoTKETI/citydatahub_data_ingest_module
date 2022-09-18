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
package com.cityhub.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.source.PollableSourceConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.dto.KafkaStatusVO;
import com.cityhub.dto.VerifyStatusVO;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPollSource extends AbstractBaseSource implements PollableSource {

  private String urlAddr;
  private String invokeClass;
  private Integer connTerm; // second

  @Override
  public void configure(Context context) {

    super.configure(context);
    urlAddr = context.getString(DefaultConstants.URL_ADDR, "");
    invokeClass = context.getString(DefaultConstants.INVOKE_CLASS, "");
    connTerm = context.getInteger(DefaultConstants.CONN_TERM, 600);

    setup(context);
  }


  @Override
  public void start() {
    super.start();
    execFirst();
  }

  @Override
  public void stop() {
    exit();
    super.stop();
  }



  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    try {
      long eventCounter = counterGroup.get("events.success");
      counterGroup.addAndGet("events.success", eventCounter);
      processing();
      Thread.sleep(connTerm * 1000); // second * 1000
      status = Status.READY;
    } catch (Exception e) {
      counterGroup.incrementAndGet("events.failed");
      status = Status.BACKOFF;
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return status;
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


  public byte[] createSendJson(JSONObject content) {
    String Uuid = "DATAINGEST_" + getUuid();
    JSONObject body = new JSONObject();
    body.put("requestId", Uuid);
    body.put("e2eRequestId", Uuid);
    body.put("owner", getInit().getString("owner"));
    body.put("operation", getInit().getString("operation"));
    body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
    body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : "") );
    body.put("queryString", "");
    body.put("eventTime", DateUtil.getTime());
    body.put("content", content);
    return body.toString().getBytes(Charset.forName("UTF-8"));
  }

  public byte[] createSendJson(JSONObject content, String Uuid) {
    JSONObject body = new JSONObject();
    body.put("requestId", Uuid);
    body.put("e2eRequestId", Uuid);
    body.put("owner", getInit().getString("owner"));
    body.put("operation", getInit().getString("operation"));
    body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
    body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : "") );
    body.put("queryString", "");
    body.put("eventTime", DateUtil.getTime());
    body.put("content", content);
    return body.toString().getBytes(Charset.forName("UTF-8"));
  }

  public void createSendJson(String sb) {
    JSONArray JSendArr = new JSONArray("[" + sb.substring(0 , sb.length() - 1) + "]");
    try {
      for (Object itm : JSendArr) {
        JSONObject content = (JSONObject)itm;
        sendEvent(createSendJson(content));
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

  }


  public abstract void setup(Context context);

  public abstract void execFirst();

  public abstract void processing();

  public void exit() {}


  @Override
  public long getBackOffSleepIncrement() {
    return PollableSourceConstants.DEFAULT_BACKOFF_SLEEP_INCREMENT;
  }

  @Override
  public long getMaxBackOffSleepInterval() {
    return PollableSourceConstants.DEFAULT_MAX_BACKOFF_SLEEP;
  }


  public String getUrlAddr() {
    return urlAddr;
  }

  public void setUrlAddr(String urlAddr) {
    this.urlAddr = urlAddr;
  }


  public String getInvokeClass() {
    return invokeClass;
  }

  public void setInvokeClass(String invokeClass) {
    this.invokeClass = invokeClass;
  }
  public void listSendKafka(List<VerifyStatusVO> msg )  {
    for (int k = 0; k < msg.size(); k++) {
      try {
        VerifyStatusVO vo = msg.get(k);
        JSONObject content = new JSONObject(vo.getJsonMsg());
        JSONObject body = new JSONObject();
        body.put("requestId", vo.getUniqueId());
        body.put("e2eRequestId", vo.getUniqueId());
        body.put("owner", getInit().getString("owner"));
        body.put("operation", getInit().getString("operation"));
        body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
        body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : "") );
        body.put("queryString", "");
        body.put("eventTime", DateUtil.getTime());
        body.put("content", content);
        sendEvent(body.toString().getBytes(Charset.forName("UTF-8")));

        if (k % 10 == 0  ) {
          Thread.sleep(1);
        }
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }
  }

  public byte[] progressSendKafka(String eventType, String modelType , String cid, String requestId, String personId ,String sdt) {
    JSONObject ff = null;
    try {
      Thread.sleep(10);
      KafkaStatusVO vo = new KafkaStatusVO();
      vo.setEventType(eventType);
      vo.setEventDataType(modelType);
      vo.setEventDataGroupId(cid);
      vo.setEventTriggeredRequestId(requestId);
      vo.setEventDataPersonId(personId);
      vo.setEventDataBeginTime(sdt);
      ff = new JSONObject(vo.toJson());
      ff.remove("eventDataEndTime");
      ff.remove("eventDataTotalCount");
      ff.remove("requestedDataTotalCount");
      ff.remove("eventDescription");
      log.info(ff.toString());

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return ff.toString().getBytes();
  }


  public String refineAddr(String addr) {
    if (addr != null && !"".equals(addr)) {
      addr = addr.replaceAll("일반 ", "");
      addr = addr.replaceAll("-0", "");
      addr = addr.replaceAll("나대지", "");
      addr = addr.replaceAll("()", "");

      if (addr.indexOf(")") <= 8 ) {
        addr = addr.substring(addr.indexOf(")") + 1 , addr.length());
      } else {
        addr = addr.substring(0 , addr.length());
      }
    }
    return StrUtil.trim(addr);
  }

  protected void extracted(InterruptedException e) {
    log.error("{}", e.getStackTrace());
  }

  protected void extracted(IOException e) {
    log.error("{}", e.getStackTrace());
  }

  protected static void extracted(Exception e) {
    log.error("{}", e.getStackTrace());
  }

}
