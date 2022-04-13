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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class ConvWeatherMeasurement_Legacy_by2 extends AbstractConvert {

	private ObjectMapper objectMapper;
	
	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
		this.objectMapper = new ObjectMapper();
	    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
	    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
	    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));
	}

	@Override
	public String doit() throws CoreException {

		List<Map<String,Object>> rtnList = new LinkedList<>();  
		String rtnStr =""; 
		
		try {
		      JSONArray svcList = ConfItem.getJSONArray("serviceList");
		      
		      for (int i = 0; i < svcList.length(); i++) {
		        JSONObject iSvc = svcList.getJSONObject(i);
		         
		        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
		         
		        JSONArray _arrList = ju.getArray("AWS1hourObser");
		        
		        for(Object AWS_obj : _arrList) {
		        	
		        	JSONObject AWS_item = (JSONObject) AWS_obj;
		        	
		        	if(AWS_item.has("row") ) {
		        	JSONArray arrList = AWS_item.getJSONArray("row");

			          for (Object obj : arrList) { 
			        	  Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
			        	  
					      JSONObject item = (JSONObject) obj;
					      

					      if (item.has("HT_INFO")) {
								Find_wMap(tMap, "altitude").put("value", item.optDouble("HT_INFO"));
							} else {
								Delete_wMap(tMap, "altitude");
							}
					      if (item.has("WD_INFO")) {
								Find_wMap(tMap, "windDirection").put("value", item.optInt("WD_INFO"));
							} else {
								Delete_wMap(tMap, "windDirection");
							}
					      if (item.has("WS_INFO")) {
								Find_wMap(tMap, "windSpeed").put("value", item.optInt("WS_INFO"));
							} else {
								Delete_wMap(tMap, "windSpeed");
							}
					      if (item.has("TP_INFO")) {
								Find_wMap(tMap, "temperature").put("value", item.optDouble("TP_INFO"));
							} else {
								Delete_wMap(tMap, "temperature");
							}
					      if (item.has("HD_INFO")) {
								Find_wMap(tMap, "humidity").put("value", item.optInt("HD_INFO"));
							} else {
								Delete_wMap(tMap, "humidity");
							}
					      if (item.has("LOCTN_ATM_INFO")) {
								Find_wMap(tMap, "atmosphericPressure").put("value", item.optDouble("LOCTN_ATM_INFO"));
							} else {
								Delete_wMap(tMap, "atmosphericPressure");
							}
					      if (item.has("WTRLVL_ATM_INFO")) {
								Find_wMap(tMap, "seaLevelPressure").put("value", item.optDouble("WTRLVL_ATM_INFO"));
							} else {
								Delete_wMap(tMap, "seaLevelPressure");
							}
					      
					      // test할때만 숫자로 값 push 나중에 변환
					      if (item.has("RAINF_YN_INFO")) {
								Find_wMap(tMap, "rainfall").put("value", item.optString("RAINF_YN_INFO"));
							} else {
								Delete_wMap(tMap, "rainfall");
							}
					      
					      if (item.has("RAINF_1HR_INFO")) {
								Find_wMap(tMap, "hourlyRainfall").put("value", item.optDouble("RAINF_1HR_INFO"));
							} else {
								Delete_wMap(tMap, "hourlyRainfall");
							}
					      if (item.has("RAINF_DAY_INFO")) {
								Find_wMap(tMap, "dailyRainfall").put("value", item.optDouble("RAINF_DAY_INFO"));
							} else {
								Delete_wMap(tMap, "dailyRainfall");
							}
					     
					      				      
					      
					      if (item.has("deviceType")) {
								Find_wMap(tMap, "deviceType").put("value", item.optString("deviceType"));
							} else {
								Delete_wMap(tMap, "deviceType");
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
					      				      
					      if (iSvc.has("globalLocationNumber")) 
								Find_wMap(tMap, "globalLocationNumber").put("value", iSvc.get("globalLocationNumber"));
					      Map<String, Object> dp_wMap = (Map) tMap.get("dataProvider");
					      dp_wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));
					      
					      tMap.put("id", iSvc.get("id"));
					      
				          String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());
				          tMap.put("createdAt", createdAt );
				          
				          Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
							addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
							addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
							addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
							addrValue.put("addressTown", iSvc.optString("addressTown", ""));
							addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));
									        
				          ArrayList<Float> coordinates = new ArrayList<Float>();
				          coordinates.add(0f);
				          coordinates.add(0f);
							
				          Map<String,Object> locMap = (Map)tMap.get("location");
				          locMap.put("observedAt",createdAt);
				          Map<String,Object> locValueMap  = (Map)locMap.get("value");
				          locValueMap.put("coordinates", coordinates);
				          		
						  rtnList.add(tMap);
			               	  
		          }            
		        } 	        
		      }
		    }
		         
		      rtnStr = objectMapper.writeValueAsString(rtnList);
		      
		    } catch (CoreException e) {
		        e.printStackTrace();
		        if ("!C0099".equals(e.getErrorCode())) {
		          log(SocketCode.DATA_CONVERT_FAIL, id,  e.getMessage());
		        }
		      } catch (Exception e) {
		        e.printStackTrace();
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
}
// end of class