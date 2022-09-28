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
package com.cityhub.web.agent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.utils.JsonUtil;
import com.cityhub.web.agent.service.CommService;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
@RestController
public class CommController {

  public static final String FIELD_HEX = (char) 0xD1 + "";

  @Autowired
  CommService cs;

  @GetMapping({ "/commList" })
  public ModelAndView CommListView() {
    log.debug("----- CommController.CommListView() -----");
    return new ModelAndView("commList");
  }

  @GetMapping("/commDataList")
  public ResponseEntity<List<Map>> commDataListView() {
    log.debug("----- CommController.commDataListView() -----");
    List<Map> result = new ArrayList<>();
    try {
      result = cs.getAll();
      log.debug("result =" + result);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/commDetail/{id}")
  public ModelAndView commDetailViewEx(@PathVariable("id") String id) {
    log.debug("----- CommController.CommDetailViewEx() -----");
    ModelAndView modelAndView = new ModelAndView();
    Map code_type_id = new HashMap<>();
    code_type_id.put("code_type_id", id);
    modelAndView.setViewName("commDetail");
    modelAndView.addObject("code_type_id", code_type_id);
    return modelAndView;
  }

  @GetMapping("/commCodeView/{id}")
  public ResponseEntity<Map> commCodeView(@PathVariable("id") String id) {
    log.debug("----- CommController.CommCodeView() -----");
    Map map = new HashMap<>();
    try {
      map = cs.selectId(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @GetMapping("/commDetailView/{id}")
  public ResponseEntity<List<Map>> commDetailView(@PathVariable("id") String id) {
    log.debug("----- CommController.CommDetailView() -----");
    List map = new ArrayList<>();
    try {
      map = cs.selectItem(id);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @PostMapping("/commTypeSave")
  public ResponseEntity<Map> commTypeSave(@RequestBody Map param) {
    log.debug("----- CommController.commTypeSave() -----");
    try {
      cs.insertCommType(param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  @PostMapping("/commCodeSave/{id}")
  public ResponseEntity<Map> adtItemSave(@PathVariable("id") String id, @RequestBody Map param) {
    log.debug("----- CommController.commCodeSave() -----");
    try {
      cs.insertCommCode(id, param);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return new ResponseEntity<>(JsonUtil.toMap(HttpStatus.CREATED), HttpStatus.CREATED);
  }

}
