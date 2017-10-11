package net.genin.christophe.vertx.depiler.event.server;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Master {
    private static final Logger logger = LoggerFactory.getLogger(Master.class);
    private static Master instance;
    private final String current;
    private String master;

    public static Master getInstance() {
        return instance;
    }

    public synchronized static Master initialize(String current) {
        if (instance == null) {
            instance = new Master(current);
        }
        return instance;
    }


    private Master(String current) {
        this.current = current;
    }

    public synchronized Master setMaster(Object master) {
        this.master = (master == null) ? this.current : master.toString();
        return this;
    }

    public boolean isCurrent() {
        return current.equals(master);
    }
}
