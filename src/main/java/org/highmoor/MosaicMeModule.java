package org.highmoor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.governator.guice.lazy.LazySingleton;

import java.nio.file.Paths;
import org.highmoor.core.ImageIndex;
import org.highmoor.core.MosaicService;
import org.highmoor.resources.MosaicResource;
import org.highmoor.resources.PhotoResource;

/**
 * Guice module, wire everything up.
 */
public class MosaicMeModule extends AbstractModule {

  @Override
  protected void configure() {
    // TODO Auto-generated method stub
    
  }
  
  @Provides
  @LazySingleton
  public ImageIndex imageIndex(MosaicMeConfiguration config) {
    return ImageIndex.builder()
        .indexDir(config.getIndexDir())
        .build();
  }
  
  @Provides
  public MosaicResource mosaicResource(MosaicService mosaicService, MosaicMeConfiguration config) {
    return MosaicResource.builder()
        .mosaicService(mosaicService)
        .minTileDimension(config.getMinTileDimension())
        .mosaicDirectory(Paths.get(config.getMosaicDir()))
        .build();
  }

  @Provides
  public MosaicService mosaicService(ImageIndex imageIndex) {
    return MosaicService.builder()
        .imageIndex(imageIndex)
        .build();
  }
  
  @Provides
  public PhotoResource photoResource(MosaicService mosaicService, MosaicMeConfiguration config) {
    return PhotoResource.builder()
        .mosaicService(mosaicService)
        .mosaicDirectory(Paths.get(config.getMosaicDir()))
        .mosaicSize(config.getMosaicSize())
        .build();
  }
}
