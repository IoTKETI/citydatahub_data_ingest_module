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
package com.cityhub.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlUtil {

  private static final int timeout = 3;

  public UrlUtil() {

  }

  /**
   * put
   *
   * <pre>
   * example :
   *Header[] headers = {
   * new BasicHeader("Content-type", "application/x-www-form-urlencoded")
   * ,new BasicHeader("Accept", "text/html,text/xml,application/xml")
   *};
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.put("http://localhost/item/?a=1&b1", headers, strJsonBody);
   * </pre>
   */
  public static HttpResponse put(String urladdr, Header[] headers, String strJsonBody) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(">>>{}", strJsonBody);
    }
    HttpPut request = new HttpPut(urladdr);
    request.setHeaders(headers);
    request.setEntity(new StringEntity(strJsonBody, StandardCharsets.UTF_8));
    HttpResponse hr = null;
    try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
      hr = createResponse(request, httpclient.execute(request));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception(e.getMessage());
    }
    return hr;
  }

  /**
   * put
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.patch(urladdr, headers, strJsonBody);
   * </pre>
   */
  public static HttpResponse put(String urladdr, ArrayList<Header> headers, String strJsonBody) {
    try {
      return put(urladdr, headers.toArray(new Header[0]), strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers.toArray(new Header[0]));
    }
  }

  /**
   * put
   *
   * <pre>
   * example :
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.put("http://localhost/item/?a=1&b1", "Content-type", "application/json", strJsonBody);
   * </pre>
   */
  public static HttpResponse put(String urladdr, String headerType, String headerValue, String strJsonBody) {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    try {
      return put(urladdr, headers, strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers);
    }
  }

  /**
   * get
   *
   * <pre>
   * example :
   *Header[] headers = {
   * new BasicHeader("Content-type", "application/x-www-form-urlencoded")
   * ,new BasicHeader("Accept", "text/html,text/xml,application/xml")
   *};
   *HttpResp resp = UrlConnect.get("http://localhost/item/?a=1&b1", headers);
   * </pre>
   */
  public static HttpResponse get(String urladdr, Header[] headers) throws Exception {
    HttpGet request = new HttpGet(urladdr);
    request.setHeaders(headers);
    HttpResponse hr = null;

    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeout * 1000);
    requestBuilder.setConnectionRequestTimeout(timeout * 1000);
    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestBuilder.build());

    try (CloseableHttpClient httpclient = builder.build()) {
      hr = createResponse(request, httpclient.execute(request));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception(e.getMessage());
    }
    return hr;
  }

  /**
   * get
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   *HttpResp resp = UrlConnect.get(urladdr, headers);
   * </pre>
   */
  public static HttpResponse get(String urladdr, ArrayList<Header> headers) {
    try {
      return get(urladdr, headers.toArray(new Header[0]));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers.toArray(new Header[0]));
    }
  }

  /**
   * get
   *
   * <pre>
   * example :
   * HttpResp resp = UrlConnect.get("http://localhost/item/?a=1&b1", "Content-type", "application/json" );
   * </pre>
   */
  public static HttpResponse get(String urladdr, String headerType, String headerValue) {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    try {
      return get(urladdr, headers);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers);
    }
  }

  /**
   * patch
   *
   * <pre>
   * example :
   *Header[] headers = {
   * new BasicHeader("Content-type", "application/x-www-form-urlencoded")
   * ,new BasicHeader("Accept", "text/html,text/xml,application/xml")
   *};
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.patch(urladdr, headers, strJsonBody);
   * </pre>
   *
   */
  public static HttpResponse patch(String urladdr, Header[] headers, String strJsonBody) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(">>>{}", strJsonBody);
    }
    HttpPatch request = new HttpPatch(urladdr);
    request.setHeaders(headers);
    request.setEntity(new StringEntity(strJsonBody, StandardCharsets.UTF_8));
    HttpResponse hr = null;

    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeout * 1000);
    requestBuilder.setConnectionRequestTimeout(timeout * 1000);
    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestBuilder.build());

    try (CloseableHttpClient httpclient = builder.build()) {
      hr = createResponse(request, httpclient.execute(request));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception(e.getMessage());
    }
    return hr;
  }

  /**
   * patch
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.patch(urladdr, headers, strJsonBody);
   * </pre>
   */
  public static HttpResponse patch(String urladdr, ArrayList<Header> headers, String strJsonBody) {
    try {
      return patch(urladdr, headers.toArray(new Header[0]), strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers.toArray(new Header[0]));
    }
  }

  /**
   * patch
   *
   * <pre>
   * example :
   * String strJsonBody = "{a:1}";
   * HttpResp resp = UrlConnect.patch("http://localhost/item/?a=1&b1", "Content-type", "application/json" , strJsonBody);
   * </pre>
   */
  public static HttpResponse patch(String urladdr, String headerType, String headerValue, String strJsonBody) {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    try {
      return patch(urladdr, headers, strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers);
    }
  }

  /**
   * post
   *
   * <pre>
   * example :
   *Header[] headers = {
   * new BasicHeader("Content-type", "application/x-www-form-urlencoded")
   * ,new BasicHeader("Accept", "text/html,text/xml,application/xml")
   *};
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.post(urladdr, headers, strJsonBody);
   * </pre>
   */
  public static HttpResponse post(String urladdr, Header[] headers, String strJsonBody) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(">>>{}", strJsonBody);
    }
    HttpPost request = new HttpPost(urladdr);
    request.setHeaders(headers);
    request.setEntity(new StringEntity(strJsonBody, StandardCharsets.UTF_8));
    HttpResponse hr = null;

    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeout * 1000);
    requestBuilder.setConnectionRequestTimeout(timeout * 1000);
    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestBuilder.build());

    try (CloseableHttpClient httpclient = builder.build()) {
      hr = createResponse(request, httpclient.execute(request));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception(e.getMessage());
    }
    return hr;
  }

  /**
   * post
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   *String strJsonBody = "{a:1}";
   *HttpResp resp = UrlConnect.post(urladdr, headers, strJsonBody);
   * </pre>
   */
  public static HttpResponse post(String urladdr, ArrayList<Header> headers, String strJsonBody) {
    try {
      return post(urladdr, headers.toArray(new Header[0]), strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers.toArray(new Header[0]));
    }
  }

  /**
   * post
   *
   * <pre>
   * example :
   * Header[] headers = {
   *   new BasicHeader("Accept", "text/html,text/xml,application/xml")
   * };
   * String strJsonBody = "{a:1}";
   * HttpResp resp = UrlConnect.post("http://localhost/item/?a=1&b1", headers , strJsonBody);
   * </pre>
   */
  public static HttpResponse post(String urladdr, String headerType, String headerValue, String strJsonBody) {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    try {
      return post(urladdr, headers, strJsonBody);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers);
    }
  }

  /**
   * delete
   *
   * <pre>
   * example :
   * Header[] headers = {
   *   new BasicHeader("Accept", "text/html,text/xml,application/xml")
   * };
   * HttpResp resp = UrlConnect.delete("http://localhost/item/?a=1&b1", headers);
   * </pre>
   */
  public static HttpResponse delete(String urladdr, Header[] headers) throws Exception {
    HttpDelete request = new HttpDelete(urladdr);
    request.setHeaders(headers);
    HttpResponse hr = null;

    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeout * 1000);
    requestBuilder.setConnectionRequestTimeout(timeout * 1000);
    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestBuilder.build());

    try (CloseableHttpClient httpclient = builder.build()) {
      hr = createResponse(request, httpclient.execute(request));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception(e.getMessage());
    }

    return hr;
  }

  /**
   * delete
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   *HttpResp resp = UrlConnect.delete(urladdr, headers);
   * </pre>
   */
  public static HttpResponse delete(String urladdr, ArrayList<Header> headers) {
    try {
      return delete(urladdr, headers.toArray(new Header[0]));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers.toArray(new Header[0]));
    }
  }

  /**
   * delete
   *
   * <pre>
   * example :
   * HttpResp resp = UrlConnect.delete("http://localhost/item/?a=1&b1", "Content-type", "application/json" );
   * </pre>
   */
  public static HttpResponse delete(String urladdr, String headerType, String headerValue) {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    try {
      return delete(urladdr, headers);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return new HttpResponse("", 9999, e.getMessage(), headers);
    }

  }

  /**
   * connect
   *
   * <pre>
   * example :
   * Header[] headers = {
   *   new BasicHeader("Accept", "text/html,text/xml,application/xml")
   * };
   * String strJsonBody = "{a:1}";
   * HttpResp resp = UrlConnect.connect(method, "http://localhost/item/?a=1&b1", headers,strJsonBody  );
   * <code>method : PUT, POST, GET, DELETE, PATCH</code>
   * </pre>
   */
  public static HttpResponse connect(String method, String urladdr, Header[] headers, String strJsonBody) throws Exception {
    HttpResponse resp = null;
    switch (method.toUpperCase()) {
    case "PUT":
      resp = put(urladdr, headers, strJsonBody);
      break;
    case "POST":
      resp = post(urladdr, headers, strJsonBody);
      break;
    case "GET":
      resp = get(urladdr, headers);
      break;
    case "DELETE":
      resp = delete(urladdr, headers);
      break;
    case "PATCH":
      resp = patch(urladdr, headers, strJsonBody);
      break;
    default:
      throw new Exception("Http '" + method + "' method not supported");
    }
    return resp;
  }

  /**
   * connect
   *
   * <pre>example :
   *ArrayList{@code <Header>} headers = new ArrayList<>();
   * headers.add(new BasicHeader("Content-type", "application/x-www-form-urlencoded"));
   * headers.add(new BasicHeader("Accept", "text/html,text/xml,application/xml"));
   * String strJsonBody = "{a:1}";
   * HttpResp resp = UrlConnect.connect(method, "http://localhost/item/?a=1&b1", headers, strJsonBody  );
   * <code>method : PUT, POST, GET, DELETE, PATCH</code>
   * </pre>
   */
  public static HttpResponse connect(String method, String urladdr, ArrayList<Header> headers, String strJsonBody) throws Exception {
    return connect(method, urladdr, headers.toArray(new Header[0]), strJsonBody);
  }

  /**
   * connect
   *
   * <pre>
   * example :
   * String strJsonBody = "{a:1}";
   * HttpResp resp = UrlConnect.connect(method, "http://localhost/item/?a=1&b1", "Content-type", "application/json" , strJsonBody  );
   * <code>method : PUT, POST, GET, DELETE, PATCH</code>
   * </pre>
   */
  public static HttpResponse connect(String method, String urladdr, String headerType, String headerValue, String strJsonBody) throws Exception {
    Header[] headers = { new BasicHeader(headerType, headerValue) };
    return connect(method, urladdr, headers, strJsonBody);
  }

  private static HttpResponse createResponse(HttpRequestBase request, CloseableHttpResponse httpRes) throws Exception {
    String payload = null;
    int StatusCode = -1;
    String StatusName = null;
    Header[] reqHeaders = {};
    Header[] resHeaders = {};

    try {
      if (httpRes == null) {
        return null;
      }
      if (log.isDebugEnabled()) {
        log.debug(">>>{}>>>{}>>>{}", request.getMethod(), httpRes.getStatusLine().toString(), request.getURI());
      }
      HttpEntity entity = httpRes.getEntity();
      if (entity != null) {
        payload = EntityUtils.toString(httpRes.getEntity());
        if (log.isDebugEnabled()) {
          log.debug("<<<{}", payload);
        }
      }
      StatusCode = httpRes.getStatusLine().getStatusCode();
      StatusName = httpRes.getStatusLine().getReasonPhrase();
      reqHeaders = request.getAllHeaders();
      resHeaders = httpRes.getAllHeaders();

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      throw new Exception("Http Error, " + e.getMessage());
    }

    return new HttpResponse(payload, StatusCode, StatusName, reqHeaders, resHeaders);
  }

  public static boolean isExist(String urladdr, Header[] headers) throws Exception {
    boolean bl = false;
    try {
      HttpResponse resp = get(urladdr, headers);
      if (resp.getStatusCode() >= 200 && resp.getStatusCode() <= 300) {
        bl = true;
      } else {
        bl = false;
      }
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return bl;
  }

} // end class
