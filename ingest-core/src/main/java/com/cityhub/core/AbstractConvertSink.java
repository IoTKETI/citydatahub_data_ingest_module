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
package com.cityhub.core;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.cityhub.dto.RequestMessageVO;
import com.cityhub.environment.Constants;
import com.cityhub.environment.ReflectExecuterEx;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractConvertSink<T1, T2> implements ReflectExecuterEx<T1, T2> {

  public SimpleDateFormat transFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);
  public JSONObject ConfItem = null;
  public RequestMessageVO requestMessageVO = null;
  public Map<String, Object> param = null;

  @Override
  public void setInitial(T1 t1) {
    this.requestMessageVO = (RequestMessageVO) t1;
    param = new HashMap<>();
    param.put("id", requestMessageVO.getId());
    param.put("createdAt", requestMessageVO.getEventTime());
    param.put("modifiedAt", requestMessageVO.getEventTime());
    param.put("owner", requestMessageVO.getOwner());
    param.put("operation", requestMessageVO.getOperation());

    JsonUtil jsonEx = new JsonUtil(requestMessageVO.getContent());
    param.put("location4326", jsonEx.getObj("location.value"));
    JsonUtil.objectToParam(param, (JSONObject) jsonEx.getObj("address.value"));
  }

  @Override
  public void setConfig(T2 t2) {
    this.ConfItem = (JSONObject) t2;
  }

  public void log(SocketCode sc, String id) {
    log.info("`{}`{}`{}", requestMessageVO.getEntityType(), sc.getCode() + ";" + sc.getMessage() + "", requestMessageVO.getId());
  }

  public void log(SocketCode sc, String msg, String id) {
    log.info("`{}`{}`{}", requestMessageVO.getEntityType(), sc.getCode() + ";" + sc.getMessage() + "-" + msg, requestMessageVO.getId());
  }

} // end of class
