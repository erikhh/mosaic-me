package org.highmoor;

import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.highmoor.api.Grid;

/**
 *  Configuration pojo.
 *
 */
@Getter
@Setter
public class MosaicMeConfiguration extends Configuration {
  private String indexDir;
  private String sourceDir;
  private String mosaicDir;
  
  @NotNull
  private Grid minTileDimension;
  
  @NotNull
  private Grid mosaicSize;
}
