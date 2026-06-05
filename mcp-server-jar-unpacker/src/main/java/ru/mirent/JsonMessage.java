package ru.mirent;

/**
 * Представление JSON-RPC сообщения
 */
public class JsonMessage {
    public Object id;
    public String method;
    public Object params;
    public Object result;
    public Object error;
}
