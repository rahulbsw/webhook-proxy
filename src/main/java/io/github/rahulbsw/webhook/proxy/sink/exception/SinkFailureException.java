package io.github.rahulbsw.webhook.proxy.sink.exception;

public class SinkFailureException extends Exception{
    public SinkFailureException() {
    }

    public SinkFailureException(String message) {
        super(message);
    }

    public SinkFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SinkFailureException(Throwable cause) {
        super(cause);
    }

    public SinkFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
