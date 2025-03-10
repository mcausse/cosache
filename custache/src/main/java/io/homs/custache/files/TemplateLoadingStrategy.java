package io.homs.custache.files;

import lombok.Value;

public interface TemplateLoadingStrategy {

    Template loadTemplate(String templateUrn);

    @Value
    class Template {
        String fullTemplateUrn;
        String templateContent;
    }
}
