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

import com.cityhub.core.AbstractNormalSource;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.OkUrlUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvFiwareTest_WeatherMeasurement_Static extends AbstractNormalSource {


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
              Overwrite(tMap, item, "atmosphericPressure");
              Overwrite(tMap, item, "dataProvider");
              Overwrite(tMap, item, "deviceType");
              Overwrite(tMap, item, "globalLocationNumber");
              Overwrite(tMap, item, "humidity");
              Overwrite(tMap, item, "location");
              Overwrite(tMap, item, "temperature");

              DeleteTMap(tMap, item);

              tMap.put("id", item.optString("id"));
              rtnList.add(tMap);
              String str = objectMapper.writeValueAsString(tMap);
              toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
              toLogger(SocketCode.DATA_SAVE_REQ, id, str.getBytes());
            } else {
              toLogger(SocketCode.DATA_CONVERT_FAIL, id);
            } // end if (arrList.length() > 0)
          }
        }

      } // end for
      sendEvent(rtnList, ConfItem.getString("datasetId"));
    } catch (CoreException e) {

      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return "Success";
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
    if (!item.has("altitude"))
      tMap.remove("altitude");
    if (!item.has("windDirection"))
      tMap.remove("windDirection");
    if (!item.has("windSpeed"))
      tMap.remove("windSpeed");
    if (!item.has("temperature"))
      tMap.remove("temperature");
    if (!item.has("humidity"))
      tMap.remove("humidity");
    if (!item.has("atmosphericPressure"))
      tMap.remove("atmosphericPressure");
    if (!item.has("seaLevelPressure"))
      tMap.remove("seaLevelPressure");
    if (!item.has("hourlyRainfall"))
      tMap.remove("hourlyRainfall");
    if (!item.has("dailyRainfall"))
      tMap.remove("dailyRainfall");
    if (!item.has("vaporPressure"))
      tMap.remove("vaporPressure");
    if (!item.has("dewPoint"))
      tMap.remove("dewPoint");
    if (!item.has("sunshine"))
      tMap.remove("sunshine");
    if (!item.has("insolation"))
      tMap.remove("insolation");
    if (!item.has("snowfall"))
      tMap.remove("snowfall");
    if (!item.has("snowfallHour3"))
      tMap.remove("snowfallHour3");
    if (!item.has("visibility"))
      tMap.remove("visibility");
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