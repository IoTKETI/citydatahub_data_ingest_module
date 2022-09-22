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
public class ConvHomeEnergyUsage_sy extends AbstractConvert {

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
					if(ju.getObj("reponse.body.items") instanceof JSONArray) {

						JSONArray arr = (JSONArray) ju.getObj("response.body.items");

						for(int j=0; j<arr.length(); j++) {

							JSONObject hems = arr.getJSONObject(j);

							Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
						    Map<String,Object> wMap = new LinkedHashMap<>();

						    Map<String, Object> blockCode = (Map)tMap.get("blockCode");
						    blockCode.put("value", hems.optString("blockCode"));
						    blockCode.put("observedAt", DateUtil.getTime());

						    Map<String, Object> blockDescription = (Map)tMap.get("blockDescription");
						    blockDescription.put("value", hems.optString("blockDescription"));
						    blockDescription.put("observedAt", DateUtil.getTime());

						    Map<String, Object> buildingCode = (Map)tMap.get("buildingCode");
						    buildingCode.put("value", hems.optString("buildingCode"));
						    buildingCode.put("observedAt", DateUtil.getTime());

						    Map<String, Object> roomCode = (Map)tMap.get("roomCode");
						    roomCode.put("value", hems.optString("roomCode"));
						    roomCode.put("observedAt", DateUtil.getTime());

						    Map<String, Object> usageHotWater = (Map)tMap.get("usageHotWater");
						    usageHotWater.put("value", hems.optDouble("usageHotWater"));
						    usageHotWater.put("observedAt", DateUtil.getTime());

						    Map<String, Object> usageWater = (Map)tMap.get("usageWater");
						    usageWater.put("value", hems.optDouble("usageWater"));
						    usageWater.put("observedAt", DateUtil.getTime());

						    Map<String, Object> usageHeat = (Map)tMap.get("usageHeat");
						    usageHeat.put("value", hems.optDouble("usageHeat"));
						    usageHeat.put("observedAt", DateUtil.getTime());

						    Map<String, Object> usageElectricity = (Map)tMap.get("usageElectricity");
						    usageElectricity.put("value", hems.optDouble("usageElectricity"));
						    usageElectricity.put("observedAt", DateUtil.getTime());

						    Map<String, Object> usageGas = (Map)tMap.get("usageGas");
						    usageGas.put("value", hems.optDouble("usageGas"));
						    usageGas.put("observedAt", DateUtil.getTime());

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

						JSONObject hems = (JSONObject)ju.getObj("response.body.items.item");

						Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
					    Map<String,Object> wMap = new LinkedHashMap<>();

					    Map<String, Object> blockCode = (Map)tMap.get("blockCode");
					    blockCode.put("value", hems.optString("blockCode"));
					    blockCode.put("observedAt", DateUtil.getTime());

					    Map<String, Object> blockDescription = (Map)tMap.get("blockDescription");
					    blockDescription.put("value", hems.optString("blockDescription"));
					    blockDescription.put("observedAt", DateUtil.getTime());

					    Map<String, Object> buildingCode = (Map)tMap.get("buildingCode");
					    buildingCode.put("value", hems.optString("buildingCode"));
					    buildingCode.put("observedAt", DateUtil.getTime());

					    Map<String, Object> roomCode = (Map)tMap.get("roomCode");
					    roomCode.put("value", hems.optString("roomCode"));
					    roomCode.put("observedAt", DateUtil.getTime());

					    Map<String, Object> usageHotWater = (Map)tMap.get("usageHotWater");
					    usageHotWater.put("value", hems.optDouble("usageHotWater"));
					    usageHotWater.put("observedAt", DateUtil.getTime());

					    Map<String, Object> usageWater = (Map)tMap.get("usageWater");
					    usageWater.put("value", hems.optDouble("usageWater"));
					    usageWater.put("observedAt", DateUtil.getTime());

					    Map<String, Object> usageHeat = (Map)tMap.get("usageHeat");
					    usageHeat.put("value", hems.optDouble("usageHeat"));
					    usageHeat.put("observedAt", DateUtil.getTime());

					    Map<String, Object> usageElectricity = (Map)tMap.get("usageElectricity");
					    usageElectricity.put("value", hems.optDouble("usageElectricity"));
					    usageElectricity.put("observedAt", DateUtil.getTime());

					    Map<String, Object> usageGas = (Map)tMap.get("usageGas");
					    usageGas.put("value", hems.optDouble("usageGas"));
					    usageGas.put("observedAt", DateUtil.getTime());

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
			if("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "'" + id, e);
		}

		return rtnStr;
	}
}
