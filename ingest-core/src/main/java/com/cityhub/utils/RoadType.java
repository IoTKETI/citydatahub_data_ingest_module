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

public enum RoadType {
  no_information("정보없음", 0, "no information"), free_flowing("원활", 1, "free flowing"), slow("서행", 2, "slow"), congested("정체", 3, "congested");

  private int code;
  private String hanNm;
  private String engNm;

  RoadType(String hanNm, int code, String engNm) {
    this.code = code;
    this.hanNm = hanNm;
    this.engNm = engNm;
  }

  public int getCode() {
    return code;
  }

  public String getHanNm() {
    return hanNm;
  }

  public String getEngNm() {
    return engNm;
  }

  public static RoadType findBy(int code) {
    for (RoadType rt : RoadType.values()) {
      if (rt.code == code) {
        return rt;
      }
    }
    return null;
  }

  public static RoadType findBy(String hanNm) {
    for (RoadType rt : RoadType.values()) {
      if (rt.hanNm.equals(hanNm)) {
        return rt;
      }
    }
    return null;
  }

}
