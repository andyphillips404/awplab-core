package com.awplab.core.vaadin.service.provider;

import com.awplab.core.common.EventAdminHelper;
import com.awplab.core.vaadin.service.*;
import com.awplab.core.vaadin.service.events.VaadinEventData;
import com.awplab.core.vaadin.service.events.VaadinEventTopics;
import com.vaadin.server.VaadinServlet;
import org.apache.felix.ipojo.annotations.*;
import org.ops4j.pax.web.service.WebContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.*;

/**
 * Created by andyphillips404 on 8/11/16.
 */
@Component(publicFactory = false, immediate = true)
@Instantiate
@Provides(specifications = {VaadinManager.class})
public class VaadinManagerProvider implements VaadinManager {

    @Requires
    WebContainer webContainer;

    private VaadinServlet bootstrapVaadinServlet;

    private Logger logger = LoggerFactory.getLogger(VaadinManagerProvider.class);

    private Set<VaadinProvider> providers = Collections.synchronizedSet(new HashSet<>());
    private Map<VaadinProvider, VaadinServlet> servlets = Collections.synchronizedMap(new HashMap<>());

    @Validate
    private void start() {
        bootstrapVaadinServlet = new VaadinServlet();
        try {
            webContainer.registerServlet(bootstrapVaadinServlet, new String[]{"/VAADIN/*"}, null, null);
        }
        catch (ServletException ex) {
            logger.error("Exception registering boostrapVaadinServlet!", ex);
        }
        logger.info("VaadinManager started, Registered boostrapVaadinServlet!");

        EventAdminHelper.postEvent(VaadinEventTopics.MANAGER_STARTED);
    }

    @Invalidate
    private void stop() {

        if (bootstrapVaadinServlet != null) webContainer.unregisterServlet(bootstrapVaadinServlet);
        providers.forEach(vaadinProvider -> {
            try {
                unregisterProvider(vaadinProvider);
            }
            catch (Exception ex) {
                logger.error("Exception unregistering provider at path " + vaadinProvider.getPath(), ex);
            }
        });

        logger.info("VaadinManager stoped");
        EventAdminHelper.postEvent(VaadinEventTopics.MANAGER_STOPED);

    }


    @Bind(optional = true, aggregate = true)
    public void bindProvider(VaadinProvider vaadinProvider) {
        try {
            registerProvider(vaadinProvider);
        }
        catch (Exception ex) {
            logger.error("Exception registering vaadin provider!", ex);
        }
    }

    @Unbind(optional = true, aggregate = true)
    public void unbindProvider(VaadinProvider vaadinProvider) {

        try {
            unregisterProvider(vaadinProvider);
        }
        catch (Exception ex) {
            logger.error("Exception registering vaadin provider!", ex);
        }
    }

    @Override
    public Set<VaadinProvider> getProviders() {
        return Collections.unmodifiableSet(providers);
    }


    @Override
    public void registerProvider(VaadinProvider vaadinProvider) throws ServletException, InvalidVaadinProvider {
        if (vaadinProvider.getPath() == null || !vaadinProvider.getPath().startsWith("/") || vaadinProvider.getPath().endsWith("/")) {
            throw new InvalidVaadinProvider("Invalid path, path must not be null, start with a / and end with no /");
        }

        //if (!(vaadinProvider instanceof VaadinServlet)) {
        //    throw new InvalidVaadinProvider("Vaading provider must extend vaadin servlet");
        //}
        if (providers.contains(vaadinProvider)) throw new InvalidVaadinProvider("VaadinProvider already registerd");

        VaadinServlet vaadinServlet = new BaseVaadinServlet(vaadinProvider);

        Dictionary<String, Object> initParams = new Hashtable<String, Object>();
        initParams.put("productionMode", vaadinProvider.productionMode() ? "true" : "false");
        vaadinProvider.heartbeatInterval().ifPresent(integer -> {initParams.put("heartbeatInterval", Integer.toString(integer)); });
        vaadinProvider.closeIdleSessions().ifPresent(close -> {initParams.put("closeIdleSessions", close ? "true" : "false"); });
        vaadinProvider.pushMode().ifPresent(pushMode -> {initParams.put("pushmode", pushMode.toString().toLowerCase());});
        //initParams.put("UIProvider", new VaadinUIProvider(vaadinProvider, vaadinServlet));

        webContainer.registerServlet(vaadinServlet, new String[] { vaadinProvider.getPath() + "/*"}, initParams, 0, true, (vaadinProvider instanceof BasicAuthRequired ? new BasicAuthHttpContext(webContainer.getDefaultSharedHttpContext(), (BasicAuthRequired)vaadinProvider) : webContainer.getDefaultSharedHttpContext()));

        servlets.put(vaadinProvider, vaadinServlet);

        EventAdminHelper.postEvent(VaadinEventTopics.PROVIDER_ADDED, VaadinEventData.VAADIN_PROVIDER, vaadinProvider);


    }

    @Override
    public void unregisterProvider(VaadinProvider vaadinProvider) throws VaadinProviderNotFound {
        if (!providers.contains(vaadinProvider)) throw new VaadinProviderNotFound();

        webContainer.unregisterServlet(servlets.get(vaadinProvider));
        providers.remove(vaadinProvider);
        servlets.remove(vaadinProvider);

        EventAdminHelper.postEvent(VaadinEventTopics.PROVIDER_REMOVED, VaadinEventData.VAADIN_PROVIDER, vaadinProvider);
    }



}
