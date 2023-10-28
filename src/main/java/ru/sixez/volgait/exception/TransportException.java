package ru.sixez.volgait.exception;

public class TransportException extends RuntimeException {
    public TransportException() {
        super();
    }

    public TransportException(String msg) {
        super(msg);
    }
}
