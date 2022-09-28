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
package com.cityhub.web.agent.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.exception.BadRequestException;
import com.cityhub.utils.DataCoreCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.UrlUtil;
import com.cityhub.web.agent.service.MonitoringService;
import com.cityhub.web.config.ConfigEnv;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
@RequestMapping({ "/monitor" })
public class MonitoringController {

  @Autowired
  MonitoringService svc;

  @Autowired
  ConfigEnv configEnv;

  @GetMapping({ "/dashView" })
  public ModelAndView dashView(HttpServletRequest request, HttpServletResponse response) {

    log.debug("----- MonitoringController.dashView() -----");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("dashView");

    return modelAndView;
  }

  // 스케줄주석
  // @Scheduled(fixedDelay = 60000)
  @GetMapping({ "/getLoggerAll" })
  public ResponseEntity<String> getLoggerAll() {
    log.debug("----- MonitoringController.getLoggerAll() -----");
    String rtnMsg;
    JSONArray jsonArray = null;

    try {
      Map result = svc.selectLastLogDt();
      log.debug("last_dt : " + result.get("last_dt").toString());

      JsonObject rtnJob = new JsonObject();
      rtnJob.addProperty("searchDate", result.get("last_dt").toString());

      rtnMsg = httpConnection(configEnv.getDaemonSrv() + "/getLoggerAll", rtnJob.toString());

      jsonArray = new JSONArray(rtnMsg);
      log.debug("jsonArray.size() : " + jsonArray.length());

      int maxSize = 10000;
      if (maxSize > jsonArray.length()) {
        maxSize = jsonArray.length();
      }

      for (int i = 0; i < maxSize; i++) {

        JsonObject jobj = (JsonObject) jsonArray.get(i);
        log.debug("jobj : " + jobj);
        String timestamp = jobj.get("timestamp").toString().replaceAll("\"", "");
        String payload = jobj.get("payload").toString().replaceAll("\"", "");
        String type = jobj.get("type").toString().replaceAll("\"", "");
        String step = jobj.get("step").toString().replaceAll("\"", "");
        String id = jobj.get("id").toString().replaceAll("\"", "");
        String sourceName = jobj.get("sourceName").toString().replaceAll("\"", "");
        String length = String.valueOf(jobj.get("length")).replaceAll("\"", "");
        String desc = jobj.get("desc").toString().replaceAll("\"", "");
        String adapterType = String.valueOf(jobj.get("adapterType")).replaceAll("\"", "");
        Map param = new HashMap();
        param.put("timestamp", timestamp);
        param.put("payload", payload);
        param.put("type", type);
        param.put("step", step);
        param.put("id", id);
        param.put("sourceName", sourceName);
        if ("null".equals(length)) {
          length = "0";
        }
        param.put("length", Integer.parseInt(length));
        param.put("desc", desc);

        if ("com.cityhub.adapter.convex.OneM2M".equals(adapterType)) {
          param.put("adapterType", "OneM2M");
        } else if ("com.cityhub.adapter.convex.OpenApiSource".equals(adapterType)) {
          param.put("adapterType", "OpenAPI");
        } else if ("com.cityhub.adapter.convex.LegacyPlatform".equals(adapterType)) {
          param.put("adapterType", "LegacyPlatform");
        } else if ("com.cityhub.adapter.convex.UrbanintegrationPlatform".equals(adapterType)) { //
          param.put("adapterType", "도시통합Platform");
        } else if ("com.cityhub.adapter.convex.Fiware".equals(adapterType)) {
          param.put("adapterType", "Fiware");
        } else {
          param.put("adapterType", "기타");
        }

        svc.insertConnectivityLog(param);
      }
    } catch (Exception e) {
      return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
    }
    return new ResponseEntity(jsonArray.length(), HttpStatus.OK);
  }

