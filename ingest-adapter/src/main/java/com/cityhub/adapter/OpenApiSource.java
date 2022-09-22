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
import com.cityhub.dto.LogVO;
import com.cityhub.environment.ReflectExecuter;
import com.cityhub.environment.ReflectExecuterManager;
import com.cityhub.model.DataModel;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.HttpResponse;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.LogWriterToDb;
import com.cityhub.utils.OkUrlUtil;
import com.cityhub.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenApiSource extends AbstractPollSource {

  private String modelId;
  private String schemaSrv;
  private JSONObject templateItem;
  private JSONObject ConfItem;
  private String[] ArrModel = null;
  private String adapterType;

  @Override
  public void setup(Context context) {
    modelId = context.getString("MODEL_ID", "");
    String confFile = context.getString("CONF_FILE", "");
    String TEMP_VALUE = context.getString("TEMP_VALUE", "");
    String TEMP_VALUE1 = context.getString("TEMP_VALUE1", "");
    adapterType = context.getString("type", "");

    schemaSrv = super.getSchemaSrv();
    ArrModel = StrUtil.strToArray(modelId, ",");

    if (!"".equals(confFile)) {
      ConfItem = new JsonUtil().getFileJsonObject(confFile);
    } else {
      ConfItem = new JSONObject();
    }
    if (getInit().has("daemonSrverLogApi") ) {
      ConfItem.put("daemonSrverLogApi", getInit().getString("daemonSrverLogApi"));
    } else {
      ConfItem.put("daemonSrverLogApi", "http://localhost:8888/logToDbApi");
    }

    ConfItem.put("model_id", modelId);
    ConfItem.put("schema_srv", schemaSrv);
    ConfItem.put("sourceName", this.getName());
    ConfItem.put("temp_value", TEMP_VALUE);
    ConfItem.put("temp_value1", TEMP_VALUE1);
    ConfItem.put("adapterType", adapterType);
  }

  @Override
  public void execFirst() {
    // 유효성 부분 JSON 가져오기

    templateItem = new JSONObject();

    if (ArrModel != null) {
      HttpResponse resp = OkUrlUtil.get(schemaSrv, "Content-type", "application/json");
      log.debug("schema connected: {},{}",modelId, resp.getStatusCode());
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
      log.error("`{}`{}`{}`{}`{}`{}", this.getName(), modelId , getStr(SocketCode.DATA_NOT_EXIST_MODEL), "", 0, adapterType);
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

        ReflectExecuter reflectExecuter = ReflectExecuterManager.getInstance(getInvokeClass() ,  ConfItem, templateItem);
        String sb = reflectExecuter.doit();
        if (sb != null && sb.lastIndexOf(",") > 0) {
          JSONArray JSendArr = new JSONArray("[" + sb.substring(0 , sb.length() - 1) + "]");
          int cnt = 0;
          for (Object itm : JSendArr) {
            JSONObject jo = (JSONObject)itm;
            // 최소한의 검증 처리 (필수값 체크)
            log.info("`{}`{}`{}`{}`{}`{}",this.getName() ,jo.getString("type"), getStr(SocketCode.DATA_SAVE_REQ) , jo.getString("id"), jo.toString().getBytes().length, adapterType);
            StringBuilder l =  new StringBuilder();
            l.append(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            l.append("`").append(ConfItem.getString("sourceName"));
            l.append("`").append(modelId);
            l.append("`").append(SocketCode.DATA_SAVE_REQ.getCode() + ";" + SocketCode.DATA_SAVE_REQ.getMessage());
            l.append("`").append(jo.getString("id"));
            l.append("`").append(jo.toString().getBytes().length);
            l.append("`").append(adapterType);
            l.append("`").append(ConfItem.getString("invokeClass"));
            LogVO logVo = new LogVO();
            logVo.setSourceName(ConfItem.getString("sourceName"));
            logVo.setPayload(l.toString());
            logVo.setTimestamp(DateUtil.getDate("yyyy-MM-dd HH:mm:ss.SSS"));
            logVo.setType(modelId);
            logVo.setStep(SocketCode.DATA_SAVE_REQ.getCode());
            logVo.setDesc(SocketCode.DATA_SAVE_REQ.getMessage());
            logVo.setId(jo.getString("id"));
            logVo.setLength(String.valueOf(jo.toString().getBytes().length));
            logVo.setAdapterType(ConfItem.getString("invokeClass"));
            LogWriterToDb.logToDaemonApi(ConfItem,logVo);
            cnt++;
            byte[] cont = createSendJson(jo);

            sendEvent(cont);

            Thread.sleep(10);
          }
        }

      } else {
        log.error("`{}`{}`{}`{}`{}`{}",this.getName(), modelId , getStr(SocketCode.DATA_NOT_EXIST_MODEL), "", 0, adapterType);
      }
    } catch (Exception e) {
      log.error("`{}`{}`{}`{}`{}`{}",this.getName(), modelId , getStr(SocketCode.NORMAL_ERROR, e.getMessage()), "", 0, adapterType);
    }
  }

  public String getStr(SocketCode sc) {
    return sc.getCode() + ";" + sc.getMessage();
  }
  public String getStr(SocketCode sc,String msg) {
    return sc.getCode() + ";" + sc.getMessage() + "-" + msg;
  }
} // end of class
