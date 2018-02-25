package org.highmoor;

import com.hubspot.dropwizard.guice.GuiceBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.http.QuotedQualityCSV;
import org.glassfish.jersey.logging.LoggingFeature;
import org.highmoor.cli.BuildIndexCommand;
import org.highmoor.resources.MosaicResource;
import org.highmoor.resources.PhotoResource;

/**
 * It starts here.
 */
public class MosaicMeApplication extends Application<MosaicMeConfiguration> {

  private GuiceBundle<MosaicMeConfiguration> guiceBundle;
  
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
    bootstrap.addBundle(new MultiPartBundle());
    guiceBundle = GuiceBundle.<MosaicMeConfiguration>newBuilder()
        .setInjectorFactory(new GovernatorInjectorFactory())
        .addModule(new MosaicMeModule())
        .setConfigClass(MosaicMeConfiguration.class)
        .build();

    bootstrap.addBundle(guiceBundle);
    bootstrap.addCommand(new BuildIndexCommand(this));
  }

  @Override
  public void run(final MosaicMeConfiguration configuration, final Environment environment) {
    environment.jersey().register(LoggingFeature.class);
    environment.jersey().register(guiceBundle.getInjector().getInstance(MosaicResource.class));
    environment.jersey().register(guiceBundle.getInjector().getInstance(PhotoResource.class));
  }

  public GuiceBundle<MosaicMeConfiguration> getGuiceBundle() {
    return guiceBundle;
  }
}
