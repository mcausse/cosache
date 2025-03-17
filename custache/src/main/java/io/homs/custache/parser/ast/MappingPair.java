package io.homs.custache.parser.ast;

import lombok.Value;

@Value
public class MappingPair {
    String left;
    ExpressionAst right;
}