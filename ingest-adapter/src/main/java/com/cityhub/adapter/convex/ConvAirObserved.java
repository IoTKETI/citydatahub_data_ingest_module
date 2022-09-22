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
package com.cityhub.adapter.convex;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.GradeType;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvAirObserved extends AbstractConvert {
  private ObjectMapper objectMapper;
  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    this.objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
    this.objectMapper.setTimeZone(TimeZone.getTimeZone(Constants.CONTENT_DATE_TIMEZONE ));
  }

  @Override
  public String doit() throws CoreException {
    List<Map<String,Object>> rtnList = new LinkedList<>();
    String rtnStr = "";
    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0 ; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        JSONObject jsonData = (JSONObject) CommonUtil.getData(iSvc );


        Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
        JsonUtil ju = new JsonUtil(jsonData);
        if (!ju.has("response.body.items")) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, jsonData.toString().getBytes());

          JSONArray jarr = ju.getArray("response.body.items");;
          if (jarr.length() > 0) {
            JSONObject item = jarr.getJSONObject(0);
            {
              log.info("{}",item.toString());
              Map<String,Object> addrValue = (Map)((Map)tMap.get("address")).get("value");
              addrValue.put("addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")) );
              addrValue.put("addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")) );
              addrValue.put("addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")) );
              addrValue.put("addressTown", JsonUtil.nvl(iSvc.getString("addressTown")) );
              addrValue.put("streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")) );

              Map<String,Object> locMap = (Map)tMap.get("location");
              locMap.put("observedAt",DateUtil.getTime());
              Map<String,Object> locValueMap  = (Map)locMap.get("value");
              locValueMap.put("coordinates", iSvc.getJSONArray("location").toList());

              tMap.put("id", iSvc.getString("gs1Code"));

              Map<String,Object> observation = new LinkedHashMap<>();
              try {
                if (item.get("so2Value") != JSONObject.NULL && !"-".equals(item.getString("so2Value")) ) {
                  observation.put("so2", item.optDouble("so2Value", 0.0d));
                }
                if (item.get("coValue") != JSONObject.NULL && !"-".equals(item.getString("coValue")) ) {
                  observation.put("co", item.optDouble("coValue", 0.0d));
                }
                if (item.get("o3Value") != JSONObject.NULL && !"-".equals(item.getString("o3Value")) ) {
                  observation.put("o3", item.optDouble("o3Value", 0.0d));
                }
                if (item.get("no2Value") != JSONObject.NULL && !"-".equals(item.getString("no2Value")) ) {
                  observation.put("no2", item.optDouble("no2Value", 0.0d));
                }
                if (item.get("pm10Value") != JSONObject.NULL && !"-".equals(item.getString("pm10Value")) ) {
                  observation.put("pm10", item.optInt("pm10Value", 0));
                }
                if (item.get("pm25Value") != JSONObject.NULL && !"-".equals(item.getString("pm25Value")) ) {
                  observation.put("pm25", item.optInt("pm25Value", 0));
                }
              } catch (Exception e) {
                log.error("Exception : "+ExceptionUtils.getStackTrace(e));
              }
              if(! observation.isEmpty()) {
                Map<String,Object> airQualityObservation = new LinkedHashMap<>();
                airQualityObservation.put("type","Property");
                airQualityObservation.put("observedAt", DateUtil.getISOTime((String)JsonUtil.nvl(item.get("dataTime") , DataType.STRING)));
                airQualityObservation.put("value",observation);
                tMap.put("airQualityObservation", airQualityObservation);
              } else {
                tMap.remove("airQualityObservation");
              }

              Map<String,Object> indexObservation = new LinkedHashMap<>();
              try {
                if (item.get("so2Grade") != JSONObject.NULL && !"-".equals(item.getString("so2Grade")) ) {
                  int o = item.optInt("so2Grade", 3) ;
                  indexObservation.put("so2Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("coGrade") != JSONObject.NULL && !"-".equals(item.getString("coGrade")) ) {
                  int o = item.optInt("coGrade", 3) ;
                  indexObservation.put("coCategory", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("o3Grade") != JSONObject.NULL && !"-".equals(item.getString("o3Grade")) ) {
                  int o = item.optInt("o3Grade", 3) ;
                  indexObservation.put("o3Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("no2Grade") != JSONObject.NULL && !"-".equals(item.getString("no2Grade")) ) {
                  int o = item.optInt("no2Grade", 3) ;
                  indexObservation.put("no2Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("pm10Grade") != JSONObject.NULL && !"-".equals(item.getString("pm10Grade")) ) {
                  int o = item.optInt("pm10Grade", 3) ;
                  indexObservation.put("pm10Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("pm25Grade") != JSONObject.NULL && !"-".equals(item.getString("pm25Grade")) ) {
                  int o = item.optInt("pm25Grade", 3) ;
                  indexObservation.put("pm25Category", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
                if (item.get("khaiValue") != JSONObject.NULL && !"-".equals(item.getString("khaiValue")) )  {
                  int o = item.optInt("khaiValue", 0) ;
                  indexObservation.put("totalIndex", JsonUtil.nvl(o , DataType.INTEGER));
                }
                if (item.get("khaiGrade") != JSONObject.NULL && !"-".equals(item.getString("khaiGrade")) ) {
                  int o = item.optInt("khaiGrade", 3) ;
                  indexObservation.put("totalCategory", JsonUtil.nvl(GradeType.findBy(o).getValue()));
                }
              } catch (Exception e) {
                log.error("Exception : "+ExceptionUtils.getStackTrace(e));
              }
              if(! indexObservation.isEmpty()) {
                Map<String,Object> airQualityIndexObservation = new LinkedHashMap<>();
                airQualityIndexObservation.put("type","Property");
                airQualityIndexObservation.put("observedAt",DateUtil.getISOTime((String)JsonUtil.nvl(item.get("dataTime") , DataType.STRING)));
                airQualityIndexObservation.put("value",indexObservation);
                tMap.put("airQualityIndexObservation", airQualityIndexObservation);
              } else {
                tMap.remove("airQualityIndexObservation");
              }

              Map<String,Object> indexRefMap = new LinkedHashMap<>();
              indexRefMap.put("type","Property");
              indexRefMap.put("value","https://www.airkorea.or.kr/web/khaiInfo?pMENU_NO=129");
              tMap.put("indexRef" , indexRefMap);

              //tMap.remove("indexRef");
              tMap.remove("name");

              rtnList.add(tMap);
              String str = objectMapper.writeValueAsString(tMap);
              log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
            } // for (Object jitem : jarr)

          } else {
            log(SocketCode.DATA_CONVERT_FAIL, id);
          } // if (jarr.length() > 0)
        } // if (!jsonData.has("list"))
      } // for (int i = 0 ; i < svcList.length(); i++)
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (CoreException e) {

      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL,  id,e.getMessage() );
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage() );
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
    }

    return rtnStr;
  }


} // end of class
