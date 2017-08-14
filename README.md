# lwjPNG
Lightweight Java PNG decoder

**Usage example:**
```
D:\lwjPNG>javac lwjPNG/Test.java

D:\lwjPNG>java lwjPNG/Test
reading image.png:
 250x250px;
 image size power of 2: false;
  => downscalling: 128x128px;
 Bit depth: 32bit;
 interlace: 1;
lwjPNG: done in 0.02531405s
ImageIO read in 0.10986439s
```
**lwjPNG v0.01**
* Support for 24 & 32 bit PNG, interlaced or not
* Rescalle, see function ByteBuffer scale(int, int)
* For more details, see Test.java
* GNU General Public License v2.0
