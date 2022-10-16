package com.cityhub.web.security.filter;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cityhub.web.security.exception.JwtAuthentioncationException;
import com.cityhub.web.security.exception.JwtExpiredException;
import com.cityhub.web.security.service.DataCoreUiSVC;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * Class for JWT authentication filter.
 *
 * @FileName JwtAuthenticationFilter.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final static String ROLE = "role";
  private final static String COOKIE = "Cookie";
  private final static String CHAUT = "chaut";
  private final static String AUTHTOKEN = "authToken";

  private AuthenticationEntryPoint entryPoint;
  private DataCoreUiSVC dataCoreUiSVC;

  /**
   * Constructor of JwtAuthenticationFilter
   *
   * @param entryPoint    AuthenticationEntryPoint
   * @param dataCoreUiSVC DataCoreUiSVC class
   */
  public JwtAuthenticationFilter(AuthenticationEntryPoint entryPoint, DataCoreUiSVC dataCoreUiSVC) {
    this.entryPoint = entryPoint;
    this.dataCoreUiSVC = dataCoreUiSVC;
  }

  /**
   * JWT verification
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    try {
      String jwt = getAccessToken(request.getHeader(COOKIE));

      if (jwt != null && isValidSession(request, response)) {
        PublicKey publicKey = getPublickey(jwt);

        Claims claims = getClaimsJws(jwt, publicKey);
        setSpringAuthentication(claims);
      } else {
        String loginUri = dataCoreUiSVC.getLoginUri(request);
        response.sendRedirect(loginUri);
        return;
      }

      chain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      if (dataCoreUiSVC.getRefreshToken(request, response)) {
        response.sendRedirect(request.getRequestURL().toString());
        return;
      } else {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        clearSessionAndCookie(request, response);
        entryPoint.commence(request, response, new JwtExpiredException("JWT Expired.", e));
      }
    } catch (JwtException e) {
      entryPoint.commence(request, response, new JwtAuthentioncationException("Authn Fail.", e));
    } catch (Exception e) {
      logger.error("Fail to set claims.", e);
    }
  }

  /**
   * Set the authentication with claims
   *
   * @param claims Claims
   */
  private void setSpringAuthentication(Claims claims) {
    String authority = (String) claims.get(ROLE);

    if (authority != null) {
      List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      grantedAuthorities.add(new SimpleGrantedAuthority(authority));

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.get("userId"), null, grantedAuthorities);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
  }

  /**
   * Get claims JWT
   *
   * @param jwt       Jason web tokens
   * @param publicKey PublicKey
   * @return Claims
   * @throws Exception Throw an exception when an IO error occurs.
   */
  private Claims getClaimsJws(String jwt, PublicKey publicKey) throws Exception {
    return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt).getBody();
  }

  /**
   * Get the public key of JWT.
   *
   * @param jwt Jason web tokens
   * @return PublicKey
   * @throws Exception Throw an exception when an IO error occurs.
   */
  private PublicKey getPublickey(String jwt) throws Exception {
    PublicKey publicKey = null;

    try {
      ResponseEntity<String> result = dataCoreUiSVC.getPublicKey(jwt);
      String responseStr = result.getBody();
      Map<String, Object> responseMap = new JSONObject(responseStr).toMap();
      String publicKeyStr = (String) responseMap.get("publickey");

      KeyFactory kf = null;

      kf = KeyFactory.getInstance("RSA");

      String publicKeyContent = publicKeyStr.replace("\n", "").replace("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\"", "");
      X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKeyContent));
      publicKey = kf.generatePublic(keySpecX509);

    } catch (Exception e) {
      logger.error("Failed to get public key.", e);
      throw e;
    }

    return publicKey;
  }

  /**
   * Get an access token from a cookie
   *
   * @param strCookie cookie
   * @return access token
   */
  private String getAccessToken(String strCookie) {
    if (strCookie == null) {
      return null;
    }

    String[] cookies = strCookie.split("; ");

    for (String cookie : cookies) {
      String[] accessToken = cookie.split("=");
      if ("chaut".equals(accessToken[0])) {
        return accessToken[1];
      }
    }

    return null;
  }

  /**
   * Check session validation
   *
   * @return Valid: true, Invalid: false
   */
  private boolean isValidSession(HttpServletRequest request, HttpServletResponse response) {
    if (request.getSession().getAttribute(AUTHTOKEN) == null) {
      clearSessionAndCookie(request, response);
      return false;
    }

    return true;
  }

  /**
   * Clear session and cookie
   */
  private void clearSessionAndCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie setCookie = new Cookie(CHAUT, null);
    setCookie.setPath("/");
    setCookie.setMaxAge(0);
    response.addCookie(setCookie);
    request.getSession().invalidate();
  }
}
