package ru.mirent.webmvc.interceptor;

import java.util.function.BiConsumer;

public class onError implements BiConsumer<Throwable, Object>  {
    @Override
    public void accept(Throwable throwable, Object s) {
        System.out.println(">> onError");
    }
}
