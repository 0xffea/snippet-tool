package org.abratuhi.snippettool.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetShape;

/**
 * Contains a procedure for cutting up an image into snippets.
 */
public final class ImageUtil {

	/**
	 * The ImageUtil class should not be instantiated.
	 */
	private ImageUtil() {
	}

	/**
	 * Cuts the snippets out of the input image file and returns an array
	 * containing the cut snippets.
	 * 
	 * @param inputImageFile
	 *            the image file containing the characters to cut out.
	 * @param characters
	 *            the characters in the image file.
	 * @param directory
	 *            the directory where the snippets should be saved.
	 * @param basename
	 *            the prefix of the snippets' file names.
	 * @return an array containing the file names of the snippets.
	 * @throws IOException
	 *             when either reading the input file or writing the snippets
	 *             fails.
	 */
	public static File[] cutSnippets(final File inputImageFile,
			final List<InscriptCharacter> characters, final String directory,
			final String basename) throws IOException {
		File dir = FileUtil.getTempdir(directory);
		File[] outputImageFiles = new File[characters.size()];
		BufferedImage inputImage = ImageIO.read(inputImageFile);
		for (int i = 0; i < characters.size(); i++) {
			InscriptCharacter ch = characters.get(i);

			BufferedImage outputImage = cutImage(inputImage, ch.shape);
			outputImageFiles[i] = new File(dir, basename + "_"
					+ characters.get(i).inscript.getId() + "_"
					+ characters.get(i).getNumber() + ".png");
			ImageIO.write(outputImage, "PNG", outputImageFiles[i]);
		}
		return outputImageFiles;
	}

	/**
	 * Returns the region described by the shape in the input image.
	 * 
	 * @author silvestre
	 * 
	 * @param inputImage
	 *            the image in which the region lies
	 * @param shape
	 *            the shape of the region
	 * @return the image in the region
	 */
	private static BufferedImage cutImage(final BufferedImage inputImage,
			final SnippetShape shape) {

		// Crop the input image to the bounding box of the shape
		Rectangle bounds = shape.main.getBounds();
		BufferedImage subImage = inputImage.getSubimage(bounds.x, bounds.y,
				bounds.width, bounds.height);

		// This makes sure that we can rotate around the center of the image,
		// which is also the center of the shape
		AffineTransformOp rotation = new AffineTransformOp(AffineTransform
				.getRotateInstance(shape.angle, subImage.getWidth() / 2,
						subImage.getHeight() / 2),
				AffineTransformOp.TYPE_BICUBIC);
		BufferedImage rotatedImage = rotation.createCompatibleDestImage(
				subImage, null);
		rotation.filter(subImage, rotatedImage);

		// Now we need to crop the image again, to the shape rectangle,
		// which is now axis-aligned. We get the new position of the left upper
		// point of the shape and cut accordingly.
		Point upperLeftPoint = new Point(shape.main.xpoints[0] - bounds.x,
				shape.main.ypoints[0] - bounds.y);
		Point rotatedUpperLeftPoint = point2DToPoint(rotation.getPoint2D(
				upperLeftPoint, null));
		BufferedImage boundedImage = rotatedImage.getSubimage(
				rotatedUpperLeftPoint.x, rotatedUpperLeftPoint.y,
				shape.base.width, shape.base.height);
		return boundedImage;
	}

	/**
	 * Rounds a double precision Point2D to an integer precision Point.
	 * 
	 * @param point2D
	 *            the point2D to convert
	 * @return a Point approximating the Point2D location, in integer precision.
	 */
	private static Point point2DToPoint(final Point2D point2D) {
		return new Point((int) Math.round(point2D.getX()), (int) Math
				.round(point2D.getY()));
	}

}
