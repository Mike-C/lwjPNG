package lwjPNG;

/* 
 * 24 & 32 bit PNG decoder to RGBA; (c) 2017 rdavidian71@gmail.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program (see COPYING); if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA.
 * 
 */

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class Test {

	public static void main(String[] args) {

		String fileName = "image.png";

		System.out.println("reading " + fileName + ":");
		long t = System.nanoTime();

		try {

			FileInputStream fin = new FileInputStream(new File(fileName));
			BufferedInputStream fr = new BufferedInputStream(fin, 32768);
			LwjPNG png = new LwjPNG(fr, false); // false - read only image header
			ByteBuffer buf = null;
			int wN = 1, hN = 1;

			int w = png.getWidth(), h = png.getHeight();
			boolean isPow2 = true;
			while (w > 1) {
				wN *= 2;
				if ((w & 1) == 1 && w != 1)
					isPow2 = false;
				w /= 2;
			}
			while (h > 1) {
				hN *= 2;
				if ((w & 1) == 1 && w != 1)
					isPow2 = false;
				h /= 2;
			}

			System.out.println(" " + png.getWidth() + "x" + png.getHeight() + "px;");
			System.out.println(" image size power of 2: " + isPow2 + ";");
			if (!isPow2)
				System.out.println("  => downscalling: " + wN + "x" + hN + "px;");
			System.out.println(" Bit depth: " + (png.getColorType() == 2 ? 3 * 8 : 4 * 8) + "bit;");
			System.out.println(" interlace: " + png.getInterlace() + ";");

			png.init(true); // fully read rest of compressed PNG data
			
			if (!isPow2) {
				buf = png.scale(wN, hN); // new width & height
			} else {
				buf = png.decode(); // decode PNG data into ByteBuffer
			}

			fin.close();

			System.out.println("lwjPNG: done in " + (System.nanoTime() - t) / 1000000000.0f + "s");

//			String format = "JPG";
//			BufferedImage image = new BufferedImage(wN, hN, BufferedImage.TYPE_INT_RGB);
//			for (int x = 0; x < wN; x++) {
//				for (int y = 0; y < hN; y++) {
//					int i = (x + (wN * y)) * 4;
//					int r = buf.get(i) & 0xFF;
//					int g = buf.get(i + 1) & 0xFF;
//					int b = buf.get(i + 2) & 0xFF;
//					image.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
//				}
//			}
//			try {
//				ImageIO.write(image, format, new File("testOut.jpg"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			t = System.nanoTime();
			BufferedImage bimg = ImageIO.read(new File(fileName));
			System.out.println("ImageIO read in " + (System.nanoTime() - t) / 1000000000.0f + "s");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
