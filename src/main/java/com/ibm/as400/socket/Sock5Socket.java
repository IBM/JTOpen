///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Sock5Socket.java
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
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Wrapper around proxied Socket for easier detection from code.
 * <p>
 * Original Java socket instance does not provide any means of detecting socket
 * type. When SocketFactory creates a socket, one of the types will be returned:
 * Socket, UinxSocket or SockSocket.
 * </p>
 * <p>
 * Code example to determine SOCK5 and type (PROXY, DIRECT, HTTP).
 * </p>
 * 
 * <pre>
 * if (socket instance of Sock5Socket) {
 *  ((Sock5Socket)socket).type() == Proxy.Type.PROXY
 * }
 * </pre>
 */
public class Sock5Socket extends Socket {

	Proxy proxy = null;

	public Sock5Socket(final Proxy proxy) {
		super(proxy);
		this.proxy = proxy;
	}

	@Override
	public void connect(final SocketAddress endpoint) throws IOException {
		super.connect(endpoint);
	}

	/**
	 * Return type of proxy. (SOCK5, DIRECT, HTTP)
	 * 
	 * @return Proxy.Type
	 */
	public Type type() {
		return proxy.type();
	}

	/**
	 * SOCK5 daemon address for clients to connect.
	 * 
	 * @return
	 */
	public String sock5Address() {
		return proxyAddress().getHostString();
	}

	/**
	 * SOCK5 daemon port for clients to connect.
	 * 
	 * @return
	 */
	public int sock5Port() {
		return proxyAddress().getPort();
	}

	InetSocketAddress proxyAddress() {
		return ((InetSocketAddress) proxy.address());
	}
}
