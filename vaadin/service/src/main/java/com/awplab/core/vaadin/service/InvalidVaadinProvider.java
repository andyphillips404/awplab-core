package com.awplab.core.vaadin.service;

/**
 * Created by andyphillips404 on 5/19/17.
 */
public class InvalidVaadinProvider extends Exception {
    public InvalidVaadinProvider() {
    }

    public InvalidVaadinProvider(String message) {
        super(message);
    }
}
