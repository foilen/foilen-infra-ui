/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.web.interceptor;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;

/**
 * Rename the current thread to have a unique ID per thread.
 */
public class ThreadUniqueIdByRequestInterceptor implements HandlerInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(ThreadUniqueIdByRequestInterceptor.class);

    static private final ThreadLocal<ThreadNameStateTool> THREAD_NAME_STATE = new ThreadLocal<ThreadNameStateTool>() {
        @Override
        protected ThreadNameStateTool initialValue() {
            return ThreadTools.nameThread();
        }
    };
    static private final ThreadLocal<AtomicLong> THREAD_EXECUTION_TIME = new ThreadLocal<AtomicLong>() {
        @Override
        protected AtomicLong initialValue() {
            return new AtomicLong();
        }
    };

    static private final String UID_PREFIX = SecureRandomTools.randomHexString(5).toUpperCase();
    static private final AtomicLong UID_COUNT = new AtomicLong(1);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        long deltaMs = System.currentTimeMillis() - THREAD_EXECUTION_TIME.get().get();
        logger.info("Request end. Took {} ms", deltaMs);
        THREAD_NAME_STATE.get().revert();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        ThreadNameStateTool threadNameStateTool = THREAD_NAME_STATE.get().clear().setSeparator("-");
        THREAD_EXECUTION_TIME.get().set(System.currentTimeMillis());

        // Username
        String principal = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                principal = authentication.getName();
            }
        }
        if (principal != null) {
            threadNameStateTool.appendText("USER");
            threadNameStateTool.appendText(principal);
        }

        // Unique ID
        String uniqueId = UID_PREFIX + "_" + UID_COUNT.getAndIncrement();
        threadNameStateTool.appendText("REQID");
        threadNameStateTool.appendText(uniqueId);

        // Controller and method
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            String controllerName = handlerMethod.getBean().getClass().getSimpleName();
            controllerName = String.valueOf(controllerName.charAt(0)).toLowerCase() + controllerName.substring(1, controllerName.length() - 10);

            String controllerAction = handlerMethod.getMethod().getName();

            threadNameStateTool.appendText("ACTION");
            threadNameStateTool.appendText(controllerName);
            threadNameStateTool.appendText(controllerAction);
        } else {
            threadNameStateTool.appendText("URL");
            threadNameStateTool.appendText(request.getMethod());
            threadNameStateTool.appendText(request.getRequestURI());
        }

        // Update the thread's name
        threadNameStateTool.change();
        logger.info("Request begin");
        return true;
    }

}
