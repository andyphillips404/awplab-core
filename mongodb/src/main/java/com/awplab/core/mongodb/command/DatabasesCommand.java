package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.MongoService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="databases")
@Service
public class DatabasesCommand implements Action {

    @Reference
    MongoService mongoService;


    @Override
    public Object execute() throws Exception {

       for (String database : mongoService.getMongoClient().listDatabaseNames()) {
           System.out.println(database);
       }

        return null;
    }

}
