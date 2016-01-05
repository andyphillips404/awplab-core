package com.awplab.core.rest.service;

import org.apache.felix.ipojo.annotations.Property;

/**
 * Created by andyphillips404 on 12/19/15.
 */
public abstract class AbstractRestProvider implements RestService {

    abstract RestManagerService getRestManagerService();

    protected void setAlias(String alias) {
        this.alias = alias;
        // reload all aliases to reflect the change
        getRestManagerService().reloadAliases();
    }

    private String alias = DEFAULT_ALIAS;

    @Override
    public String getAlias() {
        return alias;
    }
}
