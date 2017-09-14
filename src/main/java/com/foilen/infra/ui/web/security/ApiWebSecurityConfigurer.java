/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.web.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Order(1)
public class ApiWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource) //
                .passwordEncoder(new BCryptPasswordEncoder(13)) //
                .usersByUsernameQuery("SELECT user_id, user_hashed_key, IF(expire_on IS NULL OR expire_on > CURDATE(), 1, 0) FROM api_user WHERE user_id = ?") //
                .authoritiesByUsernameQuery("SELECT user_id, 'USER' FROM api_user WHERE user_id = ?"); // Always "USER" since we need something
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**").authorizeRequests() //
                .anyRequest().fullyAuthenticated() //
                .and() //
                .httpBasic().realmName("Infra UI") //
                .and() //
                .csrf().disable(); //
    }

}
