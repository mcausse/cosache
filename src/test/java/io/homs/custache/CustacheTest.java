package io.homs.custache;

import io.homs.custache.files.TemplateLoadingStrategy;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class CustacheTest {

    @Value
    public static class Dog {
        String name;
        int age;
    }

    @Test
    void basic_integration_test() {

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("basic/test-template");

        String result = sut.evaluate(templateAst, "dogs", List.of(
                new Dog("faria", 12),
                new Dog("chucho", 14)
        ));

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("<head></head><ul><li>faria-12</li><li>chucho-14</li></ul>");
        assertThat(result).isEqualTo("<head></head>\n" +
                "<ul>\n" +
                "    <li>faria-12</li>\n" +
                "    <li>chucho-14</li>\n" +
                "</ul>");
    }

    @Test
    void basic_integration_test_empty_list() {

        var context = new Context();
        context.def("dogs", List.of());

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("basic/test-template");

        String result = sut.evaluate(templateAst, "dogs", List.of());

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("<head></head>Nodogs.");
    }

    @Test
    void basic_integration_test_list_not_defined() {

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("basic/test-template");
        try {

            sut.evaluate(templateAst, "dogsXXXX", List.of());

            fail();
        } catch (Exception e) {
            assertThat(e).hasMessage("evaluating expression: dogs, at: templates/basic/test-template.html:2,4")
                    .getCause().hasMessage("variable not defined: 'dogs'");
        }
    }

    @Test
    void expression_a_full() {

        var context = new Context();
        context.def("a", "b");

        var result = new Parser("test", "{{a.class.getName.toUpperCase}}").parse().evaluate(context);

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("JAVA.LANG.STRING");
    }

    @Test
    void include_test_with_variable_mapping() {

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("include/template");

        String result = sut.evaluate(templateAst, "dogs", List.of(
                new Dog("faria", 12),
                new Dog("chucho", 14)
        ));

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("faria-12chucho-14");
        assertThat(result).isEqualTo("faria-12chucho-14");
    }

    private static Stream<Arguments> invalidTemplatesProvider() {
        return Stream.of(
                Arguments.of("{{#}}", "expected to consume an identifiers, but not; at: urn:1,4", null),
                Arguments.of("{{#j jou fdghdfgh", "expected: }}, at: urn:1,9", null),
                Arguments.of("{{#j jou}}", "expected: {{/}}, but eof; at: urn:1,11", null),
                Arguments.of("{{#jou}}{{/}}", "expected to consume an identifiers, but not; at: urn:1,7", null),
                Arguments.of("{{#j jou}}{{/}}", "evaluating expression: jou, at: urn:1,6", "class java.lang.Integer cannot be cast to class java.lang.Iterable")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTemplatesProvider")
    void for_tag_needs_an_iterable_value(String template, String expectedError, String expectedNextedError) {
        var sut = new Custache();

        try {
            var templateAst = sut.loadParseredTemplate(new TemplateLoadingStrategy.Template("urn", template));
            sut.evaluate(templateAst, "jou", 1);

            fail();
        } catch (Exception e) {
            assertThat(e).hasMessage(expectedError);
            if (expectedNextedError != null) {
                assertThat(e.getCause()).hasMessageContaining(expectedNextedError);
            }
        }
    }

}










