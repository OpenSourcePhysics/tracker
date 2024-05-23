package com.xuggle.xuggler;

import java.io.RandomAccessFile;

public class IContainer {
	
	public enum Type {
		WRITE, READ;
	}

	public static final String SEEK_FLAG_BACKWARDS = null;

	public int getNumStreams() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IStream getStream(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public static IContainer make() {
		// TODO Auto-generated method stub
		return null;
	}

	public IStream addNewStream(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public int writeTrailer() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int open(String absolutePath, Type write, IContainerFormat format) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int writeHeader() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int writePacket(IPacket packet, boolean forceInterleave) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void delete() {
		// TODO Auto-generated method stub
		
	}

	public int readNextPacket(IPacket tempPacket) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int open(RandomAccessFile raf, Type read, Object format) {
		return 0;
	}

	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public int seekKeyFrame(int streamIndex, long timestamp, long timestamp2, long timestamp3, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int seekKeyFrame(int streamIndex, long timestamp, long timestamp2, long timestamp3,
			String seekFlagBackwards) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}