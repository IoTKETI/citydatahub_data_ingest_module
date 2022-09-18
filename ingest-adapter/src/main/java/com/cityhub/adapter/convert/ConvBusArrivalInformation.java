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

import org.json.JSONArray;
import org.json.JSONObject;
import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;

import lombok.extern.slf4j.Slf4j;

import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;

@Slf4j
public class ConvBusArrivalInformation extends AbstractConvert {

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
	}

	@Override
	public String doit() throws CoreException {

		StringBuffer strBuff = new StringBuffer();
		JSONObject jsonObj = new JSONObject();

		JSONArray jsonArr = ConfItem.getJSONArray("serviceList");
		JSONObject templ = templateItem.getJSONObject("BusArrivalInformation");
		JsonUtil templUtil = new JsonUtil(templ);
		log.info(" templUtil.toString() : " + templUtil.toString());

		for (int i = 0; i < jsonArr.length(); i++) {

			jsonObj = jsonArr.getJSONObject(i);

			templUtil.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld")
					.put("http://datahub.kr/ngsi-ld/trafficInformation.jsonld"));
			templUtil.put("id", jsonObj.get("gs1Code"));
			templUtil.put("createdAt", DateUtil.getTime());
			templUtil.put("modifiedAt", DateUtil.getTime());
			templUtil.put("location.value.coordinates", jsonObj.getJSONArray("location"));
			templUtil.put("address.value.addressCountry", jsonObj.get("addressCountry"));
			templUtil.put("address.value.addressRegion", jsonObj.get("addressRegion"));
			templUtil.put("address.value.addressRegion", jsonObj.get("addressRegion"));
			templUtil.put("address.value.addressLocality", jsonObj.get("addressLocality"));
			templUtil.put("address.value.streetAddress", jsonObj.get("streetAddress"));
			templUtil.put("address.value.addressTown", jsonObj.get("addressTown"));

			try {
				JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(jsonObj));
				if (ju.has("response.msgBody")) {
					JSONObject busInfo = ju.getObject("response.msgBody");
					insertData(templUtil, busInfo);
				} else {
					templUtil.remove("flag.value");
					templUtil.remove("lowPlate1.value");
					templUtil.remove("plateNo1.value");
					templUtil.remove("predictTime1.value");
					templUtil.remove("lowPlate2.value");
					templUtil.remove("plateNo2.value");
					templUtil.remove("predictTime2.value");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			templUtil.toString();
			log(SocketCode.DATA_CONVERT_SUCCESS, id, templUtil.toString().getBytes());
			strBuff.append(templUtil + ",");
		} // end of for

		return strBuff.toString();
	}// end of doit()

	public static void insertData(JsonUtil templUtil, JSONObject busInfo) {
		String juToStr = busInfo.toString();
		int idx = juToStr.indexOf("[");

		if (idx >= 0) {
			JSONArray busArr = busInfo.getJSONArray("busArrivalList"); // [{},{},{}...]

			for (int i = 0; i < busArr.length(); i++) {
				JSONObject busArrToObj = busArr.getJSONObject(i);

				if (busArrToObj.get("flag").equals("RUN") || busArrToObj.get("flag").equals("PASS")) {
					templUtil.put("flag.value", "운행중");
				} else if (busArrToObj.get("flag").equals("STOP")) {
					templUtil.put("flag.value", "운행종료");
				} else if (busArrToObj.get("flag").equals("WAIT")) {
					templUtil.put("flag.value", "회차지대기");
				} else {
					templUtil.remove("flag.value");
				}

				if (busArrToObj.get("plateNo1").equals(null)) { // 첫번째 차량
					templUtil.remove("plateNo1.value");
					templUtil.remove("lowPlate1.value");
					templUtil.remove("predictTime1.value");
				} else {
					templUtil.put("plateNo1.value", busArrToObj.get("plateNo1"));
					templUtil.put("predictTime1.value", busArrToObj.get("predictTime1"));

					if (busArrToObj.get("lowPlate1").equals(0)) {
						templUtil.put("lowPlate1.value", "일반버스");
					} else {
						templUtil.put("lowPlate1.value", "저상버스");
					}
				}

				if (busArrToObj.get("plateNo2").equals(null)) { // 두번째 차량
					templUtil.remove("plateNo2.value");
					templUtil.remove("lowPlate2.value");
					templUtil.remove("predictTime2.value");
				} else {
					templUtil.put("plateNo2.value", busArrToObj.get("plateNo2"));
					templUtil.put("predictTime2.value", busArrToObj.get("predictTime2"));
					if (busArrToObj.get("lowPlate2").equals(0)) {
						templUtil.put("lowPlate2.value", "일반버스");
					} else {
						templUtil.put("lowPlate2.value", "저상버스");
					}
				}
			}

		} else {
			JSONObject busObj = busInfo.getJSONObject("busArrivalList"); // {...}

			if (busObj.get("flag").equals("RUN") || busObj.get("flag").equals("PASS")) {
				templUtil.put("flag.value", "운행중");
			} else if (busObj.get("flag").equals("STOP")) {
				templUtil.put("flag.value", "운행종료");
			} else if (busObj.get("flag").equals("WAIT")) {
				templUtil.put("flag.value", "회차지대기");
			} else {
				templUtil.remove("flag.value");
			}

			if (busObj.get("plateNo1").equals(null)) { // 첫번째 차량
				templUtil.remove("plateNo1.value");
				templUtil.remove("lowPlate1.value");
				templUtil.remove("predictTime1.value");
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
				templUtil.remove("plateNo2.value");
				templUtil.remove("lowPlate2.value");
				templUtil.remove("predictTime2.value");
			} else {
				templUtil.put("plateNo2.value", busObj.get("plateNo2"));
				templUtil.put("predictTime2.value", busObj.get("predictTime2"));
				if (busObj.get("lowPlate2").equals(0)) {
					templUtil.put("lowPlate2.value", "일반버스");
				} else {
					templUtil.put("lowPlate2.value", "저상버스");
				}
			}
		} // end of else
	} // end of insertData()

}// end of class
