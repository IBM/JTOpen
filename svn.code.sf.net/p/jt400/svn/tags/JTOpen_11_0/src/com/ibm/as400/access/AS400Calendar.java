///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Calenar.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class AS400Calendar {

   /*
    * Returns an instance of a Gregorian calendar to be used to set
    * Date values.   This is needed because the server uses the Gregorian calendar.
    * For some locales, the calendar returned by Calendar.getInstance is not usable.
    * For example, in the THAI local, a sun.util.BuddhistCalendar is returned.
    * @E4A
    */
   public static Calendar getGregorianInstance() {
     Calendar returnCalendar = Calendar.getInstance();
     boolean isGregorian = (returnCalendar  instanceof GregorianCalendar);
     boolean isBuddhist =  isBuddhistCalendar(returnCalendar);            /*@T3C*/

     if (isGregorian && (! isBuddhist)) {
        // Calendar is gregorian, but not buddhist
        return returnCalendar;
     } else {
       // Create a new gregorianCalendar for the current timezone and locale
       Calendar gregorianCalendar = new GregorianCalendar();
       return gregorianCalendar;
     }

   }




   /*
    * Returns an instance of a Gregorian calendar to be used to set
    * Date values.   This is needed because the server uses the Gregorian calendar.
    * For some locales, the calendar returned by Calendar.getInstance is not usable.
    * For example, in the THAI local, a java.util.BuddhistCalendar is returned.
    * @E4A
    */
   public static Calendar getGregorianInstance(java.util.TimeZone timezone) {
     Calendar returnCalendar = Calendar.getInstance(timezone);
     boolean isGregorian = (returnCalendar  instanceof GregorianCalendar);
     boolean isBuddhist =  isBuddhistCalendar(returnCalendar);
     

     if (isGregorian && (! isBuddhist)) {
        // Calendar is gregorian, but not buddhist
        return returnCalendar;
     } else {
       // Create a new gregorianCalendar for the current timezone and locale
       Calendar gregorianCalendar = new GregorianCalendar(timezone);
       return gregorianCalendar;
     }

   }




  /**
   * Get a calendar to do the conversion to java.util.Date based objects.
   * If the user passes in a non-Gregorian calendar, then use the timezone to
   * create a gregorian calendar.  This is the observed behavior of the jcc driver.
   * @param calendar base calendar
   * @return Calendar to use for java.util.Date based objects.
   */
  public static Calendar getConversionCalendar(Calendar calendar) {
    if (calendar == null) {
      return getGregorianInstance();
    } else {
      boolean isGregorian = (calendar instanceof GregorianCalendar);

      boolean isBuddhist = isBuddhistCalendar(calendar); /*@T3C*/


      if (isGregorian && (!isBuddhist)) {
        // Calendar is gregorian, but not buddhist
        return calendar;
      } else {
        // Create a new gregorianCalendar for the current timezone and locale
        Calendar gregorianCalendar = new GregorianCalendar(calendar
            .getTimeZone());
        gregorianCalendar.setLenient(calendar.isLenient());
        return gregorianCalendar;
      }
    }
  }

  /* @T3C*/ 
  private static boolean isBuddhistCalendar(Calendar calendar) {
    try {
        Class c = Class.forName("sun.util.BuddhistCalendar");
        return c.isInstance(calendar); 

  } catch (Throwable ncdfe) {
    // Just ignore if any exception occurs.  @F2C
    // Possible exceptions (from Javadoc) are:
    // java.lang.NoClassDefFoundError
    // java.security.AccessControlException (if sun.util classes cannot be used)
  }
    return false; 
  }


  /*  Get an instance of a calendar from the GMT timezone  @G4A */
  static TimeZone gmtTimeZone = null;
  public static Calendar getGMTInstance() {
    if (gmtTimeZone == null) {
      gmtTimeZone = TimeZone.getTimeZone("GMT");
    }
    return Calendar.getInstance(gmtTimeZone);
  }

}
