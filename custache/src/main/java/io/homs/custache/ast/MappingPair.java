package io.homs.custache.ast;

import lombok.Value;

@Value
public class MappingPair {
    String left;
    ExpressionAst right;
}