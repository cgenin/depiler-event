package net.genin.christophe.vertx.depiler.event.server.works;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.shareddata.Counter;
import net.genin.christophe.vertx.depiler.event.server.Counters;

public class Ws extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(Ws.class);

    public static String LAUNCH = Ws.class.getName() + ".launch";

    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonArray>consumer(LAUNCH, handler -> {
            JsonArray arr = handler.body();
            //TODO call the webservice
            vertx.setTimer(250L, l -> {
                Counters.extract(vertx)
                        .flatMap(Counter::rxDecrementAndGet)
                        .subscribe(nb -> {
                            logger.info(nb + "decrement for " + arr.encode());
                        });
            });
        });
    }
}
