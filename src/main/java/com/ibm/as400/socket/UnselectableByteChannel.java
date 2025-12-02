/*
 * Copyright (C) 2015, 2026 Green Screens Ltd.
 */
package com.ibm.as400.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * A byte channel that doesn't implement {@link SelectableChannel}. Though
 * that type isn't in the public API, if the channel passed in implements
 * that interface then unwanted synchronization is performed which can harm
 * concurrency and can cause deadlocks.
 *
 * https://bugs.openjdk.java.net/browse/JDK-4774871
 */
final class UnselectableByteChannel implements ReadableByteChannel, WritableByteChannel {
	
    private final SocketChannel channel;

    UnselectableByteChannel(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return channel.write(src);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return channel.read(dst);
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}