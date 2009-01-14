///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SocketProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 The SocketProperties class represents a set of socket options the IBM Toolbox for Java will set on its client side sockets.  Setting the values on this object will not change any existing connection to the system.  The values retrieved from this object reflect only the values set into this object, not the properties of any actual connection to the system.
<p>Socket properties are described in the javadoc for the JDK's <tt>java.net.Socket</tt> class. See the JDK documentation for further details.
 **/
public class SocketProperties implements Serializable
{
  static final long serialVersionUID = 6818790247993750518L;

    boolean keepAliveSet_ = false;
    boolean keepAlive_ = false;
    boolean receiveBufferSizeSet_ = false;
    int receiveBufferSize_ = 0;
    boolean sendBufferSizeSet_ = false;
    int sendBufferSize_ = 0;
    boolean soLingerSet_ = false;
    int soLinger_ = 0;
    boolean soTimeoutSet_ = false;
    int soTimeout_ = 0;
    boolean tcpNoDelaySet_ = false;
    boolean tcpNoDelay_ = false;

    // Internal method to copy all the options from one object to another.
    void copyValues(SocketProperties properties)
    {
        keepAliveSet_ = properties.keepAliveSet_;
        keepAlive_ = properties.keepAlive_;
        receiveBufferSizeSet_ = properties.receiveBufferSizeSet_;
        receiveBufferSize_ = properties.receiveBufferSize_;
        sendBufferSizeSet_ = properties.sendBufferSizeSet_;
        sendBufferSize_ = properties.sendBufferSize_;
        soLingerSet_ = properties.soLingerSet_;
        soLinger_ = properties.soLinger_;
        soTimeoutSet_ = properties.soTimeoutSet_;
        soTimeout_ = properties.soTimeout_;
        tcpNoDelaySet_ = properties.tcpNoDelaySet_;
        tcpNoDelay_ = properties.tcpNoDelay_;
    }

    /**
     Indicates whether some other object is "equal to" this one.
     SocketProperties objects are considered equal if all of their property values match.
     @return  <tt>true</tt> if this object is equals the obj argument; false otherwise.
     **/
    public boolean equals(Object other)
    {
       try {
         SocketProperties props = (SocketProperties)other;
         if (keepAliveSet_         == props.keepAliveSet_ &&
             keepAlive_            == props.keepAlive_ &&
             receiveBufferSizeSet_ == props.receiveBufferSizeSet_ &&
             receiveBufferSize_    == props.receiveBufferSize_ &&
             sendBufferSizeSet_    == props.sendBufferSizeSet_ &&
             sendBufferSize_       == props.sendBufferSize_ &&
             soLingerSet_          == props.soLingerSet_ &&
             soLinger_             == props.soLinger_ &&
             soTimeoutSet_         == props.soTimeoutSet_ &&
             soTimeout_            == props.soTimeout_ &&
             tcpNoDelaySet_        == props.tcpNoDelaySet_ &&
             tcpNoDelay_           == props.tcpNoDelay_)
           return true;
         else return false;
      }
      catch (Throwable e) { return false; }
    }

  /**
   Returns a hash code value for the object.
   @return A hash code value for this object.
   **/
  public int hashCode()
  {
    // We must conform to the invariant that equal objects must have equal hashcodes.
    // Since we don't anticipate that instances of this class will ever get added into a HashMap/HashTable, we will simply return an arbitrary constant.
    return 99;
  }

