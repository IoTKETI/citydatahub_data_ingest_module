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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class ConvCrowdSourcingReport_SiheungLivingLab extends AbstractConvert {
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
				id = iSvc.getString("gs1Code"); // 분리한 데이터의 gs1Code 값을 is에 넣어줌
				JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
				JSONArray arrList = ju.getArray("data.findAllPosts");

				if (arrList.length() > 0) {
					for (Object obj : arrList) {
						JSONObject item = (JSONObject) obj;
						if (item != null) {
							
							if ((item.optDouble("longitude", 0.0f) == 0.0f)
									|| (item.optDouble("latitude", 0.0f) == 0.0f))
								continue;
							
							log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
							
							Map<String, Object> wMap = new LinkedHashMap<>(); // 분리한 데이터를 넣어줄 map을 만듬
							Map<String, Object> tMap = objectMapper.readValue(
									templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
									new TypeReference<Map<String, Object>>() {
									});

							if (item.has("post_id")) {
								Find_wMap(tMap, "postId").put("value", item.optString("post_id"));
							} else {
								Delete_wMap(tMap, "postId");
							}
							if (item.has("sourcing_title")) {
								Find_wMap(tMap, "sourcingTitle").put("value", item.optString("sourcing_title"));
							} else {
								Delete_wMap(tMap, "sourcingTitle");
							}
							if (item.has("sourcing_description")) {
								Find_wMap(tMap, "sourcingDescription").put("value", item.optString("sourcing_description"));
							} else {
								Delete_wMap(tMap, "sourcingDescription");
							}
							if (item.has("latitude")) {
								Find_wMap(tMap, "latitude").put("value", item.optString("latitude"));
							} else {
								Delete_wMap(tMap, "latitude");
							}
							if (item.has("longitude")) {
								Find_wMap(tMap, "longitude").put("value", item.optString("longitude"));
							} else {
								Delete_wMap(tMap, "longitude");
							}
							if (item.has("file_data")) {
								Find_wMap(tMap, "fileData").put("value", item.optString("file_data"));
							} else {
								Delete_wMap(tMap, "fileData");
							}
							if (item.has("dislikeCount")) {
								Find_wMap(tMap, "dislikeCount").put("value", item.optInt("dislikeCount"));
							} else {
								Delete_wMap(tMap, "dislikeCount");
							}
							if (item.has("obstacle")) {
								Find_wMap(tMap, "obstacle").put("value", item.optString("obstacle"));
							} else {
								Delete_wMap(tMap, "obstacle");
							}
							if (item.has("post_date")) {
								Find_wMap(tMap, "postDate").put("value", item.optString("post_date"));
							} else {
								Delete_wMap(tMap, "postDate");
							}
							if (item.has("update_date")) {
								Find_wMap(tMap, "updateDate").put("value", DateUtil.getISOTime(item.optString("update_date").substring(0, 14)));
							} else {
								Delete_wMap(tMap, "updateDate");
							}
							if (item.has("upload_date")) {
								Find_wMap(tMap, "uploadDate").put("value", DateUtil.getISOTime(item.optString("upload_date")));
							} else {
								Delete_wMap(tMap, "uploadDate");
							}
							


							wMap = (Map) tMap.get("dataProvider");
							wMap.put("value", iSvc.optString("dataProvider", "http://healthcare.livinglab.siheung.kr/mobility/crowdSourcing"));

							wMap = (Map) tMap.get("globalLocationNumber");
							wMap.put("value", id);

							Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
							addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
							addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
							addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
							addrValue.put("addressTown", item.optString("sourcing_description", ""));
							addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

							Map<String, Object> locMap = (Map) tMap.get("location");
							locMap.put("observedAt", DateUtil.getTime());
							Map<String, Object> locValueMap = (Map) locMap.get("value");

							ArrayList<Double> location = new ArrayList<>();
							location.add(item.optDouble("longitude"));
							location.add(item.optDouble("latitude"));

							locValueMap.put("coordinates", location);

							tMap.put("id", iSvc.optString("gs1Code"));

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
			if (rtnStr.length() < 10) {
				throw new CoreException(ErrorCode.NORMAL_ERROR);
			}

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
	

	Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
		Map<String, Object> ValueMap = (Map) tMap.get(Name);
		ValueMap.put("observedAt", DateUtil.getTime());
		return ValueMap;
	}

	void Delete_wMap(Map<String, Object> tMap, String Name) {
		tMap.remove(Name);
	}


} // end of class