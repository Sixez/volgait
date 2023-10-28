package ru.sixez.volgait.exception;

public class AccountException extends RuntimeException {
    public AccountException() {
        super();
    }

    public AccountException(String msg) {
        super(msg);
    }
}
