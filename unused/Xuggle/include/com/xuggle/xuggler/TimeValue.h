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

/*
 * TimeValue.h
 *
 *  Created on: Sep 19, 2008
 *      Author: aclarke
 */

#ifndef TIMEVALUE_H_
#define TIMEVALUE_H_

#include <climits>
#include <com/xuggle/xuggler/ITimeValue.h>

namespace com { namespace xuggle { namespace xuggler {

class TimeValue: public ITimeValue
{
  VS_JNIUTILS_REFCOUNTED_OBJECT_PRIVATE_MAKE(TimeValue);
public:
  virtual int64_t get(Unit unit);
  virtual int32_t compareTo(ITimeValue* other);

  Unit getNativeUnit();

  static TimeValue* make(int64_t value, Unit unit);
  static TimeValue* make(TimeValue *src);
  static inline int32_t compare(int64_t thisValue, int64_t thatValue)
  {
    int64_t retval;
    int64_t adjustment = 1;
    const static int64_t sMaxDistance = LLONG_MAX/2;
    if ((thisValue > sMaxDistance && thatValue <= -sMaxDistance) ||
        (thatValue > sMaxDistance && thisValue <= -sMaxDistance))
      // in these  cases we assume the time value has looped over
      // a int64_t value, and we really want to invert the diff.
      adjustment = -1;

    if (thisValue < thatValue)
      retval = -adjustment;
    else if (thisValue > thatValue)
      retval = adjustment;
    else
      retval = 0;
    return retval;
  }

protected:
  TimeValue();
  virtual ~TimeValue();
private:
  void set(int64_t value, Unit unit);
  int64_t mValue;
  Unit mUnit;
};

}}}

#endif /* TIMEVALUE_H_ */
