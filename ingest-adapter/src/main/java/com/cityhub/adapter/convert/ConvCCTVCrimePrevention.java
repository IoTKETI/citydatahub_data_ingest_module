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
public class ConvCCTVCrimePrevention extends ConvCCTV2 {

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
				jobj.put("addressLocality", rs.getString("addressLocality"));
				jobj.put("addressTown", rs.getString("addressTown"));
				jobj.put("streetAddress", rs.getString("streetAddress"));
				jobj.put("name", rs.getString("name"));
				jobj.put("installedAt", rs.getString("installedAt"));
				jobj.put("hasEmergencyBell", rs.getString("hasEmergencyBell"));
				jobj.put("numberOfCCTV", rs.getString("numberOfCCTV"));
				jobj.put("isRotatable", rs.getString("isRotatable"));
				jobj.put("pixel", rs.getString("pixel"));

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
	protected String getRefinedData(JSONArray jsonData){
		int cnt = 0;
		StringBuffer resultBuffer = new StringBuffer();

		for(Object obj : jsonData) {
			JSONObject jobj = (JSONObject) obj;
//			JSONObject refinedData = new JSONObject(strTemplate);
			JsonUtil refinedData = new JsonUtil(_template.toString());

			//변수 선언
			String name = "";
			String isRotatable = "";
			String id = jobj.get("id").toString();
			String hasEmergencyBell = jobj.get("hasEmergencyBell").toString();
			String numberOfCCTV = jobj.get("numberOfCCTV").toString();
			String pixel = jobj.get("pixel").toString();
			String installedAt = jobj.get("installedAt").toString();
			String addressCountry = cctvInfo.get("addressCountry").toString();
			String addressRegion = cctvInfo.get("addressRegion").toString();
			String addressLocality = jobj.get("addressLocality").toString();
			String addressTown = jobj.get("addressTown").toString();
			String streetAddress = jobj.get("streetAddress").toString();

			Float height = 4.0f;
			String status = "normal";
			Float distance = 30.0f;
			Float direction = 0f;
			Float fieldOfView = 0f;
			String typeOfCCTV = _cctvType;
			String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());

			ArrayList<Float> coordinates = new ArrayList<>();
			coordinates.add(0f);
			coordinates.add(0f);

			//데이터 정제
			try {
				name = jobj.get("name").toString();
			}catch (Exception e) {
				name = null;
			}

			try {
				isRotatable = jobj.get("isRotatable").toString();
			} catch (Exception e) {}

			id = cctvInfo.getString("idPrefix") + id.replace("-", "_") + cctvInfo.getString("idSurffix");


			if("없음".equals(hasEmergencyBell) || hasEmergencyBell.trim().length() == 0){
				hasEmergencyBell =  "FALSE";
			}else {
				hasEmergencyBell =  "TRUE";
			}

			if(pixel != null && !"".equals(pixel) && pixel.contains("만")) {
				pixel = pixel.replace("만", "0000");
			}

			installedAt = getTimeInfo(LocalDate.of(Integer.parseInt(installedAt), 1, 1), LocalTime.of(12, 0, 0));


			if(isRotatable.trim().length() >= 1 && Integer.parseInt(isRotatable) >= 1) {
				isRotatable = "TRUE";
			}else {
				isRotatable = "FALSE";
			}

			//데이터 삽입
			refinedData.put("name.value", name);
			refinedData.put("id", id);
			refinedData.put("hasEmergencyBell.value", hasEmergencyBell);
			refinedData.put("numberOfCCTV.value", Integer.parseInt(numberOfCCTV));
			refinedData.put("pixel.value", Float.parseFloat(pixel));
			refinedData.put("installedAt.value", installedAt);
			refinedData.put("isRotatable.value", isRotatable);

			refinedData.put("address.value.addressCountry", addressCountry);
			refinedData.put("address.value.addressRegion", addressRegion);
			refinedData.put("address.value.addressLocality", addressLocality);
			refinedData.put("address.value.streetAddress", streetAddress);
			refinedData.put("address.value.addressTown", addressTown);

			refinedData.put("height.value", height);
			refinedData.put("status.value", status);
			refinedData.put("distance.value", distance);
			refinedData.put("direction.value", direction);
			refinedData.put("fieldOfView.value", fieldOfView);
			refinedData.put("typeOfCCTV.value", typeOfCCTV);
			refinedData.put("createdAt", createdAt);

			refinedData.put("location.value.coordinates", coordinates);

			resultBuffer.append(refinedData + ", ");
		}

		return resultBuffer.toString();
	}
}
