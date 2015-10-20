package org.laukvik.sql.cmd;

import org.laukvik.sql.DatabaseConnection;
import org.laukvik.sql.DatabaseConnectionInvalidException;
import org.laukvik.sql.DatabaseConnectionNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 *
 */
public class CommandManager {

    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());
    private List<Command> commands;
    private String filename;
    private ResourceBundle bundle;


    public CommandManager(String filename) {
        this.filename = filename;
        //bundle = ResourceBundle.getBundle("messages");
        commands = new ArrayList<>();
    }

    public void add(Command command){
        commands.add(command);
    }

    /**
     * Runs the specified named command
     *
     * @param args
     */
    public int run( String... args ){
        //
        String namedConnection = args.length > 0 ? args[0] : null;
        if (namedConnection == null || namedConnection.trim().isEmpty()){
            usage();
            return Command.EXCEPTION;
        }
        //
        String action = args.length > 1 ? args[1].substring(1) : null;
        //
        String parameter = args.length > 2 ? args[2] : null;

        LOG.fine("Connection=" + namedConnection + " action=" + action + " parameter=" + parameter);

        Command cmd = getCommandByName(action);
        if (cmd == null){
            LOG.fine("Could not find command with name " + action);
        } else {

            try {
                DatabaseConnection db = DatabaseConnection.read(namedConnection);
                LOG.fine("Found named command " + action + ". Option=" + parameter);
                return cmd.run(db,parameter);
            } catch (DatabaseConnectionNotFoundException e) {
                LOG.fine("Failed to connect to " + namedConnection);
            } catch (DatabaseConnectionInvalidException e) {
                LOG.fine("Failed to read named conneciton " + namedConnection + "!");
            }

        }

        return Command.SUCCESS;
    }

    /**
     * Returns a named command
     *
     * @param name
     * @return
     */
    public Command getCommandByName(String name){
        for (Command c : commands){
            if (c.getAction().equalsIgnoreCase(  name )){
                return c;
            }
        }
        return null;
    }

    /**
     * Display application parameters
     *
     */
    public void usage(){
        System.out.println("Usage: " + filename + " <connection> <option>" );
        for (Command c : commands){
            if (c.getParameter() == null){
                System.out.printf("\t-%-20s\t%s\n", c.getAction(), c.getDescription());
            } else {
                System.out.printf("\t-%-20s\t%s\n", c.getAction() + "=" + c.getParameter(), c.getDescription());
            }
        }
    }
}
