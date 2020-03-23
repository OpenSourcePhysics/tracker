package com.xuggle.xuggler.video;

import java.awt.image.BufferedImage;

import com.xuggle.xuggler.IPixelFormat.Type;
import com.xuggle.xuggler.IVideoPicture;

public class ConverterFactory {

	public class Type {

		public BufferedImage getDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static final String XUGGLER_BGR_24 = null;

	public static IConverter createConverter(BufferedImage bgrImage, Type pixelType) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Type findRegisteredConverter(String xugglerBgr24) {
		// TODO Auto-generated method stub
		return null;
	}

	public static IConverter createConverter(BufferedImage descriptor, IVideoPicture picture) {
		// TODO Auto-generated method stub
		return null;
	}

	public static IConverter createConverter(BufferedImage bgrImage, com.xuggle.xuggler.IPixelFormat.Type pixelType) {
		// TODO Auto-generated method stub
		return null;
	}

}
