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
  
  
  
  public static void main(String[] args) { 
    String[][] testcases = {
        /* time            len       picos            result         */ 
        {"1351719245000",  "19",     "0",              "2012-10-31 16:34:05"}, 
        {"1351719245000",  "20",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "21",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "22",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "23",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "24",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "25",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "26",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "27",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "28",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "29",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "30",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "31",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "32",     "0",              "2012-10-31 16:34:05"},
        {"1351719245000",  "21",     "100000000000",   "2012-10-31 16:34:05.1"},
        {"1351719245000",  "21",     "123456789012",   "2012-10-31 16:34:05.1"},
        {"1351719245000",  "22",     "123456789012",   "2012-10-31 16:34:05.12"},
        {"1351719245000",  "23",     "123456789012",   "2012-10-31 16:34:05.123"},
        {"1351719245000",  "24",     "123456789012",   "2012-10-31 16:34:05.1234"},
        {"1351719245000",  "25",     "123456789012",   "2012-10-31 16:34:05.12345"},
        {"1351719245000",  "26",     "123456789012",   "2012-10-31 16:34:05.123456"},
        {"1351719245000",  "27",     "123456789012",   "2012-10-31 16:34:05.1234567"},
        {"1351719245000",  "28",     "123456789012",   "2012-10-31 16:34:05.12345678"},
        {"1351719245000",  "29",     "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "30",     "123456789112",   "2012-10-31 16:34:05.1234567891"},
        {"1351719245000",  "31",     "123456789012",   "2012-10-31 16:34:05.12345678901"},
        {"1351719245000",  "32",     "123456789012",   "2012-10-31 16:34:05.123456789012"},
        {"1351719245000",  "0",      "0",              "2012-10-31 16:34:05"}, 
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "0",      "123456789012",   "2012-10-31 16:34:05.123456789"},
        {"1351719245123",  "0",      "-1",             "2012-10-31 16:34:05.123"}, 
        {"1351719245123",  "21",     "-1",             "2012-10-31 16:34:05.1"},
        {"1351719245123",  "21",     "-1",             "2012-10-31 16:34:05.1"},
        {"1351719245123",  "22",     "-1",             "2012-10-31 16:34:05.12"},
        {"1351719245123",  "23",     "-1",             "2012-10-31 16:34:05.123"},
        {"1351719245000",  "32",     "123456789010",   "2012-10-31 16:34:05.12345678901"},
        {"1351719245000",  "32",     "123456789110",   "2012-10-31 16:34:05.12345678911"},
        {"1351719245000",  "32",     "123456789100",   "2012-10-31 16:34:05.1234567891"},
        {"1351719245000",  "32",     "123456789000",   "2012-10-31 16:34:05.123456789"},
        {"1351719245000",  "32",     "123456780000",   "2012-10-31 16:34:05.12345678"},
        {"1351719245000",  "32",     "123456700000",   "2012-10-31 16:34:05.1234567"},
        {"1351719245000",  "32",     "123456000000",   "2012-10-31 16:34:05.123456"},
        {"1351719245000",  "32",     "123450000000",   "2012-10-31 16:34:05.12345"},
        {"1351719245000",  "32",     "123400000000",   "2012-10-31 16:34:05.1234"},
        {"1351719245000",  "32",     "123000000000",   "2012-10-31 16:34:05.123"},
        {"1351719245000",  "32",     "120000000000",   "2012-10-31 16:34:05.12"},
        {"1351719245000",  "32",     "100000000000",   "2012-10-31 16:34:05.1"},
        {"1351719245000",  "32",     "000000000000",   "2012-10-31 16:34:05"},


    }; 
    
    System.out.println("Unit testing AS400JDBCTimestamp");
    int passed = 0;
    int total = testcases.length;
    for (int i = 0; i < total; i++) { 
      long time = Long.parseLong(testcases[i][0]); 
      int  len  = Integer.parseInt(testcases[i][1]); 
      long picos = Long.parseLong(testcases[i][2]);
      String expected = testcases[i][3]; 
      
      AS400JDBCTimestamp ts;
      if (len == 0) { 
        ts = new AS400JDBCTimestamp(time);
        if (picos >= 0) { 
          ts.setNanos((int)(picos / 1000));
        }
      } else { 
        ts = new AS400JDBCTimestamp(time, len);
        if (picos >= 0) { 
        ts.setPicos(picos) ;
        }
      }
      String output=ts.toString(); 
      if (!output.equals(expected)) {
        System.out.println("For case "+i+" got "+output+" sb "+expected+" for time="+time+" len="+len+" picos="+picos); 
      } else {
        passed++; 
      }
      
    }
    System.out.println("Test completed "+passed+" of "+total+" successful"); 
  }
}
