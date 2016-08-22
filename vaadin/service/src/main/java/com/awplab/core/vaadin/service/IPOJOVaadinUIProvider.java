package com.awplab.core.vaadin.service;

import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 8/11/16.
 */
public class IPOJOVaadinUIProvider extends VaadinUIProvider {

    private Set<InstanceManager> instanceManagers = Collections.synchronizedSet(new HashSet<>());

    private final Class<? extends UI> uiClass;

    public IPOJOVaadinUIProvider(Class<? extends UI> uiClass) {
        this.uiClass = uiClass;
    }

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        return uiClass;
    }

    @Override
    public UI createInstance(UICreateEvent event) {
        //return super.createInstance(event);

        Logger logger = LoggerFactory.getLogger(IPOJOVaadinUIProvider.class);

        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(IPOJOVaadinUIProvider.class).getBundleContext();
            ServiceReference[] refs = bundleContext.getServiceReferences(Factory.class.getName(), "|(instance.name=" + uiClass.getName() +")(service.pid=" + uiClass.getName() +")");
            if (refs != null && refs.length > 0) {
                Factory factory = (Factory) bundleContext.getService(refs[0]);
                ComponentInstance componentInstance = factory.createComponentInstance(null);
                instanceManagers.add((InstanceManager) componentInstance);
                return (UI) ((InstanceManager) componentInstance).getPojoObject();
            }
            else {
                logger.error("Unable to find ipojo factory for class: " + uiClass.getName());
            }

        }
        catch (Exception ex) {
            logger.error("Exception creating instance of class: " + uiClass.getName(), ex);
        }
        return null;

    }

    @Override
    public void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        instanceManagers.removeIf(instanceManager -> {
            if (sessionDestroyEvent.getSession().getUIs().contains((UI)instanceManager.getPojoObject())) {
                instanceManager.dispose();
                return true;
            }
            return false;
        });

    }


    @Override
    public void shutdown() {
        for (InstanceManager instanceManager : instanceManagers) {
            instanceManager.dispose();
        }
        instanceManagers.clear();
    }
}
