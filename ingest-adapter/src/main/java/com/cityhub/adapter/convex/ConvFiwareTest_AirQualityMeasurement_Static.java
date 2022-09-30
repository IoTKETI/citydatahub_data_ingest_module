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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.cityhub.exception.CoreException;
import com.cityhub.source.core.AbstractConvert;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.OkUrlUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvFiwareTest_AirQualityMeasurement_Static extends AbstractConvert {

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();

    String rtnStr = "";
    try {
//			JSONArray svcList = ConfItem.getJSONArray("serviceList");
//			for (int i = 0; i < svcList.length(); i++) {
      for (int i = 0; i < 1; i++) {

//				JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        Object ju = getData("http://192.168.1.179:1026/v2/entities?type=AirQualityMeasurement_Static");
        JSONArray arrList = (JSONArray) ju;

        if (arrList.length() > 0) {
          for (Object obj : arrList) {
            JSONObject item = (JSONObject) obj;
            if (item != null) {
              Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
              });

              Overwrite(tMap, item, "address");
              Overwrite(tMap, item, "dataProvider");
              Overwrite(tMap, item, "deviceType");
              Overwrite(tMap, item, "globalLocationNumber");
              Overwrite(tMap, item, "location");
              Overwrite(tMap, item, "pm10");
              Overwrite(tMap, item, "pm25");

              DeleteTMap(tMap, item);

              tMap.put("id", item.optString("id"));
              rtnList.add(tMap);
              String str = objectMapper.writeValueAsString(tMap);
              log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
            } else {
              log(SocketCode.DATA_CONVERT_FAIL, id);
            } // end if (arrList.length() > 0)
          }
        }

      } // end for
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (CoreException e) {

      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnStr;
  }

  public void Overwrite(Map<String, Object> tMap, JSONObject item, String name) {
    Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
    wMap = (Map) tMap.get(name);
    try {
      wMap.putAll(objectMapper.readValue(item.getJSONObject(name).getJSONObject("value").toString(), new TypeReference<Map<String, Object>>() {
      }));
    } catch (JSONException | IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  void DeleteTMap(Map<String, Object> tMap, JSONObject item) {
    if (!item.has("so2"))
      tMap.remove("so2");
    if (!item.has("co"))
      tMap.remove("co");
    if (!item.has("o3"))
      tMap.remove("o3");
    if (!item.has("no2"))
      tMap.remove("no2");
    if (!item.has("pm10"))
      tMap.remove("pm10");
    if (!item.has("pm25"))
      tMap.remove("pm25");
    if (!item.has("pm10Hour24"))
      tMap.remove("pm10Hour24");
    if (!item.has("pm25Hour24"))
      tMap.remove("pm25Hour24");
    if (!item.has("totalIndex"))
      tMap.remove("totalIndex");
    if (!item.has("totalGrade"))
      tMap.remove("totalGrade");
    if (!item.has("so2Grade"))
      tMap.remove("so2Grade");
    if (!item.has("coGrade"))
      tMap.remove("coGrade");
    if (!item.has("o3Grade"))
      tMap.remove("o3Grade");
    if (!item.has("no2Grade"))
      tMap.remove("no2Grade");
    if (!item.has("pm10Grade"))
      tMap.remove("pm10Grade");
    if (!item.has("pm25Grade"))
      tMap.remove("pm25Grade");
    if (!item.has("pm10GradeHour1"))
      tMap.remove("pm10GradeHour1");
    if (!item.has("pm25GradeHour1"))
      tMap.remove("pm25GradeHour1");
    if (!item.has("deviceType"))
      tMap.remove("deviceType");
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