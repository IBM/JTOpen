///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Socks5Negotiator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * SOCK5 protocol negotiator
 */
class Socks5Negotiator {

	enum STATE {VERSION, AUTH, STATE, CONNECT, MESSAGE, ERROR}
		
	private final SocketChannel channel;
	private final String host;
	private final int port;
	
	private STATE state = STATE.VERSION;

	// not used
	private String user = null;
	
	// not used
	private String passwd = null;
			
	/**
	 * Constructor for SOCK5 negotiation protocol
	 * @param channel - SOCK5 channel connected to
	 * @param host - remote host to connect
	 * @param port - remote port to connect
	 * @throws UnknownHostException
	 */
	public Socks5Negotiator(final SocketChannel channel, final String host, final int port) throws UnknownHostException {
		super();
		this.channel = channel;
		this.host = resolve(host);
		this.port = port;
	}
	
	/**
	 * Set authentication to the SOCK5 server.
	 * <p>
	 * NOTE: Implemented but not used by AS400 object
	 * </p>
	 * @param user
	 * @param pasword
	 */
	public void setAuth(final String user, final String pasword) {
		this.user = user;
		this.passwd = pasword;
	}

	private static String resolve(final String host) throws UnknownHostException {
		return InetAddress.getByName(host).getHostAddress();
	}
	
	public boolean connect() throws IOException {

		boolean sts = false;
		
		switch (state) {
		case MESSAGE:
			sts = true;
			break;
		case VERSION:
			writeVersion();
			sts = connect();
			break;
		case AUTH:
			onAuth();
			sts = connect();
			break;
		case STATE:
			onState();
			sts = connect();
			break;			
		case CONNECT:
			onLink();
			sts = connect();
			break;
		default:
			close("Sockets5: invalid stream");
			sts = false;
			break;
		}

		return sts;
	}


	private boolean hasAuth() {		
		return UnixSocketUtil.nonEmpty(user) && UnixSocketUtil.nonEmpty(passwd);
	}
	
	/*
	    +----+----------+----------+
	    |VER | NMETHODS | METHODS  |
	    +----+----------+----------+
	    | 1  |    1     | 1 to 255 |
	    +----+----------+----------+
	
	The VER field is set to X'05' for this version of the protocol.  The
	NMETHODS field contains the number of method identifier octets that
	appear in the METHODS field.
	
	The values currently defined for METHOD are:
	
	o  X'00' NO AUTHENTICATION REQUIRED
	o  X'01' GSSAPI
	o  X'02' USERNAME/PASSWORD
	o  X'03' to X'7F' IANA ASSIGNED
	o  X'80' to X'FE' RESERVED FOR PRIVATE METHODS
	o  X'FF' NO ACCEPTABLE METHODS
	*/
	private void writeVersion() throws IOException {
		
		state = STATE.AUTH;
		
		final ByteBuffer bb = ByteBuffer.allocate(4);
		setInt(bb, 5);
		setInt(bb, 2);
		setInt(bb, 0);
		setInt(bb, 2);
		bb.flip();
		
		while (bb.hasRemaining()) {
			channel.write(bb);
		}
	}

	/*
	    The server selects from one of the methods given in METHODS, and
	    sends a METHOD selection message:
	
	                         +----+--------+
	                         |VER | METHOD |
	                         +----+--------+
	                         | 1  |   1    |
	                         +----+--------+
	*/	
	private void onAuth() throws IOException {

		getInt();
		final int method = getInt();
		
		boolean check = false;
		
		switch (method) {
		case 0:
			check = true;
			writeConnect();
			break;
		case 2:
			check = writeAuth();
			break;
		default:
			break;
		}

		if (!check) {
			close("Sockets5: authentication required.");
		}
	}
	
