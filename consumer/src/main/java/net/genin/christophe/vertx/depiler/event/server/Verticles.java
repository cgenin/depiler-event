package net.genin.christophe.vertx.depiler.event.server;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.rxjava.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import net.genin.christophe.vertx.depiler.event.server.database.DbInitializer;
import net.genin.christophe.vertx.depiler.event.server.works.Depiler;
import net.genin.christophe.vertx.depiler.event.server.works.Ws;

public class Verticles extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(Verticles.class);


    @Override
    public void start() throws Exception {
        vertx.deployVerticle(new DbInitializer(), new DeploymentOptions().setConfig(config()), (AsyncResult<String> h) -> {
            if (h.failed()) {
                logger.error("db initialization failed", h.cause());
                System.exit(-1);
                return;
            }

            Config hazelcastConfig = new Config();

            hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
            hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

            ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);


            VertxOptions options = new VertxOptions().setClusterManager(mgr);
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    String nodeID = mgr.getNodeID();
                    logger.info("nodeId : " + nodeID);
                    iAmTheNewMaster(vertx, nodeID);


                    mgr.nodeListener(new NodeListener() {
                        @Override
                        public void nodeAdded(String s) {

                        }

                        @Override
                        public void nodeLeft(String s) {
                            vertx.sharedData()
                                    .rxGetClusterWideMap("eventsMaster")
                                    .flatMap(map -> map.rxRemoveIfPresent("master", s))

                                    .subscribe(ok -> {
                                        if (ok) {
                                            iAmTheNewMaster(vertx, nodeID);
                                        }
                                    });

                        }
                    });


                    vertx.deployVerticle(Depiler.class.getName(),
                            new DeploymentOptions().setConfig(config()));

                    vertx.deployVerticle(Ws.class.getName(),
                            new DeploymentOptions().setConfig(config()));
                    logger.info("started.");
                }
            });

        });


    }

    private void iAmTheNewMaster(Vertx vertx, String nodeID) {
        vertx.sharedData()
                .rxGetClusterWideMap("eventsMaster")
                .flatMap(map -> map.rxPutIfAbsent("master", nodeID))
                .subscribe((currentMaster) -> {
                    logger.info("the current master is " + currentMaster);
                    Master.initialize(nodeID).setMaster(currentMaster);
                });
    }
}
