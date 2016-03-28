package cn.bestwu.framework.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageBuilder {
	private BufferedImage bufferedImage;
	private String formatName;
	private int width;
	private int height;
	private int type;

	public ImageBuilder(Object inputStream) throws IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
		ImageReader reader = readers.next();
		reader.setInput(iis);
		formatName = reader.getFormatName();
		bufferedImage = reader.read(0);
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
		type = bufferedImage.getType();
		if (type == BufferedImage.TYPE_CUSTOM) {
			type = BufferedImage.TYPE_INT_ARGB;
		}
		reader.dispose();
		iis.close();
	}

	public ImageBuilder scaleTrim(Integer width, Integer height) {
		double scale = 1.0;
		if (width == null) {
			width = this.width;
		}
		if (height == null) {
			height = this.height;
		}
		scale = width * scale / this.width;
		double newheight = this.height * scale;
		if (height > newheight) {
			scale = height * scale / newheight;
		}
		if (scale != 1) {
			scale(scale);
		}
		sourceRegion(width, height);
		return this;
	}

	/**
	 * 根据最小高宽 自动缩放
	 *
	 * @param min_width -1 表示不限制
	 * @param min_hight -1 表示不限制
	 * @return ImageBuilder
	 */
	public ImageBuilder autoScale(int min_width, int min_hight) {
		if (this.width <= min_width || this.height <= min_hight) {
			return scale(1);
		} else {
			double w = min_width * 1.0 / this.width;
			double h = min_hight * 1.0 / this.height;
			if (min_hight == -1) {
				return scale(w);
			}
			if (min_width == -1) {
				return scale(h);
			}
			return scale(w > h ? w : h);
		}
	}

	public ImageBuilder scale(double scale) {
		if (scale != 1) {
			width = (int) Math.ceil(this.width * scale);
			height = (int) Math.ceil(this.height * scale);
			Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage bufImg = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
			Graphics2D g = bufImg.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			bufImg.flush();
			bufferedImage = bufImg;
		}
		return this;
	}

	public ImageBuilder sourceRegion(int width, int height) {
		return sourceRegion(width, height, Origin.CENTER);
	}

	public ImageBuilder sourceRegion(int width, int height, Origin origin) {
		if (width > this.width) {
			width = this.width;
		}
		if (height > this.height) {
			height = this.height;
		}
		int x = 0;
		int y = 0;
		switch (origin) {
		case LEFT_BOTTOM:
			y = this.height - height;
			break;
		case LEFT_TOP:
			break;
		case RIGHT_BOTTOM:
			x = this.width - width;
			y = this.height - height;
			break;
		case RIGHT_TOP:
			x = this.width - width;
			break;
		case CENTER:
			x = (this.width - width) / 2;
			y = (this.height - height) / 2;
			break;
		}
		return sourceRegion(x, y, width, height);
	}

	public ImageBuilder sourceRegion(int x, int y, int width, int height) {
		if (width < this.width) {
			this.width = width;
		}
		if (height < this.height) {
			this.height = height;
		}
		bufferedImage = bufferedImage.getSubimage(x, y, this.width, this.height);
		return this;
	}

	public boolean toFile(String fileName) throws IOException {
		return ImageUtil.writeImage(bufferedImage, formatName, new File(fileName));
	}

	public boolean toFile(File file) throws IOException {
		return ImageUtil.writeImage(bufferedImage, formatName, file);
	}

	public boolean toOutputStream(OutputStream os) throws IOException {
		return ImageUtil.writeImage(bufferedImage, formatName, os);
	}

	public ImageBuilder outputFormat(String formatName) {
		if (org.springframework.util.StringUtils.hasText(formatName) && !this.formatName.equalsIgnoreCase(formatName)) {
			/*
			 * Note: The following code is a workaround for the JPEG writer
			 * which ships with the JDK.
			 * 
			 * At issue is, that the JPEG writer appears to write the alpha
			 * channel when it should not. To circumvent this, images which are
			 * to be saved as a JPEG will be copied to another BufferedImage
			 * without an alpha channel before it is saved.
			 * 
			 * Also, the BMP writer appears not to support ARGB, so an RGB image
			 * will be produced before saving.
			 */
			if (formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("jpeg") || formatName.equalsIgnoreCase("bmp")) {
				bufferedImage = copy(bufferedImage, BufferedImage.TYPE_INT_RGB);
			}
			this.formatName = formatName;
		}
		return this;
	}

	private BufferedImage copy(BufferedImage bufferedImage, int imageType) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		BufferedImage newImage = new BufferedImage(width, height, imageType);
		Graphics g = newImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.drawImage(bufferedImage, 0, 0, null);

		g.dispose();

		return newImage;

	}

	public BufferedImage asBufferedImages() {
		return this.bufferedImage;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getFormatName() {
		return formatName;
	}

	public int getType() {
		return type;
	}

	public enum Origin {
		LEFT_BOTTOM, LEFT_TOP, RIGHT_BOTTOM, RIGHT_TOP, CENTER
	}

}