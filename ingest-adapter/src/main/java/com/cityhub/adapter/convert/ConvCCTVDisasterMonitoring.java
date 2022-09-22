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
package com.cityhub.adapter.convert;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvCCTVDisasterMonitoring extends ConvCCTV2{

	@Override
	protected JSONArray getData(Connection conn) {

		StringBuffer strBuffer = new StringBuffer();
		String sql = cctvInfo.getString("query");
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while(rs.next()) {
				JSONObject jobj = new JSONObject();
				jobj.put("id", rs.getString("id"));
				jobj.put("name", rs.getString("name"));
				jobj.put("streetAddress", rs.getString("streetAddress"));
				jobj.put("streetAddress2", rs.getString("streetAddress2"));
				jobj.put("latitude", rs.getString("latitude"));
				jobj.put("longitude", rs.getString("longitude"));
				jobj.put("isRotatable", rs.getString("isRotatable"));
				jobj.put("pixel", rs.getString("pixel"));
				jobj.put("distance", rs.getString("distance"));

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

	@Override
	protected String getRefinedData(JSONArray jsonData) {


		StringBuffer resultBuffer = new StringBuffer();

		for(Object obj : jsonData) {
			JSONObject jobj = (JSONObject) obj;

//			JSONObject refinedData = new JSONObject(strTemplate);
			JsonUtil refinedData = new JsonUtil(_template.toString());


			//변수 선언
			String id = jobj.get("id").toString();
			String name = jobj.get("name").toString();
			String streetAddress = "";
			String addressCountry = cctvInfo.get("addressCountry").toString();
			String addressRegion = cctvInfo.get("addressRegion").toString();
			String addressLocality = cctvInfo.get("addressLocality").toString();
			String addressTown = jobj.get("streetAddress2").toString().split(" ")[0];
			String isRotatable = jobj.get("isRotatable").toString();
			ArrayList<Double> location = new ArrayList<>();
			String pixel = jobj.get("pixel").toString();
			String distance = jobj.get("distance").toString();
			String installedAt = getTimeInfo(LocalDate.of(2020, 1, 1), LocalTime.of(12, 0, 0));

			int numberOfCCTV = 1;
			Float height = 4.0f;
			String status = "normal";
			String hasEmergencyBell = "FALSE";
			Float direction = 0.0f;
			Float fieldOfView = 0.0f;
			String typeOfCCTV = _cctvType;
			String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());


			// 데이터 정제
			id = cctvInfo.get("idPrefix") + id.replace("-", "_") + cctvInfo.get("idSurffix");

			try {
				streetAddress = jobj.get("streetAddress").toString();
			}catch (Exception e) {
				streetAddress = jobj.get("streetAddress2").toString();
			}

			if(isRotatable.contains("회전")) {
				isRotatable = "TRUE";
			}else {
				isRotatable = "FALSE";
			}

			location.add(Double.parseDouble(jobj.get("longitude").toString()));
			location.add(Double.parseDouble(jobj.get("latitude").toString()));



			if(pixel.contains("화소")) {
				pixel = pixel.substring(0, pixel.indexOf("화소"));
			}
			if(pixel.contains("만")) {
				pixel = pixel.replace("만", "0000");
			}


			int distanceCnt = 0;
			for(char c : distance.toCharArray()) {
				if(c >= 48 && c <= 57) {
					distanceCnt++;
				}else {
					break;
				}
			}
			distance =  distance.substring(0, distanceCnt);


			//데이터 삽입
			refinedData.put("id", id);
			refinedData.put("name.value", name);
			refinedData.put("address.value.addressCountry", addressCountry);
			refinedData.put("address.value.addressRegion", addressRegion);
			refinedData.put("address.value.addressLocality", addressLocality);
			refinedData.put("address.value.streetAddress", streetAddress);
			refinedData.put("address.value.addressTown", addressTown);

			refinedData.put("isRotatable.value", isRotatable);
			refinedData.put("location.value.coordinates", location);
			refinedData.put("pixel.value", Float.parseFloat(pixel));
			refinedData.put("distance.value", Float.parseFloat(distance));
			refinedData.put("installedAt.value", installedAt);


			refinedData.put("numberOfCCTV.value", numberOfCCTV);
			refinedData.put("height.value", height);  //수정 필요함?
			refinedData.put("status.value", status);
			refinedData.put("hasEmergencyBell.value", hasEmergencyBell);
			refinedData.put("direction.value", direction);
			refinedData.put("fieldOfView.value", fieldOfView);
			refinedData.put("typeOfCCTV.value", typeOfCCTV);
			refinedData.put("createdAt", createdAt);

			resultBuffer.append(refinedData + ", ");
		}

		return resultBuffer.toString();
	}

}
