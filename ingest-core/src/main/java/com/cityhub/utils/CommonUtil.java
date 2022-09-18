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
package com.cityhub.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {


  public String getJarPath() {
    File JarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    String JarFilePath = JarFile.getAbsolutePath();
    String result = JarFilePath.replace(JarFile.getName(), "");
    return result.toString();
  }

  public static Map<String, Object> GetDbAddress(JSONObject iSvc) throws Exception {

    String DBClassName = iSvc.getString("DBClassName");
    String DBurl = iSvc.getString("DBurl");
    String DBuser = iSvc.getString("DBuser");
    String DBpassword = iSvc.getString("DBpassword");
    String DBTable = iSvc.getString("DBTable");

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Class.forName(DBClassName);
    conn = DriverManager.getConnection(DBurl, DBuser, DBpassword);
    String sql = "select * from data_service_broker.\"" + DBTable + "\"";
    pstmt = conn.prepareStatement(sql);
    rs = pstmt.executeQuery();
    Map<String, Object> DbIdMap = new LinkedHashMap<>();

    while (rs.next()) {
      if(!rs.wasNull()) {
        Map<String, Object> DbIdMapAddress = new LinkedHashMap<>();

        if (rs.getString("address_addresscountry").equals("") || rs.getString("address_addressregion").equals("")
            || rs.getString("address_addresslocality").equals("")
            || rs.getString("address_addresstown").equals("")
            || rs.getString("address_streetaddress").equals("")) {
          continue;
        }

        DbIdMapAddress.put("address_addresscountry", rs.getString("address_addresscountry"));
        DbIdMapAddress.put("address_addressregion", rs.getString("address_addressregion"));
        DbIdMapAddress.put("address_addresslocality", rs.getString("address_addresslocality"));
        DbIdMapAddress.put("address_addresstown", rs.getString("address_addresstown"));
        DbIdMapAddress.put("address_streetaddress", rs.getString("address_streetaddress"));
        DbIdMapAddress.put("modified_at", rs.getString("modified_at"));
        DbIdMap.put(rs.getString("id"), DbIdMapAddress);
      }
    }
    return DbIdMap;
  }

  public static void KakaoMapCoord2regioncode(Map<String, Object> tMap, String key, Double x, Double y)
      throws Exception {
    JsonUtil KakaoMap = new JsonUtil((JSONObject) KakaoMap_getData(
        "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?input_coord=WGS84&x=" + x + "&y=" + y, key));
    if (KakaoMap.has("documents")) {
      JSONArray arrList = KakaoMap.getArray("documents");

      for (Object obj : arrList) {
        JSONObject item = (JSONObject) obj;
        if (item.optString("region_type").equals("H")) {
          Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
          addrValue.put("addressCountry", "대한민국");
          addrValue.put("addressRegion", item.optString("region_1depth_name"));
          addrValue.put("addressLocality", item.optString("region_2depth_name"));
          addrValue.put("addressTown", item.optString("region_3depth_name"));
          if (item.optString("region_4depth_name").equals("")) {
            addrValue.put("streetAddress", item.optString("address_name"));
          } else {
            addrValue.put("streetAddress", item.optString("region_4depth_name"));
          }
        }
      }
    }
  }

  public static void KakaoMapCoord2address(Map<String, Object> tMap, String key, Double x, Double y)
      throws Exception {
    JsonUtil KakaoMap = new JsonUtil((JSONObject) KakaoMap_getData(
        "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?input_coord=WGS84&output_coord=WGS84&x=" + x
            + "&y=" + y,
        key));
    if (KakaoMap.has("documents")) {
      JSONArray arrList = KakaoMap.getArray("documents");

      JSONObject List = (JSONObject) arrList.getJSONObject(0);

      JSONObject address = (JSONObject) List.getJSONObject("address");
      Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
      addrValue.put("addressCountry", "대한민국");
      addrValue.put("addressRegion", address.getString("region_1depth_name"));
      addrValue.put("addressLocality", address.getString("region_2depth_name"));
      addrValue.put("addressTown", address.getString("region_3depth_name"));

      if (!List.isNull("road_address")) {
        JSONObject RoadAddress = (JSONObject) List.getJSONObject("road_address");

        addrValue.put("streetAddress", RoadAddress.getString("address_name"));
      } else {
        if (address.getString("sub_address_no").equals("")) {
          addrValue.put("streetAddress", address.getString("main_address_no"));
        } else {
          addrValue.put("streetAddress",
              address.getString("main_address_no") + "-" + address.getString("sub_address_no"));
        }
      }

    }
  }

  public static Object KakaoMap_getData(String url, String key) throws Exception {
    Object obj = null;

    HttpResponse resp = OkUrlUtil.get(url, "Authorization", key);
    String payload = resp.getPayload();
    if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301) {
      if (payload.startsWith("{")) {
        obj = new JSONObject(resp.getPayload());
      } else if (payload.startsWith("[")) {
        obj = new JSONArray(resp.getPayload());
      } else if (payload.toLowerCase().startsWith("<")) {
        obj = XML.toJSONObject(resp.getPayload());
      } else {
        obj = resp.getPayload();
      }
    } else {
      throw new Exception(resp.getStatusName());
    }
    return obj;
  }


  /**
   * 동적 클래스 실행
   * @param clsNm
   * @param ConfItem
   * @param templateItem
   * @param message
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static JSONArray runIt(String clsNm, JSONObject ConfItem, JSONObject templateItem, byte[] message) {
    JSONArray rtn = null;
    try {
      Class<?> clz = Class.forName(clsNm);
      Object instanceObject = clz.getDeclaredConstructor().newInstance();
      Class[] methodParamClass = {JSONObject.class, JSONObject.class, byte[].class};
      Object[] methodParamObject = {ConfItem, templateItem, message};
      Method mth = clz.getDeclaredMethod("doit", methodParamClass );
      rtn = (JSONArray) mth.invoke(instanceObject, methodParamObject );
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return rtn;
  }
  /**
   * 동적 클래스 실행
   * @param clsNm
   * @param ConfItem
   * @param templateItem
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static JSONArray runIt(String clsNm, JSONObject ConfItem, JSONObject templateItem) {
    JSONArray rtn = null;
    try {
      Class<?> clz = Class.forName(clsNm);
      Object instanceObject = clz.getDeclaredConstructor().newInstance();
      Class[] methodParamClass = {JSONObject.class, JSONObject.class};
      Object[] methodParamObject = {ConfItem, templateItem};
      Method mth = clz.getDeclaredMethod("doit", methodParamClass );
      rtn = (JSONArray) mth.invoke(instanceObject, methodParamObject );
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return rtn;
  }
  /**
   * http connection 으로 데이터 가져오기
   * @param svc
   * @return
   */
  public static Object httpConnection(JSONObject svc) {
    Object obj = null;
    URL url = null;
    HttpURLConnection conn = null;
    String jsonData = "";
    BufferedReader br = null;
    StringBuffer sb = null;
    int responseCode;
    try {
      String targetUrl = urlAssemble(svc.getString("url_addr"), svc);
      log.info("+++targetUrl:{}",targetUrl);
      url = new URL(targetUrl);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Accept-Charset", "utf-8");
      conn.setRequestProperty("Content-Type", "application/json;charset-UTF-8");
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setConnectTimeout(10);
      conn.setReadTimeout(10);

      responseCode = conn.getResponseCode();
      log.info("+++response:{}",responseCode);
      if (responseCode < 400) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
        br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }
      sb = new StringBuffer();
      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }
      log.info("+++payload:{}", sb.toString());
      String payload = sb.toString();
      if (payload.startsWith("{")) {
        obj = new JSONObject(payload);
      } else if (payload.startsWith("[")) {
        obj = new JSONArray(payload);
      } else if (payload.toLowerCase().startsWith("<")) {
        obj = XML.toJSONObject(payload);
      } else {
        obj = payload;
      }
    } catch (IOException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } finally {
    }
    return obj;
  }

  /**
   * url 커넥션으로 데이터 가져오기
   * @param svc
   * @return
   * @throws Exception
   */
  public static Object getData(JSONObject svc) throws Exception {
    Object obj = null;
    String url = urlAssemble(svc.getString("url_addr"), svc);
    log.info("+++url:{}",url);
    HttpResponse resp = OkUrlUtil.get(url, "Content-type", "application/json");
    String payload = resp.getPayload();
    if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301 ) {
      if (payload.startsWith("{")) {
        obj = new JSONObject(resp.getPayload());
      } else if (payload.startsWith("[")) {
        obj = new JSONArray(resp.getPayload());
      } else if (payload.toLowerCase().startsWith("<")) {
        obj = XML.toJSONObject(resp.getPayload());
      } else {
        obj = resp.getPayload();
      }
    }  else {
      throw new Exception(resp.getStatusName());
    }
    return obj;
  }

  /**
   * url 커넥션으로 데이터 가져오기
   * @param svc
   * @param url_addr
   * @return
   * @throws Exception
   */
  public static Object getData(JSONObject svc, String url_addr) throws Exception {
    Object obj = null;
    String url = urlAssemble(url_addr, svc);

    HttpResponse resp = OkUrlUtil.get(url, "Content-type", "application/json");
    String payload = resp.getPayload();
    if (resp.getStatusCode() >= 200 && resp.getStatusCode() < 301 ) {
      if (payload.startsWith("{")) {
        obj = new JSONObject(resp.getPayload());
      } else if (payload.startsWith("[")) {
        obj = new JSONArray(resp.getPayload());
      } else if (payload.toLowerCase().startsWith("<")) {
        obj = XML.toJSONObject(resp.getPayload());
      } else {
        obj = resp.getPayload();
      }
    }  else {
      throw new Exception(resp.getStatusName());
    }
    return obj;
  }

  /**
   * url 구성을 위한 규칙 파싱
   * @param url
   * @param svc
   * @return
   */
  private static String urlAssemble(String url, JSONObject svc) {
    if (svc.has("key_name") && svc.has("key_value")) {
      url += "&" + svc.getString("key_name") + "=" + svc.getString("key_value");
    }

    if (svc.has("PathVariable")) {
      String[] sp = svc.getString("PathVariable").split(",",-1);
      for (String it : sp) {
        if (!"".equals(it) && svc.has(it)) {
          String value = svc.getString(it);

          if (value.indexOf(",") > -1) {
            String[] val = value.split(",",-1);
            if (DateUtil.isPatternDate(val[0])) {
              url += DateUtil.addDate(DateUtil.getChronoUnit(val[1]), Integer.parseInt(val[2]), val[0]) + "/";
            }

          } else {
            if (DateUtil.isPatternDate(value)) {
              url += DateUtil.getDate(value) + "/";
            } else if (!"".equals(value)) {
              url += value + "/";
            }
          }
        }
      }
    }

    if (svc.has("ParamVariable")) {

      String[] sp = svc.getString("ParamVariable").split(",",-1);
      String value = "";
      try {
        for (String it : sp) {
          if (!"".equals(it) && svc.has(it)) {
            value = svc.getString(it) + "";
            if (value.indexOf(",") > -1) {
              String[] val = value.split(",",-1);
              if (DateUtil.isPatternDate(val[0])) {
                url += "&" + it + "=" + DateUtil.addDate(DateUtil.getChronoUnit(val[1]), Integer.parseInt(val[2]), val[0]);
              }

            } else {
              if (DateUtil.isPatternDate(value)) {
                url += "&" + it + "=" + DateUtil.getDate(value);
              } else {
                url += "&" + it + "=" + URLEncoder.encode(value, "UTF-8");
              }

            }
          } else {
            if (svc.has("key")) {
              url += "&" + it + "=" + URLEncoder.encode(svc.getString("key"), "UTF-8") ;
            }
          }
        }
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }

    }
    return url;
  }




} // end class