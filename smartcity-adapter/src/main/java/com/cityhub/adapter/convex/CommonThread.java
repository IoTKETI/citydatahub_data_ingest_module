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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
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

import com.cityhub.dto.Cor19FileInfoVO;
import com.cityhub.dto.VerifyStatusVO;
import com.cityhub.environment.Constants;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.StrUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonThread {

  public ObjectMapper objectMapper;
  public String sourceName;
  public String adapterType;
  public StatusProducer producer = null;
  public ChannelProcessor channelProcessor;
  public String requestDtPath;

  public CommonThread() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
  }
  public void logger(SocketCode sc, String modelId, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}", sourceName, modelId, sc.getCode() + ";" + sc.getMessage() + "", modelId, byteBody.length, adapterType);
  }

  public String refineAddr(String addr) {
    if (addr != null && !"".equals(addr)) {
      addr = addr.replaceAll("일반", "");
      addr = addr.replaceAll("-0", "");
      addr = addr.replaceAll("나대지", "");
    }
    return addr;
  }

  public RequestDtVerify getRequestDtVerify(List<RequestDtVerify> reqDtList, String personId) {
    RequestDtVerify req = null;
    for(int i = 0; i < reqDtList.size(); i++ ) {
      req = reqDtList.get(i);
      if (personId.equals(req.getPersonId())  ) {
        break;
      }
    }
    return req;
  }
  public boolean parsingReqDtVerify( Cor19FileInfoVO fvo, List<RequestDtVerify> reqDtList) {
    try {
      String fileNm = fvo.getFileName().split("\\.", -1)[0];
      requestDtPath = requestDtPath.lastIndexOf("/") !=  (requestDtPath.length() - 1) ? requestDtPath+= "/" : requestDtPath;
      File reqfile = new File(requestDtPath + fileNm + ".txt");
      if (reqfile.exists()) {
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(reqfile)); ) {
          while ((line = br.readLine()) != null) {
            if (!"".equals(line)) {
              String[] token = line.split(",", -1);
              if (!"".equals(token[0])) {
                reqDtList.add(new RequestDtVerify(StrUtil.trim(token[0]), StrUtil.trim(token[1]), StrUtil.trim(token[2])));
                if (log.isInfoEnabled()) {
                  log.info("Request Date: {},{},{},{}", fvo.getRequestId(),StrUtil.trim(token[0]), StrUtil.trim(token[1]), StrUtil.trim(token[2]));
                }
              }
            }
          }
        } catch (Exception e) {
          log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        }
        return true;
      } else  {
        return false;
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      return false;
    }
  }



  public Map<String,String> readResFile(String path , String filename) {
    Map<String,String> resMap = new LinkedHashMap<>();
    File f = new File(path + filename);

    try (BufferedReader br  =  new BufferedReader(new InputStreamReader(new FileInputStream(f),"euc-kr"))){
      String line  =  null;
      if ((line=br.readLine()) != null)
      {
        String[] arr = line.split("\\|",-1);
        String resFileName = getArrayIndex(arr, 2);
        String resCode = getArrayIndex(arr, 3);
        String resReason = getArrayIndex(arr, 4);
        String resMemo = getArrayIndex(arr, 5);

        resMap.put("tId", arr[0]);
        resMap.put("reqId", arr[0].substring(0,13));
        resMap.put("caseId", arr[0].substring(13,26));
        resMap.put("reqType", arr[1]);
        resMap.put("resFileName", resFileName);
        resMap.put("resCode", resCode);
        resMap.put("resReason", resReason);
        resMap.put("resMemo", resMemo);
        resMap.put("resTextFileName", filename);
      }
      log.info("ResFile: {}, {}, {}", filename, f.exists(), objectMapper.writeValueAsString(resMap));
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return resMap;
  }

  public Map<String,String> readReqFile(String path , String filename) {
    Map<String,String> reqMap = new LinkedHashMap<>();
    File f = new File(path + filename);
    try (BufferedReader br  =  new BufferedReader(new InputStreamReader(new FileInputStream(f),"euc-kr"))){
      String line  =  null;
      if ((line=br.readLine()) != null)
      {
        String[] arr = line.split("\\|",-1);
        reqMap.put("tId", arr[0]);
        reqMap.put("reqType", arr[1]);
        reqMap.put("reqId", arr[0].substring(0,13));
        reqMap.put("caseId", arr[2]);
        reqMap.put("from", DateUtil.getISOTime(arr[6], "yyyy-MM-dd HH:mm:ss"));
        reqMap.put("to", DateUtil.getISOTime(arr[7], "yyyy-MM-dd HH:mm:ss"));
        reqMap.put("reqTextFileName", filename);
      }
      log.info("ReqFile:{}, {}, {}", filename, f.exists(), objectMapper.writeValueAsString(reqMap));
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return reqMap;
  }

  /**
   * @param arr
   * @param index
   * @return
   */
  public String getArrayIndex(String[] arr, int index) {
    String result = "";
    try {
      result = arr[index];
    } catch (ArrayIndexOutOfBoundsException e) {

    }
    return result;
  }



  /**
   * @param topic
   * @param responseType
   * @param interworking
   */
  public void sendInterworking(String topic, ResponseType responseType, Interworking interworking) {
    try {
      KafkaResponse kResponse = new KafkaResponse();
      kResponse.setRequestId("ML-" + interworking.getRequestId() + "-" + interworking.getCaseId());
      kResponse.setE2eRequestId("ML-" + interworking.getRequestId() + "-" + interworking.getCaseId());
      kResponse.setType(responseType.getDetail());
      kResponse.setTitle(responseType.getTitle());
      kResponse.setDetail(interworking);
      String res =  objectMapper.writeValueAsString(kResponse);
      log.info("KAFKA-TOPIC:{}, {}, {}", topic, interworking.getRequestId() + "-" + interworking.getCaseId(), res);
      producer.send(topic,res.getBytes());
      producer.flush();
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * @param reqId
   * @param caseId
   * @param vendorType
   * @param dataFrom
   * @param dataTo
   * @param totalCount
   * @param responseDetail
   * @return
   */
  public Interworking setInterworking(String reqId, String caseId, String vendorType, String dataFrom, String dataTo, int totalCount, String responseDetail) {
    Interworking interworking = new Interworking();
    interworking = new Interworking();
    interworking.setRequestId(reqId);
    interworking.setCaseId(caseId);
    interworking.setVendor(vendorType);
    if (dataFrom.lastIndexOf(",") > 0 ) {
      interworking.setDataFrom(dataFrom.substring(0, dataFrom.lastIndexOf(",")));
    } else {
      interworking.setDataFrom(dataFrom);
    }
    if (dataTo.lastIndexOf(",") > 0 ) {
      interworking.setDataTo(dataTo.substring(0, dataTo.lastIndexOf(",")));
    } else {
      interworking.setDataTo(dataTo);
    }
    interworking.setTotalCount(totalCount);
    if (!"".equals(responseDetail)) {
      interworking.setResponseDetail(responseDetail);
    }
    return interworking;
  }


  /**
   * @param topic
   * @param eventType
   * @param modelType
   * @param cid
   * @param requestId
   * @param caseId
   * @param sdt
   */
  public void sendDataProcessStatusUpdated(String topic , String eventType, String modelType , String cid, String requestId, String caseId ,String sdt) {
    try {
      Thread.sleep(10);
      KafkaStatusVO vo = new KafkaStatusVO();
      vo.setEventType(eventType);
      vo.setEventDataType(modelType);
      vo.setEventDataGroupId(cid);
      vo.setEventTriggeredRequestId(requestId);
      vo.setEventDataPersonId(caseId);
      vo.setEventDataBeginTime(sdt);
      String res =  objectMapper.writeValueAsString(vo);
      log.info("KAFKA-TOPIC:{}, {}, {}, {}", topic, requestId, caseId, res);
      producer.send(topic,res.getBytes());
      producer.flush();
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  public void sendListKafka(JSONObject initJson , List<VerifyStatusVO> msg )  {
    try {
      for (int k = 0; k < msg.size(); k++) {
        VerifyStatusVO vo = msg.get(k);
        JSONObject content = new JSONObject(vo.getJsonMsg());
        JSONObject body = new JSONObject();
        body.put("requestId", vo.getUniqueId());
        body.put("e2eRequestId", vo.getUniqueId());
        body.put("owner", initJson.getString("owner"));
        body.put("operation", initJson.getString("operation"));
        body.put("to", "DataCore/entities/" + (content.has("id") ? content.getString("id") : ""));
        body.put("contentType", "application/json;type=" + (content.has("type") ? content.getString("type") : "") );
        body.put("queryString", "");
        body.put("eventTime", DateUtil.getTime());
        body.put("content", content);
        byte[] bodyBytes = body.toString().getBytes(Charset.forName("UTF-8"));

        ByteBuffer byteBuffer = ByteBuffer.allocate(bodyBytes.length + 5);
        byte version = 0x10;//4bit: Major version, 4bit: minor version
        Integer bodyLength = bodyBytes.length;//length = 1234
        byteBuffer.put(version);
        byteBuffer.putInt(bodyLength.byteValue());
        byteBuffer.put(bodyBytes);
        Event event = EventBuilder.withBody(byteBuffer.array());
        channelProcessor.processEvent(event);
        if (k % 10 == 0  ) {
          Thread.sleep(1);
        }
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }




}
