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

import java.sql.Driver;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.DateTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlUtil {

  private static ReentrantLock lock = new ReentrantLock();
  private SqlSessionFactoryBean sessionFactory = null;

  /**
   * @param config
   */
  public SqlUtil(Properties config) {
    lock.lock();
    try {
      sessionFactory = new SqlSessionFactoryBean();
      sessionFactory.setDataSource(buildSimpleDataSource(config));

      Resource[] res = new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/Mapper-*.xml");
      sessionFactory.setEnvironment("development");
      sessionFactory.setMapperLocations(res);

      Properties properties = new Properties();
      properties.put("cacheEnabled", true);
      properties.put("useGeneratedKeys", false);
      properties.put("defaultExecutorType", "SIMPLE");
      properties.put("lazyLoadingEnabled", false);
      properties.put("aggressiveLazyLoading", true);
      properties.put("useColumnLabel", true);
      properties.put("autoMappingBehavior", "PARTIAL");
      properties.put("multipleResultSetsEnabled", true);
      properties.put("safeRowBoundsEnabled", true);
      properties.put("mapUnderscoreToCamelCase", false);

      sessionFactory.setConfigurationProperties(properties);
      sessionFactory.setTypeHandlers(new TypeHandler[] { new DateTypeHandler(), new BooleanTypeHandler() });

    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    } finally {
      lock.unlock();
    }

  }

  /**
   * 세션 가져오기
   *
   * @param autoCommit
   * @return
   * @throws Exception
   */
  public SqlSession getSqlSession(boolean autoCommit) throws Exception {
    return sessionFactory.getObject().openSession(true);
  }

  /**
   * 데이터 소스 가져오기
   *
   * @param config
   * @return
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked")
  public DataSource buildSimpleDataSource(Properties config) throws ClassNotFoundException {
    SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

    Class<? extends Driver> driverClass;
    try {
      driverClass = (Class<? extends Driver>) Class.forName(config.getProperty("driver"));
    } catch (ClassNotFoundException e) {
      throw new ClassNotFoundException("Cannot find class driver:" + config.getProperty("driver"));
    }
    dataSource.setDriverClass(driverClass);
    dataSource.setUrl(config.getProperty("url"));
    dataSource.setUsername(config.getProperty("username"));
    dataSource.setPassword(config.getProperty("password"));
    return dataSource;
  }

  /**
   * 닫기
   */
  public void close() {
    try {
      // TODO
    } catch (Exception e) {
      log.error("Exception : " + ExceptionUtils.getStackTrace(e));
    }
  }

  /**
   * 인젝션 처리
   *
   * @param s
   * @return
   */
  public static String escapeSQL(String s) {
    return s.replaceAll("'", "\\'");
  }

} // end class