package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.service.MongoService;
import com.mongodb.client.MongoDatabase;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="database-stats")
@Service
public class DatabaseStatsCommand implements Action {

    @Reference
    MongoService mongoService;

    @Argument(name = "database", description = "name of database", required = true)
    private String database;

    @Override
    public Object execute() throws Exception {

        MongoDatabase adminDatabase = mongoService.getMongoClient().getDatabase(database);
        Document document = adminDatabase.runCommand(new Document("dbStats", 1));
        System.out.println(document.toJson(new JsonWriterSettings(true)));

        return null;
    }

}
