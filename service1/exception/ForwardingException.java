package ru.kovyazin.lab4.service1.exception;

public class ForwardingException extends RuntimeException {
    public ForwardingException(Throwable cause) {
        super("Не удалось передать запрос в Сервис 2", cause);
    }
}
