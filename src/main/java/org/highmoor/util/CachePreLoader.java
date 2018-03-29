package org.highmoor.util;

import com.google.common.cache.Cache;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.highmoor.api.Mosaic;
import org.highmoor.api.Tile;
import org.highmoor.core.ImageIndex;

/**
 * A async task we can fire to pro-activly start loading files from disk.
 */
@Builder
@Slf4j
public class CachePreLoader implements Callable<Void> {

  private static final int[] SIZES = {2, 4, 8, 16, 32};
  
  private final Cache<Tile, BufferedImage> imageCache;
  private final ImageIndex imageIndex;
  
  @Setter
  private Mosaic mosaic;
  
  @Override
  public Void call() throws Exception {
    Arrays.stream(SIZES)
        .forEach(size -> {
          preLoadBySize(size);
        });
    return null;
  }

  private void preLoadBySize(int size) {
    mosaic.getPixels()
        .forEach(pixel -> {
          Tile tile = Tile.builder()
              .height(size)
              .width(size)
              .pixel(pixel)
              .build();
          try {
            preLoadTile(tile);
          } catch (ExecutionException e) {
            log.error("Oopsie", e);
          }
        });
  }

  private void preLoadTile(Tile tile) throws ExecutionException {
    imageCache.get(tile, () -> {
      return imageIndex.getImage(tile.getPixel(), tile.getWidth(), tile.getHeigth());
    });
  }
}
