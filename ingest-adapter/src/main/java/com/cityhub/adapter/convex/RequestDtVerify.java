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
package com.cityhub.adapter.convex;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestDtVerify {
  String personId;
  LocalDateTime requestStartDt;
  LocalDateTime requestEndDt;

  public RequestDtVerify(String personId, String requestStartDt, String requestEndDt) {
    this.personId = personId;
    this.requestStartDt = filteredDate(requestStartDt);
    this.requestEndDt = filteredDate(requestEndDt);
  }

  public RequestDtVerify(String personId, long requestStartDt, long requestEndDt) {
    this.personId = personId;
    this.requestStartDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(requestStartDt), ZoneId.systemDefault());
    this.requestEndDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(requestEndDt), ZoneId.systemDefault());
  }

  public RequestDtVerify(String personId, LocalDateTime requestStartDt, LocalDateTime requestEndDt) {
    this.personId = personId;
    this.requestStartDt = requestStartDt;
    this.requestEndDt = requestEndDt;
  }

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public LocalDateTime getRequestStartDt() {
    return requestStartDt;
  }

  public void setRequestStartDt(LocalDateTime requestStartDt) {
    this.requestStartDt = requestStartDt;
  }

  public LocalDateTime getRequestEndDt() {
    return requestEndDt;
  }

  public void setRequestEndDt(LocalDateTime requestEndDt) {
    this.requestEndDt = requestEndDt;
  }

  public boolean isWithinRange(String localdate) {
    LocalDateTime _date = filteredDate(localdate);
    return (!_date.isBefore(requestStartDt)) && (_date.isBefore(requestEndDt.plusDays(1)));
  }

  public boolean isWithinRange(LocalDateTime localdate) {
    LocalDateTime _date = localdate;
    return (!_date.isBefore(requestStartDt)) && (_date.isBefore(requestEndDt.plusDays(1)));
  }

  private LocalDateTime filteredDate(String st) {
    LocalDateTime _sdate = null;
    try {
      if (!"".equals(st)) {
        st = st.replaceAll("\\-", "");
        st = st.replaceAll("/", "");
        st = st.replaceAll("\"", "");
        st = st.replaceAll("'", "");
        st = st.replaceAll("\\/", "");
        st = st.replaceAll("T", " ");
        st = st.replaceAll(",", ".");
        if (st.lastIndexOf("+") > 0) {
          st = st.substring(0, st.lastIndexOf("+"));
          ;
        }
        if (st.length() == 8) {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
          _sdate = LocalDate.parse(st, formatter).atStartOfDay();
        } else {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd [HH][H][:mm][:ss][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]");
          _sdate = LocalDateTime.parse(st, formatter);
        }

      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return _sdate;
  }

}
