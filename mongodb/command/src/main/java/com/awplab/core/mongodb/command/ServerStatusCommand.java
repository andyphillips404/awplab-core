package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.service.MongoService;
import com.mongodb.client.MongoDatabase;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="server-status")
@Service
public class ServerStatusCommand implements Action {

    @Reference
    MongoService mongoService;

    @Override
    public Object execute() throws Exception {

        MongoDatabase adminDatabase = mongoService.getMongoClient().getDatabase("admin");
        Document document = adminDatabase.runCommand(new Document("serverStatus", 1));
        System.out.println(document.toJson(new JsonWriterSettings(true)));

        return null;
    }

}
