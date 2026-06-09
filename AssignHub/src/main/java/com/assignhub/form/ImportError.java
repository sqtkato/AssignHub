package com.assignhub.form;

public class ImportError {
    private String row;
    private String target;
    private String message;

    public ImportError(String row, String target, String message) {
        this.row = row;
        this.target = target;
        this.message = message;
    }
    public String getRow() { return row; }
    public String getTarget() { return target; }
    public String getMessage() { return message; }
}