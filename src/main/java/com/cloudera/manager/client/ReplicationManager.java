package com.cloudera.manager.client;

import java.util.Properties;
import java.util.List;


import com.cloudera.api.model.*;
import com.cloudera.api.v4.*;
import org.apache.log4j.Logger;

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.DataView;
import com.cloudera.manager.client.util.CredentialsManager;
import com.cloudera.manager.client.util.AESencryption;

/**
 * Created by jholoman on 5/23/14.
 */
class ReplicationManager {

    private static final Logger log = Logger.getLogger(ReplicationManager.class);
    private static final String[] VALID_SERVICES = {"HDFS", "HIVE", "HIVE-1"};


    private static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e))
                return true;

        return false;
    }

    private static ServicesResourceV4 getServicesResource() throws Exception {

        Properties config = CredentialsManager.getCredentials();
        String cm_host = config.getProperty("cm_host", "localhost");
        String cm_user = config.getProperty("cm_user", "admin");
        int cm_port = Integer.parseInt(config.getProperty("cm_port", "7180"));
        String cm_password = config.getProperty("cm_password");


        if (cm_password.length() < 1) {
            log.error("Password not found");
            System.exit(1);
        }

        AESencryption decrypter = new AESencryption();

        RootResourceV4 apiRoot = new ClouderaManagerClientBuilder()
                .withHost(cm_host)
                .withUsernamePassword(cm_user, decrypter.decrypt(cm_password))
                .withPort(cm_port).build().getRootV4();
        log.info("Connected to Cluster" + cm_host + " with user " + cm_user);


        ClustersResourceV4 clustersResource = apiRoot.getClustersResource();
        log.debug("Got Cluster Resource");

        ApiCluster cluster1 = clustersResource.readClusters(DataView.SUMMARY).getClusters().get(0);
        log.debug("Got Api Cluster");

        @SuppressWarnings("UnnecessaryLocalVariable") ServicesResourceV4 servicesResource = clustersResource.getServicesResource(cluster1.getName());
        return servicesResource;
    }

    public static void runSchedule(long scheduleId, boolean dryRun) throws Exception {
        ServicesResourceV4 servicesResource = getServicesResource();
        List<ApiService> apiServiceList = servicesResource.readServices(DataView.EXPORT).getServices();


        try {
            for (ApiService apiService : apiServiceList) {
                if (contains(VALID_SERVICES, apiService.getType())) {
                    log.info("Checking " + apiService.getType() + " for replication schedules");
                    ReplicationsResourceV4 reps = servicesResource.getReplicationsResource(apiService.getName());
                    try {
                        ApiReplicationSchedule schedule = reps.readSchedule(scheduleId, DataView.FULL);

                        log.info("Found replication schedule");
                        log.info("Executing replication schedule " + scheduleId + " with dryRun flag set to " + String.valueOf(dryRun));
                        log.info(schedule.getHdfsArguments().getSourcePath());
                        log.info(schedule.getHdfsArguments().getDestinationPath());

                        ApiCommand command = reps.runSchedule(schedule.getId(), true);

                        log.info("Schedule started at " + command.getStartTime());
                        log.info("Exiting");
                        System.exit(0);
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Caught exception generating report: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            log.info("Did not find schedule: " + scheduleId);
            System.exit(1);
        }
    }

    public static void ListSchedules() throws Exception {
        ServicesResourceV4 servicesResource = getServicesResource();
        List<ApiService> apiServiceList = servicesResource.readServices(DataView.EXPORT).getServices();

        for (ApiService apiService : apiServiceList) {
            if (contains(VALID_SERVICES, apiService.getType())) {
                log.info("Checking " + apiService.getType() + " for replication schedules");
                ReplicationsResourceV4 reps = servicesResource.getReplicationsResource(apiService.getName());
                try {

                    for (ApiReplicationSchedule schedule : reps.readSchedules(DataView.FULL)) {
                        log.info("----------------------------------------------------------------------");
                        log.info("Schedule ID:                 " + schedule.getId());
                        log.info("Source Path:                 " + schedule.getHdfsArguments().getSourcePath());
                        log.info("Dest   Path:                 " + schedule.getHdfsArguments().getDestinationPath());
                        log.info("MR  Service:                 " + schedule.getHdfsArguments().getMapreduceServiceName());
                        log.info("Pool       :                 " + schedule.getHdfsArguments().getSchedulerPoolName());
                        log.info("# Mappers  :                 " + schedule.getHdfsArguments().getNumMaps());
                        log.info("Bandwidth per Map:           " + schedule.getHdfsArguments().getBandwidthPerMap());
                        log.info("User Name  :                 " + schedule.getHdfsArguments().getUserName());

                        for (ApiReplicationCommand replicationCommand : schedule.getHistory()) {
                            log.info("\t - - - - - - - - - - - - - - ");
                            log.info("\tStart Time:                  " + replicationCommand.getStartTime());
                            log.info("\tEnd Time  :                  " + replicationCommand.getEndTime());
                            log.info("\tActive    :                  " + replicationCommand.isActive());
                            log.info("\tSuccess   :                  " + replicationCommand.getSuccess().toString());
                            log.info("\tPeer      :                  " + replicationCommand.getServiceRef().getPeerName());
                        }
                    }
                } catch (Exception e) {
                    log.debug(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


}
