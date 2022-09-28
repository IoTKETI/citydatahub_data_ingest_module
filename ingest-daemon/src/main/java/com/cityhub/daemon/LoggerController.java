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

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cityhub.daemon.service.LoggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoggerController {

  private final LoggerService svc;

  @PostMapping(value = "/logToDbApi")
  public ResponseEntity<String> logToDbApi(@RequestBody Map<String, Object> param) {
    log.info("########logToDbApilogToDbApi");
    if (!param.isEmpty()) {
      svc.logToDbApi(param);
    } else {
      log.error("body is empty");
    }
    return new ResponseEntity<>("OK", HttpStatus.OK);

  }

}
