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
package com.cityhub.utils;

public enum GradeType {
  verygood("매우좋음", 1, "Very Good"), good("좋음", 2, "Good"), normal("보통", 3, "Normal"), wrong("나쁨", 4, "Wrong"), verywrong("매우나쁨", 5, "Very Wrong");

  private int code;
  private String value;
  private String desc;

  GradeType(String value, int code, String desc) {
    this.code = code;
    this.value = value;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getValue() {
    return value;
  }

  public String getDesc() {
    return desc;
  }

  public static GradeType findBy(int code) {
    for (GradeType gt : GradeType.values()) {
      if (gt.code == code) {
        return gt;
      }
    }
    return null;
  }

  public static GradeType findBy(String value) {

    for (GradeType gt : GradeType.values()) {
      if (gt.value.equals(value)) {
        return gt;
      }
    }
    return null;
  }

}
