package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;

import java.util.ArrayList;
import java.util.List;

public enum FormBuilder {
    INSTANCE;

    private final List<String> form = new ArrayList<>();
    private int prevLen = 0;

    public FormBuilder with(String field, String value) {
        INSTANCE.form.add(field);
        INSTANCE.form.add(value);
        this.prevLen = 2;
        return INSTANCE;
    }

    public FormBuilder without(String field, String value) {
        INSTANCE.form.add(DatabaseStrings.NOT_PREFIX);
        with(field, value);
        this.prevLen = 3;
        return INSTANCE;
    }

    public FormBuilder when(boolean value) {
        if(!value) {
            for (int i = 0; i < this.prevLen; i++) {
                this.form.remove(this.form.size() - 1);
            }
        }
        return INSTANCE;
    }

    public boolean isEmpty() {
        return this.form.isEmpty();
    }

    public void reset() {
        this.form.clear();
    }

    public String[] getForm() {
        String[] array = this.form.toArray(new String[0]);
        this.form.clear();
        return array;
    }
}
