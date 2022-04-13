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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cityhub.web.agent.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AuthController {

  @Value("${auth.yn}")
  public String authYn; // 카프카 접속 URL

  @Value("${logoutEndPoint}")
  public String logoutEndPoint; // 카프카 접속 URL

  @Autowired
  private AuthService authService;


  @MessageMapping("/sendMessage")
  @SendTo("/topic/broadMessage")
  public Map<String,String> broadMessage(@RequestBody Map<String,String> param) throws Exception {
    Thread.sleep(10); // simulated delay
    return param;
  }


  @RequestMapping("/")
  public String index(HttpServletRequest request, HttpServletResponse response) {

    HttpSession session = request.getSession();
    // 권한 없는 사용자 로그아웃 처리
    if("adminSystem".equals(session.getAttribute("type"))  && !"Connectivity_Admin".equals(session.getAttribute("role"))   ) {
      return "redirect:/logout";
    } else {
      return "redirect:/monitor/dashView";
    }
  }


  @RequestMapping("/login")
  public void moveLogin(HttpServletRequest request, HttpServletResponse response) {
    if ("Y".equals(authYn) ) {
      String tokenCheck = authService.getTokenFromCookie(request);
      try {
        if (tokenCheck == null) {
          String authCodeUrl = authService.getAuthCode(request);
          response.sendRedirect(authCodeUrl);
          return;
        } else {
          response.sendRedirect("/");
          return;
        }
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    } else {
      try {
        response.sendRedirect("/");
        return;
      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
  }

  @RequestMapping("/logout")
  public String logOut(HttpServletRequest request, HttpServletResponse response) {
    authService.removeLogout(request, response);
    authService.removeCookie(request, response);
    authService.removeSession(request);

    return "redirect:lo";
  }


  @RequestMapping("/callClient")
  public void callClientCredentials(HttpServletRequest request, HttpServletResponse response) {

    String tokenCheck = authService.getTokenFromCookie(request);
    try {
      if (tokenCheck == null) {
        String token = authService.getTokenByClientCredentials();
        if (token != null) {
          authService.cookieAddTokenByString(response, authService.getTokenFromResponse(token));
          authService.createTokenSession(token, request,response);
          response.sendRedirect("moveTestA");
        } else {
          response.sendRedirect("");
        }
      } else {
        response.sendRedirect("moveTestA");
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }
  }

  @RequestMapping("/callPassword")
  public void callPasswordCredentials(HttpServletRequest request, HttpServletResponse response) {

    String tokenCheck = authService.getTokenFromCookie(request);

    try {
      if (tokenCheck == null) {
        String token = authService.getTokenByPasswordCredentials();
        if (token != null) {
          authService.cookieAddTokenByString(response, authService.getTokenFromResponse(token));
          authService.createTokenSession(token, request,response);
          response.sendRedirect("moveTestA");
        } else {
          response.sendRedirect("");
        }
      } else {
        response.sendRedirect("moveTestA");
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }

  }



  @RequestMapping("/moveTestA")
  public String moveTestA(HttpServletRequest request, HttpServletResponse response, Model model) {

    String token = authService.getTokenFromCookie(request);

    try {
      if (token != null) {
        if (authService.ValidateToken(authService.getPublicKey(), token, request, response)) {
          if (request.getSession().getAttribute("token") == null) {
            authService.createTokenSession(token, request,response);
          }
          model.addAttribute("chaut", authService.getTokenFromCookie(request));
          return "TestA";
        } else {
          authService.removeCookie(request, response);
          authService.removeSession(request);
          return "redirect:lo";
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }

    return "redirect:lo";
  }

  @RequestMapping("/moveToB")
  public void moveToB(HttpServletResponse response) {

    try {

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }
  }



  @ResponseBody
  @RequestMapping("/getInfo")
  public String getInfo(HttpServletRequest request, HttpServletResponse response) {
    return authService.callGetInfo(authService.getTokenFromCookie(request));
  }



}
