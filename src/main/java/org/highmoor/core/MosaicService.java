package org.highmoor.core;

import com.google.common.cache.Cache;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.highmoor.api.Grid;
import org.highmoor.api.Mosaic;
import org.highmoor.api.Pixel;
import org.highmoor.api.Tile;

/**
 * Generate a (part of) the photo mosaic.
 */
@Builder
@Slf4j
public class MosaicService {
  
  @NonNull
  private ImageIndex imageIndex;
  
  @NonNull
  private Cache<Tile, BufferedImage> imageCache;
  
  public Mosaic generateMosaic(BufferedImage sourceImage) {
    Grid size = new Grid(sourceImage.getWidth(), sourceImage.getHeight());
    List<Pixel> pixels = new ArrayList<>(size.getWidth() * size.getHeight());
    ReentrantLock pixelLock = new ReentrantLock();
    IntStream.range(0, size.getWidth()).parallel().forEach((x) -> {
      IntStream.range(0, size.getHeight()).parallel().forEach((y) -> {
        int id = (size.getWidth() * y) + x;
        int rgb = sourceImage.getRGB(x, y);
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = (rgb) & 0xff;
        Pixel selected = imageIndex.selectPixel(red, green, blue)
            .toBuilder()
            .id(id)
            .build();
        pixelLock.lock();
        pixels.add(selected);
        pixelLock.unlock();
      });
    });
    
    Mosaic mosaic = new Mosaic();
    mosaic.setGrid(size);
    mosaic.setPixels(pixels);
    return mosaic;
  }
  
  public BufferedImage generateTile(Mosaic mosaic, int tileWidth, int tileHeigth, int x1, int y1, int x2, int y2) {
    int tilePixelWidth = Math.abs(x2 - x1) * tileWidth;
    int tilePixelHeight = Math.abs(y2 - y1) * tileHeigth;
    
    BufferedImage tile = new BufferedImage(tilePixelWidth, tilePixelHeight, BufferedImage.TYPE_INT_RGB);
    @Cleanup("dispose")
    Graphics2D g = tile.createGraphics();
    ReentrantLock imageLock = new ReentrantLock();
    IntStream.range(x1, x2).parallel().forEach((x) -> {
      IntStream.range(y1, y2).parallel().forEach((y) -> {
        Pixel pixel = mosaic.getPixels().get((mosaic.getGrid().getWidth() * y) + x);
        Tile pixelTile = Tile.builder()
            .pixel(pixel)
            .width(tileWidth)
            .height(tileHeigth)
            .build();
        add(imageLock, g, pixelTile, x - x1, y - y1);
      });
    });
    g.dispose();
    
    log.warn("Cache\n hit rate: {}\n size: {}\n evictions: {}", imageCache.stats().hitRate(), imageCache.size(), imageCache.stats().evictionCount());
    
    return tile;
  }

  @SneakyThrows
  private void add(ReentrantLock imageLock, Graphics2D tile, Tile pixelTile, int x, int y) {
    final BufferedImage pixelImage; 
    if (pixelTile.getWidth() >= 32) {
      pixelImage = imageIndex.getImage(pixelTile.getPixel(), pixelTile.getWidth(), pixelTile.getHeigth());
    } else {
      pixelImage = imageCache.get(pixelTile, () -> {
        return imageIndex.getImage(pixelTile.getPixel(), pixelTile.getWidth(), pixelTile.getHeigth());
      });
    }
    int tileX = x * pixelTile.getWidth();
    int tileY = y * pixelTile.getHeigth();
    imageLock.lock();
    try {
      tile.drawImage(
          pixelImage, 
          tileX, 
          tileY, tileX + pixelTile.getWidth(), 
          tileY + pixelTile.getHeigth(), 
          0, 0, pixelImage.getWidth(), pixelImage.getHeight(), null);
      Color blend = new Color(pixelTile.getRed(), pixelTile.getGreen(), pixelTile.getBlue(), 96);
      tile.setPaint(blend);
      tile.fillRect(tileX, tileY, pixelTile.getWidth(), pixelTile.getHeigth());
    } finally {
      imageLock.unlock();
    }
    
  }
}
