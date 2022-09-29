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

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.dto.LogVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.environment.ReflectExecuterManager;
import com.cityhub.model.DataModelEx;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.LogWriterToDb;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenApiSource extends AbstractPollSource {

  private String modelId;
  private String datasetId;
  private String schemaSrv;
  private JSONObject templateItem;
  private JSONObject configInfo;
  private String[] ArrModel = null;
  private String adapterType;
  private ObjectMapper objectMapper;

  @Override
  public void setup(Context context) {
    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      configInfo = new JsonUtil().getFileJsonObject(confFile);
    } else {
      configInfo = new JSONObject();
    }
    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "http://localhost:8888/logToDbApi");
    configInfo.put("daemonServerLogApi", DAEMON_SERVER_LOGAPI);

    adapterType = context.getString("type", "");

    schemaSrv = context.getString("DATAMODEL_API_URL", "");

    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");
    datasetId = context.getString("DATASET_ID", "");

    configInfo.put("modelId", modelId);
    configInfo.put("model_id", modelId);
    configInfo.put("datasetId", datasetId);
    configInfo.put("schemaSrv", schemaSrv);
    configInfo.put("sourceName", this.getName());
    configInfo.put("adapterType", adapterType);
    configInfo.put("invokeClass", getInvokeClass());

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
        HttpResponse resp = OkUrlUtil.get(schemaSrv + "?id=" + model, "Accept", "application/json");
        log.info("schema info: {},{},{}", model, resp.getStatusCode(), schemaSrv + "?id=" + model);
        if (resp.getStatusCode() == 200) {
          DataModelEx dm = new DataModelEx(resp.getPayload());
          if (dm.hasModelId(model)) {
            templateItem.put(model, dm.createModel(model));
            log.info("schema server: {},{}", model, templateItem);
          } else {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          }
        } else {
          templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
        }
      }
    } else {
      log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId, getStr(SocketCode.DATA_NOT_EXIST_MODEL), "", 0, adapterType);
    }

    if (log.isDebugEnabled()) {
      log.debug("Template : {},{}", modelId, templateItem);
    }
  }

  @Override
  public void processing() {
    log.info("Processing - {},{}", this.getName(), modelId);
    try {
      if (ArrModel != null) {

        ReflectExecuter reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass(), configInfo, templateItem);
        String sb = reflectExecuter.doit();
        if (sb != null && !"".equals(sb)) {
          List<Map<String, Object>> entities = objectMapper.readValue(sb, new TypeReference<List<Map<String, Object>>>() {
          });
          for (Map<String, Object> itm : entities) {
            int length = objectMapper.writeValueAsString(itm).getBytes().length;
            log.info("`{}`{}`{}`{}`{}`{}", this.getName(), itm.get("type"), getStr(SocketCode.DATA_SAVE_REQ), itm.get("id"), length, adapterType);
            StringBuilder l = new StringBuilder();
            l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            l.append("`").append(configInfo.getString("sourceName"));
            l.append("`").append(modelId);
            l.append("`").append(SocketCode.DATA_SAVE_REQ.getCode() + ";" + SocketCode.DATA_SAVE_REQ.getMessage());
            l.append("`").append(itm.get("id") + "");
            l.append("`").append(length);
            l.append("`").append(adapterType);
            l.append("`").append(configInfo.getString("invokeClass"));
            LogVO logVo = new LogVO();
            logVo.setSourceName(configInfo.getString("sourceName"));
            logVo.setPayload(l.toString());
            logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            logVo.setType(modelId);
            logVo.setStep(SocketCode.DATA_SAVE_REQ.getCode());
            logVo.setDesc(SocketCode.DATA_SAVE_REQ.getMessage());
            logVo.setId(itm.get("id") + "");
            logVo.setLength(String.valueOf(length));
            logVo.setAdapterType(configInfo.getString("invokeClass"));
            LogWriterToDb.logToDaemonApi(configInfo, logVo);

          }

          sendEventEx(entities);
          Thread.sleep(10);
        }
      } else {
        log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId, getStr(SocketCode.DATA_NOT_EXIST_MODEL), "", 0, adapterType);
      }
    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId, getStr(SocketCode.NORMAL_ERROR, e.getMessage()), "", 0, adapterType);
    }
  }

  public void sendEventEx(List<Map<String, Object>> entities) {
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("entities", entities);
      body.put("datasetId", datasetId);
      Event event = EventBuilder.withBody(objectMapper.writeValueAsString(body).getBytes(Charset.forName("UTF-8")));
      getChannelProcessor().processEvent(event);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }

  public String getStr(SocketCode sc, String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }
} // end of class
