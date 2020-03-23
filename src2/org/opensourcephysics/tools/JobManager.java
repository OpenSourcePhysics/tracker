/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;

/**
 * This manages jobs and their associated objects and replies.
 *
 * @author Wolfgang Christian and Doug Brown
 * @version 1.0
 */
public class JobManager {
  // instance fields
  Tool localTool;                                                                // the tool using this manager
  Map<Job, Collection<Tool>> replies = new HashMap<Job, Collection<Tool>>();     // maps job to set of replyTo recipients
  Map<Job, Collection<Object>> objects = new HashMap<Job, Collection<Object>>(); // maps job to set of objects

  /**
   * Constructs a job manager for a specified tool.
   *
   * @param tool the tool
   */
  public JobManager(Tool tool) {
    localTool = tool;
  }

  /**
   * Logs a job and tool into the manager.
   *
   * @param job the job
   * @param tool a tool interested in the job
   */
  public void log(Job job, Tool tool) {
    if(tool==null) {
      return;
    }
    Collection<Tool> tools = replies.get(job);
    if(tools==null) {
      tools = new HashSet<Tool>();
      replies.put(job, tools);
    }
    tools.add(tool);
  }

  /**
   * Associates a job with the specified object.
   *
   * @param job the job
   * @param obj the object
   */
  public void associate(Job job, Object obj) {
    if(obj==null) {
      return;
    }
    Collection<Object> tags = objects.get(job);
    if(tags==null) {
      tags = new HashSet<Object>();
      objects.put(job, tags);
    }
    tags.add(obj);
  }

  /**
   * Gets the jobs associated with the specified object.
   *
   * @param obj the object
   * @return an array of Jobs
   */
  public Job[] getJobs(Object obj) {
    Collection<Object> jobs = new ArrayList<Object>();
    Iterator<Job> it = objects.keySet().iterator();
    while(it.hasNext()) {
      Object job = it.next();
      Collection<?> tags = objects.get(job);
      if(tags==null) {
        return null;
      }
      if(tags.contains(obj)) {
        jobs.add(job);
      }
    }
    return jobs.toArray(new Job[0]);
  }

  /**
   * Gets the objects associated with the specified job.
   *
   * @param job the job
   * @return an array of objects
   */
  public Object[] getObjects(Job job) {
    Collection<?> tags = objects.get(job);
    if(tags==null) {
      return new Object[0];
    }
    return tags.toArray(new Object[0]);
  }

  /**
   * Gets the tools interested in the specified object.
   *
   * @param obj the object
   * @return a collection of tools
   */
  public Collection<Tool> getTools(Object obj) {
    Collection<Tool> tools = new HashSet<Tool>();
    Job[] jobs = getJobs(obj);
    for(int i = 0; i<jobs.length; i++) {
      Collection<Tool> next = replies.get(jobs[i]);
      if(next!=null) {
        tools.addAll(next);
      }
    }
    return tools;
  }

  /**
   * Replies to tools interested in the specified object.
   *
   * @param obj the object
   */
  public void sendReplies(Object obj) {
    Job[] jobs = getJobs(obj);
    XMLControl control = new XMLControlElement(obj);
    String xml = control.toXML();
    for(int i = 0; i<jobs.length; i++) {
      try {
        jobs[i].setXML(xml);
      } catch(RemoteException ex) {
        ex.printStackTrace();
      }
      sendReplies(jobs[i]);
    }
  }

  /**
   * Replies to tools interested in the specified job.
   *
   * @param job the job
   */
  public void sendReplies(Job job) {
    Collection<Tool> tools = replies.get(job);
    if(tools==null) {
      return;
    }
    Iterator<Tool> it = tools.iterator();
    try {
      while(it.hasNext()) {
        Tool tool = it.next();
        tool.send(job, localTool);
      }
    } catch(RemoteException ex) {
      ex.printStackTrace();
    }
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
