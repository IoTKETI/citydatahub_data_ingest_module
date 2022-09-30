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

import java.util.List;
import java.util.Map;

import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.dto.LogVO;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.environment.ReflectExecuterManager;
import com.cityhub.model.DataModel;
import com.cityhub.source.core.LogWriterToDb;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrbanintegrationPlatform extends AbstractPollSource {

  private String modelId;
  private String DATAMODEL_API_URL;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;
  private String adapterType;
  private String datasetId;

  @Override
  public void setup(Context context) {
    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "http://localhost:8888/logToDbApi");
    ConfItem.put("daemonServerLogApi", DAEMON_SERVER_LOGAPI);

    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");

    adapterType = context.getString("type", "");
    DATAMODEL_API_URL = context.getString("DATAMODEL_API_URL", "");

    ConfItem.put("modelId", modelId);
    ConfItem.put("DATAMODEL_API_URL", DATAMODEL_API_URL);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("invokeClass", context.getString("invokeClass", "") );
    ConfItem.put("datasetId", context.getString("DATASET_ID", ""));

    String TEMP_VALUE = context.getString("TEMP_VALUE", "");
    String TEMP_VALUE1 = context.getString("TEMP_VALUE1", "");
    ConfItem.put("temp_value", TEMP_VALUE);
    ConfItem.put("temp_value1", TEMP_VALUE1);
  }

  @Override
  public void execFirst() {
    // 유효성 부분 JSON 가져오기

    templateItem = new JSONObject();

    if (ArrModel != null) {
      HttpResponse resp = OkUrlUtil.get(DATAMODEL_API_URL, "Content-type", "application/json");
      log.debug("DATAMODEL_API_URL connected: {},{}",modelId, resp.getStatusCode());
      if (resp.getStatusCode() == 200) {
        DataModel dm = new DataModel(new JSONArray(resp.getPayload()));
        for (String model : ArrModel) {
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createTamplate(model));
          } else {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          }
        }
      } else {
        for (String model : ArrModel) {
          templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
        }
      }

    } else {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId , SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
    }
  }


  @Override
  public void processing() {
    log.info("Processing - {},{}", this.getName(), modelId);
    try {
      if (ArrModel != null) {

        ReflectExecuter reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass() , getChannelProcessor(), ConfItem, templateItem);
        String sb = reflectExecuter.doit();
        if (sb != null && sb.lastIndexOf(",") > 0) {
          ObjectMapper objectMapper = new ObjectMapper();
          List<Map<String, Object>> entities = objectMapper.readValue(sb, new TypeReference<List<Map<String, Object>>>() {
          });
          JSONArray JSendArr = new JSONArray("[" + sb.substring(0 , sb.length() - 1) + "]");
          int cnt = 0;
          for (Map<String, Object> itm : entities) {
            int length = objectMapper.writeValueAsString(itm).getBytes().length;
            log.info("`{}`{}`{}`{}`{}`{}", this.getName(), itm.get("type"), SocketCode.DATA_SAVE_REQ.toMessage(), itm.get("id"), length, adapterType,ConfItem.getString("invokeClass"));
            StringBuilder l = new StringBuilder();
            l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            l.append("`").append(ConfItem.getString("sourceName"));
            l.append("`").append(modelId);
            l.append("`").append(SocketCode.DATA_SAVE_REQ.toMessage());
            l.append("`").append(itm.get("id") + "");
            l.append("`").append(length);
            l.append("`").append(adapterType);
            l.append("`").append(ConfItem.getString("invokeClass"));
            LogVO logVo = new LogVO();
            logVo.setSourceName(ConfItem.getString("sourceName"));
            logVo.setPayload(l.toString());
            logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            logVo.setType(modelId);
            logVo.setStep(SocketCode.DATA_SAVE_REQ.getCode());
            logVo.setDesc(SocketCode.DATA_SAVE_REQ.getMessage());
            logVo.setId(itm.get("id") + "");
            logVo.setLength(String.valueOf(length));
            logVo.setAdapterType(ConfItem.getString("invokeClass"));
            LogWriterToDb.logToDaemonApi(ConfItem, logVo);
          }
          sendEventEx(entities,datasetId);
        }

      } else {
        log.error("`{}`{}`{}`{}`{}`{}`{}",this.getName(), modelId , SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
      }
    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}`{}",this.getName(), modelId , SocketCode.NORMAL_ERROR.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
    }
  }



} // end of class
