package com.awplab.core.vaadin.service.provider;

import com.awplab.core.vaadin.service.VaadinProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import org.osgi.framework.FrameworkUtil;

/**
 * Created by andyphillips404 on 5/20/17.
 */
public class VaadinUIProvider extends UIProvider {

    final VaadinProvider vaadinProvider;
    final VaadinServlet vaadinServlet;


    public VaadinUIProvider(VaadinProvider vaadinProvider, VaadinServlet vaadinServlet) {
        this.vaadinProvider = vaadinProvider;
        this.vaadinServlet = vaadinServlet;
    }

    @Override
    public UI createInstance(UICreateEvent event) {
        //return super.createInstance(event);
        return vaadinProvider.getUI();

    }

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent uiClassSelectionEvent) {
        return vaadinProvider.getUIClass();
    }
}