    /**
     Indicates the value to which the SO_RCVBUF socket option is set.
     @return  The value of SO_RCVBUF, in bytes.
     **/
    public int getReceiveBufferSize()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting receive buffer size:", receiveBufferSize_);
        return receiveBufferSize_;
    }

    /**
     Indicates the value to which the SO_SNDBUF socket option is set.
     @return  The value of SO_SNDBUF, in bytes.
     **/
    public int getSendBufferSize()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting send buffer size:", sendBufferSize_);
        return sendBufferSize_;
    }

    /**
     Indicates the value to which the SO_LINGER socket option is set.
     @return  The value of SO_LINGER, in seconds.
     **/
    public int getSoLinger()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting so linger:", soLinger_);
        return soLinger_;
    }

    /**
     Indicates the value to which the SO_TIMEOUT socket option is set.
     @return  The value of SO_TIMEOUT, in milliseconds.
     **/
    public int getSoTimeout()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting so timeout:", soTimeout_);
        return soTimeout_;
    }

    /**
     Indicates the value to which the SO_KEEPALIVE socket option is set.
     @return  true if SO_KEEPALIVE is set; false otherwise.
     **/
    public boolean isKeepAlive()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if keep alive:", keepAlive_);
        return keepAlive_;
    }

    /**
     Indicates if the value of any of the socket options will be set.
     This method provides a quick way to see if this SocketProperties object has been altered (via a setter) such that it would cause at least one socket option to be set.
     @return  true if any option will be set; false otherwise.
     **/
    boolean isAnyOptionSet()
    {
        boolean result =
            keepAliveSet_ || receiveBufferSizeSet_ || sendBufferSizeSet_ ||
            soLingerSet_ || soTimeoutSet_ || tcpNoDelaySet_;
          
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if any option is set:", result);
        return result;
    }

    /**
     Indicates if the value of the SO_KEEPALIVE socket option will be set.
     @return  true if SO_KEEPALIVE will be set; false otherwise.
     **/
    public boolean isKeepAliveSet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if keep alive is set:", keepAliveSet_);
        return keepAliveSet_;
    }

    /**
     Indicates if the value of the SO_RCVBUF socket option will be set.
     @return  true if SO_RCVBUF will be set; false otherwise.
     **/
    public boolean isReceiveBufferSizeSet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting receive buffer size is set:", receiveBufferSizeSet_);
        return receiveBufferSizeSet_;
    }

    /**
     Indicates if the value of the SO_SNDBUF socket option will be set.
     @return  true if SO_SNDBUF will be set; false otherwise.
     **/
    public boolean isSendBufferSizeSet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting send buffer size is set:", sendBufferSizeSet_);
        return sendBufferSizeSet_;
    }

    /**
     Indicates if the value of the SO_LINGER socket option will be set.
     @return  true if SO_LINGER will be set; false otherwise.
     **/
    public boolean isSoLingerSet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting so linger is set:", soLingerSet_);
        return soLingerSet_;
    }

    /**
     Indicates if the value of the SO_TIMEOUT socket option will be set.
     @return  true if SO_TIMEOUT will be set; false otherwise.
     **/
    public boolean isSoTimeoutSet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting so timeout is set:", soTimeoutSet_);
        return soTimeoutSet_;
    }

    /**
     Indicates the value to which the TCP_NODELAY socket option is set.
     @return  true if TCP_NODELAY is set; false otherwise.
     **/
    public boolean isTcpNoDelay()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if TCP no delay:", tcpNoDelay_);
        return tcpNoDelay_;
    }

    /**
     Indicates if the value of the TCP_NODELAY socket option will be set.
     @return  true if TCP_NODELAY will be set; false otherwise.
     **/
    public boolean isTcpNoDelaySet()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if TCP no delay is set:", tcpNoDelaySet_);
        return tcpNoDelaySet_;
    }

    /**
     Indicates the value to which the SO_KEEPALIVE socket option should be set.
     @param  keepAlive  true to set SO_KEEPALIVE; false otherwise.
     **/
    public void setKeepAlive(boolean keepAlive)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting keep alive:", keepAlive);
        keepAliveSet_ = true;
        keepAlive_ = keepAlive;
    }

    /**
     Indicates the value to which the SO_RCVBUF socket option should be set.
     @param  receiveBufferSize  The value to set SO_RCVBUF, in bytes.
     **/
    public void setReceiveBufferSize(int receiveBufferSize)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting receive buffer size:", receiveBufferSize);
        receiveBufferSizeSet_ = true;
        receiveBufferSize_ = receiveBufferSize;
    }

    /**
     Indicates the value to which the SO_SNDBUF socket option should be set.
     @param  sendBufferSize  The value to set SO_SNDBUF, in bytes.
     **/
    public void setSendBufferSize(int sendBufferSize)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting send buffer size:", sendBufferSize);
        sendBufferSizeSet_ = true;
        sendBufferSize_ = sendBufferSize;
    }

    /**
     Indicates the value to which the SO_LINGER socket option should be set.
     @param  soLinger  The value to set SO_LINGER, in seconds.
     **/
    public void setSoLinger(int soLinger)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting so linger:", soLinger);
        soLingerSet_ = true;
        soLinger_ = soLinger;
    }

    /**
     Indicates the value to which the SO_TIMEOUT socket option should be set.
     @param  soTimeout  The value to set SO_TIMEOUT, in milliseconds.
     **/
    public void setSoTimeout(int soTimeout)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting so timeout:", soTimeout);
        soTimeoutSet_ = true;
        soTimeout_ = soTimeout;
    }

    /**
     Indicates the value to which the TCP_NODELAY socket option should be set.
     @param  tcpNoDelay  true to set TCP_NODELAY; false otherwise.
     **/
    public void setTcpNoDelay(boolean tcpNoDelay)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting TCP no delay:", tcpNoDelay);
        tcpNoDelaySet_ = true;
        tcpNoDelay_ = tcpNoDelay;
    }

    /**
     Indicates that the value of the SO_KEEPALIVE socket option should not be set.
     **/
    public void unsetKeepAlive()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting keep alive.");
        keepAliveSet_ = false;
    }

    /**
     Indicates that the value of the SO_RCVBUF socket option should not be set.
     **/
    public void unsetReceiveBufferSize()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting receive buffer size.");
        receiveBufferSizeSet_ = false;
    }

    /**
     Indicates that the value of the SO_SNDBUF socket option should not be set.
     **/
    public void unsetSendBufferSize()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting send buffer size.");
        sendBufferSizeSet_ = false;
    }

    /**
     Indicates that the value of the SO_LINGER socket option should not be set.
     **/
    public void unsetSoLinger()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting so linger.");
        soLingerSet_ = false;
    }

    /**
     Indicates that the value of the SO_TIMEOUT socket option should not be set.
     **/
    public void unsetSoTimeout()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting so timeout.");
        soTimeoutSet_ = false;
    }

    /**
     Indicates that the value of the TCP_NODELAY socket option should not be set.
     **/
    public void unsetTcpNoDelay()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unsetting TCP no delay.");
        tcpNoDelaySet_ = false;
    }
}
