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
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeometryTypeHandler extends BaseTypeHandler<Object> {

  @Override
  public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {

    PGobject pGobject = (PGobject) rs.getObject(columnName);
    if (!rs.wasNull() && pGobject != null) {
      Geometry geometry = PGgeometry.geomFromString(pGobject.getValue());

      StringBuilder geoJsonBuilder = new StringBuilder();
      if (geometry instanceof Point) {
        geoJsonBuilder.append("{\"type\":\"Point\",\"coordinates\":[").append(geometry.getFirstPoint().getX()).append(",").append(geometry.getFirstPoint().getY()).append("]}");
      }
      Map<String, Object> result = null;
      try {
        ObjectMapper mapper = new ObjectMapper();
        result = mapper.readValue(geoJsonBuilder.toString(), new TypeReference<Map<String, Object>>() {
        });
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
      return result;
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
