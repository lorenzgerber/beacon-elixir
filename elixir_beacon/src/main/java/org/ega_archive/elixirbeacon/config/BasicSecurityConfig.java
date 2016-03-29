package org.ega_archive.elixirbeacon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Profile("basic")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class BasicSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private Environment environment;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    String apiVersion = environment.getProperty("server.servlet-path");

    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS); //Do not create sessions

    http.formLogin().disable();

    http.csrf().disable();

    http.httpBasic();

    http.authorizeRequests()
        .antMatchers(apiVersion + "/info").authenticated()
        .antMatchers(apiVersion + "/metrics/**").authenticated()
        .antMatchers(apiVersion + "/dump").authenticated()
        .antMatchers(apiVersion + "/trace").authenticated()
        .antMatchers(apiVersion + "/mappings").authenticated()
        .antMatchers(apiVersion + "/config/**").authenticated()
        .antMatchers(apiVersion + "/autoconfig").authenticated()
        .antMatchers(apiVersion + "/beans").authenticated()
        .antMatchers(apiVersion + "/health").authenticated()
        .antMatchers(apiVersion + "/configprops").authenticated()
        .antMatchers(apiVersion + "/login").permitAll();
  }
  //END CONFIGURATION

}