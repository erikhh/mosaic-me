package org.highmoor.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.highmoor.api.Pixel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the image index.
 */
public class ImageIndexTests {

  private ImageIndex imageIndex;
  
  @BeforeEach
  public void setup() {
    this.imageIndex = ImageIndex.builder()
        .indexDir("target/testIndex")
        .build();
  }
  
  @Test
  void shouldAddImage() throws Exception {
    String expectedNewFile = "target/testIndex/orig/121/134/132/dd7a474a18a8d1670ca5ea16e166c336f9dffec249cfc7971908e83c161232c5.jpg";
    assertThat(new File(expectedNewFile).exists()).isFalse();
    
    File imageFile = new File("src/test/resources/fixtures/rainbow.jpg");  
    
    Pixel added = imageIndex.addImage(imageFile);
    
    assertThat(added).isNotNull();
    assertThat(added.getRed()).isEqualTo(121);
    assertThat(added.getBlue()).isEqualTo(134);
    assertThat(added.getGreen()).isEqualTo(132);
  }
}
