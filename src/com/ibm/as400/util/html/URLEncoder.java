///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: URLEncoder.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;

import java.util.StringTokenizer;

/**
*  The URLEncoder class encodes a string's delimiters for use in an HTML URL string.
*
*  <P>For example, the following HTML URL string is not valid and would need to be encoded:
*  <BLOCKQUOTE><PRE>
*  http://mySystem.myCompany.com/servlet/myServlet?parm1="/library/test1#partA"&parm2="/library/test2#partB"
*  </PRE></BLOCKQUOTE>
*
*  <P>The following example uses the URLEncoder class to encode two URL string parameter values:
*  <BLOCKQUOTE><PRE>
*  HTMLForm form = new HTMLForm();
*  String action = "http://mySystem.myCompany.com/servlet/myServlet";
*  String parm1 = "parm1=" + URLEncoder.encode("\"/library/test1#partA\"");
*  String parm2 = "parm2=" + URLEncoder.encode("\"/library/test2#partB\"");
*  form.setURL(action + "?" + parm1 + "&" + parm2);
*  </PRE></BLOCKQUOTE>
*  
*  <P>The delimiters that are encoded include:
*    <UL>
*    <LI>The ASCII characters 'a' through 'z', 'A' through 'Z', and '0' through '9' remain the same.
*    <LI>The space character ' ' is converted into a plus sign '+'.
*    <LI>All other characters are converted into the 3-character string "%xy", where xy is the two-digit hexadecimal
*        representation of the lower 8-bits of the character. 
*    </UL>
**/
public class URLEncoder 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /**
    *  Encodes the URL.
    *  @param url The URL to be encoded.
    *  @return The encoded string.
    **/
    static public String encode(String url)
    {
        return encode(url, true);                                                //$B1A @B2C
    }

    /**
    *  Encodes the URL.
    *  @param url The URL to be encoded.
    *  @param encodePath true if the "/" is encoded in the url; false otherwise.  The default is true.
    *  @return The encoded string.
    **/
    static public String encode(String url, boolean encodePath)                 //$B1A @B2C
    {

        if (url == null)                                                        //$B1A
            throw new NullPointerException("url");                //$B1A

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Preparing to encode URL string.");

        if (encodePath)                                                         // @B2A
            return java.net.URLEncoder.encode(url);                 // @B2A
        else                                                                         // @B2A
        {
            // @B2A
            StringBuffer s = new StringBuffer();                                 // @B2A
            String next;                                                                  // @B2A

            StringTokenizer token = new StringTokenizer(url, "/", true);         // @B2A
            while (token.hasMoreTokens())                                                  // @B2A
            {
                // @B2A
                next = token.nextToken();                                                      // @B2A
                if (next.equals("/"))                                                               // @B2A
                    s.append(next);                                                                // @B2A
                else                                                                                    // @B2A
                    s.append(java.net.URLEncoder.encode(next));                    // @B2A
            }                                                                                            // @B2A

            // Must change the '+' to a space since some of the webserver   
            // engines don't properly decode the encoded url string.
//            return s.toString().replace('+', ' ');                                            // @C1C
// - use %20 instead of ' '
            token = new StringTokenizer(s.toString(), "+", true);
            s = new StringBuffer();
            while (token.hasMoreTokens())
            {
              next = token.nextToken();
              if (next.equals("+"))
              {
                s.append("%20");
              }
              else
              {
                s.append(next);
              }
            }
            return s.toString();            
        }
    }
}
