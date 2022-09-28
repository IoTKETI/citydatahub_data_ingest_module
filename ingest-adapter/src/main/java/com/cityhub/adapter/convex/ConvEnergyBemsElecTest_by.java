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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvEnergyBemsElecTest_by extends AbstractConvert {

	private ObjectMapper objectMapper;
	private JSONObject dataInfo;

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
		this.objectMapper = new ObjectMapper();
	    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
	    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
	    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));
	    dataInfo = new JSONObject(new JsonUtil(ConfItem).get("energeBems").toString());
	}

	@Override
	public String doit() throws CoreException {
//		StringBuffer sendJson = new StringBuffer();
		String rtnStr =""; // list로 받은것 string으로 변환해서 적재할거임

		try {
			String model = ConfItem.getString("model_id");
			log.info("model : "+model);

			rtnStr = getResult();

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

	protected Connection getConnection() {

		Connection conn = null;
		JSONObject databaseInfo = ConfItem.getJSONObject("databaseInfo");

		try {
			String className = databaseInfo.getString("className");
			String url = databaseInfo.getString("url");
			String user = databaseInfo.getString("user");
			String password = databaseInfo.getString("password");

			Class.forName(className);
			conn = DriverManager.getConnection(url, user, password);
		}catch (Exception e) {
		  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
		}
		return conn;
	}

	public final String getResult() {

		Connection conn = getConnection();
		JSONArray jsonData = getData(conn);
		String refinedData = getRefinedData(jsonData);

		return refinedData;
	}

	protected JSONArray getData(Connection conn) {

		StringBuffer strBuffer = new StringBuffer();
		String sql = dataInfo.getString("query");
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while(rs.next()) {
				JSONObject jobj = new JSONObject();
				jobj.put("tagname", rs.getString("tagname"));
				jobj.put("tagdesc", rs.getString("tagdesc"));
				jobj.put("unitname", rs.getString("unitname"));
				jobj.put("standardtagname", rs.getString("standardtagname"));
				jobj.put("measure1hour", rs.getString("measure1hour"));
				jobj.put("measurementpeak", rs.getString("measurementpeak"));
				jobj.put("measure1to15", rs.getString("measure1to15"));
				jobj.put("measure16to30", rs.getString("measure16to30"));
				jobj.put("measure31to45", rs.getString("measure31to45"));
				jobj.put("measure46to60", rs.getString("measure46to60"));

				strBuffer.append(jobj.toString() + ",");
			}

		} catch (SQLException e) {
		  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
		}finally {
			disconnectDB(conn, pstmt, rs);
		}

		String tempResult = strBuffer.toString();
		tempResult = "[" + tempResult.substring(0, tempResult.lastIndexOf(",")) + "]";
		JSONArray result = new JSONArray(tempResult);

		return result;
	}

	protected String getRefinedData(JSONArray jsonData){
		int cnt = 0;
		String rtnStr = "";
		List<Map<String,Object>> rtnList = new LinkedList<>(); // buffer대신 List로 데이터 받을예정


		try {

		for(Object obj : jsonData) {

			Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
	        Map<String,Object> wMap = new LinkedHashMap<>();

			JSONObject jobj = (JSONObject) obj;
//			JSONObject refinedData = new JSONObject(strTemplate);
			JsonUtil refinedData = new JsonUtil(templateItem.toString());

			//변수 선언
			String tagname = jobj.get("tagname").toString();
			String tagdesc = jobj.get("tagdesc").toString();
			String unitname = jobj.get("unitname").toString();
			String standardtagname = jobj.get("standardtagname").toString();
			String measurementpeak = jobj.get("measurementpeak").toString();
			String measure1hour = jobj.get("measure1hour").toString();
			String measure1to15 = jobj.get("measure1to15").toString();
			String measure16to30 = jobj.get("measure16to30").toString();
			String measure31to45 = jobj.get("measure31to45").toString();
			String measure46to60 = jobj.get("measure46to60").toString();

			String measureDay = dataInfo.get("measureDay").toString();
			String measureTime = dataInfo.get("measureTime").toString();
			String addressCountry = dataInfo.get("addressCountry").toString();
			String addressRegion = dataInfo.get("addressRegion").toString();
			String addressLocality = dataInfo.get("addressLocality").toString();
			String addressTown = dataInfo.get("addressTown").toString();
			String streetAddress = dataInfo.get("streetAddress").toString();
			String gs1Code = dataInfo.get("gs1Code").toString();
			String workplaceName = dataInfo.get("workplaceName").toString();
			String measurementType = dataInfo.get("measurementType").toString();

			String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());

			ArrayList<Float> coordinates = new ArrayList<>();
			coordinates.add(0f);
			coordinates.add(0f);


			tMap.put("id", gs1Code);
			Map<String,Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
	        addrValue.put("addressCountry",addressCountry);
	        addrValue.put("addressRegion", addressRegion);
	        addrValue.put("addressLocality", addressLocality );
	        addrValue.put("addressTown", addressTown );
	        addrValue.put("streetAddress",streetAddress);

	        Map<String,Object> locMap = (Map)tMap.get("location");
	        locMap.put("observedAt",createdAt);
	        Map<String,Object> locValueMap  = (Map)locMap.get("value");
	        locValueMap.put("coordinates", coordinates);

	        wMap.put("tagName", tagname);
	        wMap.put("tagDesc", tagdesc);
	        wMap.put("unitName", unitname);
	        wMap.put("standardTagName", standardtagname);
	        wMap.put("measure1hour", measure1hour);
	        wMap.put("measure1to15", measure1to15);
	        wMap.put("measure16to30", measure16to30);
	        wMap.put("measure31to45", measure31to45);
	        wMap.put("measure46to60", measure46to60);
	        wMap.put("measureDay", measureDay);
	        wMap.put("measureTime", measureTime);
	        wMap.put("workplaceName", workplaceName);
	        wMap.put("measurementpeak", measurementpeak);

	        Map<String,Object> energyObj = new LinkedHashMap<>();
	        energyObj.put("type", "Property");
	        energyObj.put("observedAt", DateUtil.getTime());
	        energyObj.put("value", wMap);

	        tMap.put("energe", energyObj);

	        rtnList.add(tMap);
		}

		log.info("tMap : {}", rtnList);
		rtnStr = objectMapper.writeValueAsString(rtnList);

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

	protected void disconnectDB(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		try {if(rs != null) {rs.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
		disconnectDB(conn, pstmt);
	}
	protected void disconnectDB(Connection conn, PreparedStatement pstmt) {
		try {if(pstmt != null) {pstmt.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
		try {if(conn != null) {conn.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
	}

	protected String getTimeInfo(LocalDate date, LocalTime time) {

		LocalDateTime dt = LocalDateTime.of(date, time);
	    ZoneOffset zo = ZonedDateTime.now().getOffset();
	    String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

	    return returnValue;
	}

}

// end of class