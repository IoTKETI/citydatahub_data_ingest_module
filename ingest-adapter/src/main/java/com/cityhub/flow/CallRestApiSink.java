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
package com.cityhub.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CallRestApiSink extends AbstractSink implements Configurable {

  private static final Logger logger = LoggerFactory.getLogger(CallRestApiSink.class);

  // Default Max bytes to dump
  public static final int DEFAULT_MAX_BYTE_DUMP = 16;

  public static final String MAX_BYTES_DUMP_KEY = "maxBytesToLog";
  private String ingestApiUrl;
  private String ingestYn;

  @Override
  public void configure(Context context) {
    ingestApiUrl = context.getString("INGEST_API_URL", "");
    ingestYn = context.getString("INGEST_YN", "N").toUpperCase();
    String strMaxBytes = context.getString(MAX_BYTES_DUMP_KEY);
    if (!Strings.isNullOrEmpty(strMaxBytes)) {
      try {
        Integer.parseInt(strMaxBytes);
      } catch (NumberFormatException e) {
        logger.warn(String.format("Unable to convert %s to integer, using default value(%d) for maxByteToDump", strMaxBytes, DEFAULT_MAX_BYTE_DUMP));
      }
    }
    logger.info("{},{}", ingestApiUrl, ingestYn);
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status result = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();
    Event event = null;

    try {
      transaction.begin();
      event = channel.take();

      if (event != null) {
        String content = new String(event.getBody(), StandardCharsets.UTF_8);
        logger.info("Logger: " + content);
        if ("Y".equalsIgnoreCase(ingestYn)) {
          if (!"".equals(content)) {
            if (content.startsWith("{") || content.startsWith("[")) {
              String resultConnect = httpConnection(ingestApiUrl, content);
              logger.info("{}", resultConnect);
            }
          }
        }

      } else {
        // No event found, request back-off semantics from the sink runner
        result = Status.BACKOFF;
      }
      transaction.commit();

    } catch (Exception ex) {
      transaction.rollback();
      throw new EventDeliveryException("Failed to log event: " + event, ex);
    } finally {
      transaction.close();
    }

    return result;
  }

  public String httpConnection(String targetUrl, String jsonBody) {
    URL url = null;
    HttpURLConnection conn = null;
    BufferedReader br = null;
    StringBuffer sb = null;
    String returnText = "";
    String jsonData = "";
    int responseCode;

    try {
      url = new URL(targetUrl);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestMethod("POST");

      if (!"".equals(jsonBody)) {
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonBody);
        wr.flush();
      }
      responseCode = conn.getResponseCode();
      logger.debug("responseCode : {}", responseCode);

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

      logger.debug("returnText:{}", returnText);
    } catch (IOException e) {
      logger.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      try {
        br.close();
        conn.disconnect();
      } catch (Exception e2) {
        logger.error("Exception : " + ExceptionUtils.getStackTrace(e2));
      }
    }
    return returnText;
  }

}
