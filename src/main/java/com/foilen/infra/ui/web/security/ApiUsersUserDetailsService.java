/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.security;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.smalltools.tools.AbstractBasics;

public class ApiUsersUserDetailsService extends AbstractBasics implements UserDetailsService {

    private UserApiRepository userApiRepository;
    private UserApiMachineRepository userApiMachineRepository;

    public UserApiMachineRepository getUserApiMachineRepository() {
        return userApiMachineRepository;
    }

    public UserApiRepository getUserApiRepository() {
        return userApiRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserApi> userApiO = userApiRepository.findById(username);
        if (userApiO.isPresent()) {
            UserApi userApi = userApiO.get();
            throwNotFoundIfExpired(userApi.getExpireOn());
            return new User(username, userApi.getUserHashedKey(), Collections.emptyList());
        }
        Optional<UserApiMachine> userMachineApiO = userApiMachineRepository.findById(username);
        if (userMachineApiO.isPresent()) {
            UserApi userApi = userMachineApiO.get();
            throwNotFoundIfExpired(userApi.getExpireOn());
            return new User(username, userApi.getUserHashedKey(), Collections.emptyList());
        }

        throw new UsernameNotFoundException("Not found");
    }

    public void setUserApiMachineRepository(UserApiMachineRepository userApiMachineRepository) {
        this.userApiMachineRepository = userApiMachineRepository;
    }

    public void setUserApiRepository(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }

    private void throwNotFoundIfExpired(Date expireOn) throws UsernameNotFoundException {
        if (expireOn != null) {
            if (expireOn.getTime() < System.currentTimeMillis()) {
                throw new UsernameNotFoundException("Expired");
            }
        }
    }

}
