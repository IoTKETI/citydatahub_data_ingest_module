package com.cityhub.adapter.convert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAdvancedDisinfectionSystem extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    StringBuffer sendJson = new StringBuffer();
    String sql = ConfItem.get("query").toString();

    try {

      JSONArray svcList = ConfItem.getJSONArray("serviceList");
      String model = ConfItem.getString("model_id");

      for (int i = 0; i < svcList.length(); i++) {

        // conf파일에서 serviceList를 가져옴.
        JSONObject iSvc = (JSONObject) svcList.get(i);
        // gs1Code의 콜론 뒷 부분으로 Id를 추출함.
        String gs1Code = iSvc.getString("gs1Code");
        String deviceId = gs1Code.substring(gs1Code.lastIndexOf(":") + 1);
        String encodedId = "";
        // Id Decode
        for (int j = 0; j < deviceId.length(); j++) {
          char temp = deviceId.charAt(j);
          int tempInt = temp;
          String hexInt = Integer.toHexString(tempInt);
          encodedId += hexInt;
        }

        // 조건을 Id로 하여 쿼리
        conn = getConnection();
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, encodedId);
        rs = pstmt.executeQuery();
        JSONArray jsonArr = new JSONArray();

        while (rs.next()) {
          JSONObject jobj = new JSONObject();
          jobj.put("con", rs.getString("con"));
          jobj.put("in_time", rs.getString("in_time"));
          jsonArr.put(jobj);
        }

        if (jsonArr.length() < 1) {
          continue;
        }

        log(SocketCode.DATA_RECEIVE, id, jsonArr.toString().getBytes()); // 데이터 받음

        log(SocketCode.DATA_CONVERT_REQ, id, jsonArr.toString().getBytes()); // 데이터 요청(시작?)
//        JSONObject template = templateItem.getJSONObject(model);
        JsonUtil templateJsonUtil = new JsonUtil(templateItem.getJSONObject(model).toString());

        templateJsonUtil.put("address.value.addressCountry", iSvc.get("addressCountry").toString());
        templateJsonUtil.put("address.value.addressRegion", iSvc.get("addressRegion").toString());
        templateJsonUtil.put("address.value.addressLocality", iSvc.get("addressLocality").toString());
        templateJsonUtil.put("address.value.addressTown", iSvc.get("addressTown").toString());
        templateJsonUtil.put("address.value.streetAddress", iSvc.get("streetAddress").toString());
        templateJsonUtil.put("location.value.coordinates", iSvc.getJSONArray("location"));
        templateJsonUtil.put("id", iSvc.get("gs1Code").toString());
        templateJsonUtil.put("w3wplus", iSvc.get("w3wplus").toString());
        templateJsonUtil.put("globalLocationNumber", iSvc.get("globalLocationNumber").toString());

        for (Object obj : jsonArr) {

          JSONObject jData = (JSONObject) obj;

          if (jData == null || jData.get("con") == null) {
            continue;
          }

          setData(jData, templateJsonUtil);

          sendJson.append(templateJsonUtil.toString() + ",");
        }
      }

      log(SocketCode.DATA_CONVERT_SUCCESS, id, sendJson.toString().getBytes()); // 데이터 변환 성공
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    } finally {
      disconnectDB(conn, pstmt, rs);
    }

    return sendJson.toString();
  }

  private void setData(JSONObject jData, JsonUtil templateJsonUtil) {
    String con = jData.getString("con").toString();
    decodeData(con, templateJsonUtil);
  }

  private void decodeData(String con, JsonUtil templateJsonUtil) {

    JSONArray byteLength = (JSONArray) ConfItem.get("commonByteLength");
    JSONArray property = (JSONArray) ConfItem.get("commonProperty");

    String _event = "";

    for (int i = 0; i < byteLength.length(); i++) {

      String key = property.getString(i);
      int _byteLength = byteLength.getInt(i);
      String value = con.substring(0, 2 * _byteLength);

      if ("event".equals(key)) {
        _event = value;
      }

      inputDataToTemplate(key, value, templateJsonUtil);

      con = con.substring(2 * _byteLength);
    }

    byteLength = ConfItem.getJSONObject("eventByteLength").getJSONArray(_event);
    property = ConfItem.getJSONObject("eventProperty").getJSONArray(_event);

    for (int i = 0; i < byteLength.length(); i++) {
      String key = property.getString(i);
      int _byteLength = byteLength.getInt(i);
      String value = con.substring(0, 2 * _byteLength);

      inputDataToTemplate(key, value, templateJsonUtil);

      con = con.substring(2 * _byteLength);
    }

  }

  private void inputDataToTemplate(String key, String value, JsonUtil templateJsonUtil) {

    if ("id".equals(key)) {

      String buffer = "";
      int valueLength = value.length();
      for (int i = 0; i < valueLength; i += 2) {
        int tempInt = Integer.parseInt(value.substring(0, 2), 16);
        value = value.substring(2);
        buffer += (char) tempInt;
      }

    } else if ("event".equals(key)) {

      JSONObject eventType = ConfItem.getJSONObject("eventType");
      templateJsonUtil.put("event", eventType.getString(value));

    } else if ("createdAt".equals(key)) {

      String valueBuffer = "";

      long hexToLong = Long.parseLong(value, 16) * 1000;
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      valueBuffer = DateUtil.getISOTime(sdf.format(new Date(hexToLong)));
//      valueBuffer = new Date(hexToLong).toString();
//      jsonEx.put("finishedAt.value", DateUtil.getISOTime(row.get("finishedAt")));

//      JSONObject observedAt = new JSONObject();
//      observedAt.put("type", "Property");
//      observedAt.put("value", valueBuffer);
//      observedAt.put("value", valueBuffer);

      templateJsonUtil.put("observedAt", valueBuffer);

    } else if (!"id".equals(key) && templateJsonUtil.has(key)) {

      String valueBuffer = "";

      valueBuffer = Integer.parseInt(value, 16) + "";

      int valueBufferLength = valueBuffer.length();
      for (int i = 0; i < valueBufferLength - 1; i++) {

        if ("0".equals(valueBuffer.substring(0, 1))) {
          valueBuffer = valueBuffer.substring(1);
        } else {
          break;
        }
      } // end of for valueBufferLength

      templateJsonUtil.put(key + ".value", valueBuffer);
    }

  }

  private String getTime(String date) {
    LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    return OffsetDateTime.of(ldt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();
  }

  private Connection getConnection() {

    JSONObject databaseInfo = ConfItem.getJSONObject("databaseInfo");
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

  } // end of doit

  private void disconnectDB(Connection conn, PreparedStatement pstmt, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    disconnectDB(conn, pstmt);
  }

  private void disconnectDB(Connection conn, PreparedStatement pstmt) {
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

}
// end of class