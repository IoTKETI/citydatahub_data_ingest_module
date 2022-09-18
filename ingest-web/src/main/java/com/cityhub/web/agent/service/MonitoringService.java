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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cityhub.web.agent.mapper.MonitoringMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
@Service
public class MonitoringService {
  @Autowired
  MonitoringMapper mapper;

  public Map getById(String id) {
    return mapper.selectById(id);
  }

  public List<Map> selectAll() {
    return mapper.selectAll();
  }



  public List<Map> selectAllAdaptorId(String id) {
    return mapper.selectAllAdaptorId(id);
  }


  public Map insertConnectivityLog(Map param) throws Exception {
    int bl = mapper.insertConnectivityLog(param);
    log.debug("result : {}", param);
    return param;
  }

  public List<Map> selectHourSF(Map<String,String> param) {
    return mapper.selectHourSF(param);
  }
  public List<Map> selectFailType(Map<String,String> param) {
    return mapper.selectFailType(param);
  }


  public List<Map> selectTypeSF(Map<String,String> param) {
    return mapper.selectTypeSF(param);
  }
  public List<Map> selectTypeCnt(Map<String,String> param) {
    return mapper.selectTypeCnt(param);
  }


  public Map selectLastLogDt() {
    return mapper.selectLastLogDt();
  }

  public List<Map> selectLog(Map param) {
    return mapper.selectLog(param);
  }

}
