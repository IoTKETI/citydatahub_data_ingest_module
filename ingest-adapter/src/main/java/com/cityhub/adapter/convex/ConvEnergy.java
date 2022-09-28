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

import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvEnergy extends AbstractConvert {

	private ObjectMapper objectMapper;
	static Connection conn = null;

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);this.objectMapper = new ObjectMapper();
		   this.objectMapper.setSerializationInclusion(Include.NON_NULL);
		   this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
		   this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
	}

	@Override
	public String doit() throws CoreException {

		List<Map<String, Object>> rtnList = new LinkedList<>();
		String rtnStr = "";

		try {
			JSONObject energy = ConfItem.getJSONObject("energy");
			JSONObject sqlcon = ConfItem.getJSONObject("databaseInfo");

			String url = sqlcon.getString("url");
			String user = sqlcon.getString("user");
			String password = sqlcon.getString("password");

			conn = DriverManager.getConnection(url, user, password);

			Iterator<String> etype = new JSONObject(ConfItem.get("energy").toString()).keys();

			while(etype.hasNext()) {

				String energytype = etype.next();
				log.info(energytype);

				JSONObject energyInfo = new JSONObject(new JsonUtil(ConfItem).get("energy." + energytype).toString());

				String sql = energyInfo.getString("query");
				PreparedStatement pstmt = null;
				ResultSet rs = null;

				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();

				if(energytype.equals("fems")) {
					int num = 1;
					while (rs.next()) {
						Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
				        Map<String,Object> wMap = new LinkedHashMap<>();

						ArrayList<Double> location = new ArrayList<>();
						location.add(126.736327);
						location.add(37.334748);

						wMap.put("workplaceName", "정우이지텍");
						wMap.put("measurementType", "공장에너지");
						wMap.put("measureDay", rs.getString("measureday"));
						wMap.put("measureTime", rs.getString("measuretime"));
						wMap.put("sequence", " ");
						wMap.put("measure1hour", rs.getString("measuretime"));
						wMap.put("measurePeak", rs.getString("measurementpeak"));
						wMap.put("measure1to15", rs.getString("measure1to15"));
						wMap.put("measure16to30", rs.getString("measure16to30"));
						wMap.put("measure31to45", rs.getString("measure31to45"));
						wMap.put("measure46to60", rs.getString("measure46to60"));
						wMap.put("type", "공장 에너지 사용량");

						Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
						addrValue.put("addressCountry", "kr");
						addrValue.put("addressRegion", "경기도");
						addrValue.put("addressLocality", "시흥시");
						addrValue.put("addressTown", "정왕동");
						addrValue.put("streetAddress", "마유로238번길 81");

						Map<String, Object> locMap = (Map) tMap.get("location");
						locMap.put("observedAt", DateUtil.getTime());
						Map<String, Object> locValueMap = (Map) locMap.get("value");
						locValueMap.put("coordinates", location);

						String id = energyInfo.getString("idPrefix") + Integer.toString(num);
						tMap.put("id", id);
						num++;

						Map<String,Object> energyf = new LinkedHashMap<>();
						energyf.put("type","Property");
						energyf.put("observedAt",DateUtil.getTime());
						energyf.put("value",wMap);
			            tMap.put("energy", energyf);

			            rtnList.add(tMap);
			            String str = objectMapper.writeValueAsString(tMap);
			            log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
					}
				}

				if(energytype.equals("bems")) {
					int num = 1;
					while (rs.next()) {
						Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
				        Map<String,Object> wMap = new LinkedHashMap<>();

						ArrayList<Double> location = new ArrayList<>();
						location.add(126.735936);
						location.add(37.338464);

						//TODO
						wMap.put("workplaceName", "시흥에코센터");
						wMap.put("type", "빌딩 에너지 사용량");
						wMap.put("sequence", " ");
						wMap.put("measurementType", "건물에너지");
						wMap.put("measureTime", rs.getString("measuretime"));
						wMap.put("measurePeak", rs.getString("measurementpeak"));
						wMap.put("measureDay", rs.getString("measureday"));
						wMap.put("measure1hour", rs.getString("measure1hour"));
						wMap.put("measure1to15", rs.getString("measure1to15"));
						wMap.put("measure16to30", rs.getString("measure16to30"));
						wMap.put("measure31to45", rs.getString("measure31to45"));
						wMap.put("measure46to60", rs.getString("measure46to60"));

						Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
						addrValue.put("addressCountry", "kr");
						addrValue.put("addressRegion", "경기도");
						addrValue.put("addressLocality", "시흥시");
						addrValue.put("addressTown", "정왕1동");
						addrValue.put("streetAddress", "경기과기대로 284");

						Map<String, Object> locMap = (Map) tMap.get("location");
						locMap.put("observedAt", DateUtil.getTime());
						Map<String, Object> locValueMap = (Map) locMap.get("value");
						locValueMap.put("coordinates", location);

						String id = energyInfo.getString("idPrefix") + Integer.toString(num);
						tMap.put("id", id);
						num++;

						Map<String,Object> energyf = new LinkedHashMap<>();
						energyf.put("type","Property");
						energyf.put("observedAt",DateUtil.getTime());
						energyf.put("value",wMap);
			            tMap.put("energy", energyf);

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

	} // end of doit
}
// end of class