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

public class Cor19FileInfoVO {
  private String modelId;
  private String requestId;
  private String personId;
  private String networkType = "";
  private String uploadTime;
  private String fileName;
  private String fileSeq;
  private String filePath;

  public Cor19FileInfoVO() {

  }

  public Cor19FileInfoVO(String filePath, String fileName) {
    filePath = filePath.lastIndexOf("/") !=  (filePath.length() - 1) ? filePath+= "/" : filePath;
    this.filePath = filePath;
    this.fileName = fileName;
    this.parseFileName(fileName);
  }

  public void parseFileName(String fileName) {
    this.fileName = fileName;
    String[] fArr = fileName.split("_", -1);

    this.modelId = fArr[0];
    this.requestId = fArr[1];
    /*
    this.personId = fArr[2];
    this.networkType = fArr[3];
    this.uploadTime = fArr[4];
    if (fArr.length > 5) {
      this.fileSeq = fArr[5];
    }
     */
  }


  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }



} // end class