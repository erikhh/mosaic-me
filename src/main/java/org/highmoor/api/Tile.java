package org.highmoor.api;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Data about a tile in the mosaic.
 */
@Data
@EqualsAndHashCode(exclude = {"id"})
public class Tile {
  
  private int id;
  private int red;
  private int green;
  private int blue;
  private String fileName;
  private int width; 
  private int heigth;
  
  @Builder
  private Tile(Pixel pixel, int width, int height) {
    this.id = pixel.getId();
    this.red = pixel.getRed();
    this.green = pixel.getGreen();
    this.blue = pixel.getBlue();
    this.fileName = pixel.getFileName();
    this.width = width;
    this.heigth = height;
  }
  
  public Pixel getPixel() {
    return Pixel.builder()
        .id(id)
        .red(red)
        .green(green)
        .blue(blue)
        .fileName(fileName)
        .build();
  }
}
