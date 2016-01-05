package com.awplab.core.rest.service.manager;

import com.awplab.core.rest.service.RestApplication;
import com.awplab.core.rest.service.RestManagerService;
import com.awplab.core.rest.service.RestService;
import org.apache.felix.ipojo.annotations.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by andyphillips404 on 2/22/15.
 */
@Component(immediate = true, publicFactory=false, managedservice = "com.awplab.rest.service.manager")
@Instantiate
@Provides
public class RestManagerProvider implements RestManagerService {

    private Set<RestService> restProviders = Collections.synchronizedSet(new HashSet<>());

    private Map<String, ServletContainer> servletContainers = new ConcurrentHashMap<>();

    private Map<String, RestApplication> applications = new ConcurrentHashMap<>();


    @ServiceProperty(name = "com.awplab.rest.service.manager.holdOffTimeSeconds", value = "10")
    private int holdOffTimeSeconds;

    @Requires
    private HttpService httpService;


    public RestManagerProvider() {
        LoggerFactory.getLogger(RestManagerProvider.class).info("RestManagerProvider Starting Up");

    }

    private Set<String> dirtyAliases = Collections.synchronizedSet(new HashSet<>());

    private Timer dirtyTimer = null;
    private final Object dirtyTimerLock = new Object();

    private class DirtyAliasesTask extends TimerTask {
        @Override
        public void run() {
            synchronized (dirtyTimerLock) {
                //updateDirtyAliases();
                Logger logger = LoggerFactory.getLogger(RestManagerProvider.class);

                for (String alias : dirtyAliases) {
                    logger.info("Creating / Updating service containers and servlets for alias " + alias);
                    try {
                        ServletContainer container = servletContainers.get(alias);
                        if (container != null) {
                            httpService.unregister(alias);
                            servletContainers.remove(alias);
                            applications.remove(alias);
                            //  container.destroy();
                        }

                        Set<RestService> providers = collectRestProviders(alias);
                        if (providers.size() > 0) {
                            providers.addAll(collectRestProviders(RestService.GLOBAL_ALIAS));
                            RestApplication restApplication = new RestApplication(alias, providers);
                            applications.put(alias, restApplication);
                            container = new ServletContainer(ResourceConfig.forApplication(restApplication));
                            servletContainers.put(alias, container);
                            httpService.registerServlet(alias, container, null, null);
                        }
                    } catch (Exception ex) {
                        logger.error("Exception attempting to reload rest services for alias " + alias, ex);
                    }
                }

            }
        }
    }

    private Set<RestService> collectRestProviders(String alias) {
        HashSet<RestService> returnSet = new HashSet<>();
        for (RestService restProvider : restProviders) {
            if (restProvider.getAlias().equals(alias)) returnSet.add(restProvider);
        }
        return returnSet;
    }

    //private synchronized void updateDirtyAliases() {


//    }

    private void resetDirtyTimer() {
        if (dirtyTimer != null) {
            dirtyTimer.cancel();
        }
        dirtyTimer = new Timer();
        dirtyTimer.schedule(new DirtyAliasesTask(), holdOffTimeSeconds * 1000);
        LoggerFactory.getLogger(RestManagerProvider.class).info("Change detected, holding off refresh for " + holdOffTimeSeconds  + " seconds for aliases: " + Arrays.toString(dirtyAliases.toArray()));
    }


    private void updateContainerServlet(String alias)  {
        synchronized (dirtyTimerLock) {
            if (alias.equals(RestService.GLOBAL_ALIAS)) {
                for (String dirtyAlias : servletContainers.keySet()) {
                    dirtyAliases.add(dirtyAlias);
                }

            }
            else {
                dirtyAliases.add(alias);
            }
            resetDirtyTimer();
        }
    }


    private String logString(RestService provider) {
        return "RestService provider in alias " + provider.getAlias() + ", provider class: " + provider.getClass().getName();
    }


    @Bind(aggregate=true, optional=true)
    private synchronized void bindProvider(RestService provider) {
        Logger logger = LoggerFactory.getLogger(RestManagerProvider.class);

        String alias = provider.getAlias();

        try {

            if (alias.equals(RestService.GLOBAL_ALIAS) || (alias.startsWith("/") && !alias.endsWith("/"))) {
                restProviders.add(provider);

                updateContainerServlet(alias);

                logger.info("Successfully added " + logString(provider));
            }
            else {
                logger.error("Invalid alias, unable to add " + logString(provider));
            }

        } catch (Exception ex) {
            logger.error("Unhandled exception attempting to add " + logString(provider), ex);
        }

    }

    @Unbind
    private synchronized void unbindProvider(RestService provider) {
        Logger logger = LoggerFactory.getLogger(RestManagerProvider.class);

        try {
            String alias = provider.getAlias();

            restProviders.remove(alias);

            updateContainerServlet(alias);

            logger.info("Removed " + logString(provider));

        } catch (Exception ex) {
            logger.error("Unhandled exception attemmpting to remove " + logString(provider), ex);
        }

    }

    @Override
    public synchronized Set<String> getAliases() {
        return servletContainers.keySet();
    }


    @Override
    public synchronized Set<RestService> getProviders(String alias) {
        return collectRestProviders(alias);
    }

    @Override
    public synchronized Set<Class<?>> getApplicationClasses(String alias) {
        if (!applications.containsKey(alias)) throw new IllegalArgumentException("Invalid alias: " + alias);

        return new HashSet<>(applications.get(alias).getClasses());
    }

    @Override
    public synchronized Set<Object> getApplicationSingletons(String alias) {
        if (!applications.containsKey(alias)) throw new IllegalArgumentException("Invalid alias: " + alias);

        return new HashSet<>(applications.get(alias).getSingletons());
    }

    @Override
    public synchronized void reloadAlias(String alias) {
        updateContainerServlet(alias);

    }

}
