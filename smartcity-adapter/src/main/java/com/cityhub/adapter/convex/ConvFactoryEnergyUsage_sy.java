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
public class ConvFactoryEnergyUsage_sy extends AbstractConvert {
	
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

			for (int i = 0; i < svcList.length(); i++) {
				JSONObject iSvc = svcList.getJSONObject(i);
				log.info("iSvc:{}", iSvc);

				JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
				log.info("ju:{}", ju);
				
				if(!ju.has("response.msgBody.busArrivalList")) {
					continue;
				} else {
					if(ju.getObj("response.msgBody.busArrivalList") instanceof JSONArray) {
						 
						JSONArray arr = (JSONArray) ju.getObj("response.msgBody.busArrivalList");
						
						for(int j=0; j<arr.length(); j++) {
							Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
						    Map<String,Object> wMap = new LinkedHashMap<>();
						    
						    JSONObject fems = arr.getJSONObject(j);
						    
						    Map<String, Object> factoryCode = (Map)tMap.get("factoryCode");
						    factoryCode.put("value", fems.optString("factoryCode"));
						    factoryCode.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> usage00m14m = (Map)tMap.get("usage00m14m");
						    usage00m14m.put("value", fems.optDouble("usage00m14m"));
						    usage00m14m.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> usage30m44m = (Map)tMap.get("usage30m44m");
						    usage30m44m.put("value", fems.optDouble("usage00m14m"));
						    usage30m44m.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> usage15m29m = (Map)tMap.get("usage15m29m");
						    usage15m29m.put("value", fems.optDouble("usage15m29m"));
						    usage15m29m.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> usage45m59m = (Map)tMap.get("usage45m59m");
						    usage45m59m.put("value", fems.optDouble("usage45m59m"));
						    usage45m59m.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> contolPointName = (Map)tMap.get("contolPointName");
						    contolPointName.put("value", fems.optString("contolPointName"));
						    contolPointName.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> factoryName = (Map)tMap.get("factoryName");
						    factoryName.put("value", fems.optString("factoryName"));
						    factoryName.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> peak1hour = (Map)tMap.get("peak1hour");
						    peak1hour.put("value", fems.optDouble("peak1hour"));
						    peak1hour.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> usage1hour = (Map)tMap.get("usage1hour");
						    usage1hour.put("value", fems.optDouble("usage1hour"));
						    usage1hour.put("observedAt", DateUtil.getTime());
						    
						    Map<String, Object> contolPointSequence = (Map)tMap.get("contolPointSequence");
						    contolPointSequence.put("value", fems.optDouble("contolPointSequence"));
						    contolPointSequence.put("observedAt", DateUtil.getTime());
						    
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
						//TODO => ???????????? ?????????
						JSONObject fems = (JSONObject)ju.getObj("response.body.items.item");
						
						Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					    Map<String,Object> wMap = new LinkedHashMap<>();
					    
					    Map<String, Object> factoryCode = (Map)tMap.get("factoryCode");
					    factoryCode.put("value", fems.optString("factoryCode"));
					    factoryCode.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> usage00m14m = (Map)tMap.get("usage00m14m");
					    usage00m14m.put("value", fems.optDouble("usage00m14m"));
					    usage00m14m.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> usage30m44m = (Map)tMap.get("usage30m44m");
					    usage30m44m.put("value", fems.optDouble("usage00m14m"));
					    usage30m44m.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> usage15m29m = (Map)tMap.get("usage15m29m");
					    usage15m29m.put("value", fems.optDouble("usage15m29m"));
					    usage15m29m.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> usage45m59m = (Map)tMap.get("usage45m59m");
					    usage45m59m.put("value", fems.optDouble("usage45m59m"));
					    usage45m59m.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> contolPointName = (Map)tMap.get("contolPointName");
					    contolPointName.put("value", fems.optString("contolPointName"));
					    contolPointName.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> factoryName = (Map)tMap.get("factoryName");
					    factoryName.put("value", fems.optString("factoryName"));
					    factoryName.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> peak1hour = (Map)tMap.get("peak1hour");
					    peak1hour.put("value", fems.optDouble("peak1hour"));
					    peak1hour.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> usage1hour = (Map)tMap.get("usage1hour");
					    usage1hour.put("value", fems.optDouble("usage1hour"));
					    usage1hour.put("observedAt", DateUtil.getTime());
					    
					    Map<String, Object> contolPointSequence = (Map)tMap.get("contolPointSequence");
					    contolPointSequence.put("value", fems.optDouble("contolPointSequence"));
					    contolPointSequence.put("observedAt", DateUtil.getTime());
					    
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

		} catch (CoreException e) {
			e.printStackTrace();
			if ("!C0099".equals(e.getErrorCode())) {
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
