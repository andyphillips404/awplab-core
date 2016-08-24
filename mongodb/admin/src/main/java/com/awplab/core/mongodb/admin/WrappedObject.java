package com.awplab.core.mongodb.admin;

/**
 * Created by andyphillips404 on 8/24/16.
 */
public class WrappedObject<T> {
    final private T wrappedObject;

    public WrappedObject(T wrappedObject) {
        this.wrappedObject = wrappedObject;

    }

    public T getWrappedObject() {
        return wrappedObject;
    }


}
