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
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAirObserved_PublicDataPortal extends AbstractNormalSource {

  @Override
  public String doit() {
    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {

      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      int IdNumber = 0;
      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        JSONArray arrList = ju.getArray("response.body.items");

        if (arrList.length() > 0) {
          for (Object obj : arrList) {
            JSONObject item = (JSONObject) obj;
            if (item != null) {
              Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
              Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
              });

              tMap.put("id", iSvc.getString("gs1Code"));

              Find_wMap(tMap, "so2").put("value", item.optDouble("so2Value", 0.0d));
              Find_wMap(tMap, "co").put("value", item.optDouble("coValue", 0.0d));
              Find_wMap(tMap, "o3").put("value", item.optDouble("o3Value", 0.0d));
              Find_wMap(tMap, "no2").put("value", item.optDouble("no2Value", 0.0d));
              Find_wMap(tMap, "pm10").put("value", item.optInt("pm10Value", 0));
              Find_wMap(tMap, "pm25").put("value", item.optInt("pm25Value", 0));
              Find_wMap(tMap, "pm1").put("value", item.optInt("pm10Grade1h", 0));
              Find_wMap(tMap, "co2").put("value", item.optDouble("co2Value", 0.0d));
              Find_wMap(tMap, "airQualityIndex").put("value", item.optInt("khaiValue", 0));

              id = iSvc.getString("gs1Code") + "_" + String.format("%03d", IdNumber++);

              wMap = (Map) tMap.get("source");
              wMap.put("value", "http://www.smartcity-testbed.kr");

              wMap = (Map) tMap.get("refDevice");
              wMap.put("value", id);

              Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
              addrValue.put("addressCountry", iSvc.optString("addressCountry", "정보없음"));
              addrValue.put("addressRegion", iSvc.optString("addressRegion", "정보없음"));
              addrValue.put("addressLocality", iSvc.optString("addressLocality", "정보없음"));
              addrValue.put("addressTown", iSvc.optString("addressTown", "정보없음"));
              addrValue.put("streetAddress", iSvc.optString("streetAddress", "정보없음"));

              Map<String, Object> locMap = (Map) tMap.get("location");
              locMap.put("observedAt", DateUtil.getTime());
              Map<String, Object> locValueMap = (Map) locMap.get("value");
              locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

              tMap.put("id", id);

              log.info("tMap : " + tMap);
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

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

} // end of class
