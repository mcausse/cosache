package io.homs.custache;

import io.homs.custache.eval.Context;
import io.homs.custache.parser.Parser;
import io.homs.custache.parser.ast.Ast;
import io.homs.custache.template.Template;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class CustacheTest {

    @Value
    public static class Dog {
        String name;
        int age;
        boolean alive;
    }

    @Test
    void basic_integration_test() {

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("basic/test-template");

        String result = sut.evaluate(templateAst, "dogs", List.of(
                new Dog("faria", 12, true),
                new Dog("chucho", 14, false)
        ));

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

        assertThat(result.replaceAll("\\s+", "")).isEqualTo("<head></head>Nodogs.");
        assertThat(result).isEqualTo("<head></head>\n" +
                "No dogs.");
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

        var result = new Parser(Template.of("test", "{{a.class.getName.toUpperCase}}")).parse().evaluate(context);

        System.out.println(result);
        assertThat(result).isEqualTo("JAVA.LANG.STRING");
    }

    @Test
    void include_test_with_variable_mapping() {

        var sut = new Custache();
        var templateAst = sut.loadParseredTemplate("include/template");

        String result = sut.evaluate(templateAst, "dogs", List.of(
                new Dog("faria", 12, true),
                new Dog("chucho", 14, false)
        ));

        assertThat(result).isEqualTo("faria-12(alive)chucho-14");
    }

    private static Stream<Arguments> invalidTemplatesProvider() {
        return Stream.of(
                Arguments.of("{{#}}", "expected to consume a word, but not; at: urn:1,4", null),
                Arguments.of("{{#j jou fdghdfgh", "expected: }}, at: urn:1,9", null),
                Arguments.of("{{#j jou}}", "expected: {{/}}, but eof; at: urn:1,11", null),
                Arguments.of("{{#jou}}{{/}}", "expected to consume a word, but not; at: urn:1,7", null),
                Arguments.of("{{#j jou}}{{/}}", "evaluating expression: jou, at: urn:1,6", "class java.lang.Integer cannot be cast to class java.lang.Iterable")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTemplatesProvider")
    void for_tag_needs_an_iterable_value(String template, String expectedError, String expectedNestedError) {
        var sut = new Custache();

        try {
            var templateAst = sut.loadParseredTemplate(new Template("urn", template));
            sut.evaluate(templateAst, "jou", 1);

            fail();
        } catch (Exception e) {
            assertThat(e).hasMessage(expectedError);
            if (expectedNestedError != null) {
                assertThat(e.getCause()).hasMessageContaining(expectedNestedError);
            }
        }
    }

    @Test
    void custache_can_evaluate_maps() {

        var templateAst = new Custache().loadParseredTemplate(new Template("urn", "{{model.name.1}}"));

        String r = new Custache().evaluate(templateAst, "model", Map.of("name", Map.of("1", 2)));

        assertThat(r).isEqualTo("2");
    }

    private static Stream<Arguments> model_evaluates_to_Provider() {
        return Stream.of(
                Arguments.of(null, "0"),
                Arguments.of(Color.RED, "1"),

                Arguments.of(true, "1"),
                Arguments.of(false, "0"),
                Arguments.of(5, "1"),
                Arguments.of(0, "0"),
                Arguments.of(5.2, "1"),
                Arguments.of(0.0, "0"),
                Arguments.of("jou", "1"),
                Arguments.of("", "0"),

                Arguments.of(List.of("1"), "1"),
                Arguments.of(List.of(), "0"),

                Arguments.of(new String[]{"1"}, "1"),
                Arguments.of(new String[]{}, "0")
        );
    }

    @ParameterizedTest
    @MethodSource("model_evaluates_to_Provider")
    public void model_evaluates_to(Object model, String expectedResult) {
        final Custache custache = new Custache();
        Ast templateAst = custache.loadParseredTemplate(new Template("testurn",
                "{{?model}}1{{/}}{{^model}}0{{/}}"
        ));

        String r = custache.evaluate(templateAst, "model", model);

        assertThat(r).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("model_evaluates_to_Provider")
    public void model_evaluates_to_IF_ELSE(Object model, String expectedResult) {
        final Custache custache = new Custache();
        Ast templateAst = custache.loadParseredTemplate(new Template("testurn",
                "{{? model}}1{{:}}0{{/}}"
        ));

        String r = custache.evaluate(templateAst, "model", model);

        assertThat(r).isEqualTo(expectedResult);
    }

    @Test
    void test_IF_ELSE() {
        final Custache custache = new Custache();
        final String templateContent = "{{?model.dog}}{{?model.dog.name}}1{{/}}{{:}}0{{/}}" +
                "{{?model}}1{{/}}" +
                "{{?model.dog.dead}}1{{/}}";
        Ast templateAst = custache.loadParseredTemplate(new Template("testurn",
                templateContent
        ));

        String r = custache.evaluate(templateAst, "model", Map.of("dog", Map.of("name", "duche", "dead", false)));

        assertThat(r).isEqualTo("11");
    }

    @Test
    void assert_that_2_nested_loop_should_work() {
        Ast sut = new Parser(Template.of("test", "{{#i is}}{{#j js}}{{i}}{{j}}{{/}}{{/}}")).parse();

        var ctx = new Context();
        ctx.def("is", List.of("a", "b", "c"));
        ctx.def("js", List.of(1, 2, 3));

        assertThat(sut.evaluate(ctx)).isEqualTo("a1a2a3b1b2b3c1c2c3");
    }
}

