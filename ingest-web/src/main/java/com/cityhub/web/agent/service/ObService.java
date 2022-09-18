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
import com.cityhub.web.agent.mapper.ObMapper;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
@Service
public class ObService {
  @Autowired
  ObMapper mapper;

  public List<Map> getAll() throws Exception {
    return mapper.getAll();
  }

  public List<Map> selectItem(String id) throws Exception {
    return mapper.selectItem(id);
  }

  public Map selectId(String id) throws Exception {
    return mapper.selectId(id);
  }

  public List<Map> searchNm(Map param) throws Exception {
    return mapper.searchNm(param);
  }



  public boolean isExist(String id) throws Exception {
    if (mapper.isExist(id) ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isExistIns(String id) throws Exception {
    if (mapper.isExistIns(id) ) {
      return true;
    } else {
      return false;
    }
  }



  @Transactional
  public Map insert(Map param) throws Exception {

    List<?> list = new ArrayList<Object>();
    list = (List)param.get("itemsData");

    String model_id = (String) param.get("ob_datamodel_id");
    param.remove("item_size");
    param.remove("itemsData");

    mapper.insert(param);
    log.debug("item data = " + list);
    log.debug("item insert start..............");
    mapper.deleteItem(model_id);
    if(list != null && list.size() > 0) {
      for(int i=0; i<list.size(); i++){

        Map obj = (Map)list.get(i);
        obj.put("ob_datamodel_id", model_id);
        obj.put("ob_datamodel_seq", i);
        log.debug(obj.toString());
        mapper.itemInsert(obj);
      }
    }

    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public Map update(String id, Map param) throws Exception {
    Map fetched = mapper.selectId(id);
    if (fetched == null) {
      return null;
    }

    mapper.update(param);
    log.debug("item update start..............");
    List<?> list = new ArrayList<Object>();
    list = (List)param.get("itemsData");

    String model_id = (String) param.get("ob_datamodel_id");
    param.remove("item_size");
    param.remove("itemsData");

    mapper.deleteItem(model_id);

    if(list != null && list.size() > 0) {
      for(int i=0; i<list.size(); i++){
        Map obj = (Map)list.get(i);
        obj.put("ob_datamodel_id", model_id);
        obj.put("ob_datamodel_seq", i);
        log.debug(obj.toString());
        mapper.itemInsert(obj);
      }
    }
    log.debug("result : {}", param);
    return param;
  }

  @Transactional
  public void deleteObDatamodel(String id) throws Exception {
    mapper.deleteItem(id);
    mapper.deleteObDatamodel(id);
  }

}
