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

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAirQualityMeasurement_Portable_SiheungLivingLab extends AbstractConvert {
  private String gettime;

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");
      for (Object sLobj : serviceList) {
        JSONObject _serviceList = (JSONObject) sLobj;

        JsonUtil gDL = new JsonUtil((JSONObject) CommonUtil.getData(_serviceList, _serviceList.getString("getDeviceList_url_addr") + "&deviceType=" + _serviceList.getString("deviceType")));
        if (gDL.has("list")) {
          JSONArray DLList = gDL.getArray("list");
          for (Object DLobj : DLList) {
            JSONObject DLitem = (JSONObject) DLobj;
            String deviceId = DLitem.optString("deviceId").replace(" ", "");
            id = _serviceList.optString("gs1Code") + "." + deviceId;
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
              for (Object MVLobj : MVLList) {
                JSONObject MVLitem = (JSONObject) MVLobj;

                if ((MVLitem.optFloat("lon", 0.0f) == 0.0f) || (MVLitem.optFloat("lat", 0.0f) == 0.0f)) {
                  continue;
                }

                toLogger(SocketCode.DATA_RECEIVE, id, gDL.toString().getBytes());

                Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> wMap = new LinkedHashMap<>();

                gettime = MVLitem.optString("time");

                if (MVLitem.has("pm10Value") || MVLitem.has("pm10"))
                  Find_wMap(tMap, "pm10").put("value", MVLitem.optDouble("pm10Value", MVLitem.optDouble("pm10", 0.0d)));
                else
                  Delete_wMap(tMap, "pm10");

                if (MVLitem.has("pm25Value") || MVLitem.has("pm25"))
                  Find_wMap(tMap, "pm25").put("value", MVLitem.optDouble("pm25Value", MVLitem.optDouble("pm25", 0.0d)));
                else
                  Delete_wMap(tMap, "pm25");

                if (MVLitem.has("so2Value"))
                  Find_wMap(tMap, "so2").put("value", MVLitem.optDouble("so2Value", 0.0d));
                else
                  Delete_wMap(tMap, "so2");

                if (MVLitem.has("coValue"))
                  Find_wMap(tMap, "co").put("value", MVLitem.optDouble("coValue", 0.0d));
                else
                  Delete_wMap(tMap, "co");

                if (MVLitem.has("o3Value"))
                  Find_wMap(tMap, "o3").put("value", MVLitem.optDouble("o3Value", 0.0d));
                else
                  Delete_wMap(tMap, "o3");

                if (MVLitem.has("no2Value"))
                  Find_wMap(tMap, "no2").put("value", MVLitem.optDouble("no2Value", 0.0d));
                else
                  Delete_wMap(tMap, "no2");

                if (MVLitem.has("pm10Value24"))
                  Find_wMap(tMap, "pm10Hour24").put("value", MVLitem.optDouble("pm10Value24", 0.0d));
                else
                  Delete_wMap(tMap, "pm10Hour24");

                if (MVLitem.has("pm25Value24"))
                  Find_wMap(tMap, "pm25Hour24").put("value", MVLitem.optDouble("pm25Value24", 0.0d));
                else
                  Delete_wMap(tMap, "pm25Hour24");

                if (MVLitem.has("khaiValue"))
                  Find_wMap(tMap, "totalIndex").put("value", MVLitem.optDouble("khaiValue", 0.0d));
                else
                  Delete_wMap(tMap, "totalIndex");

                if (MVLitem.has("khaiGrade"))
                  Find_wMap(tMap, "totalGrade").put("value", Grade(MVLitem.optInt("khaiGrade", 0)));
                else
                  Delete_wMap(tMap, "totalGrade");

                if (MVLitem.has("so2Grade"))
                  Find_wMap(tMap, "so2Grade").put("value", Grade(MVLitem.optInt("so2Grade", 0)));
                else
                  Delete_wMap(tMap, "so2Grade");

                if (MVLitem.has("coGrade"))
                  Find_wMap(tMap, "coGrade").put("value", Grade(MVLitem.optInt("coGrade", 0)));
                else
                  Delete_wMap(tMap, "coGrade");

                if (MVLitem.has("o3Grade"))
                  Find_wMap(tMap, "o3Grade").put("value", Grade(MVLitem.optInt("o3Grade", 0)));
                else
                  Delete_wMap(tMap, "o3Grade");

                if (MVLitem.has("no2Grade"))
                  Find_wMap(tMap, "no2Grade").put("value", Grade(MVLitem.optInt("no2Grade", 0)));
                else
                  Delete_wMap(tMap, "no2Grade");

                if (MVLitem.has("pm10Grade"))
                  Find_wMap(tMap, "pm10Grade").put("value", Grade(MVLitem.optInt("pm10Grade", 0)));
                else
                  Delete_wMap(tMap, "pm10Grade");

                if (MVLitem.has("pm25Grade"))
                  Find_wMap(tMap, "pm25Grade").put("value", Grade(MVLitem.optInt("pm25Grade", 0)));
                else
                  Delete_wMap(tMap, "pm25Grade");

                if (MVLitem.has("pm10Grade1h"))
                  Find_wMap(tMap, "pm10GradeHour1").put("value", Grade(MVLitem.optInt("pm10Grade1h", 0)));
                else
                  Delete_wMap(tMap, "pm10GradeHour1");

                if (MVLitem.has("pm25Grade1h"))
                  Find_wMap(tMap, "pm25GradeHour1").put("value", Grade(MVLitem.optInt("pm25Grade1h", 0)));
                else
                  Delete_wMap(tMap, "pm25GradeHour1");

                Find_wMap(tMap, "deviceType").put("value", Grade_DeviceType(deviceType));

                wMap = (Map) tMap.get("dataProvider");
                wMap.put("value", _serviceList.optString("dataProvider", "https://www.weather.go.kr"));

                wMap = (Map) tMap.get("globalLocationNumber");
                wMap.put("value", id);

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
              }
            }
          }
        }
      }
      rtnStr = objectMapper.writeValueAsString(rtnList);
      if (rtnStr.length() < 10) {
        throw new CoreException(ErrorCode.NORMAL_ERROR);
      }

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

  public String Grade_DeviceType(String _DeviceType) {
    if ("S".equals(_DeviceType)) {
      return "Static";
    }
    if ("P".equals(_DeviceType)) {
      return "Portable";
    }
    if ("M".equals(_DeviceType)) {
      return "Move";
    }
    return "타입없음";
  }

  public String Grade(int _grade) {
    if (_grade == 1) {
      return "좋음";
    }
    if (_grade == 2) {
      return "보통";
    }
    if (_grade == 3) {
      return "나쁨";
    }
    if (_grade == 4) {
      return "매우나쁨";
    }
    return "정보없음";
  }

} // end of class