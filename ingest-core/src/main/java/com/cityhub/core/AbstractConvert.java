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
package com.cityhub.core;

import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import com.cityhub.dto.RequestMessageVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.Operation;
import com.cityhub.utils.DataCoreCode.SocketCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractConvert implements ReflectExecuter {
  public String id = "";
  public JSONObject ConfItem = null;
  public JSONObject templateItem = null;

  public void setup(JSONObject ConfItem, JSONObject templateItem) {
    this.ConfItem = ConfItem;
    this.templateItem = templateItem;
  }

  @Override
  public String doit(Statement t1) throws CoreException {
    // legacy db
    return null;
  }

  @Override
  public String doit(byte[] t2) throws CoreException {
    // MQTT
    return null;
  }

  @Override
  public String doit() throws CoreException {
    // OPEN API
    return null;
  }

  public void log(SocketCode sc, String id) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("model_id"), sc.getCode() + ";" + sc.getMessage() + "", id, 0, ConfItem.getString("adapterType"));
  }

  public void log(SocketCode sc, String id, String msg) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("model_id"), sc.getCode() + ";" + sc.getMessage() + "-" + msg, id, 0, ConfItem.getString("adapterType"));
  }
  public void log(SocketCode sc, String id, String msg, String modelId) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.getCode() + ";" + sc.getMessage() + "-" + msg, id, 0, ConfItem.getString("adapterType"));
  }

  public void log(SocketCode sc, String id, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("model_id"), sc.getCode() + ";" + sc.getMessage() + "", id, byteBody.length, ConfItem.getString("adapterType"));
  }
  public void log(SocketCode sc, String id, byte[] byteBody, String modelId) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.getCode() + ";" + sc.getMessage() + "", id, byteBody.length, ConfItem.getString("adapterType"));
  }

  public void log(SocketCode sc, String id, String msg, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), ConfItem.getString("model_id"), sc.getCode() + ";" + sc.getMessage() + "-" + msg, id, byteBody.length,
        ConfItem.getString("adapterType"));
  }

  public void logger(SocketCode sc, String modelId, String id, byte[] byteBody) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.getCode() + ";" + sc.getMessage() + "", id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
  }
  public void logger(SocketCode sc, String modelId, String id, byte[] byteBody, String msg) {
    log.info("`{}`{}`{}`{}`{}`{}`{}", ConfItem.getString("sourceName"), modelId, sc.getCode() + ";" + sc.getMessage() + "-" + msg, id, byteBody.length, ConfItem.getString("adapterType"), ConfItem.getString("invokeClass"));
  }


  private final String REQUEST_MESSAGE_TO_SEPARATOR = "/";
  private String regex = "([a-z])([A-Z]+)";
  private String replacement = "$1_$2";
  private final String CONTENT_TYPE_SEPARATOR = ";";
  private final String CONTENT_TYPE_ENTITY_TYPE_KEY = "type";
  private final String TYPE_SEPARATOR = "=";
  private final String RESOURCE_ID_SEPARATOR = ":";
  private final int RESOURCE_TYPE_INDEX = 2;
  protected SimpleDateFormat transFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);

  public String extractId(String to) {
    if (to != null) {
      String[] toArr = to.split(REQUEST_MESSAGE_TO_SEPARATOR);
      if (toArr != null && toArr.length > 1) {
        return toArr[toArr.length - 1];
      }
    }

    return null;
  }

  public String toUnderScore(String camelStr) {
    return camelStr.replaceAll(regex, replacement).toLowerCase();
  }

  public String extractEntityType(RequestMessageVO requestMessageVO) {
    String entityType = null;
    if (requestMessageVO.getOperation() == Operation.CREATE || requestMessageVO.getOperation() == Operation.PARTIAL_UPSERT || requestMessageVO.getOperation() == Operation.FULL_UPSERT) {

      String contentType = requestMessageVO.getContentType();
      if (contentType != null) {
        String[] contentTypeStrArr = contentType.split(CONTENT_TYPE_SEPARATOR);
        if (contentTypeStrArr != null) {
          for (String contentTypeStr : contentTypeStrArr) {
            if (contentTypeStr != null) {
              if (contentTypeStr.toLowerCase().startsWith(CONTENT_TYPE_ENTITY_TYPE_KEY)) {
                String[] entityTypeStrArr = contentTypeStr.split(TYPE_SEPARATOR);
                if (entityTypeStrArr != null && entityTypeStrArr.length == 2) {
                  entityType = contentTypeStr.split(TYPE_SEPARATOR)[1];
                }
              }
            }
          }
        }
      }
    }
    if (entityType == null) {
      String[] resourceIdArr = requestMessageVO.getTo().split(RESOURCE_ID_SEPARATOR);
      if (resourceIdArr != null && resourceIdArr.length > RESOURCE_TYPE_INDEX) {
        entityType = resourceIdArr[RESOURCE_TYPE_INDEX];
      }
    }
    return entityType;
  }

} // end of class
