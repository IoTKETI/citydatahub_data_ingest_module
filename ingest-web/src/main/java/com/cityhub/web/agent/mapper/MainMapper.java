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
public interface MainMapper {

  List<Map> select_adtType();

  Map selectAllAgentId(String id);

  List<Map> selectAll();
  List<Map> platformType();
  Boolean isExist(String id);

  Boolean isExistAdapter(String id);

  int insert(Map param);
  int update(Map param);
  int delete(Map param);

  int insertAdaptor(Map param);
  int updateAdaptor(Map param);
  int deleteAdaptor(Map param);

  int insertInstance(Map param);
  int updateInstance(Map param);
  int deleteInstance(Map param);
  int deleteInstanceF(Map param);
  int deleteInstanceEtc(Map param);
  int deleteInstanceConf(String id);
  int insertInstanceEtc(Map param);


  Map selectByAdaptorId(String id);

  List<Map> selectAllAdaptorId(String id);

  List<Map> selectAgentConf(Map param);

  List<Map> selectInstanceIdAll(Map param);

  List<Map> selectInstanceDetail(Map param);

  List<Map> selectKeywordInfo();

  List<Map> selectAdaptorKeywordInfo(Map param);


  List<Map> selectAllInstanceAdaptorId(String id);

  List<Map> selectAllAdaptor();

  List<Map> selectObComboList();

  List<Map> selectAllInstance(String id);

  List<Map> selectDmTransform(Map param);
  int insertDmTransform(Map param);
  int updateDmTransform(Map param);
  Map selectDmTransformKey();

  List<Map> instanceBaseList(String id);

  List<Map> instanceBaseList2(String id);
  List<Map> instanceBaseList3(String id);

  List<Map> getInstanceList(String id);
  Map getInstanceInfo(String id);
  List<Map> instanceItem(String id);

  List<Map> instanceItem2(String id);
  List<Map> instanceItem3(String id);

  String newInstanceId(String id);

  Map getLastId();

}
