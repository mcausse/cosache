package io.homs.custache;

import io.homs.custache.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Costache templates (Mustache with Context)
 *
 * <pre>
 *
 * <template>		::= { (TEXT | <tag>) }
 * <tag>			::= <comment> | <if> | <ifnot> | <for> | <value>
 *
 * <comment> 		::= "{{!}}" TEXT "{{/}}"
 * <if>  			::= "{{?" <expression> "}}" <template> "{{/}}"
 * <ifnot>  		::= "{{^" <expression> "}}" <template> "{{/}}"
 * <for>  			::= "{{#" IDENT <expression> "}}" <template> "{{/}}"
 * <value> 		    ::= "{{" <expression> "}}"
 *
 * <expression>	::= IDENT {"." IDENT}
 *
 * </pre>
 */
public class Parser {

    final String templateId;
    final Lexer lexer;

    public Parser(String templateId, String template) {
        this.templateId = templateId;
        this.lexer = new Lexer(templateId, template);
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

            // parse Tag
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
                default:
                    tagAst = parseValue();
            }
            astsList.add(tagAst);

            if (expectEnderTag && lexer.currentPosStartsWith("{{/}}")) {
                break;
            }
        }
        return new TemplateAst(templateId, initialRow, initialCol, astsList);
    }

    protected ValueAst parseValue() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");

        return new ValueAst(templateId, initialRow, initialCol, expressionAst);
    }

    protected CommentAst parseCommentAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("!}}");
        Optional<TextAst> textOpt = parseTextAst();
        lexer.consumeChars("{{/}}");

        return new CommentAst(templateId, initialRow, initialCol, textOpt.orElse(null));
    }

    protected IfAst parseIfAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("?");
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplate(true);
        lexer.consumeChars("{{/}}");
        return new IfAst(templateId, initialRow, initialCol, expressionAst, bodyAst);
    }

    protected IfNotAst parseIfNotAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("^");
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplate(true);
        lexer.consumeChars("{{/}}");
        return new IfNotAst(templateId, initialRow, initialCol, expressionAst, bodyAst);
    }

    protected ForAst parseForAst() {
        int initialRow = lexer.getRow();
        int initialCol = lexer.getCol();

        lexer.consumeChars("#");
        String ident = lexer.consumeWord();
        lexer.consumeBlanks();
        ExpressionAst expressionAst = parseExpression();
        lexer.consumeChars("}}");
        TemplateAst bodyAst = parseTemplate(true);
        lexer.consumeChars("{{/}}");
        return new ForAst(templateId, initialRow, initialCol, ident, expressionAst, bodyAst);
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
            throw new RuntimeException("unexpected eof while parsing an expression, at: " + templateId + ":" + initialRow + "," + initialCol);
        }
        return new ExpressionAst(templateId, initialRow, initialCol, accessors);
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
        return Optional.of(new TextAst(templateId, initialRow, initialCol, text));
    }
}
