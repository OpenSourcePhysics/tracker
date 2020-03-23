/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.opensourcephysics.controls.OSPLog;

/**
 * This is a Remote Tool implementation for osp data transfers via RMI.
 *
 * @author Wolfgang Christian and Doug Brown
 * @version 1.0
 */
public class RemoteTool extends UnicastRemoteObject implements Tool {
	private static final long serialVersionUID = 1L;
	
  // instance fields
  Tool child;                                                                // a Tool to handle forwarded jobs
  Map<Job, Collection<Tool>> replies = new HashMap<Job, Collection<Tool>>(); // maps job to list of replyTo recipients
  Map<Job, Job> jobs = new HashMap<Job, Job>();                              // maps RemoteJob to LocalJob

  /**
   * Constructs a RemoteTool.
   *
   * @param tool a Tool to handle forwarded jobs
   * @throws RemoteException if this cannot be constructed
   */
  public RemoteTool(Tool tool) throws RemoteException {
    super();
    OSPLog.finest("Wrapping tool "+tool.getClass().getName()); //$NON-NLS-1$
    child = tool;
  }

  /**
   * Sends a job to this tool.
   *
   * @param job the job
   * @param replyTo the tool interested in the job (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {
    save(job, replyTo);
    job = convert(job);
    if(child.equals(replyTo)) { // job comes from child, so send replies
      sendReplies(job);
    } else {                    // forward job to child
      forward(job);
    }
  }

  // ____________________________ private methods _________________________________

  /**
   * Saves a tool for later replies.
   *
   * @param job the job
   * @param tool the tool interested in the job (may be null)
   */
  private void save(Job job, Tool tool) {
    if((tool==null)||child.equals(tool)) {
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
   * Replies to tools interested in the specified job.
   *
   * @param job the job
   */
  private void sendReplies(Job job) throws RemoteException {
    Collection<?> tools = replies.get(job);
    if(tools==null) {
      return;
    }
    Iterator<?> it = tools.iterator();
    while(it.hasNext()) {
      Tool tool = (Tool) it.next();
      tool.send(job, this);
    }
  }

  /**
   * Forwards a job to a child.
   *
   * @param job the job
   */
  private void forward(Job job) throws RemoteException {
    child.send(job, this);
  }

  /**
   * Wraps a job for forwarding or unwraps it for replies.
   *
   * @param job the job to be converted
   * @return the converted job
   */
  private Job convert(Job job) throws RemoteException {
    if(job instanceof LocalJob) {
      Job remote = new RemoteJob(job);
      jobs.put(remote, job);
      return remote;
    }
    Object obj = jobs.get(job);
    if(obj==null) {
      return job;
    }
    return(Job) obj;
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
