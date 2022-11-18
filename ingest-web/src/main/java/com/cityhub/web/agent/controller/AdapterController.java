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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.utils.DataCoreCode.ResponseCode;
import com.cityhub.utils.JsonUtil;
import com.cityhub.web.agent.service.AdapterService;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
public class AdapterController {

  public static final String FIELD_HEX = (char) 0xD1 + "";

  @Autowired
  AdapterService as;

  @GetMapping({ "/adapterDetailtest" })
  public ModelAndView AdapterListViewtest() {
    log.debug("----- AdapterController.AdapterListView() -----");
    ModelAndView modelAndView = new ModelAndView();
    try {
      List<Map> combo = as.type_search();
      modelAndView.addObject("combo", combo);
      modelAndView.setViewName("adapterDetai_test");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return modelAndView;
  }

  @GetMapping({ "/adapterDetail" })
  public ModelAndView AdapterListView() {
    log.debug("----- AdapterController.AdapterListView() -----");
    ModelAndView modelAndView = new ModelAndView();
    try {
      List<Map> combo = as.type_search();
      modelAndView.addObject("combo", combo);
      modelAndView.setViewName("adapterDetail");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return modelAndView;
  }

  @GetMapping({ "/adtView", "/adaptorType" })
  public ResponseEntity<List<Map>> adtView() {
    log.debug("----- AdapterController.adtView() -----");
    List<Map> result = new ArrayList<>();
    try {
      result = as.getAll();
      log.debug("result : {}", result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/valueTypeSearch")
  public ResponseEntity<List<Map>> value_type() {
    List<Map> result = new ArrayList();
    try {
      result = as.value_type_search();
      log.debug("result : {}", result);
      if (result == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping({ "/adtTypeSave", "/adaptorType" })
  public ResponseEntity<Map> adtTypeSave(@RequestBody Map param) {
    log.debug("----- AdapterController.adtTypeSave() -----");
    try {
      as.insertAdaptorType(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PutMapping({ "/adtTypeSave", "/adaptorType/{id}" })
  public ResponseEntity<Map> adtTypeSave2(@RequestBody Map param) {
    log.debug("----- AdapterController.adtTypeSave() -----");
    try {
      as.insertAdaptorType(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @DeleteMapping(value = { "/adaptorType/{id}" })
  public ResponseEntity<?> Delete(@PathVariable("id") String id) {
    log.debug("ob_datamodel delete start...........");
    try {
      if (as.isExistAdapterType(id)) {
        as.deleteAdtTypeInfo(id);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @GetMapping({ "/adtItemSelect/{id}", "/adaptorType/{id}" })
  public ResponseEntity<List<Map>> adtItemSelect(@PathVariable("id") String id) {
    log.debug("----- AdapterController.itemSelect -----");
    List<Map> itemResult = new ArrayList();
    try {

      itemResult = as.itemSelect(id);
      log.debug("itemResult : {}", itemResult);
      if (itemResult == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(itemResult, HttpStatus.OK);
  }

  @GetMapping({ "/adtItemSelect2/{id}", "/adaptorType2/{id}" })
  public ResponseEntity<List<Map>> adtItemSelect2(@PathVariable("id") String id) {
    log.debug("----- AdapterController.itemSelect -----");
    List<Map> itemResult = new ArrayList();
    try {

      itemResult = as.itemSelect2(id);
      log.debug("itemResult : {}", itemResult);
      if (itemResult == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(itemResult, HttpStatus.OK);
  }

  @GetMapping({ "/adtItemSelect3/{id}", "/adaptorType3/{id}" })
  public ResponseEntity<List<Map>> adtItemSelect3(@PathVariable("id") String id) {
    log.debug("----- AdapterController.itemSelect -----");
    List<Map> itemResult = new ArrayList();
    try {

      itemResult = as.itemSelect3(id);
      log.debug("itemResult : {}", itemResult);
      if (itemResult == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(itemResult, HttpStatus.OK);
  }

  @PostMapping("/adtItemSave/{id}")
  public ResponseEntity<Map> adtItemSave(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("----- AdapterController.adtItemSave() -----");
    try {
      as.saveAdaptorItem(id, param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PostMapping("/adtItemSave2/{id}")
  public ResponseEntity<Map> adtItemSave2(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("----- AdapterController.adtItemSave2() -----");
    try {
      as.saveAdaptorItem2(id, param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PostMapping("/adtItemSave3/{id}")
  public ResponseEntity<Map> adtItemSave3(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("----- AdapterController.adtItemSave3() -----");
    try {
      as.saveAdaptorItem3(id, param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PostMapping({ "/popup/popupItemValue" })
  public ModelAndView popupItemValue(HttpServletRequest request) {

    log.debug("----- AdapterController.popupItemValue() -----");
    Map param = new HashMap();
    param.put("item_nm", request.getParameter("item_nm"));
    param.put("item_value", request.getParameter("item_value"));
    param.put("item_seq", request.getParameter("item_seq"));
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("popup/popupItemValue");
    modelAndView.addObject("result", param);

    return modelAndView;
  }

  @GetMapping(value = { "/api/connectivity/adaptorType" })
  public String sourceModels(HttpServletRequest request) {
    log.debug("----- ObController.sourceModels() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/adaptorType", "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping({ "/api/connectivity/adaptorType/{id}" })
  public String sourceModel(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("----- ObController.obDetail/Id/Data() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/adaptorType/" + id, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping({ "/api/connectivity/adaptorType" })
  public String create2(@RequestBody Map param, HttpServletRequest request) {
    log.debug("----- ObController.sourceModels -----");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/adaptorType/", "POST", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PutMapping(value = { "/api/connectivity/adaptorType/{id}" })
  public String update2(@PathVariable("id") String id, @RequestBody Map param, HttpServletRequest request) {
    log.debug("ob_datamodel update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/adaptorType/" + id, "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping(value = { "/api/connectivity/adaptorType/{id}" })
  public String Delete2(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("ob_datamodel delete start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/adaptorType/" + id, "DELETE", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  public String httpConnection(HttpServletRequest request, String targetUrl, String method, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    String result = "";
    JSONObject json = null;
    String jsonData = "";
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    HttpSession session = request.getSession();
    int responseCode;

    try {

      String stoken = (String) session.getAttribute("token");
      if (stoken != null) {
        if (stoken.length() > 0) {
          if ("{".equals(stoken.substring(0, 1))) {
            json = new JSONObject(stoken);
          }
        }
      }

      String token = (String) json.get("access_token");
      log.debug(jsonBody);
      String gatewayUrl = "";
      url = new URL(gatewayUrl + targetUrl);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod(method);
      conn.setRequestProperty("Authorization", "Bearer " + token);

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

}
