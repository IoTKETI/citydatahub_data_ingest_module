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
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvLifeWeatherIndex extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit() throws CoreException {

    StringBuffer sendJson = new StringBuffer();

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);

        id = iSvc.getString("gs1Code");
        JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        JSONArray urlC = iSvc.getJSONArray("url_addr");

        for (int j = 0; j < urlC.length(); j++) {
          iSvc.put("url_addr", urlC.getString(j));

          JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
          Thread.sleep(100);

          if (ju.has("response.body.items.item")) {
            JSONArray arrList = ju.getArray("response.body.items.item");
            JSONObject item = arrList.getJSONObject(0);

            insertData(jsonEx, item);

            jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://datahub.kr/ngsi-ld/weather.jsonld"));
            jsonEx.put("id", iSvc.getString("gs1Code"));
            jsonEx.put("location.value.coordinates", iSvc.getJSONArray("location"));
            jsonEx.put("address.value.addressCountry", JsonUtil.nvl(iSvc.getString("addressCountry")));
            jsonEx.put("address.value.addressRegion", JsonUtil.nvl(iSvc.getString("addressRegion")));
            jsonEx.put("address.value.addressLocality", JsonUtil.nvl(iSvc.getString("addressLocality")));
            jsonEx.put("address.value.addressTown", JsonUtil.nvl(iSvc.getString("addressTown")));
            jsonEx.put("address.value.streetAddress", JsonUtil.nvl(iSvc.getString("streetAddress")));

          } else {
            removeData(jsonEx, iSvc);
          }

        }

        log(SocketCode.DATA_CONVERT_SUCCESS, id, jTemplate.toString().getBytes());
        sendJson.append(jTemplate.toString() + ",");
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

  public void removeData(JsonUtil jsonEx, JSONObject iSvc) {

    String a = iSvc.getString("url_addr");
    String b = a.substring(a.lastIndexOf("/"));
    int idx = b.indexOf("?");
    String getCode = b.substring(b.length() - 3, b.length());
    String c = b.substring(1, idx);

    switch (c) {
    case "getWindChillIdx":
      jsonEx.remove("windChillIndex");
      break;
    case "getFreezeIdx":
      jsonEx.remove("freezeIndex");
      break;
    case "getDiscomfortIdx":
      jsonEx.remove("discomportIndex");
      break;
    case "getUVIdx":
      jsonEx.remove("ultraVioletIndex");
      break;
    case "getAirDiffusionIdx":
      jsonEx.remove("airDiffusionIndex");
      break;
    case "getHeatFeelingIdx":
      jsonEx.remove("heatFeelingIndex");
      break;
    case "getHeatIdx":
      jsonEx.remove("heatIndex");
      break;
    case "getSenTaIdx":
      if (getCode == "A41") {
        jsonEx.remove("sensibleTemperatureOldIndex");
        break;
      } else if (getCode == "A42") {
        jsonEx.remove("sensibleTemperatureChildIndex");
        break;
      } else if (getCode == "A44") {
        jsonEx.remove("sensibleTemperatureFarmIndex");
        break;
      } else if (getCode == "A45") {
        jsonEx.remove("sensibleTemperatureVinylHouseIndex");
        break;
      } else if (getCode == "A46") {
        jsonEx.remove("sensibleTemperatureWeakResidenceIndex");
        break;
      } else if (getCode == "A47") {
        jsonEx.remove("sensibleTemperatureRoadIndex");
        break;
      } else if (getCode == "A48") {
        jsonEx.remove("sensibleTemperatureConstructionIndex");
        break;
      } else if (getCode == "A49") {
        jsonEx.remove("sensibleTemperatureShipyardIndex");
        break;
      }
    }
  }

  public void insertData(JsonUtil jsonEx, JSONObject item) {

    if ("A03".equals(item.getString("code"))) {
      if (item.getInt("h3") >= -3.2) {
        jsonEx.put("windChillIndex.value", "attention");
      } else if (item.getInt("h3") >= -10.5 || item.getInt("h3") < -3.2) {
        jsonEx.put("windChillIndex.value", "caution");
      } else if (item.getInt("h3") >= -15.4 || item.getInt("h3") < -10.5) {
        jsonEx.put("windChillIndex.value", "warning");
      } else if (item.getInt("h3") > -15.4) {
        jsonEx.put("windChillIndex.value", "danger");
      }
    }

    if ("A06".equals(item.getString("code"))) {
      if (item.getInt("h3") > 80) {
        jsonEx.put("discomportIndex.value", "very high");
      } else if (item.getInt("h3") >= 75 || item.getInt("h3") < 80) {
        jsonEx.put("discomportIndex.value", "high");
      } else if (item.getInt("h3") >= 68 || item.getInt("h3") < 75) {
        jsonEx.put("discomportIndex.value", "normal");
      } else if (item.getInt("h3") > 68) {
        jsonEx.put("discomportIndex.value", "low");
      }
    }

    if ("A07_1".equals(item.getString("code"))) {
      if (item.getInt("today") >= 11) {
        jsonEx.put("ultraVioletIndex.value", "danger");
      } else if (item.getInt("today") >= 8 || item.getInt("today") <= 10) {
        jsonEx.put("ultraVioletIndex.value", "very high");
      } else if (item.getInt("today") >= 6 || item.getInt("today") <= 7) {
        jsonEx.put("ultraVioletIndex.value", "high");
      } else if (item.getInt("today") >= 3 || item.getInt("today") <= 5) {
        jsonEx.put("ultraVioletIndex.value", "normal");
      } else if (item.getInt("today") >= 0 || item.getInt("today") <= 2) {
        jsonEx.put("ultraVioletIndex.value", "low");
      }
    }

    if ("A08".equals(item.getString("code"))) {
      if (item.getInt("h3") >= -5) {
        jsonEx.put("freezeIndex.value", "low");
      } else if (item.getInt("h3") >= -10 || item.getInt("h3") < -5) {
        jsonEx.put("freezeIndex.value", "normal");
      } else if (item.getInt("h3") >= -15 || item.getInt("h3") < -10) {
        jsonEx.put("freezeIndex.value", "high");
      } else if (item.getInt("h3") < -15) {
        jsonEx.put("freezeIndex.value", "very high");
      }
    }

    if ("A09".equals(item.getString("code"))) {
      switch (item.getString("h3")) {
      case "100":
        jsonEx.put("airDiffusionIndex.value", "low");
      case "75":
        jsonEx.put("airDiffusionIndex.value", "normal");
      case "50":
        jsonEx.put("airDiffusionIndex.value", "high");
      case "25":
        jsonEx.put("airDiffusionIndex.value", "very high");
        break;
      }
    }

    if ("A20".equals(item.getString("code"))) {
      if (item.getInt("h3") < 21) {
        jsonEx.put("heatFeelingIndex.value", "attention");
      } else if (item.getInt("h3") >= 21 || item.getInt("h3") < 25) {
        jsonEx.put("heatFeelingIndex.value", "caution");
      } else if (item.getInt("h3") >= 25 || item.getInt("h3") < 28) {
        jsonEx.put("heatFeelingIndex.value", "warning");
      } else if (item.getInt("h3") >= 28 || item.getInt("h3") < 31) {
        jsonEx.put("heatFeelingIndex.value", "danger");
      } else if (item.getInt("h3") >= 31) {
        jsonEx.put("heatFeelingIndex.value", "very danger");
      }
    }

    if ("A05".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 66) {
        jsonEx.put("heatIndex.value", "danger");
      } else if (item.getInt("h3") >= 54 || item.getInt("h3") < 66) {
        jsonEx.put("heatIndex.value", "very high");
      } else if (item.getInt("h3") >= 41 || item.getInt("h3") < 54) {
        jsonEx.put("heatIndex.value", "high");
      } else if (item.getInt("h3") >= 32 || item.getInt("h3") < 41) {
        jsonEx.put("heatIndex.value", "normal");
      } else if (item.getInt("h3") < 32) {
        jsonEx.put("heatIndex.value", "low");
      }
    }

    if ("A41".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 37) {
        jsonEx.put("sensibleTemperatureOldIndex.value", "danger");
      } else if (item.getInt("h3") >= 34 || item.getInt("h3") < 37) {
        jsonEx.put("sensibleTemperatureOldIndex.value", "warning");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 34) {
        jsonEx.put("sensibleTemperatureOldIndex.value", "caution");
      } else if (item.getInt("h3") >= 29 || item.getInt("h3") < 31) {
        jsonEx.put("sensibleTemperatureOldIndex.value", "attetion");
      }
    }

    if ("A42".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 37) {
        jsonEx.put("sensibleTemperatureChildIndex.value", "danger");
      } else if (item.getInt("h3") >= 34 || item.getInt("h3") < 37) {
        jsonEx.put("sensibleTemperatureChildIndex.value", "warning");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 34) {
        jsonEx.put("sensibleTemperatureChildIndex.value", "caution");
      } else if (item.getInt("h3") >= 29 || item.getInt("h3") < 31) {
        jsonEx.put("sensibleTemperatureChildIndex.value", "attetion");
      }
    }

    if ("A46".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 37) {
        jsonEx.put("sensibleTemperatureWeakResidenceIndex.value", "danger");
      } else if (item.getInt("h3") >= 34 || item.getInt("h3") < 37) {
        jsonEx.put("sensibleTemperatureWeakResidenceIndex.value", "warning");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 34) {
        jsonEx.put("sensibleTemperatureWeakResidenceIndex.value", "caution");
      } else if (item.getInt("h3") >= 29 || item.getInt("h3") < 31) {
        jsonEx.put("sensibleTemperatureWeakResidenceIndex.value", "attetion");
      }
    }

    if ("A44".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 38) {
        jsonEx.put("sensibleTemperatureFarmIndex.value", "danger");
      } else if (item.getInt("h3") >= 35 || item.getInt("h3") < 38) {
        jsonEx.put("sensibleTemperatureFarmIndex.value", "warning");
      } else if (item.getInt("h3") >= 33 || item.getInt("h3") < 35) {
        jsonEx.put("sensibleTemperatureFarmIndex.value", "caution");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 33) {
        jsonEx.put("sensibleTemperatureFarmIndex.value", "attetion");
      }
    }

    if ("A45".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 38) {
        jsonEx.put("sensibleTemperatureVinylHouseIndex.value", "danger");
      } else if (item.getInt("h3") >= 35 || item.getInt("h3") < 38) {
        jsonEx.put("sensibleTemperatureVinylHouseIndex.value", "warning");
      } else if (item.getInt("h3") >= 33 || item.getInt("h3") < 35) {
        jsonEx.put("sensibleTemperatureVinylHouseIndex.value", "caution");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 33) {
        jsonEx.put("sensibleTemperatureVinylHouseIndex.value", "attetion");
      }
    }

    if ("A47".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 38) {
        jsonEx.put("sensibleTemperatureRoadIndex.value", "danger");
      } else if (item.getInt("h3") >= 35 || item.getInt("h3") < 38) {
        jsonEx.put("sensibleTemperatureRoadIndex.value", "warning");
      } else if (item.getInt("h3") >= 33 || item.getInt("h3") < 35) {
        jsonEx.put("sensibleTemperatureRoadIndex.value", "caution");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 33) {
        jsonEx.put("sensibleTemperatureRoadIndex.value", "attetion");
      }
    }

    if ("A48".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 38) {
        jsonEx.put("sensibleTemperatureConstructionIndex.value", "danger");
      } else if (item.getInt("h3") >= 35 || item.getInt("h3") < 38) {
        jsonEx.put("sensibleTemperatureConstructionIndex.value", "warning");
      } else if (item.getInt("h3") >= 33 || item.getInt("h3") < 35) {
        jsonEx.put("sensibleTemperatureConstructionIndex.value", "caution");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 33) {
        jsonEx.put("sensibleTemperatureConstructionIndex.value", "attetion");
      }
    }

    if ("A49".equals(item.getString("code"))) {
      if (item.getInt("h3") >= 38) {
        jsonEx.put("sensibleTemperatureShipyardIndex.value", "danger");
      } else if (item.getInt("h3") >= 35 || item.getInt("h3") < 38) {
        jsonEx.put("sensibleTemperatureShipyardIndex.value", "warning");
      } else if (item.getInt("h3") >= 33 || item.getInt("h3") < 35) {
        jsonEx.put("sensibleTemperatureShipyardIndex.value", "caution");
      } else if (item.getInt("h3") >= 31 || item.getInt("h3") < 33) {
        jsonEx.put("sensibleTemperatureShipyardIndex.value", "attetion");
      }
    }
  }

}