///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  LocalDataArea.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;

// There is always a local data area associated with the current server job.
// It is 1024 bytes in length and is accessed using the name "*LDA". Its text
// description is "*LDA FOR JOB jobnumber/username/jobname".
// The server allows only read and write operations on a local data area.

/**
The LocalDataArea class represents a local data area on the server.
<p>
A local data area exists as a character data area on the server. It is
automatically associated with a job and cannot be accessed from another
job; hence, it cannot be directly created or deleted by the user.
<p>
Care must be taken when using local data areas so that the server job
is not ended prematurely. When the job ends, its local data area is
automatically deleted, at which point the LocalDataArea object that
is referencing it will no longer be valid.
<p>
The following example demonstrates the use of LocalDataArea:
<pre>
// Prepare to work with the server named "My400".
AS400 system = new AS400("My400");

// Create a LocalDataArea object to access
// the local data area associated with this connection.
LocalDataArea dataArea = new LocalDataArea(system);

// Clear the data area
dataArea.clear();

// Write to the data area
dataArea.write("Hello world");

// Read from the data area
String data = dataArea.read();
</pre>
**/

public class LocalDataArea extends DataArea implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  


    static final long serialVersionUID = 4L;

   /**
    Constants
   **/

   static final int DEFAULT_LENGTH = 1024;
   static final String DEFAULT_PATH = "/QSYS.LIB/          .LIB/*LDA.DTAARA";


   /**
    Variables
   ***/


   /**
    Constructs a LocalDataArea object.
    It creates a default LocalDataArea object.  The <i>system</i> property
    must be set before attempting a connection.
   **/
   public LocalDataArea()
   {
     super();

     try { super.setPath(DEFAULT_PATH); }
     catch (PropertyVetoException e) {} // Will never happen.

     length_ = DEFAULT_LENGTH;
     dataAreaType_ = LOCAL_DATA_AREA;
   }


   /**
   Constructs a LocalDataArea object.
   It creates a LocalDataArea instance that represents the local data area
   on <i>system</i>.
      @param system The server that contains the data area.
   **/
   public LocalDataArea(AS400 system)
   {
     // See if we can squeeze a local data area past QSYSObjectPathName:
     // The library name must be 10 blanks.
     // The data area name must be *LDA
     super(system, DEFAULT_PATH);
     length_ = DEFAULT_LENGTH;
     dataAreaType_ = LOCAL_DATA_AREA;
   }


   /**
   Resets the data area to contain all blanks.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public void clear()
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.clear();

     // Fire the CLEARED event.
     fireCleared();
   }


   /**
   Reads the data from the data area.
   It retrieves the entire contents of the data area. Note that if the data
   does not completely fill the data area, this method will return data
   containing trailing blanks up to the length of the data area.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the server object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public String read()
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
      String val = impl_.retrieve(-1,1);                        
      
      // Fire the READ event.
      fireRead();
      
      return val;
   }


   /**
   Reads the data from the data area.
   It retrieves the entire contents of the data area. Note that if the data
   does not completely fill the data area, this method will return data
   containing trailing blanks up to the length of the data area.
     @param type The Local Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the server object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public String read(int type)                                      //$A2A
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
     String val = impl_.retrieve(-1,1, type);                        //$A2C

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
   Reads the data from the data area.
   It retrieves <i>dataLength</i> characters beginning at
   <i>dataAreaOffset</i> in the data area. The first character in
   the data area is at offset 0.
     @param dataAreaOffset The offset in the data area at which to start reading.
     @param dataLength The number of characters to read. Valid values are from
            1 through (data area size - <i>dataAreaOffset</i>).
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the server object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public String read(int dataAreaOffset, int dataLength)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
      // Validate the dataAreaOffset parameter.
     if (dataAreaOffset < 0 || dataAreaOffset >= length_)
       throw new ExtendedIllegalArgumentException("dataAreaOffset",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the dataLength parameter.
     if (dataLength < 1 || dataLength > length_)
       throw new ExtendedIllegalArgumentException("dataLength",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the (dataAreaOffset, dataLength) combination.
     if (dataAreaOffset+dataLength > length_)
       throw new ExtendedIllegalArgumentException("dataLength",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

     if (impl_ == null)
       chooseImpl();

     // Do the read
     String val = impl_.retrieve(dataAreaOffset+1, dataLength);

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
   Reads the data from the data area.
   It retrieves <i>dataLength</i> characters beginning at
   <i>dataAreaOffset</i> in the data area. The first character in
   the data area is at offset 0.
     @param dataAreaOffset The offset in the data area at which to start reading.
     @param dataLength The number of characters to read. Valid values are from
            1 through (data area size - <i>dataAreaOffset</i>).
     @param type The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the server object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public String read(int dataAreaOffset, int dataLength, int type)  //$A2A
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
     // Validate the dataAreaOffset parameter.
     if (dataAreaOffset < 0 || dataAreaOffset >= length_)
       throw new ExtendedIllegalArgumentException("dataAreaOffset",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the dataLength parameter.
     if (dataLength < 1 || dataLength > length_)
       throw new ExtendedIllegalArgumentException("dataLength",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the (dataAreaOffset, dataLength) combination.
     if (dataAreaOffset+dataLength > length_)
       throw new ExtendedIllegalArgumentException("dataLength",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

     if (impl_ == null)
       chooseImpl();

     // Do the read
     String val = impl_.retrieve(dataAreaOffset+1, dataLength, type);  //$A2C

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
   Writes the data to the data area.
   It writes <i>data</i> to the beginning of the data area. The remaining
   characters in the data area are blank padded.
     @param data The data to be written.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public void write(String data)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
   {
     // Validate the data parameter.
     if (data == null)
       throw new NullPointerException("data");
     if (data.length() < 1 || data.length() > length_)
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

     if (impl_ == null)
       chooseImpl();

     // Do the write
     impl_.write(data, 0);

     // Fire the WRITTEN event.
     fireWritten();
   }


   /**
   Writes the data to the data area.
   It writes <i>data.length()</i> characters from <i>data</i> to the
   data area beginning at <i>dataAreaOffset</i>. The first character
   in the data area is at offset 0.
     @param data The data to be written.
     @param dataAreaOffset The offset in the data area at which to start writing.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public void write(String data, int dataAreaOffset)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
   {
      // Validate the data parameter.
     if (data == null)
       throw new NullPointerException("data");
     // Validate the data length.
     if (data.length() < 1 || data.length() > length_)
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     // Validate the dataAreaOffset parameter.
     if (dataAreaOffset < 0 || dataAreaOffset >= length_)
       throw new ExtendedIllegalArgumentException("dataAreaOffset",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the (dataAreaOffset, dataLength) combination.
     if (dataAreaOffset+data.length() > length_)
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

     if (impl_ == null)
       chooseImpl();

     // Do the write
     impl_.write(data, dataAreaOffset);

     // Fire the WRITTEN event.
     fireWritten();
   }


   /**
   Writes the data to the data area.
   It writes <i>data.length()</i> characters from <i>data</i> to the
   data area beginning at <i>dataAreaOffset</i>. The first character
   in the data area is at offset 0.
     @param data The data to be written.
     @param dataAreaOffset The offset in the data area at which to start writing.
     @param type The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the server.
     @exception ObjectDoesNotExistException     If the server object does not exist.
   **/
   public void write(String data, int dataAreaOffset, int type)      //$A2A
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
   {
     // Validate the data parameter.
     if (data == null)
       throw new NullPointerException("data");
     // Validate the data length.
     if (data.length() < 1 || data.length() > length_)
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     // Validate the dataAreaOffset parameter.
     if (dataAreaOffset < 0 || dataAreaOffset >= length_)
       throw new ExtendedIllegalArgumentException("dataAreaOffset",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the (dataAreaOffset, dataLength) combination.
     if (dataAreaOffset+data.length() > length_)
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

     if (impl_ == null)
       chooseImpl();

     // Do the write
     impl_.write(data, dataAreaOffset, type);

     // Fire the WRITTEN event.
     fireWritten();
   }
}
