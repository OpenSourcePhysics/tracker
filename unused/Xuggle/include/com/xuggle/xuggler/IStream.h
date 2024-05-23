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

#ifndef ISTREAM_H_
#define ISTREAM_H_
#include <com/xuggle/ferry/RefCounted.h>
#include <com/xuggle/xuggler/Xuggler.h>
namespace com { namespace xuggle { namespace xuggler
{
  class IStreamCoder;
  class IContainer;
  class IRational;
  class IMetaData;
  class IPacket;
  class IIndexEntry;
  
  /**
   * Represents a stream of similar data (eg video) in a {@link IContainer}.
   * <p>
   * Streams are really virtual concepts; {@link IContainer}s really just contain
   * a bunch of {@link IPacket}s.  But each {@link IPacket} usually has a stream
   * id associated with it, and all {@link IPacket}s with that stream id represent
   * the same type of (usually time-based) data.  For example in many FLV
   * video files, there is a stream with id "0" that contains all video data, and
   * a stream with id "1" that contains all audio data.
   * </p><p>
   * You use an {@link IStream} object to get properly configured {@link IStreamCoder}
   * for decoding, and to tell {@link IStreamCoder}s how to encode {@link IPacket}s when
   * decoding.
   * </p>
   */
  class VS_API_XUGGLER IStream : public com::xuggle::ferry::RefCounted
  {
  public:
    /**
     * The direction this stream is going (based on the container).
     *
     * If the container Container is opened in Container::READ mode
     * then this will be INBOUND.  If it's opened in Container::WRITE
     * mode, then this will be OUTBOUND.
     */
    typedef enum Direction {
      INBOUND,
      OUTBOUND,
    } Direction;
    
    /**
     * Get the {@link Direction} this stream is pointing in.
     * @return The direction of this stream.
     */
    virtual Direction getDirection()=0;
    /**
     * Get the relative position this stream has in the hosting
     * {@link IContainer} object.
     * @return The Index within the Container of this stream.
     */
    virtual int getIndex()=0;

    /**
     * Return a container format specific id for this stream.
     * @return The (container format specific) id of this stream.
     */
    virtual int getId()=0;

    /**
     * Get the StreamCoder than can manipulate this stream.
     * If the stream is an INBOUND stream, then the StreamCoder can
     * do a IStreamCoder::DECODE.  IF this stream is an OUTBOUND stream,
     * then the StreamCoder can do all IStreamCoder::ENCODE methods.
     *
     * @return The StreamCoder assigned to this object.
     */
    virtual IStreamCoder * getStreamCoder()=0;

    /**
     * Get the (sometimes estimated) frame rate of this container.
     * For variable frame-rate containers (they do exist) this is just
     * an approimation.  Better to use getTimeBase().
     *
     * For contant frame-rate containers, this will be 1 / ( getTimeBase() )
     *
     * @return The frame-rate of this container.
     */
    virtual IRational * getFrameRate()=0;

    /**
     * The time base in which all timestamps (e.g. Presentation Time Stamp (PTS)
     * and Decompression Time Stamp (DTS)) are represented.  For example
     * if the time base is 1/1000, then the difference between a PTS of 1 and
     * a PTS of 2 is 1 millisecond.  If the timebase is 1/1, then the difference
     * between a PTS of 1 and a PTS of 2 is 1 second.
     *
     * @return The time base of this stream.
     */
    virtual IRational * getTimeBase()=0;

    /**
     * Return the start time, in {@link #getTimeBase()} units, when this stream
     * started.
     * @return The start time.
     */
    virtual int64_t getStartTime()=0;

    /**
     * Return the duration, in {@link #getTimeBase()} units, of this stream,
     * or {@link Global#NO_PTS} if unknown.
     * @return The duration (in getTimeBase units) of this stream, if known.
     */
    virtual int64_t getDuration()=0;

    /**
     * The current Decompression Time Stamp that will be used on this stream,
     * in {@link #getTimeBase()} units.
     * @return The current Decompression Time Stamp that will be used on this stream.
     */
    virtual int64_t getCurrentDts()=0;

    /**
     * Get the number of index entries in this stream.
     * @return The number of index entries in this stream.
     * @see #getIndexEntry(int)
     */
    virtual int getNumIndexEntries()=0;

