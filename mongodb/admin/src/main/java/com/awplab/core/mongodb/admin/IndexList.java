package com.awplab.core.mongodb.admin;

import java.util.AbstractList;

/**
 * Created by andyphillips404 on 8/17/16.
 */
public class IndexList extends AbstractList<Integer> {
    private int size = 0;

    private int offset = 0;

    public IndexList(int size) {
        this.size = size;
    }

    public IndexList(int size, int offset) {
        this.size = size;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public Integer get(int index) {
        if (index >= size) throw new IndexOutOfBoundsException();
        return index + offset;
    }

    @Override
    public int size() {
        return size;
    }
}
