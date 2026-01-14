package com.narensendil.scheduler.engine.model;

public final class Diagnostic {

    public enum Severity { INFO, WARN, ERROR }

    private final Severity severity;
    private final String message;

    private Diagnostic(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public static Diagnostic error(String message) {
        return new Diagnostic(Severity.ERROR, message);
    }

    public Severity severity() { return severity; }
    public String message() { return message; }
}
