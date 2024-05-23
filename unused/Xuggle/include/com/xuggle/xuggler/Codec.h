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

#ifndef CODEC_H_
#define CODEC_H_

#include <com/xuggle/xuggler/ICodec.h>
#include <com/xuggle/xuggler/IContainerFormat.h>
#include <com/xuggle/xuggler/FfmpegIncludes.h>

namespace com { namespace xuggle { namespace xuggler
{

  class Codec : public ICodec
  {
    VS_JNIUTILS_REFCOUNTED_OBJECT_PRIVATE_MAKE(Codec)
  public:
    static Codec * make(AVCodec *);
    virtual const char * getName();
    virtual int getIDAsInt();
    virtual Type getType();
    virtual ID getID()
    {
      // Warning; this might not be protable
      ID retval = CODEC_ID_NONE;
      int id = getIDAsInt();

      // This cast is not defined in C++ when id is
      // not in ID.  That means if we're using a
      // newer version of LIBAVCODEC this might
      // give a bogus value for a Codec we don't
      // know about.
      retval = (ID)id;
      if (id != (int) retval)
      {
        // we assume some back and forth conversion failed...
        retval = CODEC_ID_NONE;
      }
      return retval;
    }

    // For calling from with C++, not Java.
    AVCodec * getAVCodec() { return mCodec; }

    static Codec *findEncodingCodec(const ICodec::ID);
    static Codec *findEncodingCodecByIntID(const int);
    static Codec *findEncodingCodecByName(const char*);

    static Codec *findDecodingCodec(const ICodec::ID);
    static Codec *findDecodingCodecByIntID(const int);
    static Codec *findDecodingCodecByName(const char*);

    static Codec *guessEncodingCodec(IContainerFormat* fmt,
        const char *shortName, const char*url, const char*mime_type,
        ICodec::Type type);

    virtual bool canDecode();
    virtual bool canEncode();
    virtual const char * getLongName();
    
    virtual int32_t getCapabilities();
    virtual bool hasCapability(Capabilities flag);
    
    virtual int32_t getNumSupportedVideoFrameRates();
    virtual IRational* getSupportedVideoFrameRate(int32_t index);
    
    virtual int32_t getNumSupportedVideoPixelFormats();
    virtual IPixelFormat::Type getSupportedVideoPixelFormat(int32_t index);
    
    virtual int32_t getNumSupportedAudioSampleRates();
    virtual int32_t getSupportedAudioSampleRate(int32_t index);
    
    virtual int32_t getNumSupportedAudioSampleFormats();
    virtual IAudioSamples::Format getSupportedAudioSampleFormat(int32_t index);
    
    virtual int32_t getNumSupportedAudioChannelLayouts();
    virtual int64_t getSupportedAudioChannelLayout(int32_t index);
    


    virtual int32_t acquire();
    virtual int32_t release();
  public:
    Codec();
    virtual ~Codec();

    AVCodec *mCodec;
  };

}}}

#endif /*CODEC_H_*/
