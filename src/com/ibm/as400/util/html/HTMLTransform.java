///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTransform.java
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
*  The HTMLTransform class encodes and decodes a string's tags for use in 
*  an HTMLTagElement's control name, initial value, or displayed text.  There are
*  a set of special characters reserved for creating HTML tags.  Those
*  special characters have a corresponding set of replacement characters that allow
*  users to visually see those characters in a browser.
*
*  <P>For example, if you wanted to set the value attribute of a TextFormInput object
*  to a resource link so you could see the HTML link in the text input box, the HTML link 
*  tag would need to be encoded to see the special characters(&lt;, &gt;, and &quot;):
*  <BLOCKQUOTE><PRE>
*  &lt;input type=&quot;text&quot; name=&quot;myText&quot; value=&quot;&lt;a href=&quot;http://www.myLink.com/&quot;&gt;Link&lt;/a&gt;&quot; /&gt;
*  </PRE></BLOCKQUOTE>
*
*  <P>The following example uses the HTMLEncoder class to encode and decode the value of
*  a TextFormInput so it displays properly:
*  <BLOCKQUOTE><PRE>
*  // The string to use for the TextFormInput value attribute.
*  String s = new String(&quot;&lt;a href=&quot;http://www.myLink.com/&quot;&gt;Link&lt;/a&gt;&quot;);
*  // Encode the string.
*  String e = HTMLTransform.encode(s);
*  // Create the TextFormInput object.
*  TextFormInput input = new TextFormInput(&quot;myText&quot;, e);
*  // Set the input size so the entire value can be seen.
*  input.setSize(45);
*  <br>
*  System.out.println(&quot;TAG: &quot; + input.getTag() + &quot;\n&quot;);
*  // Output the string with the special characters encoded for display in a browser.
*  System.out.println(&quot;Encoded: &quot; + e + &quot;\n&quot;);
*  // Output the string with the specials characters decoded back to the original string.
*  System.out.println(&quot;Decoded: &quot; + HTMLTransform.decode(e));
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is what will be produced:
*  <BLOCKQUOTE><PRE>
*  // The TextFormInput with an encoded string.
*  &lt;input type=&quot;text&quot; name=&quot;myText&quot; value=&quot;&amp;lt;a href=&amp;quot;http://www.myLink.com/&amp;quot;&amp;gt;Link&amp;lt;/a&amp;gt;&quot; size=&quot;45&quot; /&gt;
*  // The encoded string.
*  &amp;lt;a href=&amp;quot;http://www.myLink.com/&amp;quot;&amp;gt;Link&amp;lt;/a&amp;gt;
*  // The decode string.
*  &lt;a href=&quot;http://www.myLink.com/&quot;&gt;Link&lt;/a&gt;
*  </BLOCKQUOTE></PRE>
*
*  <P>Here is what the browser will show:
*  <form>
*  <input type="text" name="myText" value="&lt;a href=&quot;http://www.myLink.com/&quot;&gt;Link&lt;/a&gt;" size="45" />
*  </form>
*  
*  <P>The tags that are encoded include:
*    <UL>
*    <LI>&quot;
*    <LI>&amp;
*    <LI>&lt;
*    <LI>&gt;
*    </UL>
**/
public class HTMLTransform
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /**
    *  Encodes the HTML string, which can contain HTML tags such as &lt; , &gt;, &quot;, or &amp;.
    *  @param source The HTML string containing HTML tags to be encoded.
    *  @return The encoded string.
    **/
    public static String encode(String source)
    {
       if (source == null) 
          throw new NullPointerException("source");

       if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Encoding HTML string...");
       
       StringBuffer dest = new StringBuffer();
       
       for (int i=0; i<source.length(); ++i)
       {
          switch(source.charAt(i))
          {
             case '\"':
               dest.append("&quot;");
               break;
             case '&':
               dest.append("&amp;");
               break;
             case '<':
               dest.append("&lt;");
               break;
             case '>':
               dest.append("&gt;");
               break;
             default:
               dest.append(source.charAt(i));
               break;
          }
       }
       
       return dest.toString();
    }


    /**
    *  Decodes the HTML string, which can contain replacement characters 
    *  for HTML tags such as &amp;lt;, &amp;gt;, &amp;quot;, or &amp;amp;.
    *
    *  @param source The HTML string containing HTML replacement characters to be decoded.
    *
    *  @return The decoded string.
    **/
    public static String decode(String source)
    {
       if (source == null) 
          throw new NullPointerException("source");

       if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Decoding HTML string...");

       StringBuffer dest = new StringBuffer();

       int index = source.indexOf("&");      // location of the first special character set.
       int endIndex = 0;                     // the end of the current special character set.
                                                                                  
       // loop while there are more special characters to decode.
       while (index >= 0)                                                         
       {  
          // if the special characters appear at the beginning of the string.
          if (index == 0)
          {
             // Find which character set is in the first position.
             if (source.indexOf("&quot;") == 0)
             {  
                // Append the replacement character to the buffer if
                // this is the character set found at the beginning of the string.
                // Then set the endIndex to the position after the character set.
                dest.append("\"");
                endIndex = 6;
             }
             else if (source.indexOf("&amp;") == 0)
             {   
                dest.append("&");
                endIndex = 5;
             }
             else if (source.indexOf("&lt;") == 0)
             {   
                dest.append("<");
                endIndex = 4;
             }
             else if (source.indexOf("&gt;") == 0)
             {   
                dest.append(">");
                endIndex = 4;
             }
             
          }
          else                                                                    
          {  
             // find the end semi-colon for the special characters, so we can
             // create a substring to find out which special character it matches.
             int semiColon = source.indexOf(";", endIndex);

             if (source.substring(index, semiColon).equals("&quot"))
             {  
                // first append the characters before the special set of characters
                // and after any special characters already found
                dest.append(source.substring(endIndex, index));
                // append the replacement for the special characters
                dest.append("\"");
                
             }
             else if (source.substring(index, semiColon).equals("&amp"))
             {  
                dest.append(source.substring(endIndex, index));
                dest.append("&");
             
             }
             else if (source.substring(index, semiColon).equals("&lt"))
             {  
                dest.append(source.substring(endIndex, index));
                dest.append("<");
                
             }
             else if (source.substring(index, semiColon).equals("&gt"))
             {  
                dest.append(source.substring(endIndex, index));
                dest.append(">");
                
             }

             // set the starting point for the next indexOf to the end of the special characters found,
             // which is the next position after the semi-colon.
             endIndex = semiColon + 1;

          }
          // find the next occurrence of & after the last one we found.
          index = source.indexOf("&", endIndex);
       }                                                                          
       return dest.toString();
    }
}

