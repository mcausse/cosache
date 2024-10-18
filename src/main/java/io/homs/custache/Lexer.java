package io.homs.custache;


import lombok.Getter;


@Getter
public class Lexer {

    final String templateId;
    final String content;

    int p;
    int row;
    int col;

    public Lexer(String templateId, String content) {
        this.templateId = templateId;
        this.content = content;
        this.p = 0;
        this.row = 1;
        this.col = 1;
    }

    public boolean isNotEof() {
        return p < content.length();
    }

    public boolean currentPosStartsWith(String prefix) {
        return isNotEof() && content.startsWith(prefix, p);
    }

    public void consumeChar() {
        if (content.charAt(p) == '\n') {
            row++;
            col = 1;
        } else {
            col++;
        }
        this.p++;
    }

    public void consumeBlanks() {
        while (Character.isWhitespace(content.charAt(p))) {
            consumeChar();
        }
    }

    public String consumeWord() {
        int initialP = p;
        while (isNotEof() && Character.isJavaIdentifierPart(content.charAt(p))) {
            consumeChar();
        }
        return getString(initialP);
    }

    public void consumeChars(String prefix) {
        if (!currentPosStartsWith(prefix)) {
            throw new RuntimeException("expected: " + prefix + ", at: " + templateId + ":" + row + "," + col);
        }
        for (int i = 0; i < prefix.length(); i++) {
            consumeChar();
        }
    }

    public String getString(int textStartPos) {
        return this.content.substring(textStartPos, p);
    }

    public char getCurrentChar() {
        return content.charAt(p);
    }
}