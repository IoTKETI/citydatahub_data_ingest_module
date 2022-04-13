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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.cityhub.utils.DataCoreCode.ResponseCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.web.agent.service.ObService;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
@RestController
public class ObController {

  public static final String FIELD_HEX = (char) 0xD1 + "";

  @Autowired
  ObService os;

  @Value("${gateway.url}")
  public String gatewayUrl;

  @GetMapping({"/obList"})
  public ModelAndView obList(){
    log.debug("----- ObController.obList() -----");
    return new ModelAndView("obList");
  }


  @GetMapping(value = {"/obListView", "/sourceModels"})
  public ResponseEntity<List<Map>> obListView() {
    log.debug("----- ObController.obListView() -----");
    List<Map> result = new ArrayList<>();
    try {
      result = os.getAll();
      log.debug("result =" + result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping({"/obSearch"})
  public ResponseEntity<List<Map>>  obSearch(@RequestBody Map param){
    log.debug("----- ObController.obSearch() -----");
    List<Map> result = null;
    try {
      result = os.searchNm(param);
      log.debug("result : {}", result);
     } catch (Exception e) {
       log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }


  @GetMapping({"/obDetail"})
  public ModelAndView obDetailView(){
    log.debug("----- ObController.obDetail() -----");
    return new ModelAndView("obDetail");
   }

  @GetMapping({"/obDetail/{id}"})
  public ModelAndView obDetailViewEx(@PathVariable("id") String id) {
    log.debug("----- ObController.obDetailId() -----");
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("obDetail");
    return modelAndView;
  }

  @GetMapping({"/obDetail/{id}/data", "/sourceModels/{id}"})
  public ResponseEntity<Map> obDetailViewData(@PathVariable("id") String id) {
    log.debug("----- ObController.obDetail/Id/Data() -----");
    Map result = null;
    try {
      result = os.selectId(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }


  @GetMapping("/obDetail/{id}/item")
  public ResponseEntity<List<Map>> obDetailViewDataItem(@PathVariable("id") String id) {
    log.debug("----- ObController.obDetail/Id/Item() -----");
    List<Map> itemResult = null;
    try {
      itemResult = os.selectItem(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(itemResult, HttpStatus.CREATED);
  }

  @PostMapping({"/obDetail", "/sourceModels"})
  public ResponseEntity<Map> create(@RequestBody Map param) {
    try {
      if (os.isExist(param.get("ob_datamodel_id") + "")) {
        log.debug("ob_datamodel isExist start..........." );

        return new ResponseEntity<>(JsonUtil.toMapE("4100","ALREADY EXISTS", "이미 등록된 ID가 있습니다."), HttpStatus.CONFLICT);
      }
      log.debug("ob_datamodel insert start...........");
      os.insert(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PutMapping(value = {"/obDetail/{id}", "/sourceModel/{id}"})
  public ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("ob_datamodel update start...........");
    log.debug("param : {}, {}", id, param);
    try {
      if (os.isExist(id)) {
        os.update(id, param);
        return new ResponseEntity<>(JsonUtil.toMap(ResponseCode.CHANGED, param), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004","NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @DeleteMapping(value = {"/obDetail/{id}", "/sourceModel/{id}"})
  public ResponseEntity<?> Delete(@PathVariable("id") String id) {
    log.debug("ob_datamodel delete start...........");
    try {
      if(os.isExistIns(id)) {
        return new ResponseEntity<>(JsonUtil.toMapE("4009","Conflict", "해당 자원을 참조하는 인스턴스가 존재합니다."), HttpStatus.NOT_FOUND);
      }
      if (os.isExist(id)) {
        os.deleteObDatamodel(id);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004","NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }


  @PostMapping("/obDetailAuto")
  public ResponseEntity<List<Map>> obDetailAuto(@RequestBody Map param, UriComponentsBuilder ucBuilder) {

    JSONObject json = null;
    List<Map> pathList = new ArrayList<>();
    try {
      log.debug("ob_datamodel obDetailAuto start...........");

      String jsonUrl = param.get("jsonUrl").toString();
      if(jsonUrl != null) {
        if(jsonUrl.length()>0) {
          if("{".equals(jsonUrl.substring(0,1))){
            json = new JSONObject(jsonUrl);
          }else {
            HttpResponse resp = OkUrlUtil.get(param.get("jsonUrl").toString(), "Content-type", "application/json");
//            json = new JSONObject(resp.getPayload());

            String jxml = "";

            if(resp.getPayload().startsWith("<?xml")) {
              jxml = XML.toJSONObject(resp.getPayload()).toString();
              json = new JSONObject(jxml);
            }else {
              if (resp.getPayload().startsWith("[")) {
                JSONArray arr = new JSONArray(resp.getPayload());
                if(arr.length() > 0) {
                  json = arr.getJSONObject(0);
                }

              } else {
                json = new JSONObject(resp.getPayload());
              }

            }

            log.debug(json.toString());

          }
        }
      }

      for (String key : json.keySet()) {
        if (json.get(key) instanceof JSONObject) {
          recursiveFunc(pathList, key, "Object", key, json.getJSONObject(key));
        }
      }

      /*디버그 종료 후 주석처리 해야 함 - 시작*/
      for (Map key : pathList) {
          log.debug(key.get("property_path")+"."+key.get("property"));
          log.debug("" + key.get("property_structure"));
      }
      /*디버그 종료 후 주석처리 해야 함 - 끝*/
      //ob_datamodel_id, ob_datamodel_seq, property_nm, property, property_path, property_structure, type, option, described

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(pathList, HttpStatus.OK);
  }

  public void recursiveFunc(List pathList, String path, String nodeType, String pk, JSONObject jo) {
    for (String k : jo.keySet()) {
      if (jo.get(k) instanceof JSONObject) {
        //log.debug(path + "." + k);
        recursiveFunc(pathList, path + "." + k, nodeType + ".Object", k, jo.getJSONObject(k));
      } else if (jo.get(k) instanceof JSONArray) {
        int i=0;
        for (Object o : jo.getJSONArray(k)) {
          if(i==0) {
            //log.debug(path + "." + k);
            recursiveFunc(pathList, path + "." + k, nodeType + ".Array", k, (JSONObject) o);
          }
          i++;
        }
      } else {
        Map pathMap = new HashMap();
        pathMap.put("property_path", path);
        pathMap.put("property", k);
        pathMap.put("property_structure", nodeType);
        pathList.add(pathMap);
      }
    }
  }


  @GetMapping(value = {"/sourceModelsTest"})
  public void obListViewTest(HttpServletRequest request) {
    HttpSession session = request.getSession();
    try {


      httpConnection(request, "/api/connectivity/sourceModels", "GET", "");


    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


  @GetMapping(value = {"/api/connectivity/sourceModels"})
  public String sourceModels(HttpServletRequest request) {
    log.debug("----- ObController.sourceModels() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/sourceModels", "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }


  @GetMapping({"/api/connectivity/sourceModels/{id}"})
  public String sourceModel(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("----- ObController.obDetail/Id/Data() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/sourceModels/"+id, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping({"/api/connectivity/sourceModels"})
  public String create2(@RequestBody Map param, HttpServletRequest request) {
      log.debug("----- ObController.sourceModels -----");
      String temp = "";
      try {
        JSONObject jo = new JSONObject(param);
        temp = httpConnection(request, "/api/connectivity/sourceModels/", "POST", jo.toString());

      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
      return temp;
  }


  @PutMapping(value = {"/api/connectivity/sourceModel/{id}"})
  public String update2(@PathVariable("id") String id, @RequestBody Map param, HttpServletRequest request) {
    log.debug("ob_datamodel update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/sourceModel/"+id, "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping(value = {"/api/connectivity/sourceModel/{id}"})
  public String Delete2(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("ob_datamodel delete start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/sourceModel/"+id, "DELETE", "");

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
      if(stoken != null) {
        if(stoken.length()>0) {
          if("{".equals(stoken.substring(0,1))){
            json = new JSONObject(stoken);
          }
        }
      }

      String token = (String) json.get("access_token");
      log.debug(jsonBody);

        url = new URL(gatewayUrl + targetUrl);
//        url = new URL("http://13.124.164.104:8080"+targetUrl);

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer "+token);

        if(!"".equals(jsonBody)) {
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
