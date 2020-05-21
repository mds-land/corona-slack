package com.github.mdsina.corona;

import io.micronaut.runtime.Micronaut;
import reactor.core.publisher.Hooks;

public class Application {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        Micronaut.run(Application.class);
    }
}