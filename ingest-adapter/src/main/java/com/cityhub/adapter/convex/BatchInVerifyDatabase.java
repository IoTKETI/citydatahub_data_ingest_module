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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import com.cityhub.dto.VerifyStatusVO;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchInVerifyDatabase {
  HikariDataSource ds = null;
  public BatchInVerifyDatabase(String init) {
    JSONObject _init = new JSONObject(init);
    try {
      ds = new HikariDataSource();
      ds.setJdbcUrl(_init.getString("db_url"));
      ds.setUsername(_init.getString("db_username"));
      ds.setPassword(_init.getString("db_password"));
      ds.setAutoCommit(true);
    } catch(Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }
  public void insertListInVerifyDb(List<VerifyStatusVO> list) {
    String q = "INSERT INTO public.covid_verify (";
    q += "unique_id, group_id, data_type, request_info_id , person_id, observed_time ";
    q += " ) values (";
    q += " ?, ?, ?, ?, ?, ? ";
    q += " ) ";

    try (Connection conn = ds.getConnection();
        PreparedStatement psmt = conn.prepareStatement(q);){

      for (int i=0; i< list.size(); i++) {
        VerifyStatusVO vo = list.get(i);
        psmt.setString(1, vo.getUniqueId());
        psmt.setString(2, vo.getGroupId());
        psmt.setString(3, vo.getDataType());
        psmt.setString(4, vo.getRequestInfoId());
        psmt.setString(5, vo.getPersonid());
        psmt.setString(6, vo.getObservedTime());
        psmt.addBatch();
        psmt.clearParameters();
        if( (i % 1000) == 0){
          // Batch 실행
          psmt.executeBatch() ;
          // Batch 초기화
          psmt.clearBatch();
        }
      }
      psmt.executeBatch();
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  } // end method

  public void close() {
    if (ds != null) try {ds.close();ds = null;} catch(Exception ex){}
  }

} // end class