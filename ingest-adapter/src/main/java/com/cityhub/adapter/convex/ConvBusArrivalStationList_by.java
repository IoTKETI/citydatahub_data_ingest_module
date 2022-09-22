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

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.environment.Constants;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvBusArrivalStationList_by extends AbstractConvert {
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

	        if (!ju.has("response.msgBody.busArrivalItem") ) {
	          throw new CoreException(ErrorCode.NORMAL_ERROR);
	        } else {
	          log(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());

	          Map<String,Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String,Object>>(){});
	          Map<String,Object> wMap = new LinkedHashMap<>();

	          log.info("response:{}",ju.get("response"));
	          log.info("response.msgBody.busArrivalItem:{}",ju.getObj("response.msgBody.busArrivalItem"));

	          JSONObject item =  (JSONObject) ju.getObj("response.msgBody.busArrivalItem");

	          if (item.length() > 0) {

	        	  wMap.put("stationId", item.optString("stationId", ""));
	        	  wMap.put("staOrder", item.optString("staOrder", ""));
		  	      wMap.put("flag", item.optString("flag", ""));
		  	      wMap.put("routeId", item.optString("routeId", ""));
		  	      wMap.put("predictTime1", item.optString("predictTime1", ""));
		  	      wMap.put("predictTime2", item.optString("predictTime2", "0"));

		      	  wMap.put("plateNo1", item.optString("plateNo1", ""));
		  	      wMap.put("plateNo2", item.optString("plateNo2", "0"));
		  	      wMap.put("locationNo1", item.optString("locationNo1", ""));
		  	      wMap.put("locationNo2", item.optString("locationNo2", "0"));
		  	      wMap.put("lowPlate1", item.optString("lowPlate1", ""));
		  	      wMap.put("lowPlate2", item.optString("lowPlate2", ""));
		  	      wMap.put("remainSeatCnt1", item.optInt("remainSeatCnt1", 0));
			      wMap.put("remainSeatCnt2", item.optInt("remainSeatCnt2", 0));

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
		          Map<String,Object> busArrivalInfo = new LinkedHashMap<>();
		          busArrivalInfo.put("type", "Property");
		          busArrivalInfo.put("observedAt", DateUtil.getTime());
		          busArrivalInfo.put("value", wMap);
		          tMap.put("BusArrivalInfo", busArrivalInfo);

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