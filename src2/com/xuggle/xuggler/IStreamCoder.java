package com.xuggle.xuggler;

import com.xuggle.xuggler.IPixelFormat.Type;

public class IStreamCoder {

	public void setNumPicturesInGroupOfPictures(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setPixelType(Type pixelType) {
		// TODO Auto-generated method stub
		
	}

	public void setBitRate(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setCodec(ICodec codec) {
		// TODO Auto-generated method stub
		
	}

	public void setHeight(int height) {
		// TODO Auto-generated method stub
		
	}

	public void setWidth(int width) {
		// TODO Auto-generated method stub
		
	}

	public void setFrameRate(IRational frameRate) {
		// TODO Auto-generated method stub
		
	}

	public void setTimeBase(IRational make) {
		// TODO Auto-generated method stub
		
	}

	public int open() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int encodeVideo(IPacket packet, IVideoPicture picture, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public Object getHeight() {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete() {
		// TODO Auto-generated method stub
		
	}

	public int decodeVideo(IVideoPicture tempPicture, IPacket tempPacket, int offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getWidth() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getPixelType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCodecType() {
		// TODO Auto-generated method stub
		return null;
	}

}
