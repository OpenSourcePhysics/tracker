/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;

/**
 * This is an ArrayList that represents a chain of vectors linked tip-to-tail.
 *
 * @author Douglas Brown
 */
public class VectorChain extends ArrayList<VectorStep> {

  /**
   * Constructs a chain.
   *
   * @param start the start vector
   */
  protected VectorChain(VectorStep start) {
    if (isAllowed(start)) {
      start.chain = this;
      super.add(start);
    }
  }

  /**
   * Constructs a chain.
   *
   * @param start the start vector
   * @param end the end vector
   */
  public VectorChain(VectorStep start, VectorStep end) {
    if (isAllowed(start) && isAllowed(end)) {
      start.chain = this;
      super.add(start);
      add(end);
    }
  }

  /**
   * Gets the end of the chain.
   *
   * @return the end vector
   */
  public VectorStep getEnd() {
    return get(size() - 1);
  }

  /**
   * Gets the start of the chain.
   *
   * @return the start vector
   */
  public VectorStep getStart() {
    return get(0);
  }

  /**
   * Removes the end of the chain.
   *
   * @return the removed end
   */
  public VectorStep removeEnd() {
    if (size() == 0) return null;
    VectorStep end = getEnd();
    end.chain = null;
    end.attach(null);
    super.remove(size() - 1);
    return end;
  }

  /**
  /**
   * Attempts to break this chain in two. If successful, the specified vector
   * becomes the tail vector of a new chain.
   *
   * @param vector the vector
   * @return the new chain, if any
   */
  public VectorChain breakAt(VectorStep vector) {
    if (vector.chain != this ||
        vector == getStart()) {
      return null;
    }
    if (vector == getEnd()) {
      removeEnd();
      return null;
    }
    // remove the downstream linked vectors from this chain
    ArrayList<VectorStep> list = remove(vector);
    // create a new chain and add the removed vectors
    VectorChain chain = new VectorChain(vector);
    for (int i = 1; i < list.size(); i++) {
      chain.add(list.get(i));
    }
    return chain;
  }

  /**
   * Overrides ArrayList method.
   */
  public void clear() {
    Iterator<VectorStep> it = iterator();
    while (it.hasNext()) {
      VectorStep link = it.next();
      // detach link and remove reference to this chain
      link.chain = null;
      link.attach(null);
    }
    super.clear();
  }

  /**
   * Ads a vector to this chain.
   *
   * @param vector the vector to add
   * @return true if the vector is successfully added
   */
  public boolean add(VectorStep vector) {
    // add a chain of vectors
    if (vector.getChain() != null) {
      return add(vector.getChain());
    }
    // link to the end hinge if allowed
    if (isAllowed(vector)) {
      VectorStep end = getEnd();
      vector.attach(end.getHinge());
      vector.chain = this;
      super.add(vector);
      return true;
    }
    return false;
  }

  /**
   * Adds a VectorChain to this chain.
   *
   * @param chain the chain to add
   * @return true if successfully added
   */
  public boolean add(VectorChain chain) {
    if (chain == this) return false;
    // remove all vectors from the chain and add them to this
    ArrayList<VectorStep> vectors = chain.remove(chain.getStart());
    addAll(vectors);
    return true;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param c the collection to add
   * @return true if at least one item in the collection is successfully added
   */
  public boolean addAll(Collection<? extends VectorStep> c) {
    boolean added = false;
    Iterator<? extends VectorStep> it = c.iterator();
    while (it.hasNext()) {
      added = add(it.next()) || added;
    }
    return added;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param index the index
   * @param v the vector to add
   */
  public void add(int index, VectorStep v) {/** empty block */}

  /**
   * Overrides ArrayList method.
   *
   * @param index the index
   * @return the object removed
   */
  public VectorStep remove(int index) {
    return null;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param obj the object to remove
   * @return false
   */
  public boolean remove(Object obj) {
    return false;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param from index
   * @param to index
   */
  public void removeRange(int from, int to) {/** empty block */}

  /**
   * Overrides ArrayList method.
   *
   * @param c a collection
   * @return false
   */
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param index the index
   * @param c the collection to add
   * @return true if the collection is successfully added
   */
  public boolean addAll(int index, Collection<? extends VectorStep> c) {
    return false;
  }

  /**
   * Overrides ArrayList method.
   *
   * @param index the index
   * @param obj the object
   * @return the element previously at index
   */
  public VectorStep set(int index, VectorStep obj) {
    return null;
  }

//_____________________________ protected methods _____________________________

  /**
   * Determines whether the specified vector is allowed to be added to the end hinge.
   *
   * @param vector the vector
   * @return true if allowed
   */
  protected boolean isAllowed(VectorStep vector) {
    if (vector.getChain() != null) return false;
    if (size() == 0) return true;
    Class<? extends TTrack> endClass = getEnd().getTrack().getClass();
    return endClass.equals(vector.getTrack().getClass());
  }

  /**
   * Removes vectors downstream of and including the specified vector.
   *
   * @param vector the vector
   * @return the list of vectors removed
   */
  protected ArrayList<VectorStep> remove(VectorStep vector) {
    ArrayList<VectorStep> list = new ArrayList<VectorStep>();
    int length = size();
    for (int i = indexOf(vector); i < length; i++) {
      list.add(0, removeEnd());
    }
    return list;
  }

}

