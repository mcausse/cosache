package io.homs.custache;

import io.homs.custache.ast.Ast;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    private static Stream<Arguments> provider() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("a"),
                Arguments.of("{{?x}}{{/}}"),
                Arguments.of("a{{?x.y.z}}c{{/}}d"),
                Arguments.of("{{?x}}{{^y}}{{?z}}{{/}}{{/}}{{/}}"),

                Arguments.of("""
                        {{>basic/header(jou=juas.size, juas=juan)}}
                        {{?dogs}}
                            <ul>
                            {{!}}This is a great comment!{{/}}
                            {{!}}{{/}}
                            {{#dog dogs}}
                                <li>{{dog.name}}-{{dog.age}}</li>
                            {{/}}
                            </ul>
                        {{/}}
                        {{^dogs}}
                            No dogs.
                        {{/}}
                        {{>basic/footer}}
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource("provider")
    void test_that_parser_produces_the_same_ast_toString_as_input(String template) {
        var sut = new Parser("test", template);

        // Act
        var ast = sut.parse();

        assertThat(ast).hasToString(template);
    }

    private static Stream<Arguments> includesProvider() {
        return Stream.of(
                Arguments.of("{{>basic/footer}}", "{{>basic/footer}}"),
                Arguments.of("{{> basic/footer }}", "{{>basic/footer}}"),

                Arguments.of("{{>basic/footer(jou=dog.name)}}", "{{>basic/footer(jou=dog.name)}}"),
                Arguments.of("{{> basic/footer ( jou = dog.name ) }}", "{{>basic/footer(jou=dog.name)}}"),

                Arguments.of("{{>basic/header(jou=juas.size, juas=juan)}}", "{{>basic/header(jou=juas.size, juas=juan)}}"),
                Arguments.of("{{> basic/header ( jo-u = juas.size , juas = juan ) }}", "{{>basic/header(jo-u=juas.size, juas=juan)}}")
        );
    }

    @ParameterizedTest
    @MethodSource("includesProvider")
    void test_that_parser_includes_produces_the_expected(String template, String expected) {
        var sut = new Parser("test", template);

        // Act
        var ast = sut.parse();

        assertThat(ast).hasToString(expected);
    }

    @Test
    void assert_that_2_nested_loop_should_work() {
        Ast sut = new Parser("test", "{{#i is}}{{#j js}}{{i}}{{j}}{{/}}{{/}}").parse();

        var ctx = new Context();
        ctx.def("is", List.of("a", "b", "c"));
        ctx.def("js", List.of(1, 2, 3));

        assertThat(sut.evaluate(ctx)).isEqualTo("a1a2a3b1b2b3c1c2c3");
    }
}