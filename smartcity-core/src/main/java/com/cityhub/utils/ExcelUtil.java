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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cityhub.dto.ExcelVO;
import com.cityhub.dto.ExcelVO.ColumnRequired;
import com.cityhub.dto.ExcelVO.ColumnType;
import com.monitorjbl.xlsx.StreamingReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelUtil {
  
  /** 
   * 엑셀 파일 읽어오기
   * @param filePath
   * @param fileName
   */
  public static void readFile(String filePath, String fileName) {
    try {
      String p = filePath.substring(filePath.length() - 1, filePath.length());
      if (!"/".equals(p)) {
        filePath += "/";
      }
      int pos = fileName.lastIndexOf( "." );
      String ext = fileName.substring( pos + 1 );
      Workbook workbook = null;
      File f = new File(filePath + fileName);
      if (f.exists()) {
        if ("xls".equals(ext)) {
          FileInputStream is = new FileInputStream(f);
          workbook = new HSSFWorkbook(is);
        } else if ("xlsx".equals(ext)) {
          workbook = new XSSFWorkbook(f);
        } else if ("csv".equals(ext)) {
          //
        }
      } else {
        log.error("Not Found File!");
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * CSV 파싱
   * @param rowList
   * @param br
   * @param startRow
   * @param Arr
   */
  public static void parseCSV(List<Map<String, Object>> rowList, BufferedReader br, int startRow, String[][] Arr) {

    try {
      List<String> fix = new ArrayList<String>();
      for (int j = 0; j < Arr.length; j++) {
        if ("FIX".equals(Arr[j][2].toUpperCase())) {
          fix.add(Arr[j][1]);
        }
      }
      String line = "";
      if (startRow == 1) {
        br.readLine();
      }
      while ((line = br.readLine()) != null) {
        Map<String, Object> m = new HashMap<String, Object>();
        String[] token = line.split(",", -1);
        for (int c = 0; c < Arr.length; c++) {
          m.put(Arr[c][1], token[c]);
        }

        boolean result = true;
        for (int j = 0; j < fix.size(); j++) {
          if ("".equals(StrUtil.trim(m.get(fix.get(j) + "")))) {
            result = false;
            break;
          }
        }
        if (result) {
          rowList.add(m);
        }
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * CSV 파일 파싱
   * @param rowList
   * @param br
   * @param startRow
   * @param limitLine
   * @param Arr
   * @return
   */
  public static List<String> parseCSV(List<Map<String, Object>> rowList, BufferedReader br, int startRow, int limitLine, String[][] Arr) {
    List<String> errList = new LinkedList<>();
    try {
      String line = "";
      if (startRow == 1) {
        br.readLine();
        String[] token = line.split(",", -1);
        for (int cellIndex = 0; cellIndex < Arr.length; cellIndex++) {
          Arr[cellIndex][0] = token[cellIndex] + "";
        }
      }
      int CheckCount = 0;
      while ((line = br.readLine()) != null) {
        Map<String, Object> m = new HashMap<String, Object>();
        String[] token = line.split(",", -1);
        for (int c = 0; c < Arr.length; c++) {
          m.put(Arr[c][1], token[c]);
        }
        List<String> fix = new ArrayList<String>();
        List<String> headtitle = new ArrayList<String>();
        for (int i = 0; i < Arr.length; i++) {
          if ("FIX".equals(Arr[i][2].toUpperCase())) {
            fix.add(Arr[i][1]);
            headtitle.add(Arr[i][0]);
          }
        }

        boolean result = true;
        for (int j = 0; j < fix.size(); j++) {
          if ("".equals(StrUtil.trim(m.get(fix.get(j) + "")))) {
            if (!"".equals(StrUtil.trim(m.get(fix.get(0) + "")))) {
              String headName = "";
              if ("".equals(StrUtil.trim(headtitle.get(j) + "")) ) {
                headName = "빈  제목";
              } else {
                headName = StrUtil.trim(headtitle.get(j) + "");
              }
              CheckCount++;
              errList.add("컬럼 ( " + headName + " ) : 필수값입니다.");
            }
            result = false;
            break;
          }
        }
        if (result) {
          rowList.add(m);
        }
        if (CheckCount >= 10 || limitLine == CheckCount ) {
          return errList;
        }
      } // end while
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return errList;
  }


  /**
   * 엑셀파일 파싱
   * @param rowList
   * @param dataSheet
   * @param startRow
   * @param Arr
   * @return
   */
  public static List<String> parseExcel(List<Map<String, Object>> rowList, Sheet dataSheet, int startRow, String[][] Arr) {
    List<String> errList = new LinkedList<>();
    String[] ArrStruct = new String[] { "논리명칭", "물리명칭", "필수여부" };

    List<String> fix = new ArrayList<String>();
    List<String> headtitle = new ArrayList<String>();
    for (int i = 0; i < Arr.length; i++) {
      if ("FIX".equals(Arr[i][2].toUpperCase())) {
        fix.add(Arr[i][1]);
        headtitle.add(Arr[i][0]);
      }
    }

    int rows = dataSheet.getPhysicalNumberOfRows();
    for (int rowIndex = startRow; rowIndex < rows; rowIndex++) {
      Row row = dataSheet.getRow(rowIndex);
      if (row != null) {
        int cells = Arr.length;
        Map<String, Object> m = new HashMap<String, Object>();
        for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
          Object value = getCellText(row.getCell(cellIndex));
          m.put(Arr[cellIndex][1], value + ""); // 읽어온 엑셀데이터를 Map에 저장
        } // end for (cell)

        boolean result = true;
        for (int i = 0; i < fix.size(); i++) {
          if ("".equals(StrUtil.trim(m.get(fix.get(i) + "")))) {
            if (!"".equals(StrUtil.trim(m.get(fix.get(0) + "")))) {
              String headName = "";
              if ("".equals(StrUtil.trim(headtitle.get(i) + "")) ) {
                headName = "빈  제목";
              } else {
                headName = StrUtil.trim(headtitle.get(i) + "");
              }
              errList.add("행 :( " + rowIndex + " ), 컬럼 ( " + headName + " ) : 필수값입니다.");
            }
            result = false;
            break;
          }
        }
        if (result) {
          rowList.add(m);
        }
      } // end if (row != null)
    } // end for (row)
    return errList;
  }

  /**
   * 엑셀 파일 파싱
   * @param rowList
   * @param dataSheet
   * @param startRow
   * @param CheckCount
   * @param Arr
   * @return
   */
  public static List<String> parseExcel(List<Map<String, Object>> rowList, Sheet dataSheet, int startRow, int CheckCount, String[][] Arr) {
    List<String> errList = new LinkedList<>();
    String[] ArrStruct = new String[] { "논리명칭", "물리명칭", "필수여부" };
    int rows = 0 ;
    if (dataSheet.getPhysicalNumberOfRows() > (CheckCount + 100)) {
      rows = CheckCount + 100;
    } else {
      rows = dataSheet.getPhysicalNumberOfRows();
    }
    for (int rowIndex = startRow; rowIndex < rows; rowIndex++) {
      Row row = dataSheet.getRow(rowIndex);
      if (row != null) {
        int cells = Arr.length;
        Map<String, Object> m = new HashMap<String, Object>();
        for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
          Object value = getCellText(row.getCell(cellIndex));
          m.put(Arr[cellIndex][1], value + ""); // 읽어온 엑셀데이터를 Map에 저장
        } // end for (cell)
        List<String> fix = new ArrayList<String>();
        List<String> headtitle = new ArrayList<String>();
        for (int i = 0; i < Arr.length; i++) {
          if ("FIX".equals(Arr[i][2].toUpperCase())) {
            fix.add(Arr[i][1]);
            headtitle.add(Arr[i][0]);
          }
        }
        boolean result = true;
        for (int i = 0; i < fix.size(); i++) {
          if ("".equals(StrUtil.trim(m.get(fix.get(i) + "")))) {
            if (!"".equals(StrUtil.trim(m.get(fix.get(0) + "")))) {
              String headName = "";
              if ("".equals(StrUtil.trim(headtitle.get(i) + "")) ) {
                headName = "빈  제목";
              } else {
                headName = StrUtil.trim(headtitle.get(i) + "");
              }
              CheckCount++;
              errList.add("행 :( " + rowIndex + " ), 컬럼 ( " + headName + " ) : 필수값입니다.");
            }
            result = false;
            break;
          }
        }
        if (result) {
          rowList.add(m);
        }
        if (CheckCount == 10 || rowIndex == rows - 1) {
          return errList;
        }
      } // end if (row != null)
    } // end for (row)
    return errList;
  }

  /**
   * 엑섹파일 헤더 읽어오기
   * @param dataSheet
   * @param headerRow
   * @param Arr
   */
  public static void getHeaderExcel( Sheet dataSheet, int headerRow, String[][] Arr) {
    String[] ArrStruct = new String[] { "논리명칭", "물리명칭", "필수여부" };
    Row row = dataSheet.getRow(headerRow);
    int cells = Arr.length;
    for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
      Object value = getCellText(row.getCell(cellIndex));
      Arr[cellIndex][0] = value + "";
    }
  }

  /**
   * 배열 복사하기
   * @param original2
   * @return
   */
  public static String[][] deepArrayCopy(String[][] originalArray) {
    if (originalArray == null) {
      return null;
    }
    String[][] result = new String[originalArray.length][originalArray[0].length];
    for (int i = 0; i < originalArray.length; i++) {
      System.arraycopy(originalArray[i], 0, result[i], 0, originalArray[0].length);
    }

    return result;
  }
  /**
   * @param rowList - 정합성 통과한 목록
   * @param excelTemplate - Map구성
   * @param startRow - 시작행
   * @param path - 경로
   * @param filename - 파일명
   * @param optionSplitStr - CSV 파일 파싱할때 분리문자열 할당 디폴트는 컴마(,)
   * @return errorList - 필수값 오류 목록
   */
  public static List<Map<String, Object>> parseFile(List<Map<String, Object>> rowList, List<ExcelVO> excelTemplate, int startRow,String path, String filename, String... optionSplitStr) {
    List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
    // 필수 체크를 리스트
    List<String> requiredIdList = new ArrayList<String>();
    for (ExcelVO vo : excelTemplate ) {
      if (ColumnRequired.NOT_NULL  ==  vo.getColumnRequired() ) {
        requiredIdList.add(vo.getPhysicalId());
      }
    }
    path = path.lastIndexOf("/") !=  (path.length() - 1) ? path+= "/" : path;
    File f = new File(path + filename);
    log.debug("file exists: {}",f.exists());
    if (!f.exists()) {
      return errorList;
    }
    String ext = FilenameUtils.getExtension(filename).toLowerCase();

    if ("xls".equals(ext)) {
      try (FileInputStream is = new FileInputStream(f);
          Workbook workbook = new HSSFWorkbook(is);
          ) {
        Sheet dataSheet = workbook.getSheetAt(0);
        int rows = dataSheet.getPhysicalNumberOfRows();
        for (int rowIndex = startRow; rowIndex < rows; rowIndex++) {
          Row row = dataSheet.getRow(rowIndex);
          if (row != null) {
            int cells = excelTemplate.size();
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
              Object value = getCellText(row.getCell(cellIndex));
              ColumnType columnType = excelTemplate.get(cellIndex).getColumnType();
              castCellType(m , columnType, excelTemplate.get(cellIndex).getPhysicalId(), value);
            } // end for (cell)

            if (checkRequired(errorList, m , requiredIdList, rowIndex)) {
              rowList.add(m);
            }
          } // end if (row != null)
        } // end for (row)

      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    } else if ("xlsx".equals(ext)) {
      try (FileInputStream inputStream = new FileInputStream( f );
          Workbook workbook = StreamingReader.builder().rowCacheSize( 100 ).bufferSize( 4096 ).open( inputStream );) {
        Sheet sheet = workbook.getSheetAt(0);
        int h = 0;
        for ( Row row : sheet ) {
          if ( h >= startRow ) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            int cells = excelTemplate.size();
            for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
              Cell c = row.getCell(cellIndex);
              if (c == null) {
                m.put(excelTemplate.get(cellIndex).getPhysicalId(), "");
              } else {
                Object value = getCellText(c);
                ColumnType columnType = excelTemplate.get(cellIndex).getColumnType();
                castCellType(m , columnType, excelTemplate.get(cellIndex).getPhysicalId(), value);
              }
            }

            if (checkRequired(errorList, m , requiredIdList, row.getRowNum())) {
              rowList.add(m);
            }
          }
          h++;
        }
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    } else if ("csv".equals(ext)) {
      try (BufferedReader br = new BufferedReader(new FileReader(f)); ) {
        String line = "";
        for (int g = 0; g < startRow; g++) {
          br.readLine();
        }
        int rows = startRow;
        while ((line = br.readLine()) != null) {
          rows++;
          Map<String, Object> m = new LinkedHashMap<String, Object>();
          String[] token;
          if(optionSplitStr.length == 0) {
            token = line.split(",", -1);
          } else {
            token = line.split(optionSplitStr[0], -1);
          }
          for (int cellIndex = 0; cellIndex < excelTemplate.size(); cellIndex++) {
            String value = String.valueOf(token[cellIndex]);
            ColumnType columnType = excelTemplate.get(cellIndex).getColumnType();
            castCellType(m , columnType, excelTemplate.get(cellIndex).getPhysicalId(), value);
          }

          if (checkRequired(errorList, m , requiredIdList, rows)) {
            rowList.add(m);
          }
        }
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }
    return errorList;
  }

  /**
   * 셀의 값을 형변환
   * @param m
   * @param columnType
   * @param cellId
   * @param value
   */
  private static void castCellType(Map<String, Object> m , ColumnType columnType, String cellId, Object value) {
    try {
      if(columnType == ColumnType.DOUBLE) {
        if (value == null || "".equals(value)) {
          m.put(cellId, "");
        } else {
          //m.put(cellId, Double.valueOf(String.valueOf(Optional.ofNullable(StrUtil.trim(value)).orElse("0.0"))));
          m.put(cellId, Double.parseDouble(value.toString()));
        }
      } else if(columnType == ColumnType.INTEGER) {
        if (value == null || "".equals(value)) {
          m.put(cellId, "");
        } else {
          String val = value.toString().replaceAll("[^0-9.]", "");
          if (val.lastIndexOf(".") > -1) {
            val = val.substring(0, val.lastIndexOf("."));
          }
          String s = String.valueOf(Optional.ofNullable(StrUtil.trim(val)).orElse("0"));
          m.put(cellId, Integer.valueOf(s));
        }
      } else if(columnType == ColumnType.BOOLEAN) {
        if (value == null || "".equals(value)) {
          m.put(cellId, "");
        } else {
          m.put(cellId, Boolean.valueOf(String.valueOf(Optional.ofNullable(value).orElse("0"))));
        }
      } else {
        if (value == null || "".equals(value)) {
          m.put(cellId, "");
        } else {
          m.put(cellId, String.valueOf(value));
        }

      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

  }

  /**
   * 필수값 체크
   * @param errorList
   * @param m
   * @param requiredId
   * @param row
   * @return
   */
  private static boolean checkRequired(List<Map<String, Object>> errorList, Map<String, Object> m , List<String> requiredIdList, int row ) {
    boolean result = true;
    boolean err = false;
    String cellIds = "";
    for (String cellId : requiredIdList) {
      if ("".equals(String.valueOf(m.get(cellId))) ) {
        err = true;
        cellIds += cellId + ",";
        result = false;
      }
    }
    if (err) {
      m.put("error" ,"999");
      m.put("message" ,"Required Error : Row( " + row + " ), ColumnName( " + cellIds.substring(0, cellIds.length() - 1) + " )");
      errorList.add(m);
    }

    return result;
  }

  /**
   * 셀의 값을 가져오기
   * @param cell
   * @return
   */
  private static Object getCellText(Cell cell) {
    Object value = "";
    if (cell == null) {
      value = "";
    } else {
      try {

        if (cell.getCellType() == CellType.FORMULA) {
          value = cell.getNumericCellValue() + "";
        } else if (cell.getCellType() == CellType.NUMERIC) {
          if( DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);
          } else {
            value = String.valueOf(cell.getNumericCellValue());
          }
        } else if (cell.getCellType() == CellType.STRING) {
          value = cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BOOLEAN) {
          if (cell.getBooleanCellValue() == true) {
            value = "1";
          } else {
            value = "0";
          }
        } else if (cell.getCellType() == CellType.BLANK) {
          value = "";
        } else if (cell.getCellType() == CellType._NONE) {
          value = "";
        } else if (cell.getCellType() == CellType.ERROR) {
          value = cell.getErrorCellValue() + "";
        } else {
          value = cell.getStringCellValue();
        }
      } catch (Exception e) {
        value = "";
      }
    } // end if cell value
    return value;
  }

  /**
   * 엑셀파일의 셀 스트링 값 가져오기
   * @param n
   * @return
   */
  public static String getCellString(int n) {
    char[] buf = new char[(int) java.lang.Math.floor(java.lang.Math.log(25 * (n + 1)) / java.lang.Math.log(26))];
    for (int i = buf.length - 1; i >= 0; i--) {
      n--;
      buf[i] = (char) ('A' + n % 26);
      n /= 26;
    }
    return new String(buf);
  }


}