package ru.mirent.webmvc.interceptor;

import java.net.http.HttpResponse;
import java.util.function.BiConsumer;

public class onResponse implements BiConsumer<HttpResponse<?>, Object> {
    @Override
    public void accept(HttpResponse<?> httpResponse, Object t) {
        System.out.println(">> onResponse " + httpResponse.body());
        System.out.println(">> onResponse " + t);
    }
}
