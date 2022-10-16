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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cityhub.web.security.service.DataCoreUiSVC;
import com.cityhub.web.security.vo.UserVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AuthController {

  @Autowired
  DataCoreUiSVC dataCoreUiSVC;

  @GetMapping("/accesstoken")
  public @ResponseBody void getAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {

    dataCoreUiSVC.getAccessToken(request, response);

    String contextPath = request.getContextPath();
    response.sendRedirect(contextPath + "/");
  }


  /**
   * Responds to user information when requesting user url.
   * @return        User information
   * @throws Exception  Throw an exception when an error occurs.
   */
  @GetMapping("/user")
  public ResponseEntity<UserVO> getUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return dataCoreUiSVC.getUser(request);
  }

  /**
   * Responds to user ID when requesting user url.
   * @return        User ID
   * @throws Exception  Throw an exception when an error occurs.
   */
  @GetMapping("/userId")
  public ResponseEntity<String> getUserId(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return dataCoreUiSVC.getUserId(request);
  }

  /**
   * Logout processing when requesting logout url.
   * @return        Http status
   * @throws Exception  Throw an exception when an IO error occurs.
   */
  @GetMapping("/logout")
  public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
    dataCoreUiSVC.logout(request, response, null);

    return ResponseEntity.ok().build();
  }


}
