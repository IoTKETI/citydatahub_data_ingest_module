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
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.cityhub.environment.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateArrayListTypeHandler extends BaseTypeHandler<ArrayList<Object>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Object> parameterList, JdbcType jdbcType) throws SQLException {
    StringBuilder str = new StringBuilder();
    try {
      str.append("{");
      for (Object parameter : parameterList) {
        str.append(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT).parse((String)parameter) ).append(",");
      }
      str.deleteCharAt(str.length() - 1);
      str.append("}");
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    ps.setString(i, str.toString());
  }

  @Override
  public ArrayList<Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return null;
  }

  @Override
  public ArrayList<Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return null;
  }

  @Override
  public ArrayList<Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return null;
  }

}
