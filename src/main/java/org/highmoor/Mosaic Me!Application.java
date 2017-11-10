package org.highmoor;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Mosaic Me!Application extends Application<Mosaic Me!Configuration> {

    public static void main(final String[] args) throws Exception {
        new Mosaic Me!Application().run(args);
    }

    @Override
    public String getName() {
        return "Mosaic Me!";
    }

    @Override
    public void initialize(final Bootstrap<Mosaic Me!Configuration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final Mosaic Me!Configuration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
