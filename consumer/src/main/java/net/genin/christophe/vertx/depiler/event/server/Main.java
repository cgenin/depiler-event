package net.genin.christophe.vertx.depiler.event.server;

import io.vertx.core.Launcher;

public class Main extends Launcher {

    public static void main(String[] args) {
        new Main().dispatch(args);
    }
}
