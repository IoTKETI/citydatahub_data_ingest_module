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
public class ConvAirQualityMeasurement_PublicDataPortal extends AbstractConvert {

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

        if (ju.has("response.body.items")) {

          JSONArray arrList = ju.getArray("response.body.items");

          if (arrList.length() > 0) {
            for (Object obj : arrList) {
              JSONObject item = (JSONObject) obj;

              if (item != null) {

                id = iSvc.optString("gs1Code");
                log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());

                Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
                Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
                });

                if (item.has("pm10Value") || item.has("pm10"))
                  Find_wMap(tMap, "pm10").put("value", item.optDouble("pm10Value", item.optDouble("pm10", 0.0d)));
                else
                  Delete_wMap(tMap, "pm10");

                if (item.has("pm25Value") || item.has("pm25"))
                  Find_wMap(tMap, "pm25").put("value", item.optDouble("pm25Value", item.optDouble("pm25", 0.0d)));
                else
                  Delete_wMap(tMap, "pm25");

                if (item.has("so2Value"))
                  Find_wMap(tMap, "so2").put("value", item.optDouble("so2Value", 0.0d));
                else
                  Delete_wMap(tMap, "so2");

                if (item.has("coValue"))
                  Find_wMap(tMap, "co").put("value", item.optDouble("coValue", 0.0d));
                else
                  Delete_wMap(tMap, "co");

                if (item.has("o3Value"))
                  Find_wMap(tMap, "o3").put("value", item.optDouble("o3Value", 0.0d));
                else
                  Delete_wMap(tMap, "o3");

                if (item.has("no2Value"))
                  Find_wMap(tMap, "no2").put("value", item.optDouble("no2Value", 0.0d));
                else
                  Delete_wMap(tMap, "no2");

                if (item.has("pm10Value24"))
                  Find_wMap(tMap, "pm10Hour24").put("value", item.optDouble("pm10Value24", 0.0d));
                else
                  Delete_wMap(tMap, "pm10Hour24");

                if (item.has("pm25Value24"))
                  Find_wMap(tMap, "pm25Hour24").put("value", item.optDouble("pm25Value24", 0.0d));
                else
                  Delete_wMap(tMap, "pm25Hour24");

                if (item.has("khaiValue"))
                  Find_wMap(tMap, "totalIndex").put("value", item.optDouble("khaiValue", 0.0d));
                else
                  Delete_wMap(tMap, "totalIndex");

                if (item.has("khaiGrade"))
                  Find_wMap(tMap, "totalGrade").put("value", Grade(item.optInt("khaiGrade", 0)));
                else
                  Delete_wMap(tMap, "totalGrade");

                if (item.has("so2Grade"))
                  Find_wMap(tMap, "so2Grade").put("value", Grade(item.optInt("so2Grade", 0)));
                else
                  Delete_wMap(tMap, "so2Grade");

                if (item.has("coGrade"))
                  Find_wMap(tMap, "coGrade").put("value", Grade(item.optInt("coGrade", 0)));
                else
                  Delete_wMap(tMap, "coGrade");

                if (item.has("o3Grade"))
                  Find_wMap(tMap, "o3Grade").put("value", Grade(item.optInt("o3Grade", 0)));
                else
                  Delete_wMap(tMap, "o3Grade");

                if (item.has("no2Grade"))
                  Find_wMap(tMap, "no2Grade").put("value", Grade(item.optInt("no2Grade", 0)));
                else
                  Delete_wMap(tMap, "no2Grade");

                if (item.has("pm10Grade"))
                  Find_wMap(tMap, "pm10Grade").put("value", Grade(item.optInt("pm10Grade", 0)));
                else
                  Delete_wMap(tMap, "pm10Grade");

                if (item.has("pm25Grade"))
                  Find_wMap(tMap, "pm25Grade").put("value", Grade(item.optInt("pm25Grade", 0)));
                else
                  Delete_wMap(tMap, "pm25Grade");

                if (item.has("pm10Grade1h"))
                  Find_wMap(tMap, "pm10GradeHour1").put("value", Grade(item.optInt("pm10Grade1h", 0)));
                else
                  Delete_wMap(tMap, "pm10GradeHour1");

                if (item.has("pm25Grade1h"))
                  Find_wMap(tMap, "pm25GradeHour1").put("value", Grade(item.optInt("pm25Grade1h", 0)));
                else
                  Delete_wMap(tMap, "pm25GradeHour1");

                Find_wMap(tMap, "deviceType").put("value", "AirKorea");

                wMap = (Map) tMap.get("dataProvider");
                wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));

                wMap = (Map) tMap.get("globalLocationNumber");
                wMap.put("value", id);

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

                tMap.put("id", id);

                rtnList.add(tMap);
                String str = objectMapper.writeValueAsString(tMap);
                log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
              }
            }
          }
        }
      } // end for
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
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnStr;
  }

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

  void Delete_wMap(Map<String, Object> tMap, String Name) {
    tMap.remove(Name);
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