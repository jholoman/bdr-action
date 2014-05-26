package com.cloudera.manager.client.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;



/**
 * Created by jholoman on 5/23/14.
 */
public class CredentialsManager {

    private static final Logger log = Logger.getLogger(CredentialsManager.class);
    private static final String FILENAME = BDRConfig.getProperty("filename");
    private static final String MODE = BDRConfig.getProperty("mode");
    //private static String path = BDRConfig.getProperty("path");

    public static Properties getCredentials() throws IOException {
        String path = BDRConfig.getProperty("path");
        Properties config = new Properties();

        log.debug("Getting Credentials in " + MODE + "MODE");

        if (path.length() > 0 && (!path.substring(path.length() - 1).equals(Character.toString(File.separatorChar)))) {
            path += File.separatorChar + FILENAME;
        } else { path += FILENAME; }


        if (MODE.equals("local")) {
            config = getLocal(path);
        } else if (MODE.equals("distributed")) {
            config = getHDFS(path);
        }
        return config;
    }

    private static Properties getLocal(String path) {
        System.out.println(path);
       Properties config = new Properties();
        try {
            InputStream stream = new FileInputStream(path);
            try {
                config.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            log.info("Couldn't read the properties file.\n Ensure that " + path +" exists");
            ex.printStackTrace();
        }
        return config;
    }

    private static Properties getHDFS(String path) throws IOException {
        Properties config = new Properties();
        Configuration conf = new Configuration();

        FileSystem hdfs = FileSystem.get(conf);
        Path hdfs_path = new Path(path);
        try {
       FSDataInputStream stream = new FSDataInputStream(hdfs.open(hdfs_path));
            try {
                config.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            log.info("Couldn't read the properties file.\n Ensure that "+ path+" exists");
            ex.printStackTrace();
        }
        return config;
    }


    public static void writeCredentials() throws Exception {
        String path = BDRConfig.getProperty("path");

        Properties prop = new Properties();
        Console console = System.console();

        if (console == null) {
            log.error("Couldn't get Console instance");
            System.exit(0);
        }
        String clusterHost = console.readLine("Enter the hostname for the CM Machine:");
        String clusterPort = console.readLine("Enter the port of the CM machine:");
        String username = console.readLine("Enter the admin user:");

        prop.setProperty("cm_host", clusterHost);
        prop.setProperty("cm_port", clusterPort);
        prop.setProperty("cm_user", username);

        boolean noMatch;
        do {
            char passwordArray[] = console.readPassword("Enter the password for " + username + " on " + clusterHost + ":");
            char passwordArray2[] = console.readPassword("Confirm the password for " + username + " on " + clusterHost + ": ");
            noMatch = !Arrays.equals(passwordArray, passwordArray2);

            if (noMatch) {
                console.format("Passwords don't match. Try again.%n");
            } else {
                AESencryption encrypter = new AESencryption();
                String pw = new String(passwordArray);
                prop.setProperty("cm_password", encrypter.encrypt(pw));
            }
            Arrays.fill(passwordArray, ' ');
            Arrays.fill(passwordArray2, ' ');
        } while (noMatch);

        if (path.length() > 0 && (!path.substring(path.length() - 1).equals(Character.toString(File.separatorChar)))) {
            path += File.separatorChar + FILENAME;
        } else { path += FILENAME; }

        if (MODE.equals("local")) {
            writeLocal(prop, path);
        } else if (MODE.equals("distributed")) {
           /*TODO Implement writing to HDFS */
            writeLocal(prop, FILENAME);
        }
    }

    private static void writeLocal(Properties prop, String path) {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
            prop.store(stream, null);
        } catch (IOException e) {
            log.error("Caught exception generating report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void writeHDFS(Properties prop, String path) {

    }
}




