/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Xuggler-Main.
 *
 * Xuggle-Xuggler-Main is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Main is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Main.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

%module Xuggler
%{

// Xuggler.i: Start generated code
// >>>>>>>>>>>>>>>>>>>>>>>>>>>
#include <com/xuggle/ferry/JNIHelper.h>
#include <com/xuggle/xuggler/IProperty.h>
#include <com/xuggle/xuggler/IPixelFormat.h>
#include <com/xuggle/xuggler/ITimeValue.h>
#include <com/xuggle/xuggler/IRational.h>
#include <com/xuggle/xuggler/IMetaData.h>
#include <com/xuggle/xuggler/IMediaData.h>
#include <com/xuggle/xuggler/IAudioSamples.h>
#include <com/xuggle/xuggler/ICodec.h>
#include <com/xuggle/xuggler/IPacket.h>
#include <com/xuggle/xuggler/IIndexEntry.h>
#include <com/xuggle/xuggler/IAudioResampler.h>
#include <com/xuggle/xuggler/IVideoPicture.h>
#include <com/xuggle/xuggler/IVideoResampler.h>
#include <com/xuggle/xuggler/IStreamCoder.h>
#include <com/xuggle/xuggler/IStream.h>
#include <com/xuggle/xuggler/IContainerParameters.h>
#include <com/xuggle/xuggler/IContainerFormat.h>
#include <com/xuggle/xuggler/IContainer.h>
#include <com/xuggle/xuggler/IMediaDataWrapper.h>
#include <com/xuggle/xuggler/Global.h>
#include <com/xuggle/xuggler/IError.h>

using namespace VS_CPP_NAMESPACE;
/**
 * Here to maintain BW-compatibility with Version 3.x of Xuggler;
 * can be removed when major version goes pass 3.
 */
extern "C" {
SWIGEXPORT jint JNICALL Java_com_xuggle_xuggler_XugglerJNI_IContainer_1seekKeyFrame_1_1SWIG_10(JNIEnv *jenv, jclass jcls, jlong jarg1, jobject jarg1_, jint jarg2, jlong jarg3, jint jarg4);
SWIGEXPORT jint JNICALL Java_com_xuggle_xuggler_XugglerJNI_IContainer_1seekKeyFrame(JNIEnv *jenv, jclass jcls, jlong jarg1, jobject jarg1_, jint jarg2, jlong jarg3, jint jarg4)
{
  return Java_com_xuggle_xuggler_XugglerJNI_IContainer_1seekKeyFrame_1_1SWIG_10(jenv, jcls, jarg1, jarg1_, jarg2, jarg3, jarg4);
}
}


extern "C" {
/*
 * This will be called if an when we're loaded
 * directly by Java.  If we're linked to via
 * C/C++ linkage on another library, they
 * must call sSetVM().
 */
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *, void *)
{
  /* Because of static initialize in Mac OS, the only safe thing
   * to do here is return the version */
  return com::xuggle::ferry::JNIHelper::sGetJNIVersion();
}

JNIEXPORT void JNICALL
Java_com_xuggle_xuggler_Xuggler_init(JNIEnv *env, jclass)
{
  JavaVM* vm=0;
  if (!com::xuggle::ferry::JNIHelper::sGetVM()) {
    env->GetJavaVM(&vm);
    com::xuggle::ferry::JNIHelper::sSetVM(vm);
  }
}

}


// <<<<<<<<<<<<<<<<<<<<<<<<<<<
// Xuggler.i: End generated code

%}

%pragma(java) jniclassimports=%{
import com.xuggle.ferry.*;
%}

%pragma(java) moduleimports=%{
/**
 * Internal Only.
 * 
 * @see com.xuggle.xuggler.Converter
 */
%}

%pragma(java) modulecode=%{
  static {
    // Force the JNI library to load
    XugglerJNI.noop();
  }

  /**
   * 
   * A simple test of xuggler, this program takes an input
   * file, and outputs it as an output file.
   * 
   * @param args The command line args passed to this program.
   */
  public static void main(String[] args)
  {
    System.out.println("WARNING: The Converter main class has moved to: com.xuggle.xuggler.Converter");
  }
  /**
   * Internal Only.  Do not use.
   */
  public native static void init();

%}

%pragma(java) jniclasscode=%{
// Xuggler.i: Start generated code
// >>>>>>>>>>>>>>>>>>>>>>>>>>>

  static {
    com.xuggle.ferry.JNILibraryLoader.loadLibrary("xuggle-xuggler",
      new Long(com.xuggle.xuggler.Version.MAJOR_VERSION));
    com.xuggle.xuggler.Xuggler.init();
    com.xuggle.xuggler.Global.init();
  }
  public static void noop() {
    // Here only to force JNI library to load
  }
  
// <<<<<<<<<<<<<<<<<<<<<<<<<<<
// Xuggler.i: End generated code
  
%}
// As per 1.17, we now make sure we generate proper Java enums on
// classes
%include "enums.swg"

%import <com/xuggle/ferry/Ferry.i>

%include <com/xuggle/xuggler/Xuggler.h>
%include <com/xuggle/xuggler/IProperty.h>
%include <com/xuggle/xuggler/IPixelFormat.h>
%include <com/xuggle/xuggler/IRational.swg>
%include <com/xuggle/xuggler/ITimeValue.h>
%include <com/xuggle/xuggler/IMetaData.swg>
%include <com/xuggle/xuggler/IMediaData.swg>
%include <com/xuggle/xuggler/IPacket.swg>
%include <com/xuggle/xuggler/IAudioSamples.swg>
%include <com/xuggle/xuggler/IVideoPicture.swg>
%include <com/xuggle/xuggler/ICodec.swg>
%include <com/xuggle/xuggler/IAudioResampler.h>
%include <com/xuggle/xuggler/IVideoResampler.swg>
%include <com/xuggle/xuggler/IStreamCoder.swg>
%include <com/xuggle/xuggler/IIndexEntry.swg>
%include <com/xuggle/xuggler/IStream.swg>
%include <com/xuggle/xuggler/IContainerParameters.h>
%include <com/xuggle/xuggler/IContainerFormat.swg>
%include <com/xuggle/xuggler/IContainer.swg>
%include <com/xuggle/xuggler/IMediaDataWrapper.swg>
%include <com/xuggle/xuggler/Global.swg>
%include <com/xuggle/xuggler/IError.swg>
