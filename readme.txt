24 & 32 bit Lightweight Java PNG decoder to RGBA; (c) 2017 rdavidian71@gmail.com v0.05

You are using this software at your own risk. Author does not bear any responsibility for potential damage concerning it's use.

Version notes:

lwjPNG v0.05
* class LwjPNG is now instantiated, for better OOP

lwjPNG v0.04
* Bug fix for imgData not nulled in scale(..)
* Minor optimization

lwjPNG v0.03
* Added methods to get and set ARGB color as int at position (x, y)

lwjPNG v0.02
* Partial image read (image info)
* Minor speed improvement
* Bug fix for ByteBuffer position not set in interlace mode

lwjPNG v0.01
* Initial release
* Support for 24 & 32 bit PNG, interlaced or not
* Rescale, see function ByteBuffer scale(int, int)
* For usage example, see Test.java
* GNU General Public License v2.0
