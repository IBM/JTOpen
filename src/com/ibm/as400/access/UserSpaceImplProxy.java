///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserSpaceImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

/**
The UserSpaceImplProxy class is an implementation of the UserSpace
class used on a client communicating with a proxy server.
**/
class UserSpaceImplProxy
extends UserSpaceImpl implements ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



   // Private data.
    private long                    proxyId_;
    private ProxyClientConnection   connection_;
    //UserSpace return arguments
    private static final boolean[] args = new boolean[] {true, false, false, false};


/**
Constructs an object on the proxy server.

@param connection   The connection.
@param system       The system.
**/
    public void construct (ProxyClientConnection connection)
    {
        connection_ = connection;
        try {
            proxyId_ = connection_.callConstructor ("UserSpace");

        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }


/**
Cleans up the object.
**/
    protected void finalize ()
        throws Throwable
    {
        super.finalize();
        connection_.callFinalize (proxyId_);

    }

/**
Closes the user space's random access file stream and releases any system resources associated with the stream.
**/
   void close() throws IOException
   {
      try {
         connection_.callMethod (proxyId_, "close");

      }
      catch (InvocationTargetException e) {
         Throwable e2 = e.getTargetException ();
         if (e2 instanceof IOException) {
            throw (IOException) e2;
         }
         else {
            throw ProxyClientConnection.rethrow(e);
         }
      }
   }


/**
Creates the user space.

@param domain  The domain into which the user space is created.
      Valid value are: *DEFAULT, *USER, or *SYSTEM.
      *DEFAULT uses the allow user domain system value to determine if *USER or *SYSTEM will be used.
@param length  The initial size in bytes of the user space.
      Valid values are 1 through 16,776,704.
@param replace The value indicating if an existing user space is to be replaced.
@param extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
@param initialValue  The value used in creation and extension.
@param textDescription  The text describing the user space.  This string must be 50 characters or less.
@param authority  The authority given to users.  This string must be 10 characters or less.
      Valid values are:
      <ul>
          <li>*ALL
          <li>*CHANGE
          <li>*EXCLUDE
          <li>*LIBCRTAUT
          <li>*USE
          <li>authorization-list name
      </ul>

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void create(String domain, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority)
             throws AS400SecurityException,
                    ErrorCompletingRequestException,
                    InterruptedException,
                    IOException,
                    ObjectDoesNotExistException
    {
       try {
          connection_.callMethod (proxyId_, "create",
                                        new Class[] { String.class, Integer.TYPE, Boolean.TYPE, String.class, Byte.TYPE, String.class, String.class },
                                        new Object[] { domain, new Integer (length), new Boolean (replace), extendedAttribute, new Byte (initialValue), textDescription, authority });
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Deletes the user space.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void delete()
         throws AS400SecurityException,
                    ErrorCompletingRequestException,
                    InterruptedException,
                    IOException,
                    ObjectDoesNotExistException
    {
       try {
            connection_.callMethod (proxyId_, "delete");
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Returns the initial value used for filling in the user space during creation and extension.

@return The initial value used during user space creation and extension.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    byte getInitialValue()
           throws AS400SecurityException,
                 ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            return (byte) connection_.callMethod (proxyId_, "getInitialValue").getReturnValueByte ();
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Returns the size in bytes of the user space.

@return The size in bytes of the user space.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    int getLength()
           throws AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            return connection_.callMethod (proxyId_, "getLength").getReturnValueInt ();
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


    public long getPxId()
    {
       return proxyId_;

    }


/**
Indicates if the user space is auto extendible.  When running on a workstation the auto extend
attribute is always true and cannot be changed, so the attribute value returned should be ignored.
The auto extend attribute can be used when running on the AS/400's JVM with the optimizations that
are a part of OS/400.

@return true if the user space is auto extendible; false otherwise.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    boolean isAutoExtendible()
           throws AS400SecurityException,
                 ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            return connection_.callMethod (proxyId_, "isAutoExtendible").getReturnValueBoolean ();
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>
  beginning at <i>dataOffset</i>.


@param dataBuffer  The position in the data buffer at which results will be place.
@param userSpaceOffset  The position in the user space from which to start reading.
@param dataOffset  The data starting position for the results of the read.
@param length  The number of bytes to be read.

@return The total number of bytes read into the buffer, or -1 if there is no more data because the end of user space has been reached.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    int read(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length)
         throws AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException
    {
       try {
            ProxyReturnValue rv = connection_.callMethod (proxyId_, "read",
                                                          new Class[] { byte[].class, Integer.TYPE,
                                                                        Integer.TYPE, Integer.TYPE },
                                                          new Object[] { dataBuffer,
                                                                         new Integer (userSpaceOffset),
                                                                         new Integer (dataOffset),
                                                                         new Integer (length) },
                                                          args,
                                                          false);
            byte [] returnDataB = (byte[])rv.getArgument(0);
            for (int i=0; i<dataBuffer.length; i++) {
               dataBuffer[i] = returnDataB[i];
            }
            return rv.getReturnValueInt();
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Sets the auto extend attribute if possible.  When running on a workstation the Toolbox cannot
set the auto extend attribute so this method is ignored and auto extend is always true.  Auto
extend can be set when running on the AS/400's JVM with the optimizations that are a part of
OS/400.

@param autoExtendibility  The attribute for user space auto extendibility.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void setAutoExtendible(boolean autoExtendibility)
           throws AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            connection_.callMethod (proxyId_, "setAutoExtendible",
                                        new Class[] { Boolean.TYPE },
                                        new Object[] { new Boolean (autoExtendibility) });
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }



    void setConverter(ConverterImpl converter)                              // @C1C
    {
       try {
            connection_.callMethod (proxyId_, "setConverter",
                                    new Class[] { ConverterImpl.class },    // @C1C
                                    new Object[] { converter });
            converter_ = converter;
       }
       catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow(e);
       }

    }


/**
Sets the initial value to be used during user space creation or extension.

@param initialValue  The new initial value used during future extensions.
        For best performance set byte to hexadecimal zeros.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void setInitialValue(byte initialValue)
           throws AS400SecurityException,
                 ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            connection_.callMethod (proxyId_, "setInitialValue",
                                        new Class[] { Byte.TYPE },
                                        new Object[] { new Byte (initialValue) });
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


/**
Sets the size of the user space.  Valid values are 1 through 16,776,704.

@param length  The new size of the user space.

@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void setLength(int length)
           throws AS400SecurityException,
                 ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                  ObjectDoesNotExistException
    {
       try {
            connection_.callMethod (proxyId_, "setLength",
                                        new Class[] { Integer.TYPE },
                                        new Object[] { new Integer (length) });
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }


    // @E1 new method
    void setMustUseProgramCall(boolean value)
    {
       try {
            connection_.callMethod (proxyId_, "setMustUseProgramCall",
                                        new Class[] { Boolean.TYPE },
                                        new Object[] { new Boolean (value) });
        }
        catch (InvocationTargetException e) { throw ProxyClientConnection.rethrow (e); }

   }





/**
Sets the user space name used in API Program Call.
**/
    public void setName()
         throws UnsupportedEncodingException
    {
//       super.setName();
       try {
          connection_.callMethod (proxyId_, "setName");
       }
       catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow(e);
       }
    }



