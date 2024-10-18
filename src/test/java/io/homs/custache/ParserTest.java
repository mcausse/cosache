package io.homs.custache;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
}