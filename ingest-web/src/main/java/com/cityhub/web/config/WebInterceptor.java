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
package com.cityhub.web.config;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cityhub.web.agent.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebInterceptor implements HandlerInterceptor {

  @Autowired
  private AuthService authService;

  @Value("${auth.yn:N}")
  private String authYn;

  @Value("${logoutEndPoint}")
  private String logoutEndPoint;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession();

    Enumeration params = request.getParameterNames();
    while (params.hasMoreElements()){
        String name = (String)params.nextElement();
        try {
          request.setAttribute(name, request.getParameter(name));
        } catch (Exception e) {
          continue;
        }
    }

    if ("Y".equalsIgnoreCase(authYn)) {
      String code = request.getParameter("code");
      if(code!=null) {
        String token = authService.getTokenByAuthorizationCode(code);
        if (token != null) {
          authService.cookieAddTokenByJson(response, token);
          authService.createTokenSession(token, request,response);

          if (authService.ValidateToken(authService.getPublicKey(), token, request, response)) {
            try {
              String authCodeUrl = authService.getAuthCode(request);
              response.sendRedirect(authCodeUrl);
              return false;
            } catch (Exception e) {
              log.error("Exception : " + ExceptionUtils.getStackTrace(e));
            }
          }
        }
      }
      // 권한 없는 사용자 로그아웃 처리
      if( !"Connectivity_Admin".equals(session.getAttribute("role"))   ) {
        response.sendRedirect("/logout");
      }
    }

    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

  }

}
