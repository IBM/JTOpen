/*
 * Copyright (C) 2015, 2026 Green Screens Ltd.
 */
package com.ibm.as400.socket;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Helper class to support Java16+ AF_UNIX and to allow compile for older Java
 * versions
 */
enum UnixSocketUtil {
	;

	static String normalize(final String data) {
		return normalize(data, "");
	}
	
	static String normalize(final String data, final String def) {
		return isEmpty(data) ? def : data.trim();
	}
	
	static boolean isEmpty(final String val) {
		return Objects.isNull(val) || val.length() == 0;	
	}
	
	static boolean nonEmpty(final String val) {
		return !isEmpty(val);
	}
		
	public static boolean isUNIXAddress(final String address) {
		return  isEmpty(address) ? false : address.indexOf(File.separator) > -1;		
	}
	
	public static SocketAddress getAddress(final String proxyHost, final int proxyPort, final String host, final int port) throws IOException {
		SocketAddress address = null;
		if (!isEmpty(proxyHost)) {
			if (isUNIXAddress(proxyHost)) {
				address = getUnixAddress(proxyHost);
			} else {
				address = new InetSocketAddress(proxyHost, proxyPort);
			}
		} else {
			address = new InetSocketAddress(host, port);
		}
		return address;
	}

	public static SocketChannel getUNIXFamily() throws IOException {
		SocketChannel channel = null;
		try {
			final StandardProtocolFamily family = StandardProtocolFamily.valueOf("UNIX");
			final Method method = SocketChannel.class.getMethod("open", ProtocolFamily.class);
			channel = (SocketChannel) method.invoke(SocketChannel.class, family);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return channel;
	}

	public static SocketAddress getUnixAddress(final String address) throws IOException {
		SocketAddress obj = null;
		try {
			final Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("java.net.UnixDomainSocketAddress");
			final Method method = clazz.getMethod("of", String.class);
			obj = (SocketAddress) method.invoke(clazz, address);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return obj;
	}

	public static SocketChannel proxy(final String proxyAddr, final String address, final int port, final String user, final String pasword) throws IOException {
		final SocketAddress unixAddr = getUnixAddress(proxyAddr);
		final SocketChannel channel = getUNIXFamily();
		channel.connect(unixAddr);
		Socks5Negotiator.negotiate(channel, address, port, user, pasword);
		return channel;
	}
	
	public static UnixSocket proxySocket(final String proxyAddr, final String address, final int port, final String user, final String pasword) throws IOException {
		return new UnixSocket(proxy(proxyAddr, address, port, user, pasword));
	}
}
