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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvTagData extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {
    StringBuffer sendJson = new StringBuffer();

    try {

      JSONArray svcList = new JSONArray(ConfItem.getJSONArray("serviceList").toString());
      String model = ConfItem.getString("model_id");

      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = (JSONObject) svcList.get(i);
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc );
        System.out.println(jsonData.toString());
        log(SocketCode.DATA_RECEIVE, id, jsonData.toString().getBytes());

        JSONArray jarr = (JSONArray) jsonData.get("values");

        log(SocketCode.DATA_CONVERT_REQ, id, jsonData.toString().getBytes());
        for(int j = 0; j < jarr.length() ; j++) {
          JsonUtil js = new JsonUtil(templateItem.get(model).toString());

          modifyTemplate(js);

          Object obj = jarr.get(j);
          JSONObject tempJobj = (JSONObject) obj;
          JSONArray dataArr = tempJobj.getJSONArray("v");

          String tagName = setDataToTemplateAndReturnTagName(dataArr, js);
          setRemainDataToTemplate(iSvc, js, tagName);

          sendJson.append(js.toString() + ", ");
        } // end of for j

      } // end of for i


    } catch (CoreException e) {
      e.printStackTrace();
      log.error("0000");
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error("9999");
      log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
      throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
    }

    log(SocketCode.DATA_CONVERT_SUCCESS, id, sendJson.toString().getBytes());
    return sendJson.toString();
  } // end of doit


  private void modifyTemplate(JsonUtil js) {

    try {
      JSONObject timestamp = new JSONObject();
      timestamp.put("type", "Property");
      timestamp.put("value", "");
      js.put("timestamp", timestamp);
    } catch (JSONException e) {
      e.printStackTrace();
      log.error("111");
    }
  }


  private String setDataToTemplateAndReturnTagName(JSONArray dataArr, JsonUtil js) {

    String[] dataSequence = null;
    String tagName = "";

    try {

      dataSequence = ConfItem.getString("dataSequence").split(",");
      tagName = dataArr.getString(0);

      for(int i = 0 ; i < dataArr.length() ; i++) {

        String dataKey = dataSequence[i];

        if("value".equals(dataKey)) {

          String value = "";

          if (dataArr.get(i) != null && !"null".equals(dataArr.get(i).toString())  && !"".equals(dataArr.get(i).toString().trim()) ) {
            value = String.format("%.6f", JsonUtil.nvl(dataArr.get(i).toString() , DataType.FLOAT));
          } else {
            value = "0";
          }

            js.put(dataKey + ".value", JsonUtil.nvl(value , DataType.FLOAT));
        }else if("timestamp".equals(dataKey)){

          String timestamp = "";

          if(dataArr.get(i).toString() != null && !"null".equals(dataArr.get(i).toString()) && !"".equals(dataArr.get(i).toString().trim())) {
            timestamp = convertDateFormat(dataArr.get(i).toString());
          }else {
            timestamp = null;
          }

          js.put(dataKey + ".value", JsonUtil.nvl(timestamp , DataType.STRING));
        }else {

          String data = "";

          if(dataArr.get(i).toString() != null && !"null".equals(dataArr.get(i).toString()) && !"".equals(dataArr.get(i).toString().trim())) {

            data = dataArr.get(i).toString();
          }

          js.put(dataKey + ".value", JsonUtil.nvl(data, DataType.STRING));
        }
      }
    }catch (Exception ex) {
      ex.printStackTrace();
      log.error("222");
    }

    System.out.println(tagName);

    return tagName;
  }

  private String convertDateFormat(String inputDate) {

    String[] date = inputDate.split(" ")[0].split("-");
    String[] time = inputDate.split(" ")[1].split(":");

    LocalDate lDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
    LocalTime lTime = LocalTime.of(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));

    return getTimeInfo(lDate, lTime);
  }

  private String getTimeInfo(LocalDate date, LocalTime time) {
    LocalDateTime dt = LocalDateTime.of(date, time);
      ZoneOffset zo = ZonedDateTime.now().getOffset();
      String returnValue = OffsetDateTime.of(dt, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

      return returnValue;
  }

  private void setRemainDataToTemplate(JSONObject iSvc, JsonUtil js, String tagName) {

    try {
      js.put("id", JsonUtil.nvl(iSvc.getString("gs1Code") + tagName, DataType.STRING));

      ArrayList<Float> location = new ArrayList<Float>();
      location.add(0f);
      location.add(0f);
      js.put("location.value.coordinates", location);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }


}
// end of class