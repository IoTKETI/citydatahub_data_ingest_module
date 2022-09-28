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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWeatherMeasurement_PublicDataPortal extends AbstractConvert {

  private ObjectMapper objectMapper;

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
//		StringBuffer sendJson = new StringBuffer();
    List<Map<String, Object>> rtnList = new LinkedList<>(); // buffer대신 List로 데이터 받을예정
    String rtnStr = ""; // list로 받은것 string으로 변환해서 적재할거임

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      int IdNumber = 0;
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        log.info("ju : " + ju);

        if (ju.has("response.body.items.item")) {
          JSONArray arrList = ju.getArray("response.body.items.item");

          for (Object obj : arrList) { // array를 json object에 못넣기때문에 object에 넣고 다시 옮기는방식
            Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> wMap = new LinkedHashMap<>();

            JSONObject item = (JSONObject) obj;

            Find_wMap(tMap, "vaporPressure").put("value", item.optDouble("pv", 0.0d));
            Find_wMap(tMap, "dewPoint").put("value", item.optDouble("td", 0.0d));
            Find_wMap(tMap, "sunshine").put("value", item.optInt("ss", 0));
            Find_wMap(tMap, "insolation").put("value", item.optDouble("icsr", 0.0d));
            Find_wMap(tMap, "snowfall").put("value", item.optDouble("dsnw", 0.0d));
            Find_wMap(tMap, "snowfallHour3").put("value", item.optDouble("hr3Fhsc", 0.0d));
            Find_wMap(tMap, "visibility").put("value", item.optInt("vs", 0));
            Find_wMap(tMap, "altitude").put("value", item.optDouble("HT_INFO", 0.0d));
            Find_wMap(tMap, "windDirection").put("value", item.optInt("WD_INFO", 0));
            Find_wMap(tMap, "windSpeed").put("value", item.optInt("WS_INFO", 0));
            Find_wMap(tMap, "temperature").put("value", item.optDouble("TP_INFO", 0.0d));
            Find_wMap(tMap, "humidity").put("value", item.optDouble("HD_INFO", 0.0d));
            Find_wMap(tMap, "atmosphericPressure").put("value", item.optDouble("LOCTN_ATM_INFO", 0.0d));
            Find_wMap(tMap, "seaLevelPressure").put("value", item.optDouble("WTRLVL_ATM_INFO", 0.0d));

            if (item.has("WTRLVL_ATM_INFO")) {
              int value = item.getInt("WTRLVL_ATM_INFO");
              switch (value) {
              case 0:
                Find_wMap(tMap, "rainfall").put("value", item.optString("WTRLVL_ATM_INFO", "없음"));
                break;
              case 1:
                Find_wMap(tMap, "rainfall").put("value", item.optString("WTRLVL_ATM_INFO", "있음"));
                break;
              case 2:
                Find_wMap(tMap, "rainfall").put("value", item.optString("WTRLVL_ATM_INFO", "오류"));
                break;
              }
            }

            Find_wMap(tMap, "hourlyRainfall").put("value", item.optDouble("RAINF_1HR_INFO", 0.0d));
            Find_wMap(tMap, "dailyRainfall").put("value", item.optDouble("RAINF_DAY_INFO", 0.0d));
            Find_wMap(tMap, "deviceType").put("value", item.optString("deviceType", "없음"));

            id = iSvc.optString("gs1Code");

            wMap = (Map) tMap.get("dataProvider");
            wMap.put("value", iSvc.optString("dataProvider", "http://www.smartcity-testbed.kr"));

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

      rtnStr = objectMapper.writeValueAsString(rtnList);

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
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

}
// end of class