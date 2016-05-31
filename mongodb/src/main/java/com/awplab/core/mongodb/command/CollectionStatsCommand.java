package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.MongoService;
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
@Command(scope = "mongo", name="collection-stats")
@Service
public class CollectionStatsCommand implements Action {

    @Reference
    MongoService mongoService;

    @Argument(name = "database", description = "name of database", required = true)
    private String database;

    @Argument(name = "collection", description = "name of collection", required = true)
    private String collection;

    @Override
    public Object execute() throws Exception {

        MongoDatabase adminDatabase = mongoService.getMongoClient().getDatabase(database);
        //MongoCollection mongoCollection = adminDatabase.getCollection(collection);
        Document document = adminDatabase.runCommand(new Document("collStats", collection));
        System.out.println(document.toJson(new JsonWriterSettings(true)));

        return null;
    }

}
