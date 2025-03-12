package io.homs.custache;

public class CustacheException extends RuntimeException {

    final String templateUrn;
    final int row;
    final int col;

    public CustacheException(String message, String templateUrn, int row, int col) {
        super(message + templateUrn + ":" + row + "," + col);
        this.templateUrn = templateUrn;
        this.row = row;
        this.col = col;
    }

    public CustacheException(String message, String templateUrn, int row, int col, Throwable cause) {
        super(message + templateUrn + ":" + row + "," + col, cause);
        this.templateUrn = templateUrn;
        this.row = row;
        this.col = col;
    }
}
