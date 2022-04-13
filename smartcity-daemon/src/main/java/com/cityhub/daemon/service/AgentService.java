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
package com.cityhub.daemon.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ProcessBuilder.Redirect;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cityhub.daemon.dto.LogVO;
import com.cityhub.daemon.dto.LoggerObject;
import com.cityhub.utils.DateUtil;
import com.cityhub.utils.FileUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@Service
public class AgentService {

  @Value("${flume.log}")
  private String flumeLogPath;
  @Value("${flume.home}")
  private String flumeHomePath;
  @Value("${flume.conf}")
  private String flumeConfPath;


  /**
   * 읽어온 로그 파싱
   * @param lines
   * @return
   */
  public LogVO parseLog(String lines) {
    LogVO vo = null;
    try {

      vo = new LogVO();
      String[] arr = lines.split("`", -1);
      vo.setSourceName(arr[1]);
      vo.setPayload(lines);
      vo.setTimestamp(lines.substring(0,24).trim());
      vo.setType(arr[2]);
      vo.setStep(arr[3].split(";", -1)[0]);
      vo.setDesc(arr[3].split(";", -1)[1]);

      if (arr[4] != null && !"".equals(arr[4]) && !"{}".equals(arr[4]) ) {
        vo.setId(arr[4]);
      }
      if (arr.length > 5) {
        if (arr[5] != null && !"".equals(arr[5]) && !"{}".equals(arr[5]) ) {
          vo.setLength(arr[5]);
        }
      }
      if (arr.length > 6) {
        if (arr[6] != null && !"".equals(arr[6]) && !"{}".equals(arr[6]) ) {
          vo.setAdapterType(arr[6]);
        }
      }

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return vo;
  }


  /**
   * 아답터 아이디의 로그를 파일에서 읽어오기 
   * @param id
   * @param param
   * @return
   */
  public List<LogVO> getLinesMatchPatternInFile(String id,Map param) {
    String patternstr = "^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\,[0-9]{0,3} [a-zA-Z]{0,5}\\s{1,2}-\\s\\`" + id;

    List<LogVO> lines = new ArrayList<LogVO>();
    try (Scanner scanner = new Scanner(new File(flumeLogPath + "flume.log"))) {
      Pattern p = Pattern.compile(patternstr);

      while (scanner.hasNextLine()) {
        String l = scanner.nextLine();
        Matcher m = p.matcher(l);
        if (m.find() ) {
          if (param.get("searchDate")  != null) {
            // date is not null
            String logdate = l.substring(0,24).trim();

            if (DateUtil.isAfter(param.get("searchDate") + "" , logdate ) ) {
              lines.add(parseLog(l));
            }
          } else {
            // date is null
            lines.add(parseLog(l));
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return lines;
  }

  /**
   * 임의의 날짜 이후의 로그를 파일에서 읽어오기
   * @param param
   * @return
   */
  public List<LogVO> getAllLinesMatchPatternInFile(Map param) {
    String patternstr = "^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\,[0-9]{0,3} [a-zA-Z]{0,5}\\s{1,2}-\\s\\`";

    List<LogVO> lines = new ArrayList<LogVO>();
    try (Scanner scanner = new Scanner(new File(flumeLogPath + "flume.log"))) {
      Pattern p = Pattern.compile(patternstr);
      int limit = 500;
      int cnt = 0;
      while (scanner.hasNextLine()) {
        String l = scanner.nextLine();
        Matcher m = p.matcher(l);
        if (m.find() ) {
          if (param.get("searchDate")  != null) {
            // date is not null
            String logdate = l.substring(0,24).trim();
            if (DateUtil.isAfter(param.get("searchDate") + "" , logdate ) ) {
              cnt++;
              if (limit == cnt) {
                break;
              } else {
                lines.add(parseLog(l));
              }
            }
          } else {
            // date is null
            cnt++;
            if (limit == cnt) {
              break;
            } else {
              lines.add(parseLog(l));
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }

    return lines;
  }

  /**
   * 아답터 소스의 로그를 파일에서 읽어오기
   * @param param
   * @return
   */
  public LoggerObject tail(Map param) {

    RandomAccessFile file = null;
    StringBuilder logsb = new StringBuilder();
    long startPoint = 0;
    long endPoint = 0;
    String payload = "";
    try {
      String sourceName = param.get("sourceName") + "";
      long preEndPoint = param.get("preEndPoint") == null ? 0 : Long.parseLong(param.get("preEndPoint") + "");

      String patternstr = "^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\,[0-9]{0,3} [a-zA-Z]{0,5}\\s{1,2}-\\s\\`" + sourceName;
      Pattern p = Pattern.compile(patternstr);

      file = new RandomAccessFile(flumeLogPath + "flume.log", "r");
      endPoint = file.length();

      startPoint = preEndPoint > 0 ? preEndPoint : endPoint < 20000 ? 0 : endPoint - 20000;

      file.seek(startPoint);

      String str;
      while ((str = file.readLine()) != null) {
        String l = new String(str.getBytes("ISO-8859-1"), "UTF-8");
        Matcher m = p.matcher(l);
        if (m.find() ) {
          logsb.append(l);
          logsb.append("\n");
        }
        endPoint = file.getFilePointer();
        file.seek(endPoint);
      }

      payload = URLEncoder.encode(logsb.toString(), "utf-8").replaceAll("\\+", "%20");
    } catch (FileNotFoundException e) {
      logsb.append("File does not exist.");
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } catch (Exception e) {
      logsb.append("Sorry. An error has occurred.");
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    } finally {
      try {
        file.close();
      } catch (Exception e) {
        log.error("Exception : "+ExceptionUtils.getStackTrace(e));
      }
    }

    return new LoggerObject(endPoint, payload);
  }

  /**
   * 에이전트 시작, 중지, 재시작, 현재 상태를 관리
   * @param status
   * @param param
   * @return
   */
  public String manageAgent(String status, Map param) {
    String rtn = null;
    try {
      String id = param.get("id") + "";
      String filename = param.get("id") + ".conf";
      String cmd = "";

      if ("start".equals(status)) {
        // 시작
        if (getStatus(filename) == null) {
          cmd = flumeHomePath + "/bin/flume-ng agent -n " + id + " --conf " + flumeConfPath + " -f " + flumeConfPath + filename;
          log.debug("cmd::::::::::::::::::::::::::"+ cmd);
          String[] command = cmd.split(" ", -1);
          exec(command);
          rtn = status;
        } else {
          rtn = "Already running";
        }
      } else if ("stop".equals(status)) {
        // 중지
        if (getStatus(filename) == null) {
          rtn = "Already stop";
        } else {
          killAgent(filename);
          rtn = status;
        }

      } else if ("restart".equals(status)) {
        // 재시작
        if (getStatus(filename) != null) {
          killAgent(filename);
        }
        Thread.sleep((1000 * 10L));

        cmd = flumeHomePath + "/bin/flume-ng agent -n " + id + " --conf " + flumeConfPath + " -f " + flumeConfPath + filename;
        String[] command = cmd.split(" ", -1);
        exec(command);
        rtn = status;

      } else if ("status".equals(status)) {
        // 상태가져오기
        rtn = "running";
        if (getStatus(filename) == null) {
          rtn = "stop";
        }
      }

      if (log.isDebugEnabled()) {
        log.debug(cmd);
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return rtn;
  }


  /**
   * 에이전트의 설정파일을 관리
   * @param path
   * @param param
   * @return
   */
  public String manageAgentConfFile(String path, Map param) {
    try {
      Map<String, ?> body = (Map) param.get("body");
      Map<String, ?> agent = (Map) body.get(param.get("id") + "");

      FileUtil fu = new FileUtil();
      fu.setPath(path);
      fu.setFile(param.get("id") + ".conf");
      fu.open(false);

      for (Map.Entry<String, ?> elem : agent.entrySet()) {
        fu.write(param.get("id") + "." + elem.getKey() + " = " + elem.getValue().toString().replaceAll(",", " "));
      }
      fu.newLine();

      String[] types = {"sources", "channels", "sinks"};
      for (String type : types) {
        String[] typeArr = (agent.get(type) + "").split(" ", -1);
        for (String typeName : typeArr) {
          Map<String, ?> itms = (Map) body.get(typeName);
          for (Map.Entry<String, ?> elem : itms.entrySet()) {
            fu.write(param.get("id") + "." + type + "." + typeName + "." + elem.getKey() + " = " + elem.getValue());
          }
          fu.newLine();
        }
      }
     
      fu.newLine();
      fu.flush();
      fu.close();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  /**
   * 아답터의 설정파일을 관리
   * @param path
   * @param param
   * @return
   */
  public String manageAdapterConfFile(String path, Map param) {
    try {
      Map<String, ?> body = (Map) param.get("body");
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileUtil fu = new FileUtil();
      fu.setPath(path);
      fu.setFile(param.get("id") + ".conf");
      fu.open(false);
      fu.write(gson.toJson(body));
      fu.newLine();
      fu.flush();
      fu.close();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  /**
   * 모델의 설정파일을 관리
   * @param path
   * @param param
   * @return
   */
  public String manageModelConfFile(String path, Map param) {
    try {
      Map<String, ?> body = (Map) param.get("body");

      Gson gson = new GsonBuilder().setPrettyPrinting().create();


      FileUtil fu = new FileUtil();
      fu.setPath(path);
      fu.setFile(param.get("apdater_id") + ".template");
      fu.open(false);
      fu.write(gson.toJson(body));
      fu.flush();
      fu.close();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  /**
   * 모델의 유효성 관리
   * @param path
   * @param param
   * @return
   */
  @Deprecated
  public String manageValidationConfFile(String path, Map param) {
    try {

      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      FileUtil fu = new FileUtil();
      fu.setPath(path);
      fu.setFile(param.get("apdater_id") + ".valid");
      fu.open(false);

      if (param.get("body") instanceof Map) {
        fu.write(gson.toJson(param.get("body")));
      } else if (param.get("body") instanceof List) {
        fu.write(gson.toJson(param.get("body")));
      }

      fu.flush();
      fu.close();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  /**
   * mqtt 의 구독 파일 관리
   * @param path
   * @param param
   * @return
   */
  public String manageSubscribeFile(String path, Map param) {
    try {
      FileUtil fu = new FileUtil();
      fu.setPath(path);
      fu.setFile(param.get("topic") + ".csv");
      fu.open(false);

      List<Map<String,String>> items = (List) param.get("items");
      for (Map<String,String> m : items) {
        fu.write(param.get("ip") +","+ m.get("item").toString());
      }
      fu.flush();
      fu.close();

    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  /**
   * OS 판별
   * @return
   */
  public boolean isWindoowsOS() {
    return System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > 0 ? true : false;
  }

  /**
   * 아답터의 현재 상태 가져오기
   * @param filename
   * @return
   */
  public String getStatus(String filename) {
    String result = null;
    try {
      String[] cmd2 = {"/bin/sh", "-c", "ps -ef | grep '" + filename + "' | grep -v grep | awk '{print $1}' "};
      log.info("status:{}",Arrays.toString(cmd2));
      String pid = null;
      ProcessBuilder builder = new ProcessBuilder(cmd2);
      Process process = builder.start();
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while ((pid = input.readLine()) != null) {
        result = pid;
        if (log.isDebugEnabled()) {
          log.debug("process ID : {}", pid);
        }
      }
      log.info("result:{}",result);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return result;
  }

  /**
   * 아답터 죽이기
   * @param filename
   * @return
   */
  private String killAgent(String filename) {
    String result = null;
    try {
      String[] status = {"/bin/sh", "-c", "ps -ef | grep '" + filename + "' | grep -v grep | awk '{print $2}' "};

      ProcessBuilder builder = new ProcessBuilder(status);
      Process process = builder.start();
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String str = "";
      while ((str = input.readLine()) != null) {
        result = str;
      }
      boolean isNumeric =  result.matches("[+-]?\\d*(\\.\\d+)?");
      log.info("{},{}",result, isNumeric);
      String pid = "";
      if (isNumeric) {
        pid = "$2";
      } else {
        pid = "$1";
      }

      String[] cmd2 = {"/bin/sh", "-c", "ps -ef | grep '" + filename + "' | grep -v grep | awk '{print " + pid + "}' | xargs kill"};
      log.info("kill:{}", Arrays.toString(cmd2));

      builder = new ProcessBuilder(cmd2);
      process = builder.start();
      input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      str = "";
      while ((str = input.readLine()) != null) {
        result = str;
        if (log.isDebugEnabled()) {
          log.debug("str : {}", str);
        }
      }
      log.info("result:{}",result);
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return result;
  }


  /**
   * 쉘스크립트 실행기
   * @param command
   */
  private void exec(String[] command) {
    try {
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.redirectOutput(Redirect.INHERIT);
      builder.redirectError(Redirect.INHERIT);
      builder.start();
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * .java 파일 컴파일
   * @param param
   * @return
   */
  public String sourceCodeCompile(Map param) {
    String result = null;
    try {
      String body = (String) param.get("sourceCode");
      log.info("body : {}", body);
      String agentLib = flumeHomePath + "/plugins.d/agent/lib/";
      String flumeLib = flumeHomePath + "/lib/";
      String agentLibext = flumeHomePath + "/plugins.d/agent/libext/";

      log.info("agentLib : {}", agentLib);
      log.info("flumeLib : {}", flumeLib);
      log.info("agentLibext : {}", agentLibext);

      FileUtil fu = new FileUtil();
      fu.setPath(agentLib);
      fu.setFile(param.get("instance_id") + ".java");
      fu.open(false);
      fu.write(body);
      fu.flush();
      fu.close();

      log.info("javac" + " -d " + agentLib + " " + agentLib + param.get("instance_id") + ".java");
      log.info("process: {}", agentLib + "compile.sh", param.get("instance_id") + ".java");
      Process process = new ProcessBuilder(agentLib + "compile.sh", param.get("instance_id") + ".java").start();

      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader input2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      String msg = null;
      while ((msg = input.readLine()) != null) {
        result += msg + "\n";
        log.debug("process Message  : {}", msg);
      }
      while ((msg = input2.readLine()) != null) {
        result += msg + "\n";
        log.debug("Error Message : {}", msg);
      }
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return result;
  }

  /**
   * java 파일 읽어오기
   * @param param
   * @return
   */
  public String readJavaFile(Map param) {
    String instanceId = (String) param.get("instance_id");
    log.info("instanceId : {}", instanceId);
    String agentLib = flumeHomePath + "/plugins.d/agent/lib/";

    StringBuffer result = new StringBuffer("");
    try (Scanner scan = new Scanner(new File(agentLib + instanceId + ".java"))){
      while (scan.hasNextLine()) {
        result.append(scan.nextLine());
        result.append("\n");
      }
    } catch (FileNotFoundException e) {
      result.append("File Not Found!!");
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return result.toString();
  }


  /**
   * .java 파일 컴파일 여부
   * @param param
   * @return
   */
  public boolean isCompileJavaFile(Map param) {
    String instanceId = (String) param.get("instance_id");
    log.info("instanceId : {}", instanceId);
    String agentLib = flumeHomePath + "/plugins.d/agent/lib/";

    boolean result = false;
    StringBuffer str = new StringBuffer("");
    String classFile = "";
    try (Scanner scan = new Scanner(new File(agentLib + instanceId + ".java"))){
      while (scan.hasNextLine()) {
        String line = scan.nextLine();
        str.append(line);
        str.append("\n");
        if(line.indexOf("package") > -1) {
          String classPath = line.split(" ", -1)[1];
          classPath = classPath.replaceAll("\\." , "/").substring(0, classPath.length() - 1);
          classFile = agentLib + classPath + "/" + instanceId + ".class";
          result = isExistFile(classFile);
          if(log.isDebugEnabled()) {
            log.debug("instanceId : {}, {}", instanceId, classFile);
          }
          break;
        }
      }

      if (result == false && str.toString().contains("package") == false) {
        classFile = agentLib + instanceId + ".class";
        result = isExistFile(classFile);
        if(log.isDebugEnabled()) {
          log.debug("instanceId : {}, {}", instanceId, classFile);
        }
      }

    } catch (FileNotFoundException e) {
      result = false;
    } catch (Exception e) {
      log.error("Exception : "+ExceptionUtils.getStackTrace(e));
    }
    return result;
  }
  
  /** 
   * 파일 존재여부
   * @param filePath
   * @return
   */
  public boolean isExistFile(String filePath) {
    File f = new File(filePath);
    if (f.exists()) {
      return true;
    } else {
      return false;
    }

  }

}
