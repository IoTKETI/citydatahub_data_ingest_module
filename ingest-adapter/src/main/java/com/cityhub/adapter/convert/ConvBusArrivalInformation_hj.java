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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvBusArrivalInformation_hj extends AbstractConvert {

	@Override
	public void init(JSONObject ConfItem, JSONObject templUtilateItem) {
		super.setup(ConfItem, templUtilateItem);
	}

	@Override
	public String doit() throws CoreException {

		StringBuffer strBuff = new StringBuffer();

		JSONArray jsonArr = ConfItem.getJSONArray("serviceList");

		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject templ = templateItem.getJSONObject("BusArrivalInformation");
			log.info("templUtil.toString()" + templ.toString());
			JsonUtil templUtil = new JsonUtil(templ);
			log.info("templUtilUtil.toString() : " + templUtil.toString());

			JSONObject jsonObj = jsonArr.getJSONObject(i);
			try {
				JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(jsonObj));

				if (!ju.has("response.msgBody.busArrivalList")) {
					continue;
				} else {

					if (ju.getObj("response.msgBody.busArrivalList") instanceof JSONArray) {
						JSONArray busArr = (JSONArray) ju.getObj("response.msgBody.busArrivalList");
						for (int j = 0; j < busArr.length(); j++) {
							insertData_obj(busArr.getJSONObject(j), templUtil, jsonObj);
						}
					} else {
						JSONObject busObj = (JSONObject) ju.getObj("response.msgBody.busArrivalList");
						insertData_obj(busObj.getJSONObject("busArrivalList"), templUtil, jsonObj);
					} // end of else

				}
			} catch (Exception e) {
			  log.error("Exception : "+ExceptionUtils.getStackTrace(e));
			}

			templUtil.toString();
			log(SocketCode.DATA_CONVERT_SUCCESS, id, templUtil.toString().getBytes());
			strBuff.append(templUtil + ",");
		} // end of for

		return strBuff.toString();
	}// end of doit()

	static void insertData_obj(JSONObject busObj, JsonUtil templUtil, JSONObject jsonObj) {

		if (busObj.get("flag").equals("RUN") || busObj.get("flag").equals("PASS")) {
			templUtil.put("flag.value", "운행중");
		} else if (busObj.get("flag").equals("STOP")) {
			templUtil.put("flag.value", "운행종료");
		} else if (busObj.get("flag").equals("WAIT")) {
			templUtil.put("flag.value", "회차지대기");
		} else {
			templUtil.remove("flag");
		}

		if (busObj.get("plateNo1").equals(null)) { // 첫번째 차량
			templUtil.remove("plateNo1");
			templUtil.remove("lowPlate1");
			templUtil.remove("predictTime1");
		} else {
			templUtil.put("plateNo1.value", busObj.get("plateNo1"));
			templUtil.put("predictTime1.value", busObj.get("predictTime1"));
			if (busObj.get("lowPlate1").equals(0)) {
				templUtil.put("lowPlate1.value", "일반버스");
			} else {
				templUtil.put("lowPlate1.value", "저상버스");
			}
		}

		if (busObj.get("plateNo2").equals(null)) { // 두번째 차량
			templUtil.remove("plateNo2");
			templUtil.remove("lowPlate2");
			templUtil.remove("predictTime2");
		} else {
			templUtil.put("plateNo2.value", busObj.get("plateNo2"));
			templUtil.put("predictTime2.value", busObj.get("predictTime2"));
			templUtil.put("predictTime2.observedAt", DateUtil.getTime());
			if (busObj.get("lowPlate2").equals(0)) {
				templUtil.put("lowPlate2.value", "일반버스");
			} else {
				templUtil.put("lowPlate2.value", "저상버스");
			}
		}

		String id = jsonObj.optString("gs1Code");
		templUtil.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld")
				.put("http://datahub.kr/ngsi-ld/trafficInformation.jsonld"));
		templUtil.put("id", id);
		templUtil.put("createdAt", DateUtil.getTime());
		templUtil.put("modifiedAt", DateUtil.getTime());

		templUtil.put("location.value.coordinates", jsonObj.getJSONArray("location"));

		templUtil.put("address.value.addressCountry", jsonObj.get("addressCountry"));
		templUtil.put("address.value.addressRegion", jsonObj.get("addressRegion"));
		templUtil.put("address.value.addressRegion", jsonObj.get("addressRegion"));
		templUtil.put("address.value.addressLocality", jsonObj.get("addressLocality"));
		templUtil.put("address.value.streetAddress", jsonObj.get("streetAddress"));
		templUtil.put("address.value.addressTown", jsonObj.get("addressTown"));
	}

}// end of class
