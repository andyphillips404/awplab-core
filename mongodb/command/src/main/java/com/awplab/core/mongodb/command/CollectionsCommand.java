package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.service.MongoService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="collections")
@Service
public class CollectionsCommand implements Action {

    @Reference
    MongoService mongoService;

    @Argument(name = "database", description = "name of database to list collections", required = true)
    private String database;

    @Override
    public Object execute() throws Exception {

       for (String collection : mongoService.getMongoClient().getDatabase(database).listCollectionNames()) {
           System.out.println(collection);
       }


        return null;
    }

}
