package com.awplab.core.vaadin.service;


import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.UI;

import java.util.Optional;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface VaadinProvider{


    String getPath();

    default boolean productionMode() {
        return false;
    }

    default Optional<Integer> heartbeatInterval() {
        return Optional.empty();
    }

    default Optional<Boolean> closeIdleSessions() { return Optional.empty(); }

    default void servletInitialized(VaadinServlet vaadinServlet) {

    }

    default Optional<PushMode> pushMode() {
        return Optional.empty();
    }

    static void doAccess(UI ui, Runnable runnable) {

        if (ui != null && ui.isAttached()) {
            ui.access(runnable);
        } else {
            runnable.run();
        }
    }

    Class<? extends UI> getUIClass();

    default UI getUI() {
        try {
            return getUIClass().newInstance();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }



}
