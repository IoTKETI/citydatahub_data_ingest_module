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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWaterTapUsage extends AbstractConvert {

	public static Connection conn = null;
	public static PreparedStatement pstmt = null;
	public static ResultSet rs = null;

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
	}

	@Override
	public String doit() throws CoreException {
		StringBuffer sendJson = new StringBuffer();

		String model_id = ConfItem.get("model_id").toString();
		String template = templateItem.get(model_id).toString();
		JSONObject location = ConfItem.getJSONObject("location");

		try {

			JSONArray arrResult = getDataFromDB();
			String result = getResult(arrResult, location, template);
			sendJson.append(result);

		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		}

		return sendJson.toString();
	} // end of doit

	private JSONArray getDataFromDB() {

		StringBuffer _resultBuffer = new StringBuffer();
		JSONArray result = null;

		String sql = ConfItem.getString("query");

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ConfItem.getInt("limitNum"));
			pstmt.setInt(2, ConfItem.getInt("offsetNum"));
			rs = pstmt.executeQuery();

			while(rs.next()) {
				JSONObject jobj = new JSONObject();
				JSONArray _usageArr = new JSONArray();
				JSONArray _feeArr = new JSONArray();

				jobj.put("id", rs.getString("id"));
				jobj.put("address", rs.getString("address"));
				jobj.put("gauge", rs.getString("gauge"));
				jobj.put("household", rs.getString("household"));
				jobj.put("sewer", rs.getString("sewer"));

				jobj.put("meter_number", rs.getString("meter_number"));
				jobj.put("read_meter_date", rs.getString("read_meter_date"));

				_usageArr.put(new JSONObject().put("usage_2019_07", rs.getString("usage_19_07")));
				_usageArr.put(new JSONObject().put("usage_2019_08", rs.getString("usage_19_08")));
				_usageArr.put(new JSONObject().put("usage_2019_09", rs.getString("usage_19_09")));
				_usageArr.put(new JSONObject().put("usage_2019_10", rs.getString("usage_19_10")));
				_usageArr.put(new JSONObject().put("usage_2019_11", rs.getString("usage_19_11")));
				_usageArr.put(new JSONObject().put("usage_2019_12", rs.getString("usage_19_12")));
				_usageArr.put(new JSONObject().put("usage_2020_01", rs.getString("usage_20_01")));
				_usageArr.put(new JSONObject().put("usage_2020_02", rs.getString("usage_20_02")));
				_usageArr.put(new JSONObject().put("usage_2020_03", rs.getString("usage_20_03")));
				_usageArr.put(new JSONObject().put("usage_2020_04", rs.getString("usage_20_04")));
				_usageArr.put(new JSONObject().put("usage_2020_05", rs.getString("usage_20_05")));
				_usageArr.put(new JSONObject().put("usage_2020_06", rs.getString("usage_20_06")));

				_feeArr.put(new JSONObject().put("fee_2019_07", rs.getString("fee_19_07")));
				_feeArr.put(new JSONObject().put("fee_2019_08", rs.getString("fee_19_08")));
				_feeArr.put(new JSONObject().put("fee_2019_09", rs.getString("fee_19_09")));
				_feeArr.put(new JSONObject().put("fee_2019_10", rs.getString("fee_19_10")));
				_feeArr.put(new JSONObject().put("fee_2019_11", rs.getString("fee_19_11")));
				_feeArr.put(new JSONObject().put("fee_2019_12", rs.getString("fee_19_12")));
				_feeArr.put(new JSONObject().put("fee_2020_01", rs.getString("fee_20_01")));
				_feeArr.put(new JSONObject().put("fee_2020_02", rs.getString("fee_20_02")));
				_feeArr.put(new JSONObject().put("fee_2020_03", rs.getString("fee_20_03")));
				_feeArr.put(new JSONObject().put("fee_2020_04", rs.getString("fee_20_04")));
				_feeArr.put(new JSONObject().put("fee_2020_05", rs.getString("fee_20_05")));
				_feeArr.put(new JSONObject().put("fee_2020_06", rs.getString("fee_20_06")));

				removeEmptyJsonObj(_usageArr);
				removeEmptyJsonObj(_feeArr);

				jobj.put("usage", _usageArr);
				jobj.put("fee", _feeArr);

				_resultBuffer.append(jobj.toString() + ", ");
			}

		} catch (SQLException e) {
		  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
		}

		String strResult = _resultBuffer.toString();

		if(strResult.length() < 1) {
			return new JSONArray();
		}

		result = new JSONArray("[" + strResult.substring(0, strResult.lastIndexOf(",")) + "]");
		_resultBuffer = new StringBuffer();
		return result;
	}


	private void removeEmptyJsonObj(JSONArray target) {

		for(int i = target.length()-1; i >= 0; i--) {

			JSONObject targetObj = (JSONObject) target.get(i);

			if(targetObj.toString().trim().length() == 2) {
				target.remove(i);
			}
		}
	}


	private String getResult(JSONArray arrResult, JSONObject location, String template) {

		StringBuffer _resultBuffer = new StringBuffer();

		for(int i = 0 ; i < arrResult.length() ; i++) {

			JSONObject jobj = (JSONObject) arrResult.get(i);

			String id = jobj.getString("id");
			String household = jobj.getString("household");
			String meterNumber = "";
			String readMeterDate = jobj.getString("read_meter_date");
			String address = jobj.getString("address");
			String gauge = jobj.getString("gauge");
			String sewer = jobj.getString("sewer");

			id = refineId(id);

			try {
				meterNumber = jobj.getString("meter_number");
			}catch (Exception e) {

			}

			JSONArray usageArr = (JSONArray) jobj.get("usage");
			JSONArray feeArr = (JSONArray) jobj.get("fee");

			for(int j = 0 ; j < usageArr.length() ; j++) {
				JsonUtil _template = new JsonUtil(new JSONObject(template));
//				JSONObject defaultObj = new JSONObject();
//				_template.put("createdAt", defaultObj);

				JSONObject usageObj = (JSONObject) usageArr.get(j);
				JSONObject feeObj = (JSONObject) feeArr.get(j);

				String usageKey = usageObj.keys().next();
				String feeKey = feeObj.keys().next();

				String year = usageKey.split("_")[1];
				String month = usageKey.split("_")[2];

				_template.put("id", id);
				_template.put("household.value", Integer.parseInt(household));
//				_template.put("household.value", JsonUtil.nvl(household, DataType.INTEGER));
				_template.put("meterNumber.value", meterNumber);

				_template.put("createdAt", getTimeInfo(year, month, readMeterDate));
//				_template.put("createdAt.value", getTimeInfo(year, month, readMeterDate));
//				_template.put("createdAt.type", "Property");
				_template.put("gauge.value", Float.parseFloat(gauge + ".0"));
				_template.put("sewer.value", sewer);

				_template.put("address.value.addressCountry", ConfItem.getString("addressCountry"));
				_template.put("address.value.addressRegion", ConfItem.getString("addressRegion"));
				_template.put("address.value.addressLocality", ConfItem.getString("addressLocality"));
				_template.put("address.value.addressTown", address);
				_template.put("location.value.coordinates", location.get(address));

				_template.put("usage.value", Float.parseFloat(usageObj.get(usageKey).toString() + ".0"));
				_template.put("fee.value", Float.parseFloat(getFee(feeObj.get(feeKey).toString()) + ".0"));
//				_template.put("usage.value", JsonUtil.nvl(usageObj.get(usageKey).toString(), DataType.FLOAT));
//				_template.put("fee.value", JsonUtil.nvl(getFee(feeObj.get(feeKey).toString()), DataType.FLOAT));


				_template.remove("address.value.streetAddress");

				_resultBuffer.append(_template + ", ");
			}
		}
		return _resultBuffer.toString();
	}

	private int getFee(String fee) {

		if(fee.contains("E")){
			String feeArr[] = fee.split("E");
			feeArr[0] = feeArr[0].replace(".", "");
			while(feeArr[0].length() <= Integer.parseInt(feeArr[1])) {
				feeArr[0] = feeArr[0] + "0";
			}
			fee = feeArr[0];
		}

		return Integer.parseInt(fee);
	}

	private String refineId(String id) {

		String[] idContents = id.split("E");
		idContents[0] = idContents[0].replace(".", "");

		if("9".equals(idContents[1])) {
			idContents[0] = "00" + idContents[0];
		}else if("10".equals(idContents[1])) {
			idContents[0] =  "0" + idContents[0];
		}

		while(idContents[0].length() < 12) {
			idContents[0] = idContents[0] + "0";
		}

		StringBuffer idBuffer = new StringBuffer(idContents[0]);
		idBuffer.insert(10, "_");
		idBuffer.insert(6, "_");
		idBuffer.insert(3, "_");

		id = ConfItem.getString("id_prefix") + idBuffer.toString();

		return id;
	}

	private String getTimeInfo(String year, String month, String date) {

		if("02".equals(month) && "29".equals(date) ||"02".equals(month) && "30".equals(date) ||"02".equals(month) && "31".equals(date)) {
			date = "28";
		}

		LocalDate ld = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date));
		LocalTime lt = LocalTime.of(9, 0, 0);

		LocalDateTime dt = LocalDateTime.of(ld, lt);
	    ZoneOffset zo = ZonedDateTime.now().getOffset();
	    String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

	    return returnValue;
	}

	public static void connectDB(JSONObject databaseInfo) {

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
	}

	public static void disconnectDB(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		try {if(rs != null) {rs.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
		disconnectDB(conn, pstmt);
	}
	public static void disconnectDB(Connection conn, PreparedStatement pstmt) {
		try {if(pstmt != null) {pstmt.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
		try {if(conn != null) {conn.close();}}
		catch (Exception e) {log.error("Exception : "+ExceptionUtils.getStackTrace(e));}
	}
}
// end of class