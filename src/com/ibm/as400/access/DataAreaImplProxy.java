///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataAreaImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

/**
Provides implementations of the public Data Area classes,
when used on a client communicating with a proxy server:
 <ul compact>
 <li>DataArea (abstract base class)
 <li>CharacterDataArea
 <li>DecimalDataArea
 <li>LocalDataArea
 <li>LogicalDataArea
 </ul>
**/

class DataAreaImplProxy
extends AbstractProxyImpl
implements DataAreaImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  DataAreaImplProxy ()
  {
    super ("DataArea");
  }


   /**
   Resets the data area to contain all blanks.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void clear()
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "clear");
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Creates a character data area with the specified attributes.
     @param length The maximum number of characters in the data area.
            Valid values are 1 through 2000.
     @param initialValue The initial value for the data area.
     @param textDescription The text description for the data area.
            The maximum length is 50 characters.
     @param authority The public authority level for the data area. Valid
            values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name
            of an authorization list. The maximum length is 10 characters.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectAlreadyExistsException    If the AS/400 object already exists.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void create(int length, String initialValue,
                      String textDescription, String authority)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "create",
                         new Class[] { Integer.TYPE, String.class,
                                       String.class, String.class},
                         new Object[] { new Integer(length), initialValue,
                                        textDescription, authority});
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow6 (e);
     }
   }


   /**
   Creates a decimal data area with the specified attributes.
     @param length The maximum number of digits in the data area. Valid
            values are 1 through 24.
     @param decimalPositions The number of digits to the right of the decimal
            point. Valid values are 0 through 9.
     @param initialValue The initial value for the data area.
     @param textDescription The text description for the data area. The maximum length is 50 characters.
     @param authority The public authority level for the data area. Valid
            values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the
            name of an authorization list. The maximum length is 10 characters.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectAlreadyExistsException    If the AS/400 object already exists.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void create(int length, int decimalPositions,
                      BigDecimal initialValue, String textDescription,
                      String authority)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "create",
                         new Class[] { Integer.TYPE,
                                       Integer.TYPE,
                                       BigDecimal.class,
                                       String.class, String.class},
                         new Object[] { new Integer(length),
                                        new Integer(decimalPositions),
                                        initialValue,
                                        textDescription, authority});
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow6 (e);
     }
   }



   /**
   Creates a logical data area with the specified attributes.
     @param initialValue The initial value for the data area.
     @param textDescription The text description for the data area. The maximum length is 50 characters.
     @param authority The public authority level for the data area. Valid
            values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the
            name of an authorization list. The maximum length is 10 characters.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectAlreadyExistsException    If the AS/400 object already exists.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void create(boolean initialValue, String textDescription,
                      String authority)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "create",
                         new Class[] { Boolean.TYPE,
                                       String.class, String.class},
                         new Object[] { new Boolean(initialValue),
                                        textDescription, authority});
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow6 (e);
     }
   }


   /**
   Removes the data area from the system. Note this method is NOT public.
   It is overridden as a public method in the subclasses that use it.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception ObjectDoesNotExistException     If the object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
     @exception IOException                     If an error occurs while communicating with the AS/400.
   **/
   public void delete()
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "delete");
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Returns the number of digits to the right of the decimal point in this data area.
     @return The number of decimal positions.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public int getDecimalPositions()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       return connection_.callMethod (pxId_, "getDecimalPositions")
                         .getReturnValueInt();
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
     Returns the size of the data area.
        @return The size of the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public int getLength()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       return connection_.callMethod (pxId_, "getLength")
                         .getReturnValueInt();
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Returns the data read from the data area.
     @return The decimal data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   // Note that doing a read() will also set the attributes of this
   // object to what is returned from the 400, namely the length and
   // number of decimal positions.
   public BigDecimal readBigDecimal()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       return (BigDecimal) connection_.callMethod (pxId_, "readBigDecimal")
                                      .getReturnValue ();
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Returns the value in the data area.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public boolean readBoolean()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       return connection_.callMethod (pxId_, "readBoolean")
                         .getReturnValueBoolean();
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Refreshes the attributes of the data area.
   This method should be called if the underlying AS/400 data area has changed
   and it is desired that this object should reflect those changes.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public void refreshAttributes()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       connection_.callMethod (pxId_, "refreshAttributes");
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Makes the API call to retrieve the data area data and attributes.
     @return The String value read from the data area as a result of
     retrieving the data area's attributes.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public String retrieve(int dataAreaOffset, int dataLength)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException
   {
     try {
       return (String) connection_.callMethod (pxId_, "retrieve",
                         new Class[] { Integer.TYPE, Integer.TYPE },
                         new Object[] { new Integer(dataAreaOffset), new Integer(dataLength) })
                       .getReturnValue ();
     }
     catch (InvocationTargetException e) {
       Throwable e2 = e.getTargetException ();
       if (e2 instanceof IllegalObjectTypeException)
         throw (IllegalObjectTypeException) e2;
       else
         throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Sets the system, path, and data area type.
   **/
   public void setAttributes(AS400Impl system, QSYSObjectPathName path, int dataAreaType)
     throws IOException
   {
     try {
       connection_.callMethod (pxId_, "setAttributes",
                               new Class[] { AS400Impl.class,
                                             QSYSObjectPathName.class,
                                             Integer.TYPE },
                               new Object[] { system,
                                              path,
                                              new Integer(dataAreaType)
                                            });
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow1 (e);
     }
   }


   /**
   Writes the data to the data area.
   It writes <i>data.length()</i> characters from <i>data</i> to the
   data area beginning at <i>dataAreaOffset</i>. The first character
   in the data area is at offset 0.
     @param data The data to be written.
     @param dataAreaOffset The offset in the data area at which to start writing.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void write(String data, int dataAreaOffset)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "write",
                         new Class[] { String.class, Integer.TYPE },
                         new Object[] { data, new Integer(dataAreaOffset) });
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Writes <i>data</i> to the data area.
     @param data The decimal data to be written.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void write(BigDecimal data)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "write",
                         new Class[] { BigDecimal.class },
                         new Object[] { data });
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow5 (e);
     }
   }


   /**
   Writes the value in <i>data</i> to the data area.
     @param data The data to be written.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void write(boolean data)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              ObjectDoesNotExistException,
              IOException
   {
     try {
       connection_.callMethod (pxId_, "write",
                         new Class[] { Boolean.TYPE },
                         new Object[] { new Boolean(data) });
     }
     catch (InvocationTargetException e) {
       throw ProxyClientConnection.rethrow5 (e);
     }
   }

}