  private Map<String, String> requestToMap(HttpServletRequest request) {
    Map<String, String> param = new HashMap();
    try {
      Enumeration params = request.getParameterNames();
      while (params.hasMoreElements()) {
        String name = (String) params.nextElement();
        param.put(name, request.getParameter(name));
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return param;
  }

  @GetMapping("/selectHourSF")
  public ResponseEntity<List<Map>> selectHourSF(HttpServletRequest request, HttpServletResponse response) {
    log.debug("----- MonitoringController.selectHourSF() -----");
    List<Map> result = null;
    try {
      Map<String, String> param = requestToMap(request);
      result = svc.selectHourSF(param);
      log.debug("result :{} {}", param, result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/selectFailType")
  public ResponseEntity<List<Map>> selectFailType(HttpServletRequest request, HttpServletResponse response) {
    log.debug("----- MonitoringController.selectFailType() -----");
    List<Map> result = null;
    try {
      Map<String, String> param = requestToMap(request);
      result = svc.selectFailType(param);
      log.debug("result :{} {}", param, result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/selectTypeSF")
  public ResponseEntity<List<Map>> selectTypeSF(HttpServletRequest request, HttpServletResponse response) {
    log.debug("----- MonitoringController.selectTypeSF() -----");
    List<Map> result = null;
    try {
      Map<String, String> param = requestToMap(request);
      result = svc.selectTypeSF(param);
      log.debug("result :{} {}", param, result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/selectTypeCnt")
  public ResponseEntity<List<Map>> selectTypeCnt(HttpServletRequest request, HttpServletResponse response) {
    log.debug("----- MonitoringController.selectTypeCnt() -----");
    List<Map> result = null;
    try {
      Map<String, String> param = requestToMap(request);
      result = svc.selectTypeCnt(param);
      log.debug("result :{} {}", param, result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping({ "/agent/view" })
  public ModelAndView agentView(HttpServletRequest request, HttpServletResponse response) {

    log.debug("----- MonitoringController.agentView() -----");
    HttpSession session = request.getSession();
    String tokenCheck = (String) session.getAttribute("token");
    log.debug(tokenCheck);

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("monitoring");

    return modelAndView;
  }

  @GetMapping("/agent")
  public ResponseEntity<List<Map>> agent(HttpServletRequest request, HttpServletResponse response) {
    log.debug("----- MonitoringController.agent() -----");
    List<Map> result = null;
    try {
      result = svc.selectAll();
      log.debug("result : {}", result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/agent/{id}/view")
  public ModelAndView agentDetailView(@PathVariable("id") String id, HttpServletResponse response) {
    log.debug("----- MonitoringController.agentDetailView() -----");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("agent_id", id);
    modelAndView.setViewName("monitoring_adapter");
    return modelAndView;
  }

  @GetMapping("/agent/{id}")
  public ResponseEntity<Map> agentDetail(@PathVariable("id") String id, HttpServletResponse response) {
    log.debug("----- MonitoringController.agentDetail() -----");
    Map result = svc.getById(id);

    if (result == null) {
      throw new BadRequestException(DataCoreCode.ErrorCode.NOT_EXIST_ID, "");
    } else {
      List<Map> list = svc.selectAllAdaptorId(id);
      result.put("list", list);
      log.debug("result : {}", result);
    }

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping({ "/popup/popupMonitorAdapter" })
  public ModelAndView popupMonitorAdapter(HttpServletRequest request, HttpServletResponse response) {

    log.debug("----- MonitoringController.popupMonitorAdapter() -----");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("popup/popupMonitorAdapter");

    return modelAndView;
  }

  @GetMapping("/status/{agentId}")
  public ResponseEntity<String> statusAgent(@PathVariable("agentId") String agentId, HttpServletResponse response) {
    log.debug("----- MonitoringController.statusAgent() -----");
    HttpResponse resp = null;
    try {
      resp = OkUrlUtil.get(configEnv.getDaemonSrv() + "/exec/status/" + agentId, "Content-Type", "application/json");
    } catch (Exception e) {
      response.setStatus(HttpStatus.NO_CONTENT.value());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @PostMapping({ "/popup/popupDashLog" })
  public ModelAndView popupDashLog(HttpServletRequest request, HttpServletResponse response) {

    log.debug("----- MonitoringController.popupDashLog() -----");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("popup/popupDashLog");

    List<Map> stList = null;
    try {

      stList = new ArrayList<>();
      log.debug(configEnv.getDataModelApiUrl() + "?level=000&");
      HttpResponse resp = UrlUtil.get(configEnv.getDataModelApiUrl() + "?level=000", "Content-type", "application/json");
      JSONArray jsonarr = new JSONArray(resp.getPayload());

      for (int i = 0; i < jsonarr.length(); i++) {
        JSONObject jsonObject = jsonarr.getJSONObject(i);
        Map tmpMap = new HashMap();
        tmpMap.put("stId", jsonObject.getString("id"));
        tmpMap.put("stNm", jsonObject.getString("name"));
        String[] tmpStr1 = jsonObject.getString("creationTime").split("\\.");
        String[] tmpStr2 = tmpStr1[0].split("T");
        tmpMap.put("creationTime", tmpStr2[0] + " " + tmpStr2[1]);
        tmpMap.put("useYn", jsonObject.getString("useYn"));
        stList.add(tmpMap);
      }

      log.debug("result : {}", stList);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    modelAndView.addObject("stList", stList);

    return modelAndView;
  }

  public String httpConnection(String targetUrl, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    String result = "";
    JSONObject json = null;
    String jsonData = "";
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    int responseCode;

    try {

      log.debug(jsonBody);
      url = new URL(targetUrl);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Accept-Charset", "utf-8");
      conn.setRequestProperty("Content-Type", "application/json;charset-UTF-8");
      conn.setRequestProperty("X-EISS-CREDENTIAL", "ABCDEFGHIJHL");
      conn.setRequestProperty("X-EISS-MI", "TransactionId145444");
      conn.setRequestMethod("POST");

      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonBody);
        wr.flush();
      }

      responseCode = conn.getResponseCode();

      if (responseCode < 400) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
        br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }

      sb = new StringBuffer();

      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }

      returnText = sb.toString();

    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }
    return returnText;
  }

  @GetMapping("/selectLog")
  public ResponseEntity<List<Map>> selectLog(@RequestParam(value = "adapter_id", required = false, defaultValue = "") String adapter_id,
      @RequestParam(value = "adapter_type", required = false, defaultValue = "") String adapter_type, @RequestParam(value = "st_datamodel", required = false, defaultValue = "") String st_datamodel,
      @RequestParam(value = "step", required = false, defaultValue = "") String step, @RequestParam(value = "log_desc", required = false, defaultValue = "") String log_desc) {
    log.debug("----- MonitoringController.selectLog() -----");
    List<Map> result = null;
    Map param = new HashMap();
    try {

      param.put("adapter_id", adapter_id);
      param.put("adapter_type", adapter_type);
      param.put("st_datamodel", st_datamodel);
      param.put("step", step);
      param.put("log_desc", log_desc);

      result = svc.selectLog(param);
      log.debug("result : {}", result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

}