    /**
     * Returns the number of encoded frames if known.  Note that frames here means
     * encoded frames, which can consist of many encoded audio samples, or
     * an encoded video frame.
     *
     * @return The number of frames (encoded) in this stream.
     */
    virtual int64_t getNumFrames()=0;
    
  protected:
    virtual ~IStream()=0;
    IStream();
  /** Added in 1.17 */
  public:

    /**
     * Gets the sample aspect ratio.
     *
     * @return The sample aspect ratio.
     */
    virtual IRational* getSampleAspectRatio()=0;
    /**
     * Sets the sample aspect ratio.
     *
     * @param newRatio The new ratio.
     */
    virtual void setSampleAspectRatio(IRational* newRatio)=0;

    /**
     * Get the 4-character language setting for this stream.
     *
     * This will return null if no setting.  When calling
     * from C++, callers must ensure that the IStream outlives the
     * value returned.
     */
    virtual const char* getLanguage()=0;

    /**
     * Set the 4-character language setting for this stream.
     *
     * If a string longer than 4 characters is passed in, only the
     * first 4 characters is copied.
     *
     * @param language The new language setting.  null is equivalent to the
     *   empty string.  strings longer than 4 characters will be truncated
     *   to first 4 characters.
     */
    virtual void setLanguage(const char* language)=0;
    
    /**
     * Get the underlying container for this stream, or null if Xuggler
     * doesn't know.
     * 
     * @return the container, or null if we don't know.
     */
    virtual IContainer* getContainer()=0;
    
    /*
     * Added for 1.22
     */
    
    /**
     * Sets the stream coder to use for this stream.
     * 
     * This method will only cause a change if the IStreamCoder currently set on this
     * IStream is not open.  Otherwise the call is ignore and an error is returned.
     * 
     * @param newCoder The new stream coder to use.
     * @return >= 0 on success; < 0 on error.
     */
    virtual int32_t setStreamCoder(IStreamCoder *newCoder)=0;
    
    /*
     * Added for 3.0
     */
    
    /**
     * What types of parsing can we do on a call to
     * {@link IContainer#readNextPacket(IPacket)}
     */
    typedef enum ParseType {
      PARSE_NONE,
      PARSE_FULL,
      PARSE_HEADERS,
      PARSE_TIMESTAMPS,       
    } ParseType;
    
    /**
     * Get how the decoding codec should parse data from this stream.
     * @return the parse type.
     * @since 3.0
     */
    virtual IStream::ParseType getParseType()=0;
    
    /**
     * Set the parse type the decoding codec should use.  Set to
     * {@link ParseType#PARSE_NONE} if you don't want any parsing
     * to be done.
     * <p>
     * Warning: do not set this flag unless you know what you're doing,
     * and do not set after you've started decoding.
     * </p>
     * 
     * @param type The type to set.
     * @since 3.0
     */
    virtual void setParseType(ParseType type)=0;

    /*
     * Added for 3.1
     */
    
    /**
     * Get the {@link IMetaData} for this object,
     * or null if none.
     * <p>
     * If the {@link IContainer} or {@link IStream} object
     * that this {@link IMetaData} came from was opened
     * for reading, then changes via {@link IMetaData#setValue(String, String)}
     * will have no effect on the underlying media.
     * </p>
     * <p>
     * If the {@link IContainer} or {@link IStream} object
     * that this {@link IMetaData} came from was opened
     * for writing, then changes via {@link IMetaData#setValue(String, String)}
     * will have no effect after {@link IContainer#writeHeader()}
     * is called.
     * </p>
     * @return the {@link IMetaData}.
     * @since 3.1
     */
   virtual IMetaData* getMetaData()=0;
    
   /**
    * Set the {@link IMetaData} on this object, overriding
    * any previous meta data.  You should call this
    * method on writable containers and
    * before you call {@link IContainer#writeHeader}, as
    * it probably won't do anything after that.
    * 
    * @see #getMetaData()
    * @since 3.1
    */
   virtual void setMetaData(IMetaData* data)=0;

   /*
    * Added for 3.2
    */
   
   /**
    * Takes a packet destined for this stream, and stamps
    * the stream index, and converts the time stamp to the
    * correct units (adjusting for rounding errors between
    * stream conversions).
    * 
    * @param packet to stamp
    * @return >= 0 on success; <0 on failure.
    * @since 3.2
    */
   virtual int32_t stampOutputPacket(IPacket* packet)=0;
    

