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
import java.util.ArrayList;
import java.util.Date;
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
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvHomeEnergyUsage_by extends AbstractConvert {
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
	               
        	  if (item.has("blockCode")) {
					Find_wMap(tMap, "blockCode").put("value", item.optString("blockCode"));
				} else {
					Delete_wMap(tMap, "blockCode");
				}
        	  if (item.has("blockDescription")) {
					Find_wMap(tMap, "blockDescription").put("value", item.optString("blockDescription"));
				} else {
					Delete_wMap(tMap, "blockDescription");
				}
        	  if (item.has("buildingCode")) {
					Find_wMap(tMap, "buildingCode").put("value", item.optInt("buildingCode"));
				} else {
					Delete_wMap(tMap, "buildingCode");
				}
        	  if (item.has("roomCode")) {
					Find_wMap(tMap, "roomCode").put("value", item.optInt("roomCode"));
				} else {
					Delete_wMap(tMap, "roomCode");
				}
        	  if (item.has("usageHotWater")) {
					Find_wMap(tMap, "usageHotWater").put("value", item.optInt("usageHotWater"));
				} else {
					Delete_wMap(tMap, "usageHotWater");
				}
        	  if (item.has("usageHeat")) {
					Find_wMap(tMap, "usageHeat").put("value", item.optDouble("usageHeat"));
				} else {
					Delete_wMap(tMap, "usageHeat");
				}
        	  if (item.has("usageElectricity")) {
					Find_wMap(tMap, "usageElectricity").put("value", item.optDouble("usageElectricity"));
				} else {
					Delete_wMap(tMap, "usageElectricity");
				}
        	  if (item.has("usageGas")) {
					Find_wMap(tMap, "usageGas").put("value", item.optDouble("usageGas"));
				} else {
					Delete_wMap(tMap, "usageGas");
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
	} // end of class
