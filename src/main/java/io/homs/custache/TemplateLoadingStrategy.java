package io.homs.custache;

import io.homs.custache.ast.Ast;

public interface TemplateLoadingStrategy {

    Ast loadParseredTemplate(String templateUrn);
}
