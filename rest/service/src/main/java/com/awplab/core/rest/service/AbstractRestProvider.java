package com.awplab.core.rest.service;

/**
 * Created by andyphillips404 on 12/19/15.
 */
public abstract class AbstractRestProvider implements RestService {


    protected void setAlias(String alias) {
        if (alias == null) throw new RuntimeException("Alias must not be null");
        if (this.alias.equals(alias)) return;

        String oldAlias = this.alias;
        this.alias = alias;

        RestManager restManager = RestManager.getProvider();
        if (restManager != null) {
            restManager.reloadAlias(oldAlias);
            restManager.reloadAlias(this.alias);
        }
    }

    private String alias = RestService.DEFAULT_ALIAS;

    @Override
    public String getAlias() {
        return alias;
    }
}
