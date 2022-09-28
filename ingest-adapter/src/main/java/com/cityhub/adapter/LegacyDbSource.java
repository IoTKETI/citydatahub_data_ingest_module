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
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.model.DataModelEx;
import com.cityhub.source.core.ReflectLegacySystem;
import com.cityhub.source.core.ReflectLegacySystemManager;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyDbSource extends AbstractPollSource {

  private String modelId;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;
  private String adapterType;
  private String schemaSrv;

  @Override
  public void setup(Context context) {
    log.info("source Name:::{}", this.getName());
    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");
    schemaSrv = context.getString("DATAMODEL_API_URL", "");
    String confFile = context.getString("CONF_FILE", "");
    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }

    String DAEMON_SERVER_LOGAPI = context.getString("DAEMON_SERVER_LOGAPI", "");
    if (!"".equals(DAEMON_SERVER_LOGAPI)) {
      ConfItem.put("daemonServerLogApi", context.getString("DAEMON_SERVER_LOGAPI", ""));
    } else {
      ConfItem.put("daemonServerLogApi", "http://localhost:8888/logToDbApi");
    }

    ConfItem.put("DATAMODEL_API_URL", context.getString("DATAMODEL_API_URL", ""));
    ConfItem.put("username", context.getString("DB_USERNAME", ""));
    ConfItem.put("password", context.getString("DB_PASSWORD", ""));
    ConfItem.put("driverClassName", context.getString("DB_DRIVER_CLASS_NAME", ""));
    ConfItem.put("jdbcUrl", context.getString("DB_JDBC_URL", ""));
    ConfItem.put("model_id", context.getString("MODEL_ID", ""));
    ConfItem.put("invokeClass", context.getString("INVOKE_CLASS", ""));
    ConfItem.put("adapterType", adapterType);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("datasetId", context.getString("DATASET_ID", ""));
  }

  @Override
  public void execFirst() {

    try {
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

      ConfItem.put("MODEL_TEMPLATE",templateItem);

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  @Override
  public void exit() {

  }

  @Override
  public void processing() {
    log.info("::::::::::::::::::{} - Processing :::::::::::::::::", this.getName());

    try (HikariDataSource ds = new HikariDataSource();){

      ds.setJdbcUrl(ConfItem.getString("jdbcUrl"));
      ds.setUsername(ConfItem.getString("username"));
      ds.setPassword(ConfItem.getString("password"));

      ReflectLegacySystem reflectExecuter = ReflectLegacySystemManager.getInstance(getInvokeClass());
      reflectExecuter.init(getChannelProcessor(), ConfItem);

      String sb = reflectExecuter.doit(ds);
      /*
      if (sb != null && sb.lastIndexOf(",") > 0) {
        JSONArray JSendArr = new JSONArray("[" + sb.substring(0, sb.length() - 1) + "]");
        for (Object itm : JSendArr) {
          JSONObject jo = (JSONObject) itm;
          log.info("`{}`{}`{}`{}`{}`{}", this.getName(), jo.getString("type"), getStr(SocketCode.DATA_SAVE_REQ), jo.getString("id"), jo.toString().getBytes().length, adapterType);
          //sendEventEx(createSendJson(jo));
          Thread.sleep(10);
        }
      }
       */
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }

  public String getStr(SocketCode sc, String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }

} // end of class