	/*
	   Once the SOCKS V5 server has started, and the client has selected the
	   Username/Password Authentication protocol, the Username/Password
	   subnegotiation begins.  This begins with the client producing a
	   Username/Password request:

	           +----+------+----------+------+----------+
	           |VER | ULEN |  UNAME   | PLEN |  PASSWD  |
	           +----+------+----------+------+----------+
	           | 1  |  1   | 1 to 255 |  1   | 1 to 255 |
	           +----+------+----------+------+----------+

	   The VER field contains the current version of the subnegotiation,
	   which is X'01'. The ULEN field contains the length of the UNAME field
	   that follows. The UNAME field contains the username as known to the
	   source operating system. The PLEN field contains the length of the
	   PASSWD field that follows. The PASSWD field contains the password
	   association with the given UNAME.
	*/	
	private boolean writeAuth() throws IOException {
		
		if (!hasAuth()) return false;
		
		state = STATE.STATE;

		final byte[] userBytes = UnixSocketUtil.normalize(user).getBytes(StandardCharsets.UTF_8);
		final byte[] passwordBytes = UnixSocketUtil.normalize(passwd).getBytes(StandardCharsets.UTF_8);
		final int size = 3 + userBytes.length + passwordBytes.length;
		
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		setInt(buffer, 1);
		setInt(buffer, userBytes.length);
		buffer.put(userBytes);
		setInt(buffer, passwordBytes.length);
		buffer.put(passwordBytes);
		
		buffer.flip();
		write(buffer);
		
		return true;
	}
	
	/*
	   The server verifies the supplied UNAME and PASSWD, and sends the
	   following response:

	                        +----+--------+
	                        |VER | STATUS |
	                        +----+--------+
	                        | 1  |   1    |
	                        +----+--------+

	   A STATUS field of X'00' indicates success. If the server returns a
	   `failure' (STATUS value other than X'00') status, it MUST close the
	   connection.
	*/	
	private void onState() throws IOException {

		getInt();
		int sts = getInt();

		if (sts == 0) {			
			writeConnect();
		} else {
			close("Sockets5: invalid authentication.");
		}
	}

	/*
	    The SOCKS request is formed as follows:
	
	      +----+-----+-------+------+----------+----------+
	      |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
	      +----+-----+-------+------+----------+----------+
	      | 1  |  1  | X'00' |  1   | Variable |    2     |
	      +----+-----+-------+------+----------+----------+
	
	    Where:
	
	    o  VER    protocol version: X'05'
	    o  CMD
	       o  CONNECT X'01'
	       o  BIND X'02'
	       o  UDP ASSOCIATE X'03'
	    o  RSV    RESERVED
	    o  ATYP   address type of following address
	       o  IP V4 address: X'01'
	       o  DOMAINNAME: X'03'
	       o  IP V6 address: X'04'
	    o  DST.ADDR       desired destination address
	    o  DST.PORT desired destination port in network octet
	       order
	*/	
	private void writeConnect() throws IOException {
		
		state = STATE.CONNECT;

        int len = 0;
        int size = 6 + len;
        
		byte[] hostb = null;
		String[] ip = null;
		boolean isIp = host.split("\\.").length == 4;
		
		if (isIp) {
			ip = host.split("\\.");
			size = size + 4;
		} else {
			hostb = host.getBytes(StandardCharsets.UTF_8);
			len = hostb.length;
			size = size + 1 + len;
		}
		
        final ByteBuffer buffer = ByteBuffer.allocate(size);
        
        setInt(buffer, 5);
        setInt(buffer, 1);       // CONNECT
        setInt(buffer, 0);

        if (isIp) {
	        setInt(buffer, 1);      // IP
	        for (String octet : ip) {
	        	setInt(buffer, Integer.parseInt(octet));	
	        }
        } else {
	        setInt(buffer, 3);      // DOMAINNAME
	        setInt(buffer, len);
	        buffer.put(hostb);
        }
        
        setInt(buffer, port >>> 8);
        setInt(buffer, port & 0xff);
		
		buffer.flip();
		write(buffer);

	}

