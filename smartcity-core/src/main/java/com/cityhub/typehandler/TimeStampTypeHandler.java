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
package com.cityhub.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import com.cityhub.environment.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@MappedJdbcTypes({ JdbcType.TIMESTAMP })
public class TimeStampTypeHandler extends BaseTypeHandler<String> {

  SimpleDateFormat transFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String o, JdbcType jdbcType) throws SQLException {
    try {
      ps.setTimestamp(i, new Timestamp(transFormat.parse(o).getTime()));
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String val = rs.getString(columnName);
    if (val != null) {
      return transFormat.format(rs.getTimestamp(columnName));
    } else {
      return null;
    }
  }

  @Override
  public String getNullableResult(ResultSet resultSet, int i) throws SQLException {
    return null;
  }

  @Override
  public String getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
    return null;
  }

}
