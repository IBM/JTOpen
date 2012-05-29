///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  HostServerConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.net.Socket;
import java.text.*;

/**
 * Represents a TCP/IP socket connection to an IBM i Host Server job. All
 * HostServerConnections have associated system information provided via the
 * SystemInfo class.
 **/
public abstract class HostServerConnection implements Connection {
	private final SystemInfo info_;
	private final String user_;
	private final String jobName_;
	private boolean closed_ = false;

	private final Socket socket_;
	protected final HostInputStream in_;
	protected final HostOutputStream out_;

	protected HostServerConnection(SystemInfo info, String user,
			String jobName, Socket socket, HostInputStream in,
			HostOutputStream out) {
		info_ = info;
		user_ = user;
		jobName_ = jobName;
		socket_ = socket;
		in_ = in;
		out_ = out;
	}

	/**
	 * Returns true if debugging is enabled by default for all
	 * HostServerConnection datastreams.
	 **/
	public static boolean isDefaultDatastreamDebug() {
		return Trace.isStreamTracingEnabled();
	}

	/**
	 * Returns true if datastream debugging is currently enabled.
	 **/
	public boolean isDatastreamDebug() {
		return in_.debug_;
	}

	/**
	 * If <i>debug</i> is true, enables datastream debugging for this
	 * connection.
	 **/
	public void setDatastreamDebug(boolean debug) {
		in_.setDebug(debug);
		out_.setDebug(debug);
	}

	/**
	 * Sends an "end job" datastream (if supported) and closes the underlying
	 * socket.
	 **/
	public final void close() throws IOException {
		if (closed_)
			return;
		try {
			sendEndJobRequest();
		} finally {
			closed_ = true;
			forceClose();
		}
	}

	protected abstract void sendEndJobRequest() throws IOException;

	private void forceClose() throws IOException {
		IOException rethrownException = null;
		try {
			in_.close();
			out_.close();
		} catch (java.net.SocketException socketException) {
			if (socketException.toString().indexOf("Socket closed") >= 0) {
				// Ignore this exception
			} else {
				rethrownException = socketException;
				throw rethrownException;
			}
		} finally {
			try {
				socket_.close();
			} catch (java.net.SocketException socketException) {
				if (socketException.toString().indexOf("Socket closed") >= 0) {
					// Ignore this exception
				} else {
					// Only throw an exception if one has not yet been thrown.
					if (rethrownException == null) {
						throw socketException;
					}
				}
			}
		}
	}

	protected final void finalize() throws Throwable {
		close();
	}

	/**
	 * Returns true if close() has been called on this connection.
	 **/
	public final boolean isClosed() {
		return closed_;
	}

	/**
	 * Returns the system information associated with this connection.
	 **/
	public final SystemInfo getInfo() {
		return info_;
	}

	/**
	 * Returns the currently authenticated user of this connection.
	 **/
	public final String getUser() {
		return user_;
	}

	/**
	 * Returns the host server job string for the job that is connected to this
	 * connection.
	 **/
	public final String getJobName() {
		return jobName_;
	}

	protected static byte[] getEncryptedPassword(byte[] userBytes,
			byte[] passwordBytes, byte[] clientSeed, byte[] serverSeed,
			int passwordLevel) throws IOException {
		final boolean doSHAInsteadOfDES = passwordLevel >= 2;
		byte[] encryptedPassword = null;
		if (doSHAInsteadOfDES) {
			// TODO
		} else {
			// Normal DES encryption.
			encryptedPassword = EncryptPassword.encryptPasswordDES(userBytes,
					passwordBytes, clientSeed, serverSeed);
		}
		return encryptedPassword;
	}

