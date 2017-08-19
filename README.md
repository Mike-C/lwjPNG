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
**Release notes**

**lwjPNG v0.02**
* Partial image read (image info).
Now you can read whole image, by:
```java
 lwjPNG.LwjPNG.init(inputStream, true);
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; And than decode it, or you can call:
```java
 // read image information
 lwjPNG.LwjPNG.init(inputStream, false);
 
 // now you can get it's dimension
 int w = lwjPNG.LwjPNG.getWidth(), h = lwjPNG.LwjPNG.getHeight();
 
 // than read rest of the data
 lwjPNG.LwjPNG.init(inputStream, true);
 
 // and decode, scale
 ByteBuffer buffer = lwjPNG.LwjPNG.decode();
```
* Minor speed improvement
* Bug fix for ByteBuffer position not set in interlace mode

**lwjPNG v0.01**
* Support for 24 & 32 bit PNG, interlaced or not
* Rescalle, see function ByteBuffer scale(int, int)
* For more details, see Test.java
* GNU General Public License v2.0
