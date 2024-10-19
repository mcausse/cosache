package io.homs.custache.files;

import io.homs.custache.ast.Ast;

public interface TemplateLoadingStrategy {

    Ast loadParseredTemplate(String templateUrn);
}
