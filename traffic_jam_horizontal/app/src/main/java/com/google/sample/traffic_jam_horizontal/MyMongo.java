package com.google.sample.traffic_jam_horizontal;

import android.util.Log;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.List;

/**
 * Created by Administrator on 2017/5/6.
 */

public class MyMongo
{

    private static final String TAG = "Mongo";
    private static final String dataBaseName = "Vision";
    private static MongoClient mongoClient = null;
    private static MongoDatabase mongoDatabase = null;

    static
    {
        try
        {
            if(mongoClient == null || mongoDatabase == null)
            {
                mongoClient = new MongoClient(new MongoClientURI("mongodb://bigmms:bigmms1413b@140.138.145.77:27017"));
                //mongoClient = new MongoClient(new MongoClientURI("mongodb://140.138.152.166:27017"));
                mongoDatabase = mongoClient.getDatabase(dataBaseName);
                /*List<ServerAddress> serverAddress = new ArrayList<>();
                                serverAddress.add(new ServerAddress("140.138.145.77", 27017));
                                List<MongoCredential> credential = new ArrayList<>();
                                credential.add(MongoCredential.createCredential("bigmms", "test", "bigmms1413b".toCharArray()));
                                mongoClient = new MongoClient(serverAddress, credential);
                                mongoDatabase = mongoClient.getDatabase(dataBaseName);*/
            }
        } catch (Exception e)
        {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
        }
    }
    public MongoClient getMongo()
    {
        return mongoClient;
    }

    public void insert(String collectionName, List<String> key, List<String> value)
    {
        try
        {
                Document document = new Document();
                for (int i = 0; i < key.size(); i++)
                {
                    document.put(key.get(i), value.get(i));

                }
            mongoDatabase.getCollection(collectionName).insertOne(document);
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
