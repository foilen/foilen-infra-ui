/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;

@Order(1) // To ensure it is checked before FoilenLoginWebSecurityConfigurerAdapter
public class ApiWebSecurityRepositoryConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserApiRepository userApiRepository;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        ApiUsersUserDetailsService apiUsersUserDetailsService = new ApiUsersUserDetailsService();
        apiUsersUserDetailsService.setUserApiMachineRepository(userApiMachineRepository);
        apiUsersUserDetailsService.setUserApiRepository(userApiRepository);

        auth.userDetailsService(apiUsersUserDetailsService) //
                .passwordEncoder(new BCryptPasswordEncoder(13));
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
