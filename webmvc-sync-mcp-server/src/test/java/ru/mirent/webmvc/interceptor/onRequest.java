package ru.mirent.webmvc.interceptor;

import java.net.http.HttpRequest;
import java.util.Optional;
import java.util.function.Function;

public class onRequest implements Function<HttpRequest, Object>  {
    @Override
    public Object apply(HttpRequest httpRequest) {
        Optional<HttpRequest.BodyPublisher> bodyPublisher = httpRequest.bodyPublisher();
        if (bodyPublisher.isPresent()) {
            System.out.println(">> onRequest" + bodyPublisher.get().toString());
        }
        System.out.println(">> onRequest");
        return null;
    }
}
