import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import java.util.Iterator;


class TestImageIOReaders
{
	public static void main(String args[]) {

		File imageFile = new File("GS_B25.tiff");
		try {
			ImageInputStream is = new FileImageInputStream(imageFile);

			ImageIO.scanForPlugins();

			Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
			if (readers.hasNext()) {
				System.out.println("Found a reader!");
			}
			String names[] = ImageIO.getReaderFormatNames();  
			for (int i = 0; i < names.length; ++i) {  
				System.out.println("reader " + names[i]);  
			}

		} catch (IOException e) {
			System.out.println("IOException");
		}
	}
}
