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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataType;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.JsonUtil;

public class ConvCCTV  extends AbstractConvert {

  @Override
  public void init(JSONObject ConfItem, JSONObject templateItem) {
    super.setup(ConfItem, templateItem);
  }

  @Override
  public String doit(Statement st ) throws CoreException {
    StringBuffer sendJson = new StringBuffer();
    String q = ConfItem.getString("query");

    List<String> idArr = new ArrayList<String>();
    try (ResultSet rs = st.executeQuery(q)) {
      while (rs.next()) {

        JSONObject jTemplate = templateItem.getJSONObject(ConfItem.getString("model_id"));
        JsonUtil jsonEx = new JsonUtil(jTemplate);

        // 매핑(컨버트) 구현
        jsonEx.put("@context", new JSONArray().put("http://uri.etsi.org/ngsi-ld/core-context.jsonld").put("http://cityhub.kr/ngsi-ld/infrastructure.jsonld"));
        jsonEx.put("id", "urn:datahub:CCTV:880969104:" + JsonUtil.nvl(rs.getString("mng_sn")));
        jsonEx.put("type", "CCTV" );
        jsonEx.put("location.value.coordinates", new JSONArray().put(JsonUtil.nvl(rs.getString("point_x"))).put(JsonUtil.nvl(rs.getString("point_y"))));
        jsonEx.put("address.value.addressCountry", "KR" );
        jsonEx.put("address.value.addressRegion", "Gyeonggi-do" );
        jsonEx.put("address.value.addressLocality", "Siheung-si" );
        jsonEx.put("address.value.addressTown",  JsonUtil.nvl(rs.getString("address")));
        jsonEx.put("address.value.streetAddress", JsonUtil.nvl(rs.getString("address")));


        jsonEx.put("name", JsonUtil.nvl(rs.getString("fclt_lbl_nm")));
        jsonEx.put("isRotatable", JsonUtil.nvl(rs.getString("fclt_knd_dtl_cd")));
        jsonEx.put("status", JsonUtil.nvl(rs.getString("fclt_sttus")));
        if (rs.getString("fclt_instl_ymd") != null) {
          jsonEx.put("installedAt", DateUtil.getISOTime(rs.getString("fclt_instl_ymd")));
        } else {
          jsonEx.put("installedAt", JSONObject.NULL);
        }
        jsonEx.put("distance", JsonUtil.nvl(rs.getString("cctv_osvt_dstc"), DataType.FLOAT));
        jsonEx.put("direction", JsonUtil.nvl(rs.getString("cctv_osvt_ag"), DataType.FLOAT));
        jsonEx.put("fieldOfView", JsonUtil.nvl(rs.getString("cctv_view_ag"), DataType.FLOAT));
        jsonEx.put("hasEmergencyBell", JsonUtil.nvl(rs.getString("egb_yn")));


        String[] searchKey = {"fieldOfView", "direction", "distance", "hasEmergencyBell", "installedAt", "name", "isRotatable", "address.value"};
        JsonUtil.removeNullItem(jTemplate, searchKey , new ArrayList<String>()  );

        sendJson.append(jTemplate.toString() + ",");
        idArr.add(rs.getString("fclt_id"));
      }

      // 전송 후에 업데이트 (분당 100건 처리)
      for (String id : idArr) {
        st.executeUpdate("update cctv set send = 'Y' where fclt_id= '" + id + "'" );
      }

    } catch (Exception e) {
      throw new CoreException(ErrorCode.NORMAL_ERROR,e.getMessage(), e);
    }



    return sendJson.toString();
  }





} // end of class
