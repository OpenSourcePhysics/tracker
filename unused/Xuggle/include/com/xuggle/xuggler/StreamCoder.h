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

#ifndef STREAMCODER_H_
#define STREAMCODER_H_

#include <com/xuggle/ferry/RefPointer.h>
#include <com/xuggle/xuggler/IStreamCoder.h>
#include <com/xuggle/xuggler/FfmpegIncludes.h>
#include <com/xuggle/xuggler/Stream.h>
#include <com/xuggle/ferry/IBuffer.h>

namespace com { namespace xuggle { namespace xuggler
{

  class Codec;
  class Packet;

  class StreamCoder : public IStreamCoder
  {
    VS_JNIUTILS_REFCOUNTED_OBJECT_PRIVATE_MAKE(StreamCoder)
  public:
    // IStreamCoder Interface implementation

    /*
     * This section has all the Getters and Setters
     */
    virtual Direction getDirection() { return mDirection; }

    virtual IStream* getStream();

    virtual ICodec* getCodec();
    virtual ICodec::Type getCodecType();
    virtual ICodec::ID getCodecID();

    virtual void setCodec(ICodec *);
    virtual void setCodec(ICodec::ID);
    virtual void setCodecID(ICodec::ID id) { setCodec(id); }

    virtual int32_t getBitRate();
    virtual void setBitRate(int32_t rate);
    virtual int32_t getBitRateTolerance();
    virtual void setBitRateTolerance(int32_t tolerance);

    // These are Video codec getters and setters
    virtual int32_t getHeight();
    virtual void setHeight(int32_t);

    virtual int32_t getWidth();
    virtual void setWidth(int32_t);

    // NOTE: Caller must release() return value when done.
    virtual IRational* getTimeBase();
    virtual void setTimeBase(IRational* newTimeBase);

    // These just forward to the stream we're coding
    virtual IRational* getFrameRate();
    virtual void setFrameRate(IRational *newFrameRate);

    virtual int32_t getNumPicturesInGroupOfPictures();
    virtual void setNumPicturesInGroupOfPictures(int32_t gops);

    virtual IPixelFormat::Type getPixelType();
    virtual void setPixelType(IPixelFormat::Type pixelFmt);

    // These are Audio codec getters and setters
    virtual int32_t getSampleRate();
    virtual void setSampleRate(int32_t sampleRate);

    virtual int32_t getChannels();
    virtual void setChannels(int32_t channels);

    virtual IAudioSamples::Format getSampleFormat();
    virtual void setSampleFormat(IAudioSamples::Format aFormat);

    virtual int32_t getGlobalQuality();
    virtual void setGlobalQuality(int32_t newQuality);

    virtual int32_t getFlags();
    virtual void setFlags(int32_t newFlags);
    virtual bool getFlag(Flags flag);
    virtual void setFlag(Flags flag, bool value);
    virtual int32_t getAudioFrameSize();

    /*
     * This section has the operational aspects of
     * a StreamCoder
     */
    virtual int32_t open();
    virtual int32_t close();
    virtual int32_t decodeAudio(IAudioSamples * pOutSamples,
        IPacket *packet, int32_t byteOffset);
    virtual int32_t decodeVideo(IVideoPicture * pOutFrame,
        IPacket *packet, int32_t byteOffset);
    virtual int32_t encodeVideo(IPacket *pOutPacket,
        IVideoPicture * pFrame, int32_t suggestedBufferSize);
    virtual int32_t encodeAudio(IPacket *pOutPacket,
        IAudioSamples* pSamples, uint32_t sampleToStartFrom);

    virtual int64_t getNextPredictedPts();

    virtual int32_t getCodecTag();
    virtual void setCodecTag(int32_t);

    virtual int32_t getNumProperties();
    virtual IProperty* getPropertyMetaData(int32_t propertyNo);
    virtual IProperty* getPropertyMetaData(const char *name);

    virtual int32_t setProperty(const char* name, const char* value);
    virtual int32_t setProperty(const char* name, double value);
    virtual int32_t setProperty(const char* name, int64_t value);
    virtual int32_t setProperty(const char* name, bool value);
    virtual int32_t setProperty(const char* name, IRational *value);
    
    virtual char * getPropertyAsString(const char* name);
    virtual double getPropertyAsDouble(const char* name);
    virtual int64_t getPropertyAsLong(const char* name);
    virtual  IRational *getPropertyAsRational(const char* name);
    virtual bool getPropertyAsBoolean(const char* name);

    virtual bool isOpen();

    virtual int32_t getDefaultAudioFrameSize();
    virtual void setDefaultAudioFrameSize(int32_t);
    // Not for calling from Java
    void setCodec(int32_t);

    /**
     * This method creates a StreamCoder that is not tied to any
     * container or stream.
     */
    static StreamCoder* make(Direction direction);

    /**
     * This method creates a StreamCoder that is tied to a specific
     * stream in a container.
     */
    static StreamCoder* make(Direction direction,
        AVCodecContext *context, Stream* stream);
    static StreamCoder* make(Direction direction, IStreamCoder* copyCoder);
    
    int32_t setStream(Stream*, bool assumeOnlyStream);
    int32_t streamClosed(Stream*);

    virtual int64_t getNumDroppedFrames();
    virtual void setAutomaticallyStampPacketsForStream(bool value);
    virtual bool getAutomaticallyStampPacketsForStream();

    // RefCounted interface
    virtual int32_t acquire();
    virtual int32_t release();

    virtual int32_t setExtraData(com::xuggle::ferry::IBuffer* src, int32_t offset, int32_t length, bool allocNew);
    virtual int32_t getExtraData(com::xuggle::ferry::IBuffer *dest, int32_t offset, int32_t maxBytesToCopy);
    virtual int32_t getExtraDataSize();

  protected:
    StreamCoder();
    virtual ~StreamCoder();
  private:
    Direction mDirection;
    AVCodecContext* mCodecContext;
    Stream* mStream; // Must not refcount this.
    com::xuggle::ferry::RefPointer<Codec> mCodec;
    bool mOpened;

    // Variables used for patching up PTS values
    com::xuggle::ferry::RefPointer<IRational> mFakePtsTimeBase;
    int64_t mLastPtsEncoded;
    int64_t mFakeNextPts;
    int64_t mFakeCurrPts;
    int64_t mSamplesForEncoding;
    int64_t mSamplesCoded;
    int64_t mLastExternallySetTimeStamp;

    com::xuggle::ferry::RefPointer<com::xuggle::ferry::IBuffer> mAudioFrameBuffer;
    int32_t mBytesInFrameBuffer;
    int64_t mStartingTimestampOfBytesInFrameBuffer;
    int32_t mDefaultAudioFrameSize;
    int64_t mNumDroppedFrames;
    bool mAutomaticallyStampPacketsForStream;
    int64_t mPtsBuffer[MAX_REORDER_DELAY+1];
    
    int32_t calcAudioFrameSize();

    void setCodecInternal(int32_t id);

    void reset();
    void setPacketParameters(Packet *packet, int32_t size,
        int64_t dts,
        IRational * timebase,
        bool keyframe,
        int64_t duration);
  };

}}}

#endif /*STREAMCODER_H_*/
