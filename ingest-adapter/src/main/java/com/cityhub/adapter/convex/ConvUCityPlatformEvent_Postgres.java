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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvUCityPlatformEvent_Postgres extends AbstractConvert {
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

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Map<String, Object>> rtnList = new LinkedList<>();
		String rtnStr = "";

		try {
			JSONObject databaseInfo = ConfItem.getJSONObject("databaseInfo");
			String className = databaseInfo.getString("className");
			String url = databaseInfo.getString("url");
			String user = databaseInfo.getString("user");
			String password = databaseInfo.getString("password");

			Class.forName(className);
			log(SocketCode.SOCKET_CONNECT_TRY, id);
			conn = DriverManager.getConnection(url, user, password);
			String sql = ConfItem.getString("query");

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ConfItem.getInt("limitNum"));
			pstmt.setInt(2, ConfItem.getInt("offsetNum"));
			rs = pstmt.executeQuery();
			id = ConfItem.getString("id_prefix");
			log(SocketCode.SOCKET_CONNECT, id);
			int gs1number = 0;
			while (rs.next()) {
				log(SocketCode.DATA_RECEIVE, id);
				Map<String, Object> tMap = objectMapper.readValue(
						templateItem.getJSONObject(ConfItem.getString("modelId")).toString(),
						new TypeReference<Map<String, Object>>() {
						});
				Map<String, Object> wMap;
				ArrayList<Double> coordinates = new ArrayList<Double>();

				Object[][] EVT_ID = { { "112UC001", "112긴급영상" }, { "112UC120", "112긴급출동" }, { "119UC001", "119긴급출동" },
						{ "APLEN110", "기상특보" }, { "BISTR110", "버스정보" }, { "BNRCP140", "강도발생" },
						{ "DUCDP110", "재난정보긴급지원" }, { "EBSCP110", "비상벨" }, { "FCLFT101", "시설물" },
						{ "FMSFT110", "차량번호인식" }, { "LPRUM120", "차량번호인식" }, { "MTSUC210", "범죄" }, { "MTSUC220", "방재" },
						{ "MTSUC230", "안전" }, { "MTSUC240", "CCTV" }, { "MTSUC250", "일반시설물" }, { "POCCP103", "안심귀가" },
						{ "SEBSF110", "비상벨" }, { "STLSF110", "안심택시" }, { "TUATR100", "교통돌발" }, { "WESEN110", "기상특보" },
						{ "WPSSF110", "버스정보" }, { "BISTR110", "사회적약자" } };
				Object[][] EVT_GRAD_CD = { { "10", "긴급" }, { "20", "일반" } };
				Object[][] EVT_PRGRS_CD = { { "10", "발생" }, { "40", "정보변경" }, { "91", "종료" }, { "92", "자동종료" } };

				coordinates.add(Double.parseDouble(rs.getString("POINT_X")));
				coordinates.add(Double.parseDouble(rs.getString("POINT_Y")));
				String eventName = ExponentialStage(rs.getString("EVT_ID"), EVT_ID);
				String grade = ExponentialStage(rs.getString("EVT_GRAD_CD"), EVT_GRAD_CD);
				String status = ExponentialStage(rs.getString("EVT_PRGRS_CD"), EVT_PRGRS_CD);
				
				
				Find_wMap(tMap, "eventType").put("value", rs.getString("EVT_ID"));
				Find_wMap(tMap, "eventName").put("value", eventName);
				Find_wMap(tMap, "grade").put("value", grade);
				Find_wMap(tMap, "status").put("value", status);
				Find_wMap(tMap, "content").put("value", rs.getString("EVT_DTL"));
				Find_wMap(tMap, "generatedAt").put("value", DateUtil.getISOTime(rs.getString("EVT_OCR_YMD_HMS")));
				Find_wMap(tMap, "finishedAt").put("value", DateUtil.getISOTime(rs.getString("EVT_END_YMD_HMS")));

				id = ConfItem.getString("id_prefix") + "_" + (gs1number++) + "_" + rs.getString("EVT_ID");

				wMap = (Map) tMap.get("dataProvider");
				wMap.put("value", ConfItem.getString("dataProvider"));

				wMap = (Map) tMap.get("globalLocationNumber");
				wMap.put("value", id);

				Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
				addrValue.put("addressCountry", ConfItem.getString("addressCountry"));
				addrValue.put("addressRegion", ConfItem.getString("addressRegion"));
				addrValue.put("addressLocality", ConfItem.getString("addressLocality"));
				addrValue.put("addressTown", "");
				addrValue.put("streetAddress", rs.getString("EVT_PLACE"));

				Map<String, Object> locMap = (Map) tMap.get("location");
				locMap.put("observedAt", DateUtil.getTime());
				Map<String, Object> locValueMap = (Map) locMap.get("value");
				locValueMap.put("coordinates", coordinates);

				tMap.put("id", id);

				rtnList.add(tMap);
				String str = objectMapper.writeValueAsString(tMap);
				log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
			}
			rtnStr = objectMapper.writeValueAsString(rtnList);
			if (rtnStr.length() < 10) {
				throw new CoreException(ErrorCode.NORMAL_ERROR);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return rtnStr;
	} // end of doit

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

	String ExponentialStage(String Exponential, Object[][] arrList) {

		String resultName = "";

		for (Integer i = 0; i < arrList.length; i++) {

			String _arrayString = (String) arrList[i][0];
			String _arrayName = (String) arrList[i][1];

			if (Exponential.equals(_arrayString)) {
				return _arrayName;

			}
		}

		return resultName;
	}

} // end of class
