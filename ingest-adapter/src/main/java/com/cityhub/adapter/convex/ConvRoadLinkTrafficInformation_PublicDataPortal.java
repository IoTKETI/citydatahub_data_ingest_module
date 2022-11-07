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
import java.util.LinkedHashMap;
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
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.RoadType;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvRoadLinkTrafficInformation_PublicDataPortal extends AbstractNormalSource {
  private String gettime;

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {

      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        id = iSvc.getString("gs1Code"); // 분리한 데이터의 gs1Code 값을 is에 넣어줌
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        JSONObject item = ju.getObject("ServiceResult.msgBody.itemList");
        Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
        Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
        });

        gettime = item.optString("collDate");

        if (item != null) {

          if (item.has("routeId"))
            Find_wMap(tMap, "routeId").put("value", item.optDouble("routeId", 0));
          else
            Delete_wMap(tMap, "routeId");

          if (item.has("routeNm"))
            Find_wMap(tMap, "routeName").put("value", item.optString("routeNm", ""));
          else
            Delete_wMap(tMap, "routeName");

          if (item.has("spd"))
            Find_wMap(tMap, "speed").put("value", item.optInt("spd", 0));
          else
            Delete_wMap(tMap, "speed");

          if (item.has("vol"))
            Find_wMap(tMap, "trafficVolume").put("value", item.optInt("vol", 0));
          else
            Delete_wMap(tMap, "trafficVolume");

          if (item.has("trvlTime"))
            Find_wMap(tMap, "travelTime").put("value", item.optInt("trvlTime", 0));
          else
            Delete_wMap(tMap, "travelTime");

          if (item.has("linkDelayTime"))
            Find_wMap(tMap, "linkDelayTime").put("value", item.optInt("linkDelayTime", 0));
          else
            Delete_wMap(tMap, "linkDelayTime");

          if (item.has("congGrade"))
            Find_wMap(tMap, "congestedGrade").put("value", RoadType.findBy(item.optInt("congGrade", 0)).getEngNm());
          else
            Delete_wMap(tMap, "congestedGrade");

          id = iSvc.getString("gs1Code");

          wMap = (Map) tMap.get("globalLocationNumber");
          wMap.put("value", id);

          wMap = (Map) tMap.get("dataProvider");
          wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));

          tMap.put("id", id);

          Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
          addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
          addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
          addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
          addrValue.put("addressTown", iSvc.optString("addressTown", ""));
          addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

          Map<String, Object> locMap = (Map) tMap.get("location");
          locMap.put("observedAt", DateUtil.getTime());
          Map<String, Object> locValueMap = (Map) locMap.get("value");
          locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

          tMap.put("id", iSvc.optString("gs1Code"));

          rtnList.add(tMap);
          String str = objectMapper.writeValueAsString(tMap);
          toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
          toLogger(SocketCode.DATA_SAVE_REQ, id, str.getBytes());
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

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
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