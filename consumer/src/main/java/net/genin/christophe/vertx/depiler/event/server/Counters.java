package net.genin.christophe.vertx.depiler.event.server;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.shareddata.Counter;
import rx.Single;

public class Counters {

    public static Single<Long> size(Vertx vertx) {
        return extract(vertx)
                .flatMap(Counter::rxGet);

    }

    public static Single<Counter> extract(Vertx vertx) {
        return vertx.sharedData()
                .rxGetCounter("treatments");
    }
}
