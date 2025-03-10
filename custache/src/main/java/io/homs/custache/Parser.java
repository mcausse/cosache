package io.homs.custache;

import io.homs.custache.ast.Ast;
import io.homs.custache.ast.CommentAst;
import io.homs.custache.ast.ExpressionAst;
import io.homs.custache.ast.ForAst;
import io.homs.custache.ast.IfAst;
import io.homs.custache.ast.IfElseAst;
import io.homs.custache.ast.IfNotAst;
import io.homs.custache.ast.IncludeAst;
import io.homs.custache.ast.MappingPair;
import io.homs.custache.ast.TemplateAst;
import io.homs.custache.ast.TextAst;
import io.homs.custache.ast.ValueAst;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;

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
 * <tag>			::= <comment> | <if> | <ifnot> | <for> | <include> | <value> | <if-else>
 *
 * <comment> 		::= "{{!}}" TEXT "{{/}}"
 * <if>  			::= "{{?" <expression> "}}" <template> "{{/}}"
 * <ifnot>  		::= "{{^" <expression> "}}" <template> "{{/}}"
 * <for>  			::= "{{#" IDENT <expression> "}}" <template> "{{/}}"
 * <value> 		    ::= "{{" <expression> "}}"
 *
 * <if-else>		::= "{{if" <expression> "}}" <template> ["{{else}}" <template>] "{{end}}"
 *
 * <include>	    ::= "{{>" IDENT [<var-mapping>] "}}"
 * <var-mapping>    ::= "(" IDENT "=" <expression> {"," IDENT "=" <expression>} ")"
 *
 * <expression>	    ::= IDENT {"." IDENT}
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
 * </pre>
 */
public class Parser {

    final TemplateLoadingStrategy templateLoadingStrategy;
    final String templateUrn;
    final Lexer lexer;

    public Parser(TemplateLoadingStrategy templateLoadingStrategy, String templateUrn, String template) {
        this.templateLoadingStrategy = templateLoadingStrategy;
        this.templateUrn = templateUrn;
        this.lexer = new Lexer(templateUrn, template);
    }

    public Parser(String templateUrn, String template) {
        this(new DefaultClasspathTemplateLoadingStrategy(), templateUrn, template);
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
            default -> {
                if (lexer.currentPosStartsWith("if")) {
                    yield parseIfElseAst();
//                } else if (lexer.currentPosStartsWith("defn")) {
//                    yield parseDefnAst();
//                } else if (lexer.currentPosStartsWith("apply")) {
//                    yield parseApplyAst();
                } else {
                    // is an expression
                    yield parseValue();
                }
            }
        };
    }

//    private Ast parseApplyAst() {
//        int initialRow = lexer.getRow();
//        int initialCol = lexer.getCol();
//
//        lexer.consumeChars("apply");
//        lexer.consumeBlanks();
//        String ident = lexer.consumeWord();
//        lexer.consumeBlanks();
//
//        final List<ExpressionAst> argumentExpressions = new ArrayList<>();
//
//        if (lexer.currentPosStartsWith("(")) {
//            lexer.consumeChars("(");
//            argumentExpressions.add(parseExpression());
//            lexer.consumeBlanks();
//            while (lexer.getCurrentChar() == ',') {
//                lexer.consumeChars(",");
//                argumentExpressions.add(parseExpression());
//                lexer.consumeBlanks();
//            }
//            lexer.consumeBlanks();
//            lexer.consumeChars(")");
//        }
//        lexer.consumeBlanks();
//        lexer.consumeChars("}}");
//
//        return new ApplyAst(templateUrn, initialRow, initialCol, ident, argumentExpressions);
//    }
//
//    private DefnAst parseDefnAst() {
//        int initialRow = lexer.getRow();
//        int initialCol = lexer.getCol();
//
//        lexer.consumeChars("defn");
//        lexer.consumeBlanks();
//        String functionName = lexer.consumeWord();
//        lexer.consumeBlanks();
//
//        final List<String> argumentNames = new ArrayList<>();
//        if (lexer.currentPosStartsWith("(")) {
//            lexer.consumeChars("(");
//            lexer.consumeBlanks();
//            argumentNames.add(lexer.consumeWord());
//            lexer.consumeBlanks();
//            while (lexer.getCurrentChar() == ',') {
//                lexer.consumeChars(",");
//                lexer.consumeBlanks();
//                argumentNames.add(lexer.consumeWord());
//                lexer.consumeBlanks();
//            }
//            lexer.consumeBlanks();
//            lexer.consumeChars(")");
//        }
//        lexer.consumeBlanks();
//        lexer.consumeChars("}}");
//        TemplateAst body = parseTemplateUntilTag("{{end}}");
//        lexer.consumeChars("{{end}}");
//        return new DefnAst(templateUrn, initialRow, initialCol, functionName, argumentNames, body);
//    }

    private IfElseAst parseIfElseAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("if");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeBlanks();
        lexer.consumeChars("}}");
        TemplateAst ifAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{else}}") || lexer.currentPosStartsWith("{{end}}"));

        TemplateAst elseAst = null;
        if (lexer.currentPosStartsWith("{{else}}")) {
            lexer.consumeChars("{{else}}");
            elseAst = parseTemplateUntil(lexer -> lexer.currentPosStartsWith("{{end}}"));
        }

        lexer.consumeChars("{{end}}");

        return new IfElseAst(templateUrn, initialRow, initialCol, expressionAst, ifAst, elseAst);
    }

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
        if (!lexer.isNotEof()) {
            throw new RuntimeException("expected closing }}, but eof; at " + templateUrn + ":" + initialRow + "," + initialCol);
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
        Optional<TextAst> textOpt = parseTextAst();
        lexer.consumeChars("{{/}}");

        return new CommentAst(templateUrn, initialRow, initialCol, textOpt.orElse(null));
    }

    protected IfAst parseIfAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("?");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplateUntilTag("{{/}}");
        lexer.consumeChars("{{/}}");
        return new IfAst(templateUrn, initialRow, initialCol, expressionAst, bodyAst);
    }

    protected IfNotAst parseIfNotAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("^");
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplateUntilTag("{{/}}");
        lexer.consumeChars("{{/}}");
        return new IfNotAst(templateUrn, initialRow, initialCol, expressionAst, bodyAst);
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

        if (!lexer.isNotEof()) {
            throw new RuntimeException("unexpected eof while parsing an expression, at: " + templateUrn + ":" + initialRow + "," + initialCol);
        }
        return new ExpressionAst(templateUrn, initialRow, initialCol, accessors);
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
