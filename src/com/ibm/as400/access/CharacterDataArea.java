///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CharacterDataArea.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// @A1 - 07/25/2007 - Allow users to write data containing single quote 
//                    characters.  User needs to specify a pair of single-quote 
//                    characters to be interpretted as a single quote character.
//                    This would have resulted in potential length errors
//                    being reported by toolbox code.  Therefore, some toolbox
//                    length verification has been removed.  The i5/OS API's
//                    will report an error if the data length is invalid.
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.Serializable;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;

/**
The CharacterDataArea class represents a character data area on the system.
<p>
The following example demonstrates the use of CharacterDataArea:
<pre>
// Prepare to work with the system named "My400".
AS400 system = new AS400("My400");

// Create a CharacterDataArea object.
QSYSObjectPathName path = new QSYSObjectPathName("MYLIB", "MYDATA", "DTAARA");
CharacterDataArea dataArea = new CharacterDataArea(system, path.getPath());

// Create the character data area on the system using default values.
dataArea.create();

// Clear the data area.
dataArea.clear();

// Write to the data area.
dataArea.write("Hello world");

// Read from the data area.
String data = dataArea.read();

// Delete the data area from the system.
dataArea.delete();
</pre>

<p>
Note: Most of the read() and write() methods of this class automatically
convert characters between Unicode and the CCSID associated with the AS400 object.
See {@link AS400#getCcsid AS400.getCcsid()}.
**/

