/*
 * Title: csvSaver.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram._old;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.structures.TLoader;
import com.crawlergram.structures.TMessage;
import com.crawlergram.structures.dialog.TDialog;

import java.io.*;
import java.util.List;

public class csvSaver {

    public static void main(String[] args) {

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorageReduced dbStorage = new MongoDBStorageReduced("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        // loads dialogs
        TLoader tLoader = new TLoader.TLoaderBuilder(dbStorage).setDateFrom(0).setDateTo(0).build();

        while (tLoader.hasNext()){
            TDialog current = tLoader.next();
            current.loadMessages(dbStorage);

            System.out.println(current.getId() + " " + current.getUsername());

            writeToCSV(current.getId().toString(), "D:\\outs", ";", current.getMessages());

            System.out.println();
        }

        System.exit(0);

    }

    private static void writeToCSV(String fullName, String path, String sep, List<TMessage> msgs){
        String filePath = setFileNameAndPath(fullName + ".csv", path);
        try {
            FileOutputStream fos = new FileOutputStream(new File(filePath));

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            //header
            bw.write("id" + sep + "date" + sep + "fromUserId" + sep + "replyToMsgId" + sep + "text");
            bw.newLine();

            // content
            for (TMessage msg: msgs){
                String line = getMessageContent(msg, sep);
                if (line != null){
                    bw.write(line);
                    bw.newLine();
                    bw.flush();
                }
            }

            bw.close();

        } catch (IOException e){
            System.err.println("Unable to write " + filePath);
            System.err.println(e.getMessage());
        } finally {

        }
    }

    private static String setFileNameAndPath(String name, String path){
        String filePath = path + File.separator + name;
        checkFilePath(filePath);
        return filePath;
    }

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

    private static String getMessageContent(TMessage m, String sep){
        String txt = m.getText().replaceAll(sep, " ");
        txt = txt.replaceAll("\\s+", " ");
        return m.getId() + sep + m.getDate() + sep + txt;
    }

}
