///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UnixSocketOptions.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.socket;

import java.net.SocketOption;

/*
 * Based on original source
 * https://github.com/jnr/jnr-unixsocket/blob/master/src/main/java/jnr/unixsocket/UnixSocketOptions.java
 */

/**
 * Defines common socket options for AF_UNIX sockets.
 */
final class UnixSocketOptions {

    private static class GenericOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;
        GenericOption(final String name, final Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override public String name() { return name; }
        @Override public Class<T> type() { return type; }
        @Override public String toString() { return name; }
    }

    /**
     * Get/Set size of the socket send buffer.
     */
    public static final SocketOption<Integer> SO_SNDBUF =
        new GenericOption<Integer>("SO_SNDBUF", Integer.class);

    /**
     * Get/Set send timeout.
     */
    public static final SocketOption<Integer> SO_SNDTIMEO =
        new GenericOption<Integer>("SO_SNDTIMEO", Integer.class);

    /**
     * Get/Set size of the socket receive buffer.
     */
    public static final SocketOption<Integer> SO_RCVBUF =
        new GenericOption<Integer>("SO_RCVBUF", Integer.class);

    /**
     * Get/Set receive timeout.
     */
    public static final SocketOption<Integer> SO_RCVTIMEO =
        new GenericOption<Integer>("SO_RCVTIMEO", Integer.class);

    /**
     * Keep connection alive.
     */
    public static final SocketOption<Boolean> SO_KEEPALIVE =
        new GenericOption<Boolean>("SO_KEEPALIVE", Boolean.class);

    /**
     * Enable credential transmission.
     */
    public static final SocketOption<Boolean> SO_PASSCRED =
        new GenericOption<Boolean>("SO_PASSCRED", Boolean.class);

}

