///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NLS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.util.*;

/**
 * The NLS class contains a set of static methods that can be used
 * to access various pieces of National Language Support information
 * (such as language and country descriptions) on an OS/400 server.
**/
public final class NLS
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private static int countryBytes_ = 11000;  
  private static int languageBytes_ = 3000;
  private static final Hashtable languages_ = new Hashtable();
  private static final Hashtable countries_ = new Hashtable();
  
  private NLS()
  {
  }

  /**
   * Retrieves the descriptive text for the specified country identifier.
   * The list is cached, so that a subsequent call to this method will
   * return immediately if the specified country identifier is in the list.
   * If it is not in the list, the system will be queried.
   * @param system The OS/400 server.
   * @param countryID The country identifier.
   * @return The descriptive text.
  **/
  public static String getCountryDescription(AS400 system, String countryID)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    countryID = countryID.trim().toUpperCase();
    String description = (String)countries_.get(countryID);
    if (description == null)
    {
      // Retrieve it from the system.
      int ccsid = system.getCcsid();
      ConvTable conv = ConvTable.getTable(ccsid, null);
      ProgramParameter[] parms = new ProgramParameter[4];
      parms[0] = new ProgramParameter(countryBytes_); // receiver variable
      parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(countryBytes_)); // length of receiver variable
      parms[2] = new ProgramParameter(conv.stringToByteArray("RTVC0100")); // format name
      parms[3] = new ProgramParameter(new byte[4]); // error code

      ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QLGRTVCI.PGM", parms);
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      // Note: The V5R1 API doc has the wrong offsets.
      byte[] output = parms[0].getOutputData();
      int bytesReturned = BinaryConverter.byteArrayToInt(output, 0);
      int bytesAvailable = BinaryConverter.byteArrayToInt(output, 4);
      int offset = BinaryConverter.byteArrayToInt(output, 12);
      if (bytesAvailable > bytesReturned)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Increasing RetrieveCountryID chunk size from "+countryBytes_+" to "+(bytesAvailable+offset)+" and re-retrieving.");
        }
        countryBytes_ = bytesAvailable+offset;
        return getCountryDescription(system, countryID);
      }
      ccsid = BinaryConverter.byteArrayToInt(output, 8);
      conv = ConvTable.getTable(ccsid, null);
      int numberOfIDs = BinaryConverter.byteArrayToInt(output, 16);
      for (int i=0; i<numberOfIDs; ++i)
      {
        String country = conv.byteArrayToString(output, offset, 2).trim().toUpperCase();
        offset += 2;
        String descriptiveText = conv.byteArrayToString(output, offset, 40).trim();
        offset += 40;
        countries_.put(country, descriptiveText);
        if (country.equals(countryID))
        {
          description = descriptiveText;
        }
      }
    }
    return description;
  }

  /**
   * Retrieves the descriptive text for the specified language identifier.
   * The list is cached, so that a subsequent call to this method will
   * return immediately if the specified language identifier is in the list.
   * If it is not in the list, the system will be queried.
   * @param system The OS/400 server.
   * @param languageID The language identifier.
   * @return The descriptive text.
  **/
  public static String getLanguageDescription(AS400 system, String languageID)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    languageID = languageID.trim().toUpperCase();
    String description = (String)languages_.get(languageID);
    if (description == null)
    {
      // Retrieve it from the system.
      int ccsid = system.getCcsid();
      ConvTable conv = ConvTable.getTable(ccsid, null);
      ProgramParameter[] parms = new ProgramParameter[4];
      parms[0] = new ProgramParameter(languageBytes_); // receiver variable
      parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(languageBytes_)); // length of receiver variable
      parms[2] = new ProgramParameter(conv.stringToByteArray("RTVL0100")); // format name
      parms[3] = new ProgramParameter(new byte[4]); // error code

      ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QLGRTVLI.PGM", parms);
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      // Note: The V5R1 doc for this API shows a different format
      // than the country ID API, so be careful!
      byte[] output = parms[0].getOutputData();
      int bytesAvailable = BinaryConverter.byteArrayToInt(output, 0);
      int bytesReturned = BinaryConverter.byteArrayToInt(output, 4);
      int offset = BinaryConverter.byteArrayToInt(output, 16);
      if (bytesAvailable > bytesReturned)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Increasing RetrieveLanguageID chunk size from "+languageBytes_+" to "+(bytesAvailable+offset)+" and re-retrieving.");
        }
        languageBytes_ = bytesAvailable+offset;
        return getLanguageDescription(system, languageID);
      }
      int numberOfIDs = BinaryConverter.byteArrayToInt(output, 8);
      ccsid = BinaryConverter.byteArrayToInt(output, 12);
      conv = ConvTable.getTable(ccsid, null);
      for (int i=0; i<numberOfIDs; ++i)
      {
        String lang = conv.byteArrayToString(output, offset, 3).trim().toUpperCase();
        offset += 3;
        String descriptiveText = conv.byteArrayToString(output, offset, 40).trim();
        offset += 40;
        languages_.put(lang, descriptiveText);
        if (lang.equals(languageID))
        {
          description = descriptiveText;
        }
      }
    }
    return description;
  }
}

