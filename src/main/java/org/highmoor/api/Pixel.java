package org.highmoor.api;

import lombok.Data;

@Data
public class Pixel {
	private int id;
	private int red;
	private int green;
	private int blue;
	private String fileName;
}
