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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.OkUrlUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvFiwareTest_WeatherMeasurement_Static extends AbstractConvert {
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
//			JSONArray svcList = ConfItem.getJSONArray("serviceList");
//			for (int i = 0; i < svcList.length(); i++) {
			for (int i = 0; i < 1; i++) {

//				JSONObject iSvc = svcList.getJSONObject(i); // Column?????? ?????????
				Object ju = getData("http://192.168.1.179:1026/v2/entities?type=AirQualityMeasurement_Static");
				JSONArray arrList = (JSONArray) ju;

				if (arrList.length() > 0) {
					for (Object obj : arrList) {
						JSONObject item = (JSONObject) obj;
						if (item != null) {
							Map<String, Object> tMap = objectMapper.readValue(
									templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
									new TypeReference<Map<String, Object>>() {
									});

							Overwrite(tMap, item, "address");
							Overwrite(tMap, item, "atmosphericPressure");
							Overwrite(tMap, item, "dataProvider");
							Overwrite(tMap, item, "deviceType");
							Overwrite(tMap, item, "globalLocationNumber");
							Overwrite(tMap, item, "humidity");
							Overwrite(tMap, item, "location");
							Overwrite(tMap, item, "temperature");

							DeleteTMap(tMap, item);

							tMap.put("id", item.optString("id"));
							rtnList.add(tMap);
							String str = objectMapper.writeValueAsString(tMap);
							log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
						} else {
							log(SocketCode.DATA_CONVERT_FAIL, id);
						} // end if (arrList.length() > 0)
					}
				}

			} // end for
			rtnStr = objectMapper.writeValueAsString(rtnList);
		} catch (CoreException e) {

			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		}
		return rtnStr;
	}

	public void Overwrite(Map<String, Object> tMap, JSONObject item, String name) {
		Map<String, Object> wMap = new LinkedHashMap<>(); // ????????? ???????????? ????????? map??? ??????
		wMap = (Map) tMap.get(name);
		try {
			wMap.putAll(objectMapper.readValue(item.getJSONObject(name).getJSONObject("value").toString(),
					new TypeReference<Map<String, Object>>() {
					}));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void DeleteTMap(Map<String, Object> tMap, JSONObject item) {
		if (!item.has("altitude"))
			tMap.remove("altitude");
		if (!item.has("windDirection"))
			tMap.remove("windDirection");
		if (!item.has("windSpeed"))
			tMap.remove("windSpeed");
		if (!item.has("temperature"))
			tMap.remove("temperature");
		if (!item.has("humidity"))
			tMap.remove("humidity");
		if (!item.has("atmosphericPressure"))
			tMap.remove("atmosphericPressure");
		if (!item.has("seaLevelPressure"))
			tMap.remove("seaLevelPressure");
		if (!item.has("hourlyRainfall"))
			tMap.remove("hourlyRainfall");
		if (!item.has("dailyRainfall"))
			tMap.remove("dailyRainfall");
		if (!item.has("vaporPressure"))
			tMap.remove("vaporPressure");
		if (!item.has("dewPoint"))
			tMap.remove("dewPoint");
		if (!item.has("sunshine"))
			tMap.remove("sunshine");
		if (!item.has("insolation"))
			tMap.remove("insolation");
		if (!item.has("snowfall"))
			tMap.remove("snowfall");
		if (!item.has("snowfallHour3"))
			tMap.remove("snowfallHour3");
		if (!item.has("visibility"))
			tMap.remove("visibility");
		if (!item.has("deviceType"))
			tMap.remove("deviceType");
	}

	public static Object getData(String url) throws Exception {
		Object obj = null;

		HttpResponse resp = OkUrlUtil.get(url, "Accept", "application/json");
		String payload = resp.getPayload();
		if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301) {
			if (payload.startsWith("{")) {
				obj = new JSONObject(resp.getPayload());
			} else if (payload.startsWith("[")) {
				obj = new JSONArray(resp.getPayload());
			} else if (payload.toLowerCase().startsWith("<")) {
				obj = XML.toJSONObject(resp.getPayload());
			} else {
				obj = resp.getPayload();
			}
		} else {
			throw new Exception(resp.getStatusName());
		}
		return obj;
	}
} // end of class