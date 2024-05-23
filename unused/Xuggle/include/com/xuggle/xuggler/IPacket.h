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

#ifndef IPACKET_H_
#define IPACKET_H_

#include <com/xuggle/xuggler/Xuggler.h>
#include <com/xuggle/xuggler/IMediaData.h>
namespace com { namespace xuggle { namespace xuggler
{

  /**
   * Represents an encoded piece of data that can be placed in an {@link IContainer}
   * for a given {@link IStream} of data.
   * <p>
   * You read this object out of {@link IContainer} objects when decoding, and
   * pass to an {@link IStreamCoder} object to decode.
   * </p><p>
   * You pass this object to a {@link IStreamCoder} to encode data, and then
   * pass to an {@link IContainer} object to write to a data source.
   * </p><p>
   * Lastly, the units of timestamps in an {@link IPacket} are determined by the
   * {@link IContainer} it came from (or is going to).  For example, FLV {@link IPacket}s
   * are always in milliseconds (1/1000 of a second).  You cannot assume these
   * timestamps are in any given timeunit without getting an {@link IStream} object
   * and finding out what Time Base that stream operates in.
   * </p><p>
   * For convenience, the Xuggler API always uses Microseconds for raw data
   * ({@link IVideoPicture} and {@link IAudioSamples} objects), and will convert to
   * the right time stamp unit when decoding or encoding data (with an {@link IStreamCoder})
   * from or to an {@link IContainer}. 
   */
  class VS_API_XUGGLER IPacket : public IMediaData
  {
  public:
    /**
     * Clear out any data in this packet, but leaves
     * the buffer available for reuse.
     */
    virtual void reset()=0;

    /**
     * Is this packet complete.
     * @return Is this packet full and therefore has valid information.
     */
    virtual bool isComplete()=0;

    /**
     * Get the Presentation Time Stamp (PTS) for this packet.
     * 
     * This is the time at which the payload for this packet should
     * be <strong>presented</strong> to the user, in units of
     * {@link #getTimeBase()}, relative to the start of stream.
     * 
     * @return Get the Presentation Timestamp for this packet.
     */
    virtual int64_t getPts()=0;
    
    /**
     * Set a new Presentation Time Stamp (PTS) for this packet.
     * 
     * @param aPts a new PTS for this packet.
     * 
     * @see #getPts()
     */
    virtual void setPts(int64_t aPts)=0;
    
    /**
     * Get the Decompression Time Stamp (DTS) for this packet.
     * <p>
     * This is the time at which the payload for this packet should
     * be <strong>decompressed</strong>, in units of
     * {@link #getTimeBase()}, relative to the start of stream.
     * </p>
     * <p>
     * Some media codecs can require packets from the &quot;future&quot; to
     * be decompressed before earliest packets as an additional way to compress
     * data.  In general you don't need to worry about this, but if you're
     * curious start reading about the difference between I-Frames, P-Frames
     * and B-Frames (or Bi-Directional Frames).  B-Frames can use information
     * from future frames when compressed.
     * </p>
     * @return Get the Decompression Timestamp (i.e. when this was read relative
     * to the start of reading packets).
     */
    virtual int64_t getDts()=0;

    /**
     * Set a new Decompression Time Stamp (DTS) for this packet.
     * @param aDts a new DTS for this packet.
     * @see #getDts()
     */
    virtual void setDts(int64_t aDts)=0;
    
    /**
     * Get the size in bytes of the payload currently in this packet.
     * @return Size (in bytes) of payload currently in packet.
     */
    virtual int32_t getSize()=0;
    
    /**
     * Get the maximum size (in bytes) of payload this packet can hold.
     * @return Get maximum size (in bytes) of payload this packet can hold.
     */
    virtual int32_t getMaxSize()=0;
    
    /**
     * Get the container-specific index for the stream this packet is
     * part of.
     * @return Stream in container that this packet has data for.
     */
    virtual int32_t getStreamIndex()=0;
    
    /**
     * Get any flags set on this packet, as a 4-byte binary-ORed bit-mask.
     * This is access to raw FFMPEG
     * flags, but it is easier to use the is* methods below.
     * @return Any flags on the packet.
     */
    virtual int32_t getFlags()=0;
    
    /**
     * Does this packet contain Key data? i.e. data that needs no other
     * frames or samples to decode.
     * @return true if key; false otherwise.
     */
    virtual bool isKeyPacket()=0;
    
    /**
     * Return the duration of this packet, in units of {@link #getTimeBase()}
     * @return Duration of this packet, in same time-base as the PTS.
     */
    virtual int64_t getDuration()=0;
    
