package io.homs.custache.parser;

import io.homs.custache.parser.ast.*;
import io.homs.custache.template.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.template.Template;
import io.homs.custache.template.TemplateLoadingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Custache templates (Mustache with Context)
 *
 * <pre>
 *
 * <template>		::= { (TEXT | <tag>) }
 * <tag>			::= <comment> | <if> | <ifnot> | <for> | <include> | <value>>
 *
 * <comment> 		::= "{{!}}" <template> "{{/}}"
 * <if>  			::= "{{?" <expression> "}}" <template> ["{{:}}" <template>] "{{/}}"
 * <ifnot>  		::= "{{^" <expression> "}}" <template> "{{/}}"
 * <for>  			::= "{{#" IDENT <expression> "}}" <template> "{{/}}"
 * <value> 		    ::= "{{" <expression> "}}"
 *
 * <include>	    ::= "{{>" IDENT [<var-mapping>] "}}"
 * <var-mapping>    ::= "(" IDENT "=" <expression> {"," IDENT "=" <expression>} ")"
 *
 * <expression>	    ::= IDENT {"." IDENT} ["(" <expression> ")"]
 *
 *
 *
 * XXXX millorar els includes: fer alias?
 *      {{> table(user : repository.user)}}
 *
 * XXXX que IncludeAst no carregui cada vegada per cada eval
 * XXXX que el context (d'evaluació) que no serveixi per a configuració de parser, és diferent!
 * XXXX     * <if-else>			::= "{{if" <expression> "}}" <template> ["{{else}}" <template>] "{{end}}"
 *
 * TODO     * <fn>             ::= "{{defn" IDENT ["(" IDENT {"," IDENT} ")"]  "}}" <template> "{{end}}"
 * TODO     * <apply>          ::= "{{apply" IDENT ["(" <expression> {"," <expression>} ")"]  "}}"
 * TODO     * <expression>	   ::= IDENT {"." IDENT} | "'" TEXT "'"
 * TODO     * <switch>         ::= "{{switch" <expression> "}}" <template> {"{{case" <expression> "}}" <template>} "{{end}}"
 * </pre>
 */
public class Parser {

    final TemplateLoadingStrategy templateLoadingStrategy;
    final String templateUrn;
    final Lexer lexer;

    public Parser(TemplateLoadingStrategy templateLoadingStrategy, Template template) {
        this.templateLoadingStrategy = templateLoadingStrategy;
        this.templateUrn = template.getFullTemplateUrn();
        this.lexer = new Lexer(templateUrn, template.getTemplateContent());
    }

    public Parser(Template template) {
        this(new DefaultClasspathTemplateLoadingStrategy(), template);
    }

    public Ast parse() {
        return parseTemplate();
    }

    protected TemplateAst parseTemplate() {
        return parseTemplateUntil(null);
    }

    protected TemplateAst parseTemplateUntilTag(String untilTag) {
        return parseTemplateUntil(lexer -> lexer.currentPosStartsWith(untilTag));
    }

    protected TemplateAst parseTemplateUntil(Predicate<Lexer> until) {

        var initialRow = lexer.getRow();
        var initialCol = lexer.getCol();

        List<Ast> astsList = new ArrayList<>();
        while (lexer.isNotEof()) {

            Optional<TextAst> textAstOpt = parseTextAst();
            textAstOpt.ifPresent(astsList::add);

            if (!lexer.isNotEof() || until != null && until.test(lexer)) {
                break;
            }

            final Ast tagAst = parseTag();
            astsList.add(tagAst);

            if (until != null && until.test(lexer)) {
                break;
            }
        }
        return new TemplateAst(templateUrn, initialRow, initialCol, astsList);
    }

    private Ast parseTag() {
        lexer.consumeChars("{{");
        return switch (lexer.getCurrentChar()) {
            case '!' -> parseCommentAst();
            case '?' -> parseIfAst();
            case '^' -> parseIfNotAst();
            case '#' -> parseForAst();
            case '>' -> parseInclude();
//            case '.' -> parseMethodCall();
            default -> parseValue();
        };
    }

//    private MethodCallAst parseMethodCall() {
//        int initialRow = lexer.getRow();
//        int initialCol = lexer.getCol();
//
//        lexer.consumeChars(".");
//        lexer.consumeBlanks();
//        ExpressionAst objectExpression = parseExpression();
//        lexer.consumeBlanks();
//        lexer.consumeChars("#");
//        lexer.consumeBlanks();
//        String methodName = lexer.consumeWord();
//        lexer.consumeBlanks();
//        lexer.consumeChars("(");
//        lexer.consumeBlanks();
//        ExpressionAst argumentExpression = parseExpression();
//        lexer.consumeBlanks();
//        lexer.consumeChars(")");
//        lexer.consumeBlanks();
//        lexer.consumeChars("}}");
//
//        return new MethodCallAst(templateUrn, initialRow, initialCol, objectExpression, methodName, argumentExpression);
//    }

    protected ValueAst parseValue() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");

        return new ValueAst(templateUrn, initialRow, initialCol, expressionAst);
    }

    protected IncludeAst parseInclude() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();
        lexer.consumeChars(">");
        lexer.consumeBlanks();

        int initialPos = lexer.getP();
        while (lexer.isNotEof() && !lexer.currentPosStartsWith("}}")
                && !lexer.currentPosStartsWithBlank()
                && !lexer.currentPosStartsWith("(")) {
            lexer.consumeChar();
        }

        String includeString = lexer.getString(initialPos);

        lexer.consumeBlanks();

        final List<MappingPair> mappingPairs;
        if (lexer.currentPosStartsWith("(")) {
            mappingPairs = parseVarMappings();
        } else {
            mappingPairs = new ArrayList<>();
        }
        lexer.consumeBlanks();
        lexer.consumeChars("}}");

        return new IncludeAst(templateUrn, initialRow, initialCol, templateLoadingStrategy, includeString, mappingPairs);
    }

    private List<MappingPair> parseVarMappings() {
        List<MappingPair> mappingPairs = new ArrayList<>();
        lexer.consumeChars("(");
        lexer.consumeBlanks();

        String ident = lexer.consumeWord();
        lexer.consumeBlanks();
        lexer.consumeChars("=");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeBlanks();
        mappingPairs.add(new MappingPair(ident, expressionAst));

        while (lexer.currentPosStartsWith(",")) {
            lexer.consumeChars(",");
            lexer.consumeBlanks();
            ident = lexer.consumeWord();
            lexer.consumeBlanks();
            lexer.consumeChars("=");
            lexer.consumeBlanks();
            expressionAst = parseExpression();
            lexer.consumeBlanks();
            mappingPairs.add(new MappingPair(ident, expressionAst));
        }
        lexer.consumeChars(")");

        return mappingPairs;
    }

    protected CommentAst parseCommentAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("!}}");
        TemplateAst ignoredBody = parseTemplateUntilTag("{{/}}");
        lexer.consumeChars("{{/}}");

        return new CommentAst(templateUrn, initialRow, initialCol, ignoredBody);
    }

    protected IfElseAst parseIfAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("?");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeBlanks();
        lexer.consumeChars("}}");
        TemplateAst ifAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{:}}") || lexer.currentPosStartsWith("{{/}}"));

        TemplateAst elseAst = null;
        if (lexer.currentPosStartsWith("{{:}}")) {
            lexer.consumeChars("{{:}}");
            elseAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{/}}"));
        }

        lexer.consumeChars("{{/}}");

        return new IfElseAst(templateUrn, initialRow, initialCol, expressionAst, ifAst, elseAst);
    }

    protected IfNotAst parseIfNotAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("^");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeBlanks();
        lexer.consumeChars("}}");
        TemplateAst ifAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{:}}") || lexer.currentPosStartsWith("{{/}}"));

        TemplateAst elseAst = null;
        if (lexer.currentPosStartsWith("{{:}}")) {
            lexer.consumeChars("{{:}}");
            elseAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{/}}"));
        }

        lexer.consumeChars("{{/}}");
        return new IfNotAst(templateUrn, initialRow, initialCol, expressionAst, ifAst, elseAst);
    }

    protected ForAst parseForAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("#");
        lexer.consumeBlanks();
        String ident = lexer.consumeWord();
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplateUntilTag("{{/}}");
        lexer.consumeChars("{{/}}");
        return new ForAst(templateUrn, initialRow, initialCol, ident, expressionAst, bodyAst);
    }

    protected ExpressionAst parseExpression() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeBlanks();
        List<String> accessors = new ArrayList<>();
        if (lexer.isNotEof()) {
            accessors.add(lexer.consumeWord());
            while (lexer.isNotEof() && lexer.currentPosStartsWith(".")) {
                lexer.consumeChars(".");
                accessors.add(lexer.consumeWord());
            }
        }

        ExpressionAst expressionArg = null;
        lexer.consumeBlanks();
        if (lexer.getCurrentChar() == '(') {
            lexer.consumeChar();
            lexer.consumeBlanks();
            expressionArg = parseExpression();
            lexer.consumeBlanks();
            lexer.consumeChars(")");
        }
        lexer.consumeBlanks();

        return new ExpressionAst(templateUrn, initialRow, initialCol, accessors, expressionArg);
    }

    protected Optional<TextAst> parseTextAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        int textStartPos = lexer.getP();
        while (lexer.isNotEof() && !lexer.currentPosStartsWith("{{")) {
            lexer.consumeChar();
        }
        String text = lexer.getString(textStartPos);
        if (text.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TextAst(templateUrn, initialRow, initialCol, text));
    }
}
