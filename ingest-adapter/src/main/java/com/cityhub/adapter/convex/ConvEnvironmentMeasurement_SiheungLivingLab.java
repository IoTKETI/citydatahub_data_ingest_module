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
import com.cityhub.source.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvEnvironmentMeasurement_SiheungLivingLab extends AbstractConvert {
  private String gettime;

  @Override
  public String doit() throws CoreException {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      JSONArray serviceList = ConfItem.getJSONArray("serviceList");
      for (Object sLobj : serviceList) {
        JSONObject _serviceList = (JSONObject) sLobj;

        JsonUtil gDL = new JsonUtil((JSONObject) CommonUtil.getData(_serviceList, _serviceList.getString("getDeviceList_url_addr")));

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
                log(SocketCode.DATA_RECEIVE, id, gDL.toString().getBytes());

                Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
                });
                Map<String, Object> wMap = new LinkedHashMap<>();

                gettime = MVLitem.optString("time");

                if (MVLitem.has("pm10"))
                  Find_wMap(tMap, "pm10").put("value", MVLitem.optDouble("pm10", 0.0d));
                else
                  Delete_wMap(tMap, "pm10");

                if (MVLitem.has("pm25"))
                  Find_wMap(tMap, "pm25").put("value", MVLitem.optDouble("pm25", 0.0d));
                else
                  Delete_wMap(tMap, "pm25");

                if (MVLitem.has("temper"))
                  Find_wMap(tMap, "temperature").put("value", MVLitem.optDouble("temper", 0.0d));
                else
                  Delete_wMap(tMap, "temperature");

                if (MVLitem.has("humid"))
                  Find_wMap(tMap, "humidity").put("value", MVLitem.optDouble("humid", 0.0d));
                else
                  Delete_wMap(tMap, "humidity");

                if (MVLitem.has("ap"))
                  Find_wMap(tMap, "atmosphericPressure").put("value", MVLitem.optDouble("ap", 0.0d));
                else
                  Delete_wMap(tMap, "atmosphericPressure");

                if (MVLitem.has("tod"))
                  Find_wMap(tMap, "tod").put("value", MVLitem.optDouble("tod", 0.0d));
                else
                  Delete_wMap(tMap, "tod");

                if (MVLitem.has("h2s"))
                  Find_wMap(tMap, "h2s").put("value", MVLitem.optDouble("h2s", 0.0d));
                else
                  Delete_wMap(tMap, "h2s");

                if (MVLitem.has("nh3"))
                  Find_wMap(tMap, "nh3").put("value", MVLitem.optDouble("nh3", 0.0d));
                else
                  Delete_wMap(tMap, "nh3");

                if (MVLitem.has("vocs"))
                  Find_wMap(tMap, "voc").put("value", MVLitem.optDouble("vocs", 0.0d));
                else
                  Delete_wMap(tMap, "voc");

                id = _serviceList.optString("gs1Code");

                Find_wMap(tMap, "deviceType").put("value", Grade_DeviceType(deviceType));

                id += "." + deviceId;

                wMap = (Map) tMap.get("dataProvider");
                wMap.put("value", _serviceList.optString("dataProvider", "http://www.smartcity-testbed.kr"));

                Find_wMap(tMap, "globalLocationNumber").put("value", id);

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
                log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
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
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
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
      id += ".135000.GRA_131231";
      return "Static";
    }
    if ("P".equals(_DeviceType)) {
      id += ".135001.GRA_131231";
      return "Portable";
    }
    if ("M".equals(_DeviceType)) {
      id += ".135002.GRA_131231";
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