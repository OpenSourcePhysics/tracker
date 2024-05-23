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

#ifndef PIXELFORMAT_H_
#define PIXELFORMAT_H_
#include <com/xuggle/xuggler/Xuggler.h>
#include <com/xuggle/xuggler/IPixelFormat.h>

namespace com { namespace xuggle { namespace xuggler
{
  class PixelFormat : public IPixelFormat {
  public:
#ifndef SWIG
    /*
     * Inline version of prior function that does the same thing, but has no error
     * checking.
     * 
     * This code is very tricky; it relies on /2 shaving off odd digits.
     * 
     */
    static int getFastYUV420PPixelOffset(int width, int height,
        int x, int y,
        IPixelFormat::YUVColorComponent c)
    {
      int retval = 0;
      switch(c)
      {
        case IPixelFormat::YUV_Y:
        {
          retval = y*width+x;
        }
        break;
        case IPixelFormat::YUV_U:
        {
          int area = width*height;
          int w2 = (width+1)>>1;
          retval = (y>>1)*w2+(x>>1)+area;
        }
        break;
        case IPixelFormat::YUV_V:
        {
          int area = width*height;
          int w2 = (width+1)>>1;
          int h2 = (height+1)>>1;
          int area2 = w2*h2;
          retval = (y>>1)*w2+(x>>1)+area+area2;
        }
        break;
      }
      return retval;
    }
#endif
  };
}}}
#endif // ! PIXELFORMAT_H_

