/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import com.foilen.infra.ui.web.interceptor.AddUserDetailsModelExtension;
import com.foilen.infra.ui.web.interceptor.AddVisualModelExtension;
import com.foilen.infra.ui.web.interceptor.RemoveModelOnRedirection;
import com.foilen.smalltools.spring.resourceresolver.BundleResourceResolver;

@Configuration
@ComponentScan({ "com.foilen.infra.ui.web" })
public class InfraUiWebSpringConfig extends WebMvcConfigurerAdapter {

    @Bean
    public AddUserDetailsModelExtension addUserDetailsModelExtension() {
        return new AddUserDetailsModelExtension();
    }

    @Bean
    public AddVisualModelExtension addVisualModelExtension() {
        return new AddVisualModelExtension();
    }

    @Bean
    public RemoveModelOnRedirection removeModelOnRedirection() {
        return new RemoveModelOnRedirection();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(addUserDetailsModelExtension());
        registry.addInterceptor(addVisualModelExtension());
        registry.addInterceptor(removeModelOnRedirection());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/WEB-INF/infra/ui/resources/images/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/WEB-INF/infra/ui/resources/fonts/");

        boolean isProd = "PROD".equals(System.getProperty("MODE"));

        ResourceChainRegistration chain = registry.addResourceHandler("/bundles/**") //
                .setCachePeriod(365 * 24 * 60 * 60) //
                .resourceChain(isProd) //
                .addResolver(new GzipResourceResolver()); //
        if (isProd) {
            chain.addResolver(new CachingResourceResolver(new ConcurrentMapCache("bundles")));
        }
        chain.addResolver(new VersionResourceResolver() //
                .addContentVersionStrategy("/**")) //
                .addResolver(new BundleResourceResolver() //
                        .setCache(isProd) //
                        .setGenerateGzip(true) //
                        .addBundleResource("all.css", "/META-INF/resources/webjars/bootstrap/3.3.7-1/css/bootstrap.css") //
                        .addBundleResource("all.css", "/META-INF/resources/webjars/bootstrap/3.3.7-1/css/bootstrap-theme.css") //
                        .addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/infra-ui.css") //
                        .addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/glyphicons.css") //
                        .addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/glyphicons-bootstrap.css") //
                        .addBundleResource("all.js", "/META-INF/resources/webjars/jquery/1.11.1/jquery.js") //
                        .addBundleResource("all.js", "/META-INF/resources/webjars/bootstrap/3.3.7-1/js/bootstrap.js") //
                        .addBundleResource("all.js", "/WEB-INF/infra/ui/resources/js/Chart.bundle.js") //
                        .addBundleResource("all.js", "/WEB-INF/infra/ui/resources/js/infra-ui.js") //
                        .primeCache() //

        );

    }

}
