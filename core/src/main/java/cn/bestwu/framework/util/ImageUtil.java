package cn.bestwu.framework.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图像处理工具类
 */
public class ImageUtil {

	/**
	 * 读取图像
	 *
	 * @param file 文件
	 * @return 图像
	 * @throws IOException IOException
	 */
	public static BufferedImage readImage(File file) throws IOException {
		return ImageIO.read(file);
	}

	/**
	 * 读取图像
	 *
	 * @param inputStream 输入流
	 * @return 图像
	 * @throws IOException IOException
	 */
	public static BufferedImage readImage(InputStream inputStream) throws IOException {
		BufferedImage image = ImageIO.read(inputStream); // 构造Image对象
		inputStream.close();
		return image;
	}

	/**
	 * 把图像写入文件
	 *
	 * @param im         图像
	 * @param formatName 格式名
	 * @param file       文件
	 * @return 是否成功
	 * @throws IOException IOException
	 */
	public static boolean writeImage(RenderedImage im, String formatName, File file) throws IOException {
		return ImageIO.write(im, formatName, file);
	}

	/**
	 * 把图像写入流
	 *
	 * @param im           图像
	 * @param formatName   格式名
	 * @param outputStream 输出流
	 * @return 是否成功
	 * @throws IOException IOException
	 */
	public static boolean writeImage(RenderedImage im, String formatName, OutputStream outputStream) throws IOException {
		return ImageIO.write(im, formatName, outputStream);
	}

	/**
	 * 根据文件名创建ImageBuilder
	 *
	 * @param fileName 文件名
	 * @return ImageBuilder
	 * @throws IOException IOException
	 */
	public static ImageBuilder builder(String fileName) throws IOException {
		return new ImageBuilder(new File(fileName));
	}

	/**
	 * 根据文件创建ImageBuilder
	 *
	 * @param file 文件
	 * @return ImageBuilder
	 * @throws IOException IOException
	 */
	public static ImageBuilder builder(File file) throws IOException {
		return new ImageBuilder(file);
	}

	/**
	 * 根据输入流创建ImageBuilder
	 *
	 * @param inputStream 输入流
	 * @return ImageBuilder
	 * @throws IOException IOException
	 */
	public static ImageBuilder builder(InputStream inputStream) throws IOException {
		return new ImageBuilder(inputStream);
	}

}
