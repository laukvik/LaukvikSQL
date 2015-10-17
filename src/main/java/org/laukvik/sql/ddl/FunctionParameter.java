package org.laukvik.sql.ddl;

/**
 * Created by morten on 10.10.2015.
 */
public class FunctionParameter {

    private String name;
    private String comments;

    public FunctionParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
