package io.homs.custache.spring;

import io.homs.custache.Parser;
import io.homs.custache.ast.Ast;
import io.homs.custache.files.TemplateLoadingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class ToolFactory implements FactoryBean<Ast> {

    private TemplateLoadingStrategy templateLoadingStrategy;
    private String templateUrn;
    private String template;

    @Override
    public Ast getObject() throws Exception {
        return new Parser(templateLoadingStrategy, templateUrn, template).parse();
    }

    @Override
    public Class<?> getObjectType() {
        return Ast.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public TemplateLoadingStrategy getTemplateLoadingStrategy() {
        return templateLoadingStrategy;
    }

    public void setTemplateLoadingStrategy(TemplateLoadingStrategy templateLoadingStrategy) {
        this.templateLoadingStrategy = templateLoadingStrategy;
    }

    public String getTemplateUrn() {
        return templateUrn;
    }

    public void setTemplateUrn(String templateUrn) {
        this.templateUrn = templateUrn;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}