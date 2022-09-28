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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
public class ConvHealthMeasurement extends AbstractConvert {
  private ObjectMapper objectMapper;

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) { // templateItem 데이터모델
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

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < svcList.length(); i++) {

        JSONObject iSvc = svcList.getJSONObject(i);
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

        if (ju.has("list")) {

          JSONArray arrList = ju.getArray("list");

          for (Object obj : arrList) { // array를 json object에 못넣기때문에 object에 넣고 다시 옮기는방식
            Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> wMap = new LinkedHashMap<>();

            JSONObject item = (JSONObject) obj;

            if (item.has("height")) {
              Find_wMap(tMap, "height").put("value", item.optString("height"));
            } else {
              tMap.remove("height");
            }
            if (item.has("weight")) {
              Find_wMap(tMap, "weight").put("value", item.optString("weight"));
            } else {
              tMap.remove("weight");
            }
            if (item.has("fatRate")) {
              Find_wMap(tMap, "fatRate").put("value", item.optString("fatRate"));
            } else {
              tMap.remove("fatRate");
            }
            if (item.has("muscle")) {
              Find_wMap(tMap, "muscle").put("value", item.optString("muscle"));
            } else {
              tMap.remove("muscle");
            }
            if (item.has("fatLevel")) {
              Find_wMap(tMap, "fatLevel").put("value", item.optString("fatLevel"));
            } else {
              tMap.remove("fatLevel");
            }
            if (item.has("kcal")) {
              Find_wMap(tMap, "kcal").put("value", item.optString("kcal"));
            } else {
              tMap.remove("kcal");
            }
            if (item.has("boneVolume")) {
              Find_wMap(tMap, "boneVolume").put("value", item.optString("boneVolume"));
            } else {
              tMap.remove("boneVolume");
            }
            if (item.has("water")) {
              Find_wMap(tMap, "water").put("value", item.optString("water"));
            } else {
              tMap.remove("water");
            }
            if (item.has("waterRate")) {
              Find_wMap(tMap, "waterRate").put("value", item.optString("waterRate"));
            } else {
              tMap.remove("waterRate");
            }
            if (item.has("bmi")) {
              Find_wMap(tMap, "bmi").put("value", item.optString("bmi"));
            } else {
              tMap.remove("bmi");
            }
            if (item.has("fat")) {
              Find_wMap(tMap, "fat").put("value", item.optString("fat"));
            } else {
              tMap.remove("fat");
            }
            if (item.has("abdominalObesityRate")) {
              Find_wMap(tMap, "abdominalObesityRate").put("value", item.optString("abdominalObesityRate"));
            } else {
              tMap.remove("abdominalObesityRate");
            }
            if (item.has("waist")) {
              Find_wMap(tMap, "waist").put("value", item.optString("waist"));
            } else {
              tMap.remove("waist");
            }
            if (item.has("weightControl")) {
              Find_wMap(tMap, "weightControl").put("value", item.optString("weightControl"));
            } else {
              tMap.remove("weightControl");
            }
            if (item.has("fatControl")) {
              Find_wMap(tMap, "fatControl").put("value", item.optString("fatControl"));
            } else {
              tMap.remove("fatControl");
            }
            if (item.has("muscleControl")) {
              Find_wMap(tMap, "muscleControl").put("value", item.optString("muscleControl"));
            } else {
              tMap.remove("muscleControl");
            }
            if (item.has("physicalDevelopment")) {
              Find_wMap(tMap, "physicalDevelopment").put("value", item.optString("physicalDevelopment"));
            } else {
              tMap.remove("physicalDevelopment");
            }
            if (item.has("maxBloodPressure")) {
              Find_wMap(tMap, "maxBloodPressure").put("value", item.optString("maxBloodPressure"));
            } else {
              tMap.remove("maxBloodPressure");
            }
            if (item.has("minBloodPressure")) {
              Find_wMap(tMap, "minBloodPressure").put("value", item.optString("minBloodPressure"));
            } else {
              tMap.remove("minBloodPressure");
            }
            if (item.has("pulse")) {
              Find_wMap(tMap, "pulse").put("value", item.optString("pulse"));
            } else {
              tMap.remove("pulse");
            }
            if (item.has("beforeBloodSugar")) {
              Find_wMap(tMap, "beforeBloodSugar").put("value", item.optString("beforeBloodSugar"));
            } else {
              tMap.remove("beforeBloodSugar");
            }
            if (item.has("afterBloodSugar")) {
              Find_wMap(tMap, "afterBloodSugar").put("value", item.optString("afterBloodSugar"));
            } else {
              tMap.remove("afterBloodSugar");
            }
            if (item.has("cholesterol")) {
              Find_wMap(tMap, "cholesterol").put("value", item.optString("cholesterol"));
            } else {
              tMap.remove("cholesterol");
            }
            if (item.has("triglyceride")) {
              Find_wMap(tMap, "triglyceride").put("value", item.optString("triglyceride"));
            } else {
              tMap.remove("triglyceride");
            }
            if (item.has("hdlc")) {
              Find_wMap(tMap, "hdlc").put("value", item.optString("hdlc"));
            } else {
              tMap.remove("hdlc");
            }
            if (item.has("ldlc")) {
              Find_wMap(tMap, "ldlc").put("value", item.optString("ldlc"));
            } else {
              tMap.remove("ldlc");
            }
            if (item.has("stressScore")) {
              Find_wMap(tMap, "stressScore").put("value", item.optString("stressScore"));
            } else {
              tMap.remove("stressScore");
            }
            if (item.has("physicalStress")) {
              Find_wMap(tMap, "physicalStress").put("value", item.optString("physicalStress"));
            } else {
              tMap.remove("physicalStress");
            }
            if (item.has("mentalStress")) {
              Find_wMap(tMap, "mentalStress").put("value", item.optString("mentalStress"));
            } else {
              tMap.remove("mentalStress");
            }
            if (item.has("stressManagemenet")) {
              Find_wMap(tMap, "stressManagemenet").put("value", item.optString("stressManagemenet"));
            } else {
              tMap.remove("stressManagemenet");
            }
            if (item.has("vascularPhase")) {
              Find_wMap(tMap, "vascularPhase").put("value", item.optString("vascularPhase"));
            } else {
              tMap.remove("vascularPhase");
            }
            if (item.has("cardiacOutput")) {
              Find_wMap(tMap, "cardiacOutput").put("value", item.optString("cardiacOutput"));
            } else {
              tMap.remove("cardiacOutput");
            }
            if (item.has("vascularElasticity")) {
              Find_wMap(tMap, "vascularElasticity").put("value", item.optString("vascularElasticity"));
            } else {
              tMap.remove("vascularElasticity");
            }
            if (item.has("remainedBloodVolume")) {
              Find_wMap(tMap, "remainedBloodVolume").put("value", item.optString("remainedBloodVolume"));
            } else {
              tMap.remove("remainedBloodVolume");
            }

            if (iSvc.has("globalLocationNumber"))
              Find_wMap(tMap, "globalLocationNumber").put("value", iSvc.get("globalLocationNumber"));

            if (iSvc.has("data_Provider"))
              Find_wMap(tMap, "dataProvider").put("value", iSvc.get("dataProvider"));

            tMap.put("id", iSvc.get("id"));

            String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());

            Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
            addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
            addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
            addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
            addrValue.put("addressTown", iSvc.optString("addressTown", ""));
            addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

            Map<String, Object> locMap = (Map) tMap.get("location");
            locMap.put("observedAt", createdAt);
            Map<String, Object> locValueMap = (Map) locMap.get("value");
            locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

            rtnList.add(tMap);
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

  protected String getTimeInfo(LocalDate date, LocalTime time) {

    LocalDateTime dt = LocalDateTime.of(date, time);
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

    return returnValue;
  }
} // end of class
