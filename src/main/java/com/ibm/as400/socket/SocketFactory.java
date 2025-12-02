/*
 * Copyright (C) 2015, 2026 Green Screens Ltd.
 */
package com.ibm.as400.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Objects;

/**
 * Adds JT400 support for proxied socket
 */
public enum SocketFactory {
;

	public static Socket createUnixSocket(final String systemName, final int port, final String proxyAddress, final int proxyPort) throws IOException {
		final boolean isUnix = UnixSocketUtil.isUNIXAddress(proxyAddress);
		if (isUnix) {
			return SocketFactory.createSocket(systemName, port, proxyAddress, proxyPort);
		} else {
			return SocketFactory.createSocket(null, 0, proxyAddress, proxyPort);
		}
	}
	
	public static Socket createSocket(final String systemName, final int port, final String proxyAddress, final int proxyPort) throws IOException {

		Socket pmSocket = null;
		
		if (isProxySock(proxyAddress, proxyPort)) {
			
			final boolean isUnix = UnixSocketUtil.isUNIXAddress(proxyAddress);
			
			if (isUnix) {
				pmSocket = UnixSocketUtil.proxySocket(proxyAddress, systemName, port, null, null);
			} else {
				final InetSocketAddress sockAddr = new InetSocketAddress(proxyAddress, proxyPort);
				final Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, sockAddr);
				pmSocket = new Socket(socksProxy);					
			}

			if (Objects.nonNull(systemName) && !pmSocket.isConnected()) {
    			final InetSocketAddress endpoint = new InetSocketAddress(systemName, port);
    			pmSocket.connect(endpoint);
    		}
    		
		} else if (Objects.isNull(systemName)) {
			pmSocket = new Socket();
		} else {
			pmSocket = new Socket(systemName, port); 
		}
		
		return pmSocket;
	}

	static boolean isProxySock(final String proxyAddress, final int proxyPort) {
		return UnixSocketUtil.nonEmpty(proxyAddress) && proxyPort > 0;
	}
	
}
