package com.awplab.core.rest.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andyphillips404 on 1/4/16.
 */
public class SimpleRestProvider implements RestService {

    private final String alias;
    private final Set<Class<?>> classes;
    private final Set<Object> singletons;

    public SimpleRestProvider(String alias, Set<Class<?>> classes, Set<Object> singletons) {
        this.alias = alias;
        this.classes = classes;
        this.singletons = singletons;
    }

    public SimpleRestProvider(String alias, Class<?>... classes) {
        this.alias = alias;
        this.classes = new HashSet<>(Arrays.asList(classes));
        this.singletons = Collections.emptySet();
    }

    public SimpleRestProvider(String alias, Object... singletons) {
        this.alias = alias;
        this.singletons = new HashSet<>(Arrays.asList(singletons));
        this.classes = Collections.emptySet();
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Set<Class<?>> getClasses(String alias) {
        return classes;
    }

    @Override
    public Set<Object> getSingletons(String alias) {
        return singletons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleRestProvider)) return false;

        SimpleRestProvider that = (SimpleRestProvider) o;

        if (alias != null ? !alias.equals(that.alias) : that.alias != null) return false;
        if (classes != null ? !classes.equals(that.classes) : that.classes != null) return false;
        return singletons != null ? singletons.equals(that.singletons) : that.singletons == null;

    }

    @Override
    public int hashCode() {
        int result = alias != null ? alias.hashCode() : 0;
        result = 31 * result + (classes != null ? classes.hashCode() : 0);
        result = 31 * result + (singletons != null ? singletons.hashCode() : 0);
        return result;
    }
}
