///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCFieldedTimestamp.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2015-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

/**
 * A internal version representing a timestamp that is used only 
 * for the implementation of batching. 
 * 
 */
public class AS400JDBCFieldedTimestamp  {
  static final String copyright = "Copyright (C) 2016 International Business Machines Corporation and others.";

  static final long serialVersionUID = 1L;

  private int                     year_;
  private int                     month_;  /* zero based, per Java convention */ 
  private int                     day_;
  private int                     hour_;
  private int                     minute_;
  private int                     second_;
  private long                    picos_;   /*@H3C*/
  private int                     length_; 

  
  public AS400JDBCFieldedTimestamp(
      int                     year,
      int                     month,  
      int                     day,
      int                     hour,
      int                     minute,
      int                     second,
      long                    picos,  
      int                     length
      ) {
                      year_ = year;
                      month_ = month;  
                      day_ = day;
                      hour_ = hour;
                      minute_ = minute;
                      second_ = second;
                      picos_ = picos;  
                      length_ = length; 
    
  }


 int getYear() { return year_;}
 int getMonth() { return month_; }
 int getDay() { return day_; }
 int getHour() { return hour_; }
 int getMinute() { return minute_; }
 int getSecond() { return second_; }
 long getPicos() { return picos_; }
 int getLength() { return length_; }
   
  
}