/**
Sets the path for the user space.
The path can only be set before a connection has been established.

@param path  The fully qualified integrated file system path name.
**/
    public void setPath(String path)
    {
       super.setPath(path);
       try {
          connection_.callMethod (proxyId_, "setPath",
                                       new Class[] { String.class },
                                       new Object[] { path });
       }
       catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow(e);
       }
    }


/**
Sets the AS/400 to run the program.

@param system  The AS/400 on which to run the program.
**/
    public void setSystem (AS400Impl system)
    {
        //super.setSystem(system);
        try {
            connection_.callMethod (proxyId_, "setSystem",
                                        new Class[] { AS400Impl.class },
                                        new Object[] { system });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }


/**
Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user
  space beginning at <i>userSpaceOffset</i>.

@param dataBuffer  The data buffer to be written to the user space.
@param userSpaceOffset  The position in the user space to start writing.
@param dataOffset  The position in the write data buffer from which to start copying.
@param length  The length of data to be written.
@param forceAuxiliary  The method of forcing changes made to the user space to
        auxiliary storage.  Valid values are:
        <UL>
           <LI>FORCE_NONE
           <LI>FORCE_ASYNCHRONOUS
           <LI>FORCE_SYNCHRONOUS
        </UL>
@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException If this thread is interrupted.
@exception IOException If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException If the AS400 object does not exist.
**/
    void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary)
         throws AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException
    {
       try {
            connection_.callMethod (proxyId_, "write",
                                        new Class[] { byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE },
                                        new Object[] { dataBuffer, new Integer (userSpaceOffset), new Integer (dataOffset), new Integer (length), new Integer (forceAuxiliary) });
        }
        catch (InvocationTargetException e) {
           throw ProxyClientConnection.rethrow5 (e);
        }
    }
}

