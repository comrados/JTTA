/*
 * Title: MongoInterface.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.db.mongo;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.topicextractor.structures.TEDialog;
import com.crawlergram.topicextractor.structures.message.TEMessage;

import java.io.*;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.crawlergram.db.Constants.*;

/**
 * Class for writing and reading data to and from MongoDB.
 */

public class MongoDBStorageReduced implements DBStorageReduced {

    private String user; // the user name
    private String db; // the name of the db in which the user is defined
    private String psw; // the psw
    private String host; // host
    private Integer port; // port
    private MongoCredential credential; // auth info
    private MongoClientOptions options; // client options
    private MongoClient mongoClient; // client instance
    private MongoDatabase database; // db instance
    private GridFSBucket gridFSBucket; // bucket for files
    private MongoCollection<Document> collection; //collection
    private boolean upsert; // upsert into DB? if false - regular write

    public MongoDBStorageReduced(String user, String db, String psw, String host, Integer port, String gridFSBucketName){
        this.user = user;
        this.db = db;
        this.psw = psw;
        this.host = host;
        this.port = port;
        this.credential = MongoCredential.createCredential(user, db, psw.toCharArray());
        this.options = MongoClientOptions.builder().build();
        this.mongoClient = new MongoClient(new ServerAddress(host, port), credential, options);
        this.database = mongoClient.getDatabase(db);
        this.gridFSBucket = GridFSBuckets.create(this.database, gridFSBucketName);
        this.upsert = false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public MongoCredential getCredential() {
        return credential;
    }

    public void setCredential(MongoCredential credential) {
        this.credential = credential;
    }

    public MongoClientOptions getOptions() {
        return options;
    }

    public void setOptions(MongoClientOptions options) {
        this.options = options;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    public void setGridFSBucket(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public boolean isUpsert() {
        return upsert;
    }

    public void setUpsert(boolean upsert) {
        this.upsert = upsert;
    }

    public void setGridFSBucket(String gridFSBucketName) {
        gridFSBucket = GridFSBuckets.create(database, gridFSBucketName);
    }

    public void setCollection(String collName) {
        collection = database.getCollection(collName);
    }

    /**
     * sets target database
     * @param database target database
     */
    @Override
    public void setDatabase(String database) {
        try {
            this.database = mongoClient.getDatabase(database);
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * place to read/write
     * @param target target collection
     */
    @Override
    public void setTarget(String target) {
        try {
            collection = database.getCollection(target);
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * Drops target collection
     * @param target target collection
     */
    @Override
    public void dropTarget(String target) {
        try{
            database.getCollection(target).drop();
        } catch (MongoException e){
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * Drops current db
     */
    @Override
    public void dropDatabase() {
        try{
            database.drop();
        } catch (MongoException e){
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * writes object to db
     * @param obj object
     */
    @Override
    public void write(Object obj) {
        if (obj != null) {
            if (!isUpsert()) {
                try {
                    collection.insertOne((Document) obj);
                } catch (MongoException e) {
                    System.err.println(e.getCode() + " " + e.getMessage());
                }
            } else {
                try {
                    Document doc = (Document) obj;
                    collection.updateOne(Filters.eq("_id", doc.get("_id")), new Document("$set", doc), new UpdateOptions().upsert(true));
                } catch (MongoException e) {
                    System.err.println(e.getCode() + " " + e.getMessage());
                }

            }
        }
    }

    /**
     * creates single field index
     * @param field indexing field
     * @param type switch: 1 - ascending, -1 - descending, default - ascending
     */
    @Override
    public void createIndex(String field, int type) {
        try {
            switch (type) {
                case 1:
                    collection.createIndex(Indexes.ascending(field));
                    break;
                case -1:
                    collection.createIndex(Indexes.descending(field));
                    break;
                default:
                    collection.createIndex(Indexes.ascending(field));
                    break;
            }
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * creates composite index
     * @param fields indexing fields
     * @param types switch: 1 - ascending, -1 - descending, default - ascending
     */
    @Override
    public void createIndex(List<String> fields, List<Integer> types) {
        try {
            // asc and desc indexes
            List<String> asc = new ArrayList<>();
            List<String> desc = new ArrayList<>();
            //check sizes
            if (fields.size() == types.size()){
                // separate desc and asc
                for (int i = 0; i < types.size(); i++){
                    switch (types.get(i)) {
                        case 1:
                            asc.add(fields.get(i)); break;
                        case -1:
                            desc.add(fields.get(i)); break;
                        default:
                            asc.add(fields.get(i)); break;
                    }
                }
                // if only desc is not empty
                if (asc.isEmpty() && (!desc.isEmpty())){
                    collection.createIndex(Indexes.descending(desc));
                }
                // if only asc is not empty
                if (desc.isEmpty() && (!asc.isEmpty())){
                    collection.createIndex(Indexes.ascending(asc));
                }
                // if asc & desc is not empty
                if ((!asc.isEmpty()) && (!desc.isEmpty())) {
                    collection.createIndex(Indexes.compoundIndex(Indexes.ascending(asc), Indexes.descending(desc)));
                }
            } else {
                System.out.println("UNABLE TO CREATE INDEXES: fields and types have different lengths");
            }
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * reads all messages from DB for target collection
     * @param target target collection
     */
    @Override
    public List<TEMessage> readMessages(TEDialog target) {
        try {
            List<TEMessage> msgs = new LinkedList<>();
            this.setTarget(MSG_DIAL_PREF + target.getId());
            FindIterable<Document> docs = collection.find().sort(descending("_id"));
            for (Document doc : docs) {
                msgs.add(TEMessage.topicExtractionMessageFromMongoDocument(doc));
            }
            return msgs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * reads messages between two dates from DB for target collection
     * @param target targetCollectionName collection
     * @param dateFrom start date date
     * @param dateTo end date
     */
    @Override
    public List<TEMessage> readMessages(TEDialog target, int dateFrom, int dateTo) {
        try {
            List<TEMessage> msgs = new LinkedList<>();
            this.setTarget(MSG_DIAL_PREF + target.getId());
            FindIterable<Document> docs = collection
                    .find(and(gte("date", dateFrom), lte("date", dateTo)))
                    .sort(descending("_id"));
            for (Document doc : docs) {
                msgs.add(TEMessage.topicExtractionMessageFromMongoDocument(doc));
            }
            return msgs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns dialogs list from respective collection
     */
    @Override
    public List<TEDialog> getDialogs() {
        try {
            List<TEDialog> dialogs = new ArrayList<>();
            this.setTarget("DIALOGS");
            FindIterable<Document> dials = collection.find();
            for (Document dial : dials) {
                Document info = getPeerInfo((Integer) dial.get("_id"));
                if (info != null){
                    dialogs.add(TEDialog.topicExtractionDialogFromMongoDocument(info));
                }
            }
            return dialogs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * saves files from DB to HDD
     * @param path HDD path
     */
    @Override
    public void saveFilesToHDD(String path) {
        List<GridFSFile> files = getDBFilesInfo();
        for (GridFSFile file : files) {
            saveFileToHDD(path, file);
        }
    }

    /**
     * saves files from DB to HDD
     * @param path path
     * @param filePointer file id or another pointer
     */
    @Override
    public void saveFileToHDD(String path, Object filePointer) {
        try {
            GridFSFile file = (GridFSFile) filePointer;
            ObjectId oid = file.getObjectId();
            path += File.separator + file.getFilename();
            checkFilePath(path);
            FileOutputStream fos = new FileOutputStream(path);
            gridFSBucket.downloadToStream(oid, fos);
            fos.close();
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            System.out.println("MONGODB ERROR " + ((GridFSFile) filePointer).getFilename());
        } catch (IOException e){
            System.err.println(e.getMessage());
            System.out.println("OUTPUT STREAM ERROR " + ((GridFSFile) filePointer).getFilename());
        }
    }

    /**
     * gets peer info from database
     * @param id id
     */
    public Document getPeerInfo(Integer id) {
        try {
            this.setTarget("CHATS");
            Document peerInfo = collection.find(eq("_id", id)).first();
            if (peerInfo == null) {
                this.setTarget("USERS");
                peerInfo = collection.find(eq("_id", id)).first();
            }
            return peerInfo;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing collections names
     */
    public List<String> getAllCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                colNames.add(collection);
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing message collections names
     */
    public List<String> getMessagesCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                if (collection.startsWith("MESSAGES")) {
                    colNames.add(collection);
                }
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing participant collections names
     */
    public List<String> getParticipantsCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                if (collection.startsWith("PARTICIPANTS")) {
                    colNames.add(collection);
                }
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns names of stored files
     */
    public List<GridFSFile> getDBFilesInfo() {
        try {
            List<GridFSFile> files = new LinkedList<>();
            GridFSFindIterable gfsi = gridFSBucket.find();
            for (GridFSFile gfsf : gfsi) {
                files.add(gfsf);
            }
            return files;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the file and path to file exist. If not - creates them.
     * @param filePath file path
     */
    private static void checkFilePath(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            file.createNewFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
