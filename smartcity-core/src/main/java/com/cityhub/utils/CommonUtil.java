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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {

  /**
   * 자르 파일 경로 가져오기
   * @return
   */
  public String getJarPath() {
    File JarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    String JarFilePath = JarFile.getAbsolutePath();
    String result = JarFilePath.replace(JarFile.getName(), "");
    return result.toString();
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