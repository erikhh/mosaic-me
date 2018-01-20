package org.highmoor.resources;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.highmoor.api.Grid;
import org.highmoor.api.Mosaic;
import org.highmoor.core.MosaicService;

/**
 * Resoure to serve out parts of the photo mosaic.
 */
@Builder
@Path("/mosaic/{x}/{y}/{z}")
@Produces("image/jpeg")
@Slf4j
public class MosaicResource {

  @NonNull
  private final MosaicService mosaicService;
  
  @NonNull
  private final Grid minTileDimension;
  
  @NonNull
  private final java.nio.file.Path mosaicDirectory;
  
  @GET
  public Response getMosaicTile(@PathParam("x") Integer x, @PathParam("y") Integer y, @PathParam("z") Integer z) throws IOException {
    Mosaic mosaic = getMostRecentMosaic();
    int tileWidth =  timesTimesTwo(minTileDimension.getWidth(), z - 1);
    int tileHeight = timesTimesTwo(minTileDimension.getHeight(), z - 1);
    
    int tileGridWidth = timesDivideTwo(mosaic.getGrid().getWidth(), z - 1);
    int tileGridHeight = timesDivideTwo(mosaic.getGrid().getHeight(), z - 1);
    
    int x1 = x * tileGridWidth;
    int y1 = y * tileGridHeight;
    int x2 = x1 + tileGridWidth;
    int y2 = y1 + tileGridHeight;
    
    BufferedImage tile = mosaicService.generateTile(mosaic, tileWidth, tileHeight, x1, y1, x2, y2);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(tile, "jpeg", baos);
    
    return Response.ok(new ByteArrayInputStream(baos.toByteArray())).build();
  }

  private Mosaic getMostRecentMosaic() {
    File[] files = mosaicDirectory.toFile().listFiles();
    Arrays.sort(files, (l, r) -> {
      return new Long(r.lastModified() - l.lastModified()).intValue();
    });
    
    try {
      log.debug("Reading mosaic: {}", files[0]);
      return new ObjectMapper().readValue(files[0], Mosaic.class);
    } catch (IOException e) {
      log.error("Failed to read mosaic; {}", files[0].getAbsolutePath());
      throw new RuntimeException(e);
    }
  }
  
  private int timesTimesTwo(int value, int timesTwo) {
    int result = value;
    for (int i = 0; i < timesTwo; i++) {
      result = result * 2;
    }
    return result;
  }
  
  private int timesDivideTwo(int value, int divdeTwo) {
    int result = value;
    for (int i = 0; i < divdeTwo; i++) {
      result = result / 2;
    }
    return result;
  }
}
