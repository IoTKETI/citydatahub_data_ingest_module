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
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvOdorObserved_SiheungLivingLab extends AbstractConvert {
  private ObjectMapper objectMapper;
  private String gettime;

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
  }

  @Override
  public String doit() throws CoreException {
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
//						String deviceType = DLitem.optString("deviceType").replace(" ", "");

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

                  if (MVLitem.has("tod")) {
                    Find_wMap(tMap, "tod").put("value", MVLitem.optDouble("tod"));
                  } else {
                    Delete_wMap(tMap, "tod");
                  }
                  if (MVLitem.has("h2s")) {
                    Find_wMap(tMap, "h2s").put("value", MVLitem.getDouble("h2s"));
                  } else {
                    Delete_wMap(tMap, "h2s");
                  }
                  if (MVLitem.has("nh3")) {
                    Find_wMap(tMap, "nh3").put("value", MVLitem.getDouble("nh3"));
                  } else {
                    Delete_wMap(tMap, "nh3");
                  }
                  if (MVLitem.has("vocs")) {
                    Find_wMap(tMap, "voc").put("value", MVLitem.getDouble("vocs"));
                  } else {
                    Delete_wMap(tMap, "vocs");
                  }
                  if (MVLitem.has("ou")) {
                    Find_wMap(tMap, "ou").put("value", MVLitem.getDouble("ou"));
                  } else {
                    Delete_wMap(tMap, "ou");
                  }

                  id = _serviceList.optString("gs1Code") + "_" + deviceId;

                  wMap = (Map) tMap.get("source");
                  wMap.put("value", "http://www.smartcity-testbed.kr");

                  wMap = (Map) tMap.get("refDevice");
//									wMap.put("value", id);
                  wMap.put("object", id);

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

      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }

    }
    try {
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (JsonProcessingException e) {
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

} // end of class