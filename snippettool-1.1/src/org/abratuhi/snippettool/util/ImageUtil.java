package org.abratuhi.snippettool.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.abratuhi.snippettool.model.InscriptCharacter;

public class ImageUtil {

	public static File[] cutSnippets(File inputImageFile, ArrayList<InscriptCharacter> characters, String directory, String basename){
		if(!directory.endsWith(File.separator)) directory += File.separator;
		File[] outputImageFiles = new File[characters.size()];
		BufferedImage inputImage = load(inputImageFile);
		for(int i=0; i<characters.size(); i++){
			Rectangle characterSnippet = characters.get(i).shape.getBounds();
			BufferedImage outputImage = inputImage.getSubimage((int)Math.max(0,characterSnippet.getX()), 
					(int)Math.max(0, characterSnippet.getY()), 
					(int)Math.max(1, Math.min(inputImage.getWidth()-characterSnippet.getX(), characterSnippet.getWidth())), 
					(int)Math.max(1, Math.min(inputImage.getHeight()-characterSnippet.getY(), characterSnippet.getHeight())));
			outputImageFiles[i] = new File(directory + basename + "_" + characters.get(i).inscript.id+"_"+characters.get(i).getNumber()+".png");
			store(outputImage, "PNG", outputImageFiles[i]);
		}
		return outputImageFiles;
	}

	public static void store(BufferedImage image, String format, File f){
		try {
			ImageIO.write(image, format, f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage load(File f){
		try {
			return ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
