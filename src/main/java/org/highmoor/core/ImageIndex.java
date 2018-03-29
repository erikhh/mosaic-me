package org.highmoor.core;

import static java.lang.Math.round;

import com.google.common.base.Stopwatch;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.imageio.ImageIO;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.highmoor.api.Pixel;
import org.highmoor.util.collection.KDTree;

/**
 * Manage the index of tile images. When an image is added to the index the average RGB is
 * calculated, then the file is written to [indexDir]/[dimensions]/[red]/[green]/[blue]/[content-hash].jpg
 */
@Slf4j
public class ImageIndex {

  private static final Random RND = new Random();
  
  private static final String ORIG = "orig";
  
  @NonNull
  private String indexDir;
  
  private Map<Pixel, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();
  
  private KDTree colorIndex = new KDTree(3);
  
  @Builder
  private ImageIndex(@NonNull String indexDir) {
    this.indexDir = indexDir;
    log.warn("Loading index dir {}", indexDir);
    Stopwatch timer = Stopwatch.createStarted();
    File indexDirFile = Paths.get(indexDir, ORIG).toFile();
    if (!indexDirFile.exists()) {
      indexDirFile.mkdirs();
    }
    loadIndex(indexDirFile);
    log.info("Index loaded in: {}", timer.stop());
    log.info("Index size: {}", colorIndex.getSize());
  }
  
  private void loadIndex(File indexDir) {
    Arrays.stream(indexDir.listFiles()).forEach((file) -> {
      if (file.isDirectory()) {
        loadIndex(file);
      } else if ("jpg".equals(Files.getFileExtension(file.getName()))) {
        Path imagePath = file.toPath();
        int nameCount = imagePath.getNameCount();
        Pixel pixel = Pixel.builder()
            .red(Integer.valueOf(imagePath.getName(nameCount - 4).toString()))
            .green(Integer.valueOf(imagePath.getName(nameCount - 3).toString()))
            .blue(Integer.valueOf(imagePath.getName(nameCount - 2).toString()))
            .fileName(imagePath.getFileName().toString())
            .build();
        
        colorIndex.insert(pixel.getKey(), pixel);
      }
    });
  }

  private void lockRead(Pixel pixel) {
    if (locks.containsKey(pixel)) {
      locks.get(pixel).readLock().lock();
    } else {
      locks.put(pixel, new ReentrantReadWriteLock());
      lockRead(pixel);
    }
  }
  
  private void unlockRead(Pixel pixel) {
    locks.get(pixel).readLock().unlock();
  }
  
  private void lockWrite(Pixel pixel) {
    if (locks.containsKey(pixel)) {
      locks.get(pixel).writeLock().lock();
    } else {
      locks.put(pixel, new ReentrantReadWriteLock());
      lockWrite(pixel);
    }
  }
  
  private void unlockWrite(Pixel pixel) {
    locks.get(pixel).writeLock().unlock();
  }
  
  public Pixel addImage(@NonNull File imageFile) {
    try {
      DigestInputStream digestStream = new DigestInputStream(new FileInputStream(imageFile), MessageDigest.getInstance("SHA-256"));
      BufferedImage image = ImageIO.read(digestStream);
      int[] avgColor = averageColor(image);
      String fileName = 
          BaseEncoding.base16().encode(digestStream.getMessageDigest().digest()).toLowerCase() 
          + "." + Files.getFileExtension(imageFile.getName());
      
      File destination = Paths.get(indexDir, ORIG, format(avgColor[0]), format(avgColor[1]), format(avgColor[2]), fileName).toFile();
      Pixel pixel = Pixel.builder()
          .red((int) avgColor[0])
          .green((int) avgColor[1])
          .blue((int) avgColor[2])
          .fileName(fileName)
          .build();
      
      lockWrite(pixel);
      try {
        Files.createParentDirs(destination);
        ImageIO.write(image, "JPEG", destination);
        return pixel;
      } finally {
        unlockWrite(pixel);
      }
    } catch (Exception e) {
      log.error("Failed to read image", e);
      throw new RuntimeException(e);
    }
  }

  private int[] averageColor(BufferedImage image) {
    double avgRed = 0;
    double avgGreen = 0;
    double avgBlue = 0;
    int pixelCount = image.getWidth() * image.getHeight();
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgb = image.getRGB(x, y);
        double red = (rgb >> 16) & 0xff;
        double green = (rgb >> 8) & 0xff;
        double blue = (rgb) & 0xff;
        avgRed += red / pixelCount;
        avgGreen += green / pixelCount;
        avgBlue += blue / pixelCount;
      }
    }
    return new int[] {(int) round(avgRed), (int) round(avgGreen), (int) round(avgBlue)};
  }
  
  private String format(int value) {
    return String.valueOf(value);
  }

  public Pixel selectPixel(int red, int green, int blue) {
    double[] key = new double[] {(double) red, (double) green, (double) blue};
    
    Object[] selected = colorIndex.nearest(key, 25);
    
    Pixel theOne = (Pixel) selected[RND.nextInt(selected.length)];
    if (theOne == null) {
      throw new NullPointerException();
    }
    return theOne;
  }
  
  // Get image at desired size. Down scaling the original if not already present.
  public BufferedImage getImage(Pixel pixel, int width, int height) {
    Path basePath = Paths.get(format(pixel.getRed()), format(pixel.getGreen()), format(pixel.getBlue()), pixel.getFileName());
    Path dimensionPath = Paths.get(indexDir, width + "X" + height);
    
    lockRead(pixel);
    try {
      File scaledImage = dimensionPath.resolve(basePath).toFile();
      if (scaledImage.exists()) {
        return ImageIO.read(scaledImage);
      } else {
        unlockRead(pixel);
        lockWrite(pixel);
        try {
          Path originalPath = Paths.get(indexDir, ORIG).resolve(basePath);
          BufferedImage original = ImageIO.read(originalPath.toFile());
          BufferedImage scaled = new BufferedImage(width, height, original.getType());
          @Cleanup("dispose")
          Graphics2D g = scaled.createGraphics();
          g.drawImage(original.getScaledInstance(width, height, Image.SCALE_FAST), 0, 0, null);
          g.dispose();
          Files.createParentDirs(scaledImage);
          ImageIO.write(scaled, "JPEG", scaledImage);
          return getImage(pixel, width, height);
        } finally {
          unlockWrite(pixel);
          lockRead(pixel);
        }
      }
    } catch (IOException e) {
      log.error("Failed to read pixel {}, w: {}, h: {}", pixel, width, height, e);
      throw new RuntimeException(e);
    } finally {
      unlockRead(pixel);
    }
  }
}
