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

import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.core.ReflectNormalSystem;
import com.cityhub.core.ReflectNormalSystemManager;
import com.cityhub.model.DataModel;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UCityPlatformEventSource extends AbstractPollSource {

  private String modelId;
  private String DATAMODEL_API_URL;
  private String adapterType;
  private String EXCEL_FILE_FULL_PATH;
  private JSONObject templateItem = new JSONObject();
  private JSONObject ConfItem = new JSONObject();
  private String datasetId;

  @Override
  public void setup(Context context) {

    modelId = context.getString("MODEL_ID"); // UCityPlatformEvent
    adapterType = context.getString("type"); // com.cityhub.adapter.UCityPlatformEventSource
    DATAMODEL_API_URL = context.getString("DATAMODEL_API_URL", "");
    EXCEL_FILE_FULL_PATH = context.getString("EXCEL_FILE"); // event.xls
    ConfItem.put("EXCEL", EXCEL_FILE_FULL_PATH);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("modelId", modelId);
    ConfItem.put("DATAMODEL_API_URL", DATAMODEL_API_URL);
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("invokeClass", context.getString("invokeClass", "") );

    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "http://localhost:8888/logToDbApi");
    ConfItem.put("daemonServerLogApi", DAEMON_SERVER_LOGAPI);
    ConfItem.put("datasetId", context.getString("DATASET_ID", ""));
  }

  @Override
  public void execFirst() {
    HttpResponse resp = OkUrlUtil.get(DATAMODEL_API_URL, "content-type", "application/json");
    if (resp.getStatusCode() == 200) {
      DataModel dm = new DataModel(new JSONArray(resp.getPayload()));
      if (dm.hasModelId(modelId)) {
        templateItem = dm.createTamplate(modelId);
      } else {
        templateItem = new JsonUtil().getFileJsonObject("openapi/" + modelId + ".template");
      }

    } else {
      templateItem.put(modelId, new JsonUtil().getFileJsonObject("openapi/" + modelId + ".template"));
    }

    if (log.isDebugEnabled()) {
      log.debug("Template : {},{}", modelId, templateItem);
    }

  }

  @Override
  public void processing() {
    try {
      ReflectNormalSystem reflectExecuter = ReflectNormalSystemManager.getInstance(ConfItem.getString("invokeClass"));
      reflectExecuter.init(getChannelProcessor(), ConfItem);
      String sb = reflectExecuter.doit();

    } catch (Exception e) {
      log.info("ERROR");
    }
  }


} // end of class
