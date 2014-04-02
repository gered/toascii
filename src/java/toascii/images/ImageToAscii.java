package toascii.images;

import java.awt.image.BufferedImage;

public class ImageToAscii {
	static final char[] asciiChars = {'#', 'A', '@', '%', '$', '+', '=', '*', ':', ',', '.', ' '};
	static final int numAsciiChars = asciiChars.length - 1;
	static final int spanLength = "<span style=\"color:#112233;\">X</span>".length();
	static final int lineTerminatorLength = "<br>".length();

	// copied from java.lang.Integer.digits (which is private so we can't just reference it, boourns)
	static final char[] digits = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};

	// modification of java.lang.Integer.toUnsignedString -- no garbage generated, but limited to max value
	// of 255 ...hence the 'unsigned byte' thing :)
	private static void unsignedByteToHex(int unsignedByte, StringBuilder sb) {
		for (int i = 0; i < 2; ++i) {
			int index = sb.length + 1 - i;
			if (unsignedByte != 0) {
				sb.chars[index] = digits[unsignedByte & 15];
				unsignedByte >>>= 4;
			} else
				sb.chars[index] = '0';
		}
		sb.length += 2;
	}

	public static String convert(BufferedImage image, boolean useColor) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final int maxLength = (useColor ?
		                       (width * height * spanLength) + (height * lineTerminatorLength) :
		                       (width * height) + height);

		final StringBuilder sb = new StringBuilder(maxLength);

		final int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				final int argb = pixels[(y * width) + x];
				final int r = (0x00ff0000 & argb) >> 16;
				final int g = (0x0000ff00 & argb) >> 8;
				final int b = (0x000000ff & argb);
				final double brightness = Math.sqrt((r * r * 0.241f) +
				                                (g * g * 0.691f) +
				                                (b * b * 0.068f));
				int charIndex;
				if (brightness == 0.0f)
					charIndex = numAsciiChars;
				else
					charIndex = (int)((brightness / 255.0f) * numAsciiChars);

				final char pixelChar = asciiChars[charIndex > 0 ? charIndex : 0];

				if (useColor) {
					sb.append("<span style=\"color:#");
					unsignedByteToHex(r, sb);
					unsignedByteToHex(g, sb);
					unsignedByteToHex(b, sb);
					sb.append(";\">");
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
