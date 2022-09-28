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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {
  private File file = null;
  private String filename = "";
  private String fullPath = "";
  BufferedWriter out = null;

  public FileUtil() {

  }

  /**
   * 파일의 경로 세팅하기
   *
   * @param path
   * @throws Exception
   */
  public void setPath(String path) throws Exception {
    file = new File(path);
    if (!file.exists()) {
      file.mkdirs();
    }
    fullPath = path;
    if (log.isDebugEnabled()) {
      log.debug(fullPath);
    }
  }

  /**
   * 파일의 이름 세팅하기
   *
   * @param name
   * @throws Exception
   */
  public void setFile(String name) throws Exception {
    filename = name;
    if (log.isDebugEnabled()) {
      log.debug(filename);
    }
  }

  /**
   * 파일을 열기
   *
   * @throws Exception
   */
  public void open() throws Exception {
    open(true);
  }

  /**
   * 파일을 열기
   *
   * @param append
   * @throws Exception
   */
  public void open(boolean append) throws Exception {
    if ("".equals(fullPath)) {
      throw new Exception("Not Setting Directory");
    } else if ("".equals(filename)) {
      throw new Exception("Not Setting File Name");
    } else {
      out = new BufferedWriter(new FileWriter(fullPath + filename, append));
    }
  }

  /**
   * 파일에 내용 쓰기
   *
   * @param data
   * @throws Exception
   */
  public void write(String data) throws Exception {
    if (out != null) {
      out.write(data);
      out.newLine();
      out.flush();
    }
  }

  /**
   * 버퍼 flush
   *
   * @throws Exception
   */
  public void flush() throws Exception {
    if (out != null) {
      out.flush();
    } else {
      throw new Exception("Not Open FileUtil");
    }
  }

  /**
   * 새 라인 추가
   *
   * @throws Exception
   */
  public void newLine() throws Exception {
    if (out != null) {
      out.newLine();
      out.flush();
    } else {
      throw new Exception("Not Open FileUtil");
    }

  }

  /**
   * 파일 닫기
   *
   * @throws Exception
   */
  public void close() throws Exception {
    if (out != null) {
      out.close();
    }
  }

}
