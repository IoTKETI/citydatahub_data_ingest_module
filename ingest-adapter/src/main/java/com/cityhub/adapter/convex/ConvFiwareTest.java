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
public class ConvFiwareTest extends AbstractConvert {
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
				
				JSONObject iSvc = svcList.getJSONObject(i); // Column별로 분리함
				Object ju = getData("http://192.168.1.179:1026/v2/entities?type=CCTV");
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
							Overwrite(tMap, item, "distance");
							Overwrite(tMap, item, "globalLocationNumber");
							Overwrite(tMap, item, "installedAt");
							Overwrite(tMap, item, "hasEmergencyBell");
							Overwrite(tMap, item, "fieldOfView");
							Overwrite(tMap, item, "typeOfCCTV");
							Overwrite(tMap, item, "isRotatable");
							Overwrite(tMap, item, "name");
							Overwrite(tMap, item, "numberOfCCTV");
							Overwrite(tMap, item, "location");
							Overwrite(tMap, item, "dataProvider");
							Overwrite(tMap, item, "pixel");
							Overwrite(tMap, item, "direction");
							Overwrite(tMap, item, "height");
							Overwrite(tMap, item, "status");

							tMap.put("id", item.optString("id"));
							log.info("tMap : " + tMap);
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
		Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
		wMap = (Map) tMap.get(name);
		try {
			wMap.putAll(objectMapper.readValue(item.getJSONObject(name).getJSONObject("value").toString(),new TypeReference < Map < String, Object >> () {}));
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