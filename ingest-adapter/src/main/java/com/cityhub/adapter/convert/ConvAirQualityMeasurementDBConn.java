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
import java.util.ArrayList;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAirQualityMeasurementDBConn extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();

    try {
      String model = ConfItem.getString("model_id");
      JsonUtil templateJsonUtil = new JsonUtil(templateItem.getJSONObject(model).toString());
      String maxTime = getMaxTime();
      setAirQualityMeasurementDataToSendJson(maxTime, sendJson, templateJsonUtil);

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

  private void setAirQualityMeasurementDataToSendJson(String maxTime, StringBuffer sendJson, JsonUtil templateJsonUtil) {
    Connection conn = getConnection(ConfItem.getJSONObject("databaseInfoForGetData"));
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = conn.prepareStatement(ConfItem.getString("queryForGetData"));
      pstmt.setString(1, maxTime);

      rs = pstmt.executeQuery();
      while (rs.next()) {
        JsonUtil ju = new JsonUtil(templateJsonUtil.toString());
        ju.put("pm25.value", JsonUtil.nvl(rs.getString("pm25"), DataType.FLOAT));
        ju.put("temper.value", JsonUtil.nvl(rs.getString("temper"), DataType.FLOAT));
        ju.put("pm10.value", JsonUtil.nvl(rs.getString("pm10"), DataType.FLOAT));
        ju.put("tod.value", JsonUtil.nvl(rs.getString("tod"), DataType.FLOAT));
        ju.put("h2s.value", JsonUtil.nvl(rs.getString("h2s"), DataType.FLOAT));
        ju.put("nh3.value", JsonUtil.nvl(rs.getString("nh3"), DataType.FLOAT));
        ju.put("humid.value", JsonUtil.nvl(rs.getString("humid"), DataType.FLOAT));
        ju.put("vocs.value", JsonUtil.nvl(rs.getString("vocs"), DataType.FLOAT));
        ju.put("observedAt.value", JsonUtil.nvl(rs.getString("observed_at"), DataType.STRING));
        ju.put("deviceId.value", JsonUtil.nvl(rs.getString("deviceId"), DataType.STRING));

        Float latitude = (Float) JsonUtil.nvl(rs.getString("latitude"), DataType.FLOAT);
        Float longitude = (Float) JsonUtil.nvl(rs.getString("longitude"), DataType.FLOAT);
        ArrayList<Float> location = new ArrayList<>();
        location.add(latitude);
        location.add(longitude);
        ju.put("location.value.coordinates", location);

        String deviceType = "stationary"; // stationary, mobile, portable 중 일단 stationary로..

        ju.put("deviceType.value", JsonUtil.nvl(deviceType, DataType.STRING));
        ju.put("id", ConfItem.getString("gs1Code") + rs.getString("idSurffix"));

        ju.put("address.value.addressCountry", ConfItem.getString("addressCountry"));
        ju.put("address.value.streetAddress", ConfItem.getString("streetAddress"));
        ju.put("address.value.addressLocality", ConfItem.getString("addressLocality"));
        ju.put("address.value.addressRegion", ConfItem.getString("addressRegion"));
        ju.put("address.value.addressTown", ConfItem.getString("addressTown"));

        sendJson.append(ju.toString() + ", ");
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      disConnection(conn, pstmt, rs);
    }
  }

  private String getMaxTime() {
    Connection conn = getConnection(ConfItem.getJSONObject("databaseInfoForGetTimeMax"));
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String maxTime = "";
    try {
      pstmt = conn.prepareStatement(ConfItem.getString("queryForTimeMax"));
      rs = pstmt.executeQuery();

      while (rs.next()) {
        maxTime = rs.getString("maxTime");
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      disConnection(conn, pstmt, rs);
    }

    return maxTime;
  }

  private Connection getConnection(JSONObject databaseInfo) {

    Connection conn = null;

    try {
      String className = databaseInfo.getString("className");
      String url = databaseInfo.getString("url");
      String user = databaseInfo.getString("user");
      String password = databaseInfo.getString("password");

      Class.forName(className);
      conn = DriverManager.getConnection(url, user, password);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return conn;
  }

  private void disConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
    if (pstmt != null) {
      try {
        pstmt.close();
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
    if (conn != null) {
      try {
        conn.close();
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
  }

}
// end of class