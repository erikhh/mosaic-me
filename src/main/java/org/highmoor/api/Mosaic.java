package org.highmoor.api;

import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * The Json reporesentation of a Mosaic.
 */
@Data
public class Mosaic {
  private Grid grid;
  private List<Pixel> pixels;
  
  
  public void setPixels(List<Pixel> pixels) {
    Collections.sort(pixels, (l, r) -> {
      if (r == null && l == null) {
        return 0;
      }
      if (l == null) {
        return -1;
      }
      if (r == null) {
        return 1;
      }
      return l.getId() - r.getId(); 
    });
    
    this.pixels = pixels;
  }
  
}
