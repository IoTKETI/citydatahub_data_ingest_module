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
package com.cityhub.web.agent.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.cityhub.web.config.ConfigEnv;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class AuthService {

  @Autowired
  private ConfigEnv configEnv;

  final static String AUTHORIZATION = "Authorization";
  final static String AUTHORIZATION_CODE = "authorization_code";
  final static String CODE = "code";
  final static String STATE = "state";
  final static String ACCESS_TOKEN = "access_token";
  final static String AUTHTOKEN = "authToken";
  final static String REFRESHTOKEN = "refreshtoken";
  final static String REFRESH_TOKEN = "refresh_token";
  final static String CHAUT = "chaut";
  final static Integer COOKIE_MAX_AGE = 60 * 60 * 1; // 1 hours
  final static String SHA256 = "SHA-256";


  private OkHttpClient client = new OkHttpClient();

  /**
   * @Author : jungyun
   * @Date : 2019-08-13
   * @param : HttpServletRequest
   * @return : auth코드 요청을위한 uri 반환
   */
  public String getAuthCode(HttpServletRequest request) {

    String state = sha256Encoder(request);
    String urlParam = "?response_type="+CODE+"&redirect_uri=" + configEnv.getRedirectUri() + "&client_id=" + configEnv.getClientId() + "&state=" + state + "";
    String apiUri = configEnv.getAuthorizationUri() + urlParam;

    return apiUri;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-13
   * @param :HttpServletRequest
   * @return : accessToken 반환
   */
  public String getTokenByAuthorizationCode(String code) {

    JSONObject object = new JSONObject();
    String resMessage = "";

    object.put("grant_type", AUTHORIZATION_CODE);
    object.put("client_id", configEnv.getClientId());
    object.put("client_secret", configEnv.getClientSecret());
    object.put("redirect_uri", configEnv.getRedirectUri());
    object.put(CODE, code);

    String url = configEnv.getTokenUri() + "?grant_type="+AUTHORIZATION_CODE+"&client_id=" + configEnv.getClientId() + "&client_secret=" + configEnv.getClientSecret() + "&redirect_uri=" + configEnv.getRedirectUri() + "&code=" + code;

    log.info("###url:{},{}", configEnv.getTokenUri(), object.toString());

    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), object.toString());
    Request okRequest = new Request.Builder().url(url).post(requestBody).build();
    Response response = null;

    try {
      response = client.newCall(okRequest).execute();
      resMessage = response.body().string();
      log.info("isSuccessful():{} , resMessage:{}", response.isSuccessful(), resMessage);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }

    if (response.isSuccessful()) {
      return resMessage;
    } else {
      return null;
    }

  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-13
   * @param :
   * @return : ClientCredentials 방식 토큰 반환
   */
  public String getTokenByClientCredentials() {

    String clientCredentials = configEnv.getClientId() + ":" + configEnv.getClientSecret();
    String resMessage = "";
    String apiheader = "Basic " + Base64Utils.encodeToString(clientCredentials.getBytes());
    JSONObject object = new JSONObject();

    object.put("grant_type", "client_credentials");

    resMessage = httpConnection(configEnv.getTokenUri(), "POST", apiheader, object.toString());
    log.info("resMessage:{}", resMessage);
    return resMessage;

  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-13
   * @param :
   * @return : PasswordCredentials 방식 토큰 반환
   */
  public String getTokenByPasswordCredentials() {

    String passwordCredentials = configEnv.getClientId() + ":" + configEnv.getClientSecret();
    String apiheader = "Basic " + Base64Utils.encodeToString(passwordCredentials.getBytes());
    JSONObject object = new JSONObject();
    String resMessage = "";

    object.put("grant_type", "password");
    resMessage = httpConnection(configEnv.getTokenUri(), "POST", apiheader, object.toString());
    log.info("resMessage:{}", resMessage);
    return resMessage;

  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-13
   * @param :HttpServletRequest
   * @return : SHA-256으로 암호화된 sessionid 반환
   */
  private String sha256Encoder(HttpServletRequest request) {

    HttpSession session = request.getSession();
    String sessionId = session.getId();
    StringBuffer state = null;
    String encodingType = SHA256;

    try {
      MessageDigest digest = MessageDigest.getInstance(encodingType);
      byte[] hash = digest.digest(sessionId.getBytes("UTF-8"));
      state = new StringBuffer();
      for (byte element : hash) {
        String hex = Integer.toHexString(0xff & element);
        if (hex.length() == 1) {
          state.append('0');
        }
        state.append(hex);
      }
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
    }

    return state.toString();
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : HttpServletRequest
   * @return : String token 쿠키에서 토큰을 파싱해 반환
   */
  public String getTokenFromCookie(HttpServletRequest request) {

    Cookie[] cookies = request.getCookies();
    String token = null;

    if (cookies != null) {
      for (Cookie itr : cookies) {
        if (itr.getName().equals(CHAUT)) {
          token = itr.getValue();
          break;
        }
      }
    }

    return token;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : String (토큰요청api 콜 응답값)
   * @return : Cookie 생성및 설정후 반환
   */
  public Cookie cookieAddTokenByString(HttpServletResponse response, String tokenResponse) {

    Cookie cookie = null;

    if (tokenResponse != null) {
      cookie = new Cookie(CHAUT, tokenResponse);

      cookie.setHttpOnly(true);
      cookie.setSecure(false);
      cookie.setMaxAge(60 * 60 * 24);
      response.addCookie(cookie);
    }

    return cookie;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : String (토큰요청api 콜 응답값)
   * @return : Cookie 생성및 설정후 반환
   */
  public Cookie cookieAddTokenByJson(HttpServletResponse response, String tokenResponse) {
    Cookie cookie = null;
    if (tokenResponse != null) {
      JSONObject token = new JSONObject(tokenResponse);
      String target = ACCESS_TOKEN;
      cookie = new Cookie(CHAUT, token.getString(target));

      cookie.setHttpOnly(true);
      cookie.setSecure(false);
      // cookie.setDomain("192.168.123.140:8083/");
      cookie.setMaxAge(60 * 60 * 24);
      response.addCookie(cookie);

    }

    return cookie;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : HttpServletRequest HttpServletResponse
   * @return :
   */
  public void removeCookie(HttpServletRequest request, HttpServletResponse response) {

    Cookie cookie = new Cookie(CHAUT, null);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : HttpServletRequest
   * @return :
   */
  public void removeSession(HttpServletRequest request) {

    HttpSession session = request.getSession();
    session.removeAttribute("token");
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : HttpServletRequest HttpServletResponse
   * @return :
   */
  public void removeLogout(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    okhttp3.RequestBody body = null;
    if (session.getAttribute("token") != null) {
      try {
        String bodyStr = "{\"userId\":\"" + session.getAttribute("userId") + "\"}";
        body = okhttp3.RequestBody.create(MediaType.parse("application/json"), bodyStr);

        JSONObject token = new JSONObject(session.getAttribute("token").toString());
        String target = ACCESS_TOKEN;

        String resMessage = "";

        resMessage = httpConnection(configEnv.getLogoutUri(), "POST", "Basic " + token.getString(target), bodyStr);
        log.info("resMessage:{}", resMessage);

      } catch (Exception e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : String ,HttpServletRequest
   * @return :
   */
  public void createTokenSession(String tokenResponse, HttpServletRequest request, HttpServletResponse response) {

    HttpSession session = request.getSession();

    if (tokenResponse != null) {
      session.setAttribute("token", tokenResponse);
      JSONObject token = new JSONObject(tokenResponse);
      String target = ACCESS_TOKEN;
      String getInfo = callGetInfo(token.getString(target));
      if (getInfo != null && !"".equals(getInfo)) {
        JSONObject jsonInfo = new JSONObject(getInfo);
        session.setAttribute("chaut", token.getString(target));
        session.setAttribute("role", jsonInfo.getString("role"));
        session.setAttribute("userId", jsonInfo.getString("userId"));
        session.setAttribute("nickname", jsonInfo.getString("nickname"));
      }
    }
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-23
   * @param : String
   * @return : 토큰발급api 응답값을 받아 access-token 분리 후 응답
   */
  public String getTokenFromResponse(String tokenResponse) {
    String token = null;
    if (tokenResponse != null) {
      JSONObject tokenJsonObject = new JSONObject(tokenResponse);
      String target = ACCESS_TOKEN;
      token = tokenJsonObject.getString(target);
    }

    return token;
  }

  public String httpConnection(String targetUrl, String method, String token, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    String result = "";
    JSONObject json = null;
    String jsonData = "";
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    int responseCode;

    try {
      url = new URL(targetUrl);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod(method);
      if (!"".equals(token)) {
        conn.setRequestProperty("Authorization", token);
      }

      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonBody);
        wr.flush();
      }
      responseCode = conn.getResponseCode();
      if (responseCode < 400) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
      } else {
        br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      }
      sb = new StringBuffer();
      while ((jsonData = br.readLine()) != null) {
        sb.append(jsonData);
      }
      returnText = sb.toString();
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {

    }
    return returnText;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-23
   * @param :
   * @return : api콜 응답값(공개키)
   */
  public String getPublicKey() {
    String clientCredentials = configEnv.getClientId() + ":" + configEnv.getClientSecret();
    String apiheader = "Bearer " + Base64Utils.encodeToString(clientCredentials.getBytes());
    log.info("apiheader :{},{}", configEnv.getPublicKeyUri(), apiheader);
    String resMessage = "";

    resMessage = httpConnection(configEnv.getPublicKeyUri(), "GET", apiheader, "");
    log.info("resMessage:{}", resMessage);

    return resMessage;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-23
   * @param : String publicKeyResponse,String token
   * @return : 토큰 검증후 true/false
   */
  public boolean ValidateToken(String publicKeyResponse, String token, HttpServletRequest request, HttpServletResponse response)
      throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

    KeyFactory kf = KeyFactory.getInstance("RSA");
    log.info("publicKeyResponse:{}", publicKeyResponse);

    JSONObject jPublicKey = new JSONObject(publicKeyResponse);
    String publicKeyContent = jPublicKey.getString("publickey").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
    publicKeyContent = publicKeyContent.replaceAll("\\r\\n", "").replaceAll("\\n", "").trim();
    log.info("publicKeyContent:{}", publicKeyContent);
    X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyContent));
    PublicKey publicKey = kf.generatePublic(keySpecX509);

    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
      if (claims.getBody().getExpiration().before(new Date())) {
        return callRefreshToken(request, response);
      }
      return true;
    } catch (ExpiredJwtException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return callRefreshToken(request, response);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return false;
    } finally {
    }
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-21
   * @param : String token
   * @return : 유저정보호출 api call 결과값
   */
  public String callGetInfo(String token) {
    String resMessage = "";
    try {
      String[] t = token.split("\\.", -1);
      Decoder decoder = Base64.getDecoder();
      byte[] decodedBytes = decoder.decode(t[1].getBytes());
      resMessage = new String(decodedBytes);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return resMessage;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-29
   * @param : HttpServletRequest
   * @return : session에서 refreshToken 분리해서 리턴
   */
  public String getRefreshTokenFromSession(HttpServletRequest request) {

    String token = (String) request.getSession().getAttribute("token");
    String refreshToken = null;

    if (token != null) {
      JSONObject getRefreshToken = new JSONObject(token);
      String target = REFRESH_TOKEN;
      if (getRefreshToken.get(target) != null) {
        refreshToken = getRefreshToken.getString(target);
      }
    }

    return refreshToken;
  }

  /**
   * @Author : jungyun
   * @Date : 2019-08-29
   * @param : HttpServletRequest,HttpServletResponse
   * @return : token발급 성공시true 실패시 false
   */
  public boolean callRefreshToken(HttpServletRequest request, HttpServletResponse response) {
    String base64IdPw = configEnv.getClientId() + ":" + configEnv.getClientSecret();
    String refreshHeader = "Basic " + Base64Utils.encodeToString(base64IdPw.getBytes());
    String refreshToken = getRefreshTokenFromSession(request);
    String resMessage = "";
    JSONObject object = new JSONObject();

    if (refreshToken == null) {
      return false;
    }

    object.put("grant_type", REFRESH_TOKEN);
    object.put("refresh_token", refreshToken);

    resMessage = httpConnection(configEnv.getTokenUri(), "POST", refreshHeader, object.toString());

    log.debug("리프레시 토큰 값 : " + refreshToken);
    log.debug("리프레시 토큰 응답 : " + resMessage);
    cookieAddTokenByJson(response, resMessage);
    return true;
  }

}
