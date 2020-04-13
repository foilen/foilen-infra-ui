/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.CachingResourceResolver;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import com.foilen.infra.ui.web.controller.api.SwaggerExpose;
import com.foilen.infra.ui.web.interceptor.AddUserDetailsModelExtension;
import com.foilen.infra.ui.web.interceptor.AddVisualModelExtension;
import com.foilen.infra.ui.web.interceptor.RemoveModelOnRedirection;
import com.foilen.smalltools.spring.resourceresolver.BundleResourceResolver;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan({ "com.foilen.infra.ui.web" })
public class InfraUiWebSpringConfig implements WebMvcConfigurer {

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

        // New UI
        registry.addResourceHandler("/ui2/**").addResourceLocations("classpath:/WEB-INF/infra/ui/resources/ui2/");
        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/WEB-INF/infra/ui/resources/ui2/index.html");

        boolean isProd = "PROD".equals(System.getProperty("MODE"));

        ResourceChainRegistration chain = registry.addResourceHandler("/bundles/**") //
                .setCachePeriod(365 * 24 * 60 * 60) //
                .resourceChain(isProd) //
                .addResolver(new EncodedResourceResolver()); //
        if (isProd) {
            chain.addResolver(new CachingResourceResolver(new ConcurrentMapCache("bundles")));
        }
        BundleResourceResolver bundleResourceResolver = new BundleResourceResolver().setCache(isProd) //
                .setGenerateGzip(true);
        bundleResourceResolver.addBundleResource("all.css", "/META-INF/resources/webjars/bootstrap/3.3.7-1/css/bootstrap.css");
        bundleResourceResolver.addBundleResource("all.css", "/META-INF/resources/webjars/bootstrap/3.3.7-1/css/bootstrap-theme.css");
        bundleResourceResolver.addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/infra.css");
        bundleResourceResolver.addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/glyphicons.css");
        bundleResourceResolver.addBundleResource("all.css", "/WEB-INF/infra/ui/resources/css/glyphicons-bootstrap.css");

        bundleResourceResolver.addBundleResource("all.js", "/META-INF/resources/webjars/jquery/1.11.1/jquery.js");
        bundleResourceResolver.addBundleResource("all.js", "/META-INF/resources/webjars/bootstrap/3.3.7-1/js/bootstrap.js");
        bundleResourceResolver.addBundleResource("all.js", "/META-INF/resources/webjars/typeaheadjs/0.11.1/typeahead.jquery.js");
        bundleResourceResolver.addBundleResource("all.js", "/WEB-INF/infra/ui/resources/js/Chart.bundle.js");
        bundleResourceResolver.addBundleResource("all.js", "/WEB-INF/infra/ui/resources/js/infra.js");
        bundleResourceResolver.addBundleResource("all.js", "/WEB-INF/infra/ui/resources/js/infra-charts.js");

        bundleResourceResolver.addBundleResource("all-vendors.css", "/WEB-INF/infra/ui/resources/ui2/css/vendors/bootstrap-4.3.1.min.css");
        bundleResourceResolver.addBundleResource("all-vendors.css", "/WEB-INF/infra/ui/resources/ui2/css/vendors/vis-network-6.1.1.min.css");

        bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/jquery-3.4.1.min.js");
        bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/bootstrap-4.3.1.bundle.min.js");
        if (isProd) {
            bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/vue-2.6.10.min.js");
        } else {
            bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/vue-2.6.10-dev.js");
        }
        bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/vue-router-3.1.3.js");
        bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/vue-i18n-8.15.0.js");
        bundleResourceResolver.addBundleResource("all-vendors.js", "/WEB-INF/infra/ui/resources/ui2/js/vendors/vis-network-6.1.1.min.js");

        bundleResourceResolver.addBundleResource("all-app.js", "/WEB-INF/infra/ui/resources/ui2/js/errors.js");
        bundleResourceResolver.addBundleResource("all-app.js", "/WEB-INF/infra/ui/resources/ui2/js/views.js");
        bundleResourceResolver.addBundleResource("all-app.js", "/WEB-INF/infra/ui/resources/ui2/js/components.js");
        bundleResourceResolver.addBundleResource("all-app.js", "/WEB-INF/infra/ui/resources/ui2/js/app.js");

        bundleResourceResolver.primeCache();
        chain.addResolver(new VersionResourceResolver() //
                .addContentVersionStrategy("/**")) //
                .addResolver(bundleResourceResolver //
                );

    }

    @Bean
    public AddUserDetailsModelExtension addUserDetailsModelExtension() {
        return new AddUserDetailsModelExtension();
    }

    @Bean
    public AddVisualModelExtension addVisualModelExtension() {
        return new AddVisualModelExtension();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2) //
                .select() //
                .apis(RequestHandlerSelectors.withClassAnnotation(SwaggerExpose.class)) //
                .paths(PathSelectors.any()) //
                .build();
    }

    @Bean
    public RemoveModelOnRedirection removeModelOnRedirection() {
        return new RemoveModelOnRedirection();
    }

}