	protected static String connect(SystemInfo info, HostOutputStream dout,
			HostInputStream din, int serverID, String user, String password)
			throws IOException {
		// Exchange random seeds.
		long seed = sendExchangeRandomSeedsRequest(dout, serverID);
		byte[] clientSeed = Conv.longToByteArray(seed);
		dout.flush();

		int length = din.readInt();
		if (length < 20) {
			throw DataStreamException.badLength("exchangeRandomSeeds-"
					+ serverID, length);
		}
		din.skipBytes(16);
		int rc = din.readInt();
		if (rc != 0) {
			throw DataStreamException.badReturnCode("exchangeRandomSeeds-"
					+ serverID, rc);
		}
		byte[] serverSeed = new byte[8];
		din.readFully(serverSeed);

		byte[] userBytes = getUserBytes(user);
		byte[] passwordBytes = getPasswordBytes(password);
		password = null;
		byte[] encryptedPassword = getEncryptedPassword(userBytes,
				passwordBytes, clientSeed, serverSeed, info.getPasswordLevel());

		din.end();

		sendStartServerRequest(dout, userBytes, encryptedPassword, serverID);
		dout.flush();

		length = din.readInt();
		if (length < 20) {
			throw DataStreamException.badLength("startServer-" + serverID,
					length);
		}
		din.skipBytes(16);
		rc = din.readInt();
		if (rc != 0) {
			String msg = getReturnCodeMessage(rc);
			throw msg == null ? DataStreamException.badReturnCode(
					"startServer-" + serverID, rc) : DataStreamException
					.errorMessage("startServer-" + serverID, new Message(String
							.valueOf(rc), msg));
		}
		String jobName = null;
		int remaining = length - 24;
		while (remaining > 10) {
			int ll = din.readInt();
			int cp = din.readShort();
			remaining -= 6;
			if (cp == 0x111F) // Job name.
			{
				din.skipBytes(4); // CCSID is always 0.
				remaining -= 4;
				int jobLength = ll - 10;
				byte[] jobBytes = new byte[jobLength];
				din.readFully(jobBytes);
				jobName = Conv.ebcdicByteArrayToString(jobBytes, 0, jobLength);
				remaining -= jobLength;
			} else {
				din.skipBytes(ll - 6);
				remaining -= (ll - 6);
			}
		}
		din.skipBytes(remaining);
		din.end();
		return jobName;
	}

	private static String getReturnCodeMessage(int rc) {
		if ((rc & 0xFFFF0000) == 0x00010000)
			return "Error on request data";
		if ((rc & 0xFFFF0000) == 0x00040000)
			return "General security error, function not performed";
		if ((rc & 0xFFFF0000) == 0x00060000)
			return "Authentication Token error";
		switch (rc) {
		case 0x00020001:
			return "Userid error: User Id unknown";
		case 0x00020002:
			return "Userid error: User Id valid, but revoked";
		case 0x00020003:
			return "Userid error: User Id mismatch with authentication token";
		case 0x0003000B:
			return "Password error: Password or Passphrase incorrect";
		case 0x0003000C:
			return "Password error: User profile will be revoked on next invalid password or passphrase";
		case 0x0003000D:
			return "Password error: Password or Passphrase correct, but expired";
		case 0x0003000E:
			return "Password error: Pre-V2R2 encrypted password";
		case 0x00030010:
			return "Password error: Password is *NONE";
		}
		return null;
	}

	static void sendStartServerRequest(HostOutputStream out, byte[] userBytes,
			byte[] encryptedPassword, int serverID) throws IOException {
		out.writeInt(44 + encryptedPassword.length);
		out.writeByte(2); // Client attributes, 2 means return job info.
		out.writeByte(0); // Server attribute.
		out.writeShort(serverID); // Server ID.
		out.writeInt(0); // CS instance.
		out.writeInt(0); // Correlation ID.
		out.writeShort(2); // Template length.
		out.writeShort(0x7002); // ReqRep ID.
		out.writeByte(encryptedPassword.length == 8 ? 1 : 3); // Password
																// encryption
																// type.
		out.writeByte(1); // Send reply.
		out.writeInt(6 + encryptedPassword.length); // Password LL.
		out.writeShort(0x1105); // Password CP. 0x1115 is other.
		out.write(encryptedPassword);
		out.writeInt(16); // User ID LL.
		out.writeShort(0x1104); // User ID CP.
		out.write(userBytes);
	}

	static long sendExchangeRandomSeedsRequest(HostOutputStream out,
			int serverID) throws IOException {
		out.writeInt(28); // Length.
		out.writeByte(1); // Client attributes, 1 means capable of SHA-1.
		out.writeByte(0); // Server attributes.
		out.writeShort(serverID); // Server ID.
		out.writeInt(0); // CS instance.
		out.writeInt(0); // Correlation ID.
		out.writeShort(8); // Template length.
		out.writeShort(0x7001); // ReqRep ID.
		long clientSeed = System.currentTimeMillis();
		out.writeLong(clientSeed);
		return clientSeed;
	}

	static byte[] getUserBytes(String user) throws IOException {
		if (user.length() > 10) {
			throw new IOException("User too long");
		}
		byte[] user37 = Conv.blankPadEBCDIC10(user.toUpperCase());
		return user37;
	}

	static byte[] getPasswordBytes(String password) throws IOException {
		// Prepend a Q to numeric password.
		if (password.length() > 0 && Character.isDigit(password.charAt(0))) {
			password = "Q" + password;
		}
		if (password.length() > 10) {
			throw new IOException("Password too long");
		}
		byte[] password37 = Conv.blankPadEBCDIC10(password.toUpperCase());
		return password37;
	}

	protected static final class HostInputStream {
		// private static boolean allDebug_ = false;

		private final InputStream in_;
		private boolean debug_;
		private int debugCounter_ = 0;

