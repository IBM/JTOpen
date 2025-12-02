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
import java.util.concurrent.atomic.AtomicBoolean;

public class UnixSocket extends java.net.Socket {

    private SocketChannel chan;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean indown = new AtomicBoolean(false);
    private AtomicBoolean outdown = new AtomicBoolean(false);

    private InputStream in;
    private OutputStream out;
    private boolean bound = false;

    public UnixSocket(final SocketChannel chan) {
        this.chan = chan;
        in = Channels.newInputStream(new UnselectableByteChannel(chan));
        out = Channels.newOutputStream(new UnselectableByteChannel(chan));
    }

    @Override
    public void bind(final SocketAddress local) throws IOException {
        if (null != chan) {
            if (isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (isBound()) {
                throw new SocketException("already bound");
            }
            try {
                chan.bind(local);
                bound = true;
            } catch (IOException e) {
                throw (SocketException)new SocketException().initCause(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (null != chan && closed.compareAndSet(false, true)) {
            try {
            	bound = false;
                chan.close();
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
    	chan.connect(addr);
    }

    @Override
    public SocketChannel getChannel() {
        return chan;
    }

    @Override
    public InetAddress getInetAddress() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (chan.isConnected()) {
            return in;
        } else {
            throw new IOException("not connected");
        }
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        try {
			return chan.getLocalAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (chan.isConnected()) {
            return out;
        } else {
            throw new IOException("not connected");
        }
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        SocketAddress address = null;
		try {
			address = chan.getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (address != null) {
            return address;
        } else {
            return null;
        }
    }

    @Override
    public boolean isBound() {
        if (null == chan) {
            return false;
        }
        return bound && chan.isOpen();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public boolean isConnected() {
        return chan.isConnected();
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
            chan.shutdownInput();
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (outdown.compareAndSet(false, true)) {
            chan.shutdownOutput();
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
            return chan.getOption(UnixSocketOptions.SO_RCVBUF).intValue();
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        try {
            return chan.getOption(UnixSocketOptions.SO_SNDBUF).intValue();
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
            chan.setOption(UnixSocketOptions.SO_RCVBUF, Integer.valueOf(size));
        } catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        try {
            chan.setOption(UnixSocketOptions.SO_SNDBUF, Integer.valueOf(size));
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
