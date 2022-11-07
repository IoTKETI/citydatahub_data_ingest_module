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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.json.JSONObject;

import com.cityhub.core.ReflectNormalSystem;
import com.cityhub.core.ReflectNormalSystemManager;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.model.DataModelEx;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyPlatform extends AbstractSource implements PollableSource , Configurable {
  protected CounterGroup counterGroup;
  private int connTerm;

  private String modelId;
  private JSONObject templateItem;
  private JSONObject ConfItem;

  private String[] ArrModel = null;
  private String adapterType;

  @Override
  public void configure(Context context) {
    if (counterGroup == null) {
      counterGroup = new CounterGroup();
    }

    log.info("source Name:::{}", this.getName());

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
    connTerm = context.getInteger(DefaultConstants.CONN_TERM, 600);


    ConfItem.put("DATAMODEL_API_URL", context.getString("DATAMODEL_API_URL", ""));
    ConfItem.put("username", context.getString("DB_USERNAME", ""));
    ConfItem.put("password", context.getString("DB_PASSWORD", ""));
    ConfItem.put("driverClassName", context.getString("DB_DRIVER_CLASS_NAME", ""));
    ConfItem.put("jdbcUrl", context.getString("DB_JDBC_URL", ""));

    ConfItem.put("invokeClass", context.getString("INVOKE_CLASS", ""));
    ConfItem.put("modelId", context.getString("MODEL_ID", ""));
    ConfItem.put("datasetId", context.getString("DATASET_ID", ""));
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("sourceName", this.getName());
  }

  @Override
  public void start() {
    super.start();
    log.debug("source {} started. Metrics:{}", this.getName(), counterGroup);

    try {
      templateItem = new JSONObject();
      if (ArrModel != null) {
        for (String model : ArrModel) {
          HttpResponse resp = OkUrlUtil.get(ConfItem.getString("DATAMODEL_API_URL") + "?id=" + model, "Accept", "application/json");
          log.info("DATAMODEL_API_URL info: {},{},{}", model, resp.getStatusCode(), ConfItem.getString("DATAMODEL_API_URL") + "?id=" + model);
          if (resp.getStatusCode() == 200) {
            DataModelEx dm = new DataModelEx(resp.getPayload());
            if (dm.hasModelId(model)) {
              templateItem.put(model, dm.createModel(model));
              log.info("DATAMODEL_API_URL server: {},{}", model, templateItem);
            } else {
              templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
            }
          } else {
            templateItem.put(model, new JsonUtil().getFileJsonObject("openapi/" + model + ".template"));
          }
        }
      } else {
        log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId, SocketCode.DATA_NOT_EXIST_MODEL.toMessage(), "", 0, adapterType);
      }

      ConfItem.put("MODEL_TEMPLATE",templateItem);

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }


  public void processing() {
    log.info("::::::::::::::::::{} - Processing :::::::::::::::::", this.getName());

    try (BasicDataSource dataSource = new BasicDataSource();){
      dataSource.setDriverClassName(ConfItem.getString("driverClassName"));
      dataSource.setUrl(ConfItem.getString("jdbcUrl"));
      dataSource.setUsername(ConfItem.getString("username"));
      dataSource.setPassword(ConfItem.getString("password"));

      ReflectNormalSystem reflectExecuter = ReflectNormalSystemManager.getInstance(ConfItem.getString("invokeClass"));
      reflectExecuter.init(getChannelProcessor(), ConfItem);
      String sb = reflectExecuter.doit(dataSource);
    } catch (NullPointerException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
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
