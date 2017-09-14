/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.web.interceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import com.foilen.infra.ui.visual.MenuEntry;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tuple.Tuple2;

/**
 * Add all the visual parts of the application.
 */
public class AddVisualModelExtension extends AbstractCommonHandlerInterceptor {

    private Map<String, MenuEntry> topMenuEntryByUri = new ConcurrentHashMap<>();
    private Map<String, MenuEntry> leftMenuEntryByUri = new ConcurrentHashMap<>();
    private List<Tuple2<String, MenuEntry>> topMenustartsWith = new ArrayList<>();
    private List<Tuple2<String, MenuEntry>> leftMenustartsWith = new ArrayList<>();
    private String version = "LOCAL";

    @Autowired
    private MenuEntry rootMenuEntry;

    private MenuEntry findEntryStartsWith(List<Tuple2<String, MenuEntry>> menuStartsWith, String uri) {

        for (Tuple2<String, MenuEntry> menyEntryByStartsWith : menuStartsWith) {
            if (uri.startsWith(menyEntryByStartsWith.getA())) {
                return menyEntryByStartsWith.getB();
            }
        }

        return null;
    }

    /**
     * Cache the menu entries lookups.
     */
    @PostConstruct
    public void init() {

        // Version
        try {
            version = FileTools.getFileAsString("/app/version.txt");
        } catch (Exception e) {
        }

        // Menu
        for (MenuEntry topMenuEntry : rootMenuEntry.getChildren()) {
            // If top menu is not a dropdown
            String uri = topMenuEntry.getUri();
            if (uri != null) {
                topMenuEntryByUri.put(uri, topMenuEntry);
            }
            List<String> uriStartsWith = topMenuEntry.getUriStartsWith();
            for (String uriStartWith : uriStartsWith) {
                topMenustartsWith.add(new Tuple2<String, MenuEntry>(uriStartWith, topMenuEntry));
            }

            // For the drop down items
            for (MenuEntry childMenuEntry : topMenuEntry.getChildren()) {
                uri = childMenuEntry.getUri();
                if (uri != null) {
                    topMenuEntryByUri.put(uri, topMenuEntry);
                    leftMenuEntryByUri.put(uri, childMenuEntry);
                }
                uriStartsWith = childMenuEntry.getUriStartsWith();
                for (String uriStartWith : uriStartsWith) {
                    topMenustartsWith.add(new Tuple2<String, MenuEntry>(uriStartWith, topMenuEntry));
                    leftMenustartsWith.add(new Tuple2<String, MenuEntry>(uriStartWith, childMenuEntry));
                }
            }
        }

        Comparator<Tuple2<String, MenuEntry>> sorting = (a, b) -> {
            int diff = b.getA().length() - a.getA().length();
            if (diff == 0) {
                diff = a.getA().compareTo(b.getA());
            }
            return diff;
        };

        // Sort in reverse length order
        topMenustartsWith.sort(sorting);
        leftMenustartsWith.sort(sorting);

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && !isRedirect(modelAndView)) {
            // Root menu
            modelAndView.addObject("rootMenuEntry", rootMenuEntry);

            // Controller details
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;

                String controllerName = handlerMethod.getBean().getClass().getSimpleName();
                controllerName = String.valueOf(controllerName.charAt(0)).toLowerCase() + controllerName.substring(1, controllerName.length() - 10);

                String controllerAction = handlerMethod.getMethod().getName();
                modelAndView.addObject("controllerName", controllerName);
                modelAndView.addObject("controllerAction", controllerAction);
                modelAndView.addObject("version", version);

                // Left and top menus
                String uri = request.getRequestURI();
                MenuEntry topMenuEntry = topMenuEntryByUri.get(uri);
                MenuEntry leftMenuEntry = leftMenuEntryByUri.get(uri);
                if (topMenuEntry == null) {
                    topMenuEntry = findEntryStartsWith(topMenustartsWith, uri);
                }
                if (leftMenuEntry == null) {
                    leftMenuEntry = findEntryStartsWith(leftMenustartsWith, uri);
                }

                if (modelAndView != null) {
                    modelAndView.addObject("topMenuEntry", topMenuEntry);
                    modelAndView.addObject("leftMenuEntry", leftMenuEntry);
                }

            }
        }

    }

}
