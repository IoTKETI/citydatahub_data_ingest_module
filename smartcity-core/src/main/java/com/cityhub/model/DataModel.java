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
package com.cityhub.model;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.dto.RequestMessageVO;
import com.cityhub.dto.ResultObject;
import com.cityhub.utils.ByteUtil;
import com.cityhub.utils.DataCoreCode.EventType;
import com.cityhub.utils.DataCoreCode.Operation;
import com.cityhub.utils.DataType;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DataModel {

  private static final String REQUEST_MESSAGE_TO_SEPARATOR = "/";
  private static final String regex = "([a-z])([A-Z]+)";
  private static final String replacement = "$1_$2";
  private static final String CONTENT_TYPE_SEPARATOR = ";";
  private static final String CONTENT_TYPE_ENTITY_TYPE_KEY = "type";
  private static final String TYPE_SEPARATOR = "=";
  private static final String RESOURCE_ID_SEPARATOR = ":";
  private static final int RESOURCE_TYPE_INDEX = 2;
  private static final int HEADER_LENGTH = 5;
  private static final byte DEFAULT_KAFKA_MESSAGE_MAIN_VERSION = 0X01;
  private static final byte DEFAULT_KAFKA_MESSAGE_SUB_VERSION = 0X00;

  JSONArray schema;

  public DataModel(JSONArray schema) {
    this.schema = schema;
  }

  /**
   * @param Id
   * @return
   */
  public boolean hasModelId(String Id) {
    boolean bl = false;
    for (Object schem : schema) {
      JSONObject sche = (JSONObject) schem;
      if (Id.equals(sche.get("id"))) {
        bl = true;
        break;
      }
    }
    return bl;
  }

  /**
   * @param Id
   * @return
   */
  public JSONObject getModelId(String Id) {
    JSONObject tm = new JSONObject();
    for (Object schem : schema) {
      JSONObject sche = (JSONObject) schem;
      if (Id.equals(sche.get("id"))) {
        tm = sche;
        break;
      }
    }
    return tm;
  }

  /**
   * @return
   */
  public JSONObject getBasicJson() {
    JSONObject tmp = new JSONObject();
    tmp.put("type", "Property");
    tmp.put("value", JSONObject.NULL);
    return tmp;
  }

  /**
   * @param modelId
   * @return
   */
  public JSONObject createTamplate(String modelId) {
    JSONObject tplate = new JSONObject();
    JSONObject model = getModelId(modelId);
    for (Object vItem : model.getJSONArray("attributes")) {
      JSONObject vc = (JSONObject) vItem;

      if ("id".equals(vc.getString("id"))) {
        tplate.put(vc.getString("id"), "");
      } else if ("type".equals(vc.getString("id"))) {
        tplate.put(vc.getString("id"), model.getString("id"));
      } else if ("@context".equals(vc.getString("id"))) {
        JSONArray ja = new JSONArray();
        ja.put("http://uri.etsi.org/ngsi-ld/core-context.jsonld");
        ja.put("http://datahub.kr/" + model.getString("id").toLowerCase() + ".jsonld");
        tplate.put(vc.getString("id"), ja);
      } else {
        if (!vc.has("referenceDataModelId")) {
          tplate.put(vc.getString("id"), JSONObject.NULL);
        } else if (vc.has("referenceDataModelId") && !"ClassProperty".equals(vc.getString("referenceDataModelId"))) {
          tplate.put(vc.getString("id"), createReferenceDataModelId(vc.getString("referenceDataModelId")));
        } else {
          tplate.put(vc.getString("id"), createReferenceDataModelId(vc.getString("referenceDataModelId")));
        }
      } // end if

    } // end for
    return tplate;
  }

  /**
   * @param referenceDataModelId
   * @return
   */
  public JSONObject createReferenceDataModelId(String referenceDataModelId) {

    JSONObject subM = new JSONObject();

    JSONObject refModel = getModelId(referenceDataModelId);
    if (refModel.has("attributes")) {
      for (Object vI : refModel.getJSONArray("attributes")) {
        JSONObject c = (JSONObject) vI;
        if (c.has("referenceDataModelId") && !"ClassProperty".equals(c.getString("referenceDataModelId"))) {
          if (hasModelId(c.getString("referenceDataModelId"))) {
            if ("006".equals(c.getString("dataType"))) {
              subM.put(c.getString("id"), new JSONArray().put(createReferenceDataModelId(c.getString("referenceDataModelId"))));
            } else {
              subM.put(c.getString("id"), createReferenceDataModelId(c.getString("referenceDataModelId")));
            }

          } else {
            subM.put(c.getString("id"), JSONObject.NULL);
          }
        } else {
          if ("type".equals(c.getString("id"))) {
            if (c.has("possibleValues")) {
              if (c.getJSONArray("possibleValues").length() > 0 ) {
                if (!"".equals(c.getJSONArray("possibleValues").getString(0))) {
                  subM.put(c.getString("id"), c.getJSONArray("possibleValues").getString(0));
                } else {
                  subM.put(c.getString("id"), "Property");
                }
              } else {
                subM.put(c.getString("id"), "Property");
              }
            } else {
              subM.put(c.getString("id"), "Property");
            }
          } else {
            subM.put(c.getString("id"), JSONObject.NULL);
            /*
             * if (c.has("dataType") && ("004".equals(c.getString("dataType")) ||
             * "005".equals(c.getString("dataType")) ||
             * "006".equals(c.getString("dataType")) ||
             * "007".equals(c.getString("dataType")))) {
             *
             *
             * } else if (c.has("dataType") && ("003".equals(c.getString("dataType")))) {
             * subM.put(c.getString("id"), getBasicJson());
             *
             * } else { subM.put(c.getString("id"), JSONObject.NULL); }
             */
          }
        }
      }
    }
    return subM;
  }

  /**
   * dataType values 000 INTEGER 001 STRING 002 DOUBLE 003 CLASS 004 INTEGERARRAY
   * 005 STRINGARRAY 006 CLASSARRAY 007 DOUBLEARRAY 008 DATETIME
   */
  @SuppressWarnings("rawtypes")
  public ResultObject checkValidation(JSONObject val, String modelId) {

    return new ResultObject(2000, "success");
  }

  /**
   * @param val
   * @param dt
   * @return
   */
  public static Object conv(Object val, DataType dt) {
    if (val != null) {
      if (DataType.SHORT == dt || DataType.INTEGER == dt || DataType.LONG == dt) {
        return Long.parseLong(val + "");
      } else if (DataType.FLOAT == dt || DataType.DOUBLE == dt) {
        return Double.parseDouble(val + "");
      } else if (DataType.BIGDECIMAL == dt) {
        return new BigDecimal(val + "");
      } else if (DataType.STRING == dt) {
        return String.valueOf(val);
      } else {
        return val;
      }
    }
    return "";
  }

  /**
   * @param to
   * @return
   */
  public static String extractId(String to) {
    if (to != null) {
      String[] toArr = to.split(REQUEST_MESSAGE_TO_SEPARATOR);
      if (toArr != null && toArr.length > 1) {
        return toArr[toArr.length - 1];
      }
    }
    return null;
  }

  /**
   * @param camelStr
   * @return
   */
  public static String toUnderScore(String camelStr) {
    return camelStr.replaceAll(regex, replacement).toLowerCase();
  }

  /**
   * @param requestMessageVO
   * @return
   */
  public static String extractEntityType(RequestMessageVO requestMessageVO) {
    String entityType = null;
    if (requestMessageVO.getOperation() == Operation.CREATE || requestMessageVO.getOperation() == Operation.PARTIAL_UPSERT || requestMessageVO.getOperation() == Operation.FULL_UPSERT) {

      // ContentType에서 EntityType 추출
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
      // To 에서 EntityType 추출
      String[] resourceIdArr = requestMessageVO.getTo().split(RESOURCE_ID_SEPARATOR);
      if (resourceIdArr != null && resourceIdArr.length > RESOURCE_TYPE_INDEX) {
        entityType = resourceIdArr[RESOURCE_TYPE_INDEX];
      }
    }
    return entityType;
  }

  /**
   *
   * @param processOperation 실제 처리된 Operation
   * @return 카프카 전달 EventType
   */
  public static EventType operationToEventType(Operation processOperation) {

    switch (processOperation) {
      case CREATE:
        return EventType.CREATED;
      case PARTIAL_UPDATE:
        return EventType.PARTIALLY_UPDATED;
      case FULL_UPDATE:
        return EventType.FULLY_UPDATED;
      case DELETE:
        return EventType.DELETED;
      default:
        return null;
    }
  }

  /**
   * @param bodyMessage
   * @return
   */
  public static byte[] createSendMessage(byte[] bodyMessage) {
    byte kafkaMessageVersion = ((byte) ((DEFAULT_KAFKA_MESSAGE_MAIN_VERSION << 4) | DEFAULT_KAFKA_MESSAGE_SUB_VERSION));
    byte[] bodyLength = ByteUtil.intTobytes(bodyMessage.length);

    ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_LENGTH + bodyMessage.length);
    byteBuffer.put(kafkaMessageVersion);
    byteBuffer.put(bodyLength);
    byteBuffer.put(bodyMessage);

    return byteBuffer.array();
  }

}
