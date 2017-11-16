package org.highmoor.core;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;
import org.highmoor.api.Mosaic;
import org.highmoor.api.Pixel;

public class MosaicService {

	private ImageIndex imageIndex;
	
	public BufferedImage generateTile(Mosaic mosaic, int tileWidth, int tileHeigth, int x1, int y1, int x2, int y2) {
		int tilePixelWidth = mosaic.getGrid().getWidth() * tileWidth;
		int tilePixelHeight = mosaic.getGrid().getHeigth() * tileHeigth;
		
		BufferedImage tile = new BufferedImage(tilePixelWidth, tilePixelHeight, BufferedImage.TYPE_INT_RGB);
		IntStream.range(x1, x2).parallel()
				.forEach((x) -> {
					IntStream.range(y1, y2).parallel()
					.forEach((y) -> {
						add(tile, mosaic, tileWidth, tileHeigth, x, y);
					});
				});
		
		return tile;
	}

	private void add(BufferedImage tile, Mosaic mosaic, int tileWidth, int tileHeigth, int x, int y) {
		Pixel pixel = mosaic.getPixels().get((mosaic.getGrid().getWidth() * y) + x);
		BufferedImage pixelImage = imageIndex.getImage(pixel, tileWidth, tileHeigth);
	}
}
