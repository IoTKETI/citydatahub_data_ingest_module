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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.cityhub.environment.Constants;
import com.cityhub.utils.DataCoreCode.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Slf4j
public class JsonUtil {

  private JSONObject _jsonObject;

  public JsonUtil() {
  }

  public JsonUtil(JSONObject jsObj) {
    _jsonObject = jsObj;
  }

  public JsonUtil(String jsonStr) {
    if (jsonStr.startsWith("[")) {
      _jsonObject = new JSONArray(jsonStr).getJSONObject(0);
    } else if (jsonStr.startsWith("{")) {
      _jsonObject = new JSONObject(jsonStr);
    }
  }

  @Override
  public String toString() {
    return _jsonObject.toString();
  }
  public Map toMap() {
    return _jsonObject.toMap();
  }


  public void setStrToObject(String strBody) {
    _jsonObject = new JSONObject(strBody);
  }

  public void setFileObject(String fileName) {
    _jsonObject = new JSONObject(getFile(fileName));
  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>JsonEx.has(JsonObject, "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static boolean has(JSONObject jsObj, String ids) {
    boolean rtnValue = false;

    try {
      String[] id = ids.split("\\.");
      int len = id.length;
      JSONObject rtnObj = new JSONObject();
      if (len == 1) {
        if (jsObj.has(id[len - 1])) {
          rtnValue = true;
        }
      } else {
        for (int i = 0; i < len; i++) {
          if (i == 0) {
            if (jsObj.has(id[i]) == false) {
              rtnValue = false;
              break;
            }
            rtnObj = jsObj.getJSONObject(id[i]);

          } else if (i == len - 1) {
            if (rtnObj.has(id[len - 1])) {
              rtnValue = true;
            }
          } else {
            if (rtnObj.has(id[i]) == false) {
              rtnValue = false;
              break;
            }
            rtnObj = rtnObj.getJSONObject(id[i]);
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnValue;
  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>JsonEx je = new JsonEx(jsonString);
   * je.has( "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public boolean has(String ids) {
    return has(_jsonObject, ids);

  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>JsonEx.has(jsonString, "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static boolean has(String str, String ids) {
    return has(new JSONObject(str), ids);
  }

  public static String get(JSONObject jobj, String ids) {
    String rtnValue = "";
    try {
      Object rtnObj = getObjectFindLoop(jobj, ids);
      if (rtnObj instanceof JSONObject) {
        rtnValue = rtnObj + "";
      } else if (rtnObj instanceof JSONArray) {
        rtnValue = rtnObj + "";
      } else {
        rtnValue = rtnObj + "";
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return StrUtil.nvl(rtnValue);
  }

  public String get(String ids) {
    return get(_jsonObject, ids);
  }

  public static String get(String str, String ids) {
    return get(new JSONObject(str), ids);
  }

  public Object getObj(String ids) {
    try {
      return getObjectFindLoop(_jsonObject, ids);
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  public static Object getObj(Object jo, String ids) {
    Object rtnValue = null;
    try {

      if (jo instanceof JSONObject) {
        rtnValue = getObjectFindLoop((JSONObject) jo, ids);
      } else if (jo instanceof JSONArray) {
        ArrayList<Object> rtnlist = new ArrayList<>();
        for (Object obj : (JSONArray) jo) {
          rtnlist.add(getObjectFindLoop((JSONObject) obj, ids));
        }
        rtnValue = rtnlist;
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return rtnValue;
  }

  public static Object getObj(String str, String ids) {
    Object rtnValue = null;
    try {
      Object jo = null;
      if (str.startsWith("[")) {
        jo = new JSONArray(str);
      } else if (str.startsWith("{")) {
        jo = new JSONObject(str);
      }

      if (jo instanceof JSONObject) {
        rtnValue = getObjectFindLoop((JSONObject) jo, ids);
      } else if (jo instanceof JSONArray) {
        ArrayList<Object> rtnlist = new ArrayList<>();
        for (Object obj : (JSONArray) jo) {
          rtnlist.add(getObjectFindLoop((JSONObject) obj, ids));
        }
        rtnValue = rtnlist;
      }

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));

    }
    return rtnValue;
  }

  private static Object getObjectFindLoop(JSONObject jobj, String ids) throws Exception {

    Object rtnValue = null;
    JSONObject rtnObj = new JSONObject();

    String[] id = ids.split("\\.");
    int len = id.length;

    if (len == 1) {
      String _id = id[len - 1];
      if (_id.startsWith("[") == true) {
        String __id = _id.substring(0, _id.indexOf("["));
        if (rtnObj.get(__id) instanceof JSONArray) {
          int _arridx = Integer.parseInt(_id.substring(_id.indexOf("[") + 1, _id.indexOf("]")));
          JSONArray ja = (JSONArray) rtnObj.get(__id);
          if (ja.length() <= _arridx) {
            throw new Exception("Array Size Over \nMax Size: " + ja.length() + " , Input Value : " + _arridx);
          }
          rtnValue = ja.getJSONObject(_arridx);
        }
      } else {
        if (jobj.get(_id) instanceof JSONObject) {
          rtnValue = jobj.getJSONObject(_id);
        } else if (jobj.get(_id) instanceof JSONArray) {
          rtnValue = jobj.getJSONArray(_id);
        } else {
          rtnValue = jobj.get(_id);
        }
      }

    } else {
      for (int i = 0; i < len; i++) {
        if (i == 0) {
          rtnObj = jobj.getJSONObject(id[i]);
        } else if (i == len - 1) {
          String _id = id[len - 1];
          if (_id.startsWith("[") == true) {
            String __id = _id.substring(0, _id.indexOf("["));
            if (rtnObj.get(__id) instanceof JSONArray) {
              int _arridx = Integer.parseInt(_id.substring(_id.indexOf("[") + 1, _id.indexOf("]")));
              JSONArray ja = (JSONArray) rtnObj.get(__id);
              if (ja.length() <= _arridx) {
                throw new Exception("Array Size Over \nMax Size: " + ja.length() + " , Input Value : " + _arridx);
              }
              if (ja.get(_arridx) instanceof JSONObject) {
                rtnValue = ja.getJSONObject(_arridx);
              } else if (ja.get(_arridx) instanceof JSONArray) {
                rtnValue = ja.getJSONArray(_arridx);
              } else {
                rtnValue = ja.get(_arridx);
              }
            }
          } else {
            if (rtnObj.get(_id) instanceof JSONObject) {
              rtnValue = rtnObj.getJSONObject(_id);
            } else if (rtnObj.get(_id) instanceof JSONArray) {
              rtnValue = rtnObj.getJSONArray(_id);
            } else {
              rtnValue = rtnObj.get(_id);
            }
          }
        } else {
          String _id = id[i];
          if (_id.startsWith("[") == true) {
            String __id = _id.substring(0, _id.indexOf("["));
            if (rtnObj.get(__id) instanceof JSONArray) {
              int _arridx = Integer.parseInt(_id.substring(_id.indexOf("[") + 1, _id.indexOf("]")));
              JSONArray ja = (JSONArray) rtnObj.get(__id);
              if (ja.length() <= _arridx) {
                throw new Exception("Array Size Over \nMax Size: " + ja.length() + " , Input Value : " + _arridx);
              }
              rtnObj = ja.getJSONObject(_arridx);
            }
          } else {
            if (rtnObj.get(_id) instanceof JSONObject) {
              rtnObj = rtnObj.getJSONObject(_id);
            } else if (rtnObj.get(_id) instanceof JSONArray) {
              throw new Exception("A JSONArray text must start with '['");
            } else {
              rtnObj = rtnObj.getJSONObject(_id);
            }
          }
        }
      }
    }

    return rtnValue;
  }

  /**
   * <p>
   *
   * <pre>
   * <code>JSONObject jobj = JsonEx.getObject(jsonObject, "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static JSONObject getObject(JSONObject jobj, String ids) {
    JSONObject rtnObj = new JSONObject();
    try {
      String[] id = ids.split("\\.");
      int len = id.length;

      for (int i = 0; i < len; i++) {
        if (i == 0) {
          rtnObj = jobj.getJSONObject(id[i]);
        } else {
          rtnObj = rtnObj.getJSONObject(id[i]);
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));

    }

    return rtnObj;
  }

  /**
   * <p>
   *
   * <pre>
   * <code>
   * JsonEx je = new JsonEx(jsonObject);
   * JSONObject jobj = je.getObject("location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public JSONObject getObject(String ids) {
    return getObject(_jsonObject, ids);

  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>JSONObject jobj = JsonEx.getObject(jsonString, "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static JSONObject getObject(String str, String ids) {
    return getObject(new JSONObject(str), ids);
  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>
   * JSONArray jarr = JsonEx.getArray(JsonObject , "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static JSONArray getArray(JSONObject jobj, String ids) {
    JSONArray rtnAry = new JSONArray();
    try {
      String[] id = ids.split("\\.");
      int len = id.length;

      JSONObject rtnObj = new JSONObject();
      if (len == 1) {
        rtnAry = jobj.getJSONArray(id[len - 1]);
      } else {
        for (int i = 0; i < len; i++) {
          if (i == 0) {
            rtnObj = jobj.getJSONObject(id[i]);
          } else if (i == len - 1) {
            rtnAry = rtnObj.getJSONArray(id[len - 1]);
          } else {
            rtnObj = rtnObj.getJSONObject(id[i]);
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }

    return rtnAry;
  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>
   * JsonEx je = new JsonEx(jsonObject);
   * JSONArray jarr = je.getArray("location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public JSONArray getArray(String ids) {
    return getArray(_jsonObject, ids);

  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>
   * JSONArray jarr = JsonEx.getArray(JsonString, "location.value.coordinates")</code>
   * </pre>
   * <p>
   */
  public static JSONArray getArray(String jsonString, String ids) {
    return getArray(new JSONObject(jsonString), ids);
  }

  private static void puts(JSONObject jobj, String ids, Object value) {
    JSONObject rtnObj = new JSONObject();
    try {
      String[] id = ids.split("\\.", -1);
//      log.info("@@ids:{},{}, {}",ids,id, id.length);
      int len = id.length;
      if (len == 1) {
        if (value instanceof JSONArray) {
          jobj.put(id[len - 1], value);
        } else if (value instanceof JSONObject) {
          jobj.put(id[len - 1], value);
        } else {
          jobj.put(id[len - 1], value);
        }
      } else {
        for (int i = 0; i < len; i++) {
          if (i == 0) {

//            log.info("@@id:{},{}",id[0], jobj);
            rtnObj = jobj.getJSONObject(id[0]);
          } else if (i == len - 1) {
            String _id = id[len - 1];
            if (_id.startsWith("[") == true) {
              String __id = _id.substring(0, _id.indexOf("["));
              if (rtnObj.get(__id) instanceof JSONArray) {
                int _arridx = Integer.parseInt(_id.substring(_id.indexOf("[") + 1, _id.indexOf("]")));
                JSONArray ja = (JSONArray) rtnObj.get(__id);
                if (ja.length() <= _arridx) {
                  throw new Exception("Array Size Over \nMax Size: " + ja.length() + " , Input Value : " + _arridx);
                }
                if (value instanceof JSONArray) {
                  rtnObj.getJSONArray(__id).put(_arridx, value);
                } else if (value instanceof JSONObject) {
                  rtnObj.getJSONArray(__id).put(_arridx, value);
                } else {
                  rtnObj.getJSONArray(__id).put(_arridx, value);
                }
              }
            } else {
              if (value instanceof JSONArray) {
                rtnObj.put(id[len - 1], value);
              } else if (value instanceof JSONObject) {
                rtnObj.put(id[len - 1], value);
              } else {
                rtnObj.put(id[len - 1], value);
              }
            }
          } else {
            String _id = id[i];
            if (_id.startsWith("[") == true) {
              String __id = _id.substring(0, _id.indexOf("["));
              if (rtnObj.get(__id) instanceof JSONArray) {
                int _arridx = Integer.parseInt(_id.substring(_id.indexOf("[") + 1, _id.indexOf("]")));
                JSONArray ja = (JSONArray) rtnObj.get(__id);
                if (ja.length() <= _arridx) {
                  throw new Exception("Array Size Over \nMax Size: " + ja.length() + " , Input Value : " + _arridx);
                }
                rtnObj = ja.getJSONObject(_arridx);
              }
            } else {
              if (rtnObj.get(_id) instanceof JSONObject) {
                rtnObj = rtnObj.getJSONObject(_id);
              } else if (rtnObj.get(_id) instanceof JSONArray) {
                throw new Exception("A JSONArray text must start with '['");
              } else {
                rtnObj = rtnObj.getJSONObject(_id);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  private void puts(String ids, Object value) {
    puts(_jsonObject, ids, value);
  }

  public void put(String ids, int value) {
    this.puts(ids, value);
  }

  public void put(String ids, float value) {
    this.puts(ids, value);
  }

  public void put(String ids, long value) {
    this.puts(ids, value);
  }

  public void put(String ids, double value) {
    this.puts(ids, value);
  }

  public void put(String ids, String value) {
    this.puts(ids, value);
  }

  public void put(String ids, JSONObject value) {
    this.puts(ids, value);
  }

  public void put(String ids, JSONArray value) {
    this.puts(ids, value);
  }

  public void put(String ids, Object value) {
    this.puts(ids, value);
  }

  public static void put(JSONObject jobj, String ids, int value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, float value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, long value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, double value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, boolean value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, String value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, Object value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, JSONObject value) {
    puts(jobj, ids, value);
  }

  public static void put(JSONObject jobj, String ids, JSONArray value) {
    puts(jobj, ids, value);
  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>
   * JsonEx je = new JsonEx(jsonObject);
   * je.remove("location.value.coordinates")
   * je.remove("location")</code>
   * </pre>
   * <p>
   */
  public void remove(String ids) {
    remove(_jsonObject, ids);

  }

  /**
   *
   * <p>
   *
   * <pre>
   * <code>JsonEx.remove(jsonObject , "location.value.coordinates")</code>
   * <code>JsonEx.remove(jsonObject , "location")</code>
   * </pre>
   * <p>
   */
  public static void remove(JSONObject jobj, String ids) {
    String[] id = ids.split("\\.");
    int len = id.length;
    JSONObject rtnObj = new JSONObject();
    try {
      if (len == 1) {
        if (jobj.has(id[len - 1])) {
          jobj.remove(id[len - 1]);
        }
      } else {
        for (int i = 0; i < len; i++) {

          if (i == 0) {

            if (id[i].contains("[")) {
              int idx = Integer.parseInt(id[i].substring(id[i].indexOf("[") + 1, id[i].indexOf("]")));
              rtnObj = jobj.getJSONArray(id[i].substring(0, id[i].indexOf("["))).getJSONObject(idx);
            } else {
              rtnObj = jobj.getJSONObject(id[i]);
            }
          } else if (i == len - 1) {
            if (rtnObj.has(id[len - 1])) {
              rtnObj.remove(id[len - 1]);
            }
          } else {
            if (id[i].contains("[")) {
              int idx = Integer.parseInt(id[i].substring(id[i].indexOf("[") + 1, id[i].indexOf("]")));
              rtnObj = rtnObj.getJSONArray(id[i].substring(0, id[i].indexOf("["))).getJSONObject(idx);
            } else {
              rtnObj = rtnObj.getJSONObject(id[i]);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  public String getFile(String fileName) {
    String templatePath = new CommonUtil().getJarPath();

    StringBuffer result = new StringBuffer("");
    File file = new File(templatePath + fileName);
    log.info("+++++++++++++++{},{},{}", templatePath, fileName, file.exists());
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line);
      }
      scanner.close();
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    log.info("+++++++++++++++{}", result.toString());
    return result.toString();
  }

  public JSONObject getFileJsonObject(String fileName) {
    return new JSONObject(this.getFile(fileName));
  }

  public JSONArray getFileJsonArray(String fileName) {
    return new JSONArray(this.getFile(fileName));
  }

  /**
   *
   * <pre>
   * <code>JsonEx.removes(jsonObject , new String() {"item1","item2","item3"})</code>
   * <code>JsonEx.removes(jsonObject , "item1,item2,item3,item4".split(","))</code>
   * </pre>
   */
  public static void removes(JSONObject json, String[] removeItems) {
    try {
      for (String item : removeItems) {
        if (!"".equals(item)) {
          remove(json, item);
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   *
   * <pre>
   * example:
   * List list = new ArrayList();
   * list.add("item1");
   * list.add("item2");
   * list.add("item3");
   * JsonEx.removes(jsonObject , list)
   * </pre>
   */
  public static void removes(JSONObject json, List<String> removeItems) {
    try {
      for (String item : removeItems) {
        if (!"".equals(item)) {
          remove(json, item);
        }
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  public static void removeNullItem(JSONObject rawdata, String[] searchKey, List<String> rmKeys) {
    JsonUtil jEx = new JsonUtil(rawdata);
    for (String k : searchKey) {
      Object object = jEx.getObj(k);
      if (object instanceof JSONArray) {
        JSONArray rmArr = jEx.getArray(k);
        int arrIdx = 0;
        for (Object obj : rmArr) {
          JSONObject rm = (JSONObject) obj;
          for (String key : rm.keySet()) {
            if (rm.get(key) == JSONObject.NULL) {
              rmKeys.add(k + "[" + arrIdx + "]." + key);
            }
          }
          arrIdx++;
        }
      } else if (object instanceof JSONObject) {
        JSONObject rm = jEx.getObject(k);

        for (String key : rm.keySet()) {
          if ("value".equals(key)) {
            if (rm.get(key) == JSONObject.NULL) {
              rmKeys.add(k);
            }
          } else {
            if (rm.get(key) == JSONObject.NULL) {
              rmKeys.add(k + "." + key);
            }
          }

        }
      } else {
        if (object == JSONObject.NULL) {
          rmKeys.add(k);
        }
      }
    }

    for (String s : rmKeys) {
      jEx.remove(s);
    }

  }

  public static void removeNullItem(JSONObject rawdata, String searchKey, List<String> rmKeys) {
    removeNullItem(rawdata, new String[] { searchKey }, rmKeys);
  }

  /**
   * provides check for verifying if this element is an array or not.
   */
  public static boolean isJsonArray(Object obj) {
    return obj instanceof JSONArray;
  }

  public static boolean isJsonArray(JSONObject json, String ids) {
    return isJsonArray(json.toString(), ids);
  }

  public static boolean isJsonArray(String str, String ids) {
    return getObj(str, ids) instanceof JSONArray;
  }

  /**
   * provides check for verifying if this element is a Json object or not.
   */
  public static boolean isJsonObject(Object obj) {
    return obj instanceof JSONObject;
  }

  public static boolean isJsonObject(JSONObject json, String ids) {
    return isJsonObject(json.toString(), ids);
  }

  public static boolean isJsonObject(String str, String ids) {
    return getObj(str, ids) instanceof JSONObject;
  }

  /**
   * provides check for verifying if this element is a primitive or not.
   */
  public static boolean isJsonPrimitive(Object obj) {
    try {
      if (isJsonObject(obj)) {
        return false;
      } else if (isJsonArray(obj)) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return false;
    }
  }

  public static boolean isJsonPrimitive(JSONObject json, String ids) {
    return isJsonPrimitive(json.toString(), ids);
  }

  public static boolean isJsonPrimitive(String str, String ids) {
    try {
      if (isJsonObject(getObj(str, ids))) {
        return false;
      } else if (isJsonArray(getObj(str, ids))) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      return false;
    }
  }

  public static JSONObject getKeyValues(JSONObject obj) {
    Map result = new HashMap();
    obj.toMap().forEach((k, v) -> {
      if (v instanceof Map) {
        result.put(k, ((Map) v).get("value"));
      } else {
        result.put(k, v);
      }
    });
    return new JSONObject(result);
  }

  public JsonUtil getKeyValues() {
    JSONObject result = new JSONObject();
    _jsonObject.toMap().forEach((k, v) -> {
      if (v instanceof Map) {
        result.put(k, ((Map) v).get("value"));
      } else {
        result.put(k, v);
      }
    });
    _jsonObject = result;
    return this;
  }

  public static Object toJson(String code, String msg) {
    JSONObject result = new JSONObject();
    result.put("resultCode", code);
    result.put("resultMsg", msg);
    return result;
  }

  public static Object toMap(String code, String msg, Object data) {
    JSONObject result = new JSONObject();
    result.put("resultCode", code);
    result.put("resultMsg", msg);
    result.put("resultData", data);
    return result;
  }

  public static Map toMap(String code, String msg) {
    Map result = new HashMap();
    result.put("resultCode", code);
    result.put("resultMsg", msg);
    return result;
  }

  public static Map toMap(ResponseCode hs) {
    JSONObject result = new JSONObject();
    result.put("resultCode", hs.getCode());
    result.put("resultMsg", hs.getMessage());
    return result.toMap();
  }

  public static Map toMap(ResponseCode hs, Object data) {
    JSONObject result = new JSONObject();
    result.put("resultCode", hs.getCode());
    result.put("resultMsg", hs.getMessage());
    result.put("resultData", data);
    return result.toMap();
  }

  public static Map toMap(HttpStatus hs) {
    JSONObject result = new JSONObject();
    result.put("resultCode", hs.value());
    result.put("resultMsg", hs.name());
    return result.toMap();
  }

  public static Map toMap(HttpStatus hs, Object data) {
    JSONObject result = new JSONObject();
    result.put("resultCode", hs.value());
    result.put("resultMsg", hs.name());
    result.put("resultData", data);
    return result.toMap();
  }

  public static Map toMapE(String code, String title, String detail) {
    Map result = new HashMap();
    result.put("type", code);
    result.put("title", title);
    result.put("detail", detail);
    return result;
  }

  public static Object toMapC(String code, String msg, Object data) {
    JSONObject result = new JSONObject();
    result.put("responseCode", code);
    result.put("responseDescription", msg);
    result.put("resultData", data);
    return result;
  }

  public static Map toMapC(String code, String msg) {
    Map result = new HashMap();
    result.put("responseCode", code);
    result.put("responseDescription", msg);
    return result;
  }

  public static Map toMapC(ResponseCode hs) {
    JSONObject result = new JSONObject();
    result.put("responseCode", hs.getCode());
    result.put("responseDescription", hs.getMessage());
    return result.toMap();
  }

  public static Map toMapC(ResponseCode hs, Object data) {
    JSONObject result = new JSONObject();
    result.put("responseCode", hs.getCode());
    result.put("responseDescription", hs.getMessage());
    result.put("resultData", data);
    return result.toMap();
  }

  public static Map toMapC(HttpStatus hs) {
    JSONObject result = new JSONObject();
    result.put("responseCode", hs.value());
    result.put("responseDescription", hs.name());
    return result.toMap();
  }

  public static Map toMapC(HttpStatus hs, Object data) {
    JSONObject result = new JSONObject();
    result.put("responseCode", hs.value());
    result.put("responseDescription", hs.name());
    result.put("resultData", data);
    return result.toMap();
  }

  public static String result(Object data) {
    return data.toString();
  }

  public static Object nvl(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return val;
    }
  }

  public static Object nvl(Object val, DataType dt) {
    if (val instanceof String || val instanceof Character) {

      if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
        return JSONObject.NULL;
      } else {
        if (DataType.SHORT == dt || DataType.INTEGER == dt) {
          try {
            return Integer.valueOf(val + "");
          } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
          } catch (Exception e) {
            return Integer.valueOf((val + "").substring(0, (val + "").lastIndexOf(".")));
          }
        } else if (DataType.LONG == dt) {
          try {
            return Long.valueOf(val + "");
          } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
          } catch (Exception e) {
            return Long.valueOf((val + "").substring(0, (val + "").lastIndexOf(".")));
          }
        } else if (DataType.FLOAT == dt) {
          try {
            return Float.parseFloat(val + "");
          } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0.0f;
          }
        } else if (DataType.DOUBLE == dt) {
          try {
            return Double.parseDouble(val + "");
          } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0.0f;
          }
        } else if (DataType.BIGDECIMAL == dt) {
          try {
            return new BigDecimal(val + "");
          } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
          }
        } else if (DataType.STRING == dt) {
          return String.valueOf(val);
        } else {
          return val;
        }
      }

    } else if (val instanceof Short || val instanceof Integer || val instanceof BigInteger || val instanceof Byte || val instanceof Long || val instanceof Boolean || val instanceof BigDecimal
        || val instanceof Float || val instanceof Double) {
      try {
        if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
          return JSONObject.NULL;
        } else {
          if (DataType.SHORT == dt || DataType.INTEGER == dt || DataType.LONG == dt) {
            try {
              return Long.valueOf(val + "");
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
              return 0;
            } catch (Exception e) {
              return Long.valueOf((val + "").substring(0, (val + "").lastIndexOf(".")));
            }
          } else if (DataType.FLOAT == dt || DataType.DOUBLE == dt) {
            try {
              return Double.parseDouble(val + "");
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
              return 0.0f;
            }
          } else if (DataType.BIGDECIMAL == dt) {
            try {
              return new BigDecimal(val + "");
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
              return 0;
            }
          } else if (DataType.STRING == dt) {
            return String.valueOf(val);
          } else {
            return val;
          }

        }
      } catch (Exception e) {
        return JSONObject.NULL;
      }
    } else {
      return val == null || "-".equals(val) || "".equals(val) || "null".equals(val) ? JSONObject.NULL : val;
    }

  }

  public static Object parseInt(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return Integer.parseInt(val + "");
    }
  }

  public static Object parseLong(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return Long.parseLong(val + "");
    }
  }

  public static Object parseFloat(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return Float.parseFloat(val + "");
    }
  }

  public static Object parseDouble(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return Double.parseDouble(val + "");
    }
  }

  public static Object parseString(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return String.valueOf(val);
    }
  }

  public static Object parseDecimal(Object val) {
    if (val == null || "-".equals(val) || "".equals(val) || "null".equals(val)) {
      return JSONObject.NULL;
    } else {
      return new BigDecimal(val + "");
    }
  }

  /**
   * JsonObject를 Map&lt;String, String&gt;으로 변환한다.
   *
   * @param jsonObj JSONObject.
   * @return Map&lt;String, Object&gt;.
   */

  public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObj) {
    Map<String, Object> map = null;
    try {
      map = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
    } catch (IOException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return map;
  }

  public static void objectToParam(Map<String, Object> param, JSONArray jlist) {
    JSONObject jso = jlist.getJSONObject(0);
    List<String> keys = new ArrayList<>();
    for (String key : jso.keySet()) {
      if (jso.has(key)) {
        keys.add(key);
      }
    }
    for (String k : keys) {
      List<Object> l = new ArrayList<>();
      for (int i = 0; i < jlist.length(); i++) {
        JSONObject kk = jlist.getJSONObject(i);
        if (kk.has(k)) {
          l.add(kk.get(k));
        }
      }
      param.put(k, l);
    }

  }

  public static void objectToParam(Map<String, Object> param, JSONObject jso) {

    for (String key : jso.keySet()) {
      if (jso.has(key)) {
        param.put(key, jso.get(key));
      }
    }
  }

  public Date getDate(String ids) {
    try {
      // date = new
      // SimpleDateFormat(Constants.CONTENT_DATE_FORMAT).parse(get(_jsonObject, ids));
      // Timestamp createdOn = new Timestamp(date.getTime());
      return new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT).parse((String) getObj(_jsonObject, ids));
    } catch (Exception e) {
      return null;
    }
  }

} // end class
