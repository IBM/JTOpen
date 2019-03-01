///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Formatter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.lang.String;
import java.lang.StringBuffer;
import java.lang.Integer;
import java.util.StringTokenizer;
import com.ibm.as400.access.Trace;

/**
 * Implements a printf style formatter.<br>
 * The format specifier is {v,w,j} where v is the element number in the arg list<br>
 * for the variable to be formatted, w is the field width, and the j values are:<br>
 *   r: Right Justification, allow overflow if l > w<br>
 *   R: Right Justification, do not allow overflow<br>
 *   l: Left Justification, allow overflow if l > w<br>
 *   L: Left Justification, do not allow overflow<br>
 * 
 */
abstract class Formatter {
	private static final String CLASS="Formatter";	
	/**
	 * send a justified string with the specifed arguments to Trace.log. 
	 * @param fmt       Format of this string.                       
	 * @param args      arguments to be formatted.                   
	 */
	public static void jprintf(String fmt, Object[] args) {
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + "jprintf() " + jsprintf(fmt, args));
		}
	}

	/**
	 * create a justified string with the specifed arguments and a maximum length.
	 * @param maxlen    maximum length of this String
	 * @param fmt       Format of this string.                       
	 * @param args      arguments to be formatted.
	 * @return		String
	 */
	public static String jsnprintf(int maxlen, String fmt, Object[] args) {
		StringBuffer out= new StringBuffer(maxlen);
		StringTokenizer t= new StringTokenizer(fmt, "{}", true);

		int s= 0;
		String token;

		try {
			while (true) {
				token= t.nextToken();
				if (!token.equals("{")) {
					out.append(token);
				} else {
					token= t.nextToken();
					if (token.equals("{")) {
						out.append(token);
					} else {
						StringBuffer varspec= new StringBuffer(20);
						while (!token.equals("}")) {
							varspec.append(token);
							token= t.nextToken();
						}

						// Now var contains a format spec
						StringTokenizer vars= new StringTokenizer(varspec.toString(), ", ", false);
						int n= Integer.decode(vars.nextToken()).intValue();
						if (vars.hasMoreElements() == false) {
							out.append(args[n]);
						} else {
							int w= Integer.decode(vars.nextToken()).intValue();
							if (w < 0) {
								w= -w;
							}
							String j= vars.nextToken();
							String x= args[n].toString();
							int l= x.length();

							// r: Right Justification, allow overflow if l > w
							// R: Right Justification, do not allow overflow
							// l: Left Justification, allow overflow if l > w
							// L: Left Justification, do not allow overflow

							if (j.equalsIgnoreCase("r")) {
								if (l < w) {
									out.append(repeater(" ", w - l)).append(x);
								} else if (j.equals("r")) {
									out.append(x);
								} else {
									out.append(x.substring(l - w));
								}
							} else if (j.equalsIgnoreCase("l")) {
								if (l < w) {
									out.append(x).append(repeater(" ", w - l));
								} else if (j.equals("l")) {
									out.append(x);
								} else {
									out.append(x.substring(0, w));
								}
							} else {
								out.append(args[n]);
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			// At some point, getting a token or possibly dereffing args[n] will fail.  Just return what was formatted so far.
		}

		// Truncate output if a max length was set.
		if (maxlen > 0) {
			if (out.length() > maxlen) {
				out.setLength(maxlen);
			}
		}

		return out.toString();
	}

	/**
	 * create a justified string with the specifed arguments. 
	 * @param fmt       Format of this string.                       
	 * @param args      arguments to be formatted.
	 * @return		String
	 */
	public static String jsprintf(String fmt, Object[] args) {
		return jsnprintf(0, fmt, args);
	}

	/**
	 * creates a string consisting of s repeated n times.
	 * @param string         String to repeat.                
	 * @param number         number of times to repeat s.
	 * @return		String
	 */
	public static String repeater(String s, int n) {
		if (n <= 0) {
			return "";
		}
		StringBuffer out= new StringBuffer(s.length() * n);
		while (n-- > 0) {
			out.append(s);
		}
		return out.toString();
	}
}
