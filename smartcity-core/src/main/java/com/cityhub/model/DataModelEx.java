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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.dto.RequestMessageVO;
import com.cityhub.utils.ByteUtil;
import com.cityhub.utils.DataCoreCode.EventType;
import com.cityhub.utils.DataCoreCode.Operation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class DataModelEx {

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

  /**
   * @param jsonArray
   */
  public DataModelEx(JSONArray jsonArray) {
    this.schema = jsonArray;
  }

  /**
   * @param StrJsonArray
   */
  public DataModelEx(String StrJsonArray) {
    if (StrJsonArray.startsWith("[")) {
      this.schema = new JSONArray(StrJsonArray);
    } else {
      JSONObject jj = new JSONObject(StrJsonArray);
      this.schema = jj.getJSONArray("dataModelResponseVOs");
    }

  }

  /**
   * @param modelId
   * @return
   */
  public boolean hasModelId(String modelId) {
    boolean bl = false;
    for (Object models : schema) {
      JSONObject model = (JSONObject) models;
      if (modelId.equals(model.get("type"))) {
        bl = true;
        break;
      }
    }
    return bl;
  }

  /**
   * @param modelId
   * @param version
   * @return
   */
  public JSONObject getModel(String modelId, String version) {
    JSONObject tm = new JSONObject();
    for (Object models : schema) {
      JSONObject model = (JSONObject) models;
      if (modelId.equals(model.get("type"))  ) {
        tm = model;
        break;
      }
    }
    return tm;
  }
  /**
   * @return
   */
  public List<Map<String,String>> listOfModel(){
    List<Map<String,String>> list = new LinkedList<>();
    for (Object models : schema) {
      JSONObject model = (JSONObject) models;
      Map<String,String> item = new HashMap<>();
      item.put("modelId",model.getString("type"));
      item.put("version",model.getString("version"));
      item.put("namespace",model.getString("namespace"));
      list.add(item);
      //log.info("Model Info:{}, {}, {}, {}",model.getString("type"), model.getString("type"), model.getString("namespace"), model.getString("version"),model.getJSONArray("context"));
    }
    return list;
  }
  /**
   * @param modelId
   * @param version
   * @return
   */
  public JSONObject createModel(String modelId, String version) {
    JSONObject templateItem = new JSONObject();
    if (hasModelId(modelId)) {
      JSONObject jModel = getModel(modelId, version);
      /*
      if (jModel.has("indexAttributeNames")) {
        log.info("indexAttributeNames:{}",jModel.getJSONArray("indexAttributeNames"));
      }
      */
      templateItem.put("@context", jModel.getJSONArray("context"));
      templateItem.put("type", jModel.getString("type"));
      templateItem.put("id", "");

      JSONArray ja = jModel.getJSONArray("attributes");
      for (Object items : ja) {
        JSONObject item = (JSONObject) items;

        item.getString("name");

        JSONObject subItem = new JSONObject();
        subItem.put("type", item.getString("attributeType"));
        if (item.has("hasObservedAt") && item.get("hasObservedAt") != JSONObject.NULL && item.getBoolean("hasObservedAt") == true) {
          subItem.put("observedAt", "");
        }
        if ("String".equals(item.getString("valueType")) ) {
          subItem.put("value", JSONObject.NULL);
        } else if ("GeoJson".equals(item.getString("valueType")) ) {
          JSONObject jMember = new JSONObject();
          jMember.put("type", "Point");
          jMember.put("coordinates", new JSONArray());
          subItem.put("value", jMember);
        } else if ("Object".equals(item.getString("valueType")) ) {
          if (item.has("objectMembers") ) {
            JSONObject jMember = new JSONObject();
            for(Object members : item.getJSONArray("objectMembers")) {
              JSONObject member = (JSONObject) members;
              jMember.put(member.getString("name") , getValueType(member.getString("valueType")));
            }
            subItem.put("value", jMember);
          } else {
            subItem.put("value", JSONObject.NULL);
          }
        } else if ("ArrayString".equals(item.getString("valueType"))) {
          subItem.put("value", new JSONArray());
        } else if ("Integer".equals(item.getString("valueType")) ) {
          subItem.put("value", JSONObject.NULL);
        } else if ("Double".equals(item.getString("valueType")) ) {
          subItem.put("value", JSONObject.NULL);
        } else if ("Date".equals(item.getString("valueType")) ) {
          subItem.put("value", JSONObject.NULL);
        } else if ("ArrayObject".equals(item.getString("valueType")) ) {
          if (item.has("objectMembers")) {
            JSONObject jMember = new JSONObject();
            for(Object members : item.getJSONArray("objectMembers")) {
              JSONObject member = (JSONObject) members;
              jMember.put(member.getString("name") , getValueType(member.getString("valueType")));
            }
            subItem.put("value", new JSONArray().put(jMember));
          } else {
            subItem.put("value", JSONObject.NULL);
          }
        }
        templateItem.put(item.getString("name"), subItem);
      }
    }

    //log.info("templateItem:{}",templateItem);
    return templateItem;
  }
  /**
   * @param valueType
   * @return
   */
  public Object getValueType(String valueType) {
    Object rtn = null;
    if ("Integer".equals(valueType)) {
      rtn = JSONObject.NULL;
    } else if ("Double".equals(valueType)) {
      rtn = JSONObject.NULL;
    } else if ("String".equals(valueType)) {
      rtn = "";
    } else if ("Date".equals(valueType)) {
      rtn = "";
    } else {
      rtn = "";
    }
    return rtn;
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
   * 처리된 Operation 기반으로 카프카 전달할 EventType조회
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
