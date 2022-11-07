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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractNormalSource;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.WeatherType;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherObserved_SiheungLivingLab extends AbstractNormalSource {
  private String gettime;

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    JSONArray serviceList = ConfItem.getJSONArray("serviceList");
    int IdNumber = 0;
    for (Object sLobj : serviceList) {
      JSONObject _serviceList = (JSONObject) sLobj;
      try {
        JsonUtil gDL = new JsonUtil((JSONObject) CommonUtil.getData(_serviceList, _serviceList.getString("getDeviceList_url_addr") + "&deviceType=S"));
        JSONArray DLList = gDL.getArray("list");
        for (Object DLobj : DLList) {
          if (DLList.length() > 0) {
            JSONObject DLitem = (JSONObject) DLobj;
            IdNumber++;
            String deviceId = DLitem.optString("deviceId").replace(" ", "");
            String deviceType = DLitem.optString("deviceType").replace(" ", "");

            // 현재 시각에서 30분전 데이터를 가져옴
            Calendar cal = Calendar.getInstance();
            cal.setTime(DateUtil.getTimestamp());
            cal.add(Calendar.MINUTE, -30);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String thistime = df.format(cal.getTime().getTime());

            JsonUtil gMVL = new JsonUtil(
                (JSONObject) CommonUtil.getData(_serviceList, _serviceList.getString("getMeasureValueList_url_addr") + "&" + "deviceId" + "=" + deviceId + "&" + "pastReqTime" + "=" + thistime));

            if (gMVL.has("list")) {
              JSONArray MVLList = gMVL.getArray("list");
              if ((MVLList.length() > 0)) {
                for (Object MVLobj : MVLList) {
                  JSONObject MVLitem = (JSONObject) MVLobj;

                  if ((MVLitem.optFloat("lon", 0.0f) == 0.0f) || (MVLitem.optFloat("lat", 0.0f) == 0.0f))
                    continue;

                  Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
                  });
                  Map<String, Object> wMap = new LinkedHashMap<>();

                  gettime = MVLitem.optString("time");

                  if (MVLitem.has("temper")) {
                    Find_wMap(tMap, "temperature").put("value", MVLitem.optDouble("temper"));
                  } else {
                    Delete_wMap(tMap, "temperature");
                  }
                  if (MVLitem.has("ap")) {
                    Find_wMap(tMap, "atmosphericPressure").put("value", MVLitem.optDouble("ap"));
                  } else {
                    Delete_wMap(tMap, "atmosphericPressure");
                  }

                  if (MVLitem.has("windSpeed")) {
                    Find_wMap(tMap, "windSpeed").put("value", MVLitem.optDouble("windSpeed"));
                  } else {
                    Delete_wMap(tMap, "windSpeed");
                  }

                  if (MVLitem.has("rainType")) {
                    Find_wMap(tMap, "rainType").put("value", MVLitem.optString("obsrValue", WeatherType.findBy(MVLitem.optInt("obsrValue", 0)).getEngNm()));
                  } else {
                    Delete_wMap(tMap, "rainType");
                  }

                  if (MVLitem.has("rainfall")) {
                    Find_wMap(tMap, "rainfall").put("value", MVLitem.optDouble("rainfall"));
                  } else {
                    Delete_wMap(tMap, "rainfall");
                  }

                  if (MVLitem.has("hourlyRainfall")) {
                    Find_wMap(tMap, "hourlyRainfall").put("value", MVLitem.optDouble("hourlyRainfall"));
                  } else {
                    Delete_wMap(tMap, "hourlyRainfall");
                  }

                  if (MVLitem.has("snowfall")) {
                    Find_wMap(tMap, "snowfall").put("value", MVLitem.optDouble("snowfall"));
                  } else {
                    Delete_wMap(tMap, "snowfall");
                  }

                  if (MVLitem.has("humid")) {
                    Find_wMap(tMap, "humidity").put("value", MVLitem.optInt("humid"));
                  } else {
                    Delete_wMap(tMap, "humidity");
                  }

                  if (MVLitem.has("visibility")) {
                    Find_wMap(tMap, "visibility").put("value", MVLitem.optDouble("visibility"));
                  } else {
                    Delete_wMap(tMap, "visibility");
                  }

                  id = _serviceList.optString("gs1Code") + "_" + deviceId;

                  wMap = (Map) tMap.get("source");
                  wMap.put("value", "http://www.smartcity-testbed.kr");

                  Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
                  addrValue.put("addressCountry", _serviceList.optString("addressCountry", ""));
                  addrValue.put("addressRegion", _serviceList.optString("addressRegion", ""));
                  addrValue.put("addressLocality", _serviceList.optString("addressLocality", ""));
                  addrValue.put("addressTown", _serviceList.optString("addressTown", ""));
                  addrValue.put("streetAddress", _serviceList.optString("streetAddress", ""));

                  Map<String, Object> locMap = (Map) tMap.get("location");
                  locMap.put("observedAt", DateUtil.getTime());
                  Map<String, Object> locValueMap = (Map) locMap.get("value");

                  ArrayList<Float> location = new ArrayList<>();
                  location.add(MVLitem.optFloat("lon", 0.0f));
                  location.add(MVLitem.optFloat("lat", 0.0f));
                  locValueMap.put("coordinates", location);

                  tMap.put("id", id);
                  rtnList.add(tMap);
                  String str = objectMapper.writeValueAsString(tMap);
                  toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
                  toLogger(SocketCode.DATA_SAVE_REQ, id, str.getBytes());
                }

              }
            }
          }

        }

      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }

    }
    sendEvent(rtnList, ConfItem.getString("datasetId"));
    return "Success";

  }

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    Date date = null;
    try {
      date = df.parse(gettime);
    } catch (ParseException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    cal.setTime(date);
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
    ValueMap.put("observedAt", df2.format(cal.getTime()));
    return ValueMap;
  }

  void Delete_wMap(Map<String, Object> tMap, String Name) {
    tMap.remove(Name);
  }

} // end of class