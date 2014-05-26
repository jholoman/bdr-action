package com.cloudera.manager.client;

import com.cloudera.manager.client.util.BDRConfig;
import com.cloudera.manager.client.util.CredentialsManager;
import org.apache.log4j.Logger;

/**
 * Created by jholoman on 5/23/14.
 */

class ScheduleLauncher {
    private static final Logger log = Logger.getLogger(ScheduleLauncher.class);


    private static void showOptions() {
        log.info("Usage: java -jar -D[option]=[value] bdr-action.jar <cmd> <args>");
        log.info("Options:");
        log.info("  mode" + "    (default '" + BDRConfig.getProperty("mode-Default") + "')  Run Mode options are local or distributed");
        log.info("  logLevel"  + "    (default '" + BDRConfig.getProperty("logLevel-Default") + "') The log level to use: debug, info, warn, error");
        log.info("Path must be specified when running in distributed mode.");
        log.info("Path defaults to $PWD when running in local mode");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Version 0.1");
        showOptions();


        if (args.length == 0) {
            System.out.println("---------");
            System.out.println("<cmd> args");
            System.out.println("Available Commands ");
            System.out.println("");
            System.out.println("Run scheduleId dryRun /path/to/output/file");
            System.out.println("GenerateCredentials /path/to/output_file");
            System.out.println("List /path/to/credentials_file");
            System.out.println("---------");
            return;
        }
        String cmd = args[0];
        String path = "";

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        String MODE = BDRConfig.getProperty("mode");
        try {
            if (cmd.equals("Run")) {
                boolean dryRun;
                Long scheduleId;
                //Check the path value
                if (subArgs.length < 3) {
                    path = "";
                } else {
                    path = subArgs[2];
                }
                if (subArgs.length ==0 ) {
                    System.out.println("Usage: Run scheduleId dryRun path");
                    System.out.println("Exiting...");
                    return;
                }
                if (MODE.equals("distributed") && path.equals("")) {
                    log.info("Usage: Run scheduleId dryRun /path/to/credentials_file");
                    log.error("Must provide a path");
                    log.info("Exiting...");
                    return;
                } else {
                    scheduleId = Long.parseLong(subArgs[0]);
                    dryRun = Boolean.valueOf(subArgs[1]);

                }
                BDRConfig.setProperty("path", path);
                ReplicationManager.runSchedule(scheduleId, dryRun);

            } else if (cmd.equals("GenerateCredentials")) {
                if (subArgs.length == 0) {
                    if (MODE.equals("distributed")) {
                        log.info("Usage: GenerateCredentials /path/to/credentials_file");
                        log.error("Must provide a path");
                        log.info("Exiting...");
                        return;
                    }
                } else if (subArgs.length == 1) {
                    path = subArgs[0];
                }
                BDRConfig.setProperty("path", path);

                CredentialsManager.writeCredentials();

            } else if (cmd.equals("List")) {
                if (subArgs.length == 0) {
                    if (MODE.equals("distributed")) {
                        System.out.println("Usage: List /path/to/credentials_file");
                        System.out.println("Exiting...");
                        System.exit(1);
                    }
                } else if (subArgs.length == 1) {
                    path = subArgs[0];
                }
                BDRConfig.setProperty("path", path);

                ReplicationManager.ListSchedules();

            } else {
                System.out.println("Unknown cmd:" + args[0]);
            }


        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
