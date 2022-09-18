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

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.cityhub.environment.Constants;

public class ListTypeHandler extends BaseTypeHandler<Object> {

  SimpleDateFormat transFormat = new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT);

  @Override
  public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {

    Array array = rs.getArray(columnName);
    List<Object> items = new ArrayList<>();

    if (!rs.wasNull()) {

      Object[] arrayItems = (Object[]) array.getArray();

      Object item = new Object();
      for (int i = 0; i < arrayItems.length; i++) {

        item = arrayItems[i];
        if (item != null) {
          if (item instanceof java.sql.Timestamp) {
            item = transFormat.format(item);
          }
        }
        items.add(item);
      }

      return items;
    } else {
      return null;
    }
  }

  @Override
  public Object getNullableResult(ResultSet resultSet, int i) throws SQLException {
    return null;
  }

  @Override
  public Object getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
    return null;
  }
}
