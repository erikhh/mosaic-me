package org.highmoor.api;

import lombok.Value;

/**
 * A RGB value.
 */
@Value
public class RGB implements Comparable<RGB> {
  int red;
  int green;
  int blue;
  
  @Override
  public int compareTo(RGB o) {
    double distance = Math.pow((this.getRed() - o.getRed()), 2d) 
        + Math.pow((this.getGreen() - o.getGreen()), 2d)
        + Math.pow((this.getBlue() - o.getBlue()), 2d);
    return (int) Math.round(distance);
  }
}
