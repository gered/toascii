package toascii.images;

import java.awt.image.BufferedImage;

public class ImageToAscii {
	static final char[] asciiChars = {'#', 'A', '@', '%', '$', '+', '=', '*', ':', ',', '.', ' '};
	static final int spanLength = "<span style=\"color:rgb(255,255,255);\">X</span>".length();
	static final int lineTerminatorLength = "<br>".length();

	public static String convert(BufferedImage image, boolean useColor) {
		int width = image.getWidth();
		int height = image.getHeight();

		int maxLength = (useColor ?
		                 (width * height * spanLength) + (height * lineTerminatorLength) :
		                 (width * height) + height);

		StringBuilder sb = new StringBuilder(maxLength);

		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int argb = pixels[(y * width) + x];
				int r = (0x00ff0000 & argb) >> 16;
				int g = (0x0000ff00 & argb) >> 8;
				int b = (0x000000ff & argb);
				int brightness = (int)Math.sqrt((r * r * 0.241f) +
				                                (g * g * 0.691f) +
				                                (b * b * 0.068f));
				int charIndex;
				if (brightness == 0.0f)
					charIndex = asciiChars.length - 1;
				else
					charIndex = (int)((brightness / 255.0f) * asciiChars.length) - 1;

				char pixelChar = asciiChars[charIndex > 0 ? charIndex : 0];

				if (useColor) {
					sb.append("<span style=\"color:rgb(");
					sb.append(r);
					sb.append(',');
					sb.append(g);
					sb.append(',');
					sb.append(b);
					sb.append(");\">");
					sb.append(pixelChar);
					sb.append("</span>");
				} else
					sb.append(pixelChar);
			}
			if (useColor)
				sb.append("<br>");
			else
				sb.append('\n');
		}

		return sb.toString();
	}
}
