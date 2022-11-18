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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cityhub.web.agent.mapper.AdapterMapper;
import com.cityhub.web.config.ConfigEnv;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@Service
public class AdapterService {
  @Autowired
  AdapterMapper mapper;

  @Autowired
  ConfigEnv configEnv;

  public List<Map> getAll() throws Exception {
    return mapper.getAll();
  }

  public List<Map> value_type_search() throws Exception {
    return mapper.value_type_search();
  }

  @Transactional
  public Map insertAdaptorType(Map param) throws Exception {
    log.debug("param = " + param);
    List<?> list = new ArrayList<>();
    list = (List) param.get("adt_type_data");

    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        log.debug(obj.toString());
        String adapter_type_id = (String) obj.get("adapter_type_id");
        if ("newAdd".equals(adapter_type_id)) {
          String new_adapter_type_id = mapper.new_code_id();
          String adapter_type_nm = (String) obj.get("adapter_type_nm");
          obj.put("adapter_type_id", new_adapter_type_id);
          obj.put("adapter_type_nm", adapter_type_nm);
          mapper.insertAdaptorType(obj);
        } else {
          mapper.updateAdaptorType(obj);
        }
      }
    }
    return param;
  }

  @Transactional
  public List<Map> itemSelect(String id) throws Exception {
    return mapper.itemSelect(id);
  }

  @Transactional
  public List<Map> itemSelect2(String id) throws Exception {
    return mapper.itemSelect2(id);
  }

  @Transactional
  public List<Map> itemSelect3(String id) throws Exception {
    return mapper.itemSelect3(id);
  }

  @Transactional
  public Map saveAdaptorItem(String id, Map param) throws Exception {

    mapper.deleteAdtItem(id);
    List<?> list = new ArrayList<>();
    list = (List) param.get("itemsData");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        obj.put("adapter_type_id", id);
        log.debug("obj = " + obj);
        mapper.adtItemInsert(obj);
      }
    }
    return param;
  }

  @Transactional
  public Map saveAdaptorItem2(String id, Map param) throws Exception {

    mapper.deleteAdtItem2(id);
    List<?> list = new ArrayList<>();
    list = (List) param.get("itemsData");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        obj.put("adapter_type_id", id);
        log.debug("obj = " + obj);
        mapper.adtItemInsert(obj);
      }
    }
    return param;
  }

  @Transactional
  public Map saveAdaptorItem3(String id, Map param) throws Exception {

    mapper.deleteAdtItem3(id);
    List<?> list = new ArrayList<>();
    list = (List) param.get("itemsData");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Map obj = (Map) list.get(i);
        obj.put("adapter_type_id", id);
        log.debug("obj = " + obj);
        mapper.adtItemInsert(obj);
      }
    }
    return param;
  }

  public List<Map> type_search() throws Exception {
    return mapper.type_search();
  }

  @Transactional
  public void restApiInsertAdaptorType(Map param) throws Exception {
    mapper.insertAdaptorType(param);
  }

  @Transactional
  public void restApiUpdateAdaptorType(Map param) throws Exception {
    mapper.updateAdaptorType(param);
  }

  public boolean isExistAdapterType(String id) {
    return mapper.isExistAdapterType(id);
  }

  @Transactional
  public void deleteAdtTypeInfo(String id) throws Exception {
    mapper.deleteAdtTypeInfo(id);
  }

}
