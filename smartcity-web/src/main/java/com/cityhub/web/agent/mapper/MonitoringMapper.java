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
package com.cityhub.web.agent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@SuppressWarnings("rawtypes")
@Mapper
public interface MonitoringMapper {

  List<Map> selectAll();

  Map selectById(String id);

  List<Map> selectAllAdaptorId(String id);

  List<Map> selectHourSF(Map<String,String> param);

  List<Map> selectFailType(Map<String,String> param);

  List<Map> selectTypeSF(Map<String,String> param);

  List<Map> selectTypeCnt(Map<String,String> param);

  List<Map> selectLog(Map param);


  Map selectLastLogDt();

  int insertConnectivityLog(Map param);

}
