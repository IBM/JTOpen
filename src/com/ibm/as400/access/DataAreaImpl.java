///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: DataAreaImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.math.BigDecimal;

/**
Specifies the methods which the implementation objects for the Data Area classes
need to support.  The Data Area classes are:
 <ul compact>
 <li>DataArea (abstract base class)
 <li>CharacterDataArea
 <li>DecimalDataArea
 <li>LocalDataArea
 <li>LogicalDataArea
 </ul>
**/
interface DataAreaImpl
{

  // For all xxxDataArea classes:
  void clear()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For CharacterDataArea:
  void create(int length, String initialValue,
              String textDescription, String authority)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectAlreadyExistsException,
           ObjectDoesNotExistException,
           IOException;

  // For DecimalDataArea:
  void create(int length, int decimalPositions,
              BigDecimal initialValue, String textDescription,
              String authority)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectAlreadyExistsException,
           ObjectDoesNotExistException,
           IOException;

  // For LogicalDataArea:
  void create(boolean initialValue, String textDescription,
              String authority)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectAlreadyExistsException,
           ObjectDoesNotExistException,
           IOException;

  // For all except LocalDataArea:
  void delete()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For DecimalDataArea:
  int getDecimalPositions()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For all xxxDataArea classes:
  int getLength()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For DecimalDataArea:
  BigDecimal readBigDecimal()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For LogicalDataArea:
  boolean readBoolean()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For all:
  void refreshAttributes()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For CharacterDataArea, LocalDataArea:
  String retrieve(int dataAreaOffset, int dataLength)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For CharacterDataArea, LocalDataArea:
  String retrieve(int dataAreaOffset, int dataLength, int type)      //$D2A
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           IllegalObjectTypeException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For all xxxDataArea classes:
  void setAttributes(AS400Impl system, QSYSObjectPathName path, int dataAreaType)
    throws IOException;

  // For CharacterDataArea, LocalDataArea:
  void write(String data, int dataAreaOffset)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For CharacterDataArea, LocalDataArea:
  void write(String data, int dataAreaOffset, int type)              //$D2A
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For DecimalDataArea:
  void write(BigDecimal data)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

  // For LogicalDataArea:
  void write(boolean data)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           ObjectDoesNotExistException,
           IOException;

}
