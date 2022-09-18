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

import org.json.JSONObject;

public class VerifyStatusVO {
  private String groupId;
  private String dataType;
  private String requestInfoId;
  private String personid;
  private String observedTime;

  private String uniqueId;
  private String marking;
  private String jsonMsg;

  public String toJson() {
    return new JSONObject(this).toString();
  }

  public VerifyStatusVO(String groupId,String dataType , String uniqueId, String requestInfoId, String personid, String observedTime, String jsonMsg) {
    this.groupId = groupId;
    this.dataType = dataType;
    this.requestInfoId = requestInfoId;
    this.personid = personid;
    this.uniqueId = uniqueId;
    this.observedTime = observedTime;
    this.jsonMsg = jsonMsg;
  }

  public String getMarking() {
    return marking;
  }

  public void setMarking(String marking) {
    this.marking = marking;
  }


  public String getPersonid() {
    return personid;
  }

  public void setPersonid(String personid) {
    this.personid = personid;
  }

  public String getRequestInfoId() {
    return requestInfoId;
  }

  public void setRequestInfoId(String requestInfoId) {
    this.requestInfoId = requestInfoId;
  }

  public String getObservedTime() {
    return observedTime;
  }

  public void setObservedTime(String observedTime) {
    this.observedTime = observedTime;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getJsonMsg() {
    return jsonMsg;
  }

  public void setJsonMsg(String jsonMsg) {
    this.jsonMsg = jsonMsg;
  }




} // end class