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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractNormalSource;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherMeasurement_Aws_Siheung extends AbstractNormalSource {
  private String gettime;


  @Override
  public String doit() {

    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        if (ju.has("AWS1hourObser")) {
          JSONArray _arrList = ju.getArray("AWS1hourObser");

          if (_arrList.length() > 0) {
            for (Object AWS_obj : _arrList) {

              JSONObject AWS_item = (JSONObject) AWS_obj;

              if (AWS_item.has("row")) {
                JSONArray arrList = AWS_item.getJSONArray("row");
                if (arrList.length() > 0) {
                  for (Object obj : arrList) {
                    JSONObject item = (JSONObject) obj;
                    if (item != null) {
                      toLogger(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
                      Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
                      });
                      Map<String, Object> wMap;

                      gettime = item.optString("MESURE_DE") + item.optString("MESURE_TM");

                      if (item.has("HT_INFO") && (item.optDouble("HT_INFO") > -99.0d)) {
                        Find_wMap(tMap, "altitude").put("value", item.optDouble("HT_INFO"));
                      } else {
                        Delete_wMap(tMap, "altitude");
                      }
                      if (item.has("WD_INFO") && (item.optInt("WD_INFO") > -99)) {
                        Find_wMap(tMap, "windDirection").put("value", item.optInt("WD_INFO"));
                      } else {
                        Delete_wMap(tMap, "windDirection");
                      }
                      if (item.has("WS_INFO") && (item.optInt("WS_INFO") > -99)) {
                        Find_wMap(tMap, "windSpeed").put("value", item.optInt("WS_INFO"));
                      } else {
                        Delete_wMap(tMap, "windSpeed");
                      }
                      if (item.has("TP_INFO") && (item.optDouble("TP_INFO") > -99.0d)) {
                        Find_wMap(tMap, "temperature").put("value", item.optDouble("TP_INFO"));
                      } else {
                        Delete_wMap(tMap, "temperature");
                      }
                      if (item.has("HD_INFO") && (item.optInt("HD_INFO") > -99)) {
                        Find_wMap(tMap, "humidity").put("value", item.optInt("HD_INFO"));
                      } else {
                        Delete_wMap(tMap, "humidity");
                      }
                      if (item.has("LOCTN_ATM_INFO") && (item.optDouble("LOCTN_ATM_INFO") > -99.0d)) {
                        Find_wMap(tMap, "atmosphericPressure").put("value", item.optDouble("LOCTN_ATM_INFO"));
                      } else {
                        Delete_wMap(tMap, "atmosphericPressure");
                      }
                      if (item.has("WTRLVL_ATM_INFO") && (item.optDouble("WTRLVL_ATM_INFO") > -99.0d)) {
                        Find_wMap(tMap, "seaLevelPressure").put("value", item.optDouble("WTRLVL_ATM_INFO"));
                      } else {
                        Delete_wMap(tMap, "seaLevelPressure");
                      }

                      // test할때만 숫자로 값 push 나중에 변환
                      if (item.has("RAINF_YN_INFO") && (Double.parseDouble(item.optString("RAINF_YN_INFO")) > -99.0d)) {
                        Find_wMap(tMap, "rainfall").put("value", item.optString("RAINF_YN_INFO"));
                      } else {
                        Delete_wMap(tMap, "rainfall");
                      }

                      if (item.has("RAINF_1HR_INFO") && (item.optDouble("RAINF_1HR_INFO") > -99.0d)) {
                        Find_wMap(tMap, "hourlyRainfall").put("value", item.optDouble("RAINF_1HR_INFO"));
                      } else {
                        Delete_wMap(tMap, "hourlyRainfall");
                      }
                      if (item.has("RAINF_DAY_INFO") && (item.optDouble("RAINF_DAY_INFO") > -99.0d)) {
                        Find_wMap(tMap, "dailyRainfall").put("value", item.optDouble("RAINF_DAY_INFO"));
                      } else {
                        Delete_wMap(tMap, "dailyRainfall");
                      }

                      if (item.has("vaporPressure")) {
                        Find_wMap(tMap, "vaporPressure").put("value", item.optDouble("vaporPressure"));
                      } else {
                        Delete_wMap(tMap, "vaporPressure");
                      }
                      if (item.has("dewPoint")) {
                        Find_wMap(tMap, "dewPoint").put("value", item.optDouble("dewPoint"));
                      } else {
                        Delete_wMap(tMap, "dewPoint");
                      }
                      if (item.has("sunshine")) {
                        Find_wMap(tMap, "sunshine").put("value", item.optDouble("sunshine"));
                      } else {
                        Delete_wMap(tMap, "sunshine");
                      }
                      if (item.has("insolation")) {
                        Find_wMap(tMap, "insolation").put("value", item.optDouble("insolation"));
                      } else {
                        Delete_wMap(tMap, "insolation");
                      }
                      if (item.has("snowfall")) {
                        Find_wMap(tMap, "snowfall").put("value", item.optDouble("snowfall"));
                      } else {
                        Delete_wMap(tMap, "snowfall");
                      }
                      if (item.has("snowfallHour3")) {
                        Find_wMap(tMap, "snowfallHour3").put("value", item.optDouble("snowfallHour3"));
                      } else {
                        Delete_wMap(tMap, "snowfallHour3");
                      }
                      if (item.has("visibility")) {
                        Find_wMap(tMap, "visibility").put("value", item.optDouble("visibility"));
                      } else {
                        Delete_wMap(tMap, "visibility");
                      }

                      Find_wMap(tMap, "deviceType").put("value", item.optString("deviceType", "Static"));

                      wMap = (Map) tMap.get("globalLocationNumber");
                      wMap.put("value", id);

                      wMap = (Map) tMap.get("dataProvider");
                      wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));

                      tMap.put("id", id);

                      String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());
                      tMap.put("createdAt", createdAt);

                      Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
                      addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
                      addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
                      addrValue.put("addressLocality", item.optString("SIGUN_NM", ""));
                      addrValue.put("addressTown", item.optString("SPOT_NM", ""));
                      addrValue.put("streetAddress", item.optString("LEGALDONG_NM", ""));

                      ArrayList<Double> coordinates = new ArrayList<>();

                      coordinates.add(item.optDouble("WGS84_LOGT", 0.0d));
                      coordinates.add(item.optDouble("WGS84_LAT", 0.0d));

                      Map<String, Object> locMap = (Map) tMap.get("location");
                      locMap.put("observedAt", createdAt);
                      Map<String, Object> locValueMap = (Map) locMap.get("value");
                      locValueMap.put("coordinates", coordinates);

                      rtnList.add(tMap);
                      String str = objectMapper.writeValueAsString(tMap);
                      toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
                      toLogger(SocketCode.DATA_SAVE_REQ, id, str.getBytes());
                    }
                  }
                }
              }
            }
          }
        }
      }
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

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyyMMddHH");
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

  protected String getTimeInfo(LocalDate date, LocalTime time) {

    LocalDateTime dt = LocalDateTime.of(date, time);
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

    return returnValue;
  }
}
// end of class