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
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class OkUrlUtil {

  private static final int timeout = 3;
  private static final int MAX_TRY_COUNT = 3;
  private static final int RETRY_BACKOFF_DELAY = 3000;
  public static final MediaType JSON = MediaType.get("application/json;charset=UTF-8");

  public static OkHttpClient getClient() {
    return new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
              Request request = chain.request();

              // try the request
              Response response = null;
              int tryCount = 1;
              while (tryCount <= MAX_TRY_COUNT) {
                try {
                  response = chain.proceed(request);
                  break;
                } catch (Exception e) {
                  log.error("tryCount: {}, msg: {}",tryCount, ExceptionUtils.getStackTrace(e));
                  if ("Canceled".equalsIgnoreCase(e.getMessage())) {
                      // Request canceled, do not retry
                      throw e;
                  }
                  if (tryCount >= MAX_TRY_COUNT) {
                      // max retry count reached, giving up
                      throw e;
                  }
                  try {
                      // sleep delay * try count (e.g. 1st retry after 3000ms, 2nd after 6000ms, etc.)
                      Thread.sleep(RETRY_BACKOFF_DELAY * tryCount);
                  } catch (InterruptedException e1) {
                      throw new RuntimeException(e1);
                  }
                  tryCount++;
                }
              }
              // otherwise just pass the original response on
              return response;
            }
          })
        .connectTimeout(timeout , TimeUnit.SECONDS)
        .writeTimeout(timeout  * 10, TimeUnit.SECONDS)
        .readTimeout(timeout  * 10, TimeUnit.SECONDS)
        .build();
  }

  public static OkHttpClient getUnsafeOkHttpClient() {
    try {
      final TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
          }

          @Override
          public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
          }

          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
          }
        }
      };

      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      OkHttpClient.Builder builder = new OkHttpClient.Builder();
      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

      builder.hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });

      return builder
          .addInterceptor(new Interceptor() {
              @Override
              public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                // try the request
                Response response = null;
                int tryCount = 1;
                while (tryCount <= MAX_TRY_COUNT) {
                  try {
                    response = chain.proceed(request);
                    break;
                  } catch (Exception e) {
                    log.error("tryCount: {}, msg: {}",tryCount, ExceptionUtils.getStackTrace(e));
                    if ("Canceled".equalsIgnoreCase(e.getMessage())) {
                        // Request canceled, do not retry
                        throw e;
                    }
                    if (tryCount >= MAX_TRY_COUNT) {
                        // max retry count reached, giving up
                        throw e;
                    }
                    try {
                        // sleep delay * try count (e.g. 1st retry after 3000ms, 2nd after 6000ms, etc.)
                        Thread.sleep(RETRY_BACKOFF_DELAY * tryCount);
                    } catch (InterruptedException e1) {
                        throw new RuntimeException(e1);
                    }
                    tryCount++;
                  }
                }
                // otherwise just pass the original response on
                return response;
              }
            })
          .connectTimeout(timeout , TimeUnit.SECONDS)
          .writeTimeout(timeout  * 10, TimeUnit.SECONDS)
          .readTimeout(timeout  * 10, TimeUnit.SECONDS)
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * http GET 메소드
   *
   * <pre>example :
   * Map h = new HashMap();
   * h.put("Content-type", "application/x-www-form-urlencoded");
   * h.put("Accept", "text/html,text/xml,application/xml");
   *
   * HttpResp resp = OkUrlUtil.get("http://localhost/item/?a=1&b1", h);
   * </pre>
   */
  public static HttpResponse get(String urladdr, String headerType, String headerValue) {
    Map<String,String> h = new HashMap<String,String>();
    h.put(headerType,headerValue);
    try  {
      return get(urladdr, h);
    } catch (Exception e) {
      return new HttpResponse(urladdr, 9999, e.getMessage());
    }
  }
  /**
   * http GET 메소드
   * @param urladdr
   * @param headers
   * @return
   * @throws Exception
   */
  public static HttpResponse get(String urladdr, Map<String,String> headers) throws Exception {
    Headers headerbuild = Headers.of(headers);
    Request request = new Request.Builder().url(urladdr).get().headers(headerbuild).build();

    try (Response response = getClient().newCall(request).execute()) {
      String msg =  response.body().string();
      if (log.isTraceEnabled()) {
        log.trace("<<<{}" , msg);
      }
      return new HttpResponse(msg, response.code(), HttpStatus.valueOf(response.code()).getReasonPhrase());
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      return new HttpResponse(urladdr, 9999, e.getMessage());
    }
  }
  /**
   * http GET SSL 메소드
   * @param urladdr
   * @param headers
   * @return
   * @throws Exception
   */
  public static HttpResponse getSSL(String urladdr, Map<String,String> headers) throws Exception {
    Headers headerbuild = Headers.of(headers);
    Request request = new Request.Builder().url(urladdr).get().headers(headerbuild).build();

    try (Response response = getUnsafeOkHttpClient().newCall(request).execute()) {
      String msg =  response.body().string();
      if (log.isTraceEnabled()) {
        log.trace("<<<{}" , msg);
      }
      return new HttpResponse(msg, response.code(), HttpStatus.valueOf(response.code()).getReasonPhrase());
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      return new HttpResponse(urladdr, 9999, e.getMessage());
    }
  }

  /**
   * http POST 메소드
   * @param urladdr
   * @param headers
   * @param strJsonBody
   * @return
   * @throws Exception
   */
  public static HttpResponse post(String urladdr, Map<String,String> headers, String strJsonBody) throws Exception {

    if (log.isTraceEnabled()) {
      log.trace(">>>{}" , strJsonBody);
    }
    RequestBody body = null;
    try {
      body = RequestBody.create(JSON, strJsonBody);
    } catch (NullPointerException npe) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(npe));
      return new HttpResponse("{\"resultCode\":4105, \"resultMsg\":\"UNSUPPORTED MEDIA TYPE\"}", 4105, npe.getMessage());
    }

    if (body != null) {
      Headers headerbuild = Headers.of(headers);
      Request request = new Request.Builder().url(urladdr).post(body).headers(headerbuild).build();

      try (Response response = getClient().newCall(request).execute()) {
        String msg =  response.body().string();
        if (log.isTraceEnabled()) {
          log.trace("<<<{}" , msg);
        }
        return new HttpResponse(msg, response.code(), response.message());
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        return new HttpResponse(null, 9999, e.getMessage());
      }
    } else {
      return new HttpResponse("{\"resultCode\":4000, \"resultMsg\":\"BAD REQUEST\"}", 4000, "");
    }
  }
  /**
   * http POST SSL 메소드
   * @param urladdr
   * @param headers
   * @param strJsonBody
   * @return
   * @throws Exception
   */
  public static HttpResponse postSSL(String urladdr, Map<String,String> headers, String strJsonBody) throws Exception {

    if (log.isTraceEnabled()) {
      log.trace(">>>{}" , strJsonBody);
    }
    RequestBody body = null;
    try {
      body = RequestBody.create(JSON, strJsonBody);
    } catch (NullPointerException npe) {
      return new HttpResponse("{\"resultCode\":4105, \"resultMsg\":\"UNSUPPORTED MEDIA TYPE\"}", 4105, npe.getMessage());
    }

    if (body != null) {
      Headers headerbuild = Headers.of(headers);
      Request request = new Request.Builder().url(urladdr).post(body).headers(headerbuild).build();

      try (Response response = getUnsafeOkHttpClient().newCall(request).execute()) {
        String msg =  response.body().string();
        if (log.isTraceEnabled()) {
          log.trace("<<<{}" , msg);
        }
        return new HttpResponse(msg, response.code(), response.message());
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        return new HttpResponse(null, 9999, e.getMessage());
      }
    } else {
      return new HttpResponse("{\"resultCode\":4000, \"resultMsg\":\"BAD REQUEST\"}", 4000, "");
    }
  }

  /**
   * http PUT 메소드
   * @param urladdr
   * @param headers
   * @param strJsonBody
   * @return
   * @throws Exception
   */
  public static HttpResponse put(String urladdr, Map<String,String> headers, String strJsonBody) throws Exception {
    if (log.isTraceEnabled()) {
      log.trace(">>>{}" , strJsonBody);
    }
    RequestBody body = null;
    try {
      body = RequestBody.create(JSON, strJsonBody);
    } catch (NullPointerException npe) {

      return new HttpResponse("{\"resultCode\":4105, \"resultMsg\":\"UNSUPPORTED MEDIA TYPE\"}", 4105, npe.getMessage());
    }

    if (body != null) {
      Headers headerbuild = Headers.of(headers);
      Request request = new Request.Builder().url(urladdr).put(body).headers(headerbuild).build();

      try (Response response = getClient().newCall(request).execute()) {
        String msg =  response.body().string();
        if (log.isTraceEnabled()) {
          log.trace("<<<{}" , msg);
        }
        return new HttpResponse(msg, response.code(), response.message());
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        return new HttpResponse(null, 9999, e.getMessage());
      }
    } else {
      return new HttpResponse("{\"resultCode\":4000, \"resultMsg\":\"BAD REQUEST\"}", 4000, "");
    }

  }
  /**
   * http PATCH 메소드
   * @param urladdr
   * @param headers
   * @param strJsonBody
   * @return
   * @throws Exception
   */
  public static HttpResponse patch(String urladdr, Map<String,String> headers, String strJsonBody) throws Exception {

    if (log.isTraceEnabled()) {
      log.trace(">>>{}" , strJsonBody);
    }
    RequestBody body = null;
    try {
      body = RequestBody.create(JSON, strJsonBody);
    } catch (NullPointerException npe) {
      return new HttpResponse("{\"resultCode\":4105, \"resultMsg\":\"UNSUPPORTED MEDIA TYPE\"}", 4105, npe.getMessage());
    }
    if (body != null) {
      Headers headerbuild = Headers.of(headers);
      Request request = new Request.Builder().url(urladdr).patch(body).headers(headerbuild).build();

      try (Response response = getClient().newCall(request).execute()) {
        String msg =  response.body().string();
        if (log.isTraceEnabled()) {
          log.trace("<<<{}" , msg);
        }
        return new HttpResponse(msg, response.code(), response.message());
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        return new HttpResponse(null, 9999, e.getMessage());
      }
    } else {
      return new HttpResponse("{\"resultCode\":4000, \"resultMsg\":\"BAD REQUEST\"}", 4000, "");
    }

  }


  /**
   * http DELETE 메소드 
   * @param urladdr
   * @param headers
   * @return
   * @throws Exception
   */
  public static HttpResponse delete(String urladdr, Map<String,String> headers) throws Exception {

    Headers headerbuild = Headers.of(headers);
    Request request = new Request.Builder().url(urladdr).delete().headers(headerbuild).build();

    try (Response response = getClient().newCall(request).execute()) {
      String msg =  response.body().string();
      if (log.isTraceEnabled()) {
        log.trace("<<<{}" , msg);
      }
      return new HttpResponse(msg, response.code(), response.message());
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      return new HttpResponse(null, 9999, e.getMessage());
    }
  }

  public static boolean isExist(String urladdr, Map<String,String> headers) throws Exception {
    boolean bl = false;
    try {
      HttpResponse resp = get(urladdr, headers);
      if (resp.getStatusCode() >= 200 && resp.getStatusCode() <= 300) {
        bl = true;
      } else {
        bl = false;
      }
    } catch (IOException e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return bl;
  }


} // end class
