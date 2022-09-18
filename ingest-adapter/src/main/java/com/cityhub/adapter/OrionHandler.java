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
package com.cityhub.adapter;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.http.MethodNotSupportedException;
import org.json.JSONArray;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrionHandler implements HTTPSourceHandler {

  public static final String HEADER_NAME_CORRELATOR_ID  = "fiware-correlator";
  public static final String HEADER_NAME_CONTENT_TYPE   = "content-type";
  public static final String HEADER_VALUE_CONTENT_TYPE  = "application/json; charset=utf-8";
  public static final String HEADER_NAME_SERVICE        = "fiware-service";
  public static final String HEADER_NAME_SERVICE_PATH   = "fiware-servicepath";
  public static final String HEADER_NAME_TRANSACTION_ID = "transaction-id";

  public static final int HEADER_SERVICE_MAX_LEN      = 50;
  public static final int HEADER_SERVICE_PATH_MAX_LEN = 50;

  public String DEFAULT_SERVICE;
  public String DEFAULT_SERVICE_PATH;

  @Override
  public void configure(Context context) {
    DEFAULT_SERVICE       = context.getString("DEFAULT_SERVICE", "default_service");
    DEFAULT_SERVICE_PATH  = context.getString("DEFAULT_SERVICE_PATH", "default_service_path");
  }

  @Override
  public List<Event> getEvents(HttpServletRequest request) throws Exception {

    String corrId = null;
    String contentType = null;
    String service = DEFAULT_SERVICE;
    String servicePath = DEFAULT_SERVICE_PATH;

    Enumeration<?> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = ((String) headerNames.nextElement()).toLowerCase(Locale.ENGLISH);
      String value = request.getHeader(key);

      switch (key) {
        case HEADER_NAME_CORRELATOR_ID:
          corrId = value;
          break;

        case HEADER_NAME_CONTENT_TYPE:
          if (!value.toLowerCase(Locale.ENGLISH).contains(HEADER_VALUE_CONTENT_TYPE)) {
            throw new HTTPBadRequestException(value + " content type not supported");
          } else {
            contentType = value;
          }
          break;

        case HEADER_NAME_SERVICE:
          if (value.length() > HEADER_SERVICE_MAX_LEN) {
            throw new HTTPBadRequestException(
                    "'" + HEADER_NAME_SERVICE + "' header length greater than " + HEADER_SERVICE_MAX_LEN + ")");
          } else {
            service = value;
          }
          break;

        case HEADER_NAME_SERVICE_PATH:
          String[] splitValues = value.split(",");

          for (String splitValue : splitValues) {
            if (splitValue.length() > HEADER_SERVICE_PATH_MAX_LEN) {
              throw new HTTPBadRequestException(
                      "'" + HEADER_NAME_SERVICE_PATH + "' header length greater than " + HEADER_SERVICE_PATH_MAX_LEN + ")");
            } else if (!splitValue.startsWith("/")) {
              throw new HTTPBadRequestException(
                      "'" + HEADER_NAME_SERVICE_PATH + "' header value must start with '/'");
            }
          }

          servicePath = value;
          break;

        default:
          if (log.isInfoEnabled()) {
            log.info("OrionHandler Unnecessary header");
          }
          break;
      }
    }

    String method = request.getMethod().toUpperCase(Locale.ENGLISH);
    if (!method.equals("POST")) {
      throw new MethodNotSupportedException(method + " method not supported");
    }

    if (contentType == null) {
      throw new HTTPBadRequestException("Missing content type. Required 'application/json; charset=utf-8'");
    }

    StringBuilder sb = new StringBuilder();
    String line;

    try (BufferedReader reader = request.getReader()) {
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }

    String transId = generateUniqueId(null, null);
    corrId = generateUniqueId(corrId, transId);

    if (sb.length() == 0) {
      throw new HTTPBadRequestException("No content in the request");
    }

    String[] servicePaths = servicePath.split(",");

    JSONObject js = new JSONObject(sb.toString());
    JSONArray list = (JSONArray) js.get("data");

    if (servicePaths.length != list.length()) {
      throw new HTTPBadRequestException(
              "'" + HEADER_NAME_SERVICE_PATH + "' header value does not match the number of notified context responses");
    }

    List<Event> eventList = new ArrayList<Event>();

    for (int i = 0; i < list.length(); i++) {
      JSONObject item = list.getJSONObject(i);

      Map<String, String> headers = new HashMap<>();
      headers.put(HEADER_NAME_SERVICE, service);
      headers.put(HEADER_NAME_SERVICE_PATH, servicePaths[i]);
      headers.put(HEADER_NAME_CORRELATOR_ID, corrId);
      headers.put(HEADER_NAME_TRANSACTION_ID, transId);

      Event event = EventBuilder.withBody(item.toString().getBytes(), headers);
      eventList.add(event);
    }

    return eventList;
  }

  public static String generateUniqueId(String notifiedId, String transactionId) {
    if (notifiedId != null) {
      return notifiedId;
    } else if (transactionId != null) {
      return transactionId;
    } else {
      return UUID.randomUUID().toString();
    }
  }

} // end class
