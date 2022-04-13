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

public class KafkaStatusVO {

  private String eventType = "DATA_STORED";
  private String eventDataType;
  private String eventDataGroupId;
  private String eventTriggeredRequestId;
  private String eventDataPersonId;
  private String eventDataBeginTime;
  private String eventDataEndTime;
  private long eventDataTotalCount;
  private long requestedDataTotalCount;
  private String  eventDescription;

  public String toJson() {
    return new JSONObject(this).toString();
  }


  public String getEventType() {
    return eventType;
  }
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
  public String getEventDataType() {
    return eventDataType;
  }
  public void setEventDataType(String eventDataType) {
    this.eventDataType = eventDataType;
  }
  public String getEventDataGroupId() {
    return eventDataGroupId;
  }
  public void setEventDataGroupId(String eventDataGroupId) {
    this.eventDataGroupId = eventDataGroupId;
  }
  public String getEventTriggeredRequestId() {
    return eventTriggeredRequestId;
  }
  public void setEventTriggeredRequestId(String eventTriggeredRequestId) {
    this.eventTriggeredRequestId = eventTriggeredRequestId;
  }
  public String getEventDataBeginTime() {
    return eventDataBeginTime;
  }
  public void setEventDataBeginTime(String eventDataBeginTime) {
    this.eventDataBeginTime = eventDataBeginTime;
  }
  public String getEventDataEndTime() {
    return eventDataEndTime;
  }
  public void setEventDataEndTime(String eventDataEndTime) {
    this.eventDataEndTime = eventDataEndTime;
  }
  public long getEventDataTotalCount() {
    return eventDataTotalCount;
  }
  public void setEventDataTotalCount(long eventDataTotalCount) {
    this.eventDataTotalCount = eventDataTotalCount;
  }
  public String getEventDataPersonId() {
    return eventDataPersonId;
  }
  public void setEventDataPersonId(String eventDataPersonId) {
    this.eventDataPersonId = eventDataPersonId;
  }


  public long getRequestedDataTotalCount() {
    return requestedDataTotalCount;
  }


  public void setRequestedDataTotalCount(long requestedDataTotalCount) {
    this.requestedDataTotalCount = requestedDataTotalCount;
  }


  public String getEventDescription() {
    return eventDescription;
  }


  public void setEventDescription(String eventDescription) {
    this.eventDescription = eventDescription;
  }




} // end class