package io.homs.custache;

import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CachedModelAndViewTest {

    @Test
    void name() {
        TemplateLoadingStrategy tls = new DefaultClasspathTemplateLoadingStrategy("templates/", ".html");

        CachedModelAndView mav = new CachedModelAndView(tls);
        String r = mav.getOrParse("basic/test-template")
                .with("dogs", List.of(Map.of("name", "duche", "age", 10)))
                .evaluate();

        assertThat(r).isEqualTo(
                "<head></head>\n" +
                        "<ul>\n" +
                        "    <li>duche-10</li>\n" +
                        "</ul>"
        );
    }
}