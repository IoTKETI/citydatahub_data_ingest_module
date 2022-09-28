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

import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConvCCTV2 {

  JSONObject _confItem;
  JSONObject cctvInfo;
  JSONObject _template;
  String _cctvType;

  protected void init(JSONObject ConfItem, String template, String cctvType) {
    _confItem = ConfItem;
    _cctvType = cctvType;
    cctvInfo = new JSONObject(new JsonUtil(ConfItem).get("cctvs." + cctvType).toString());

    _template = new JSONObject(template);

    JSONObject locationObj = new JSONObject();
    locationObj.put("type", "GeoProperty");
    JSONObject locationValueObj = new JSONObject();
    locationValueObj.put("type", "Point");
    locationValueObj.put("coordinates", "");
    locationObj.put("value", locationValueObj);

    JSONObject addressObj = new JSONObject();
    addressObj.put("type", "Property");
    JSONObject addressValueObj = new JSONObject();
    addressValueObj.put("addressCountry", "");
    addressValueObj.put("addressRegion", "");
    addressValueObj.put("addressLocality", "");
    addressValueObj.put("streetAddress", "");
    addressValueObj.put("addressTown", "");
    addressObj.put("value", addressValueObj);

    JSONObject defaultJsonObj = new JSONObject();
    defaultJsonObj.put("type", "Property");
    defaultJsonObj.put("value", "");

    _template.put("distance", defaultJsonObj);
    _template.put("installedAt", defaultJsonObj);
    _template.put("hasEmergencyBell", defaultJsonObj);
    _template.put("fieldOfView", defaultJsonObj);
    _template.put("name", defaultJsonObj);
    _template.put("isRotatable", defaultJsonObj);
    _template.put("status", defaultJsonObj);
    _template.put("direction", defaultJsonObj);

    _template.put("location", locationObj);
    _template.put("address", addressObj);
  }

  public final String getResult() {

    Connection conn = getConnection();
    JSONArray jsonData = getData(conn);
    String refinedData = getRefinedData(jsonData);

    return refinedData;
  }

  protected Connection getConnection() {

    Connection conn = null;
    JSONObject databaseInfo = _confItem.getJSONObject("databaseInfo");

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

  protected abstract JSONArray getData(Connection conn);

  protected abstract String getRefinedData(JSONArray jsonData);

  protected void disconnectDB(Connection conn, PreparedStatement pstmt, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    disconnectDB(conn, pstmt);
  }

  protected void disconnectDB(Connection conn, PreparedStatement pstmt) {
    try {
      if (pstmt != null) {
        pstmt.close();
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  protected String getTimeInfo(LocalDate date, LocalTime time) {

    LocalDateTime dt = LocalDateTime.of(date, time);
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

    return returnValue;
  }
}
