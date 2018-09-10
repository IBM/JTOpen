///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Time.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

/**
 * Converts a 64 bit timestamp into a readable date and time 
 */
class Time {
	long mask= 0xFFFFFFFFFFC00000L; // Mask to and out uneeded bits
	// Mask to and with if date is in 21st Century
	long mask2= 0x7FFFFFFFFFC00000L;
	long timestamp;
	Calendar cal;

	/**
	 * Creates and calculates the timestamp 
	 * @param timestamp	    The 64 bit timestamp        
	 */
	public Time(long timestamp) {
		this.timestamp= timestamp;
		if (timestamp != 0) {
			createCal();
		}
	}

	/**
	 * Modifies the timestamp to remove unneeded bits and sets the time of the calendar. 
	 */
	private void createCal() {
		timestamp= (timestamp & mask2);

		// Remove unneeded bits
		timestamp= timestamp >>> 16;
		timestamp= ((timestamp / 64) * 1024);
		timestamp += 946702800000000L;
		timestamp /= 1000;

		// Used if you don't want milliseconds to be calculated
		//    	timestamp /= 1000000;
		//      timestamp += 946702800;
		//      timestamp *= 1000;

		cal= new GregorianCalendar();
		Date d= new Date(timestamp);
		cal.clear();
		cal.setTime(d);
	}

	/**
	 * Returns the timestamp as a string in Calendar.getTime() format.
	 * @return	    The timestamp in the same format as Calendar.getTime().
	 * @see Calendar
	 */
	public String toString() {
		if (cal == null) {
			return "";
		} else {
			return (cal.getTime()).toString();
		}
	}

	/**
	 * Returns the timestamp as a string in HH:MM:SS.mm format.
	 * @return	    The timestamp in HH:MM:SS.mm format.
	 * @see Calendar
	 */
	public String getTime() {
		if (cal == null) {
			return "";
		} else {
			StringBuffer time = new StringBuffer();
			time.append(cal.get(Calendar.HOUR_OF_DAY));
			time.append(":");
			String min = Integer.toString(cal.get(Calendar.MINUTE));
			if(min.length()==1) { // If there is no leading 0 add one
				time.append("0");
				time.append(min);
			} else { // Otherwise just append the time
				time.append(min);
			}
			time.append(":");
			String sec = Integer.toString(cal.get(Calendar.SECOND));
			int length = sec.length();
			if(length==1) { // If there is no leading 0 add one
				time.append("0");
				time.append(sec);
			} else { // Otherwise just append the time
				time.append(sec);
			}
			time.append(".");
			String millisec = Integer.toString(cal.get(Calendar.MILLISECOND));
			length = millisec.length();
			if(length==1) { // If there is no leading 0's add them
				time.append("00");
				time.append(millisec);
			} else if (length==2) {
				time.append("0");
				time.append(millisec);
			} else { // Otherwise just append the time
				time.append(millisec);
			}
			return time.toString();
		}
	}

	/**
	 * Returns the timestamp in milliseconds since the epoch(January 1, 1970, 00:00:00 GMT). 
	 * @return long
	 */
	public long getTimeStamp() {
		return timestamp;
	}
}