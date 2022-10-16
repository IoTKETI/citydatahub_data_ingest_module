package com.cityhub.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.cityhub.web.security.filter.JwtAuthenticationFilter;
import com.cityhub.web.security.handler.DataCoreUIAuthenticationEntryPoint;
import com.cityhub.web.security.service.DataCoreUiSVC;


/**
 * Class for application security
 * @FileName ApplicationSecurity.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Configuration
@EnableOAuth2Sso
public class ApplicationSecurity extends WebSecurityConfigurerAdapter {

  @Autowired
  OAuth2ClientContext oauth2ClientContext;


  @Autowired
  private DataCoreUiSVC dataCoreUiSVC;


  @Autowired
  private ConfigEnv configEnv;


  /**
   * Set up application http security configuration.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if ("Y".equals(configEnv.getAuthYn())) {
      http.antMatcher("/**")
      .authorizeRequests()
      .antMatchers("/error**")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and().csrf().disable()
      .addFilterAfter(new JwtAuthenticationFilter(authenticationEntryPoint(), dataCoreUiSVC), BasicAuthenticationFilter.class);
    } else {
      http.csrf()
      .disable()
      .authorizeRequests()
      .anyRequest().permitAll();
    }
  }

  /**
   * Set up application web security configuration.
   */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(
        "/accesstoken"
        ,"/logout"
        ,"/restApi/**"
        ,"/agents/**"
        ,"/adaptorType/**"
        ,"/sourceModel/**"
        ,"/sourceModels/**"
        ,"/sourceModelsTest"
        );
  }

  /**
   * Create bean for DataCoreUIAuthenticationEntryPoint class
   * @return
   */
  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new DataCoreUIAuthenticationEntryPoint();
  }
}
