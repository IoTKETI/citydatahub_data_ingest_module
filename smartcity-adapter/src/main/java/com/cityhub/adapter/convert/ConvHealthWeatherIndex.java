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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

public class ConvHealthWeatherIndex extends AbstractConvert {

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
	}

	@Override
	public String doit() throws CoreException {
		StringBuffer sendJson = new StringBuffer();

		String[][] types = { { "asthmaIndex", "getAsthmaIdx" }, { "strokeIndex", "getStrokeIdx" },
				{ "foodpoisonIndex", "getFoodPoisoningIdx" }, { "oakpollenriskIndex", "getOakPollenRiskIdx" },
				{ "pinepollenriskIndex", "getPinePollenRiskIdx" }, { "weedspollenriskIndex", "getWeedsPollenRiskndx" },
				{ "coldIndex", "getColdIdx" }, { "skindiseaseIndex", "getSkinDiseaseIdx" } };

		try {
			JSONArray svcList = ConfItem.getJSONArray("serviceList");
			JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
			// 행정구역별 정보
			for (int i = 0; i < svcList.length(); i++) {

				Thread.sleep(300);

				JSONObject iSvc = svcList.getJSONObject(i);
				id = iSvc.getString("gs1Code");

				insertData(jTemplate, types, iSvc, i);

				// 기타 정보(address, @context, loacation, id) 등 입력
				JsonUtil jsonUtil = new JsonUtil(jTemplate);
				jsonUtil.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld")
						.put("http://cityhub.kr/ngsi-ld/weather.jsonld"));
				jsonUtil.put("id", iSvc.get("gs1Code") + "");
				jsonUtil.put("location.observedAt", DateUtil.getTime());
				jsonUtil.put("location.value.coordinates", iSvc.get("location"));
				jsonUtil.put("address.value.addressCountry", iSvc.get("addressCountry"));
				jsonUtil.put("address.value.addressRegion", iSvc.get("addressRegion"));
				jsonUtil.put("address.value.addressLocality", iSvc.get("addressLocality"));
				jsonUtil.put("address.value.streetAddress", iSvc.get("streetAddress"));
				jsonUtil.put("address.value.addressTown", iSvc.get("addressTown"));

				log(SocketCode.DATA_CONVERT_SUCCESS, id);

				// null 항목에서 삭제
				JSONObject tmpResult = new JSONObject(jTemplate.toString());

				sendJson.append(tmpResult.toString() + ",");
			} // for i end

		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		}

		return sendJson.toString();
	}


	private void insertData(JSONObject jTemplate, String[][] types, JSONObject iSvc, int idx) throws Exception {

		String urlParamIntro = "?pageNo=1&numOfRows=10&dataType=JSON&";

		for (int i = 0; i < types.length; i++) {

			String url = iSvc.get("url_addr").toString() + types[i][1] + urlParamIntro;
			JSONObject datum = (JSONObject) CommonUtil.getData(iSvc, url);

			if (idx == 0 && !new JsonUtil(datum).has("response.body.items.item")) {

				makeFinalTemplate(jTemplate, types[i][0]); // idx가 0일 때 최종 결과 템플릿 설정

			} else if (new JsonUtil(datum).has("response.body.items.item")) {

				JSONArray jArrItem = new JsonUtil(datum).getArray("response.body.items.item");
				JSONObject item = jArrItem.getJSONObject(0);

				insertDatum(jTemplate, item, types[i][0]);
				log(SocketCode.DATA_RECEIVE, id);
			}
		}
	}


	private void makeFinalTemplate(JSONObject jTemplate, String type) {
		jTemplate.remove(type);
	}


	private void insertDatum(JSONObject jTemplate, JSONObject item, String type) {

		int obsvTime = Integer.parseInt((new SimpleDateFormat("yyyyMMddHH").format(new Date()).substring(8)));
		JsonUtil jsonEx = new JsonUtil(jTemplate);
		String resultVal = "";

		if (obsvTime >= 6 && obsvTime < 18) {

			resultVal = JsonUtil.nvl(item.get("today")).toString();
		} else {

			resultVal = JsonUtil.nvl(item.get("tomorrow")).toString();
		}

		int compareVal = Integer.parseInt(resultVal);

		resultVal = getWeight(compareVal, type);

//		new JsonUtil(jTemplate).put(type, resultVal);
//		jTemplate.put(type, resultVal);
		jsonEx.put(type + ".value", resultVal);
	}


	private String getWeight(int compareVal, String type) {

		String weight = "";
		// 식중독일 경우와 그 외의 경우의 지표 설정
		if ("foodPoisonIndex".equals(type)) {

			if (compareVal < 55) {
				weight = "attention";
			} else if (compareVal < 71) {
				weight = "caution";
			} else if (compareVal < 86) {
				weight = "warning";
			} else {
				weight = "danger";
			}
		} else {

			switch (compareVal) {
			case 0:
				weight = "low";
				break;
			case 1:
				weight = "normal";
				break;
			case 2:
				weight = "high";
				break;
			case 3:
				weight = "very high";
				break;
			}
		}

		return weight;
	}

}
// end of class