		private final byte[] shortArray_ = new byte[2];
		private final byte[] intArray_ = new byte[4];
		private final byte[] longArray_ = new byte[8];

		private PrintStream tracePrintStream = null;

		// public static void setAllDebug(boolean debug)
		// {
		// allDebug_ = debug;
		// }

		public HostInputStream(InputStream in) {
			in_ = in;
			debug_ = Trace.isStreamTracingEnabled();
			if (debug_) {
				tracePrintStream = Trace.getPrintStream();
			}

		}

		public void setDebug(boolean debug) {
			debug_ = debug;
			if (debug_) {
				tracePrintStream = Trace.getPrintStream();
			}

		}

		private void debugByte(int i) {
			if (tracePrintStream != null) {
				if (debugCounter_ == 0) {
					synchronized (HostOutputStream.formatter_) {

						tracePrintStream.print(HostOutputStream.formatter_
								.format(new java.util.Date()));
						tracePrintStream
								.println(" Data stream data received...");
					}
				}
				int highNibble = (0x00FF & i) >> 4;
				int lowNibble = 0x000F & i;
				tracePrintStream.print(HostOutputStream.CHAR[highNibble]);
				tracePrintStream.print(HostOutputStream.CHAR[lowNibble]);
				if (++debugCounter_ % 16 == 0)
					tracePrintStream.println();
				else
					tracePrintStream.print(" ");
			}
		}

		private void debugBytes(byte[] b, int offset, int length) {
			for (int i = offset; i < offset + length; ++i) {
				debugByte(b[i]);
			}
		}

		/**
		 * Used to note the end of a datastream when debugging is enabled.
		 **/
		public void end() {
			if (debug_ && tracePrintStream != null) {
				tracePrintStream.println();
				debugCounter_ = 0;
			}
		}

		public int read() throws IOException {
			int i = in_.read();
			if (i < 0) {
				throw new EOFException();
			}
			if (debug_) {
				debugByte(i);
			}
			return i;
		}

		public int readByte() throws IOException {
			return read();
		}

		public int readShort() throws IOException {
			int i = in_.read(shortArray_);
			if (i != 2) {
				int numRead = (i >= 0 ? i : 0);
				while (i >= 0 && numRead < 2) {
					i = in_.read(shortArray_, numRead, 2 - numRead);
					numRead += (i >= 0 ? i : 0);
				}
				if (numRead < 2) {
					throw new EOFException();
				}
			}
			if (debug_) {
				debugBytes(shortArray_, 0, 2);
			}
			return ((shortArray_[0] & 0x00FF) << 8) | (shortArray_[1] & 0x00FF);
		}

		public int readInt() throws IOException {
			int i = in_.read(intArray_);
			if (i != 4) {
				int numRead = (i >= 0 ? i : 0);
				while (i >= 0 && numRead < 4) {
					i = in_.read(intArray_, numRead, 4 - numRead);
					numRead += (i >= 0 ? i : 0);
				}
				if (numRead < 4) {
					throw new EOFException();
				}
			}
			if (debug_) {
				debugBytes(intArray_, 0, 4);
			}
			return Conv.byteArrayToInt(intArray_, 0);
		}

		public long readLong() throws IOException {
			int i = in_.read(longArray_);
			if (i != 8) {
				int numRead = (i >= 0 ? i : 0);
				while (i >= 0 && numRead < 8) {
					i = in_.read(longArray_, numRead, 8 - numRead);
					numRead += (i >= 0 ? i : 0);
				}
				if (numRead < 8) {
					throw new EOFException();
				}
			}
			if (debug_) {
				debugBytes(longArray_, 0, 8);
			}
			return Conv.byteArrayToLong(longArray_, 0);
		}

		public int skipBytes(final int n) throws IOException {
			if (debug_) {
				int num = 0;
				while (num < n) {
					read();
					++num;
				}
				return num;
			}

			int i = (int) in_.skip(n);
			if (i != n) {
				int numSkipped = (i >= 0 ? i : 0);
				while (i >= 0 && numSkipped < n) {
					i = (int) in_.skip(n - numSkipped);
					numSkipped += (i >= 0 ? i : 0);
				}
				if (numSkipped < n) {
					throw new EOFException();
				}
			}
			return i;
		}

		public void close() throws IOException {
			in_.close();
			if (debug_ && tracePrintStream != null) {
				tracePrintStream.println();
				debugCounter_ = 0;
			}
		}

		public void readFully(final byte[] b) throws IOException {
			int i = in_.read(b);
			if (i != b.length) {
				int numRead = (i >= 0 ? i : 0);
				while (i >= 0 && numRead < b.length) {
					i = in_.read(b, numRead, b.length - numRead);
					numRead += (i >= 0 ? i : 0);
				}
				if (numRead < b.length) {
					throw new EOFException();
				}
			}
			if (debug_) {
				debugBytes(b, 0, b.length);
			}
		}

