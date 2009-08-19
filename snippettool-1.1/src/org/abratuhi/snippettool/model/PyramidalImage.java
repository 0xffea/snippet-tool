/**
 * @author silvestre
 *
 */
package org.abratuhi.snippettool.model;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.abratuhi.snippettool.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Utility class to read a <a
 * href="http://iipimage.sourceforge.net/documentation/images/">tiled pyramidal
 * image</a>. Simple images are a degenerated case of a tiled pyramidal image
 * and are processed correctly but not especially efficient.
 * 
 * @author silvestre
 * 
 */
public class PyramidalImage {

	/**
	 * A tile index value object. Basically a 3-d point in the pyramid. Used as
	 * key in the cache.
	 */
	private class Tile {
		/**
		 * The index of the page the tile is on (0 being the base of the
		 * pyramid).
		 */
		private final int imageIndex;

		/** The column index (starting with 0) of the tile. */
		private final int x;

		/** The row index (starting with 0) of the tile. */
		private final int y;

		/**
		 * Creates a tile index.
		 * 
		 * @param imageIndex
		 *            The index of the page the tile is on (0 being the base of
		 *            the pyramid).
		 * @param x
		 *            The column index (starting with 0) of the tile.
		 * @param y
		 *            The row index (starting with 0) of the tile.
		 */
		public Tile(final int imageIndex, final int x, final int y) {
			this.imageIndex = imageIndex;
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Tile)) {
				return false;
			} else {
				Tile t = (Tile) o;
				return t.imageIndex == imageIndex && t.x == x && t.y == y;
			}
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + imageIndex;
			result = 31 * result + x;
			result = 31 * result + y;
			return result;
		}

		@Override
		public String toString() {
			return String.format("Tile[%d, %d, %d]", imageIndex, x, y);
		}
	}

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(PyramidalImage.class);

	/** The image reader used to access the associated image. */
	private final ImageReader reader;

	/** Tile cache with soft references to the values. */
	private final Map<Tile, BufferedImage> cache;

	/** height of the image */
	private final int height;

	/** width of the image */
	private final int width;

	/**
	 * Creates a PyramidalImage instance accessing the image through the
	 * provided ImageReader.
	 * 
	 * @param reader
	 *            the reader already set up to read the actual image.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public PyramidalImage(final ImageReader reader) throws IOException {
		this.reader = reader;

		// TODO: This read access fixes a bug, that seems to stem from a
		// non-thread-safe tiff library. Check if thread-safety is documented.
		height = reader.getHeight(0);
		width = reader.getWidth(0);

		cache = new MapMaker().softValues().makeComputingMap(new Function<Tile, BufferedImage>() {
			public BufferedImage apply(final Tile tile) {
				try {
					long start = System.currentTimeMillis();
					BufferedImage rawTile = reader.readTile(tile.imageIndex, tile.x, tile.y);

					// Create a compatible image for faster drawing ...
					BufferedImage result = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
							.getDefaultConfiguration().createCompatibleImage(rawTile.getWidth(), rawTile.getHeight(),
									Transparency.OPAQUE);

					// and copy the loaded tile onto it.
					result.getGraphics().drawImage(rawTile, 0, 0, null);

					long loadTime = System.currentTimeMillis() - start;
					if (loadTime > 250) {
						logger.warn(
								"Loading tile {} took {} ms - consider checking image format (tiling, ICC profile)",
								tile, loadTime);
					} else {
						logger.trace("Loading tile {} took {} ms", tile, loadTime);
					}
					return result;
				} catch (IOException e) {
					logger.error("Could not load" + tile, e);
					return null;
				}
			}
		});
	}

	/**
	 * A factory creating a {@link PyramidalImage} for a given file.
	 * 
	 * @param imageFile
	 *            the image file the PyramidalImage should use.
	 * @return a {@link PyramidalImage} operating on the given file.
	 * @throws IOException
	 *             if reading the given file fails or if no suitable
	 *             {@link ImageReader} is found
	 */
	public static PyramidalImage loadImage(final File imageFile) throws IOException {
		ImageInputStream is = new FileImageInputStream(imageFile);
		ImageIO.scanForPlugins();
		Iterator<ImageReader> readers = ImageIO.getImageReaders(is);

		ImageReader reader = null;
		if (readers.hasNext()) {
			reader = readers.next();
		}
		if (reader == null) {
			throw new IOException("No ImageReader found for " + imageFile.toString() + " - is JAI Image I/O installed?");
		} else {
			reader.setInput(is);
			return new PyramidalImage(reader);
		}
	}

	/**
	 * Reads the tile indicated by the tileX and tileY arguments, returning it
	 * as a BufferedImage. If the arguments are out of range, an
	 * IllegalArgumentException is thrown. If the image is not tiled, the values
	 * 0, 0 will return the entire image; any other values will cause an
	 * IllegalArgumentException to be thrown.
	 * 
	 * @see ImageReader#readTile(int, int, int)
	 * 
	 * @param imageIndex
	 *            the index of the page the tile is on (0 being the base of the
	 *            pyramid).
	 * @param x
	 *            the column index (starting with 0) of the tile to be
	 *            retrieved.
	 * @param y
	 *            the row index (starting with 0) of the tile to be retrieved.
	 * @return the tile as BufferedImage
	 * @throws IOException
	 *             if the image cannot be read
	 */
	private BufferedImage getTile(final int imageIndex, final int x, final int y) throws IOException {
		return cache.get(new Tile(imageIndex, x, y));
	}

	/**
	 * Gets the base {@link Dimension} of the image.
	 * 
	 * @return the base dimension of the image.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public Dimension getDimension() {
		return new Dimension(getWidth(), getHeight());
	}

	/**
	 * Gets the scaled {@link Dimension} of the image.
	 * 
	 * @param scale
	 *            the scale to apply to the base image size
	 * @return the scaled dimension of the image.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public Dimension getDimension(final double scale) throws IOException {
		return new Dimension((int) Math.floor(getWidth() * scale), (int) Math.floor(getHeight() * scale));
	}

	/**
	 * Gets the base height of the image.
	 * 
	 * @return base height of the image.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the base width of the image.
	 * 
	 * @return base width of the image.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Draws the scaled image on the provided graphics context starting at (0,
	 * 0). If the clip bounds have been set up and the image is a tiled
	 * pyramidal image only the minimum number of tiles is loaded
	 * 
	 * @param scale
	 *            the scale to apply to the base image size
	 * @param g
	 *            the graphics context to draw upon.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	public void drawImage(final double scale, final Graphics g) throws IOException {
		double baseWidth = getWidth();
		double requestedWidth = scale * baseWidth;

		int imageIndex = 0;
		double preScale = 1.0;
		for (int i = 1; i < getNumImages(); i++) {
			double preScaledWidth = reader.getWidth(i);
			if (requestedWidth < preScaledWidth) {
				imageIndex = i;
				preScale = preScaledWidth / baseWidth;
			}
		}

		double postScale = scale / preScale;

		logger.trace("Requested scale of " + scale + " - selected index " + imageIndex + " with preScale " + preScale
				+ " and postScale " + postScale);

		Rectangle clip = g.getClipBounds();

		long start = System.currentTimeMillis();
		drawSubimage(g, imageIndex, postScale, clip);
		long drawTime = System.currentTimeMillis() - start;
		logger.trace("Drawing took {} ms", drawTime);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private int numImages = -1;

	private int getNumImages() throws IOException {
		if (numImages < 0) {
			numImages = reader.getNumImages(true);
		}
		return numImages;
	}

	/**
	 * Draws the clipped and scaled image on the provided graphics context
	 * starting at (0, 0) using the given image index. If image is a tiled
	 * pyramidal image only the minimum number of tiles is loaded
	 * 
	 * @param g
	 *            the graphics context to draw upon.
	 * @param imageIndex
	 *            the page index to use for drawing
	 * @param scale
	 *            the scale factor to apply to the (possibly pre-scaled) image
	 * @param clip
	 *            the clip rectangle to actually draw.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	private void drawSubimage(final Graphics g, final int imageIndex, final double scale, final Rectangle clip)
			throws IOException {

		// compute rect inside the image we should draw
		Rectangle image = new Rectangle((int) (reader.getWidth(imageIndex) * scale), (int) (reader
				.getHeight(imageIndex) * scale));
		Rectangle rect = clip.intersection(image);

		int lastTileY = (int) (Math.ceil(1.0 * reader.getHeight(imageIndex) / reader.getTileHeight(imageIndex)) - 1);
		int lastTileX = (int) Math.ceil(1.0 * reader.getWidth(imageIndex) / reader.getTileWidth(imageIndex)) - 1;

		double tileHeight = reader.getTileHeight(imageIndex) * scale;
		double tileWidth = reader.getTileWidth(imageIndex) * scale;

		int minTileX = (int) Math.floor(Math.max((rect.getMinX() / tileWidth), 0));
		int maxTileX = Math.min((int) Math.ceil(rect.getMaxX() / tileWidth), lastTileX);
		int minTileY = (int) Math.floor(Math.max((rect.getMinY() / tileHeight), 0));
		int maxTileY = Math.min((int) Math.ceil(rect.getMaxY() / tileHeight), lastTileY);

		for (int i = minTileX; i <= maxTileX; i++) {
			for (int j = minTileY; j <= maxTileY; j++) {
				int x = (int) Math.floor(i * tileWidth);
				int y = (int) Math.floor(j * tileHeight);

				BufferedImage tile = getTile(imageIndex, i, j);
				int width = (int) Math.ceil(tile.getWidth() * scale);
				int height = (int) Math.ceil(tile.getHeight() * scale);

				long start = System.currentTimeMillis();
				g.drawImage(tile, x, y, width, height, null);
				long drawTime = System.currentTimeMillis() - start;
				logger.trace("Drawing of tile (" + i + "," + j + ") took {} ms", drawTime);
			}
		}
	}

	/**
	 * Cuts the snippets out of the input image file and returns an array
	 * containing the cut snippets.
	 * 
	 * @param characters
	 *            the characters in the image file.
	 * @param directory
	 *            the directory where the snippets should be saved.
	 * @param basename
	 *            the prefix of the snippets' file names.
	 * @return an array containing the file names of the snippets.
	 * @throws IOException
	 *             if the image cannot be read or writing the snippets fails.
	 */
	public File[] cutSnippets(final List<InscriptCharacter> characters, final String directory, final String basename)
			throws IOException {
		File dir = FileUtil.getTempdir(directory);
		File[] outputImageFiles = new File[characters.size()];
		for (int i = 0; i < characters.size(); i++) {
			InscriptCharacter ch = characters.get(i);

			if (!ch.shape.isEmpty()) {
				BufferedImage outputImage = cutImage(ch.shape);
				outputImageFiles[i] = new File(dir, basename + "_" + ch.inscript.getId() + "_" + ch.getNumber()
						+ ".png");
				ImageIO.write(outputImage, "PNG", outputImageFiles[i]);
			}
		}
		return outputImageFiles;
	}

	/**
	 * Returns the region described by the shape in the input image.
	 * 
	 * @param shape
	 *            the shape of the region
	 * @return the image in the region
	 * @throws IOException
	 *             if the image cannot be read
	 */
	private BufferedImage cutImage(final SnippetShape shape) throws IOException {

		// Crop the input image to the bounding box of the shape
		Rectangle bounds = shape.main.getBounds();
		BufferedImage subImage = getSubimage(bounds);

		// This makes sure that we can rotate around the center of the image,
		// which is also the center of the shape
		AffineTransformOp rotation = new AffineTransformOp(AffineTransform.getRotateInstance(shape.angle, subImage
				.getWidth() / 2, subImage.getHeight() / 2), AffineTransformOp.TYPE_BICUBIC);
		BufferedImage rotatedImage = rotation.createCompatibleDestImage(subImage, null);
		rotation.filter(subImage, rotatedImage);

		// Now we need to crop the image again, to the shape rectangle,
		// which is now axis-aligned. We get the new position of the left upper
		// point of the shape and cut accordingly.
		Point upperLeftPoint = new Point(shape.main.xpoints[0] - bounds.x, shape.main.ypoints[0] - bounds.y);
		Point rotatedUpperLeftPoint = point2DToPoint(rotation.getPoint2D(upperLeftPoint, null));
		BufferedImage boundedImage = rotatedImage.getSubimage(rotatedUpperLeftPoint.x, rotatedUpperLeftPoint.y,
				shape.base.width, shape.base.height);
		return boundedImage;
	}

	/**
	 * Returns a subimage defined by a specified rectangular region.
	 * 
	 * @see BufferedImage#getSubimage(int, int, int, int)
	 * 
	 * @param bounds
	 *            the rectangular region to return.
	 * @return a BufferedImage that is the subimage of this image at the
	 *         specified region.
	 * @throws IOException
	 *             if the image cannot be read
	 */
	private BufferedImage getSubimage(final Rectangle bounds) throws IOException {
		BufferedImage result = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.translate(-bounds.x, -bounds.y);
		drawSubimage(g, 0, 1.0, bounds);
		return result;
	}

	/**
	 * Rounds a double precision Point2D to an integer precision Point.
	 * 
	 * @param point2D
	 *            the point2D to convert
	 * @return a Point approximating the Point2D location, in integer precision.
	 */
	private static Point point2DToPoint(final Point2D point2D) {
		return new Point((int) Math.round(point2D.getX()), (int) Math.round(point2D.getY()));
	}

}
