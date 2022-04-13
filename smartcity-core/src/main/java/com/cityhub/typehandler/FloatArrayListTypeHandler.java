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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class FloatArrayListTypeHandler extends BaseTypeHandler<ArrayList<Float>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Float> parameterList, JdbcType jdbcType) throws SQLException {
    StringBuilder str = new StringBuilder();
    str.append("{");
    for (int idx = 0; idx < parameterList.size(); idx++) {
      str.append(parameterList.get(idx)).append(",");
    }
    str.deleteCharAt(str.length() - 1);
    str.append("}");
    ps.setString(i, str.toString());
  }

  @Override
  public ArrayList<Float> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Array array = rs.getArray(columnName);
    ArrayList<Float> items = new ArrayList<>();

    if (!rs.wasNull()) {

      BigDecimal[] arrayItems = (BigDecimal[]) array.getArray();

      for (int i = 0; i < arrayItems.length; i++) {

        items.add(arrayItems[i].floatValue());

      }
      return items;
    } else {
      return null;
    }
  }

  @Override
  public ArrayList<Float> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return null;
  }

  @Override
  public ArrayList<Float> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return null;
  }

}
