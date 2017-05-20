package com.awplab.core.vaadin.service;

import javax.servlet.ServletException;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public interface VaadinManager {

    Set<VaadinProvider> getProviders();

    void registerProvider(VaadinProvider vaadinProvider) throws ServletException, InvalidVaadinProvider;

    void unregisterProvider(VaadinProvider vaadinProvider) throws VaadinProviderNotFound;


}
