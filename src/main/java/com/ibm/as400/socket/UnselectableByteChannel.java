///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UnselectableByteChannel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

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

    UnselectableByteChannel(final SocketChannel channel) {
    	if (Objects.isNull(channel)) throw new NullPointerException("Channel not defined");
        this.channel = channel;
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        return channel.write(src);
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
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