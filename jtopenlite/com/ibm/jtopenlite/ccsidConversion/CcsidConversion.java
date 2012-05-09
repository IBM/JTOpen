///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CcsidConversion.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Hashtable;

/*
 * This class provides conversion routine for CCSID typically not available in a JVM.
 * A mobile implementation should only ship the conversion table that it believes are necessary.
 */
public class CcsidConversion {
	static Object                lock = new Object();
	static SingleByteConversion[]  ccsidToSingleByteConversion = new SingleByteConversion[700];
	static StringBuffer sb = new StringBuffer();



	public static SingleByteConversion getSingleByteConversion(int ccsid) throws UnsupportedEncodingException {
		SingleByteConversion singleByteConversion;
		synchronized(lock) {
			if (ccsid >= ccsidToSingleByteConversion.length) {
				SingleByteConversion[] newCcsidToSingleByteConversion  = new SingleByteConversion[ccsid+1];
				for (int i = 0; i < ccsidToSingleByteConversion.length; i++ ) {
					newCcsidToSingleByteConversion [i]  = ccsidToSingleByteConversion [i];
				}
				ccsidToSingleByteConversion = newCcsidToSingleByteConversion ;
			}
			singleByteConversion = ccsidToSingleByteConversion[ccsid];
			if (singleByteConversion == null) {
				singleByteConversion = acquireSingleByteConversion(ccsid);
			}
		}
		return singleByteConversion;
	}

	/* String in the specified CCSID */
	public static String createString(byte[] data, int offset, int length,
			int ccsid) throws UnsupportedEncodingException {
		    SingleByteConversion singleByteConversion;

			singleByteConversion = getSingleByteConversion(ccsid);

			char[] toUnicodeTable = singleByteConversion.returnToUnicode();
			synchronized(lock) {
			sb.setLength(0);

			for (int i = 0; i < length; i++) {
				int b = 0xFF & data[i+offset];
				if (b < toUnicodeTable.length) {
					sb.append(toUnicodeTable[b]);
				} else {
					sb.append('\uFFFD');
				}
			}
			return sb.toString();
			}

	}

	static SingleByteConversion acquireSingleByteConversion(int ccsid) throws UnsupportedEncodingException {
		// Attempt to find the shipped table using reflection
		SingleByteConversion singleByteConversion = null;
		Class conversionClass = null ;

		try {
			conversionClass = Class.forName("com.ibm.jtopenlite.ccsidConversion.CCSID"+ccsid);

			Class[] emptyParameterTypes = new Class[0];
			Method method = conversionClass.getMethod("getInstance", emptyParameterTypes);
			Object[] args = new Object[0];
			singleByteConversion = (SingleByteConversion) method.invoke(null, args);

		} catch (ClassNotFoundException exceptionCause) {
            //
			// TODO:   Download tables from the server.
			//
			UnsupportedEncodingException ex = new UnsupportedEncodingException("CCSID="+ccsid);
			ex.initCause(exceptionCause);
			throw ex ;


		} catch (Throwable exceptionCause) {
			UnsupportedEncodingException ex = new UnsupportedEncodingException("CCSID="+ccsid);
			ex.initCause(exceptionCause);
			throw ex ;

		}
		ccsidToSingleByteConversion[ccsid] = singleByteConversion;
		return singleByteConversion;

	}

	public static byte[] stringToEBCDICByteArray(String s, int ccsidToUse) throws UnsupportedEncodingException {
		int sLen = s.length();
		byte[] buffer = new byte[sLen*2];
		int outLen = stringToEBCDICByteArray(s, sLen, buffer, 0, ccsidToUse);
		byte[] outArray = new byte[outLen];
		System.arraycopy(buffer, 0, outArray, 0, outLen);
		return outArray;
	}

	public static int stringToEBCDICByteArray(String s, byte[] data,
			int offset, int ccsidToUse)  throws UnsupportedEncodingException{
		return stringToEBCDICByteArray(s, s.length(), data, offset, ccsidToUse);
	}

	public static int stringToEBCDICByteArray(String s, int length,
			byte[] data, int offset, int ccsidToUse)  throws UnsupportedEncodingException {


	    SingleByteConversion singleByteConversion;

		singleByteConversion = getSingleByteConversion(ccsidToUse);

		byte[] fromUnicodeTable = singleByteConversion.returnFromUnicode();

		for (int i = 0; i < length; i++) {
			int b = s.charAt(i);
			byte x;
			if (b < fromUnicodeTable.length) {
				x = fromUnicodeTable[b];
			} else {
				x = 0x3f;
			}
			data[offset+i]=x;
		}

		return length;

	}

}
