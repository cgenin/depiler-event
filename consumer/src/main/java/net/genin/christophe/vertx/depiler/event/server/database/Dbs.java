package net.genin.christophe.vertx.depiler.event.server.database;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import rx.Single;

import java.util.Optional;

public class Dbs {

    private final static Logger logger = LoggerFactory.getLogger(Dbs.class);

    private final Vertx vertx;
    private final JsonObject config;

    public Dbs(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    public JDBCClient getJdbcclient() {
        JsonObject dataSourceJson = Optional.ofNullable(config.getJsonObject("dataSource"))
                .orElseThrow(() -> new IllegalArgumentException("No datasource configured"));
        return JDBCClient.createShared(vertx, dataSourceJson);
    }

    public Single<ResultSet> depile() {

        return getJdbcclient().rxGetConnection()
                .flatMap(conn ->
                        conn.rxCall("Select * from events order by retry asc,  priority desc")
                                .doAfterTerminate(() -> {
                                    logger.debug("close connection");
                                    conn.close();
                                })

                );
    }
}
