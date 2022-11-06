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
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
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
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.UrlUtil;
import com.cityhub.web.agent.service.AdapterService;
import com.cityhub.web.agent.service.MainService;
import com.cityhub.web.config.ConfigEnv;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
public class AgentController {

  public static final String FIELD_HEX = (char) 0xD1 + "";

  @Autowired
  MainService svc;

  @Autowired
  ConfigEnv configEnv;

  @Autowired
  AdapterService as;

  @GetMapping({ "/agentList" })
  public ModelAndView agentListView() {
    log.debug("----- AgentController.agentListView() -----");
    return new ModelAndView("agentList");
  }

  @GetMapping({ "/agentDataList", "/agents" })
  public ResponseEntity<List<Map>> agentDataListView() {
    log.debug("----- AgentController.agentDataListView() -----");
    List<Map> result = new ArrayList<>();
    try {
      result = svc.getAll();
      log.debug("result =" + result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping({ "/agentDetail" })
  public ModelAndView agentDetailView() {
    log.debug("----- AgentController.agentDetail() -----");
    return new ModelAndView("agentDetail");
  }

  @GetMapping({ "/agentDetailNew" })
  public ModelAndView agentDetailnewView() {
    log.debug("----- AgentController.agentDetail() -----");
    return new ModelAndView("agentDetailNew");
  }

  @GetMapping({ "/agentDetail/{id}" })
  public ModelAndView agentDetailIdView(@PathVariable("id") String id) {
    log.debug("----- AgentController.agentDetailIdView() -----");
    ModelAndView modelAndView = new ModelAndView();
    try {
      List<Map> platform_type = svc.platformType();
      modelAndView.addObject("platform_type", platform_type);
      modelAndView.setViewName("agentDetail");
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return modelAndView;
  }

  @GetMapping({ "/agentDetail/{id}/agent", "/agents/{id}" })
  public ResponseEntity<Map> agentDetailAgent(@PathVariable("id") String id) {
    log.debug("----- AgentController.agentDetail/Id/agent() -----");
    Map result = null;
    try {
      result = svc.selectAllAgentId(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @DeleteMapping(value = { "/agents/{id}" })
  public ResponseEntity<?> Delete(@PathVariable("id") String id) {
    log.debug("agentDetail delete start...........");
    try {
      if (svc.isExist(id)) {
        svc.delete(id);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @PutMapping(value = { "/agents/{id}" })
  public ResponseEntity<?> update2(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("agentDetail update start...........");
    try {
      if (svc.isExist(id)) {
        param.put("agent_id", id);
        svc.update(id, param);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.CHANGED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @GetMapping(value = { "/agentsIsExist/{id}" })
  public boolean update2(@PathVariable("id") String id) throws Exception {
    return svc.isExist(id);
  }

  @PostMapping(value = { "/agents2" }) // -------------------agentid 획일화----------------------
  public ResponseEntity<?> update2(@RequestBody Map param) {
    log.debug("agentDetail update start...........");
    try {
      String t = svc.getLastId();
      if (t.isEmpty()) {
        param.put("agent_id", "Agent_001");
        svc.insert(param);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.CHANGED), HttpStatus.OK);
      } else {
        int num = Integer.parseInt(t.substring(6)) + 1;
        String id_ = "Agent_";
        if (num < 10) {
          id_ += "00" + Integer.toString(num);
        } else if (num >= 10 && num < 100) {
          id_ += "0" + Integer.toString(num);
        } else {
          id_ += Integer.toString(num);
        }

        param.put("agent_id", id_);
        svc.insert(param);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.CHANGED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @PostMapping({ "/agentDetail", "/agents" })
  public ResponseEntity<Map> create2(@RequestBody Map param) {
    log.debug("----- AgentController.agentDetailSave() -----");
    try {
      if (svc.isExist(param.get("agent_id") + "")) {
        return new ResponseEntity<>(JsonUtil.toMapE("4100", "ALREADY EXISTS", "이미 등록된 ID가 있습니다."), HttpStatus.CONFLICT);
      }
      String t = param.get("agent_id").toString();
      if (t.length() > 10) {
        return new ResponseEntity<>(JsonUtil.toMapE("4100", "ALREADY EXISTS", "에이전트 아이디의 길이가 맞지않습니다."), HttpStatus.CONFLICT);
      } else if (t.length() == 0) {
        return new ResponseEntity<>(JsonUtil.toMapE("4100", "ALREADY EXISTS", "공백은 id로 사용할 수 없습니다."), HttpStatus.CONFLICT);
      }
      svc.insert(param);
      return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

  }

  @PutMapping(value = { "/agentDetail/{id}" })
  public ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("----- AgentController.agentDetailUpdate() -----");
    log.debug("PARAM = " + param);
    log.debug("param : {}, {}", id, param);
    try {
      if (svc.isExist(id)) {
        param.put("agent_id", id);
        svc.update(id, param);
        return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.OK, param), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping({ "/agentDetail/{agentId}/adaptor", "/agents/{agentId}/adaptors" })
  public ResponseEntity<List<Map>> agentDetailAdaptor(@PathVariable("agentId") String agentId) {
    log.debug("----- AgentController.agentDetail/Id/adaptor() -----");
    List<Map> itemResult = null;
    try {
      itemResult = svc.selectAllAdaptorId(agentId);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(itemResult, HttpStatus.OK);
  }

  @PostMapping({ "/adapterSave", "/agents/{agentId}/adaptors" })
  public ResponseEntity<Map> create(@RequestBody Map param) {
    try {
      log.debug(param.toString());
      if (svc.isExistAdapter(param.get("adapter_id") + "")) {
        log.debug("adaptor isExist start...........");
        return new ResponseEntity<>(JsonUtil.toMapE("4100", "ALREADY EXISTS", "이미 등록된 ID가 있습니다."), HttpStatus.CONFLICT);
      }
      log.debug("adaptor insert start...........");
      svc.insertAdaptor(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PutMapping({ "/adapterModify", "/agents/{agentId}/adaptors/{adaptorId}" })
  public ResponseEntity<Map> adapterModify(@RequestBody Map param) {
    log.debug("----- AgentController.adapterModify() -----");
    try {
      log.debug("param = " + param);
      svc.updateAdaptor(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.OK, param), HttpStatus.OK);
  }

  @GetMapping("/instanceDetail/{id}")
  public ModelAndView instanceDetail(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceDetail() -----");
    ModelAndView modelAndView = new ModelAndView();
    try {
      List<Map> adapter_type = svc.select_adtType();
      modelAndView.addObject("adapter_type", adapter_type);
      modelAndView.addObject("id", id);
      modelAndView.setViewName("instanceDetail");
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return modelAndView;
  }

  @GetMapping({ "/instanceControl/{adaptorId}/insAdpInfo/", "/agents/{adaptorId}/adaptors/{adaptorId}" })
  public ResponseEntity<Map> insAdpInfo(@PathVariable("adaptorId") String adaptorId) {
    log.debug("----- AgentController.instanceControl/Id/insAdpInfo() -----");
    Map result = null;
    try {
      if (svc.isExistAdapter(adaptorId)) {
        result = svc.getByAdaptorId(adaptorId);
        return new ResponseEntity<>(result, HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @GetMapping({ "/instanceControl/{id}/insList/", "/agents/{agentId}/adaptors/{adaptorId}/instance" })
  public ResponseEntity<List<Map>> insDetail(@PathVariable("adaptorId") String adaptorId) {
    log.debug("----- AgentController.instanceControl/Id/insList() -----");
    List<Map> instance_list = null;
    try {
      instance_list = svc.getInstanceList(adaptorId);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(instance_list, HttpStatus.OK);
  }

  @GetMapping({ "/instanceControl/{instanceId}/insDetail/", "/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}" })
  public ResponseEntity<Map> instanceSelect(@PathVariable("instanceId") String instanceId) {
    log.debug("----- AgentController.instanceControl/Id/insDetail() -----");
    Map instance_detail = null;
    try {
      instance_detail = svc.getInstanceInfo(instanceId);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(instance_detail, HttpStatus.OK);
  }

  @GetMapping("/instanceControl/{id}/insItem/")
  public ResponseEntity<List<Map>> instanceItem(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceControl/Id/insItem() -----");
    List<Map> result = null;
    try {
      result = svc.instanceItem(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  ///////////////////////////////////////////////////////////////////////////////////
  @GetMapping("/instanceControl/{id}/insItem2/")
  public ResponseEntity<List<Map>> instanceItem2(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceControl/Id/insItem() -----");
    List<Map> result = null;
    try {
      result = svc.instanceItem2(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/instanceControl/{id}/insItem3/")
  public ResponseEntity<List<Map>> instanceItem3(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceControl/Id/insItem() -----");
    List<Map> result = null;
    try {
      result = svc.instanceItem3(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
///////////////////////////////////////////////////////////////////////////////////

  @PostMapping({ "/instanceSave", "/agents/{agentId}/adaptors/{adaptorId}/instance" })
  public ResponseEntity<Map> instanceSave(@PathVariable("adaptorId") String adaptorId, @RequestBody Map param) {
    log.debug("----- AgentController.instanceSave() -----");
    try {
      svc.instanceSave(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PutMapping({ "/instanceSave", "/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}" })
  public ResponseEntity<Map> instanceSave2(@PathVariable("instanceId") String instanceId, @RequestBody Map param) {
    log.debug("----- AgentController.instanceSave() -----");
    try {
      svc.instanceSave(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @GetMapping("/instanceListAll/{id}")
  public ResponseEntity<List<Map>> instanceListAll(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceDetailViewList() -----");
    List<Map> result = null;
    try {
      result = svc.selectAllInstance(id);
      log.debug("result : {}", result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping("/instanceCompile")
  public ResponseEntity<String> instanceCompile(@RequestBody String jsonData) {
    log.debug("----- AgentController.instanceCompile() -----");
    HttpResponse resp = null;
    try {
      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      log.debug(configEnv.getCompileUrl());
      resp = UrlUtil.post(configEnv.getCompileUrl(), headers, jsonData);
      log.debug("resp = " + resp);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @PutMapping("/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public ResponseEntity<String> instanceCompile2(@RequestBody String jsonData) {
    log.debug("----- AgentController.instanceCompile() -----");
    HttpResponse resp = null;
    try {
      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      log.debug(configEnv.getCompileUrl());
      resp = UrlUtil.post(configEnv.getCompileUrl(), headers, jsonData);
      log.debug("resp = " + resp);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @PostMapping("/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public ResponseEntity<String> instanceCompile3(@RequestBody String jsonData) {
    log.debug("----- AgentController.instanceCompile3() -----");
    HttpResponse resp = null;
    try {
      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      log.debug(configEnv.getCompileUrl());
      resp = UrlUtil.post(configEnv.getCompileUrl(), headers, jsonData);
      log.debug("resp = " + resp);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @GetMapping("/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public ResponseEntity<String> instanceCompileGet(@PathVariable("instanceId") String instanceId) {
    log.debug("----- AgentController.instanceCompileGet() -----");
    HttpResponse resp = null;

    try {
      JSONObject body = new JSONObject();
      body.put("instance_id", instanceId);

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      resp = UrlUtil.post(configEnv.getDaemonSrv() + "/exec/read", headers, body.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @DeleteMapping("/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public ResponseEntity<Map> instanceCompileDel(@PathVariable("instanceId") String instanceId) {
    log.debug("----- AgentController.instanceCompileDel() -----");
    HttpResponse resp = null;
    try {

      JSONObject body = new JSONObject();
      body.put("instance_id", instanceId);
      body.put("sourceCode", "");

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      log.debug(configEnv.getCompileUrl());
      resp = UrlUtil.post(configEnv.getCompileUrl(), headers, body.toString());
      log.debug("resp = " + resp);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
  }

  @DeleteMapping(value = "/agentDetail/{id}")
  public ResponseEntity<Map> delete(@PathVariable("id") String id) {
    log.debug("----- AgentController.agentDetailDelete() -----");
    try {
      if (svc.isExist(id)) {
        if (!svc.delete(id)) {
          return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
        } else {
          return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
        }
      } else {
        return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping(value = { "/agents/{agentId}/adaptors/{adaptorId}" })
  public ResponseEntity<?> Delete(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId) {
    log.debug("adaptorDetail delete start...........");
    try {
      if (svc.isExistAdapter(adaptorId)) {
        svc.deleteAdaptor(adaptorId);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @DeleteMapping(value = { "/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}" })
  public ResponseEntity<?> Delete2(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId) {
    log.debug("instanceDetail delete start...........");

    List<Map> result = null;
    try {
      Map param = new HashMap();
      param.put("agent_Id", agentId);
      param.put("adapter_id", adaptorId);
      param.put("instance_id", instanceId);
      svc.deleteInstanceF(param);
      result = svc.selectInstanceDetail(param);
      if (result != null) {
        if (result.size() > 0) {
          svc.deleteInstanceF(param);
          return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
        } else {
          Map param2 = new HashMap();
          param2.put("instance_id", instanceId);
          svc.deleteInstanceF(param2);
          return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
        }
      } else {
        Map param2 = new HashMap();
        param2.put("instance_id", instanceId);
        svc.deleteInstanceF(param2);
        return new ResponseEntity<>(JsonUtil.toMapC(ResponseCode.DELETED), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMapE("4004", "NOT FOUND", "리소스 식별 실패"), HttpStatus.NOT_FOUND);
  }

  @GetMapping({ "/pushConf/{agent_id}/{adapter_id}", "/restApi/pushConf/{agent_id}/{adapter_id}" })
  public ResponseEntity<String> pushConf(@PathVariable("agent_id") String agent_id, @PathVariable("adapter_id") String adapter_id) {

    HttpResponse resp = null;
    try {

      HashMap<String, JSONObject> agentHashMap = new HashMap<>();

      Map param = new HashMap();
      param.put("agent_id", agent_id);
      param.put("adapter_id", adapter_id);

      List<Map> result = svc.selectAgentConf(param);

      JSONObject body = new JSONObject();

      for (int i = 0; i < result.size(); i++) {
        Map curMap = result.get(i);

        if (i == 0) {
          JSONObject agentConf = new JSONObject();
          agentConf.put("id", adapter_id);
          agentConf.put("type", "agent");
          agentHashMap.put(adapter_id, agentConf);

          JSONObject tmp = new JSONObject();

          JSONObject bodyAgentTop = new JSONObject();
          JSONObject bodyAgentChannel = new JSONObject();
          JSONObject bodyAgentSink = new JSONObject();

          tmp.put("id", adapter_id);
          tmp.put("type", "agent");

          List<Map> insResult = svc.selectInstanceIdAll(param);

          String instanceStr = "";
          for (int j = 0; j < insResult.size(); j++) {
            Map curMap2 = insResult.get(j);
            if (j == 0) {
              instanceStr += (String) curMap2.get("instance_id");
            } else {
              instanceStr += " " + (String) curMap2.get("instance_id");
            }
          }
          bodyAgentTop.put("sources", instanceStr);

          bodyAgentTop.put("channels", "logCh");
          bodyAgentTop.put("sinks", "logSink");

          bodyAgentChannel.put("type", "memory");
          bodyAgentChannel.put("capacity", 10000);
          bodyAgentChannel.put("transactionCapacity", 1000);

          bodyAgentSink.put("channel", "logCh");
          bodyAgentSink.put("type", "com.cityhub.flow.CallRestApiSink");
          bodyAgentSink.put("INGEST_INTERFACE_API_URL", configEnv.getInterfaceApiUrl());
          bodyAgentSink.put("INGEST_INTERFACE_YN", configEnv.getInterfaceApiUrlUseYn().toUpperCase());
          bodyAgentSink.put("DAEMON_SERVER_LOGAPI", configEnv.getDaemonSrv() + "/logToDbApi");

          body.put(adapter_id, bodyAgentTop);
          body.put("logCh", bodyAgentChannel);
          body.put("logSink", bodyAgentSink);

          tmp.put("body", body);

          agentHashMap.put(adapter_id, tmp);
        }
        param.put("instance_id", curMap.get("instance_id"));

        List<Map> insDetail = svc.selectInstanceDetail(param);

        JSONObject bodyInstance = new JSONObject();

        bodyInstance.put("channels", "logCh");

        bodyInstance.put("CONF_FILE", "openapi/" + curMap.get("instance_id") + ".conf");
        bodyInstance.put("DATAMODEL_API_URL", configEnv.getDataModelApiUrl());
        bodyInstance.put("DAEMON_SERVER_LOGAPI", configEnv.getDaemonSrv() + "/logToDbApi");

        for (Map<String, String> vMp : insDetail) {
          if ("b".equalsIgnoreCase(vMp.get("sector").toString())) {
            bodyInstance.put(vMp.get("item").toString(), vMp.get("value").toString());
          }
        }

        if ("Y".equalsIgnoreCase(curMap.get("datamodel_conv_div").toString())) {
          bodyInstance.put("type", curMap.get("type"));
          bodyInstance.put("INVOKE_CLASS", "com.cityhub.adapter.convex." + curMap.get("instance_id"));
        } else {
          String type = curMap.get("type").toString().replace("OpenApiSystem","OpenApiSource");
          bodyInstance.put("type", type );
        }



        body.put((String) curMap.get("instance_id"), bodyInstance);

        JSONObject adtConf = new JSONObject();
        JSONObject insServiceList = new JSONObject();

        JSONArray jsonarr = new JSONArray();
        JSONObject insDetailObj = new JSONObject();

        for (int j = 0; j < insDetail.size(); j++) {
          Map curMap3 = insDetail.get(j);
          if ( "a".equalsIgnoreCase(curMap3.get("sector").toString())) {
            if ("location".equals(curMap3.get("item"))) {
              String locString = (String) curMap3.get("value");
              String[] words = locString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
              JSONArray locArr = new JSONArray();
              for (String wo : words) {
                locArr.put(Float.parseFloat(wo));
              }
              insDetailObj.put((String) curMap3.get("item"), locArr);

            }
            ////////////////////////////////////////////////////
            else if ("headers".equals(curMap3.get("item"))) {
              String locString = (String) curMap3.get("value");
              String[] words = locString.replaceAll("\"\\[", "\\[").replaceAll("\\]\"", "\\]").split(",");
              JSONArray locArr = new JSONArray();
              for (String wo : words) {
                JSONObject jsonObject = new JSONObject(wo);
                locArr.put(jsonObject);
              }
              insDetailObj.put((String) curMap3.get("item"), locArr);
            } else if ("address".equals(curMap3.get("item"))) {
              String locString = (String) curMap3.get("value");
              String[] words = locString.replaceAll("\"\\[", "\\[").replaceAll("\\]\"", "\\]").split("\\},");
              JSONArray locArr = new JSONArray();
              for (String wo : words) {
                wo += '}';
                JSONObject jsonObject = new JSONObject(wo);
                locArr.put(jsonObject);
              }
              insDetailObj.put((String) curMap3.get("item"), locArr);
            } else if ("databaseInfo".equals(curMap3.get("item"))) {
              String locString = (String) curMap3.get("value");
              String[] words = locString.replaceAll("\"\\[", "\\[").replaceAll("\\]\"", "\\]").split("\\},");
              JSONArray locArr = new JSONArray();
              for (String wo : words) {
                wo += '}';
                JSONObject jsonObject = new JSONObject(wo);
                locArr.put(jsonObject);
              }
              insDetailObj.put((String) curMap3.get("item"), locArr.toString().replaceAll("[", "\"").replaceAll("]", "\""));
            }
            //////////////////////////////////////////////////
            else {
              if ("MODEL_ID".equals(curMap3.get("item")) || "INVOKE_CLASS".equals(curMap3.get("item")) || "DATASET_ID".equals(curMap3.get("item")) || "CONN_TERM".equals(curMap3.get("item"))) {
                bodyInstance.put((String) curMap3.get("item"), curMap3.get("value"));
              } else {
                insDetailObj.put((String) curMap3.get("item"), curMap3.get("value"));
              }
            }

          }

        }
        jsonarr.put(insDetailObj);
        insServiceList.put("serviceList", jsonarr);

        adtConf.put("id", curMap.get("instance_id"));
        adtConf.put("type", "adapter");
        adtConf.put("body", insServiceList);

        Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
        log.info("###{}", adtConf.toString());
        resp = UrlUtil.post(configEnv.getConfigUrl() + "/adapter", headers, adtConf.toString()); /// plugins.d/agent/lib/openapi/

      }

      Header[] headers = new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
      log.info("###{}", agentHashMap.get(adapter_id).toString());
      resp = UrlUtil.post(configEnv.getConfigUrl() + "/agent", headers, agentHashMap.get(adapter_id).toString()); /// conf/

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<>(resp.getPayload(), HttpStatus.OK);
  }

  @GetMapping("/instanceBaseList/{id}")
  public ResponseEntity<List<Map>> instanceBaseList(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceBaseList() -----");
    List<Map> result = null;
    try {
      result = svc.instanceBaseList(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////
  @GetMapping("/instanceBaseList2/{id}")
  public ResponseEntity<List<Map>> instanceBaseList2(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceBaseList() -----");
    List<Map> result = null;
    try {
      result = svc.instanceBaseList2(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/instanceBaseList3/{id}")
  public ResponseEntity<List<Map>> instanceBaseList3(@PathVariable("id") String id) {
    log.debug("----- AgentController.instanceBaseList() -----");
    List<Map> result = null;
    try {
      result = svc.instanceBaseList3(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
///////////////////////////////////////////////////////////////////////////////////////////

  @GetMapping("/conv/{step}/{id}/{insId}")
  public ModelAndView convertStep(@PathVariable("step") String step, @PathVariable("id") String id, @PathVariable("insId") String insId) {
    log.debug("----- AgentController.convertStep/" + step + "/" + id + "/" + insId + " -----");
    return new ModelAndView(step);
  }

  @GetMapping("/conv/{insId}/dataModel_Topinfo")
  public ResponseEntity<Map> convertTopinfoView(@PathVariable("insId") String insId) {
    log.debug("----- AgentController.convertTopinfoView() -----");
    Map result = null;
    try {
      result = svc.getInstanceTopInfo(insId);
      log.debug("result =" + result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/conv/{datamodel_tf_id}/{ob_datamodel_id}/{st_datamodel_id}/convInfo")
  public ResponseEntity<Map> convInfoUpdate(@PathVariable("datamodel_tf_id") String datamodel_tf_id, @PathVariable("ob_datamodel_id") String ob_datamodel_id,
      @PathVariable("st_datamodel_id") String st_datamodel_id) {
    log.debug("----- AgentController.convInfoUpdate() -----");
    Map result = null;
    try {

      Map<String, Object> param = new HashMap<>();
      param.put("datamodel_tf_id", datamodel_tf_id);
      param.put("ob_datamodel_id", ob_datamodel_id);
      param.put("st_datamodel_id", st_datamodel_id);

      result = svc.updateDmTransform(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping("/searchStModel")
  public ResponseEntity<List<Map>> searchStModel(@RequestBody Map param) {
    log.debug("----- AgentController.searchStModel(" + param + ") -----");

    List<Map> stList = null;
    try {

      stList = new ArrayList<>();
      log.debug(configEnv.getDataModelApiUrl() + "?level=000&name=" + param.get("search_datamodel_nm"));
      HttpResponse resp = UrlUtil.get(configEnv.getDataModelApiUrl() + "?level=000&name=" + param.get("search_datamodel_nm"), "Content-type", "application/json");
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

    return new ResponseEntity<>(stList, HttpStatus.OK);
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

  @GetMapping(value = { "/api/connectivity/agents" })
  public String agents(HttpServletRequest request) {
    log.debug("----- AgentController.agents() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents", "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping({ "/api/connectivity/agents/{id}" })
  public String sourceModel(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("----- AgentController.obDetail/Id/Data() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + id, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping({ "/api/connectivity/agents" })
  public String create2(@RequestBody Map param, HttpServletRequest request) {
    log.debug("----- AgentController.agents -----");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/", "POST", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PutMapping(value = { "/api/connectivity/agents/{id}" })
  public String update2(@PathVariable("id") String id, @RequestBody Map param, HttpServletRequest request) {
    log.debug("agents update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + id, "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping(value = { "/api/connectivity/agents/{id}" })
  public String Delete2(@PathVariable("id") String id, HttpServletRequest request) {
    log.debug("agents delete start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + id, "DELETE", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping(value = { "/api/connectivity/agents/{agentId}/adaptors" })
  public String adaptors(@PathVariable("agentId") String agentId, HttpServletRequest request) {
    log.debug("----- AgentController.agents() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents" + agentId, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping(value = { "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}" })
  public String adaptors(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, HttpServletRequest request) {
    log.debug("----- AgentController.obDetail/Id/Data() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping(value = { "/api/connectivity/agents/{agentId}/adaptors" })
  public String adaptors3(@PathVariable("agentId") String agentId, @RequestBody Map param, HttpServletRequest request) {
    log.debug("----- AgentController.agents -----");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors", "POST", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PutMapping(value = { "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}" })
  public String adaptors(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @RequestBody Map param, HttpServletRequest request) {
    log.debug("ob_datamodel update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId, "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping(value = { "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}" })
  public String adaptors2(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, HttpServletRequest request) {
    log.debug("ob_datamodel delete start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId, "DELETE", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping(value = { "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance" })
  public String instance(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, HttpServletRequest request) {
    log.debug("----- AgentController.agents() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance", "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping({ "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}" })
  public String instance(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
    log.debug("----- AgentController.obDetail/Id/Data() -----");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId, "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PostMapping({ "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance" })
  public String instance3(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @RequestBody Map param, HttpServletRequest request) {
    log.debug("----- AgentController.agents -----");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance", "POST", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PutMapping("/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}")
  public String instance(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, @RequestBody Map param,
      HttpServletRequest request) {
    log.debug("ob_datamodel update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId, "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping(value = { "/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}" })
  public String adaptors2(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
    log.debug("ob_datamodel delete start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId, "DELETE", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @PutMapping("/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public String instanceCompile(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, @RequestBody Map param,
      HttpServletRequest request) {
    log.debug("instanceCompile update start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId + "/modelConversion", "PUT", jo.toString());

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @GetMapping("/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public String instanceCompileGet(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
    log.debug("instanceCompile get start...........");
    String temp = "";
    try {
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId + "/modelConversion", "GET", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  @DeleteMapping("/api/connectivity/agents/{agentId}/adaptors/{adaptorId}/instance/{instanceId}/modelConversion")
  public String instanceCompileDel(@PathVariable("agentId") String agentId, @PathVariable("adaptorId") String adaptorId, @PathVariable("instanceId") String instanceId, @RequestBody Map param,
      HttpServletRequest request) {
    log.debug("instanceCompile delete start...........");
    String temp = "";
    try {
      JSONObject jo = new JSONObject(param);
      temp = httpConnection(request, "/api/connectivity/agents/" + agentId + "/adaptors/" + adaptorId + "/instance/" + instanceId + "/modelConversion", "DELETE", "");

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return temp;
  }

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    ValueMap.put("observedAt", DateUtil.getTime());
    return ValueMap;
  }

  String ExponentialStage(Integer Exponential, Object[][] arrList) {

    Integer Min = 0;

    String resultName = "";

    for (Integer i = 0; i < arrList.length; i++) {

      Integer _arrayNumber = (Integer) arrList[i][0];
      String _arrayName = (String) arrList[i][1];

      if ((Exponential >= _arrayNumber) && (_arrayNumber >= Min)) {
        Min = _arrayNumber;
        resultName = _arrayName;
      }
    }

    return resultName;
  }

  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }

  public String getStr(SocketCode sc, String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }

}
