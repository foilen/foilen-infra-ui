/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.foilen.infra.api.model.ui.ApplicationDetails;
import com.foilen.infra.api.model.ui.ApplicationDetailsResult;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.login.spring.services.FoilenLoginService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.ResourceTools;

@Service
public class ApplicationServiceImpl extends AbstractBasics implements ApplicationService {

    @Autowired
    private FoilenLoginService foilenLoginService;
    @Autowired
    private ReloadableResourceBundleMessageSource messageSource;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private UserHumanRepository userHumanRepository;

    private Map<String, Object> translations = new TreeMap<>();
    private List<String> resourceTypes;

    private String version = "TEST";

    private void addTranslations(Map<String, String> lang, String filename) {
        filename = filename.substring(filename.indexOf('/'));
        Properties properties = new Properties();
        try {
            InputStream inputStream = ResourceTools.getResourceAsStream(filename);
            if (inputStream == null) {
                logger.error("Resource {} does not exist", filename);
                return;
            }
            properties.load(new InputStreamReader(inputStream, CharsetTools.UTF_8));

            properties.forEach((key, value) -> lang.put((String) key, (String) value));
        } catch (IOException e) {
            logger.error("Could not load {}", filename, e);
        }

    }

    @Override
    public ApplicationDetailsResult getDetails(String userId) {

        ApplicationDetails applicationDetails = new ApplicationDetails() //
                .setVersion(version) //
                .setUserId(userId) //
                .setLang(LocaleContextHolder.getLocale().getLanguage()) //
                .setTranslations(translations) //
                .setResourceTypes(resourceTypes) //
        ;

        // Logged in user
        FoilenLoginUserDetails userDetails = foilenLoginService.getLoggedInUserDetails();
        if (userDetails != null) {
            applicationDetails.setUserEmail(userDetails.getEmail());

            Optional<UserHuman> user = userHumanRepository.findById(userDetails.getUsername());
            if (user.isPresent()) {
                applicationDetails.setUserAdmin(user.get().isAdmin());
            }
        }

        return new ApplicationDetailsResult(applicationDetails);
    }

    @PostConstruct
    public void init() {

        // Version
        try {
            version = FileTools.getFileAsString("/app/version.txt");
        } catch (Exception e) {
        }

        // Translations
        Map<String, String> langEn = new TreeMap<>();
        translations.put("en", langEn);

        Map<String, String> langFr = new TreeMap<>();
        translations.put("fr", langFr);

        for (String basename : messageSource.getBasenameSet()) {
            addTranslations(langEn, basename + "_en.properties");
            addTranslations(langFr, basename + "_fr.properties");
        }

        // Resource Types
        resourceTypes = resourceService.getResourceDefinitions().stream() //
                .map(it -> it.getResourceType()) //
                .sorted() //
                .collect(Collectors.toList());

    }

}
