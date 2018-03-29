package org.highmoor.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.highmoor.core.ImageIndex;
import org.highmoor.util.CachePreLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoaderTest {

  private ExecutorService executorService;
  private Cache<Tile, BufferedImage> imageCache;
  private ImageIndex imageIndex;
  
  @BeforeEach
  void setUp() throws Exception {
    this.executorService = Executors.newCachedThreadPool();
    this.imageCache = CacheBuilder.newBuilder()
        .concurrencyLevel(8)
        .maximumSize(256 * 256 * 5)
        .recordStats()
        .build();
    this.imageIndex = ImageIndex.builder()
        .indexDir("/Users/erik/Desktop/work/indexed")
        .build();
  }

  @Test
  void shouldStopWhenBusy() throws Exception {
    Mosaic mosaic = new ObjectMapper().readValue(new File("src/test/resources/fixtures/yoda.json"), Mosaic.class);
    
    CachePreLoader cachePreLoader = CachePreLoader.builder()
        .imageCache(imageCache)
        .imageIndex(imageIndex)
        .mosaic(mosaic)
        .build();
    
    assertEquals(0, imageCache.size());
    
    Future<Void> runningPreLoader = executorService.submit(cachePreLoader);
    runningPreLoader.get();
    System.out.println(imageCache.stats().hitRate());
  }

}
