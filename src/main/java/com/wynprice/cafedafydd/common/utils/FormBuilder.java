package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Form builders are helper classes used to build the forms that are used for database requests.
 */
public class FormBuilder {

    private FormBuilder() { }

    /**
     * Creates a new form builder
     * @return the new form builder.
     */
    public static FormBuilder create() {
        return new FormBuilder();
    }

    /**
     * The form list. When {@link #getForm()} is called, this is compiled into an array.
     */
    private final List<String> form = new ArrayList<>();

    /**
     * The previous amount of entries added {@link #form}. Used in {@link #when(boolean)}
     */
    private int prevLen = 0;

    /**
     * Adds the field and value to the form
     * @param field the field to put in the form
     * @param value the {@code field}'s value to put in the form
     * @return itself
     */
    public FormBuilder with(String field, String value) {
        this.form.add(field);
        this.form.add(value);
        this.prevLen = 2;
        return this;
    }

    /**
     * Adds an inline request to the form. <br>
     * Inline requests are used to query another database for the value of a field.
     * @param field the field to put in the form
     * @param requestDatabase the database to request in.
     * @param requestDatabaseField the field to request in the other database.
     * @param form the form to use to request in the other database.
     * @return itself
     */
    public FormBuilder withInline(String field, String requestDatabase, String requestDatabaseField, String... form) {
        this.form.add(field);
        this.form.add(DatabaseStrings.INLINE_REQUEST_PREFIX);
        this.form.add(requestDatabase);
        this.form.add(requestDatabaseField);
        this.form.add(String.valueOf(form.length));
        Collections.addAll(this.form, form);
        this.prevLen = 5 + form.length;
        return this;
    }

    /**
     * Has this form blacklist the field with this value. All searched entries MUST NOT have this value at this field
     * @param field the field to search for
     * @param value the value to blacklist
     * @return itself
     */
    public FormBuilder without(String field, String value) {
        this.form.add(DatabaseStrings.NOT_PREFIX);
        with(field, value);
        this.prevLen = 3;
        return this;
    }

    /**
     * Add something to the form only when a certain value is reached.
     * <pre>{@code
     *
     * formBuilder
     *  .with("field1", "value1")
     *  .with("field2", "value2").when(true)
     *  .getForm()
     * ===> ["field1", "value1", "field2", "value2"]
     *
     * formBuilder
     *  .with("field1", "value1")
     *  .with("field2", "value2").when(false)
     *  .getForm()
     * ===> ["field1", "value1"]
     * }</pre>
     * @param value the value to, if false, remove the last form entry.
     * @return itself
     */
    public FormBuilder when(boolean value) {
        if(!value) {
            for (int i = 0; i < this.prevLen; i++) {
                this.form.remove(this.form.size() - 1);
            }
        }
        return this;
    }

    /**
     * Whether this form is empty Returns true when {@link #form} is empty.
     * @return true if this form is empty.
     */
    public boolean isEmpty() {
        return this.form.isEmpty();
    }

    /**
     * Gets the form in a string array. Returns {@link #form} as {@link List#toArray(Object[])}
     * @return a string array representing this form.
     */
    public String[] getForm() {
        return this.form.toArray(new String[0]);
    }
}
