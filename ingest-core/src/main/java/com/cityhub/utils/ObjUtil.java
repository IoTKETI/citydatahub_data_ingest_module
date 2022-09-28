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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Slf4j
public class ObjUtil {

  /**
   * 오브젝트를 Map으로 변환
   *
   * @param obj
   * @return
   */
  public static Map objToMap(Object obj) {
    try {
      // Field[] fields = obj.getClass().getFields(); //private field는 나오지 않음.
      Field[] fields = obj.getClass().getDeclaredFields();
      Map resultMap = new HashMap();
      for (int i = 0; i <= fields.length - 1; i++) {
        fields[i].setAccessible(true);
        resultMap.put(fields[i].getName(), fields[i].get(obj));
      }
      return resultMap;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
    return null;
  }

  /**
   * 맵을 오브젝트로 변환
   *
   * @param map
   * @param objClass
   * @return
   */
  public static Object mapToObj(Map map, Object objClass) {
    String keyAttribute = null;
    String setMethodString = "set";
    String methodString = null;
    Iterator itr = map.keySet().iterator();
    while (itr.hasNext()) {
      keyAttribute = (String) itr.next();
      methodString = setMethodString + keyAttribute.substring(0, 1).toUpperCase() + keyAttribute.substring(1);
      try {
        Method[] methods = objClass.getClass().getDeclaredMethods();
        for (int i = 0; i <= methods.length - 1; i++) {
          if (methodString.equals(methods[i].getName())) {
            if (log.isDebugEnabled()) {
              log.debug("invoke : " + methodString);
            }

            methods[i].invoke(objClass, map.get(keyAttribute));
          }
        }
      } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        log.error("Exception : " + ExceptionUtils.getStackTrace(e));
      }
    }
    return objClass;
  }

} // end class
