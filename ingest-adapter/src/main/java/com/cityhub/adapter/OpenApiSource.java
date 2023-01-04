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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.flume.Context;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.core.ReflectNormalSystem;
import com.cityhub.core.ReflectNormalSystemManager;
import com.cityhub.environment.Constants;
import com.cityhub.model.DataModelEx;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenApiSource extends AbstractPollSource {

  private String modelId;
  private String datasetId;
  private String DATAMODEL_API_URL;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;
  private String adapterType;
  private ObjectMapper objectMapper;
  private String invokeClass;

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

    adapterType = context.getString("type", "");

    DATAMODEL_API_URL = context.getString("DATAMODEL_API_URL", "");

    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");
    datasetId = context.getString("DATASET_ID", "");
    invokeClass = context.getString("INVOKE_CLASS", "");

    ConfItem.put("modelId", modelId);
    ConfItem.put("datasetId", datasetId);
    ConfItem.put("DATAMODEL_API_URL", DATAMODEL_API_URL);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("invokeClass", getInvokeClass());

    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE));
  }

  @Override
  public void execFirst() {
    templateItem = new JSONObject();
    if (ArrModel != null) {
      for (String model : ArrModel) {
        HttpResponse resp = OkUrlUtil.get(DATAMODEL_API_URL + "?id=" + model, "Accept", "application/json");
        log.info("DATAMODEL_API_URL info: {},{},{}", model, resp.getStatusCode(), DATAMODEL_API_URL + "?id=" + model);
        if (resp.getStatusCode() == 200) {
          DataModelEx dm = new DataModelEx(resp.getPayload());
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createModel(model));
            log.info("MODEL INFO: {},{}", model, templateItem);
          } else {
            log.info("HAS NOT MODEL : {}", model);
            if (exists(model)) {
              templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
            } else {
              log.info("NOT FOUND TEPLATE FILE: {},{}", model);
            }
          }
        } else {
          if (exists(model)) {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          } else {
            log.info("NOT FOUND TEPLATE FILE: {},{}", model);
          }
        }
      }
    } else {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
    }

    ConfItem.put("MODEL_TEMPLATE",templateItem);
  }
  private boolean exists(String model) {
    String templatePath = new CommonUtil().getJarPath();
    File file = new File(templatePath + "openapi/" + model + ".template");
    return file.exists();
  }
  private boolean hasModel() {
    boolean hasModel = false;
    for (String model : ArrModel) {
      JSONObject templateItem = ConfItem.getJSONObject("MODEL_TEMPLATE");
      if (templateItem.has(model)) {
        hasModel = true;
      }
    }
    return hasModel;
  }
  @Override
  public void processing() {
    log.info("Processing - {},{}", this.getName(), modelId);
    try {
      if (ArrModel != null && hasModel()) {
        ReflectNormalSystem reflectNormalSystem = ReflectNormalSystemManager.getInstance(getInvokeClass());
        reflectNormalSystem.init(getChannelProcessor(), ConfItem);
        String sb = reflectNormalSystem.doit();
      } else {
        log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
      }
    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.NORMAL_ERROR.toMessage(), "", 0, adapterType,ConfItem.getString("invokeClass"));
    }
  }

} // end of class
