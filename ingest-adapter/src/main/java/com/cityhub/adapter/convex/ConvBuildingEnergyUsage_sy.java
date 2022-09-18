package com.cityhub.adapter.convex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvBuildingEnergyUsage_sy extends AbstractConvert {
	
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
							
							JSONObject bems = arr.getJSONObject(j);
							
							 Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
						     Map<String,Object> wMap = new LinkedHashMap<>();
						     
						     Map<String, Object> buildingCode = (Map)tMap.get("buildingCode");
						     buildingCode.put("value", bems.optString("buildingCode"));
						     buildingCode.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> buildingName = (Map)tMap.get("buildingName");
						     buildingName.put("value", bems.optString("buildingName"));
						     buildingCode.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> contolPointName = (Map)tMap.get("contolPointName");
						     contolPointName.put("value", bems.optString("controlPointName"));
						     contolPointName.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> contolPointSequence = (Map)tMap.get("contolPointSequence");
						     contolPointSequence.put("value", bems.optInt("contolPointSequence"));
						     contolPointSequence.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> peak1hour = (Map)tMap.get("peak1hour");
						     peak1hour.put("value", bems.optDouble("peak1hour"));
						     peak1hour.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> usage1hour = (Map)tMap.get("usage1hour");
						     usage1hour.put("value", bems.optDouble("usage1hour"));
						     usage1hour.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> usage00m14m = (Map)tMap.get("usage00m14m");
						     usage00m14m.put("value", bems.optDouble("usage00m14m"));
						     usage00m14m.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> usage15m29m = (Map)tMap.get("usage15m29m");
						     usage15m29m.put("value", bems.optDouble("usage15m29m"));
						     usage15m29m.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> usage30m44m = (Map)tMap.get("usage30m44m");
						     usage30m44m.put("value", bems.optDouble("usage30m44m"));
						     usage30m44m.put("observedAt", DateUtil.getTime());
						     
						     Map<String, Object> usage45m59m = (Map)tMap.get("usage45m59m");
						     usage45m59m.put("value", bems.optDouble("usage45m59m"));
						     usage45m59m.put("observedAt", DateUtil.getTime());
						     
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
						//TODO => 어레이가 아닐때
						JSONObject bems = (JSONObject)ju.getObj("response.body.items.item");
						
						Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					     Map<String,Object> wMap = new LinkedHashMap<>();
					     
					     Map<String, Object> buildingCode = (Map)tMap.get("buildingCode");
					     buildingCode.put("value", bems.optString("buildingCode"));
					     buildingCode.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> buildingName = (Map)tMap.get("buildingName");
					     buildingName.put("value", bems.optString("buildingName"));
					     buildingCode.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> contolPointName = (Map)tMap.get("contolPointName");
					     contolPointName.put("value", bems.optString("controlPointName"));
					     contolPointName.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> contolPointSequence = (Map)tMap.get("contolPointSequence");
					     contolPointSequence.put("value", bems.optInt("contolPointSequence"));
					     contolPointSequence.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> peak1hour = (Map)tMap.get("peak1hour");
					     peak1hour.put("value", bems.optDouble("peak1hour"));
					     peak1hour.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> usage1hour = (Map)tMap.get("usage1hour");
					     usage1hour.put("value", bems.optDouble("usage1hour"));
					     usage1hour.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> usage00m14m = (Map)tMap.get("usage00m14m");
					     usage00m14m.put("value", bems.optDouble("usage00m14m"));
					     usage00m14m.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> usage15m29m = (Map)tMap.get("usage15m29m");
					     usage15m29m.put("value", bems.optDouble("usage15m29m"));
					     usage15m29m.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> usage30m44m = (Map)tMap.get("usage30m44m");
					     usage30m44m.put("value", bems.optDouble("usage30m44m"));
					     usage30m44m.put("observedAt", DateUtil.getTime());
					     
					     Map<String, Object> usage45m59m = (Map)tMap.get("usage45m59m");
					     usage45m59m.put("value", bems.optDouble("usage45m59m"));
					     usage45m59m.put("observedAt", DateUtil.getTime());
					     
					     wMap = (Map) tMap.get("globalLocationNumber");
					     wMap.put("value", iSvc.getString("gs1Code"));
					      
					     wMap = (Map) tMap.get("dataProvider");
					     wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));
					     
					     String id = iSvc.getString("gs1Code");
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
}
