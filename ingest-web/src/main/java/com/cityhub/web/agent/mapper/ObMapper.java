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
public interface ObMapper {

  List<Map> getAll();
  List<Map> selectItem(String id);

  Map selectId(String id);
  List<Map> searchNm(Map param);

  Boolean isExist(String id);

  Boolean isExistIns(String id);

  int insert(Map param);
  int itemInsert(Map map);
  int update(Map param);
  int delete(Map param);
  void deleteItem(String id);
  void deleteObDatamodel(String id);
}
