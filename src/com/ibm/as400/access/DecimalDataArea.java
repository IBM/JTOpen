///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DecimalDataArea.java
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
import java.math.BigDecimal;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;

/**
The DecimalDataArea class represents a decimal data area on the AS/400.
<p>
The following example demonstrates the use of DecimalDataArea:
<pre>
// Prepare to work with the AS/400 system named "My400".
AS400 system = new AS400("My400");

// Create a DecimalDataArea object.
QSYSObjectPathName path = new QSYSObjectPathName("MYLIB", "MYDATA", "DTAARA");
DecimalDataArea dataArea = new DecimalDataArea(system, path.getPath());

// Create the decimal data area on the AS/400 using default values.
dataArea.create();

// Clear the data area.
dataArea.clear();

// Write to the data area.
dataArea.write(new BigDecimal("1.2"));

// Read from the data area.
BigDecimal data = dataArea.read();

// Delete the data area from the AS/400.
dataArea.delete();
</pre>
**/

// Note: (length, decimalPositions) for a DDA corresponds to
//       (precision, scale) for a decimal number.

// Note: If the value you write to the 400 has a greater scale (number
// of decimal positions) than the DDA will hold, the number will be
// truncated.

// Note: The largest precision (currently) for a DDA on the 400 is
// 24 digits which includes a scale of 9 digits. This means that the
// base part of the number (i.e. to the left of the decimal point) can
// only have a maximum of 15 digits, irrespective of the fact that the
// DDA may have been created with a greater precision.
//   For example, a DDA created with a precision of 20 and a scale of 3
//   means that the length of the DDA is 20, which includes the 3 digits
//   of scale. This means that there are theoretically 17 digits left to
//   use for the base part of the number. However, the DDA will still
//   only accept a maximum base of 15 digits.


public class DecimalDataArea extends DataArea implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

 

    static final long serialVersionUID = 4L;

   /**
    Constants
   **/

   static final int DEFAULT_LENGTH = 15;

   /**
    Variables
   ***/

   private BigDecimal initialValue_ = new BigDecimal("0.0"); // The initial value written to the data area upon creation.

  //@B0 It was decided that the number of decimal positions should NOT be a bean property
  //    because the property getter getDecimalPositions() needs to go the system. Bean property
  //    constructors, getters, and setters should not make connections to the system
  //    because of the way a visual builder environment manipulates bean objects.
   private int decimalPositions_ = 5;  // The default number of decimal positions.


   /**
    Constructs a DecimalDataArea object.
    It creates a default DecimalDataArea object.  The <i>system</i> and <i>path</i>
    properties must be set before attempting a connection.
   **/
   public DecimalDataArea()
   {
     super();
     length_ = DEFAULT_LENGTH;
     dataAreaType_ = DECIMAL_DATA_AREA;
   }


   /**
   Constructs a DecimalDataArea object.
   It creates a DecimalDataArea instance that represents the data area <i>path</i>
   on <i>system</i>.
      @param system The AS/400 that contains the data area.
      @param path The fully qualified integrated file system path name. The
             integrated file system file extension for a data area is DTAARA. An example of a
             fully qualified integrated file system path to a data area "MYDATA" in library
             "MYLIB" is: /QSYS.LIB/MYLIB.LIB/MYDATA.DTAARA
   **/
   public DecimalDataArea(AS400 system, String path)
   {
     super(system, path);
     length_ = DEFAULT_LENGTH;
     dataAreaType_ = DECIMAL_DATA_AREA;
   }


   /**
   Resets the data area to contain 0.0.
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
   Creates a decimal data area on the AS/400.
   This method uses the following default property values.
   <ul>
   <li>length - 15 digits.
   <li>decimalPositions - 5 digits.
   <li>initialValue - A value of 0.0.
   <li>textDescription - A blank string.
   <li>authority - A value of *LIBCRTAUT.
   </ul>
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

     impl_.create(length_, decimalPositions_, initialValue_, textDescription_, authority_);

     // Fire the CREATED event.
     fireCreated();
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
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectAlreadyExistsException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     // Validate the length parameter.
     if (length < 1 || length > 24)
       throw new ExtendedIllegalArgumentException("length",
         ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     // Validate the number of decimal positions.
     if (decimalPositions < 0 || decimalPositions > 9)
       throw new ExtendedIllegalArgumentException("decimalPositions",
         ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     // Validate the number of decimal positions in relation to the length.
     if (decimalPositions > length)
       throw new ExtendedIllegalArgumentException("decimalPositions",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

     // Validate the initialValue parameter.
     if (initialValue == null)
       throw new NullPointerException("initialValue");
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
     decimalPositions_ = decimalPositions;
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
     super.delete0();
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
               IOException,
               ObjectDoesNotExistException
   {
  //@B0 It was decided that the number of decimal positions should NOT be a bean property
  //    because the property getter getDecimalPositions() needs to go the system. Bean property
  //    constructors, getters, and setters should not make connections to the system
  //    because of the way a visual builder environment manipulates bean objects.
     if (impl_ == null)
       chooseImpl();

     return impl_.getDecimalPositions();
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
   Returns the data read from the data area.
     @return The decimal data read from the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public BigDecimal read()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     BigDecimal val = impl_.readBigDecimal();

     fireRead(); // Fire the READ event.

     return val;
   }


   /**
   Sets the fully qualified data area name.
   The following example demonstrates the use of setPath:
   <pre>
   // Create a DecimalDataArea object.
   DecimalDataArea dataArea = new DecimalDataArea();

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
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     // Validate the data parameter.
     if (data == null)
       throw new NullPointerException("data");

     // Do the write
     if (impl_ == null)
       chooseImpl();

     impl_.write(data);

     // Fire the WRITTEN event.
     fireWritten();
   }
}
