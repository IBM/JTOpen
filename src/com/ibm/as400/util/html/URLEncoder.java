///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

/**
*  The URLEncoder class encodes a string's delimiters for use in an HTML URL string.
*  Information on encoding standards for Universal Resource Identifiers in WWW 
*  can be found at http://www.ietf.org/rfc/rfc1630.txt.
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
*    <LI>(space character)
*    <LI>&quot;
*    <LI>#
*    <LI>%
*    <LI>&amp;
*    <LI>\
*    <LI>:
*    <LI>;
*    <LI>&lt;
*    <LI>=
*    <LI>&gt;
*    <LI>?
*    <LI>@
*    <LI>[
*    <LI>/
*    <LI>]
*    <LI>^
*    <LI>{
*    <LI>|
*    <LI>}
*    <LI>~
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
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "   Preparing to encode URL string.");
        
        StringBuffer s = new StringBuffer();

        int length = url.length();

        for (int i = 0; i < length; i++) 
        {
           switch(url.charAt(i))
           {
            
           case ' ':
              s.append("%20");
              break;
              
           case '\"':
              s.append("%22");
              break;

           case '#':
              s.append("%23");
              break;

           case '%':
              s.append("%25");
              break;

           case '&':
              s.append("%26");
              break;

           case '/':
              s.append("%2f");
              break;

           case ':':
              s.append("%3a");
              break;

           case ';':
              s.append("%3b");
              break;

           case '<':
              s.append("%3c");
              break;

           case '=':
              s.append("%3d");
              break;

           case '>':
              s.append("%3e");
              break;

           case '?':
              s.append("%3f");
              break;

           case '@':
              s.append("%40");
              break;
              
           case '[':
              s.append("%5b");
              break;
              
           case '\\':
              s.append("%5c");
              break;
                    
           case ']':
              s.append("%5d");
              break;

           case '^':
              s.append("%5e");
              break;

           case '{':
              s.append("%7b");
              break;

           case '|':
              s.append("%7c");
              break;

           case '}':
              s.append("%7d");
              break;

           case '~':
              s.append("%7e");
              break;

           default:
              s.append(url.charAt(i));
              break;

           }
        }
        return s.toString();
    }
}
