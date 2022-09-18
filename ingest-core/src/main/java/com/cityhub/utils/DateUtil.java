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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtil {
  public static DateTimeFormatter isoformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX");

  public static enum DateCode {
    IS_AFTER("C001"), IS_EQUAL("C002"), IS_BEFORE("C003"), IS_ETC("C999");

    private String code;

    private DateCode(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public static DateCode parseCode(String code) {
      for (DateCode entityType : values()) {
        if (entityType.getCode().equals(code)) {
          return entityType;
        }
      }
      return null;
    }
  }

  /**
   * <p>
   * <code>DateUtil.getTime()</code>
   * <p>
   * @return
   */
  public static String getTime() {
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    return OffsetDateTime.of(LocalDateTime.now(), zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();
  }

  /**
   * <p>
   * <code>DateUtil.getTime("yyyy-MM-dd HH:mm:ss")</code>
   * <p>
   * return value : 2018-12-12T17:18:00
   */
  public static String getTime(String pattern) {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
  }

  /**
   * LocalDateTime.now() -> Timestamp 컨버트
   *
   * <p>
   * return value : 2018-12-12T17:18:00
   */
  public static Timestamp getTimestamp() {
    return Timestamp.valueOf(LocalDateTime.now());
  }


  /**
   * <p>
   * time : 2018-12-12 17:18:00 , 2018-12-12 , 2018-12-12 01:20
   * <p>
   * <code>DateUtil.getISOTime("2018-12-12 17:18")</code>
   * <p>
   * return value : 2018-12-12T17:18:00
   */
  public static String getISOTime(String time) {
    String sDate = "";
    String atime = "";
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    try {
      LocalDateTime _date = null;
      if (time != null && !"".equals(time)) {
        atime = time.replaceAll("[^0-9]", "");

        if (atime.length() == 8) {
          _date = LocalDate.parse(atime, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
        } else if (atime.length() == 10) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHH"));
        } else if (atime.length() == 12) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        } else if (atime.length() == 14) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } else if (atime.length() > 14  && atime.length() <= 17 ) {
          DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
          _date = LocalDateTime.parse(atime, formatter);
        } else {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"));
        }
        sDate = OffsetDateTime.of(_date, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

      } else {
        sDate = null;
      }

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return sDate;
  }

  /**
   * <p>
   * time : 2018-12-12 17:18:00 , 2018-12-12 , 2018-12-12 01:20
   * <p>
   * <code>DateUtil.getISOTime("2018-12-12 17:18", "yyyy-MM-dd")</code>
   * <p>
   * return value : 2018-12-12T17:18:00
   */
  public static String getISOTime(String time, String pattern) {
    String sDate = "";
    try {
      time = time.replaceAll("[^0-9]", "");

      LocalDateTime _date = null;
      if (time.length() == 8) {
        _date = LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
        sDate = _date.format(DateTimeFormatter.ofPattern(pattern)).toString();
      } else if (time.length() == 10) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHH"));
        sDate = _date.format(DateTimeFormatter.ofPattern(pattern)).toString();
      } else if (time.length() == 12) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        sDate = _date.format(DateTimeFormatter.ofPattern(pattern)).toString();
      } else if (time.length() == 14) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        sDate = _date.format(DateTimeFormatter.ofPattern(pattern)).toString();
      } else if (time.length() == 17) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
        _date = LocalDateTime.parse(time, formatter);
        sDate = _date.format(DateTimeFormatter.ofPattern(pattern)).toString();
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return sDate;
  }
  
  /**
   * <p>
   * time : 20181212171800 , 20181212 , 201812120120
   * <p>
   * <code>DateUtil.getLocalDateTime("201812121718")</code>
   * <p>
   * return : LocalDateTime
   */
  public static LocalDateTime getLocalDateTime(String time) {
    LocalDateTime _date = null;
    String atime = "";
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    try {
      if (time != null && !"".equals(time)) {
        atime = time.replaceAll("[^0-9]", "");

        if (atime.length() == 8) {
          _date = LocalDate.parse(atime, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
        } else if (atime.length() == 10) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHH"));
        } else if (atime.length() == 12) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        } else if (atime.length() == 14) {
          _date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } else if (atime.length() > 14 && atime.length() <= 17) {
          DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
          _date = LocalDateTime.parse(atime, formatter);
        } else {
          DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.NANO_OF_SECOND, 6).toFormatter();
          _date = LocalDateTime.parse(atime, formatter);
          //_date = LocalDateTime.parse(atime, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS"));
        }
        //sDate = OffsetDateTime.of(_date, zo).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSSXXX")).toString();

      } else {
        //sDate = null;
      }

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return _date;
  }


  /**
   * 현재 시간 가져오기
   * @return
   */
  public static int getHour() {
    return LocalTime.now().getHour();
  }
  
  /**
   * 현재 분 가져오기
   * @return
   */
  public static int getMinute() {
    return LocalTime.now().getMinute();
  }

  /**
   * 현재 초 가져오기
   * @return
   */
  public static int getSecond() {
    return LocalTime.now().getSecond();
  }


  /**
   * <p>
   * day : +- Ingeter.
   * <p>
   * <code>DateUtil.addDay(-1)</code>
   */
  public static String addDay(int dayNumber) {
    return LocalDate.now().plus(dayNumber, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  /**
   * <p>
   * pattern : yyyyMMdd , yyyyMMdd HH:mm.
   * <p>
   * dateType : ChronoUnit.YEARS, ChronoUnit.MONTHS ,ChronoUnit.DAYS , ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS
   * <p>
   * num : +- Ingeter.
   *
   * <pre>
   * {@code
   *DateUtil.addDate(ChronoUnit.MINUTES, -40, "yyyyMMdd" )
   *DateUtil.addDate(ChronoUnit.MINUTES, -40, "HHmm" )
   * }
   * </pre>
   */
  public static String addDate(ChronoUnit dateType, int num, String pattern) {
    return LocalDateTime.now().plus(num, dateType).format(DateTimeFormatter.ofPattern(pattern));
  }

  /**
   * <p>
   * pattern : yyyyMMdd , yyyyMMdd HH:mm.
   * <p>
   * dateType : ChronoUnit.YEARS, ChronoUnit.MONTHS ,ChronoUnit.DAYS , ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS
   * <p>
   * num : +- Ingeter.
   *
   * <pre>
   * {@code
   *DateUtil.addDate("2018-06-06 18:10", ChronoUnit.MINUTES, -40, "yyyyMMdd" )
   *DateUtil.addDate("2018-06-06 18:10", ChronoUnit.MINUTES, -40, "HHmm" )
   * }
   * </pre>
   */
  public static String addDate(String time, ChronoUnit dateType, int num, String pattern) {
    LocalDateTime _date = null;
    try {
      time = time.replaceAll("[^0-9]", "");
      if (time.length() == 8) {
        _date = LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
      } else if (time.length() == 10) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHH"));
      } else if (time.length() == 12) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
      } else if (time.length() == 14) {
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      } else if (time.length() > 14) {
        time = time.substring(0, 14);
        _date = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return _date.plus(num, dateType).format(DateTimeFormatter.ofPattern(pattern)).toString();
  }

  public static ChronoUnit getChronoUnit(String str) {
    ChronoUnit cu = null;
    str = str.toUpperCase();
    switch (str) {
      case "YEAR":
        cu = ChronoUnit.YEARS;
        break;
      case "MONTH":
        cu = ChronoUnit.MONTHS;
        break;
      case "DAY":
        cu = ChronoUnit.DAYS;
        break;
      case "HOUR":
        cu = ChronoUnit.HOURS;
        break;
      case "MINUTE":
        cu = ChronoUnit.MINUTES;
        break;
      case "SECONDE":
        cu = ChronoUnit.SECONDS;
        break;
      default:
        cu = ChronoUnit.DAYS;
        break;
    }
    return cu;
  }

  /**
   * 입력 문자의 패턴 확인
   * @param str
   * @return
   */
  public static boolean isPatternDate(String str) {
    boolean bl = false;
    if ("yyyy-MM-dd".equals(str)) {
      bl = true;
    } else if ("yyyyMMdd".equals(str)) {
      bl = true;
    } else if ("HHmm".equals(str)) {
      bl = true;
    } else if ("HH:mm".equals(str)) {
      bl = true;
    } else if ("yyyyMMddHH".equals(str)) {
      bl = true;
    } else if ("yyyyMMddHHmm".equals(str)) {
      bl = true;
    }
    return bl;
  }

  /**
   * <p>
   * {@code DateUtil.getDate()}
   */
  public static String getDate() {
    return getDate("yyyyMMdd");
  }

  /**
   *
   * <p>
   * <code>DateUtil.getDate("yyyyMMdd HH:mm")</code>
   */
  public static String getDate(String pattern) {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
  }

  /**
   * 날짜비교
   * @param raw
   * @param prevTime
   * @return
   */
  public static int compareDate(String raw, long prevTime) {
    LocalDateTime _date = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    LocalDateTime comparedate = LocalDateTime.now().minus(prevTime, ChronoUnit.MINUTES);
    if (log.isDebugEnabled()) {
      //  log.debug("{} < {} = {}" , _date ,comparedate, comparedate.compareTo(_date));
    }
    return comparedate.compareTo(_date);
  }

  /**
   * 두 날자 사이이 비교
   * @param baseDate
   * @param compareDate
   * @return
   */
  public static DateCode compareTo(String baseDate, String compareDate) {
    LocalDateTime date1 = LocalDateTime.parse(DateUtil.getISOTime(baseDate, "yyyyMMddHHmmssSSS"),
        new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter());
    LocalDateTime date2 = LocalDateTime.parse(compareDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
    if (date2.isAfter(date1)) {
      return DateCode.IS_AFTER;
    } else if (date2.isEqual(date1)) {
      return DateCode.IS_EQUAL;
    } else if (date2.isBefore(date1)) {
      return DateCode.IS_BEFORE;
    } else {
      return DateCode.IS_ETC;
    }
  }

  
  /**
   * 두 날짜 사이의 비교 이후
   * @param baseDate
   * @param compareDate
   * @return
   */
  public static boolean isAfter(String baseDate, String compareDate) {
    LocalDateTime bDate = LocalDateTime.parse(DateUtil.getISOTime(baseDate, "yyyyMMddHHmmssSSS"),
        new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter());
    LocalDateTime cDate = LocalDateTime.parse(compareDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
    return cDate.isAfter(bDate);
  }
  
  /** 
   * 두 날짜 사이의 비교 이전
   * @param baseDate
   * @param compareDate
   * @return
   */
  public static boolean isBefore(String baseDate, String compareDate) {
    LocalDateTime bDate = LocalDateTime.parse(DateUtil.getISOTime(baseDate, "yyyyMMddHHmmssSSS"),
        new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter());
    LocalDateTime cDate = LocalDateTime.parse(compareDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
    return cDate.isBefore(bDate);
  }
  
  /** 
   * 두 날짜가 같은지 비교
   * @param baseDate
   * @param compareDate
   * @return
   */
  public static boolean isEqual(String baseDate, String compareDate) {
    LocalDateTime bDate = LocalDateTime.parse(DateUtil.getISOTime(baseDate, "yyyyMMddHHmmssSSS"),
        new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter());
    LocalDateTime cDate = LocalDateTime.parse(compareDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
    return cDate.isEqual(bDate);
  }


  /**
   * ISO 시간을 변환
   * @param isoTime
   * @return yyyy-MM-dd HH:mm:ss,SSS
   */
  public static String isoTimeToDataTime(String isoTime) {
    DateTimeFormatter format = new DateTimeFormatterBuilder()
        // date/time
        .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSS"))
        // offset (hh:mm - "+00:00" when it's zero)
        .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
        // offset (hhmm - "+0000" when it's zero)
        .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
        // offset (hh - "Z" when it's zero)
        .optionalStart().appendOffset("+HH", "Z").optionalEnd()
        // create formatter
        .toFormatter();
    OffsetDateTime odt = OffsetDateTime.parse( isoTime, format);
    DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss,").appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
    return odt.format(formatter);
  }
  
  /** 
   * 입력 받은 날짜 형식을 LocalDateTime 로 파싱
   * @param st
   * @return
   */
  public static LocalDateTime filteredDate(String st) {
    LocalDateTime _sdate = null;
    if (!"".equals(st)) {
      st = st.replaceAll("\\-", "");
      st = st.replaceAll("/", "");
      st = st.replaceAll("\"", "");
      st = st.replaceAll("'", "");
      st = st.replaceAll("\\/", "");
      st = st.replaceAll("T", " ");
      st = st.replaceAll(",", ".");
      if (st.lastIndexOf("+") > 0) {
        st = st.substring(0, st.lastIndexOf("+"));;
      }
      if (st.length() == 8 ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        _sdate = LocalDate.parse(st, formatter).atStartOfDay();;
      } else {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd [HH][H][:mm][:ss][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]");
        _sdate = LocalDateTime.parse(st, formatter);
      }
    }
    return _sdate;
  }

  /**
   * 입력받은 LocalDateTime 을 OffsetDateTime으로 변환
   * @param _sdate
   * @return
   */
  public static String convertOffsetDateTime(LocalDateTime _sdate) {
    ZoneOffset zo = ZonedDateTime.now().getOffset();
    return OffsetDateTime.of(_sdate, zo).format(isoformatter).toString();
  }
  
  /**
   * 입력 받은 LocalDateTime을 EpochMillis 로 변환
   * @param _sdate
   * @return
   */
  public static long getEpochMillis(LocalDateTime _sdate) {
    return _sdate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
  /**
   * 입력 받은 문자열(날짜형식) 을 EpochMillis 로 변환
   * @param st
   * @return
   */
  public static long getEpochMillis(String st) {
    LocalDateTime _sdate = DateUtil.filteredDate(st);
    return _sdate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
  
  /**
   * 입력 받은 날짜가 오류가 났을 경우 replaceDate 로 치환
   * @param date
   * @param replaceDate
   * @return
   */
  public static String getTime2Replace(String date, String replaceDate) {
    String resultDate = "";
    if (!"".equals(date)) {
      try {
        resultDate = DateUtil.convertOffsetDateTime(DateUtil.filteredDate(date));
      } catch (Exception e) {
        resultDate = replaceDate;
      }
    } else {
      resultDate = replaceDate;
    }
    return resultDate;
  }

} // end class
