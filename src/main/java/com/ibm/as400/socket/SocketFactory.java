///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SocketFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;

/**
 * Factory for SOCK5, AF_UNIX socket or standard TCP socket.
 */
public enum SocketFactory {
;

	/**
	 * Factory to instantiate proper socket type.
	 * <p> 
	 * Type of socket returned is based on proxyAddress format:
	 * </p>
	 * <pre>
	 * 1. If proxyAddress is not provided, standard socket is created
	 * 2. if proxyAddress is file based socket format, UNIX socket is created
	 * 3. if proxyAddress is a hostname or IP address, SOCK5 socket is created
	 * </pre>
	 *  
	 * @param systemName - Remote system to connect to
	 * @param port - Remote port to connect to
	 * @param proxyAddress - Address in format host:port or host or /unix/path
	 * @return
	 * @throws IOException
	 */	
	public static Socket createSocket(final String systemName, final int port, final String proxyAddress) throws IOException {
		if (UnixSocketUtil.isEmpty(proxyAddress)) {
			return createSocket(systemName, port, null, 0);
		} else {
			final URI uri = UnixSocketUtil.toUri(proxyAddress);
			return createSocket(systemName, port, uri.getHost(), uri.getPort());
		}
	}
		
	/**
	 * Factory to instantiate proper socket type.
	 * <p>
	 * Type of socket returned is based on proxyAddress format:
	 * </p>
	 * <pre>
	 * 1. If proxyAddress is not provided, standard socket is created
	 * 2. if proxyAddress is file based socket format, UNIX socket is created
	 * 3. if proxyAddress is a hostname or IP address, SOCK5 socket is created
	 * </pre>
	 * 
	 * @param remoteSystem - Remote system to connect to
	 * @param remotePort - Remote port to connect to
	 * @param socketAddress - Local sock5 daemon to connect to
	 * @param socketPort - Local daemon port to connect to
	 * @return
	 * @throws IOException
	 */
	public static Socket createSocket(final String remoteSystem, final int remotePort, final String socketAddress, final int socketPort) throws IOException {

		Socket pmSocket = null;

		final boolean isUnix = UnixSocketUtil.isUNIXAddress(socketAddress);
		final boolean isSock = isProxySock(socketAddress, socketPort);
		final boolean isSystem = UnixSocketUtil.nonEmpty(remoteSystem); 

		if (isUnix) {
			pmSocket = UnixSocketUtil.create(remoteSystem, remotePort, socketAddress, null, null);
		} else if (isSock) { 
			pmSocket = createSock5(socketAddress, socketPort);
		} else if (isSystem) { 
			pmSocket = new Socket(remoteSystem, remotePort);
		} else {
			pmSocket = new Socket();
		}
		
		if (isSystem && (isSock || isUnix)) {
			if (!pmSocket.isConnected()) {
    			final InetSocketAddress endpoint = new InetSocketAddress(remoteSystem, remotePort);
    			pmSocket.connect(endpoint);
    		}			
		}
		
		return pmSocket;
	}

	/**
	 * Create SOCK5 socket connection.
	 * 
	 * @param socketAddress
	 * @param socketPort
	 * @return
	 */
	static Socket createSock5(final String socketAddress, final int socketPort) {
		final InetSocketAddress sockAddr = new InetSocketAddress(socketAddress, socketPort);
		final Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, sockAddr);		
		return new Sock5Socket(socksProxy);
	}
	
	/**
	 * Check if sock5 target is defined.
	 * @param proxyAddress
	 * @param proxyPort
	 * @return
	 */
	static boolean isProxySock(final String proxyAddress, final int proxyPort) {
		return UnixSocketUtil.nonEmpty(proxyAddress) && proxyPort > 0;
	}
	
}
