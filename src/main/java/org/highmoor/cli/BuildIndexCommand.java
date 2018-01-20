package org.highmoor.cli;

import com.google.common.io.Files;
import com.google.inject.Inject;

import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.highmoor.MosaicMeApplication;
import org.highmoor.MosaicMeConfiguration;
import org.highmoor.core.ImageIndex;

/**
 * A cli command to build a image index from a directory with source images.
 */
@Slf4j
public class BuildIndexCommand extends EnvironmentCommand<MosaicMeConfiguration> {

  @Inject
  private ImageIndex imageIndex;
  
  public BuildIndexCommand(MosaicMeApplication application) {
    super(application, "build-index", "Build an image index from alls te images in a folder.");
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
  }

  @Override
  protected void run(Environment environment, Namespace namespace, MosaicMeConfiguration configuration) throws Exception {
    File sourceDir = new File(configuration.getSourceDir());
    final AtomicLong counter = new AtomicLong();
    forEachFile(sourceDir, (imageFile) -> {
      imageIndex.addImage(imageFile);
      if (counter.incrementAndGet() % 1000 == 0) {
        log.info("Added {}", counter.get());
      }
      return null;
    });
    log.info("Total files {}", counter.get());
  }

  private void forEachFile(File sourceDir, Function<File, Void> action) {
    Arrays.stream(sourceDir.listFiles()).parallel().forEach((file) -> {
      if (file.isDirectory()) {
        forEachFile(file, action);
      } else {
        if ("jpg".equals(Files.getFileExtension(file.getName()))) {
          action.apply(file);
        }
      }
    });

  }
}
