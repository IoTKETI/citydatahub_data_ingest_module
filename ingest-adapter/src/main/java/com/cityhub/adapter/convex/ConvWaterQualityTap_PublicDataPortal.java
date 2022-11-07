package com.cityhub.adapter.convex;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cityhub.core.AbstractNormalSource;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.CommonUtil;
import com.cityhub.utils.DataCoreCode.SocketCode;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvWaterQualityTap_PublicDataPortal extends AbstractNormalSource {
  private String gettime;

  @Override
  public String doit() {

    List<Map<String, Object>> rtnList = new LinkedList<>();
    String rtnStr = "";

    try {
      JSONArray svcList = ConfItem.getJSONArray("serviceList");

      for (int i = 0; i < svcList.length(); i++) {
        JSONObject iSvc = svcList.getJSONObject(i);
        id = iSvc.getString("gs1Code");
        JsonUtil ju = new JsonUtil((JSONObject) CommonUtil.getData(iSvc));
        Thread.sleep(300);
        if (ju.getObj("response.body.items").toString().equals("")) {
          toLogger(SocketCode.SOCKET_CONNECT_FAIL, id);
          continue;
        } else {
          toLogger(SocketCode.DATA_RECEIVE, id, ju.toString().getBytes());
          JSONArray arr = new JSONArray();
          Integer arrlength;
          boolean JsonArrayAs = ju.getObj("response.body.items.item") instanceof JSONArray;

          if (JsonArrayAs) {
            arr = (JSONArray) ju.getObj("response.body.items.item");
            arrlength = arr.length();
          } else if (ju.getObj("response.body.items.item") instanceof JSONObject) {
            arrlength = 1;
          } else {
            toLogger(SocketCode.SOCKET_CONNECT_FAIL, id);
            continue;
          }

          for (int j = 0; j < arrlength; j++) {

            JSONObject item;
            if (JsonArrayAs) {
              item = arr.getJSONObject(j);
            } else {
              item = (JSONObject) ju.getObj("response.body.items.item");
            }

            if (item != null) {
              Map<String, Object> tMap = objectMapper.readValue(templateItem.getJSONObject(ConfItem.getString("modelId")).toString(), new TypeReference<Map<String, Object>>() {
              });
              Map<String, Object> wMap = new LinkedHashMap<>();

              gettime = item.optString("mesurede");

              if (item.has("item1"))
                Find_wMap(tMap, "taste").put("value", item.optString("item1"));
              else
                Delete_wMap(tMap, "taste");

              if (item.has("item2"))
                Find_wMap(tMap, "odor").put("value", item.optString("item2"));
              else
                Delete_wMap(tMap, "odor");

              if (item.has("item3"))
                Find_wMap(tMap, "chromaticity").put("value", item.optDouble("item3"));
              else
                Delete_wMap(tMap, "chromaticity");

              if (item.has("item4"))
                Find_wMap(tMap, "hydrogenIndex").put("value", item.optDouble("item4"));
              else
                Delete_wMap(tMap, "hydrogenIndex");

              if (item.has("item5"))
                Find_wMap(tMap, "turbidity").put("value", item.optDouble("item5"));
              else
                Delete_wMap(tMap, "turbidity");

              if (item.has("item6"))
                Find_wMap(tMap, "residualChlorine").put("value", item.optDouble("item6"));
              else
                Delete_wMap(tMap, "residualChlorine");

              wMap = (Map) tMap.get("globalLocationNumber");
              wMap.put("value", id);

              wMap = (Map) tMap.get("dataProvider");
              wMap.put("value", iSvc.optString("dataProvider", "http://www.kwater.or.kr"));

              tMap.put("id", id);

              Map<String, Object> addrValue = (Map) ((Map) tMap.get("address")).get("value");
              addrValue.put("addressCountry", iSvc.optString("addressCountry"));
              addrValue.put("addressRegion", iSvc.optString("addressRegion"));
              addrValue.put("addressLocality", iSvc.optString("addressLocality"));
              addrValue.put("addressTown", iSvc.optString("addressTown"));
              addrValue.put("streetAddress", iSvc.optString("streetAddress"));

              Map<String, Object> locMap = (Map) tMap.get("location");
              locMap.put("observedAt", DateUtil.getTime());
              Map<String, Object> locValueMap = (Map) locMap.get("value");
              locValueMap.put("coordinates", iSvc.getJSONArray("location"));

              rtnList.add(tMap);
              String str = objectMapper.writeValueAsString(tMap);
              toLogger(SocketCode.DATA_CONVERT_SUCCESS, id, str.getBytes());
              toLogger(SocketCode.DATA_SAVE_REQ, id, str.getBytes());
            }
          }
        }
      }
      sendEvent(rtnList, ConfItem.getString("datasetId"));
    } catch (CoreException e) {
      if ("!C0099".equals(e.getErrorCode())) {
        toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      }
    } catch (Exception e) {
      toLogger(SocketCode.DATA_CONVERT_FAIL, id, e.getMessage());
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return "Success";
  }

  Map<String, Object> Find_wMap(Map<String, Object> tMap, String Name) {
    Map<String, Object> ValueMap = (Map) tMap.get(Name);
    Calendar cal = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyyMMdd");
    Date date = null;
    try {
      date = df.parse(gettime);
    } catch (ParseException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    cal.setTime(date);
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");
    ValueMap.put("observedAt", df2.format(cal.getTime()));
    return ValueMap;
  }

  void Delete_wMap(Map<String, Object> tMap, String Name) {
    tMap.remove(Name);
  }

} // end of class
