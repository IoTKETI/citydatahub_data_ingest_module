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
import com.cityhub.web.agent.mapper.CommMapper;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
@Service
public class CommService {
  @Autowired
  CommMapper mapper;

  public List<Map> getAll() throws Exception {
    return mapper.getAll();
  }

  public List<Map> selectItem(String id) throws Exception {
    return mapper.selectItem(id);
  }

  public Map selectId(String id) throws Exception {
    return mapper.selectId(id);
  }

  @Transactional
  public Map insertCommType(Map param) throws Exception {

    List<?> list = new ArrayList<Object>();
    list = (List)param.get("comm_type_data");

    if(list != null && list.size() > 0) {
      for(int i=0; i<list.size(); i++){
        Map obj = (Map)list.get(i);
        String db_yn = (String) obj.get("db_yn");
        obj.remove("db_yn");
        if("N".equals(db_yn)) {
          mapper.insertCommType(obj);
        } else {
          mapper.updateCommType(obj);
        }

      }
    }

    return param;
  }

  @Transactional
  public Map insertCommCode(String id, Map param) throws Exception {
    log.debug("id = " + id);
    log.debug("param = " + param);
    mapper.deleteCommCode(id);
    List<?> list = new ArrayList<Object>();
    list = (List)param.get("codesData");
    if(list != null && list.size() > 0) {
      for(int i=0; i<list.size(); i++){
        Map obj = (Map)list.get(i);
        obj.put("code_type_id", id);
        log.debug("obj = " + obj);
        mapper.insertCommCode(obj);
      }
    }

    return param;
  }
}
