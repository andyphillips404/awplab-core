package com.awplab.core.mongodb.command;


import com.awplab.core.mongodb.MongoService;
import com.awplab.core.mongodb.PojoCodecKey;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by andyphillips404 on 2/25/15.
 */
@Command(scope = "mongo", name="test")
@Service
public class TestCommand implements Action {

    @Reference
    MongoService mongoService;

    @Argument(name = "desc")
    private String desc;

    @Override
    public Object execute() throws Exception {

        MongoClient mongoClient = mongoService.getMongoClient();

        MongoDatabase mongoDatabase = mongoClient.getDatabase("TEST");

        MongoCollection<TestObject> testObjectMongoCollection = mongoDatabase.getCollection("TEST_COLLECTION", TestObject.class);

        TestObject testObjectInsert = new TestObject(ObjectId.get(), desc);
        testObjectInsert.setTestDate(new Date());
        testObjectInsert.setTestInt(new Random().nextInt());

        testObjectMongoCollection.insertOne(testObjectInsert);

        for (TestObject testObject : testObjectMongoCollection.find()) {
            System.out.println(testObject.description);
        }

        return null;
    }

    public static class TestObject {

        @PojoCodecKey(value = "_id")
        private ObjectId id;

        private ObjectId testTwo;

        private String description;

        private Date testDate;

        private Integer testInt;

        private List<String> emptyList = new ArrayList<>();

        private String[] emptyArray;

        public List<String> getEmptyList() {
            return emptyList;
        }

        public void setEmptyList(List<String> emptyList) {
            this.emptyList = emptyList;
        }

        public String[] getEmptyArray() {
            return emptyArray;
        }

        public void setEmptyArray(String[] emptyArray) {
            this.emptyArray = emptyArray;
        }

        public TestObject() {
        }

        public TestObject(ObjectId testTwo, String description) {
            this.testTwo = testTwo;
            this.description = description;
        }

        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public ObjectId getTestTwo() {
            return testTwo;
        }

        public void setTestTwo(ObjectId testTwo) {
            this.testTwo = testTwo;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getTestDate() {
            return testDate;
        }

        public void setTestDate(Date testDate) {
            this.testDate = testDate;
        }

        public Integer getTestInt() {
            return testInt;
        }

        public void setTestInt(Integer testInt) {
            this.testInt = testInt;
        }
    }

}
