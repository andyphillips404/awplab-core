package com.awplab.core.vaadin.service;

import com.vaadin.server.VaadinServlet;

import javax.servlet.ServletException;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface VaadinManager {

    Set<VaadinProvider> getProviders();

    Set<VaadinServlet> getServlets();

    void registerProvider(VaadinProvider vaadinProvider) throws ServletException;

    void unregisterProvider(VaadinProvider vaadinProvider);

}
