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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWaterTapUsage_Legacy extends AbstractConvert {
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
    JSONObject location = ConfItem.getJSONObject("location");

    try {

      JSONObject databaseInfo = ConfItem.getJSONObject("databaseInfo");
      String className = databaseInfo.getString("className");
      String url = databaseInfo.getString("url");
      String user = databaseInfo.getString("user");
      String password = databaseInfo.getString("password");

      Class.forName(className);
      conn = DriverManager.getConnection(url, user, password);

      String sql = ConfItem.getString("query");

      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, ConfItem.getInt("limitNum"));
      pstmt.setInt(2, ConfItem.getInt("offsetNum"));
      rs = pstmt.executeQuery();

      while (rs.next()) {
        Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> wMap = new LinkedHashMap<>();

        int read_meter_date = rs.getInt("read_meter_date");

        Find_wMap(tMap, "gauge").put("value", rs.getDouble("gauge"));
        Find_wMap(tMap, "household").put("value", rs.getInt("household"));
        if (rs.getString("sewer").equals("X"))
          Find_wMap(tMap, "sewer").put("value", "FALSE");
        else if (rs.getString("sewer").equals("O"))
          Find_wMap(tMap, "sewer").put("value", "TRUE");
        Find_wMap(tMap, "usage").put("value", rs.getDouble("usage"));
        Find_wMap(tMap, "fee").put("value", rs.getDouble("fee"));
        Find_wMap(tMap, "meterNumber").put("value", rs.getString("meter_number"));

        String addressTown = rs.getString("address");

        id = refineId(rs.getString("id")) + "_" + rs.getString("yearmonth") + read_meter_date;

        wMap = (Map) tMap.get("dataProvider");
        wMap.put("value", ConfItem.getString("dataProvider"));

        wMap = (Map) tMap.get("globalLocationNumber");
        wMap.put("value", id);

        Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
        addrValue.put("addressCountry", ConfItem.getString("addressCountry"));
        addrValue.put("addressRegion", ConfItem.getString("addressRegion"));
        addrValue.put("addressLocality", ConfItem.getString("addressLocality"));
        addrValue.put("addressTown", addressTown);
        addrValue.put("streetAddress", "");

        Map<String, Object> locMap = (Map) tMap.get("location");
        locMap.put("observedAt", DateUtil.getTime());
        Map<String, Object> locValueMap = (Map) locMap.get("value");
        locValueMap.put("coordinates", location.get(addressTown));

        tMap.put("id", id);

        rtnList.add(tMap);
        String str = objectMapper.writeValueAsString(tMap);
        log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
      }

      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (SQLException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));

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
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }

    return rtnStr;
  } // end of doit

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

  private String refineId(String id) {

    while (id.length() < 12) {
      id = "0" + id;
    }
    StringBuffer idBuffer = new StringBuffer(id);
    idBuffer.insert(10, "_");
    idBuffer.insert(6, "_");
    idBuffer.insert(3, "_");
    id = ConfItem.getString("id_prefix") + idBuffer.toString();
    return id;
  }
}
// end of class