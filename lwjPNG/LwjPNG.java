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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;

public class LwjPNG {

	private static int w, h, dataLen, cs; // cs: chunk size
	private static byte[] imgData = null, header = new byte[5];
	private static ByteBuffer buf = null;

	public static void init(InputStream in, boolean fullRead) throws IOException {
		readChunks(new DataInputStream(in), fullRead);
	}

	public static ByteBuffer scale(int fw, int fh) {
		if (buf != null)
			buf.clear();
		ByteBuffer bb = ByteBuffer.allocateDirect(cs);
		getImage(bb);
		int i = 0, bpx = 4;
		float dx = w / (float) fw, dy = h / (float) fh;
		buf = ByteBuffer.allocateDirect(4 * fw * fh);
		for (float y = 0; y < h; y += dy) {
			for (float x = 0; x < w; x += dx) {
				buf.putInt(bb.getInt((i + (int) x) * bpx));
			}
			i = w * (int) y;
		}
		bb.clear();
		buf.flip();
		return buf;
	}

	public static ByteBuffer decode() {
		if (buf != null)
			buf.clear();
		buf = ByteBuffer.allocateDirect(cs);
		getImage(buf);
		buf.flip();
		return buf;
	}

	private static void readChunks(DataInputStream in, boolean all) throws IOException {
		if (imgData == null && in.available() > 4)
			in.readLong(); // PNG signature
		else if (imgData == null) {
			w = 0;
			return;
		}
		dataLen = 0;
		int chunkType = 0;
		do {
			int chunkLen = in.readInt(); // Read the chunk length.
			if (chunkLen <= 0 || chunkLen > 99998192)
				break;
			chunkType = in.readInt();
			if (chunkType == (int) 0x49454e44) // IEND
				break; // last chunk reached..
			if (chunkType != (int) 0x49444154) { // IDAT
				if (chunkType == (int) 0x49484452) { // IHDR
					w = in.readInt();
					h = in.readInt();
					cs = 4 * w * h;
					imgData = new byte[in.available()]; // initialize image array
					in.readFully(header);
				} else {
					byte[] chunkData = new byte[chunkLen];
					in.readFully(chunkData);
				}
			} else {
				in.readFully(imgData, dataLen, chunkLen);
				dataLen += chunkLen;
			}
			in.readInt(); // checksum skip
		} while (all);
	}

	public static int getWidth() {
		return w;
	}

	public static int getHeight() {
		return h;
	}

	public static short getBitsPerPixel() {
		return (short) (header[0] & 0xFF);
	}

	public static short getColorType() {
		return (short) (header[1] & 0xFF);
	}

	public static short getCompression() {
		return (short) (header[2] & 0xFF);
	}

	public static short getFilter() {
		return (short) (header[3] & 0xFF);
	}

	public static short getInterlace() {
		return (short) (header[4] & 0xFF);
	}

	private static int ab(int a) { // absolute value
		int b = a >> 8;
		return (a ^ b) - b;
	}

	private static int paethP(int a, int b, int c) {
		int pa = b - c, pb = a - c, pc = ab(pa + pb);
		pa = ab(pa);
		pb = ab(pb);
		return (pa <= pb && pa <= pc) ? a : (pb <= pc) ? b : c;
	}

	private static void getImage(ByteBuffer bb) {
		// bPx bytes per pixel, in interlace, wT total output width, v scanline width
		// oH - output offset start horizontal; oV - output offset start vertical
		// rH - repetitions horizontal (rH[p]-1) << 2; rV - repetitions vertical
		// sw - scanline width per pass; sp - scanlines/rows per pass;

		int bPx = getColorType() == 2 ? 3 : 4, in = getInterlace() == 1 ? 7 : 1, wT = w * 4, v = w * bPx;
		int[] sw = { in == 7 ? ((w & 7) != 0 ? ((w / 8) + 1) * bPx : v / 8) : v,
				(w & 7) != 0 ? (w + 3) / 8 * bPx : v / 8, (w & 3) != 0 ? ((w / 4) + 1) * bPx : v / 4,
				(w & 3) != 0 ? (w + 1) / 4 * bPx : v / 4, (w & 1) != 0 ? ((w / 2) + 1) * bPx : v / 2, w / 2 * bPx, v };
		int[] sp = { in == 7 ? ((h & 7) != 0 ? (h / 8) + 1 : h / 8) : h, (h & 7) != 0 ? (h / 8) + 1 : h / 8,
				(h & 7) != 0 ? (h + 3) / 8 : h / 8, (h & 3) != 0 ? (h / 4) + 1 : h / 4,
				(h & 3) != 0 ? (h + 1) / 4 : h / 4, (h & 1) != 0 ? (h / 2) + 1 : h / 2, h / 2 };
		int[] oH = { 0, 16, 0, 8, 0, 4, 0 }, oV = { 0, 0, 4, 0, 2, 0, 1 };
		int[] rH = { in == 7 ? 28 : 0, 28, 12, 12, 4, 4, 0 }, rV = { in == 7 ? 8 : 1, 8, 8, 4, 4, 2, 2 };
		int oI = 0; // oI output offset/index

		Inflater inflater = new Inflater();
		inflater.setInput(imgData, 0, dataLen);

		for (int p = 0; p < in; p++) { // interlace passes..
			v = sw[p] + 1; // scanLine width
			byte[] row0 = new byte[wT + 1], row = new byte[wT + 1]; // every row contains filter byte!!!!
			oI = oH[p] + (oV[p] * wT); // start oI position
			for (int i = 1, s = 0; s < sp[p]; i = 1, s++) { // scanLine
				try {
					inflater.inflate(row, 0, v);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (row[0] != 0) { // apply filters
					if (row[0] == 1) {
						for (i += bPx; i < v; i++) {
							row[i] += row[i - bPx];
						}
					} else if (row[0] == 2) {
						for (; i < v; i++) {
							row[i] += row0[i];
						}
					} else if (row[0] == 3) {
						for (; i < bPx + 1; i++) {
							row[i] += (row0[i] & 0xFF) >>> 1;
						}
						for (; i < v; i++) {
							row[i] += ((row0[i] & 0xFF) + (row[i - bPx] & 0xFF)) >>> 1;
						}
					} else {
						for (; i < bPx + 1; i++) {
							row[i] += row0[i];
						}
						for (; i < v; i++) {
							row[i] += paethP(row[i - bPx] & 0xFF, row0[i] & 0xFF, row0[i - bPx] & 0xFF);
						}
					}
				}
				ByteBuffer wRow = ByteBuffer.wrap(row);
				if (in == 1) { // format output, normal mode
					if (bPx == 3) {
						for (i = 1; i < v; i += bPx) {
							bb.putInt((wRow.getInt(i) & 0xFFFFFF00) + 0xFF);
						}
					} else
						bb.put(row, 1, v - 1);
				} else { // interlaced mode, or normal mode
					if (bPx == 3)
						for (i = 1; i < v; i += bPx, oI += rH[p] + 4) {
							bb.putInt(oI, (wRow.getInt(i) & 0xFFFFFF00) + 0xFF);
						}
					else
						for (i = 1; i < v; i += bPx, oI += rH[p] + 4) {
							bb.putInt(oI, wRow.getInt(i));
						}
				}
				byte[] swap = row0;
				row0 = row;
				row = swap;
				// start oI position, increased by current scanline's iteration offset
				oI = oH[p] + (oV[p] * wT) + ((rV[p] * wT) * (s + 1));
			} // for scanLine
		}
		bb.position(bb.capacity());
		imgData = null;
	}
}