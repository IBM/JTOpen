///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCTimestamp.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2012-2013 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.sql.Timestamp;
import java.util.Date;
/**
 * A version of an java.sql.Timestamp that support picosecond precision. 
 * @see java.sql.Timestamp
 *
 */
public class AS400JDBCTimestamp extends Timestamp {
  static final String copyright = "Copyright (C) 2012-2013 International Business Machines Corporation and others.";

  private static final long serialVersionUID = 1L;
  
  long picos_;
  int length_;
  /**
   * Create an AS400JDBC Timestamp
   * @param time   milliseconds since January 1, 1970, 00:00:00 GMT.
   * @param length Length of the formatted timestamp.  Valid values are 19,21-32
   */
  public AS400JDBCTimestamp(long time, int length) {
    super(time);
    picos_ = super.getNanos() * 1000L; 
    length_ = length; 
  }

  /**
   * Create an AS400JDBC Timestamp
   * @param time   milliseconds since January 1, 1970, 00:00:00 GMT.
   */
  public AS400JDBCTimestamp(long time) {
    super(time);
    picos_ = super.getNanos() * 1000L; 
    length_ = 32; 
  }

  
  /**
   * Get this Timestamp objects' picos value. 
   * 
   * @return
   */
  public long getPicos() {
    return picos_; 
  }
  
 
  /**
   * Sets the picos value for this Timestamp object. 
   */
  public void setPicos(long picos) {
      picos_ = picos ; 
      super.setNanos((int)(picos_ / 1000)); 
  }
 
  /**
   * Gets this Timestamp object's nanos value.
   * @see  java.sql.Timestamp#getNanos() 
   * @return this Timestamp object's factional seconds component 
   */

  public int getNanos() {
    return (int) (getPicos() / 1000); 
  }


  /**
   * Sets the nanos values for this Timestamp object. 
   * @see java.sql.Timestamp#setNanos(int)
   */
  public void setNanos(int nanos) {
      setPicos(nanos * 1000L); 
  }
  
  /* 
   * Formats a timestamp in JDBC timestamp escape format. 
   * yyyy-mm-dd hh:mm:ss.ffffffffffff, where fffffffffffff indicates picoseconds. 
   * @return: a String object in yyyy-mm-dd hh:mm:ss.fffffffffffff format.
   *         By convention with java.sql.Timestamp, all trailing 0's are dropped. 
   * @see java.sql.Timestamp#toString()
   */
  public String toString() {
    String nonFractional=super.toString();
    StringBuffer sb = new StringBuffer(); 
    int dotIndex = nonFractional.indexOf('.'); 
    /* Note:   the current Timestamp.toString() always returns a dot */ 
    if (dotIndex > 0) { 
      nonFractional = nonFractional.substring(0,dotIndex); 
    }
    sb.append(nonFractional);
    
    if ( length_ < 20 || picos_ == 0) {
      // no need to add fractional digits
    } else { 
      sb.append("."); 
      int currentLength = 20;
      long digitPlace = 100000000000L;
      while (currentLength < length_ && digitPlace > 0) {
        char digit = (char) ('0'+ ( (picos_ / digitPlace) % 10));
        sb.append(digit); 
        currentLength++; 
        digitPlace = digitPlace / 10; 
      }
      while (sb.charAt(sb.length()-1) == '0') {
        sb.setLength( sb.length() - 1); 
      }
    } 
    return sb.toString();  
  }

  
  /**
   * Sets this Timestamp object to represent a point in time that is time milliseconds after January 1, 1970 00:00:00 GMT.
   * 
   * @override   setTime in class Timestmap
   * 
   * @param  time -- the number of milliseconds
   * @see java.sql.Timestamp#setTime(long)
   */
  public void setTime(long millis) {
    super.setTime(millis); 
    picos_ = super.getNanos() * 1000L; 
    
  }

  /**
   * Tests to see if this Timestamp object is equal to the given Timestamp object.
   * Two timestamps are equal if both their millisecond time and picosecond time are equal.
  * @param ts the Timestamp value to compare with 
  * @return true if the given Timestamp object is equal to this Timestamp object; false otherwise
     * @see java.sql.Timestamp#equals(java.sql.Timestamp)
   */
  public boolean equals(Timestamp ts) { 
    return compareTo(ts) == 0; 
  }
  
