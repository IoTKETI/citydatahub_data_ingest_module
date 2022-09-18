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
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.environment.Constants;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvHealthWeatherIndex_PublicDataPortal extends AbstractConvert {
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

		String[][] types = { { "asthmaIndex", "getAsthmaIdx" }, { "strokeIndex", "getStrokeIdx" },
				{ "foodPoisonIndex", "getFoodPoisoningIdx" }, { "oakPollenRiskIndex", "getOakPollenRiskIdx" },
				{ "coldIndex", "getColdIdx" }, { "weedsPollenRiskIndex", "getWeedsPollenRiskndx" },
				{ "pinePollenRiskIndex", "getPinePollenRiskIdx" }, { "skinDiseaseIndex", "getSkinDiseaseIdx" } };

		List<Map<String, Object>> rtnList = new LinkedList<>();
		String rtnStr = "";

		try {
			JSONArray svcList = ConfItem.getJSONArray("serviceList");
			// 행정구역별 정보
			for (int i = 0; i < svcList.length(); i++) {
				JSONObject iSvc = svcList.getJSONObject(i);
				id = iSvc.getString("gs1Code");
				Map<String, Object> tMap = objectMapper.readValue(
						templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
						new TypeReference<Map<String, Object>>() {
						});
				Map<String, Object> wMap;

				String urlParamIntro = "?pageNo=1&numOfRows=10&dataType=JSON&";
				JSONObject Tempitem = new JSONObject();
				
				for (int j = 0; j < types.length; j++) {

					String url = iSvc.get("url_addr").toString() + types[j][1] + urlParamIntro;
					JSONObject datum = (JSONObject) CommonUtil.getData(iSvc, url);
					Thread.sleep(500);
					
					if (new JsonUtil(datum).has("response.body.items.item")) {
						log(SocketCode.SOCKET_CONNECT_TRY, id, datum.toString().getBytes());
						JSONArray jArrItem = new JsonUtil(datum).getArray("response.body.items.item");
						JSONObject item = jArrItem.getJSONObject(0);
						if (item != null) {
							int obsvTime = Integer
									.parseInt((new SimpleDateFormat("yyyyMMddHH").format(new Date()).substring(8)));
							String resultVal = "";
							int compareVal;

							if (obsvTime >= 6 && obsvTime < 18) {
								compareVal = item.optInt("today", 0);
							} else {
								compareVal = item.optInt("tomorrow", 0);
							}

							Object[][] foodpoison = { { 0, "attention" }, { 55, "caution" }, { 71, "warning" },
									{ 86, "danger" } };
							Object[][] nomal = { { 0, "low" }, { 1, "normal" }, { 2, "high" }, { 3, "very high" } };

							// 식중독일 경우와 그 외의 경우의 지표 설정
							if ("foodPoisonIndex".equals(types[j][0])) {
								resultVal = ExponentialStage(compareVal, foodpoison);
							} else {
								resultVal = ExponentialStage(compareVal, nomal);
							}

							Tempitem.put(types[j][0], resultVal);

							log(SocketCode.SOCKET_CONNECT, id, Tempitem.toString());
						} else {
							log(SocketCode.SOCKET_CONNECT_FAIL, id);
						}
					} else {
						log(SocketCode.SOCKET_CONNECT_FAIL, id);
					}
				}

				for (int j = 0; j < types.length; j++) {
					if (Tempitem.has(types[j][0])) {
						Find_wMap(tMap, types[j][0]).put("value", Tempitem.get(types[j][0]));
					} else {
						tMap.remove(types[j][0]);
					}
				}

				log(SocketCode.DATA_RECEIVE, id, Tempitem.toString());

				wMap = (Map) tMap.get("dataProvider");
				wMap.put("value", iSvc.optString("dataProvider", "https://www.weather.go.kr"));

				wMap = (Map) tMap.get("globalLocationNumber");
				wMap.put("value", id);

				Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
				addrValue.put("addressCountry", iSvc.optString("addressCountry", "정보없음"));
				addrValue.put("addressRegion", iSvc.optString("addressRegion", "정보없음"));
				addrValue.put("addressLocality", iSvc.optString("addressLocality", "정보없음"));
				addrValue.put("addressTown", iSvc.optString("addressTown", "정보없음"));
				addrValue.put("streetAddress", iSvc.optString("streetAddress", "정보없음"));

				Map<String, Object> locMap = (Map) tMap.get("location");
				locMap.put("observedAt", DateUtil.getTime());
				Map<String, Object> locValueMap = (Map) locMap.get("value");
				locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

				tMap.put("id", id);

				rtnList.add(tMap);
				String str = objectMapper.writeValueAsString(tMap);
				log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
			} // for i end
			rtnStr = objectMapper.writeValueAsString(rtnList);
			if (rtnStr.length() < 10) {
				throw new CoreException(ErrorCode.NORMAL_ERROR);
			}
		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		}
		return rtnStr;
	}

	Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
		Map<String, Object> ValueMap = (Map) tMap.get(Name);
		ValueMap.put("observedAt", DateUtil.getTime());
		return ValueMap;
	}

	String ExponentialStage(Integer Exponential, Object[][] arrList) {

		Integer Min = 0;

		String resultName = "";

		for (Integer i = 0; i < arrList.length; i++) {

			Integer _arrayNumber = (Integer) arrList[i][0];
			String _arrayName = (String) arrList[i][1];

			if ((Exponential >= _arrayNumber) && (_arrayNumber >= Min)) {
				Min = _arrayNumber;
				resultName = _arrayName;

			}
		}

		return resultName;
	}
}
// end of class
