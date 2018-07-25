package f5.helpers;

public class RawImage {

	final private int pixels[];
	final private int width;
	final private int height;
	
	public RawImage(int pixels[], int width, int height) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		
		if(pixels.length != width * height) throw new IllegalStateException("Invalid Size");
	}
	
	public int[] getPixels() {
		return pixels;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
