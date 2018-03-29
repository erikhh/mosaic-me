package org.highmoor.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TileTest {

  @BeforeEach
  void setUp() throws Exception {
  }

  @Test
  void testEquals() {
    Pixel pixel1 = Pixel.builder()
        .id(42)
        .red(37)
        .green(103)
        .blue(167)
        .fileName("b729ff94070a8b8720e8af318fd8f7f3b26b22bdad9922f2b39b005ca87b6b5c.jpg")
        .build();
    
    Pixel pixel2 = Pixel.builder()
        .id(43)
        .red(37)
        .green(103)
        .blue(167)
        .fileName("b729ff94070a8b8720e8af318fd8f7f3b26b22bdad9922f2b39b005ca87b6b5c.jpg")
        .build();
    
    Tile firstTile = Tile.builder()
        .height(32)
        .width(32)
        .pixel(pixel1)
        .build();
    
    Tile secondTile = Tile.builder()
        .height(32)
        .width(32)
        .pixel(pixel2)
        .build();
    
    assertEquals(firstTile, secondTile);
    assertEquals(firstTile.hashCode(), secondTile.hashCode());
    
  }

}
