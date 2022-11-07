package com.cityhub.adapter.convex;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.exception.CoreException;
import com.cityhub.core.AbstractConvert;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWaterIndDaily extends AbstractConvert {

  @Override
  public String doit() {

    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        log.info("iSvc:{}", iSvc);

        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        log.info("ju:{}", ju);

        if (!ju.has("response.body.items.item")) {
          // throw new CoreException(ErrorCode.NORMAL_ERROR);
          continue;
        } else {
          JSONArray arr = (JSONArray) ju.getObj("response.body.items.item");

          for (int j = 0; j < arr.length(); j++) {
            Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> wMap = new LinkedHashMap<>();

            JSONObject wd = arr.getJSONObject(j);
            log.info("wd:{}", wd);

            wMap.put("mesurede", wd.optString("mesurede", ""));
            wMap.put("item1", wd.optString("item1", ""));
            wMap.put("item2", wd.optString("item2", ""));
            wMap.put("item3", wd.optString("item3", ""));
            wMap.put("item4", wd.optString("item4", ""));
            wMap.put("item5", wd.optString("item5", ""));
            wMap.put("item6", wd.optString("item6", ""));
            wMap.put("item7", wd.optString("item7", ""));
            wMap.put("item8", wd.optString("item8", ""));
            wMap.put("item9", wd.optString("item9", ""));
            wMap.put("item10", wd.optString("item10", ""));

            Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
            addrValue.put("addressCountry", iSvc.optString("addressCountry", ""));
            addrValue.put("addressRegion", iSvc.optString("addressRegion", ""));
            addrValue.put("addressLocality", iSvc.optString("addressLocality", ""));
            addrValue.put("addressTown", iSvc.optString("addressTown", ""));
            addrValue.put("streetAddress", iSvc.optString("streetAddress", ""));

            String idid = iSvc.getString("fcode") + "_" + Integer.toString(j);
            tMap.put("id", idid);
            Map<String, Object> waterDaily = new LinkedHashMap<>();
            waterDaily.put("type", "Property");
            waterDaily.put("observedAt", DateUtil.getTime());
            waterDaily.put("value", wMap);
            tMap.put("getWaterIndDaily", waterDaily);

            rtnList.add(tMap);
            log.info("tMap:{}", tMap);
            String str = objectMapper.writeValueAsString(tMap);
            toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
          }
        }
      }
      rtnStr = objectMapper.writeValueAsString(rtnList);
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return rtnStr;
  }
} // end of class
