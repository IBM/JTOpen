///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UnixSocketUtil.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Helper class to support Java16+ AF_UNIX and to allow compile for older Java
 * versions
 */
enum UnixSocketUtil {
	;

	/**
	 * Helper to normalize string. If string is null, will return blank.
	 * 
	 * @param data
	 * @return
	 */
	static String normalize(final String data) {
		return normalize(data, "");
	}

	/**
	 * Helper to normalize string. If string is null, will return default value.
	 * 
	 * @param data
	 * @param def
	 * @return
	 */
	static String normalize(final String data, final String def) {
		return isEmpty(data) ? def : data.trim();
	}

	/**
	 * Check if string is null or blank.
	 * 
	 * @param val
	 * @return
	 */
	static boolean isEmpty(final String val) {
		return Objects.isNull(val) || val.length() == 0;
	}

	/**
	 * Check if string is not null nor blank
	 * 
	 * @param val
	 * @return
	 */
	static boolean nonEmpty(final String val) {
		return !isEmpty(val);
	}

	/**
	 * Helper method to check if provided address is of type AF_UNIX. Address is a
	 * path to a local file system path and must contain at least one file
	 * separator.
	 * 
	 * @param address
	 * @return
	 */
	public static boolean isUNIXAddress(final String address) {
		return isEmpty(address) ? false : address.contains("/");
	}

	public static boolean hasSchema(final String address) {
		return isEmpty(address) ? false : address.contains("://");
	}

	/**
	 * Convert string format to URI.
	 * 
	 * <p>
	 * NOTE: For correct URI parsing, schema must be specified.
	 * </p>
	 * 
	 * <p>
	 * If schema is not specified, file:// will be used if value contains UNIX file
	 * separator and is considered AF_UNIX connection. If schema is not specified
	 * and no UNIX file separator, sock:// is used to distinguish from HTTP/s used
	 * by standard web proxy
	 * </p>
	 * 
	 * Examples: 127.0.0.1:5600 127.0.0.1 /var/temp/mysock
	 * 
	 * sock://127.0.0.1:5600 sock://127.0.0.1 file:///var/temp/mysock
	 * 
	 * @param address host:port or file path with optional schema prefix
	 * @return
	 */
	public static URI toUri(final String address) {
		final boolean isUnix = isUNIXAddress(address);
		final boolean hasSchema = hasSchema(address);
		String url = address;
		if (isUnix && !hasSchema) url = String.format("file://%s", address);
		if (!isUnix && !hasSchema) url = String.format("sock://%s", address);
		return URI.create(url);
	}

	/**
	 * Create an AF_UNIX socket channel.
	 * 
	 * <p>
	 * NOTE: Reflection used to allow compilation for older Java versions.
	 * Original code should be: ServerSocketChannel.open(StandardProtocolFamily.UNIX);
	 * </p>
	 * <p>
	 * NOTE: In Java versions older than 16, exception will be thrown for unknown
	 * class.
	 * </p>
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Parse AF_UNIX address and create SocketAddress instance.
	 * 
	 * <p>
	 * NOTE: Reflection is used to allow compilation for pre-Java 16.
	 * Original code shouldbe: UnixDomainSocketAddress.of(socketPath);
	 * </p>
	 * <p>
	 * NOTE: In Java versions older than 16, exception will be thrown for unknown
	 * class.
	 * </p>
	 * 
	 * @param address - AF_UNIX address format (path to a file in local file system
	 *                used as IPC socket)
	 * @return - Return socket address of a AF_UNIX format
	 * @throws IOException
	 */
	public static SocketAddress getUnixAddress(final String address) throws IOException {
		if (!isUNIXAddress(address)) throw new IOException(String.format("Address is not of AF_UNIX type (%s)",address));
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

	/**
	 * SOCK5 channel over AF_UNIX.
	 * 
	 * <p>
	 * NOTE: SOCK5 protocol supports access authorization. This is optional and
	 * commonly not used in AF_UNIX or TCP based SOCK5 listening on localhost. 
	 * </p>
	 * 
	 * For example: Start remote shadowsock service and local SOCK5 daemon with
	 * ability to bind to AF_UNIX. Then use this to create a client SOCK5 to
	 * forward all AS400 network through encrypted channel.
	 * 
	 * @param remoteAddress  - remote system address to connect
	 * @param remotePort     - remote system port to connect
	 * @param unixAddr       - AF_UNIX address to connect to (local file system path)
	 * @param unixUser       - Optional SOCK5 access user
	 * @param unixPasword    - Optional SOCK5 access user credential
	 * @return Return SOCK5 channel over AF_UNIX
	 * @throws IOException
	 */
	public static SocketChannel connect(final String remoteAddress, final int remotePort,
			final String unixAddr, 	final String unixUser, final String unixPasword) throws IOException {
		final SocketAddress socketAddr = getUnixAddress(unixAddr);
		final SocketChannel channel = getUNIXFamily();
		channel.connect(socketAddr);
		Socks5Negotiator.negotiate(channel, remoteAddress, remotePort, unixUser, unixPasword);
		return channel;
	}

	/**
	 * Helper method for SOCK5 over AF_UNIX channel that wraps AF_UNIX channel with
	 * socket.
	 * 
	 * @param unixAddr - AF_UNIX address to connect to (local file system path)
	 * @param address  - remote system address to connect
	 * @param port     - remote system port to connect
	 * @param user     - SOCK5 access user
	 * @param pasword  - SOCK5 access user credential
	 * @return - Return UnixSocket representing SOCK5 channel over AF_UNIX
	 * @throws IOException
	 */
	public static UnixSocket create(final String remoteAddress, final int remotePort,
			final String unixAddr, 	final String unixUser, final String unixPasword) throws IOException {
		return new UnixSocket(connect(remoteAddress, remotePort, unixAddr, unixUser, unixPasword));
	}

}
