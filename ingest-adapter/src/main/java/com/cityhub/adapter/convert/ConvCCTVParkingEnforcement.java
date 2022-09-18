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

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.utils.JsonUtil;
import com.ibm.icu.text.DecimalFormat;

public class ConvCCTVParkingEnforcement extends ConvCCTV2{

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
				jobj.put("addressRegionAndAddressLocality", rs.getString("addressRegionAndAddressLocality"));
				jobj.put("streetAddress", rs.getString("streetAddress"));
				jobj.put("streetAddress2", rs.getString("streetAddress2"));
				jobj.put("numberOfCCTV", rs.getString("numberOfCCTV"));
				jobj.put("pixel", rs.getString("pixel"));
				jobj.put("isRotatable", rs.getString("isRotatable"));
				jobj.put("installedAt", rs.getString("installedAt"));
				jobj.put("latitude", rs.getString("latitude"));
				jobj.put("longitude", rs.getString("longitude"));

				strBuffer.append(jobj.toString() + ",");
			}

		} catch (SQLException e) {
			e.printStackTrace();
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

		for(int i = 0; i < jsonData.length(); i++) {

			JSONObject jobj = (JSONObject) jsonData.get(i);
//			JSONObject refinedData = new JSONObject(_template.toString());
			JsonUtil refinedData = new JsonUtil(_template.toString());

			// 변수 선언

			String id = "";
			String streetAddress = "";
			String addressCountry = cctvInfo.get("addressCountry").toString();
			String addressRegionAndAddressLocality = jobj.get("addressRegionAndAddressLocality").toString();
			String addressRegion = addressRegionAndAddressLocality.split(" ")[0];
			String addressLocality = addressRegionAndAddressLocality.split(" ")[1];
			String addressTown = jobj.get("streetAddress2").toString().split(" ")[0];
			String isRotatable = jobj.get("isRotatable").toString();
			String pixel = jobj.getString("pixel").toString();
			String numberOfCCTV = jobj.get("numberOfCCTV").toString();
			String installedAt = jobj.get("installedAt").toString();
			ArrayList<Double> location = new ArrayList<Double>();

			Float height = 4.0f;
			String status = "normal";
			String hasEmergencyBell = "FALSE";
			Float direction = 0.0f;
			Float distance = 30.0f;
			Float fieldOfView = 0.0f;
			String typeOfCCTV = _cctvType;
			String createdAt = getTimeInfo(LocalDate.now(), LocalTime.now());

			// 데이터 정제
			id = cctvInfo.get("idPrefix") + new DecimalFormat("000").format(i+1);


			location.add(Double.parseDouble(jobj.get("longitude").toString()));
			location.add(Double.parseDouble(jobj.get("latitude").toString()));

			try {
				streetAddress = jobj.get("streetAddress").toString();
				if(streetAddress.trim().length() <= 1) {
					streetAddress = jobj.get("streetAddress2").toString();
				}
			}catch (Exception e) {
				streetAddress = jobj.get("streetAddress2").toString();
			}

			if(isRotatable.contains("360")) {
				isRotatable = "TRUE";
			}else {
				isRotatable = "FALSE";
			}

			if(pixel.contains("화소")) {
				pixel = pixel.substring(0, pixel.indexOf("화소"));
			}
			if(pixel.contains("만")) {
				pixel = pixel.replace("만", "0000");
			}

			installedAt = getInstalledAt(installedAt);

			//데이터 삽입
			refinedData.put("id", id);
			refinedData.put("address.value.addressCountry", addressCountry);
			refinedData.put("address.value.addressRegion", addressRegion);
			refinedData.put("address.value.addressLocality", addressLocality);
			refinedData.put("address.value.streetAddress", streetAddress);
			refinedData.put("address.value.addressTown", addressTown);

			refinedData.put("pixel.value", Float.parseFloat(pixel));
			refinedData.put("isRotatable.value", isRotatable);


			refinedData.put("numberOfCCTV.value", Float.parseFloat(numberOfCCTV));
 			refinedData.put("installedAt.value", installedAt);
 			refinedData.put("location.value.coordinates", location);

			refinedData.put("height.value", height);  //수정 필요함?
			refinedData.put("status.value", status);
			refinedData.put("hasEmergencyBell.value", hasEmergencyBell);
			refinedData.put("direction.value", direction);
			refinedData.put("distance.value", distance);
			refinedData.put("fieldOfView.value", fieldOfView);
			refinedData.put("typeOfCCTV.value", typeOfCCTV);
			refinedData.put("createdAt", createdAt);

			refinedData.remove("name");

			resultBuffer.append(refinedData + ", ");
		}

		return resultBuffer.toString();
	}

	private String getInstalledAt(String installedAt) {

		String month = "1";
		String year = installedAt;
		if(installedAt.contains(".")) {
			String[] date = installedAt.trim().split("\\.");
			year = date[0].trim();
			month = date[1].trim();
		}else if(installedAt.contains("(")) {
			year = installedAt.substring(installedAt.lastIndexOf(")")+1);
		}
		LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
		LocalTime time = LocalTime.of(12, 0, 0);
		return getTimeInfo(date, time);
	}

}
