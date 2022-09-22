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
public class ConvHealthMeasurement_by extends AbstractConvert {
	private ObjectMapper objectMapper;

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) { // templateItem 데이터모델
    super.setup(ConfItem, templateItem);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));
  }

  @Override
  public String doit() throws CoreException {

	  List<Map<String,Object>> rtnList = new LinkedList<>();
		String rtnStr = "";

	    try {
	      JSONArray svcList = ConfItem.getJSONArray("serviceList");

	      for (int i = 0; i < svcList.length(); i++) {

	        JSONObject iSvc = svcList.getJSONObject(i);
	        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

	        if (ju.has("list")) {

	          JSONArray arrList = ju.getArray("list");

	          for (Object obj : arrList) { //array를 json object에 못넣기때문에 object에 넣고 다시 옮기는방식
	        	  Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
	              Map<String,Object> wMap = new LinkedHashMap<>();

	        	  JSONObject item = (JSONObject) obj;

	        	  if (item.has("sex")) {
						Find_wMap(tMap, "sex").put("value", item.optString("sex"));
					} else {
						Delete_wMap(tMap, "sex");
					}
	        	  if (item.has("age")) {
						Find_wMap(tMap, "age").put("value", item.optInt("age"));
					} else {
						Delete_wMap(tMap, "age");
					}
	        	  if (item.has("height")) {
						Find_wMap(tMap, "height").put("value", item.optDouble("height"));
					} else {
						Delete_wMap(tMap, "height");
					}
	        	  if (item.has("weight")) {
						Find_wMap(tMap, "weight").put("value", item.optDouble("weight"));
					} else {
						Delete_wMap(tMap, "weight");
					}
	        	  if (item.has("bodyFatRate")) {
						Find_wMap(tMap, "bodyFatRate").put("value", item.optDouble("bodyFatRate"));
					} else {
						Delete_wMap(tMap, "bodyFatRate");
					}
	        	  if (item.has("muscle")) {
						Find_wMap(tMap, "muscle").put("value", item.optDouble("muscle"));
					} else {
						Delete_wMap(tMap, "muscle");
					}
	        	  if (item.has("visceralFatLevel")) {
						Find_wMap(tMap, "visceralFatLevel").put("value", item.optDouble("visceralFatLevel"));
					} else {
						Delete_wMap(tMap, "visceralFatLevel");
					}
	        	  if (item.has("kcal")) {
						Find_wMap(tMap, "kcal").put("value", item.optDouble("kcal"));
					} else {
						Delete_wMap(tMap, "kcal");
					}
	        	  if (item.has("estimatedBoneVolume")) {
						Find_wMap(tMap, "estimatedBoneVolume").put("value", item.optDouble("estimatedBoneVolume"));
					} else {
						Delete_wMap(tMap, "estimatedBoneVolume");
					}
	        	  if (item.has("bodyWaterRate")) {
						Find_wMap(tMap, "bodyWaterRate").put("value", item.optDouble("bodyWaterRate"));
					} else {
						Delete_wMap(tMap, "bodyWaterRate");
					}
	        	  if (item.has("BMI")) {
						Find_wMap(tMap, "BMI").put("value", item.optDouble("BMI"));
					} else {
						Delete_wMap(tMap, "BMI");
					}
	        	  if (item.has("bodyFat")) {
						Find_wMap(tMap, "bodyFat").put("value", item.optDouble("bodyFat"));
					} else {
						Delete_wMap(tMap, "bodyFat");
					}
	        	  if (item.has("abdominalObesityRate")) {
						Find_wMap(tMap, "abdominalObesityRate").put("value", item.optDouble("abdominalObesityRate"));
					} else {
						Delete_wMap(tMap, "abdominalObesityRate");
					}
	        	  if (item.has("waist")) {
						Find_wMap(tMap, "waist").put("value", item.optDouble("waist"));
					} else {
						Delete_wMap(tMap, "waist");
					}
	        	  if (item.has("bodyFat")) {
						Find_wMap(tMap, "weightControl").put("value", item.optDouble("weightControl"));
					} else {
						Delete_wMap(tMap, "weightControl");
					}
	        	  if (item.has("fatControl")) {
						Find_wMap(tMap, "fatControl").put("value", item.optDouble("fatControl"));
					} else {
						Delete_wMap(tMap, "fatControl");
					}
	        	  if (item.has("muscleControl")) {
						Find_wMap(tMap, "muscleControl").put("value", item.optDouble("muscleControl"));
					} else {
						Delete_wMap(tMap, "muscleControl");
					}
	        	  if (item.has("physicalDevelopment")) {
						Find_wMap(tMap, "physicalDevelopment").put("value", item.optDouble("physicalDevelopment"));
					} else {
						Delete_wMap(tMap, "physicalDevelopment");
					}
	        	  if (item.has("maxBloodPressure")) {
						Find_wMap(tMap, "maxBloodPressure").put("value", item.optDouble("maxBloodPressure"));
					} else {
						Delete_wMap(tMap, "maxBloodPressure");
					}
	        	  if (item.has("minBloodPressure")) {
						Find_wMap(tMap, "minBloodPressure").put("value", item.optDouble("minBloodPressure"));
					} else {
						Delete_wMap(tMap, "minBloodPressure");
					}
	        	  if (item.has("pulse")) {
						Find_wMap(tMap, "pulse").put("value", item.optDouble("pulse"));
					} else {
						Delete_wMap(tMap, "pulse");
					}
	        	  if (item.has("beforeBloodSugar")) {
						Find_wMap(tMap, "beforeBloodSugar").put("value", item.optDouble("beforeBloodSugar"));
					} else {
						Delete_wMap(tMap, "beforeBloodSugar");
					}
	        	  if (item.has("afterBloodSugar")) {
						Find_wMap(tMap, "afterBloodSugar").put("value", item.optDouble("afterBloodSugar"));
					} else {
						Delete_wMap(tMap, "afterBloodSugar");
					}
	        	  if (item.has("cholesterol")) {
						Find_wMap(tMap, "cholesterol").put("value", item.optDouble("cholesterol"));
					} else {
						Delete_wMap(tMap, "cholesterol");
					}
	        	  if (item.has("triglyceride")) {
						Find_wMap(tMap, "triglyceride").put("value", item.optDouble("triglyceride"));
					} else {
						Delete_wMap(tMap, "triglyceride");
					}
	        	  if (item.has("HDLC")) {
						Find_wMap(tMap, "HDLC").put("value", item.optDouble("HDLC"));
					} else {
						Delete_wMap(tMap, "HDLC");
					}
	        	  if (item.has("LDLC")) {
						Find_wMap(tMap, "LDLC").put("value", item.optDouble("LDLC"));
					} else {
						Delete_wMap(tMap, "LDLC");
					}
	        	  if (item.has("gameStartTime")) {
						Find_wMap(tMap, "gameStartTime").put("value", item.optDouble("gameStartTime"));
					} else {
						Delete_wMap(tMap, "gameStartTime");
					}
	        	  if (item.has("gameLevel")) {
						Find_wMap(tMap, "gameLevel").put("value", item.optDouble("gameLevel"));
					} else {
						Delete_wMap(tMap, "gameLevel");
					}
	        	  if (item.has("gameStep")) {
						Find_wMap(tMap, "gameStep").put("value", item.optDouble("gameStep"));
					} else {
						Delete_wMap(tMap, "gameStep");
					}
	        	  if (item.has("gameScore")) {
						Find_wMap(tMap, "gameScore").put("value", item.optDouble("gameScore"));
					} else {
						Delete_wMap(tMap, "gameScore");
					}
	        	  if (item.has("gamePlayTime")) {
						Find_wMap(tMap, "gamePlayTime").put("value", item.optDouble("gamePlayTime"));
					} else {
						Delete_wMap(tMap, "gamePlayTime");
					}
	        	  if (item.has("alarmTime")) {
						Find_wMap(tMap, "alarmTime").put("value", item.optDouble("alarmTime"));
					} else {
						Delete_wMap(tMap, "alarmTime");
					}
	        	  if (item.has("alarmMonday")) {
						Find_wMap(tMap, "alarmMonday").put("value", item.optDouble("alarmMonday"));
					} else {
						Delete_wMap(tMap, "alarmMonday");
					}
	        	  if (item.has("alarmTuesday")) {
						Find_wMap(tMap, "alarmTuesday").put("value", item.optDouble("alarmTuesday"));
					} else {
						Delete_wMap(tMap, "alarmTuesday");
					}
	        	  if (item.has("alarmWednesday")) {
						Find_wMap(tMap, "alarmWednesday").put("value", item.optDouble("alarmWednesday"));
					} else {
						Delete_wMap(tMap, "alarmWednesday");
					}
	        	  if (item.has("alarmThursday")) {
						Find_wMap(tMap, "alarmThursday").put("value", item.optDouble("alarmThursday"));
					} else {
						Delete_wMap(tMap, "alarmThursday");
					}
	        	  if (item.has("alarmFriday")) {
						Find_wMap(tMap, "alarmFriday").put("value", item.optDouble("alarmFriday"));
					} else {
						Delete_wMap(tMap, "alarmFriday");
					}
	        	  if (item.has("alarmSaturday")) {
						Find_wMap(tMap, "alarmSaturday").put("value", item.optDouble("alarmSaturday"));
					} else {
						Delete_wMap(tMap, "alarmSaturday");
					}
	        	  if (item.has("alarmSunday")) {
						Find_wMap(tMap, "alarmSunday").put("value", item.optDouble("alarmSunday"));
					} else {
						Delete_wMap(tMap, "alarmSunday");
					}
	        	  if (item.has("alarmType")) {
						Find_wMap(tMap, "alarmType").put("value", item.optDouble("alarmType"));
					} else {
						Delete_wMap(tMap, "alarmType");
					}
	        	  if (item.has("medicationTime")) {
						Find_wMap(tMap, "medicationTime").put("value", item.optDouble("medicationTime"));
					} else {
						Delete_wMap(tMap, "medicationTime");
					}
	        	  if (item.has("medicationType")) {
						Find_wMap(tMap, "medicationType").put("value", item.optDouble("medicationType"));
					} else {
						Delete_wMap(tMap, "medicationType");
					}
	        	  if (item.has("medicationComment")) {
						Find_wMap(tMap, "medicationComment").put("value", item.optDouble("medicationComment"));
					} else {
						Delete_wMap(tMap, "medicationComment");
					}
	        	  if (item.has("medicationAfterMeal")) {
						Find_wMap(tMap, "medicationAfterMeal").put("value", item.optDouble("medicationAfterMeal"));
					} else {
						Delete_wMap(tMap, "medicationAfterMeal");
					}
	        	  if (item.has("medicationBeforeMeal")) {
						Find_wMap(tMap, "medicationBeforeMeal").put("value", item.optDouble("medicationBeforeMeal"));
					} else {
						Delete_wMap(tMap, "medicationBeforeMeal");
					}

	        	  if (iSvc.has("globalLocationNumber"))
						Find_wMap(tMap, "globalLocationNumber").put("value", iSvc.get("globalLocationNumber"));

	        	  if (iSvc.has("data_Provider"))
						Find_wMap(tMap, "dataProvider").put("value", iSvc.get("dataProvider"));

			      tMap.put("id", iSvc.get("id"));

		          String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());

		          Map<String,Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
		          addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
				  addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
				  addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
				  addrValue.put("addressTown", iSvc.optString("addressTown", ""));
				  addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

		          Map<String,Object> locMap = 	(Map)tMap.get("location");
		          locMap.put("observedAt",createdAt );
		          Map<String,Object> locValueMap  = (Map)locMap.get("value");
		          locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

			  rtnList.add(tMap);
	          }
	        }
	      }

	      rtnStr = objectMapper.writeValueAsString(rtnList);

	    } catch (CoreException e) {
	        if ("!C0099".equals(e.getErrorCode())) {
	          log(SocketCode.DATA_CONVERT_FAIL, id,  e.getMessage());
	        }
	      } catch (Exception e) {
	        log(SocketCode.DATA_CONVERT_FAIL,  id,  e.getMessage());
	        throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
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