public class CharacterDataArea extends DataArea implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2007 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;
   
   /**
    Constants
   **/

   static final int DEFAULT_LENGTH = 32;  // number of bytes
   static final int UNKNOWN_LENGTH = DataArea.UNKNOWN_LENGTH; //@B1A

   /**
    Variables
   ***/

   private String initialValue_ = " ";
                        // The initial value written to the data area upon creation.

   /**
    Constructs a CharacterDataArea object.
    It creates a default CharacterDataArea object.  The <i>system</i> and <i>path</i>
    properties must be set before attempting a connection.
   **/
   public CharacterDataArea()
   {
     super();
     length_ = UNKNOWN_LENGTH; //@B1C
     dataAreaType_ = CHARACTER_DATA_AREA;
   }


   /**
   Constructs a CharacterDataArea object.
   It creates a CharacterDataArea instance that represents the data area <i>path</i>
   on <i>system</i>.
      @param system The system that contains the data area.
      @param path The fully qualified integrated file system path name. The
             integrated file system file extension for a data area is DTAARA. An example of a
             fully qualified integrated file system path to a data area "MYDATA" in library
             "MYLIB" is: /QSYS.LIB/MYLIB.LIB/MYDATA.DTAARA
   **/
   public CharacterDataArea(AS400 system, String path)
   {
     super(system, path);
     length_ = UNKNOWN_LENGTH; //@B1C
     dataAreaType_ = CHARACTER_DATA_AREA;
   }


   /**
   Resets the data area to contain all blanks.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
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
   Creates a character data area on the system.
   This method uses the following default property values.
   <ul>
   <li>length - 32 bytes.
   <li>initialValue - A blank string.
   <li>textDescription - A blank string.
   <li>authority - A value of *LIBCRTAUT.
   </ul>
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectAlreadyExistsException    If the system object already exists.
     @exception ObjectDoesNotExistException     If the system object does not exist.
   **/
   public void create()
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     if (length_ == UNKNOWN_LENGTH) length_ = DEFAULT_LENGTH; //@B1A

     impl_.create(length_, initialValue_, textDescription_, authority_);

     // Fire the CREATED event.
     fireCreated();
   }


   /**
   Creates a character data area with the specified attributes.
     @param length The maximum number of bytes in the data area.
            Valid values are 1 through 2000.
     @param initialValue The initial value for the data area.
     @param textDescription The text description for the data area.
            The maximum length is 50 characters.
     @param authority The public authority level for the data area. Valid
            values are *ALL, *CHANGE, *EXCLUDE, *LIBCRTAUT, *USE, or the name
            of an authorization list. The maximum length is 10 characters.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectAlreadyExistsException    If the system object already exists.
     @exception ObjectDoesNotExistException     If the system object does not exist.
   **/
   public void create(int length, String initialValue,
                      String textDescription, String authority)
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException
   {
     // Validate the length
     if (length < 1 || length > 2000)
       throw new ExtendedIllegalArgumentException("length",
                             ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     // Validate the initialValue parameter.
     if (initialValue == null)
       throw new NullPointerException("initialValue");
     if (initialValue.length() == 0)
       throw new ExtendedIllegalArgumentException("initialValue",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
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

     length_ = length;
     initialValue_ = initialValue;
     textDescription_ = textDescription;
     authority_ = authority;

     create();
   }


   /**
   Removes the data area from the system.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the object does not exist.
   **/
   public void delete()
       throws AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
   {
     super.delete0();
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
   Reads the data from the data area.
   It retrieves the entire contents of the data area. Note that if the data
   does not completely fill the data area, this method will return data
   padded with trailing blanks up to the length of the data area.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the system object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
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

      if (length_ == UNKNOWN_LENGTH) length_ = getLength(); //@B1A
      
      // Do the read
      String val = impl_.retrieve(-1,length_);
      
      // Fire the READ event.
      fireRead();
      
      return val;
   }


   /**
   Reads the data from the data area.
   It retrieves the entire contents of the data area. Note that if the data
   does not completely fill the data area, this method will return data
   padded with trailing blanks up to the length of the data area.
     @param type The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the system object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
   **/
   public String read(int type)                                          //$A2A
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     if (length_ == UNKNOWN_LENGTH) length_ = getLength(); //@B1A

     // Do the read
     String val = impl_.retrieve(-1,length_, type);                      //$A2C

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
   Reads the data from the data area.
   It retrieves <i>dataLength</i> characters (or fewer if multi-byte characters) beginning at
   <i>dataAreaOffset</i> in the data area. The first character in
   the data area is at offset 0.
     @param dataAreaOffset The offset in the data area at which to start reading.
     @param dataLength The number of bytes to read. Valid values are from
            1 through (data area size - <i>dataAreaOffset</i>).
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the system object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
   **/
   public String read(int dataAreaOffset, int dataLength)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
      if (length_ == UNKNOWN_LENGTH) length_ = getLength(); //@B1A

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
     String val = impl_.retrieve(dataAreaOffset, dataLength);

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
   Reads the data from the data area.
   It retrieves <i>dataLength</i> characters (or fewer if multi-byte characters) beginning at
   <i>dataAreaOffset</i> in the data area. The first character in
   the data area is at offset 0.
     @param dataAreaOffset The offset in the data area at which to start reading.
     @param dataLength The number of bytes to read. Valid values are from
            1 through (data area size - <i>dataAreaOffset</i>).
     @param type The Data Area bidi string type, as defined by the CDRA (Character
                 Data Representataion Architecture). See <a href="BidiStringType.html">
                 BidiStringType</a> for more information and valid values.
     @return The data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the system object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
   **/
   public String read(int dataAreaOffset, int dataLength, int type)                //$A2A
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
      if (length_ == UNKNOWN_LENGTH) length_ = getLength(); //@B1A

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
     String val = impl_.retrieve(dataAreaOffset, dataLength, type);              //$A2C

     // Fire the READ event.
     fireRead();

     return val;
   }


   /**
    Reads the data from the data area.
    It retrieves up to <i>dataLength</i> bytes, without conversion,
    beginning at offset <i>dataAreaOffset</i> in the data area.
    Note that the first byte in the data area is at offset 0.
    @param dataBuffer The buffer into which to read the data.  Must be non-null.
    @param dataBufferOffset The starting offset in <tt>dataBuffer</tt>.
    @param dataAreaOffset The offset in the data area at which to start reading.
    @param dataLength The number of bytes to read. Valid values are from
    1 through (data area size - <i>dataAreaOffset</i>).
    @return The total number of bytes read into the buffer.
    @exception AS400SecurityException          If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception IllegalObjectTypeException      If the system object is not the required type.
    @exception InterruptedException            If this thread is interrupted.
    @exception IOException                     If an error occurs while communicating with the system.
    @exception ObjectDoesNotExistException     If the system object does not exist.
    @see #write(byte[],int,int,int)
    **/
   public int read(byte[] dataBuffer, int dataBufferOffset, int dataAreaOffset, int dataLength)
     throws AS400SecurityException,
   ErrorCompletingRequestException,
   IllegalObjectTypeException,
   InterruptedException,
   IOException,
   ObjectDoesNotExistException
   {
     return super.read(dataBuffer, dataBufferOffset, dataAreaOffset, dataLength);
   }


   /**
   Sets the fully qualified data area name.
   The following example demonstrates the use of setPath:
   <pre>
   // Create a CharacterDataArea object.
   CharacterDataArea dataArea = new CharacterDataArea();

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
   Writes the data to the data area.
   It writes <i>data</i> to the beginning of the data area. The remaining
   characters in the data area are blank padded.
     @param data The data to be written.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
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
     //if (data.length() < 1 || data.length() > length_)      //@A1D
     if (data.length() < 1)                                   //@A1C
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
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
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
      //if (data.length() < 1 || data.length() > length_)      //@A1D
      if (data.length() < 1)                                   //@A1C
         throw new ExtendedIllegalArgumentException("data",
                                                    ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      if (length_ == UNKNOWN_LENGTH) {
        try //@B1A
        {
          length_ = getLength(); //@B1A
        }
        catch(IllegalObjectTypeException iote) //@B1A
        {
          if (Trace.isTraceOn() && Trace.isTraceWarningOn()) //@B1A
          {
            Trace.log(Trace.WARNING, "Unexpected exception when retrieving length for character data area.", iote); //@B1A
          }
        }
      }

      // Validate the dataAreaOffset parameter.
      if (dataAreaOffset < 0 || dataAreaOffset >= length_)
         throw new ExtendedIllegalArgumentException("dataAreaOffset",
                                                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      // Validate the (dataAreaOffset, dataLength) combination.
      /*             
      if (dataAreaOffset+data.length() > length_)               //@A1D
         throw new ExtendedIllegalArgumentException("data",
                                                    ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        */
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
     @exception IOException                     If an error occurs while communicating with the system.
     @exception ObjectDoesNotExistException     If the system object does not exist.
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

     if (length_ == UNKNOWN_LENGTH) {
       try //@B1A
       {
         length_ = getLength(); //@B1A
       }
       catch(IllegalObjectTypeException iote) //@B1A
       {
         if (Trace.isTraceOn() && Trace.isTraceWarningOn()) //@B1A
         {
           Trace.log(Trace.WARNING, "Unexpected exception when retrieving length for character data area.", iote); //@B1A
         }
       }
     }

     // Validate the data length.
     //if (data.length() < 1 || data.length() > length_)       //@A1D
     if (data.length() < 1)                                    //@A1C
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     // Validate the dataAreaOffset parameter.
     if (dataAreaOffset < 0 || dataAreaOffset >= length_)
       throw new ExtendedIllegalArgumentException("dataAreaOffset",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     // Validate the (dataAreaOffset, dataLength) combination.
     /*                                                        
     if (dataAreaOffset+data.length() > length_)               //@A1D
       throw new ExtendedIllegalArgumentException("data",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
           */
     if (impl_ == null)
       chooseImpl();

     // Do the write
     impl_.write(data, dataAreaOffset, type);                        //$A2C

     // Fire the WRITTEN event.
     fireWritten();
   }


   /**
    Writes the data to the data area.
    It writes the specified bytes, without conversion, to the data area, at offset <i>dataAreaOffset</i>.
    Note that the first byte in the data area is at offset 0.
    @param dataBuffer The data to be written.  Must be non-null.
    @param dataBufferOffset The starting offset in <tt>dataBuffer</tt>.
    @param dataAreaOffset The offset in the data area at which to start writing.
    @param dataLength The number of bytes to write.
    @exception AS400SecurityException          If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception InterruptedException            If this thread is interrupted.
    @exception IOException                     If an error occurs while communicating with the system.
    @exception ObjectDoesNotExistException     If the system object does not exist.
    **/
   public void write(byte[] dataBuffer, int dataBufferOffset, int dataAreaOffset, int dataLength)
     throws AS400SecurityException,
   ErrorCompletingRequestException,
   InterruptedException,
   IOException,
   ObjectDoesNotExistException
   {
     super.write(dataBuffer, dataBufferOffset, dataAreaOffset, dataLength);
   }

}
