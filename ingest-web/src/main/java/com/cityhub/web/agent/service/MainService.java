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
package com.cityhub.web.agent.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.UrlUtil;
import com.cityhub.web.agent.mapper.MainMapper;
import com.cityhub.web.config.ConfigEnv;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@Service
public class MainService {
  @Autowired
  MainMapper mapper;
  @Autowired
  ConfigEnv configEnv;

  public List<Map> select_adtType() throws Exception {
    return mapper.select_adtType();
  }

  public Map selectAllAgentId(String id) throws Exception {
    return mapper.selectAllAgentId(id);
  }

  public boolean isExist(String id) throws Exception {
    if (mapper.isExist(id)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isExistAdapter(String id) throws Exception {
    if (mapper.isExistAdapter(id)) {
      return true;
    } else {
      return false;
    }
  }

  public List<Map> getAll() throws Exception {
    return mapper.selectAll();
  }

  public List<Map> platformType() throws Exception {
    return mapper.platformType();
  }

  @Transactional
  public Map insert(Map param) throws Exception {
    int bl = mapper.insert(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public Map update(String id, Map param) throws Exception {
    Map fetched = mapper.selectAllAgentId(id);
    if (fetched == null) {
      return null;
    }
    int bl = mapper.update(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public boolean delete(String id) throws Exception {
    Map fetched = mapper.selectAllAgentId(id);
    if (fetched == null) {
      return false;
    } else {
      mapper.delete(fetched);
      log.debug("result : {}", fetched);
      return true;
    }
  }

  public Map getByAdaptorId(String id) throws Exception {
    return mapper.selectByAdaptorId(id);
  }

  @Transactional
  public Map insertAdaptor(Map param) throws Exception {
    int bl = mapper.insertAdaptor(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public Map updateAdaptor(Map param) throws Exception {
    int bl = mapper.updateAdaptor(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public boolean deleteAdaptor(String id) throws Exception {
    Map fetched = mapper.selectByAdaptorId(id);
    if (fetched == null) {
      return false;
    } else {
      mapper.deleteAdaptor(fetched);
      log.debug("result : {}", fetched);
      return true;
    }
  }

  @Transactional
  public Map insertInstance(Map param) throws Exception {
    int bl = mapper.insertInstance(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public boolean deleteInstance(String id) throws Exception {
    Map fetched = mapper.selectByAdaptorId(id);
    if (fetched == null) {
      return false;
    } else {
      mapper.deleteInstance(fetched);
      log.debug("result : {}", fetched);
      return true;
    }
  }

  @Transactional
  public boolean deleteInstanceF(Map param) throws Exception {
    mapper.deleteInstanceF(param);
    log.debug("result : {}", param);
    return true;
  }

  @Transactional
  public boolean deleteInstanceEtc(String id) throws Exception {
    Map fetched = mapper.selectByAdaptorId(id);
    if (fetched == null) {
      return false;
    } else {
      mapper.deleteInstanceEtc(fetched);
      log.debug("result : {}", fetched);
      return true;
    }
  }

  @Transactional
  public Map insertInstanceEtc(Map param) throws Exception {
    int bl = mapper.insertInstanceEtc(param);
    log.debug("result : {}", param);
    return param;
  }

  public List<Map> selectAllAdaptorId(String id) throws Exception {
    return mapper.selectAllAdaptorId(id);
  }

  public List<Map> selectAllInstanceAdaptorId(String id) throws Exception {
    return mapper.selectAllInstanceAdaptorId(id);
  }

  public List<Map> selectAllAdaptor() throws Exception {
    return mapper.selectAllAdaptor();
  }

  public List<Map> selectAgentConf(Map param) throws Exception {
    return mapper.selectAgentConf(param);
  }

  public List<Map> selectInstanceIdAll(Map param) throws Exception {
    return mapper.selectInstanceIdAll(param);
  }

  public List<Map> selectInstanceDetail(Map param) throws Exception {
    return mapper.selectInstanceDetail(param);
  }

  public List<Map> selectKeywordInfo() throws Exception {
    return mapper.selectKeywordInfo();
  }

  public List<Map> selectAdaptorKeywordInfo(Map param) throws Exception {
    return mapper.selectAdaptorKeywordInfo(param);
  }

  public List<Map> selectObComboList() throws Exception {
    return mapper.selectObComboList();
  }

  public List<Map> selectDmTransform(Map param) throws Exception {
    return mapper.selectDmTransform(param);
  }

  public Map selectDmTransformKey() throws Exception {
    return mapper.selectDmTransformKey();
  }

  @Transactional
  public Map insertDmTransform(Map param) throws Exception {
    int bl = mapper.insertDmTransform(param);
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public Map updateDmTransform(Map param) throws Exception {
    int bl = mapper.updateDmTransform(param);
    log.debug("result : {}", param);
    return param;
  }

  public List<Map> selectAllInstance(String id) throws Exception {
    return mapper.selectAllInstance(id);
  }

  public List<Map> instanceBaseList(String id) throws Exception {
    return mapper.instanceBaseList(id);
  }

  public List<Map> instanceBaseList2(String id) throws Exception {
    return mapper.instanceBaseList2(id);
  }

  public List<Map> instanceBaseList3(String id) throws Exception {
    return mapper.instanceBaseList3(id);
  }

  public List<Map> getInstanceList(String id) throws Exception {
    return mapper.getInstanceList(id);
  }

  public Map getInstanceTopInfo(String id) throws Exception {
    Map result = mapper.getInstanceInfo(id);

    List<Map> obList = mapper.selectObComboList();

    if (result.get("ob_datamodel_id") != null) {
      for (Map rows : obList) {
        if (rows.get("ob_datamodel_id").equals(result.get("ob_datamodel_id"))) {
          result.put("ob_datamodel_nm", rows.get("ob_datamodel_nm"));
          break;
        }
      }
    } else {
      result.put("ob_datamodel_nm", "");
    }

    if (result.get("st_datamodel_id") != null) {
      HttpResponse resp = UrlUtil.get(configEnv.getDataModelApiUrl() + "?level=000", "Content-type", "application/json");
      JSONArray jsonarr = new JSONArray(resp.getPayload());
      for (int i = 0; i < jsonarr.length(); i++) {
        JSONObject jsonObject = jsonarr.getJSONObject(i);
        if (jsonObject.getString("id").equals(result.get("st_datamodel_id"))) {
          result.put("st_datamodel_nm", jsonObject.getString("name"));
          break;
        }
      }
    } else {
      result.put("st_datamodel_nm", "");
    }

    return result;
  }

  public Map getInstanceInfo(String id) throws Exception {
    return mapper.getInstanceInfo(id);
  }

  public List<Map> instanceItem(String id) throws Exception {
    return mapper.instanceItem(id);
  }

  public List<Map> instanceItem2(String id) throws Exception {
    return mapper.instanceItem2(id);
  }

  public List<Map> instanceItem3(String id) throws Exception {
    return mapper.instanceItem3(id);
  }

  @Transactional
  public Map instanceSave(Map param) throws Exception {

    List<?> list = new ArrayList<>();
    list = (List) param.get("insItemsData");
    log.debug("list = " + list);

    String insId = (String) param.get("instance_id");
    String adtId = (String) param.get("adapter_id");
    log.debug("param = " + param);

    if (!"".equals(param.get("obModel")) && !"".equals(param.get("stModel"))) {
      log.debug("데이터 모델 시작");
      log.debug("obModel = " + param.get("obModel"));
      log.debug("stModel = " + param.get("stModel"));
      Map tmpParam = new HashMap();
      tmpParam.put("ob_datamodel_id", param.get("obModel"));
      tmpParam.put("st_datamodel_id", param.get("stModel"));
      List<Map> keyExList = mapper.selectDmTransform(tmpParam);

      if (keyExList.size() > 0) {
        log.debug("keyExList.size() 0보다 큼");
        Map tmpKeyMap = keyExList.get(0);
        param.put("datamodel_tf_id", tmpKeyMap.get("datamodel_tf_id"));
        log.debug("클때는 " + tmpKeyMap.get("datamodel_tf_id"));
      } else {
        log.debug("keyExList.size() 0보다 작음");
        Map tmpKeyMap = mapper.selectDmTransformKey();
        log.debug("작을때는 " + tmpKeyMap.get("datamodel_tf_id"));
        param.put("datamodel_tf_id", tmpKeyMap.get("datamodel_tf_id"));
        Map tmpInsertMap = new HashMap();
        tmpInsertMap.put("datamodel_tf_id", tmpKeyMap.get("datamodel_tf_id"));
        tmpInsertMap.put("ob_datamodel_id", param.get("obModel"));
        tmpInsertMap.put("st_datamodel_id", param.get("stModel"));
        mapper.insertDmTransform(tmpInsertMap);
      }
      log.debug("데이터 모델 끝");
    } else {
      param.put("datamodel_tf_id", "");
    }

    if ("new_ins".equals(insId)) {
      insId = mapper.newInstanceId(adtId);
      param.put("instance_id", insId);
      mapper.insertInstance(param);
    } else {
      mapper.updateInstance(param);
    }

    mapper.deleteInstanceConf(insId);
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        obj.put("seq", i + 1);
        obj.put("instance_id", insId);
        log.debug("obj = " + obj);
        mapper.insertInstanceEtc(obj);
      }
    }

    log.debug("result : {}", param);
    return param;
  }

  public String getLastId() throws Exception {
    Map result = mapper.getLastId();
    if (result == null)
      return "";
    else
      return result.get("agent_id").toString();
  }

}
