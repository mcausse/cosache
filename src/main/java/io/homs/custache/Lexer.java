package io.homs.custache;

import lombok.Getter;

@Getter
public class Lexer {

    final String templateUrn;
    final String content;

    int p;
    int row;
    int col;

    public Lexer(String templateUrn, String content) {
        this.templateUrn = templateUrn;
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

    public boolean currentPosStartsWithBlank() {
        return isNotEof() && Character.isWhitespace(content.charAt(p));
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
        while (isNotEof() && Character.isWhitespace(content.charAt(p))) {
            consumeChar();
        }
    }

    public String consumeWord() {
        int initialP = p;
        while (isNotEof() && isWordChar(content.charAt(p))) {
            consumeChar();
        }
        var r = getString(initialP);
        if (r.isEmpty()) {
            throw new RuntimeException("expected to consume an identifiers, but not; at: " + templateUrn + ":" + row + "," + col);
        }
        return r;
    }

    protected boolean isWordChar(char c) {
        return Character.isJavaIdentifierPart(c) || c == '-';
    }

    public void consumeChars(String prefix) {
        if (p + prefix.length() > content.length()) {
            throw new RuntimeException("expected: " + prefix + ", but eof; at: " + templateUrn + ":" + row + "," + col);
        }
        if (!currentPosStartsWith(prefix)) {
            throw new RuntimeException("expected: " + prefix + ", at: " + templateUrn + ":" + row + "," + col);
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