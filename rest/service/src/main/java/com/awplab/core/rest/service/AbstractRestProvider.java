package com.awplab.core.rest.service;

import org.apache.felix.ipojo.annotations.Property;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Created by andyphillips404 on 12/19/15.
 */
public abstract class AbstractRestProvider implements RestService {


    protected void setAlias(String alias) {
        if (alias == null) throw new RuntimeException("Alias must not be null");
        if (this.alias.equals(alias)) return;

        String oldAlias = this.alias;
        this.alias = alias;

        RestManagerService restManagerService = RestManagerService.getProvider();
        if (restManagerService != null) {
            restManagerService.reloadAlias(oldAlias);
            restManagerService.reloadAlias(this.alias);
        }
    }

    private String alias = RestManagerService.DEFAULT_ALIAS;

    @Override
    public String getAlias() {
        return alias;
    }
}
