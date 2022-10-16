package com.cityhub.web.security.service;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.cityhub.web.security.exception.DataCoreUIException;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * Data core Rest Service
 *
 * @FileName DataCoreRestSVC.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Slf4j
@Service
public class DataCoreRestSVC {

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Get request url
   *
   * @param baseUrl Base url
   * @param path    path
   * @param params  params
   * @return URI
   */
  protected URI getUrl(String baseUrl, String path, Map<String, Object> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(path);
    if (params != null) {
      Iterator<String> iteratortor = params.keySet().iterator();
      while (iteratortor.hasNext()) {
        String key = (String) iteratortor.next();
        if (params.get(key) != null && !"".equals(params.get(key))) {
          builder.queryParam(key, params.get(key));
        }
      }
    }
    return builder.build().toUri();
  }

  /**
   * Set http header and body with request entity
   *
   * @param body    Http request body
   * @param headers Http request header
   * @return HttpEntity
   */
  protected <T> HttpEntity<T> getRequestEntity(T body, Map<String, String> headers) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    if (headers != null) {
      Iterator<String> iterator = headers.keySet().iterator();
      while (iterator.hasNext()) {
        String key = (String) iterator.next();
        httpHeaders.add(key, headers.get(key));
      }
    }
    return new HttpEntity<>(body, httpHeaders);
  }

  /**
   * Request get method
   *
   * @param moduleHost   Destination module host
   * @param pathUri      Path uri
   * @param headers      Http request header
   * @param body         Http request body
   * @param params       Http request param
   * @param responseType Response type
   * @return Result object by retrieved.
   */
  public <T> ResponseEntity<T> get(String moduleHost, String pathUri, Map<String, String> headers, Object body, Map<String, Object> params, Class<T> responseType) {
    URI uri = getUrl(moduleHost, pathUri, params);
    log.info("GET - REST API URL : {},{},{}", uri,params, body );

    ResponseEntity<T> response = null;
    try {
      response = restTemplate.exchange(uri, HttpMethod.GET, getRequestEntity(body, headers), responseType);
    } catch(HttpClientErrorException e) {
      ClientExceptionPayloadVO clientExceptionPayload = new Gson().fromJson(e.getResponseBodyAsString(), ClientExceptionPayloadVO.class);
      log.error("Client Exception - Error Status Code: {}, Response Body as String: {}, {}", e.getStatusCode(), clientExceptionPayload, e.getMessage());
      throw new DataCoreUIException(e.getStatusCode(), clientExceptionPayload);
    } catch(ResourceAccessException e) {
      log.error("Connection refused - {}", moduleHost, e);
      throw new DataCoreUIException(HttpStatus.SERVICE_UNAVAILABLE);
    } catch (RestClientException e) {
        log.error("REST GET Exception, ", e);
        throw new DataCoreUIException(HttpStatus.BAD_REQUEST);
    }

    return response;
  }

  /**
   * Request post method
   *
   * @param moduleHost   Destination module host
   * @param pathUri      Path uri
   * @param headers      Http request header
   * @param body         Http request body
   * @param params       Http request param
   * @param responseType Response type
   * @return A response object to a POST request.
   */
  @SuppressWarnings("unchecked")
  public <T> ResponseEntity<T> post(String moduleHost, String pathUri, Map<String, String> headers, Object body, Map<String, Object> params, Class<T> responseType) {
    URI uri = getUrl(moduleHost, pathUri, params);
    log.info("POST - REST API URL : {},{},{}", uri,params, body );

    ResponseEntity<T> response = null;
    try {
      response = restTemplate.exchange(uri, HttpMethod.POST, getRequestEntity(body, headers), responseType);
    } catch (HttpClientErrorException e) {
      ClientExceptionPayloadVO clientExceptionPayload = new Gson().fromJson(e.getResponseBodyAsString(), ClientExceptionPayloadVO.class);
      log.error("Client Exception - Error Status Code: {}, Response Body as String: {}, {}", e.getStatusCode(), clientExceptionPayload, e);
      throw new DataCoreUIException(e.getStatusCode(), clientExceptionPayload);
    } catch (ResourceAccessException e) {
      log.error("Connection refused - {}", moduleHost, e);
      throw new DataCoreUIException(HttpStatus.SERVICE_UNAVAILABLE);
    } catch (RestClientException e) {
      log.error("REST POST Exception, ", e);
      throw new DataCoreUIException(HttpStatus.BAD_REQUEST);
    }

    return response;
  }

}
