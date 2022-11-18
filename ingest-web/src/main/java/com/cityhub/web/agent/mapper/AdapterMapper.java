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
public interface AdapterMapper {

  List<Map> getAll();
  Boolean isExistAdapterType(String id);
  List<Map> value_type_search();
  String new_code_id();
  int insertAdaptorType(Map param);
  int updateAdaptorType(Map param);
  List<Map> itemSelect(String id);
  List<Map> itemSelect2(String id);
  List<Map> itemSelect3(String id);
  int deleteAdtItem(String id);
  int deleteAdtItem2(String id);
  int deleteAdtItem3(String id);
  int adtItemInsert(Map param);
  List<Map> type_search();
  void deleteAdtTypeInfo(String id);
}
