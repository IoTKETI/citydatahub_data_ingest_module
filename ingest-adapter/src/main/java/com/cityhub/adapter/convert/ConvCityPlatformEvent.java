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
package com.cityhub.adapter.convert;

import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.JsonUtil;

public class ConvCityPlatformEvent  extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit(Statement st) throws CoreException {

        StringBuffer sendJson = new StringBuffer();

        try{
              JSONArray svcList = ConfItem.getJSONArray("serviceList");

              for(int i=0; i<svcList.length(); i++) {
                JSONObject iSvc = svcList.getJSONObject(i);

                //id = iSvc.getString("gs1Code");
                //confItem에서 "gs1Code" 값 호출

                JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
                //Source.java파일에서 설정한 "model_id" 값 호출 - templateItem에 만들어진 양식 호출

                JsonUtil jsonEx = new JsonUtil(jTemplate);
              }

        } catch (CoreException e) {
              if ("!C0099".equals(e.getErrorCode())) {
                log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
              }
        } catch (Exception e) {
              log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
              throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
        }

            return sendJson.toString();
  }






} // end of class
