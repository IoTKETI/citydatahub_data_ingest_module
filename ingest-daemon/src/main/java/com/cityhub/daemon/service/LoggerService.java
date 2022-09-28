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
package com.cityhub.daemon.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggerService {

  private final SqlSessionTemplate sqlSession;

  public void logToDbApi(Map<String,Object> parameter) {
    JSONObject jobj = new JSONObject(parameter);
    Map<String,Object> param = new HashMap<>();
    String timestamp = jobj.get("timestamp").toString().replaceAll("\"", "");
    String payload = jobj.get("payload").toString().replaceAll("\"", "");
    String type = jobj.get("type").toString().replaceAll("\"", "");
    String step = jobj.get("step").toString().replaceAll("\"", "");
    String id = jobj.get("id").toString().replaceAll("\"", "");
    String sourceName = jobj.get("sourceName").toString().replaceAll("\"", "");
    String length = String.valueOf(jobj.get("length")).replaceAll("\"", "");
    String desc = jobj.get("desc").toString().replaceAll("\"", "");
    String adapterType = String.valueOf(jobj.get("adapterType")).replaceAll("\"", "");

    param.put("timestamp", timestamp);
    param.put("payload", payload);
    param.put("type", type);
    param.put("step", step);
    param.put("id", id);
    param.put("sourceName", sourceName);
    if ("null".equals(length)) {
      length = "0";
    }
    param.put("length", Integer.parseInt(length));
    param.put("desc", desc);

    if (adapterType.contains("OneM2M")) {
      param.put("adapterType", "OneM2M");
    } else if (adapterType.contains("OpenApiSource")) {
      param.put("adapterType", "OpenAPI");
    } else if (adapterType.contains("LegacyPlatform")) {
      param.put("adapterType", "LegacyPlatform");
    } else if (adapterType.contains("UrbanintegrationPlatform")) {
      param.put("adapterType", "도시통합Platform");
    } else if (adapterType.contains("Fiware")) {
      param.put("adapterType", "Fiware");
    } else {
      param.put("adapterType", "기타");
    }
    sqlSession.insert("loggerMapper.insertConnectivityLog", param);
  }


}
