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
public class ConvWaterDaily_sy extends AbstractConvert {
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
			 
			 JSONObject temp = ju.getObject("response.body");
			 String numOfRows = temp.optString("numOfRows", "");
			 
			 if(ju.getObj("response.body.items").toString().equals("")) {
				 continue;
			 } else {
				 if(ju.getObj("response.body.items.item") instanceof JSONArray) {
					 
					 JSONArray arr = (JSONArray) ju.getObj("response.body.items.item");
					 
					 for(int j=0; j<arr.length(); j++) {
						 Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					     Map<String,Object> wMap = new LinkedHashMap<>();
					     
					     JSONObject waterday = arr.getJSONObject(j);
					     
					     Map<String, Object> taste = (Map)tMap.get("taste");
					     taste.put("value", waterday.optString("item1"));
					     taste.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> odor = (Map)tMap.get("odor");
					     odor.put("value", waterday.optString("item2"));
					     odor.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> chromaticity = (Map)tMap.get("chromaticity");
					     chromaticity.put("value", waterday.optDouble("item3"));
					     chromaticity.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> hydrogenIndex = (Map)tMap.get("hydrogenIndex");
					     hydrogenIndex.put("value", waterday.optDouble("item4"));
					     hydrogenIndex.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> turbidity = (Map)tMap.get("turbidity");
					     turbidity.put("value", waterday.optDouble("item5"));
					     turbidity.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> residualChlorine = (Map)tMap.get("residualChlorine");
					     residualChlorine.put("value", waterday.optDouble("item6"));
					     residualChlorine.put("observedAt", DateUtil.getTime());
					     
					     id = iSvc.getString("gs1Code");
					     
					     wMap = (Map) tMap.get("globalLocationNumber");
					     wMap.put("value", id);
					      
					     wMap = (Map) tMap.get("dataProvider");
					     wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));
					     
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
					 //TODO=>제이슨어레이가 아닐때
					 JSONObject waterday = (JSONObject)ju.getObj("response.body.items.item");
					 
					 Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
				     Map<String,Object> wMap = new LinkedHashMap<>();
				     
				     Map<String, Object> taste = (Map)tMap.get("taste");
				     taste.put("value", waterday.optString("item1"));
				     taste.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> odor = (Map)tMap.get("odor");
				     odor.put("value", waterday.optString("item2"));
				     odor.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> chromaticity = (Map)tMap.get("chromaticity");
				     chromaticity.put("value", waterday.optDouble("item3"));
				     chromaticity.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> hydrogenIndex = (Map)tMap.get("hydrogenIndex");
				     hydrogenIndex.put("value", waterday.optDouble("item4"));
				     hydrogenIndex.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> turbidity = (Map)tMap.get("turbidity");
				     turbidity.put("value", waterday.optDouble("item5"));
				     turbidity.put("observedAt", DateUtil.getTime());
				     
				     Map<String, Object> residualChlorine = (Map)tMap.get("residualChlorine");
				     residualChlorine.put("value", waterday.optDouble("item6"));
				     residualChlorine.put("observedAt", DateUtil.getTime());
				     
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
		 throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "'" + id , e);
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
	
} // end of class
