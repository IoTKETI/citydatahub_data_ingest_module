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
package com.cityhub.daemon;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cityhub.daemon.dto.LogVO;
import com.cityhub.daemon.dto.LoggerObject;
import com.cityhub.daemon.service.AgentService;
import com.cityhub.utils.DateUtil;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@RestController
public class AgentController {

  @Value("${flume.log}")
  private String flumeLogPath;
  @Value("${flume.home}")
  private String flumeHomePath;
  @Value("${flume.conf}")
  private String flumeConfPath;

  @Autowired
  AgentService svc;

  /**
   * 아답터에서 생성된 로그 읽어오기
   * @param param
   * @return
   */
  @PostMapping(value = "/exec/logger")
  public ResponseEntity<Map> getLogger(@RequestBody Map param) {
    if (log.isDebugEnabled()) {
      log.debug("body: {}", param);
    }

    Map logmap = new HashMap();

    try {
      LoggerObject dto = svc.tail(param);

      logmap.put("endPoint", dto.getEndPoint());
      logmap.put("log", dto.getPayload());

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<Map>(logmap, HttpStatus.OK);

  }
  /**
   * 아답터에서 생성된 특정 아이디의 로그 읽어오기 
   * @param id
   * @param param
   * @return
   */
  @PostMapping(value = "/getlogger/{id}")
  public ResponseEntity<List<LogVO>> logIngest(@PathVariable("id") String id, @RequestBody Map param) {
    if (log.isDebugEnabled()) {
      log.debug("id: {},body:{}", id,param);
    }
    List<LogVO> l =  svc.getLinesMatchPatternInFile(id, param);

    return new ResponseEntity<List<LogVO>>(l, HttpStatus.OK);

  }
  
  /**
   * 아답터에서 생성된 전체 로그 읽어오기
   * @param param
   * @return
   */
  @PostMapping(value = "/getLoggerAll")
  public ResponseEntity<List<LogVO>> getLoggerAll( @RequestBody Map param) {
    if (log.isDebugEnabled()) {
      log.debug("{}", param);
    }
    List<LogVO> l =  svc.getAllLinesMatchPatternInFile(param);

    return new ResponseEntity<List<LogVO>>(l, HttpStatus.OK);

  }


  /**
   * 서버의 현재 시간 가져오기
   * @return
   */
  @GetMapping(value = "/getSrvTime")
  public ResponseEntity<String> getSrvTime() {

    String l = DateUtil.getTime("yyyy-MM-dd HH:mm:ss,SSS");

    return new ResponseEntity<String>(l, HttpStatus.OK);

  }

  /**
   * 에이전트의 현재 상태 가져오기
   * @param status
   * @param param
   * @return
   */
  @PostMapping(value = "/exec/agent/{status}")
  public ResponseEntity<Map> agentRun(@PathVariable("status") String status, @RequestBody Map param) {
    Map json = new HashMap();

    try {
      if (log.isDebugEnabled()) {
        log.debug("body: {}", param);
      }
      json.put("id", param.get("id").toString());


      File f = new File(flumeHomePath + "/conf/" + param.get("id") + ".conf");
      if (!f.exists()) {
        json.put("responseCode", "9999");
        json.put("responseDescription", "agent does not exist");
        json.put("status", "agent does not exist");
      } else {
        String result = svc.manageAgent(status, param);
        json.put("responseCode", "2000");
        json.put("responseDescription", "success");
        json.put("status", result);
      }

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<Map>(json, HttpStatus.OK);

  }


  /**
   * 아답터의 현재 설정파일 내용 가져오기
   * @param type
   * @param param
   * @return
   */
  @PostMapping(value = "/exec/config/{type}")
  public ResponseEntity<Map> setConfigAdapter(@PathVariable("type") String type, @RequestBody Map param) {
    Map json = new HashMap();
    try {
      json.put("id", param.get("id"));
      json.put("status", "OK");
      json.put("responseCode", "2000");
      json.put("responseDescription", "success");
      String path = "";

      switch (type) {
        case "agent":
          path = svc.isWindoowsOS() == true ? "/temp/": flumeHomePath + "/conf/";
          svc.manageAgentConfFile(path, param);
          break;

        case "adapter":
          path = svc.isWindoowsOS() == true ? "/temp/": flumeHomePath + "/plugins.d/agent/lib/openapi/";
          svc.manageAdapterConfFile(path, param);
          break;

        case "model":
          path = svc.isWindoowsOS() == true ? "/temp/": flumeHomePath + "/plugins.d/agent/lib/openapi/";
          svc.manageModelConfFile(path, param);
          break;

        case "validation":
          path = svc.isWindoowsOS() == true ? "/temp/": flumeHomePath + "/plugins.d/agent/lib/openapi/";
          svc.manageValidationConfFile(path, param);
          break;
        case "subscribe":
          path = svc.isWindoowsOS() == true ? "/temp/": flumeHomePath + "/plugins.d/agent/lib/openapi/";
          svc.manageSubscribeFile(path, param);
          break;

        default:
          json.put("id", param.get("id"));
          json.put("status", "NOT FOUND");
          json.put("responseCode", "9999");
          json.put("responseDescription", "NOT FOUND");
          break;
      }

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      json.put("id", param.get("id"));
      json.put("status", e.getMessage());
      json.put("responseCode", "9999");
      json.put("responseDescription", e.getMessage());
    }

    return new ResponseEntity<Map>(json, HttpStatus.OK);

  }


  /**
   * 웹 UI에서 작성된 내용을 컴파일하기
   * @param param
   * @return
   */
  @PostMapping(value = "/exec/compile")
  public ResponseEntity<Map> getCompile(@RequestBody Map param) {
    if (log.isDebugEnabled()) {
      log.debug("body: {}", param);
    }

    Map logmap = new HashMap();

    try {
      String dto = svc.sourceCodeCompile(param);

      logmap.put("log", dto);

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<Map>(logmap, HttpStatus.OK);

  }
  /**
   * 생성된 파일의 내용과 컴파일 여부 읽어오기
   * @param param
   * @return
   */
  @PostMapping(value = "/exec/read")
  public ResponseEntity<Map> getReadJavaFile(@RequestBody Map param) {
    if (log.isDebugEnabled()) {
      log.debug("body: {}", param);
    }
    Map logmap = new HashMap();

    try {
      String result = svc.readJavaFile(param);

      boolean isCompile = svc.isCompileJavaFile(param);
      logmap.put("body", result);
      logmap.put("isCompile", isCompile);

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<Map>(logmap, HttpStatus.OK);
  }




  /**
   * 에이전트의 현재 상태 읽어오기
   * @param agentId
   * @return
   */
  @GetMapping(value = "/exec/status/{agentId}")
  public ResponseEntity<Map> statusAgent(@PathVariable("agentId") String agentId) {
    Map json = new HashMap();

    try {
      String filename = agentId + ".conf";
      if (svc.getStatus(filename) == null) {
        json.put("status", "stop");
      } else {
        json.put("status", "running");
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return new ResponseEntity<Map>(json, HttpStatus.OK);

  }


}
