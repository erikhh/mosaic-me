package org.highmoor.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

/**
 * One pixel in the photo mosaic.
 */
@Builder(toBuilder = true)
@Data
public class Pixel {
  private int id;
  private int red;
  private int green;
  private int blue;
  private String fileName;
  
  @JsonIgnore
  public RGB getRGB() {
    return new RGB(red, green, blue);
  }
  
  @JsonIgnore
  public double[] getKey() {
    return new double[] {(double) red, (double) green, (double) blue};
  }
}
