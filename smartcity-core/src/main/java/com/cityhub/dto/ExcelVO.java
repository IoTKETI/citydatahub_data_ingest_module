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
package com.cityhub.dto;


import java.util.List;

import lombok.Data;

@Data
public class ExcelVO {

  private String logicalId = "";
  private String physicalId = "";
  //private String required = "";
  private ColumnRequired columnRequired;
  private ColumnType columnType;

  public ExcelVO(String logicalId, String  physicalId, ColumnRequired  columnRequired, ColumnType  columnType){
    this.logicalId = logicalId;
    this.physicalId = physicalId;
    this.columnRequired = columnRequired;
    this.columnType = columnType;
  }
  public ExcelVO(String logicalId, String  physicalId, ColumnRequired  columnRequired){
    this.logicalId = logicalId;
    this.physicalId = physicalId;
    this.columnRequired = columnRequired;
    this.columnType = ColumnType.STRING;
  }

  /**
   * 카프카 EntityType
   * @author shaby
   *
   */
  public static enum ColumnRequired {
    NULL("NULL"),
    NOT_NULL("NOT_NULL");

    private String code;

    private ColumnRequired(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static ColumnRequired parseType(String code) {
      for (ColumnRequired columnRequired : values()) {
        if (columnRequired.getCode().equals(code.toUpperCase())) {
          return columnRequired;
        }
      }
      return null;
    }
  }
  /**
   * 카프카 EntityType
   * @author shaby
   *
   */
  public static enum ColumnType {
    INTEGER("INTEGER"),
    STRING("STRING"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN");

    private String code;

    private ColumnType(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static ColumnType parseType(String code) {
      for (ColumnType columnType : values()) {
        if (columnType.getCode().equals(code.toUpperCase())) {
          return columnType;
        }
      }
      return null;
    }
  }


public  enum BooleanType {
  TRUE("true", java.util.Arrays.asList("TRUE", "true", "1", "Y", "y")),
  FALSE("false", java.util.Arrays.asList("FALSE", "false", "0", "N", "n"));
  //EMPTY("", java.util.Collections.EMPTY_LIST) ;

  private String code;
  private List<String> list;

  BooleanType(String code, List<String> list) {
    this.code = code;
    this.list = list;
  }

  public String getCode() {
    return code;
  }
  public List<String> getList() {
    return list;
  }

  public static BooleanType findByCode(String code) {
    return java.util.Arrays.stream(BooleanType.values())
        .filter(booleanType -> booleanType.hasCode(code))
        .findAny()
        .orElse(FALSE);
  }
  public boolean hasCode(String  code) {
    return list.stream().anyMatch(bool -> bool.equals(code));
  }
}


} // end class



