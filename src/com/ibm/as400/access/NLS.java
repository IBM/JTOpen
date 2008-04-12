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
 * (such as language and country descriptions) on a system.
**/
public final class NLS
{
    private static final String CLASSNAME = "com.ibm.as400.access.NLS";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

  private static int countryBytes_ = 11000;  
  private static int languageBytes_ = 3000;
  private static final Hashtable languages_ = new Hashtable();
  private static final Hashtable countries_ = new Hashtable();
  
  static boolean forceJavaTables_ = false;

  private NLS()
  {
  }

  
  //@B0A
  /**
   * Returns a best-guess Java encoding given a CCSID. 
   * @param ccsid The coded character set identifier (CCSID), e.g. 37.
   * @return The encoding that maps to the given CCSID, or null
   * if one is not known.
   * @see #encodingToCCSID
   * @see #localeToCCSID
  **/
  public static String ccsidToEncoding(int ccsid)
  {
    return (String)ConversionMaps.ccsidEncoding_.get(Integer.toString(ccsid));
  }


  //@B0A
  /**
   * Returns a best-guess CCSID given a Java encoding.
   * @param encoding The encoding, e.g. "Cp037".
   * @return The CCSID that maps to the given encoding, or -1
   * if one is not known.
   * @see #ccsidToEncoding
   * @see #localeToCCSID
  **/
  public static int encodingToCCSID(String encoding)
  {
    if (encoding == null) return -1;
    String ccsid = (String)ConversionMaps.encodingCcsid_.get(encoding);
    if (ccsid == null) return -1;
    return Integer.parseInt(ccsid);
  }


  //@B0A
  /**
   * Returns a best-guess CCSID given a Java locale string.
   * Note that the CCSID returned will be the preferred system CCSID, i.e. 
   * usually EBCDIC. So, the locale string representing English "en" will
   * return the single-byte EBCDIC CCSID of 37.
   * @param localeString The locale string, e.g. "de_CH".
   * @return The CCSID that maps the given locale string, or -1
   * if one is not known.
  **/
  public static int localeToCCSID(String localeString)
  {
    if (localeString == null) return -1;
    String ls = localeString.trim();
    while (ls != null && ls.length() > 0)
    {
      String ccsidString = (String)ConversionMaps.localeCcsidMap_.get(ls);
      if (ccsidString != null)
      {
        return Integer.parseInt(ccsidString);
      }
      ls = ls.substring(0, ls.lastIndexOf('_'));
    }
    return -1;
  }


  //@B0A
  /**
   * Returns a best-guess CCSID given a Java Locale object.
   * Note that the CCSID returned will be the preferred system CCSID, i.e. 
   * usually EBCDIC. So, the Locale representing English ({@link java.util.Locale#ENGLISH Locale.ENGLISH})
   * will return the single-byte EBCDIC CCSID of 37.
   * @param locale The Locale object.
   * @return The CCSID that maps the given locale, or -1
   * if one is not known.
  **/
  public static int localeToCCSID(Locale locale)
  {
    if (locale == null) return -1;
    String ls = locale.toString();
    return localeToCCSID(ls);
  }


  /**
   * Returns a best-guess National Language Version (NLV) string given a Java locale string.
   * If there is no known mapping for the given Locale or one of its parents (e.g. "en" is a parent of "en_US"),
   * then "" is returned.
   * @param localeString The locale string, e.g. "de_CH".
   * @return The NLV string (e.g. "2924") that maps the given locale, or "" if one is not known.
  **/
  public static String localeToNLV(String localeString)
  {
    if (localeString == null) return "";
    String ls = localeString.trim();
    while (ls != null && ls.length() > 0)
    {
      String nlvString = (String)ConversionMaps.localeNlvMap_.get(ls);
      if (nlvString != null)
      {
        return nlvString;
      }
      ls = ls.substring(0, ls.lastIndexOf('_'));
    }
    return "";
  }


  /**
   * Returns a best-guess National Language Version (NLV) string given a Java Locale object.
   * If there is no known mapping for the given Locale or one of its parents (e.g. "en" is a parent of "en_US"),
   * then "" is returned.
   * @param locale The Locale object.
   * @return The NLV string (e.g. "2924") that maps the given locale, or "" if one is not known.
  **/
  public static String localeToNLV(Locale locale)
  {
    if (locale == null) return "";
    String ls = locale.toString();
    return localeToNLV(ls);
  }


  /**
   * Retrieves the descriptive text for the specified country or region identifier.
   * The list is cached, so that a subsequent call to this method will
   * return immediately if the specified country or region identifier is in the list.
   * If it is not in the list, the system will be queried.
   * @param system The system.
   * @param countryID The country or region identifier.
   * @return The descriptive text.
   * @see #getLanguageDescription
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
   * @param system The system.
   * @param languageID The language identifier.
   * @return The descriptive text.
   * @see #getCountryDescription
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

  /**
   * Indicates whether or not any text conversion performed by the Toolbox uses the
   * converter tables that are part of the Toolbox, or the converter tables that are
   * part of the Java Runtime Environment. The default is to use the Toolbox
   * converter tables, since their behavior more closely matches the behavior of 
   * text conversion on the system.
   * @return true if the Java Runtime Environement converter tables are used;
   * false if the Toolbox converter tables are used. The default is false.
   * @see #setForceJavaConversion
  **/
  public static boolean isForceJavaConversion()
  {
    return forceJavaTables_;
  }

  /**
   * Sets whether or not any text conversion performed by the Toolbox uses the
   * converter tables that are part of the Toolbox, or the converter tables that are
   * part of the Java Runtime Environment. The default is to use the Toolbox
   * converter tables, since their behavior more closely matches the behavior of 
   * text conversion on the system.
   * <p>
   * The usefulness of this method is arbitrary. Typically, applications only need to 
   * force Java conversion if they are seeing inconsistent character conversion between
   * an older release of the Toolbox and a newer one. Some problem characters include
   * EBCDIC line feeds and a few characters in Katakana, such as the middle dot or bullet.
   * @param forceJavaTables Specify true if the Java Runtime Environement converter tables are used;
   * false if the Toolbox converter tables are used. The default is false.
   * @see #isForceJavaConversion
  **/
  public static void setForceJavaConversion(boolean forceJavaTables)
  {
    forceJavaTables_ = forceJavaTables;
  }
}

