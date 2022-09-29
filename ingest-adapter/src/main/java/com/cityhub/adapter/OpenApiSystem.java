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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.json.JSONObject;

import com.cityhub.environment.DefaultConstants;
import com.cityhub.model.DataModelEx;
import com.cityhub.source.core.ReflectNormalSystem;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenApiSystem extends AbstractSource implements PollableSource, Configurable {


  protected CounterGroup counterGroup;
  private String invokeClass;
  private String modelId;

  private JSONObject configInfo;
  private String[] ArrModel = null;
  private String adapterType;
  private int connTerm;


  @Override
  public void configure(Context context) {
    if (counterGroup == null) {
      counterGroup = new CounterGroup();
    }

    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      configInfo = new JsonUtil().getFileJsonObject(confFile);
    } else {
      configInfo = new JSONObject();
    }
    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "http://localhost:8888/logToDbApi");
    configInfo.put("daemonServerLogApi", DAEMON_SERVER_LOGAPI);

    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");

    connTerm = context.getInteger(DefaultConstants.CONN_TERM, 600);

    configInfo.put("DATAMODEL_API_URL", context.getString("DATAMODEL_API_URL", ""));
    configInfo.put("modelId", context.getString("MODEL_ID", ""));
    configInfo.put("invokeClass", context.getString("INVOKE_CLASS", ""));
    configInfo.put("datasetId", context.getString("DATASET_ID", ""));
    configInfo.put("adapterType", context.getString("type", ""));
    configInfo.put("sourceName", this.getName());
  }

  @Override
  public void start() {
    super.start();
    log.debug("source {} started. Metrics:{}", this.getName(), counterGroup);

    JSONObject templateItem = new JSONObject();
    if (ArrModel != null) {
      for (String model : ArrModel) {
        HttpResponse resp = OkUrlUtil.get(configInfo.getString("DATAMODEL_API_URL") + "?id=" + model, "Accept", "application/json");
        log.info("schema info: {},{},{}", model, resp.getStatusCode(), configInfo.getString("DATAMODEL_API_URL") + "?id=" + model);
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

      configInfo.put("MODEL_TEMPLATE",templateItem);
    } else {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,invokeClass);
    }

  }

  public void processing() {
    log.info("Processing - {},{}", this.getName(), modelId);
    try {
      if (ArrModel != null) {
        ReflectNormalSystem reflectExecuter = null;
        try {
          Class<?> clz = Class.forName(configInfo.get("invokeClass").toString());
          reflectExecuter  = (ReflectNormalSystem)clz.newInstance();
          reflectExecuter.init(getChannelProcessor(), configInfo);
          reflectExecuter.doit();

        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
          log.error("Exception : "+ExceptionUtils.getStackTrace(e));
        }
      } else {
        log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType,invokeClass);
      }
    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.NORMAL_ERROR.toMessage(), "", 0, adapterType,invokeClass);
    }
  }


  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;
    try {
      long eventCounter = counterGroup.get("events.success");
      counterGroup.addAndGet("events.success", eventCounter);
      processing();
      Thread.sleep(connTerm * 1000); // second * 1000
      status = Status.READY;
    } catch (Exception e) {
      counterGroup.incrementAndGet("events.failed");
      status = Status.BACKOFF;
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return status;
  }


  @Override
  public void stop() {
    super.stop();
    log.debug("source {} stopping", this.getName());

    log.debug("source {} stopped. Metrics:{}", this.getName(), counterGroup);
  }

  @Override
  public long getBackOffSleepIncrement() {
    return 0;
  }

  @Override
  public long getMaxBackOffSleepInterval() {
    return 0;
  }


} // end of class
