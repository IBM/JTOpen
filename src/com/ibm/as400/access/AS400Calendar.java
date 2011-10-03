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

import sun.util.BuddhistCalendar;

public class AS400Calendar {
  
   /*
    * Returns an instance of a Gregorian calendar to be used to set
    * Date values.   This is needed because the server uses the Gregorian calendar.
    * For some locales, the calendar returned by Calendar.getInstance is not usable. 
    * For example, in the THAI local, a java.util.BuddhistCalendar is returned. 
    * @E4A
    */
   public static Calendar getGregorianInstance() {
     Calendar returnCalendar = Calendar.getInstance(); 
     boolean isGregorian = (returnCalendar  instanceof GregorianCalendar);
     boolean isBuddhist = false; 
     try { 
         isBuddhist  = (returnCalendar  instanceof BuddhistCalendar);
     } catch (java.lang.NoClassDefFoundError ncdfe) { 
       // Just ignore if class cannot be found 
     }
     
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
     boolean isBuddhist = false; 
     try {
       isBuddhist = (returnCalendar  instanceof BuddhistCalendar); 
     } catch (java.lang.NoClassDefFoundError ncdfe) { 
       // Just ignore if class cannot be found 
     }

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
   * @param calendar
   * @return
   */
  public static Calendar getConversionCalendar(Calendar calendar) {
    if (calendar == null) {
      return getGregorianInstance(); 
    } else {
      boolean isGregorian = (calendar instanceof GregorianCalendar);
      
      boolean isBuddhist = false; 
      try { 
        isBuddhist =  (calendar instanceof BuddhistCalendar);
      } catch (java.lang.NoClassDefFoundError ncdfe) { 
        // Just ignore if class cannot be found 
      }


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

}
