/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JComponent;


/**
 * A class to use Sockets for inter-process communication on a local machine.
 * Typical use: 1. one object creates a server socket on a unique port (between 1024 and 65K)
 * 							2. other objects create client sockets on the same port 
 * 							3. messages received by both server and clients are passed to registered 
 * 								 PropertyChangeListeners as "socket" events.
 *
 * @author Doug Brown
 * @version 1.0
 */
public class OSPSocket extends JComponent {
	
	public static final String READY = "ready"; //$NON-NLS-1$
	public static final String OPEN = "open: "; //$NON-NLS-1$
	
	int portNumber;
	DataOutputStream os;
	DataInputStream is;
	boolean clientReady = false;
	boolean isServer = false;
	boolean terminated = false;
	
  /**
   * Constructor.
   * @param port the port number to use (must be between 1024 and 65K)
   * @param server true to create a server socket, false to create a client
   */
	public OSPSocket(int port, boolean server) {
		portNumber = port;
		isServer = server;
  	Runnable runner = new Runnable() {
  		public void run() {
				try {
		      if (isServer) createServer();
		      else createClient();
				} catch (Exception e) {
					terminated = true;
				}
  		}
  	};
  	Thread socketThread = new Thread(runner);
  	socketThread.setDaemon(true);
  	socketThread.start();
	}

  /**
   * Sends a message to the connected server or client.
   * @param message the message to send
   */
  public void send(String message) throws Exception {
    try {
    	byte[] byteData = message.getBytes();
      os.write(byteData);
      os.flush();
    }
    catch (Exception exception) {
      throw exception;
    }
  }
  
//_____________________________________________  private methods  ____________________________________  
	
  /**
   * Receives a message from the connected server or client.
   * @return the message received
   */
  private String receive() throws Exception {
    try {
      byte[] inputData = new byte[1024];
      is.read(inputData);
      return new String(inputData).trim();
    }
    catch (Exception exception) {
      throw exception;
    }
  }

	private void createServer() throws Exception {
		ServerSocket serverSocket = new ServerSocket(portNumber);
		
		Socket clientSocket = serverSocket.accept();
		os = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
    is = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
		
    // monitor client for ready signal
    while (!clientReady && !terminated) {
    	// don't hog processor time
      Thread.sleep(100);      
      String message = receive();
      if (READY.equals(message)) {
      	clientReady = true;
      }
    }
	}

	private void createClient() throws Exception {
		Socket socket = new Socket("localhost", portNumber); //$NON-NLS-1$
    os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

    // inform server this is ready for input
    send(READY);
    
    // monitor server 
    while (!terminated) {
    	// don't hog processor time
      Thread.sleep(100);      
      String data = receive();
      // send the message to listeners
      firePropertyChange("socket", null, data); //$NON-NLS-1$
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
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
