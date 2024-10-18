package io.homs.custache;

import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class CustacheTest {

    final String template = """
            {{?dogs}}
                <ul>
                {{!}}This is a great comment!{{/}}
                {{#dog dogs}}
                    <li>{{dog.name}}-{{dog.age}}</li>
                {{/}}
                </ul>
            {{/}}
            {{^dogs}}
                No dogs.
            {{/}}
            """;

    @Value
    public static class Dog {
        String name;
        int age;
    }

    @Test
    void basic_integration_test() {

        var context = new Context();
        context.def("dogs", List.of(
                new Dog("faria", 12),
                new Dog("chucho", 14)
        ));

        String result = Custache.parse("test", template).evaluate(context);

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("<ul><li>faria-12</li><li>chucho-14</li></ul>");
        assertThat(result).isEqualTo("\n" +
                "    <ul>\n" +
                "        <li>faria-12</li>\n" +
                "        <li>chucho-14</li>\n" +
                "    </ul>");
    }

    @Test
    void basic_integration_test_empty_list() {

        var context = new Context();
        context.def("dogs", List.of());

        String result = Custache.parse("test", template).evaluate(context);

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("Nodogs.");
    }

    @Test
    void basic_integration_test_list_not_defined() {

        var context = new Context();

        try {
            Custache.parse("test", template).evaluate(context);
            fail();
        } catch (Exception e) {
            assertThat(e).hasMessage("evaluating expression: dogs, at: test:1,4")
                    .getCause().hasMessage("variable not defined: 'dogs'");
        }
    }

    @Test
    void expression_a_full() {

        var context = new Context();
        context.def("a", "b");

        var result = Custache.parse("test", "{{a.class.getName.toUpperCase}}").evaluate(context);

        System.out.println(result);
        assertThat(result.replaceAll("\\s+", "")).isEqualTo("JAVA.LANG.STRING");
    }
}