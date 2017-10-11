package net.genin.christophe.vertx.depiler.event.server.database;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DbInitializer extends io.vertx.rxjava.core.AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(DbInitializer.class);


    @Override
    public void start() throws Exception {
        new Dbs(vertx, config()).depile()
                .subscribe(rs -> {
                    logger.info("db is UP");
                }, err -> {
                    logger.error("Error in opening connection", err);
                    throw new IllegalArgumentException("Error in opening connection");
                });
    }
}
