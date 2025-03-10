package io.homs.custache.spring;

import io.homs.custache.CachedModelAndView;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustacheConfiguration {

    @Value("${custache.prefix:templates/}")
    String templatePrefix;

    @Value("${custache.suffix:.html}")
    String templateSuffix;

    @Bean
    public TemplateLoadingStrategy templateLoadingStrategy() {
        return new DefaultClasspathTemplateLoadingStrategy(templatePrefix, templateSuffix);
    }

    @Bean
    public CachedModelAndView cachedModelAndView(TemplateLoadingStrategy templateLoadingStrategy) {
        return new CachedModelAndView(templateLoadingStrategy);
    }
}
