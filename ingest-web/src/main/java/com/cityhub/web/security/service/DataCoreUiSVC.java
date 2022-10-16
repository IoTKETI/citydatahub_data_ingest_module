package com.cityhub.web.security.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.cityhub.web.config.ConfigEnv;
import com.cityhub.web.security.vo.AccessTokenFormVO;
import com.cityhub.web.security.vo.RefreshTokenFormVO;
import com.cityhub.web.security.vo.UserVO;

import lombok.extern.slf4j.Slf4j;

/**
 * Class for Data core UI service.
 *
 * @FileName DataCoreUiSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@Service
public class DataCoreUiSVC {

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

  @Autowired
  private DataCoreRestSVC dataCoreRestSVC;

  /**
   * Encrypt the string with SHA256.
   * @param txt             The string to apply encryption to.
   * @return                Encrypted string value.
   * @throws NoSuchAlgorithmException   Throw an exception when a "NoSuchAlgorithm" error occurs.
   */
  public String stringToSHA256(String txt) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(SHA256);
    md.update(txt.getBytes());
    return byteToHexString(md.digest());
  }

  /**
   * Encrypt the byte with HexString.
   * @param data  Byte array data
   * @return    Hex string
   */
  public String byteToHexString(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for(byte b : data) {
      sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

    }
    return sb.toString();
  }

  /**
   * Create login uri for SSO authentication.
   *
   * @return Login uri
   */
  public String getLoginUri(HttpServletRequest request) {
    String authorizeUri = configEnv.getAuthorizationUri();
    String redirectUri = configEnv.getRedirectUri();
    String clientId = configEnv.getClientId();
    String state = "";
    String loginUri = "";
    String sessionId = request.getSession().getId();

    try {
      if (sessionId != null) {
        state = stringToSHA256(sessionId);
      }
    } catch (NoSuchAlgorithmException e) {
      log.error("Fail to create state.", e);
    }

    loginUri = authorizeUri + "?response_type=code" + "&redirect_uri=" + redirectUri + "&client_id=" + clientId + "&state=" + state;

    log.debug("getLoginUri() - loginUri:{}", loginUri);

    return loginUri;
  }

  /**
   * Set the access token to the cookie
   */
  public void getAccessToken(HttpServletRequest request, HttpServletResponse response) {
    AccessTokenFormVO form = new AccessTokenFormVO();
    form.setGrant_type(AUTHORIZATION_CODE);
    form.setCode(request.getParameter(CODE));
    form.setRedirect_uri(configEnv.getRedirectUri());
    form.setClient_id(configEnv.getClientId());
    form.setClient_secret(configEnv.getClientSecret());

    try {
      log.info("!!!{}",new JSONObject(form).toString());
      ResponseEntity<String> result = dataCoreRestSVC.post(configEnv.getTokenUri(), null, null, form, null, String.class);
      log.info("@@@@@@@@@@result:{}",result);
      if (result != null && result.getBody() != null) {
        String tokenJson = result.getBody();
        setTokenToSessionAndCookie(request, response, tokenJson);
      }
    } catch (Exception e) {
      log.error("Failed to get access_token: {}", e.getMessage());
    }
  }

  /**
   * Get a refresh token from the SSO server.
   *
   * @return Success: true, Failed: false
   */
  public boolean getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
    Map<String, String> header = new HashMap<>();
    RefreshTokenFormVO form = new RefreshTokenFormVO();
    String authorization = getAuthorization();

    header.put(AUTHORIZATION, "Basic " + authorization);
    form.setGrant_type(REFRESH_TOKEN);
    form.setRefresh_token((String) request.getSession().getAttribute(REFRESHTOKEN));

    try {
      ResponseEntity<String> result = dataCoreRestSVC.post(configEnv.getTokenUri(), null, header, form, null, String.class);

      if (result != null && result.getBody() != null) {
        String tokenJson = result.getBody();
        setTokenToSessionAndCookie(request, response, tokenJson);
        return true;
      }
    } catch (Exception e) {
      log.error("Failed to get refresh_token.", e);
      return false;
    }

    return false;
  }

  /**
   * Set token to session and cookie
   *
   * @param tokenJson Json type token
   */
  private void setTokenToSessionAndCookie(HttpServletRequest request, HttpServletResponse response, String tokenJson) {
    Map<String, Object> tokenMap = new JSONObject(tokenJson).toMap();
    String accessToken = (String) tokenMap.get(ACCESS_TOKEN);
    request.getSession().setAttribute(AUTHTOKEN, accessToken);
    request.getSession().setAttribute(REFRESHTOKEN, (String) tokenMap.get(REFRESH_TOKEN));
    String jmsg = callGetInfo(accessToken);
    if (jmsg.startsWith("{")) {
      JSONObject body = new JSONObject(jmsg) ;
      request.getSession().setAttribute("userId", body.getString("userId"));
      request.getSession().setAttribute("nickname", body.getString("nickname"));
    }

    Cookie setCookie = new Cookie(CHAUT, accessToken);
    setCookie.setPath("/");
    setCookie.setMaxAge(COOKIE_MAX_AGE);
    response.addCookie(setCookie);
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
   * Get public key or JWT
   *
   * @param jwtToken Json type token
   * @return Public key
   */
  public ResponseEntity<String> getPublicKey(String jwt) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

    return dataCoreRestSVC.get(configEnv.getPublicKeyUri(), null, headers, null, null, String.class);
  }

  /**
   * For logout, logout is processed to the SSO server and the session and cookie
   * are cleared.
   *
   * @param object User ID
   * @throws JSONException Throw an exception when a json parsing error occurs.
   * @throws IOException   Throw an exception when an IO error occurs.
   */
  public void logout(HttpServletRequest request, HttpServletResponse response, Object object) throws JSONException, IOException {
    Object principal = getPrincipal(request);

    if (principal != null) {
      UserVO user = new UserVO();

      user.setUserId(principal.toString());
      Map<String, String> headers = new HashMap<>();
      headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));

      // SSO Logout
      dataCoreRestSVC.post(configEnv.getLogoutUri(), null, headers, user, null, Void.class);
    } else if (object != null) {
      UserVO user = new UserVO();

      user.setUserId(object.toString());
      Map<String, String> headers = new HashMap<>();
      headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));

      // SSO Logout
      dataCoreRestSVC.post(configEnv.getLogoutUri(), null, headers, user, null, Void.class);
    }

    // Clear cookie and session
    Cookie setCookie = new Cookie(CHAUT, null);
    setCookie.setPath("/");
    setCookie.setMaxAge(0);
    response.addCookie(setCookie);
    request.getSession().invalidate();
    response.sendRedirect("/");
  }

  /**
   * Get user information
   *
   * @return User information
   */
  public ResponseEntity<UserVO> getUser(HttpServletRequest request) {
    ResponseEntity<UserVO> user = null;

    // test data
    if ("N".equals(configEnv.getAuthYn())) {
      return ResponseEntity.ok().body(getTestUser());
    }

    Object principal = getPrincipal(request);

    if (principal != null) {
      String userId = principal.toString();
      String pathUri = "/" + userId;
      Map<String, String> headers = new HashMap<>();
      headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + request.getSession().getAttribute(AUTHTOKEN));

      user = dataCoreRestSVC.get(configEnv.getUserInfoUri(), pathUri, headers, null, null, UserVO.class);
    } else {
      return null;
    }

    return ResponseEntity.ok().body(user.getBody());
  }

  /**
   * Get user ID
   *
   * @return User ID
   */
  public ResponseEntity<String> getUserId(HttpServletRequest request) {

    // test data
    if ("N".equals(configEnv.getAuthYn())) {
      return ResponseEntity.ok().body("cityhub03");
    }

    Object securityContextObject = request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    if (securityContextObject != null) {
      SecurityContext securityContext = (SecurityContext) securityContextObject;
      Authentication authentication = securityContext.getAuthentication();
      if (authentication != null && authentication.getPrincipal() != null) {
        return ResponseEntity.ok().body(authentication.getPrincipal().toString());
      }
    }

    return ResponseEntity.badRequest().build();
  }

  /**
   * Get principal information from request
   *
   * @return Principal
   */
  public Object getPrincipal(HttpServletRequest request) {
    Object securityContextObject = request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

    if (securityContextObject != null) {
      SecurityContext securityContext = (SecurityContext) securityContextObject;
      Authentication authentication = securityContext.getAuthentication();

      if (authentication != null && authentication.getPrincipal() != null) {
        return authentication.getPrincipal();
      }
    }

    return null;
  }

  /**
   * Test user data
   *
   * @return Test user
   */
  private UserVO getTestUser() {
    UserVO user = new UserVO();
    user.setUserId("cityhub03");
    user.setNickname("관리자");

    return user;
  }

  /**
   * Get information of authorization
   *
   * @return Authorization information
   */
  private String getAuthorization() {
    Encoder encoder = Base64.getEncoder();
    String authorization = configEnv.getClientId() + ":" + configEnv.getClientSecret();

    return encoder.encodeToString(authorization.getBytes());
  }
}
