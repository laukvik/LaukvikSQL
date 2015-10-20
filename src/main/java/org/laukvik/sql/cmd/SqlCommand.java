package org.laukvik.sql.cmd;

/**
 * Created by morten on 17.10.2015.
 */
public abstract class SqlCommand implements Command{

    private String action;
    private String description;
    private String parameter;

    public SqlCommand(String action, String parameter, String description) {
        this.action = action;
        this.parameter = parameter;
        this.description = description;
    }

    public SqlCommand(String action, String description) {
        this.action = action;
        this.parameter = null;
        this.description = description;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getParameter() {
        return parameter;
    }
}
