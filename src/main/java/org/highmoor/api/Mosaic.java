package org.highmoor.api;

import java.util.List;
import lombok.Data;

@Data
public class Mosaic {

	private Grid grid;
	private List<Pixel> pixels;
}
