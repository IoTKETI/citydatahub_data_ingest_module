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
public class ConvWaterIndDaily_sy extends AbstractConvert {
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
	 
	 List<Map<String, Object>> rtnList = new LinkedList<>();
	 String rtnStr = "";
	 
	 try {
		 JSONArray svcList = ConfItem.getJSONArray("serviceList");
		 
		 for(int i=0; i<svcList.length(); i++) {
			 JSONObject iSvc = svcList.getJSONObject(i);
			 log.info("iSvc:{}", iSvc);
			 
			 JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
			 log.info("ju:{}", ju);
			 
			 if(ju.getObj("response.body.items").toString().equals("")) {
				 continue;
			 } else {
				 
				 if(ju.getObj("response.body.items.item") instanceof JSONArray) {
					 JSONArray arr = (JSONArray) ju.getObj("response.body.items.item");
					 
					 for(int j=0; j<arr.length(); j++) {
						 
						 JSONObject waterind = arr.getJSONObject(j);
						 
						 Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					     Map<String,Object> wMap = new LinkedHashMap<>();
					     
					     log.info("tMap:{}", tMap);
					     
					     Map<String, Object> temperatureOfSource = (Map)tMap.get("temperatureOfSource");
					     temperatureOfSource.put("value", waterind.optInt("item1"));
					     temperatureOfSource.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> temperatureOfPrecipitation = (Map)tMap.get("temperatureOfPrecipitation");
					     temperatureOfPrecipitation.put("value", waterind.optInt("item2"));
					     temperatureOfPrecipitation.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> hydrogenIndexOfSource = (Map)tMap.get("hydrogenIndexOfSource");
					     hydrogenIndexOfSource.put("value", waterind.optDouble("item3"));
					     hydrogenIndexOfSource.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> hydrogenIndexOfPrecipitation = (Map)tMap.get("hydrogenIndexOfPrecipitation");
					     hydrogenIndexOfPrecipitation.put("value", waterind.optDouble("item4"));
					     hydrogenIndexOfPrecipitation.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> turbidityOfSource = (Map)tMap.get("turbidityOfSource");
					     turbidityOfSource.put("value", waterind.optDouble("item5"));
					     turbidityOfSource.put("observedAt", DateUtil.getTime());
					     
					     Map<String ,Object> turbidityOfPrecipitation = (Map)tMap.get("turbidityOfPrecipitation");
					     turbidityOfPrecipitation.put("value", waterind.optDouble("item6"));
					     turbidityOfPrecipitation.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> conductivityOfSource = (Map)tMap.get("conductivityOfSource");
					     conductivityOfSource.put("value", waterind.optInt("item7"));
					     conductivityOfSource.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> conductivityOfPrecipitation = (Map)tMap.get("conductivityOfPrecipitation");
					     conductivityOfPrecipitation.put("value", waterind.optInt("item8"));
					     conductivityOfPrecipitation.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> alkaliOfSource = (Map)tMap.get("alkaliOfSource");
					     alkaliOfSource.put("value", waterind.optInt("item9"));
					     alkaliOfSource.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> alkaliOfPrecipitation = (Map)tMap.get("alkaliOfPrecipitation");
					     alkaliOfPrecipitation.put("value", waterind.optInt("item10"));
					     alkaliOfPrecipitation.put("observedAt", DateUtil.getTime());
					     
					     wMap = (Map) tMap.get("globalLocationNumber");
					     wMap.put("value", iSvc.getString("gs1Code"));
					      
					     wMap = (Map) tMap.get("dataProvider");
					     wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));
					     
					     String id = iSvc.getString("gs1Code");
					     id += "_" + Integer.toString(j);
					     tMap.put("id", id);
					     
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
					     
					     rtnList.add(tMap);
					     String str = objectMapper.writeValueAsString(tMap);
					     log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
					 }
				 } else {
					 JSONObject waterind = (JSONObject) ju.getObj("response.body.items.item");
					 
					 Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
				     Map<String,Object> wMap = new LinkedHashMap<>();
				     
				     log.info("tMap:{}", tMap);
				     
				     Map<String, Object> temperatureOfSource = (Map)tMap.get("temperatureOfSource");
				     temperatureOfSource.put("value", waterind.optInt("item1"));
				     temperatureOfSource.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> temperatureOfPrecipitation = (Map)tMap.get("temperatureOfPrecipitation");
				     temperatureOfPrecipitation.put("value", waterind.optInt("item2"));
				     temperatureOfPrecipitation.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> hydrogenIndexOfSource = (Map)tMap.get("hydrogenIndexOfSource");
				     hydrogenIndexOfSource.put("value", waterind.optDouble("item3"));
				     hydrogenIndexOfSource.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> hydrogenIndexOfPrecipitation = (Map)tMap.get("hydrogenIndexOfPrecipitation");
				     hydrogenIndexOfPrecipitation.put("value", waterind.optDouble("item4"));
				     hydrogenIndexOfPrecipitation.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> turbidityOfSource = (Map)tMap.get("turbidityOfSource");
				     turbidityOfSource.put("value", waterind.optDouble("item5"));
				     turbidityOfSource.put("observedAt", DateUtil.getTime());
				     
				     Map<String ,Object> turbidityOfPrecipitation = (Map)tMap.get("turbidityOfPrecipitation");
				     turbidityOfPrecipitation.put("value", waterind.optDouble("item6"));
				     turbidityOfPrecipitation.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> conductivityOfSource = (Map)tMap.get("conductivityOfSource");
				     conductivityOfSource.put("value", waterind.optInt("item7"));
				     conductivityOfSource.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> conductivityOfPrecipitation = (Map)tMap.get("conductivityOfPrecipitation");
				     conductivityOfPrecipitation.put("value", waterind.optInt("item8"));
				     conductivityOfPrecipitation.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> alkaliOfSource = (Map)tMap.get("alkaliOfSource");
				     alkaliOfSource.put("value", waterind.optInt("item9"));
				     alkaliOfSource.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> alkaliOfPrecipitation = (Map)tMap.get("alkaliOfPrecipitation");
				     alkaliOfPrecipitation.put("value", waterind.optInt("item10"));
				     alkaliOfPrecipitation.put("observedAt", DateUtil.getTime());
				     
				     wMap = (Map) tMap.get("globalLocationNumber");
				     wMap.put("value", iSvc.getString("gs1Code"));
				      
				     wMap = (Map) tMap.get("dataProvider");
				     wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));
				     
				     tMap.put("id", iSvc.getString("gs1Code"));
				     
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
 }
} // end of class
