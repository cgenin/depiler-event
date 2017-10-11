package net.genin.christophe.vertx.depiler.event.server.works;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.shareddata.Counter;
import net.genin.christophe.vertx.depiler.event.server.Counters;
import net.genin.christophe.vertx.depiler.event.server.Master;
import net.genin.christophe.vertx.depiler.event.server.database.Dbs;
import rx.Observable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Depiler extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(Depiler.class);

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(10000L, l -> {
            if (!Master.getInstance().isCurrent()) {
                return;
            }
            Counters.size(vertx).subscribe(nb -> {
                logger.info("nb" + nb);
                if (nb > 0) {
                    return;
                }
                new Dbs(vertx, config())
                        .depile()
                        .map(ResultSet::toJson)
                        .flatMapObservable(jsonObject ->
                                Counters.extract(vertx)
                                        .flatMap(counter -> counter
                                                .rxAddAndGet(jsonObject.getLong("numRows"))
                                        )
                                        .flatMapObservable(newCount -> {
                                            logger.info("counter set to " + newCount);
                                            JsonArray results = jsonObject.getJsonArray("results", new JsonArray());
                                            List<JsonArray> list = results.getList();
                                            return Observable.from(list)
                                                    .map(o -> (JsonArray) o);
                                        })
                        )
                        .delay(200, TimeUnit.MILLISECONDS)
                        .subscribe(jsonObject -> {
                            vertx.eventBus().send(Ws.LAUNCH, jsonObject);
                        });
            });
        });
    }
}
