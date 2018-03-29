package org.highmoor.resources;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.highmoor.api.Grid;
import org.highmoor.api.Mosaic;
import org.highmoor.core.MosaicService;

/**
 * Generate a new photo mosaic for the uploaded photo.
 */
@Builder
@Path("/mosaic/")
@Slf4j
public class PhotoResource {

  @NonNull
  private final MosaicService mosaicService;
  
  @NonNull
  private final Grid mosaicSize;
  
  @NonNull
  private java.nio.file.Path mosaicDirectory;
  
  @Builder.Default
  private Future<Void> runningCacheLoader = null;
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Object newPhoto(@FormDataParam("webcam") InputStream fileInputStream) throws IOException, URISyntaxException {    
    BufferedImage original = ImageIO.read(fileInputStream);
    log.debug("Original w:{} h: {}", original.getWidth(), original.getHeight());
    int x = 0;
    int y = 0;
    int w = original.getWidth();
    int h = original.getHeight();

    if (w > h) {
      x = (w - h) / 2;
      w = h;
    } else if (h > w) {
      y = (h - w) / 2;
      h = w;
    }
    
    log.debug("Copped x:{} y:{} w: {} h:{}", x, y, w, h);
    BufferedImage cropped = original.getSubimage(x, y, w, h);
    BufferedImage scaled = new BufferedImage(mosaicSize.getWidth(), mosaicSize.getHeight(), original.getType());
    @Cleanup("dispose")
    Graphics2D g = scaled.createGraphics();
    g.drawImage(cropped.getScaledInstance(mosaicSize.getWidth(), mosaicSize.getHeight(), Image.SCALE_FAST), 0, 0, null);
    g.dispose();
    
    Mosaic mosaic = mosaicService.generateMosaic(scaled);
    File mosaicFile = Paths.get(mosaicDirectory.toString(), "mosaic.json").toFile();
    new ObjectMapper().writeValue(mosaicFile, mosaic);
    return Response.ok().build();
  }
  
}
