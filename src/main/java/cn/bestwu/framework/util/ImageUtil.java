package cn.bestwu.framework.util;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtil {

	public static BufferedImage readImage(File file) throws IOException {
		return ImageIO.read(file);
	}

	public static BufferedImage readImage(InputStream inputStream) throws IOException {
		BufferedImage image = ImageIO.read(inputStream); // 构造Image对象
		inputStream.close();
		return image;
	}

	public static boolean writeImage(RenderedImage im, String formatName, File file) throws IOException {
		return ImageIO.write(im, formatName, file);
	}

	public static boolean writeImage(RenderedImage im, String formatName, OutputStream outputStream) throws IOException {
		return ImageIO.write(im, formatName, outputStream);
	}

	public static ImageBuilder builder(String fileName) throws IOException {
		return new ImageBuilder(new File(fileName));
	}

	public static ImageBuilder builder(File file) throws IOException {
		return new ImageBuilder(file);
	}

	public static ImageBuilder builder(InputStream inputStream) throws IOException {
		return new ImageBuilder(inputStream);
	}

}