  /**
   * Tests to see if this Timestamp object is equal to the given object.  If the given object
   * is not a timestamp object, then the two objects are not equal.
   */

  public boolean equals(Object obj) {
    if (obj instanceof java.sql.Timestamp){
      return equals((java.sql.Timestamp)obj);
    } else {
      return false; 
    }
  }

  
  
  /**
   * Indicates whether this Timestamp object is earlier than the given Timestamp object.
   * @param ts - the Timestamp value to compare with 
   * @return true if this Timestamp object is earlier; false otherwise
   * @see java.sql.Timestamp#before(java.sql.Timestamp)
   */
  public boolean before(Timestamp ts) {
    return (compareTo(ts) < 0); 
  }


  /**
   * Indicates whether this Timestamp object is later than the given Timestamp object.
   * @param ts - the Timestamp value to compare with
   * @return true if this Timestamp object is later; false otherwise
   * @see java.sql.Timestamp#after(java.sql.Timestamp)
   */
  public boolean after(Timestamp ts) {
    return (compareTo(ts) > 0); 
  }

  /**
   * Compares this Timestamp object to the given Timestamp object.
   * @param ts - the Timestamp object to be compared to this Timestamp object
   * @return the value 0 if the two Timestamp objects are equal; 
   *         a value less than 0 if this Timestamp object is before the given argument; 
   *         and a value greater than 0 if this Timestamp object is after the given argument.
   * @since 1.2
   * @see java.sql.Timestamp#compareTo(java.sql.Timestamp)
   */
  public int compareTo(Timestamp ts) {
    long thisTime = getTime(); 
    long otherTime = ts.getTime(); 
    if (thisTime != otherTime) {
      if (thisTime < otherTime) {
        return -1; 
      } else { 
        return 1; 
      }
    } else {
      long otherPicos;
      if (ts instanceof AS400JDBCTimestamp) {
        otherPicos = ((AS400JDBCTimestamp) ts).getPicos(); 
      } else {
        otherPicos = ts.getNanos() * 1000; 
      }
      if (picos_ == otherPicos) {
        return 0;
      } else if (picos_ < otherPicos) {
        return -1;
      } else {
        return 1; 
      }
    }
  }
  
  
  /**
   * Compares this Timestamp object to the given Date, which must be a Timestamp object. If the argument is not a Timestamp object, this method throws a ClassCastException object. (Timestamp objects are comparable only to other Timestamp objects.)
   * @param o - the Date to be compared, which must be a Timestamp object
   * @return the value 0 if this Timestamp object and the given object are equal; 
   *          a value less than 0 if this Timestamp object is before the given argument; 
   *          and a value greater than 0 if this Timestamp object is after the given argument. 
   * @throws ClassCastException - if the argument is not a Timestamp object
   * @see java.util.Date#compareTo(java.util.Date)
   */
  public int compareTo(Date o) {
     return compareTo((java.sql.Timestamp)o); 
 }
  
  public static Timestamp valueOf(String inString) {
    AS400JDBCTimestamp returnTimestamp = null; 
    if (inString != null) inString=inString.trim(); 
    if ((inString == null ) || (inString.length() <=29)) {
      return Timestamp.valueOf(inString); 
    } else {
      // Get a regular timestamp
      Timestamp ts = Timestamp.valueOf(inString.substring(0,29)); 
      
      // Determine the picoseonds
      String picoString=inString.substring(29); 
      int picoStringLength=picoString.length(); 
      if (picoStringLength==1) {
        picoString=picoString+"00"; 
      } else if (picoStringLength==2) {
        picoString=picoString+"0"; 
      } else if (picoStringLength > 3) {
        picoString = picoString.substring(0,3); 
      }
      long picos = Integer.parseInt(picoString); 
      if (picos == 0) {
        return ts; 
      } else {
        returnTimestamp = new AS400JDBCTimestamp(ts.getTime());
        picos += 1000L * ts.getNanos();
        returnTimestamp.setPicos(picos);
      }
      
    }
    return returnTimestamp; 
  }
  
}
