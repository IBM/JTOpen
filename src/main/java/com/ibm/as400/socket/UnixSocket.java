/*
 * Copyright (C) 2015, 2026 Green Screens Ltd.
 */
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

    @Override
    public void bind(final SocketAddress local) throws IOException {
        if (Objects.nonNull(channel)) {
            if (isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (isBound()) {
                throw new SocketException("already bound");
            }
            try {
            	channel.bind(local);
                bound = true;
            } catch (IOException e) {
                throw (SocketException)new SocketException().initCause(e);
            }
        }
    }

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

    @Override
    public void connect(final SocketAddress addr) throws IOException {
        connect(addr, 0);
    }

    @Override
    public void connect(final SocketAddress addr, final int timeout) throws IOException {
    	channel.connect(addr);
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
    public SocketAddress getLocalSocketAddress() {
        try {
			return channel.getLocalAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
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
