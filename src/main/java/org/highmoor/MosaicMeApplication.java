package org.highmoor;

import com.hubspot.dropwizard.guice.GuiceBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.logging.LoggingFeature;

public class MosaicMeApplication extends Application<MosaicMeConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MosaicMeApplication().run(args);
    }

    @Override
    public String getName() {
        return "Mosaic Me!";
    }

    @Override
    public void initialize(final Bootstrap<MosaicMeConfiguration> bootstrap) {
    		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    		
    		GuiceBundle<MosaicMeConfiguration> guiceBundle = GuiceBundle.<MosaicMeConfiguration>newBuilder()
    	            .addModule(new MosaicMeModule())
    	            .enableAutoConfig(getClass().getPackage().getName())
    	            .setConfigClass(MosaicMeConfiguration.class)
    	            .build();
    	    
    	    bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(final MosaicMeConfiguration configuration,
                    final Environment environment) {
    		environment.jersey().register(LoggingFeature.class);
    }

}
