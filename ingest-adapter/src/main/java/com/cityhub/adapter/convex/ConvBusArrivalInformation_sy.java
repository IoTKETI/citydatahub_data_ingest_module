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


import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.RoadType;
import com.cityhub.utils.WeatherType;
import com.cityhub.environment.Constants;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvBusArrivalInformation_sy extends AbstractConvert { 
	
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
  public String doit() throws CoreException{
	  
	  List<Map<String, Object>> rtnList = new LinkedList<>();
	  String rtnStr = "";
	  
	  try {
		  JSONArray svcList = ConfItem.getJSONArray("serviceList");
		  for(int i=0; i<svcList.length(); i++) {
			  JSONObject iSvc = svcList.getJSONObject(i);
			  log.info("iSvc:{}", iSvc);
			  id = iSvc.getString("gs1Code");
			  
			  JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
			  log.info("ju:{}", ju);
			  
			  
			  if(!ju.has("response.msgBody.busArrivalList")) {
				  //throw new CoreException(ErrorCode.NORMAL_ERROR);
				  continue;
			  } else {
				  if(ju.getObj("response.msgBody.busArrivalList") instanceof JSONArray) {
					  JSONArray arr = (JSONArray) ju.getObj("response.msgBody.busArrivalList");
				      
				      for(int j=0; j<arr.length(); j++) {
				    	  Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					      Map<String,Object> wMap = new LinkedHashMap<>();
					      
					      JSONObject bus = arr.getJSONObject(j);
					      
					      Map<String, Object> predictTime1 = (Map)tMap.get("predictTime1");
					      predictTime1.put("value", bus.optInt("predictTime1"));
					      predictTime1.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> predictTime2 = (Map)tMap.get("predictTime2");
					      predictTime2.put("value", bus.optInt("predictTime2"));
					      predictTime2.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> flag = (Map)tMap.get("flag");
					      flag.put("value", bus.optString("flag", null));
					      flag.put("observedAt", DateUtil.getTime());
					      
					      wMap = (Map) tMap.get("globalLocationNumber");
					      wMap.put("value", iSvc.getString("gs1Code"));
					      
					      wMap = (Map) tMap.get("dataProvider");
					      wMap.put("value", iSvc.optString("dataProvider", "https://www.gbis.go.kr"));
					      
					      Map<String, Object> lowPlate1 = (Map)tMap.get("lowPlate1");
					      lowPlate1.put("value", bus.optString("lowPlate1", null));
					      lowPlate1.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> lowPlate2 = (Map)tMap.get("lowPlate2");
					      lowPlate2.put("value", bus.optString("lowPlate2", null));
					      lowPlate2.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> plateNo1 = (Map)tMap.get("plateNo1");
					      plateNo1.put("value", bus.optString("plateNo1", null));
					      plateNo1.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> plateNo2 = (Map)tMap.get("plateNo2");
					      plateNo2.put("value", bus.optString("plateNo2", null));
					      plateNo2.put("observedAt", DateUtil.getTime());
					      
					      Map<String, Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
					      addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
					      addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
					      addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
					      addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
					      addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));
					      
					      Map<String, Object> locMap = (Map)tMap.get("location");
					      locMap.put("observedAt", DateUtil.getTime());
					      Map<String, Object> locValueMap = (Map)locMap.get("value");
					      locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());
					      
					      String id_t = id + "_" + Integer.toString(j);
					      tMap.put("id", id_t);
					  
					      
					      rtnList.add(tMap);
					      String str = objectMapper.writeValueAsString(tMap);
					      log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
				      }
				  } else {
					  JSONObject bus = (JSONObject) ju.getObj("response.msgBody.busArrivalList");
					  log.info("bus:{}", bus);
					  
					  Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
				      Map<String,Object> wMap = new LinkedHashMap<>();
				      
				      Map<String, Object> predictTime1 = (Map)tMap.get("predictTime1");
				      predictTime1.put("value", bus.optInt("predictTime1"));
				      predictTime1.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> predictTime2 = (Map)tMap.get("predictTime2");
				      predictTime2.put("value", bus.optInt("predictTime2"));
				      predictTime2.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> flag = (Map)tMap.get("flag");
				      flag.put("value", bus.optString("flag", null));
				      flag.put("observedAt", DateUtil.getTime());
				      
				      wMap = (Map) tMap.get("globalLocationNumber");
				      wMap.put("value", iSvc.getString("gs1Code"));
				      
				      wMap = (Map) tMap.get("dataProvider");
				      wMap.put("value", iSvc.optString("dataProvider", "https://www.gbis.go.kr"));
				      
				      Map<String, Object> lowPlate1 = (Map)tMap.get("lowPlate1");
				      lowPlate1.put("value", bus.optString("lowPlate1", null));
				      lowPlate1.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> lowPlate2 = (Map)tMap.get("lowPlate2");
				      lowPlate2.put("value", bus.optString("lowPlate2", null));
				      lowPlate2.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> plateNo1 = (Map)tMap.get("plateNo1");
				      plateNo1.put("value", bus.optString("plateNo1", null));
				      plateNo1.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> plateNo2 = (Map)tMap.get("plateNo2");
				      plateNo2.put("value", bus.optString("plateNo2", null));
				      plateNo2.put("observedAt", DateUtil.getTime());
				      
				      Map<String, Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
				      addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
				      addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
				      addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
				      addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
				      addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));
				      
				      Map<String, Object> locMap = (Map)tMap.get("location");
				      locMap.put("observedAt", DateUtil.getTime());
				      Map<String, Object> locValueMap = (Map)locMap.get("value");
				      locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());
				      
				      String id_t = id + "_";
				      tMap.put("id", id_t);
				      
				      rtnList.add(tMap);
				      String str = objectMapper.writeValueAsString(tMap);
				      log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
				  }
			  }
		  }
		  rtnStr = objectMapper.writeValueAsString(rtnList);
		  
	  } catch (CoreException e) {
		  e.printStackTrace();
		  if("!C0099".equals(e.getErrorCode())) {
			  log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
		  log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
		  throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "'" + id, e);
	  }
	  
	  return rtnStr;
  }//end of doit()

}//end of class
