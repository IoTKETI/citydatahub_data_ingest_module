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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.OkUrlUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvSandbox extends AbstractConvert {

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();

    String rtnStr = "";
    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        Object ju = getData("http://192.168.1.204:8082/entities?Type=kr.siheung.citydatahub.AirQualityMeasurement:1.0");
        JSONArray arrList = (JSONArray) ju;

        if (arrList.length() > 0) {
//					for (Object obj : arrList) {
          for (int ii = 0; ii < 1; ii++) {
            Object obj = arrList.get(ii);
            JSONObject item = (JSONObject) obj;
            if (item != null) {
              Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
              });

              log.info("item : " + item);
              log.info("item.optString(\"modifiedAt\") : " + item.optString("modifiedAt"));
              Calendar cal = Calendar.getInstance();
              DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
              Date date = null;
              try {
                date = df.parse(item.optString("modifiedAt"));
              } catch (ParseException e) {
                log.error("Exception : " + ExceptionUtils.getStackTrace(e));
              }
              cal.setTime(date);
              // yyyyMMddHHmmss
              DateFormat df2 = new SimpleDateFormat("yyyy");
              DateFormat df3 = new SimpleDateFormat("MM");
              DateFormat df4 = new SimpleDateFormat("dd");
              DateFormat df5 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
              String Y = df2.format(cal.getTime());
              String M = df3.format(cal.getTime());
              String D = df4.format(cal.getTime());
              // hdfs://192.168.1.182:8020/logs/WeatherMeasurement/%Y/%m/%d
              log.info("hdfs://192.168.1.182:8020/logs/WeatherMeasurement/" + Y + "/" + M + "/" + D);
              // DS_AirQualityMeasurement_AirKorea_Siheung_%Y_%m_%d_%H_%M_%S
              log.info("DS_AirQualityMeasurement_AirKorea_Siheung" + df5.format(cal.getTime()));

              String str = objectMapper.writeValueAsString(tMap);
              toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
            } else {
              toLogger(SocketCode.DATA_CONVERT_FAIL, id);
            } // end if (arrList.length() > 0)
          }
        }

      } // end for
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (CoreException e) {

      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnStr;
  }

  public static Object getData(String url) throws Exception {
    Object obj = null;

    HttpResponse resp = OkUrlUtil.get(url, "Accept", "application/json");
    String payload = resp.getPayload();
    if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301) {
      if (payload.startsWith("{")) {
        obj = new JSONObject(resp.getPayload());
      } else if (payload.startsWith("[")) {
        obj = new JSONArray(resp.getPayload());
      } else if (payload.toLowerCase().startsWith("<")) {
        obj = XML.toJSONObject(resp.getPayload());
      } else {
        obj = resp.getPayload();
      }
    } else {
      throw new Exception(resp.getStatusName());
    }
    return obj;
  }
} // end of class