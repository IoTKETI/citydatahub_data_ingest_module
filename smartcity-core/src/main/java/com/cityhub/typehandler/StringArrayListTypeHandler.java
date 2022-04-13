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
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class StringArrayListTypeHandler extends BaseTypeHandler<ArrayList<String>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<String> parameterList, JdbcType jdbcType) throws SQLException {
    StringBuilder str = new StringBuilder();
    str.append("{");
    for (String parameter : parameterList) {
      if (parameter == null) {
        str.append("null").append(",");
        continue;
      }
      if (parameter.indexOf("\"") > 0) {
        parameter = parameter.replace("\"", "\\\"");
      }
      str.append("\"").append(parameter).append("\"").append(",");
    }
    str.deleteCharAt(str.length() - 1);
    str.append("}");
    ps.setString(i, str.toString());
  }

  @Override
  public ArrayList<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String str = rs.getString(columnName);

    ArrayList<String> roles = new ArrayList<String>();
    String[] rolesarray = str.split(",");
    for (String s : rolesarray) {
      roles.add(s);
    }
    return roles;
  }

  @Override
  public ArrayList<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String str = rs.getString(columnIndex);

    ArrayList<String> roles = new ArrayList<String>();
    String[] rolesarray = str.split(",");
    for (String s : rolesarray) {
      roles.add(s);
    }
    return roles;
  }

  @Override
  public ArrayList<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String str = cs.getString(columnIndex);

    ArrayList<String> roles = new ArrayList<String>();
    String[] rolesarray = str.split(",");
    for (String s : rolesarray) {
      roles.add(s);
    }
    return roles;
  }

}
