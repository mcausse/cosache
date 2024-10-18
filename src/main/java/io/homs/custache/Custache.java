package io.homs.custache;

import io.homs.custache.ast.Ast;
import lombok.SneakyThrows;

public class Custache {

    public static Ast parse(String templateId, String templateContent) {
        return new Parser(templateId, templateContent).parse();
    }

    @SneakyThrows
    public static String templateFromClasspath(String templateName, String modelName, Object model) {
        String template = FileUtils.loadFromClasspath(templateName);
        Context ctx = new Context();
        ctx.def(modelName, model);
        String result = parse(templateName, template).evaluate(ctx);
        return result;
    }
}
