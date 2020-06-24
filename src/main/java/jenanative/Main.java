package jenanative;

import org.apache.jena.fuseki.main.FusekiServer;

public class Main {

    public static void main(String[] args) {
        FusekiServer
                .create()
                .loopback(true)
                .port(4654)
                .build()
                .start();
    }

}