		public void readFully(final byte[] b, final int offset, final int length)
				throws IOException {
			int i = in_.read(b, offset, length);
			if (i != length) {
				int numRead = (i >= 0 ? i : 0);
				while (i >= 0 && numRead < length) {
					i = in_.read(b, offset + numRead, length - numRead);
					numRead += (i >= 0 ? i : 0);
				}
				if (numRead < length) {
					throw new EOFException();
				}
			}
			if (debug_) {
				debugBytes(b, offset, length);
			}
		}
	};

	protected static final class HostOutputStream {
		static SimpleDateFormat formatter_ = new SimpleDateFormat(
				"EEE MMM d HH:mm:ss:SSS z yyyy");
		static final char[] CHAR = new char[] { '0', '1', '2', '3', '4', '5',
				'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		// private static boolean allDebug_ = false;

		private final OutputStream out_;
		private boolean debug_;
		private int debugCounter_ = 0;
		private PrintStream tracePrintStream = null;

		// public static void setAllDebug(boolean debug)
		// {
		// allDebug_ = debug;
		// }

		public HostOutputStream(final OutputStream out) {
			out_ = out;
			debug_ = Trace.isStreamTracingEnabled();
			if (debug_) {
				tracePrintStream = Trace.getPrintStream();
			}
		}

		public void setDebug(boolean debug) {
			debug_ = debug;
			if (debug_) {
				tracePrintStream = Trace.getPrintStream();
			}
		}

		public void writeInt(final int i) throws IOException {
			out_.write(i >> 24);
			out_.write(i >> 16);
			out_.write(i >> 8);
			out_.write(i);
			if (debug_) {
				debugInt(i);
			}
		}

		private void debugInt(int i) {
			debugByte(i >> 24);
			debugByte(i >> 16);
			debugByte(i >> 8);
			debugByte(i);
		}

		private void debugShort(int i) {
			debugByte(i >> 8);
			debugByte(i);
		}

		private void debugByte(int i) {
			if (tracePrintStream != null) {
				if (debugCounter_ == 0) {
					synchronized (formatter_) {
						tracePrintStream.print(formatter_
								.format(new java.util.Date()));
						tracePrintStream.println(" Data stream sent...");
					}
				}
				int highNibble = (0x00FF & i) >> 4;
				int lowNibble = 0x000F & i;
				tracePrintStream.print(CHAR[highNibble]);
				tracePrintStream.print(CHAR[lowNibble]);
				if (++debugCounter_ % 16 == 0)
					tracePrintStream.println();
				else
					tracePrintStream.print(" ");
			}
		}

		private void debugBytes(byte[] b, int offset, int length) {
			for (int i = offset; i < offset + length; ++i) {
				debugByte(b[i]);
			}
		}

		public void writeShort(final int i) throws IOException {
			out_.write(i >> 8);
			out_.write(i);
			if (debug_) {
				debugShort(i);
			}
		}

		public void writeLong(final long l) throws IOException {
			int i1 = (int) (l >> 32);
			int i2 = (int) l;
			writeInt(i1);
			writeInt(i2);
		}

		public void write(final byte[] b) throws IOException {
			out_.write(b, 0, b.length);
			if (debug_) {
				debugBytes(b, 0, b.length);
			}
		}

		public void write(final byte[] b, final int offset, final int length)
				throws IOException {
			out_.write(b, offset, length);
			if (debug_) {
				debugBytes(b, offset, length);
			}
		}

		public void write(final int i) throws IOException {
			out_.write(i);
			if (debug_) {
				debugByte(i);
			}
		}

		public void writeByte(final int i) throws IOException {
			out_.write(i);
			if (debug_) {
				debugByte(i);
			}
		}

		public void close() throws IOException {
			out_.close();
			if (debug_ && tracePrintStream != null) {
				tracePrintStream.println();
				debugCounter_ = 0;
			}
		}

		public void flush() throws IOException {
			out_.flush();
			if (debug_ && tracePrintStream != null) {
				tracePrintStream.println();
				debugCounter_ = 0;
			}
		}
	}

	protected static void writePadEBCDIC10(String s, HostOutputStream out)
			throws IOException {
		Conv.writePadEBCDIC10(s, out);
	}

	protected static void writePadEBCDIC(String s, int len, HostOutputStream out)
			throws IOException {
		Conv.writePadEBCDIC(s, len, out);
	}

	protected static void writeStringToUnicodeBytes(String s,
			HostOutputStream out) throws IOException {
		Conv.writeStringToUnicodeBytes(s, out);
	}
}
