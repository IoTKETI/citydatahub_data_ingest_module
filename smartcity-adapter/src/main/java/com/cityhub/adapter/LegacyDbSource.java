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

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flume.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractPollSource;
import com.cityhub.environment.DefaultConstants;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.environment.ReflectExecuterManager;
import com.cityhub.model.DataModel;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyDbSource extends AbstractPollSource {

  private String password;
  private String username;
  private String url_addr;
  private String modelId;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;
  private String adapterType;

  @Override
  public void setup(Context context) {
    log.info("source Name:::{}", this.getName());
    url_addr = context.getString(DefaultConstants.URL_ADDR, "");
    username = context.getString("USERNAME", "");
    password = context.getString("PASSWORD", "");
    modelId = context.getString("MODEL_ID", "");
    ArrModel = StrUtil.strToArray(modelId, ",");
    adapterType = context.getString("type", "");

    String confFile = context.getString("CONF_FILE", "");
    setInvokeClass(context.getString(DefaultConstants.INVOKE_CLASS, "com.cityhub.adapter.convert.ConvPostgresql"));

    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    ConfItem.put("username", username);
    ConfItem.put("password", password);
    ConfItem.put("model_id", modelId);
    ConfItem.put("adapterType", adapterType);
  }

  @Override
  public void execFirst() {

    try {
      templateItem = new JSONObject();

      if (ArrModel != null) {
        HttpResponse resp = OkUrlUtil.get(getSchemaSrv(), "Content-type", "application/json");
        log.info("schema connected: {}", resp.getStatusCode());
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
        log.error("{} : SCHEMA MODEL-ID NOT FOUND ", modelId);
      }
      if (log.isDebugEnabled()) {
        log.debug("templateItem:{} -- {}", getName(), templateItem);
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }


  }

  @Override
  public void exit() {

  }

  @Override
  public void processing() {
    log.info("::::::::::::::::::{} - Processing :::::::::::::::::", this.getName());
    HikariDataSource  ds = null;
    Connection conn = null;
    Statement st = null;

    try {
      ds = new HikariDataSource();
      ds.setJdbcUrl(url_addr);
      ds.setUsername(username);
      ds.setPassword(password);

      conn = ds.getConnection();
      conn.setAutoCommit(true);
      st = conn.createStatement();

      ReflectExecuter reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass() ,  ConfItem, templateItem);
      String sb = reflectExecuter.doit(st);

      if (sb != null && sb.lastIndexOf(",") > 0) {
        JSONArray JSendArr = new JSONArray("[" + sb.substring(0 , sb.length() - 1) + "]");
        for (Object itm : JSendArr) {
          JSONObject jo = (JSONObject)itm;
          log.info("`{}`{}`{}`{}`{}`{}",this.getName() ,jo.getString("type"), getStr(SocketCode.DATA_SAVE_REQ) , jo.getString("id"), jo.toString().getBytes().length, adapterType);
          sendEvent(createSendJson(jo));
          Thread.sleep(10);
        }
      }


      if (st != null) {
        st.close();
      }
      if (conn != null) {
        conn.close();
      }
      if (ds != null) {
        ds.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }

  }


  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }
  public String getStr(SocketCode sc,String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }

} // end of class
