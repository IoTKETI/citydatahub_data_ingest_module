package com.cityhub.daemon.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cityhub.daemon.code.Consts;

/**
 * Application DB 접속 설정 클래스
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfiguration {

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Value("${spring.datasource.url}")
  private String jdbcUrl;

  @Value("${spring.datasource.username}")
  private String jdbcUserName;

  @Value("${spring.datasource.password}")
  private String jdbcPassword;

  @Bean
  @Primary
  @Qualifier("dataSource")
  public DataSource dataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(jdbcUrl);
    dataSource.setUsername(jdbcUserName);
    dataSource.setPassword(jdbcPassword);

    return dataSource;
  }

  @Bean
  @Primary
  @Qualifier("dataSourceTransactionManager")
  DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  @Primary
  @Qualifier("sqlSessionFactory")
  SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
    configuration.setCacheEnabled(true);
    configuration.setUseGeneratedKeys(false);
    configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
    configuration.setLazyLoadingEnabled(false);
    configuration.setAggressiveLazyLoading(true);
    configuration.setUseColumnLabel(true);
    configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    configuration.setMultipleResultSetsEnabled(true);
    configuration.setSafeRowBoundsEnabled(true);
    configuration.setMapUnderscoreToCamelCase(false);

    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
    factory.setConfiguration(configuration);
    factory.setTypeAliasesPackage(Consts.BASE_PACKAGE);
    return factory.getObject();
  }

  @Bean
  @Primary
  @Qualifier("sqlSession")
  SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }

  @Bean
  @Qualifier("batchSqlSession")
  SqlSessionTemplate batchSqlSession(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
  }

}