    /**
     * Return the position (in bytes) of this packet in the stream.
     * @return The position of this packet in the stream, or -1 if
     *   unknown.
     */
    virtual int64_t getPosition()=0;
    
    /**
     * Discard the current payload and allocate a new payload.
     * <p>
     * Note that if any people have access to the old payload using
     * getData(), the memory will continue to be available to them
     * until they release their hold of the IBuffer.
     * </p>
     * <p>
     * When requesting a packet size, the system
     *   may allocate a larger payloadSize.
     * </p>
     * @param payloadSize The (minimum) payloadSize of this packet in bytes.
     * 
     * @return >= 0 if successful.  < 0 if error.
     */
    virtual int32_t allocateNewPayload(int32_t payloadSize)=0;

    /**
     * Allocate a new packet.
     * <p>
     * Note that any buffers this packet needs will be
     * lazily allocated (i.e. we won't actually grab all
     * the memory until we need it).
     * </p>
     * 
     * @return a new packet, or null on error.
     */
    static IPacket* make();
    
    /**
     * Allocate a new packet that wraps an existing IBuffer.
     * 
     * @param buffer The IBuffer to wrap.
     * @return a new packet or null on error.
     */
    static IPacket* make(com::xuggle::ferry::IBuffer* buffer);
    
  protected:
    IPacket();
    virtual ~IPacket();

  public:
    /*
     * Added for 1.19
     * 
     */

    /**
     * Set if this is a key packet.
     * 
     * @param keyPacket true for yes, false for no.
     */
    virtual void setKeyPacket(bool keyPacket)=0;
    
    /**
     * Set any internal flags.
     * 
     * @param flags Flags to set
     */
    virtual void setFlags(int32_t flags)=0;
    
    /**
     * Set if this packet is complete, and what the total size of the data should be assumed to be.
     * 
     * @param complete True for complete, false for not.
     * @param size Size of data in packet.
     */
    virtual void setComplete(bool complete, int32_t size)=0;
    
    /**
     * Set the stream index for this packet.
     * 
     * @param streamIndex The stream index, as determined from the {@link IContainer} this packet will be written to.
     */
    virtual void setStreamIndex(int32_t streamIndex)=0;

    /*
     * Added for 2.1
     */
    
    /**
     * Set the duration.
     * @param duration new duration
     * @see #getDuration()
     */
    virtual void setDuration(int64_t duration)=0;
    
    /**
     * Set the position.
     * @param position new position
     * @see #getPosition()
     */
    virtual void setPosition(int64_t position)=0;
    
    /**
     * Time difference in {@link IStream#getTimeBase()} units
     * from the presentation time stamp of this
     * packet to the point at which the output from the decoder has converged
     * independent from the availability of previous frames. That is, the
     * frames are virtually identical no matter if decoding started from
     * the very first frame or from this keyframe.
     * Is {@link Global#NO_PTS} if unknown.
     * This field is not the display duration of the current packet.
     * <p>
     * The purpose of this field is to allow seeking in streams that have no
     * keyframes in the conventional sense. It corresponds to the
     * recovery point SEI in H.264 and match_time_delta in NUT. It is also
     * essential for some types of subtitle streams to ensure that all
     * subtitles are correctly displayed after seeking.
     * </p>
     * <p>
     * If you didn't follow that, try drinking one to two glasses
     * of Absinthe.  It won't help, but it'll be more fun.
     * </p>
     * 
     * @return the convergence duration
     */
    virtual int64_t getConvergenceDuration()=0;
    
    /**
     * Set the convergence duration.
     * @param duration the new duration
     */
    virtual void setConvergenceDuration(int64_t duration)=0;
    
    /**
     * Allocate a new packet wrapping the existing contents of
     * a passed in packet.  Callers can then modify
     * {@link #getPts()},
     * {@link #getDts()} and other get/set methods without
     * modifying the original packet.
     * 
     * @param packet Packet to reuse buffer from and to
     *   copy settings from.
     * @param copyData if true copy data from packet
     *   into our own buffer.  If false, share the same
     *   data buffer that packet uses 
     *   
     * @return a new packet or null on error.
     */
    static IPacket* make(IPacket *packet, bool copyData);

    /**
     * Allocate a new packet.
     * <p>
     * Note that any buffers this packet needs will be
     * lazily allocated (i.e. we won't actually grab all
     * the memory until we need it).
     * </p>
     * @param size The maximum size, in bytes, of data you
     *   want to put in this packet.
     * 
     * @return a new packet, or null on error.
     */
    static IPacket* make(int32_t size);
    
 

  };

}}}

#endif /*IPACKET_H_*/
