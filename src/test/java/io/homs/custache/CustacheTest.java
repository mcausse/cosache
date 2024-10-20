package io.homs.custache;

import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

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

}