package io.homs.custache.spring;

import io.homs.custache.CachedModelAndView;
import io.homs.custache.template.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.template.TemplateLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:custache.properties", ignoreResourceNotFound = true)
public class CustacheConfiguration {

    @Value("${custache.prefix:templates/}")
    String templatePrefix;

    @Value("${custache.suffix:}")
    String templateSuffix;

    @Bean
    public TemplateLoadingStrategy custacheTemplateLoadingStrategy() {
        return new DefaultClasspathTemplateLoadingStrategy(templatePrefix, templateSuffix);
    }

    @Bean
    public CachedModelAndView custacheCachedModelAndView(TemplateLoadingStrategy templateLoadingStrategy) {
        return new CachedModelAndView(templateLoadingStrategy);
    }
}
