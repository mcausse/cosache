package io.homs.custache.spring;

import io.homs.custache.CachedModelAndView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CustacheConfiguration.class)
class CustacheConfigurationTest {

    @Autowired
    CachedModelAndView cachedModelAndView;

    @Test
    void testGetObject() {

        String r = cachedModelAndView.getOrParse("jou")
                .with("name", "world")
                .evaluate();

        assertThat(r).isEqualTo("Hello world!");
    }
}