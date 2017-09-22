package com.broadcom.wbi;

import com.broadcom.wbi.util.ProjectConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class WbiApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(WbiApplication.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // webjars will be cached in a month
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
//                .setCachePeriod(ProjectConstant.CacheTimeout.MONTH.getSecond())
//                .resourceChain(true)
//                .addResolver(new GzipResourceResolver());

        // font size will be cached long
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(ProjectConstant.CacheTimeout.MONTH.getSecond())
                .resourceChain(true)
                .addResolver(new GzipResourceResolver());

        registry.addResourceHandler("/css/*.css").addResourceLocations("classpath:/static/css/", "file:/resources/css/")
                .setCachePeriod(ProjectConstant.CacheTimeout.WEEK.getSecond())
                .resourceChain(true)
                .addResolver(new GzipResourceResolver());

        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/", "file:/resources/js/")
                .setCachePeriod(ProjectConstant.CacheTimeout.MINUTE.getSecond())
                .resourceChain(true)
                .addResolver(new GzipResourceResolver());
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }
}
