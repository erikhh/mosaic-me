package org.highmoor.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.highmoor.api.Grid;
import org.highmoor.api.Mosaic;
import org.highmoor.api.Pixel;

/**
 * Generate a (part of) the photo mosaic.
 */
@Builder
@Slf4j
public class MosaicService {

  //private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f);
  @NonNull
  private ImageIndex imageIndex;
  
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
    
    log.debug("Selected pixels {}", pixels.size());
    
    Mosaic mosaic = new Mosaic();
    mosaic.setGrid(size);
    mosaic.setPixels(pixels);
    return mosaic;
  }
  
  public BufferedImage generateTile(Mosaic mosaic, int tileWidth, int tileHeigth, int x1, int y1, int x2, int y2) {
    int tilePixelWidth = Math.abs(x2 - x1) * tileWidth;
    int tilePixelHeight = Math.abs(y2 - y1) * tileHeigth;
    
    BufferedImage tile = new BufferedImage(tilePixelWidth, tilePixelHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = tile.createGraphics();
    ReentrantLock imageLock = new ReentrantLock();
    IntStream.range(x1, x2).parallel().forEach((x) -> {
      IntStream.range(y1, y2).parallel().forEach((y) -> {
        Pixel pixel = mosaic.getPixels().get((mosaic.getGrid().getWidth() * y) + x);
        add(imageLock, g, pixel, tileWidth, tileHeigth, x - x1, y - y1);
      });
    });
    g.dispose();
    
    return tile;
  }

  private void add(ReentrantLock imageLock, Graphics2D tile, Pixel pixel, int tileWidth, int tileHeigth, int x, int y) {
    BufferedImage pixelImage = imageIndex.getImage(pixel, tileWidth, tileHeigth);
    int tileX = x * tileWidth;
    int tileY = y * tileHeigth;
    imageLock.lock();
    try {
      tile.drawImage(pixelImage, tileX, tileY, tileX + tileWidth, tileY + tileHeigth, 0, 0, pixelImage.getWidth(), pixelImage.getHeight(), null);
      Color blend = new Color(pixel.getRed(), pixel.getGreen(), pixel.getBlue(), 96);
      tile.setPaint(blend);
      tile.fillRect(tileX, tileY, tileWidth, tileHeigth);
    } finally {
      imageLock.unlock();
    }
    
  }
}