	/*
	   The SOCKS request information is sent by the client as soon as it has
	   established a connection to the SOCKS server, and completed the
	   authentication negotiations.  The server evaluates the request, and
	   returns a reply formed as follows:

	        +----+-----+-------+------+----------+----------+
	        |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
	        +----+-----+-------+------+----------+----------+
	        | 1  |  1  | X'00' |  1   | Variable |    2     |
	        +----+-----+-------+------+----------+----------+

	   Where:

	   o  VER    protocol version: X'05'
	   o  REP    Reply field:
	      o  X'00' succeeded
	      o  X'01' general SOCKS server failure
	      o  X'02' connection not allowed by ruleset
	      o  X'03' Network unreachable
	      o  X'04' Host unreachable
	      o  X'05' XMPPConnection refused
	      o  X'06' TTL expired
	      o  X'07' Command not supported
	      o  X'08' Address type not supported
	      o  X'09' to X'FF' unassigned
	    o  RSV    RESERVED
	    o  ATYP   address type of following address
	      o  IP V4 address: X'01'
	      o  DOMAINNAME: X'03'
	      o  IP V6 address: X'04'
	    o  BND.ADDR       server bound address
	    o  BND.PORT       server bound port in network octet order
	*/	
	private void onLink() throws IOException {

		getInt();
		int rep = getInt();
		getInt();
		int atyp = getInt();
		
		if (rep != 0) {
			close(getMessage(rep));
			return;
		}
		
		final int addressBytes;
		
        switch (atyp) {
	        case 1:
	            addressBytes = 4;
	            break;
	        case 3:
	            // domainnameLengthByte
	            addressBytes = getInt();
	            break;
	        case 4:
	            addressBytes = 16;
	            break;
	        default:
	        	close("Unknown ATYP value: " + atyp);
	        	return;
	    }

        final ByteBuffer bb = ByteBuffer.allocate(addressBytes + 2);
        read(bb);
        
        state = STATE.MESSAGE;
		// listener.onConnect(context);
	}

	/**
	 * Close connection
	 * @param context
	 * @param reason
	 * @throws IOException 
	 */
	private void close(final String reason) throws IOException {
		channel.close();
		throw new IOException(reason);
	}
	
	/**
	 * Helper to convert byte to int 
	 * @param context
	 * @return
	 * @throws IOException 
	 */
	private int getInt() throws IOException {
		final  ByteBuffer bb = ByteBuffer.allocate(1);
		read(bb);
		return bb.get() & 0xff;
	}
	
	private void setInt(final ByteBuffer buffer, final int val) {
		buffer.put((byte)val);
	}
	
	/**
	  Convert response code to message	  
	 */
	private String getMessage(final int code) {
		
		String msg = null;
		
		switch (code) {
		case 1:
			msg = "General SOCKS server failure";
			break;
		case 2:
			msg = "Connection not allowed by ruleset";
			break;
		case 3:
			msg = "Network unreachable";
			break;
		case 4:
			msg = "Host unreachable";
			break;
		case 5:
			msg = "Connection refused";
			break;
		case 6:
			msg = "TTL expired";
			break;
		case 7:
			msg = "Command not supported";
			break;
		case 8:
			msg = "Address type not supported";
			break;
		default:
			msg = "Undefiend error reply";
			break;
		}
		
		return msg;
	}

	private void read(final ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			channel.read(buffer);
		}
		buffer.limit(buffer.position());
		buffer.rewind();
	}
	
	private void write(final ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			channel.write(buffer);
		}
	}

	/**
	 * Instruct SOCK5 server to establish connection to the remote host:port. 
	 * @param channel - SOCK5 channel connected to
	 * @param host - remote host to connect
	 * @param port - remote port to connect
	 * @param user
	 * @param pasword
	 * @return Return TRUE if negotiation is successful and connection to remote system is active   
	 * @throws IOException
	 */
	public static boolean negotiate(final SocketChannel channel, final String host, final int port, final String user, final String pasword) throws IOException {
		final Socks5Negotiator nego = new Socks5Negotiator(channel, host, port);
		nego.setAuth(user, pasword);
		return nego.connect();
	} 

}
