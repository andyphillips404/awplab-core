package com.awplab.core.vaadin.service;

import javax.servlet.ServletException;
import java.util.Optional;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface VaadinManager {

    Set<VaadinProvider> getProviders();

    Set<BaseVaadinServlet> getServlets();

    void registerProvider(VaadinProvider vaadinProvider) throws ServletException;

    void unregisterProvider(VaadinProvider vaadinProvider);

    Optional<BaseVaadinServlet> findServlet(VaadinProvider vaadinProvider);


}
