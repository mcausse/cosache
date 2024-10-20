package io.homs.custache;

import io.homs.custache.ast.*;
import io.homs.custache.files.DefaultClasspathTemplateLoadingStrategy;
import io.homs.custache.files.TemplateLoadingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Costache templates (Mustache with Context)
 *
 * <pre>
 *
 * <template>		::= { (TEXT | <tag>) }
 * <tag>			::= <comment> | <if> | <ifnot> | <for> | <include> | <value>
 *
 * <comment> 		::= "{{!}}" TEXT "{{/}}"
 * <if>  			::= "{{?" <expression> "}}" <template> "{{/}}"
 * <ifnot>  		::= "{{^" <expression> "}}" <template> "{{/}}"
 * <for>  			::= "{{#" IDENT <expression> "}}" <template> "{{/}}"
 * <value> 		    ::= "{{" <expression> "}}"
 *
 * <include>	    ::= "{{>" IDENT ["(" IDENT "=" <expression> {"," IDENT "=" <expression>} ")"] "}}"
 *
 * <expression>	    ::= IDENT {"." IDENT}
 *
 *
 * TODO millorar els includes: fer alias?
 *      {{> table(user : repository.user)}}
 *
 * XXXX que IncludeAst no carregui cada vegada per cada eval
 * XXXX que el context (d'evaluació) que no serveixi per a configuració de parser, és diferent!
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
        return parseTemplate(false);
    }

    protected TemplateAst parseTemplate(boolean expectEnderTag) {
        var initialRow = lexer.getRow();
        var initialCol = lexer.getCol();

        List<Ast> astsList = new ArrayList<>();
        while (lexer.isNotEof()) {

            Optional<TextAst> textAstOpt = parseTextAst();
            textAstOpt.ifPresent(astsList::add);

            if (!lexer.isNotEof() || expectEnderTag && lexer.currentPosStartsWith("{{/}}")) {
                break;
            }

            final Ast tagAst = parseTag();
            astsList.add(tagAst);

            if (expectEnderTag && lexer.currentPosStartsWith("{{/}}")) {
                break;
            }
        }
        return new TemplateAst(templateUrn, initialRow, initialCol, astsList);
    }

    private Ast parseTag() {
        lexer.consumeChars("{{");
        final Ast tagAst;
        switch (lexer.getCurrentChar()) {
            case '!':
                tagAst = parseCommentAst();
                break;
            case '?':
                tagAst = parseIfAst();
                break;
            case '^':
                tagAst = parseIfNotAst();
                break;
            case '#':
                tagAst = parseForAst();
                break;
            case '>':
                tagAst = parseForInclude();
                break;
            default:
                tagAst = parseValue();
        }
        return tagAst;
    }

    protected ValueAst parseValue() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");

        return new ValueAst(templateUrn, initialRow, initialCol, expressionAst);
    }

    protected IncludeAst parseForInclude() {
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

        List<IncludeAst.MappingPair> mappingPairs = new ArrayList<>();
        if (lexer.currentPosStartsWith("(")) {
            lexer.consumeChars("(");
            lexer.consumeBlanks();

            // es parsejen els mappings:
            // "(" IDENT "=" <expression> {"," IDENT "=" <expression>} ")"

            String ident = lexer.consumeWord();
            lexer.consumeBlanks();
            lexer.consumeChars("=");
            lexer.consumeBlanks();
            ExpressionAst expressionAst = parseExpression();
            lexer.consumeBlanks();
            mappingPairs.add(new IncludeAst.MappingPair(ident, expressionAst));

            while (lexer.currentPosStartsWith(",")) {
                lexer.consumeChars(",");
                lexer.consumeBlanks();
                ident = lexer.consumeWord();
                lexer.consumeBlanks();
                lexer.consumeChars("=");
                lexer.consumeBlanks();
                expressionAst = parseExpression();
                lexer.consumeBlanks();
                mappingPairs.add(new IncludeAst.MappingPair(ident, expressionAst));
            }
            lexer.consumeChars(")");
        }
        lexer.consumeBlanks();
        lexer.consumeChars("}}");

        return new IncludeAst(templateUrn, initialRow, initialCol, templateLoadingStrategy, includeString, mappingPairs);
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
        TemplateAst bodyAst = parseTemplate(true);
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
        TemplateAst bodyAst = parseTemplate(true);
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
        TemplateAst bodyAst = parseTemplate(true);
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
