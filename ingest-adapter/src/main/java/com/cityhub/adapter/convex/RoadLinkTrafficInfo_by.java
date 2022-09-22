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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoadLinkTrafficInfo_by extends AbstractConvert {
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
      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));

        if (!ju.has("ServiceResult.msgBody.itemList") ) {
          throw new CoreException(ErrorCode.NORMAL_ERROR);
        } else {
          log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());

          Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
          Map<String,Object> wMap = new LinkedHashMap<>();

          log.info("ServiceResult:{}",ju.get("ServiceResult"));
          log.info("getObj:{}",ju.getObj("ServiceResult.msgBody.itemList"));

          JSONObject item =  (JSONObject) ju.getObj("ServiceResult.msgBody.itemList");

          if (item.length() > 0) {
	        	  if (item.has("linkDelayTime")) {
	                  wMap.put("linkDelayTime",  JsonUtil.nvl(item.get("linkDelayTime") , DataType.INTEGER));
	        	  }
	              if (item.has("trvlTime")) {
		              wMap.put("trvlTime", JsonUtil.nvl(item.get("trvlTime") , DataType.INTEGER));
	              }
		          if (item.has("spd")) {
		              wMap.put("spd", JsonUtil.nvl(item.get("spd") , DataType.STRING));
		          }
		          if (item.has("endNodeNm")) {
			          wMap.put("endNodeNm", JsonUtil.nvl(item.get("endNodeNm") , DataType.STRING));
		          }
                  if (item.has("startNodeNm")) {
	                  wMap.put("startNodeNm", JsonUtil.nvl(item.get("startNodeNm") , DataType.STRING));
                  }
	              if (item.has("endNodeId")) {
		              wMap.put("endNodeId", JsonUtil.nvl(item.get("endNodeId") , DataType.STRING));
	              }
		          if (item.has("startNodeId")) {
		              wMap.put("startNodeId", JsonUtil.nvl(item.get("startNodeId") , DataType.STRING));
		          }
		          if (item.has("vol")) {
			          wMap.put("vol", JsonUtil.nvl(item.get("vol") , DataType.INTEGER));
		          }
			      if (item.has("linkId")) {
			          wMap.put("linkId", JsonUtil.nvl(item.get("linkId") , DataType.STRING));
			      }
			      if (item.get("routeId")==null || item.get("routeId")=="") {

			      }else {
			    	  wMap.put("routeId", JsonUtil.nvl(item.get("routeId") , DataType.INTEGER));
			      }
	              if (item.has("congGrade")) {

	            	  switch((Integer)item.get("congGrade")) {
	            	  		case 0:
	            	  			wMap.put("congGrade", "noData");
	            	  			break;
	            	  		case 1:
	            	  			wMap.put("congGrade", "smooth");
	            	  			break;
	            	  		case 2:
	            	  			wMap.put("congGrade", "slow");
	            	  			break;
	            	  		case 3:
	            	  			wMap.put("congGrade", "congested");
	            	  			break;
	            	  }
		              //wMap.put("congGrade", JsonUtil.nvl(item.get("congGrade") , DataType.STRING));
	              }
		          if (item.has("collDate")) {
		        	  Timestamp tt = Timestamp.valueOf((String) item.get("collDate"));
			          wMap.put("collDate", tt);
			          // DateUtil.getTimestamp()  수집시간데이터를 가져올 수 있음
		          }
		          if (item.get("routeNm")==null || item.get("routeNm")=="") {

			      }else {
			    	  wMap.put("routeNm", JsonUtil.nvl(item.get("routeNm") , DataType.STRING));
			      }

			      System.out.println("wMet----------------- : "+wMap.get("spd") );

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
	          Map<String,Object> roadTrafficInfo = new LinkedHashMap<>();
	          roadTrafficInfo.put("type", "Property");
	          roadTrafficInfo.put("observedAt", DateUtil.getTime());
	          roadTrafficInfo.put("value", wMap);
	          tMap.put("RoadLinkTrafficInfo", roadTrafficInfo);

	          log.info("tMap:{}", tMap);
	          rtnList.add(tMap);
	          String str = objectMapper.writeValueAsString(tMap);
	          log(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
        }else {
            log(SocketCode.DATA_CONVERT_FAIL, "" , id);
          }
        }
      }

      rtnStr = objectMapper.writeValueAsString(rtnList);

    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        log(SocketCode.DATA_CONVERT_FAIL, id,  e.getMessage());
      }
    } catch (Exception e) {
      log(SocketCode.DATA_CONVERT_FAIL,  id,  e.getMessage());
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage() + "`" + id  , e);
    }

    return rtnStr;
  }


} // end of class
