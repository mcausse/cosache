package io.homs.custache.template;

import lombok.Value;

@Value
public class Template {

    String fullTemplateUrn;
    String templateContent;

    public static Template of(String fullTemplateUrn, String templateContent) {
        return new Template(fullTemplateUrn, templateContent);
    }
}