   /**
    * Sets the stream coder to use for this stream.
    * 
    * This method will only cause a change if the IStreamCoder currently set on this
    * IStream is not open.  Otherwise the call is ignored and an error is returned.
    * 
    * @param newCoder The new stream coder to use.
    * @param assumeOnlyStream If true then this {@link IStream} will notify the {@link IStreamCoder} that it is the only stream and the {@link IStreamCoder} may use it to determine time stamps to output packets with.
    *   If false then the {@link IStreamCoder}
    *   does not support automatic stamping of packets with stream index IDs
    *   and users must call {@link #stampOutputPacket(IPacket)} themselves.
    * @return >= 0 on success; < 0 on error.
    * @since 3.2
    */
   virtual int32_t setStreamCoder(IStreamCoder *newCoder, bool assumeOnlyStream)=0;

   /*
    * Added for 3.4
    */
   /**
    * Search for the given time stamp in the key-frame index for this {@link IStream}.
    * <p>
    * Not all {@link IContainerFormat} implementations
    * maintain key frame indexes, but if they have one,
    * then this method searches in the {@link IStream} index
    * to quickly find the byte-offset of the nearest key-frame to
    * the given time stamp.
    * </p>
    * @param wantedTimeStamp the time stamp wanted, in the stream's
    *                        time base units.
    * @param flags A bitmask of the <code>SEEK_FLAG_*</code> flags, or 0 to turn
    *              all flags off.  If {@link IContainer#SEEK_FLAG_BACKWARDS} then the returned
    *              index will correspond to the time stamp which is <=
    *              the requested one (not supported by all demuxers).
    *              If {@link IContainer#SEEK_FLAG_BACKWARDS} is not set then it will be >=.
    *              if {@link IContainer#SEEK_FLAG_ANY} seek to any frame, only
    *              keyframes otherwise (not supported by all demuxers).
    * @return The {@link IIndexEntry} for the nearest appropriate timestamp
    *   in the index, or null if it can't be found.
    * @since 3.4
    */
   virtual IIndexEntry* findTimeStampEntryInIndex(
       int64_t wantedTimeStamp, int32_t flags)=0;

   /**
    * Search for the given time stamp in the key-frame index for this {@link IStream}.
    * <p>
    * Not all {@link IContainerFormat} implementations
    * maintain key frame indexes, but if they have one,
    * then this method searches in the {@link IStream} index
    * to quickly find the index entry position of the nearest key-frame to
    * the given time stamp.
    * </p>
    * @param wantedTimeStamp the time stamp wanted, in the stream's
    *                        time base units.
    * @param flags A bitmask of the <code>SEEK_FLAG_*</code> flags, or 0 to turn
    *              all flags off.  If {@link IContainer#SEEK_FLAG_BACKWARDS} then the returned
    *              index will correspond to the time stamp which is <=
    *              the requested one (not supported by all demuxers).
    *              If {@link IContainer#SEEK_FLAG_BACKWARDS} is not set then it will be >=.
    *              if {@link IContainer#SEEK_FLAG_ANY} seek to any frame, only
    *              keyframes otherwise (not supported by all demuxers).
    * @return The position in this {@link IStream} index, or -1 if it cannot
    *   be found or an index is not maintained.
    * @see #getIndexEntry(int)
    * @since 3.4
    */
   virtual int32_t findTimeStampPositionInIndex(
       int64_t wantedTimeStamp, int32_t flags)=0;

   /**
    * Get the {@link IIndexEntry} at the given position in this
    * {@link IStream} object's index.
    * <p>
    * Not all {@link IContainerFormat} types maintain
    * {@link IStream} indexes, but if they do,
    * this method can return those entries.
    * </p>
    * <p>
    * Do not modify the {@link IContainer} this stream
    * is from between calls to this method and
    * {@link #getNumIndexEntries()} as indexes may
    * be compacted while processing.
    * </p>
    * @param position The position in the index table.
    * @since 3.4
    */
   virtual IIndexEntry* getIndexEntry(int32_t position)=0;

   /**
    * Adds an index entry into the stream's sorted index list.
    * Updates the entry if the list
    * already contains it.
    *
    * @param entry The entry to add.
    * @return >=0 on success; <0 on error.
    * @since 3.4
    */
   virtual int32_t addIndexEntry(IIndexEntry* entry)=0;
  };
}}}

#endif /*ISTREAM_H_*/
