/*
 * Copyright (C) 2015, 2026 Green Screens Ltd.
 */

package com.ibm.as400.socket;

import java.net.SocketOption;

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

