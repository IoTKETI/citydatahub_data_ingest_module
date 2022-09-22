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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
public class ConvWeatherMeasurement_Portable_SiheungLivingLab extends AbstractConvert {
	private ObjectMapper objectMapper;
	private String gettime;

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
			JSONArray serviceList = ConfItem.getJSONArray("serviceList");
			for (Object sLobj : serviceList) {
				JSONObject _serviceList = (JSONObject) sLobj;

				JsonUtil gDL = new JsonUtil(
						(JSONObject) CommonUtil.getData(_serviceList, _serviceList.getString("getDeviceList_url_addr")
								+ "&deviceType=" + _serviceList.getString("deviceType")));
				if (gDL.has("list")) {
					JSONArray DLList = gDL.getArray("list");
					for (Object DLobj : DLList) {
						JSONObject DLitem = (JSONObject) DLobj;
						String deviceId = DLitem.optString("deviceId").replace(" ", "");
						id = _serviceList.optString("gs1Code") + "." + deviceId;
						String deviceType = DLitem.optString("deviceType").replace(" ", "");

						// 현재 시각에서 30분전 데이터를 가져옴
						Calendar cal = Calendar.getInstance();
						cal.setTime(DateUtil.getTimestamp());
						cal.add(Calendar.MINUTE, -30);
						DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
						String thistime = df.format(cal.getTime().getTime());

						JsonUtil gMVL = new JsonUtil((JSONObject) CommonUtil.getData(_serviceList,
								_serviceList.getString("getMeasureValueList_url_addr") + "&" + "deviceId" + "="
										+ deviceId + "&" + "pastReqTime" + "=" + thistime));

						if (gMVL.has("list")) {
							JSONArray MVLList = gMVL.getArray("list");
							for (Object MVLobj : MVLList) {
								JSONObject MVLitem = (JSONObject) MVLobj;

								if ((MVLitem.optFloat("lon", 0.0f) == 0.0f)
										|| (MVLitem.optFloat("lat", 0.0f) == 0.0f)) {
									continue;
								}

								log(SocketCode.DATA_RECEIVE, id, gDL.toString().getBytes());

								Map<String, Object> tMap = objectMapper.readValue(
										templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
										new TypeReference<Map<String, Object>>() {
										});
								Map<String, Object> wMap = new LinkedHashMap<>();

								gettime = MVLitem.optString("time");

								if (MVLitem.has("altitude"))
									Find_wMap(tMap, "altitude").put("value", MVLitem.optDouble("altitude", 0.0d));
								else
									Delete_wMap(tMap, "altitude");

								if (MVLitem.has("windDirection"))
									Find_wMap(tMap, "windDirection").put("value",
											MVLitem.optDouble("windDirection", 0.0d));
								else
									Delete_wMap(tMap, "windDirection");
								;

								if (MVLitem.has("windSpeed"))
									Find_wMap(tMap, "windSpeed").put("value", MVLitem.optDouble("windSpeed", 0.0d));
								else
									Delete_wMap(tMap, "windSpeed");

								if (MVLitem.has("temper"))
									Find_wMap(tMap, "temperature").put("value", MVLitem.optDouble("temper", 0.0d));
								else
									Delete_wMap(tMap, "temperature");

								if (MVLitem.has("humid"))
									Find_wMap(tMap, "humidity").put("value", MVLitem.optDouble("humid", 0.0d));
								else
									Delete_wMap(tMap, "humidity");

								if (MVLitem.has("ap"))
									Find_wMap(tMap, "atmosphericPressure").put("value", MVLitem.optDouble("ap", 0.0d));
								else
									Delete_wMap(tMap, "atmosphericPressure");

								if (MVLitem.has("seaLevelPressure"))
									Find_wMap(tMap, "seaLevelPressure").put("value",
											MVLitem.optDouble("seaLevelPressure", 0.0d));
								else
									Delete_wMap(tMap, "seaLevelPressure");

								if (MVLitem.has("rainfall"))
									Find_wMap(tMap, "rainfall").put("value", MVLitem.optString("rainfall", ""));
								else
									Delete_wMap(tMap, "rainfall");

								if (MVLitem.has("hourlyRainfall"))
									Find_wMap(tMap, "hourlyRainfall").put("value",
											MVLitem.optDouble("hourlyRainfall", 0.0d));
								else
									Delete_wMap(tMap, "hourlyRainfall");

								if (MVLitem.has("dailyRainfall"))
									Find_wMap(tMap, "dailyRainfall").put("value",
											MVLitem.optDouble("dailyRainfall", 0.0d));
								else
									Delete_wMap(tMap, "dailyRainfall");

								if (MVLitem.has("vaporPressure"))
									Find_wMap(tMap, "vaporPressure").put("value",
											MVLitem.optDouble("vaporPressure", 0.0d));
								else
									Delete_wMap(tMap, "vaporPressure");

								if (MVLitem.has("dewPoint"))
									Find_wMap(tMap, "dewPoint").put("value", MVLitem.optDouble("dewPoint", 0.0d));
								else
									Delete_wMap(tMap, "dewPoint");

								if (MVLitem.has("sunshine"))
									Find_wMap(tMap, "sunshine").put("value", MVLitem.optDouble("sunshine", 0.0d));
								else
									Delete_wMap(tMap, "sunshine");

								if (MVLitem.has("insolation"))
									Find_wMap(tMap, "insolation").put("value", MVLitem.optDouble("insolation", 0.0d));
								else
									Delete_wMap(tMap, "insolation");

								if (MVLitem.has("snowfall"))
									Find_wMap(tMap, "snowfall").put("value", MVLitem.optDouble("snowfall", 0.0d));
								else
									Delete_wMap(tMap, "snowfall");

								if (MVLitem.has("snowfallHour3"))
									Find_wMap(tMap, "snowfallHour3").put("value",
											MVLitem.optDouble("snowfallHour3", 0.0d));
								else
									Delete_wMap(tMap, "snowfallHour3");

								if (MVLitem.has("visibility"))
									Find_wMap(tMap, "visibility").put("value", MVLitem.optDouble("visibility", 0.0d));
								else
									Delete_wMap(tMap, "visibility");

								Find_wMap(tMap, "deviceType").put("value", Grade_DeviceType(deviceType));


								wMap = (Map) tMap.get("dataProvider");
								wMap.put("value", _serviceList.optString("dataProvider", "https://www.weather.go.kr"));

								wMap = (Map) tMap.get("globalLocationNumber");
								wMap.put("value", id);

								Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
								addrValue.put("addressCountry", _serviceList.optString("addressCountry", ""));
								addrValue.put("addressRegion", _serviceList.optString("addressRegion", ""));
								addrValue.put("addressLocality", _serviceList.optString("addressLocality", ""));
								addrValue.put("addressTown", _serviceList.optString("addressTown", ""));
								addrValue.put("streetAddress", _serviceList.optString("streetAddress", ""));

								Map<String, Object> locMap = (Map) tMap.get("location");
								locMap.put("observedAt", DateUtil.getTime());
								Map<String, Object> locValueMap = (Map) locMap.get("value");

								ArrayList<Float> location = new ArrayList<>();
								location.add(MVLitem.optFloat("lon", 0.0f));
								location.add(MVLitem.optFloat("lat", 0.0f));
								locValueMap.put("coordinates", location);

								tMap.put("id", id);

								rtnList.add(tMap);
								String str = objectMapper.writeValueAsString(tMap);
								log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
							}
						}
					}
				}
			}
			rtnStr = objectMapper.writeValueAsString(rtnList);
			if (rtnStr.length() < 10) {
				throw new CoreException(ErrorCode.NORMAL_ERROR);
			}

		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage(), e);
		}
		return rtnStr;
	}

	Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
		Map<String, Object> ValueMap = (Map) tMap.get(Name);
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = null;
		try {
			date = df.parse(gettime);
		} catch (ParseException e) {
		  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
		}
		cal.setTime(date);
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
		ValueMap.put("observedAt", df2.format(cal.getTime()));
		return ValueMap;
	}

	void Delete_wMap(Map<String, Object> tMap, String Name) {
		tMap.remove(Name);
	}

	public String Grade_DeviceType(String _DeviceType) {
		if ("S".equals(_DeviceType)) {
			return "Static";
		}
		if ("P".equals(_DeviceType)) {
			return "Portable";
		}
		if ("M".equals(_DeviceType)) {
			return "Move";
		}
		return "타입없음";
	}

	public String Grade(int _grade) {
		if (_grade == 1) {
			return "좋음";
		}
		if (_grade == 2) {
			return "보통";
		}
		if (_grade == 3) {
			return "나쁨";
		}
		if (_grade == 4) {
			return "매우나쁨";
		}
		return "정보없음";
	}

} // end of class