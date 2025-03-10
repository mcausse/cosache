package io.homs.custache.spring;

import io.homs.custache.ast.Ast;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = ToolFactoryTest.CustacheSpringConfiguration.class)
class ToolFactoryTest {

    @Configuration
    @ComponentScan("io.homs.custache.spring")
    public static class CustacheSpringConfiguration {

    }

    @Autowired
    private ToolFactory toolFactory;

//    @Resource(name = "singleTool")
//    private Ast a;

    @Test
    void testGetObject() throws Exception {
//        TemplateLoadingStrategy strategy = new DefaultClasspathTemplateLoadingStrategy("", "");
//        toolFactory.setTemplateLoadingStrategy(strategy);
//        toolFactory.setTemplateUrn("testUrn");
//        toolFactory.setTemplate("testTemplate");
//
//        Ast ast = toolFactory.getObject();
//        assertThat(ast).isNotNull();
    }
}