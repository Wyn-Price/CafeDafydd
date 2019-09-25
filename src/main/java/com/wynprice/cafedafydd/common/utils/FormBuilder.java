package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;

import java.util.ArrayList;
import java.util.List;

public class FormBuilder {

    private FormBuilder() { }

    public static FormBuilder create() {
        return new FormBuilder();
    }

    private final List<String> form = new ArrayList<>();
    private int prevLen = 0;

    public FormBuilder with(String field, String value) {
        this.form.add(field);
        this.form.add(value);
        this.prevLen = 2;
        return this;
    }

    public FormBuilder without(String field, String value) {
        this.form.add(DatabaseStrings.NOT_PREFIX);
        with(field, value);
        this.prevLen = 3;
        return this;
    }

    public FormBuilder when(boolean value) {
        if(!value) {
            for (int i = 0; i < this.prevLen; i++) {
                this.form.remove(this.form.size() - 1);
            }
        }
        return this;
    }

    public boolean isEmpty() {
        return this.form.isEmpty();
    }

    public String[] getForm() {
        return this.form.toArray(new String[0]);
    }
}
