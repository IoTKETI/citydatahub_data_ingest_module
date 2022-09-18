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

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.JsonUtil;

public class ConvAirQualityMeasurementPortable extends AbstractConvert {

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
	}

	@Override
	public String doit() throws CoreException {
		StringBuffer sendJson = new StringBuffer();

		try {
			JSONArray svcList = ConfItem.getJSONArray("serviceList");
			String model = ConfItem.getString("model_id");
			for (int i = 0; i < svcList.length(); i++) {

				JSONObject iSvc = (JSONObject) svcList.get(i);

				JSONObject jsonData = getData(iSvc);
				JsonUtil ju = new JsonUtil(jsonData);

				JSONArray itemsArray = null;

				if("00".equals(jsonData.getString("resultCode")) &&
				   "SUCCESS".equals(jsonData.getString("resultMsg")) &&
				   ju.has("list")) {
					itemsArray = ju.getArray("list");
				}else {
					itemsArray = new JSONArray();
				}

				for(Object tempItemObj : itemsArray) {
					JsonUtil templateJsonUtil = new JsonUtil(templateItem.getJSONObject(model).toString());
					JSONObject jobj = (JSONObject) tempItemObj;

					inputData(jobj, templateJsonUtil, iSvc);
					sendJson.append(templateJsonUtil.toString() + ", ");
				}

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
	} // end of doit

	private void inputData(JSONObject jobj, JsonUtil templateJsonUtil, JSONObject iSvc) {

		templateJsonUtil.put("pm25.value",JsonUtil.nvl(jobj.get("pm25"), DataType.FLOAT));
		templateJsonUtil.put("temper.value",JsonUtil.nvl(jobj.get("temper"), DataType.FLOAT));
		templateJsonUtil.put("pm10.value",JsonUtil.nvl(jobj.get("pm10"), DataType.FLOAT));
		templateJsonUtil.put("tod.value",JsonUtil.nvl(jobj.get("tod"), DataType.FLOAT));
		templateJsonUtil.put("h2s.value",JsonUtil.nvl(jobj.get("h2s"), DataType.FLOAT));
		templateJsonUtil.put("nh3.value",JsonUtil.nvl(jobj.get("nh3"), DataType.FLOAT));
		templateJsonUtil.put("humid.value",JsonUtil.nvl(jobj.get("humid"), DataType.FLOAT));
		templateJsonUtil.put("vocs.value",JsonUtil.nvl(jobj.get("vocs"), DataType.FLOAT));

		templateJsonUtil.put("observedAt.value",JsonUtil.nvl(jobj.get("time"), DataType.STRING));
		templateJsonUtil.put("deviceId.value",JsonUtil.nvl(jobj.get("deviceId"), DataType.STRING));

		Float lon = jobj.getFloat("lon");
		Float lat = jobj.getFloat("lat");
		ArrayList<Float> location = new ArrayList<>();
		location.add(lon);
		location.add(lat);
		templateJsonUtil.put("location.value.coordinates",location);


		templateJsonUtil.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry"), DataType.STRING));
		templateJsonUtil.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion"), DataType.STRING));
		templateJsonUtil.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality"), DataType.STRING));
		templateJsonUtil.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown"), DataType.STRING));
		templateJsonUtil.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress"), DataType.STRING));

		templateJsonUtil.put("deviceType.value", JsonUtil.nvl(iSvc.getString("deviceType"), DataType.STRING));
		templateJsonUtil.put("id", JsonUtil.nvl(iSvc.getString("gs1Code"), DataType.STRING) + "" + JsonUtil.nvl(jobj.get("deviceId"), DataType.STRING));

	}

	private JSONObject getData(JSONObject svc) throws Exception {
		return new JsonUtil().getFileJsonObject("/openapi/AirQualityMeasurementDummyData.conf");
	}
/*
	private String urlAssemble(JSONObject svc) {
		String url = svc.getString("url_addr");
		String[] paramVariable = svc.getString("ParamVariable").split(",");
		String value = "";

		if (svc.has("key_name") && svc.has("key_value")) {
	        url += "&" + svc.getString("key_name") + "=" + svc.getString("key_value");
	    }

		for (String it : paramVariable) {
			if (!"".equals(it) && svc.has(it)) {
				value = svc.getString(it) + "";
				if (value.indexOf(",") > -1) {
					String[] val = value.split(",");
					if (DateUtil.isPatternDate(val[0])) {
						url += "&" + it + "="
								+ DateUtil.addDate(DateUtil.getChronoUnit(val[1]), Integer.parseInt(val[2]), val[0]);
					}
				} else {
					if (DateUtil.isPatternDate(value)) {
						url += "&" + it + "=" + DateUtil.getDate(value);
					} else {
						url += "&" + it + "=" + value;
					}
				}
			} else {
				if (svc.has("key")) {
					url += "&" + it + "=" + svc.getString("key");
				}
			}
		}
		return url;
	}

	private Headers buildHeader(JSONObject svc) {
		Map h = new HashMap();
		JSONArray headers = svc.getJSONArray("headers");

		for (Object obj : headers) {
			JSONObject jobj = (JSONObject) obj;
			h.putAll(jobj.toMap());
		}

		return Headers.of(h);
	}
*/
}
// end of class