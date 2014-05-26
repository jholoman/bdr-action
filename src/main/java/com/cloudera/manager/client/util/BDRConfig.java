package com.cloudera.manager.client.util;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Properties;
/**
 * Created by jholoman on 5/25/14.
 */
public class BDRConfig {
    private static final Properties props = new Properties();
    private static final Logger log = Logger.getLogger(BDRConfig.class);
    private static final String[] VALID_MODES = {"local", "distributed"};

    static {
        String LOG_LEVEL = System.getProperty("logLevel", "info");
        String RUN_MODE = System.getProperty("mode", "distributed");
        if (!contains(VALID_MODES, RUN_MODE)) {
            log.warn("Mode selected is invalid; Setting to local mode.");
            RUN_MODE = "local";
        }
        props.setProperty("logLevel", LOG_LEVEL);
        props.setProperty("logLevel-Default", "info");
        props.setProperty("mode-Default", "distributed");
        props.setProperty("mode", RUN_MODE);
        props.setProperty("filename",System.getProperty("filename","bdr.properties"));

        log.getRootLogger().setLevel(Level.toLevel(LOG_LEVEL));

    }
    private static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e))
                return true;

        return false;
    }
    public static String getProperty(String key) {
        return props.getProperty(key);
    }
    public static void setProperty(String key, String value) {
        props.setProperty(key,value);
    }
}