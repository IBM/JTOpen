///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UnixSocket.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A UNIX socket (Unix Domain Socket), is an inter-process communication mechanism 
 * that allows bidirectional data exchange between processes running on the same machine.
 * 
 * UnixSocket class is a wrapper around AF_UNIX based SocketChannel, which later binds
 * to the UNIX "file" address (~/example.sock). 
 * 
 * Based on original
 * https://github.com/jnr/jnr-unixsocket/blob/master/src/main/java/jnr/unixsocket/UnixSocket.java
 */
public class UnixSocket extends java.net.Socket {

    private SocketChannel channel;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean indown = new AtomicBoolean(false);
    private AtomicBoolean outdown = new AtomicBoolean(false);

    private InputStream in;
    private OutputStream out;
    private boolean bound = false;

    public UnixSocket(final SocketChannel channel) {
    	if (Objects.isNull(channel)) throw new NullPointerException("Channel not defined");
        this.channel = channel;
        in = Channels.newInputStream(new UnselectableByteChannel(channel));
        out = Channels.newOutputStream(new UnselectableByteChannel(channel));
    }

    /**
     * Bind channel to AF_UNIX address (unix file socket).
     * <p>
     * This method is used when creating a service listener (local server)
     * attached to the unix file socket.
     * </p>
     * @param address - AF_UNIX address pointing to the local file system socket file   
     */
    @Override
    public void bind(final SocketAddress address) throws IOException {
        if (Objects.nonNull(channel)) {
            if (isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (isBound()) {
                throw new SocketException("already bound");
            }
            try {
            	channel.bind(address);
                bound = true;
            } catch (IOException e) {
                throw (SocketException)new SocketException().initCause(e);
            }
        }
    }

    /**
     * Close socket and underlying channel
     */
    @Override
    public void close() throws IOException {
        if (Objects.nonNull(channel) && closed.compareAndSet(false, true)) {
            try {
            	bound = false;
            	channel.close();
            } catch (IOException e) {
                ignore();
            }
        }
    }

    /**
     * Connect to the AF_UNIX address (unix file socket).
     * <p>
     * This method is used when creating a client connection to AF_UNIX service. 
     * </p>
     * @param address - AF_UNIX address pointing to the local file system socket file   
     */
    @Override
    public void connect(final SocketAddress address) throws IOException {
        connect(address, 0);
    }

    /**
     * The same as connect(SocketAddress addr), timeout not implemented   
     */    
    @Override
    public void connect(final SocketAddress address, final int timeout) throws IOException {
    	channel.connect(address);
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public InetAddress getInetAddress() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (channel.isConnected()) {
            return in;
        } else {
            throw new IOException("not connected");
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (channel.isConnected()) {
            return out;
        } else {
            throw new IOException("not connected");
        }
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        try {
			return channel.getLocalAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    @Override
    public SocketAddress getRemoteSocketAddress() {
        SocketAddress address = null;
		try {
			address = channel.getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (Objects.nonNull(address)) {
            return address;
        } else {
            return null;
        }
    }

    @Override
    public boolean isBound() {
        if (Objects.isNull(channel)) {
            return false;
        }
        return bound && channel.isOpen();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return indown.get();
    }

    @Override
    public boolean isOutputShutdown() {
        return outdown.get();
    }

    @Override
    public void shutdownInput() throws IOException {
        if (indown.compareAndSet(false, true)) {
        	channel.shutdownInput();
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (outdown.compareAndSet(false, true)) {
        	channel.shutdownOutput();
        }
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
    	// not supported, do not throw
    	return false;
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        try {
            return channel.getOption(UnixSocketOptions.SO_RCVBUF).intValue();
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        try {
            return channel.getOption(UnixSocketOptions.SO_SNDBUF).intValue();
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSoTimeout() throws SocketException {
    	// not supported, do not throw
    	return 0;
    }

    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
    	// not supported, do not throw
    }

    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        try {
        	channel.setOption(UnixSocketOptions.SO_RCVBUF, Integer.valueOf(size));
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        try {
        	channel.setOption(UnixSocketOptions.SO_SNDBUF, Integer.valueOf(size));
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
    	// not supported, do not throw
    }

    private void ignore() {
    }

}
