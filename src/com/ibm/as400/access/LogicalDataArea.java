///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: LogicalDataArea.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;

/**
The LogicalDataArea class represents a logical data area on the AS/400.
<p>
The following example demonstrates the use of LogicalDataArea:
<pre>
// Prepare to work with the AS/400 system named "My400".
AS400 system = new AS400("My400");

// Create a LogicalDataArea object.
QSYSObjectPathName path = new QSYSObjectPathName("MYLIB", "MYDATA", "DTAARA");
LogicalDataArea dataArea = new LogicalDataArea(system, path.getPath());

// Create the logical data area on the AS/400 using default values.
dataArea.create();

// Clear the data area.
dataArea.clear();

// Write to the data area.
dataArea.write(true);

// Read from the data area.
boolean data = dataArea.read();

// Delete the data area from the AS/400.
dataArea.delete();
</pre>
**/

public class LogicalDataArea extends DataArea implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   

   /**
    Constants
   **/

   static final int DEFAULT_LENGTH = 1;


   /**
    Variables
   ***/

   private boolean initialValue_ = false; // The initial value written to the data area upon creation.


   /**
    Constructs a LogicalDataArea object.
    It creates a default LogicalDataArea object.  The <i>system</i> and <i>path</i>
    properties must be set before attempting a connection.
   **/
   public LogicalDataArea()
   {
     super();
     length_ = DEFAULT_LENGTH;
     dataAreaType_ = LOGICAL_DATA_AREA;
   }


   /**
   Constructs a LogicalDataArea object.
   It creates a LogicalDataArea instance that represents the data area <i>path</i>
   on <i>system</i>.
      @param system The AS/400 that contains the data area.
      @param path The fully qualified integrated file system path name. The
             integrated file system file extension for a data area is DTAARA. An example of a
             fully qualified integrated file system path to a data area "MYDATA" in library
             "MYLIB" is: /QSYS.LIB/MYLIB.LIB/MYDATA.DTAARA
   **/
   public LogicalDataArea(AS400 system, String path)
   {
     super(system, path);
     length_ = DEFAULT_LENGTH;
     dataAreaType_ = LOGICAL_DATA_AREA;
   }


   /**
   Clears the data area.
   This method resets the data area to contain the default value of false.
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
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.clear();

     // Fire the CLEARED event.
     fireCleared();
   }


   /**
   Creates a logical data area on the AS/400.
   This method uses the following default property values.
   <ul>
   <li>initialValue - A value of false.
   <li>textDescription - A blank string.
   <li>authority - A value of *LIBCRTAUT.
   </ul>
   Note the <i>length</i> of a LogicalDataArea is always 1.
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
   public void create()
       throws AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.create(initialValue_, textDescription_, authority_);

     // Fire the CREATED event.
     fireCreated();
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
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     // Validate the text description parameter
     if (textDescription == null)
       throw new NullPointerException("textDescription");
     if (textDescription.length() > 50)
       throw new ExtendedIllegalArgumentException("textDescription",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     // Validate the authority parameter.
     if (authority == null)
       throw new NullPointerException("authority");
     if (authority.length() == 0 || authority.length() > 10)
       throw new ExtendedIllegalArgumentException("authority",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

     initialValue_ = initialValue;
     textDescription_ = textDescription;
     authority_ = authority;

     create();
   }


   /**
   Removes the data area from the system.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   public void delete()
       throws AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     super.delete();
   }


   /**
   Returns the integrated file system path name of the object represented by the data area.
      @return The integrated file system path name of the object represented by the data area.
   **/
   public String getPath()
   {
     return super.getPath();
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
   public boolean read()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     // Do the read
     boolean data = impl_.readBoolean();

     fireRead(); // Fire the READ event.

     return data;
   }


   /**
   Sets the fully qualified data area name.
   The following example demonstrates the use of setPath:
   <pre>
   // Create a LogicalDataArea object.
   LogicalDataArea dataArea = new LogicalDataArea();

   // Set its path to be the data area "MYDATA" in the library "MYLIB".
   dataArea.setPath("/QSYS.LIB/MYLIB.LIB/MYDATA.DTAARA");
   </pre>
     @param path The fully qualified integrated file system path name of the data area.
     @exception PropertyVetoException If the change is vetoed.
   **/
   public void setPath(String path) throws PropertyVetoException
   {
     super.setPath(path);
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
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.write(data);

     // Fire the WRITTEN event.
     fireWritten();
   }
}
