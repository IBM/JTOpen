///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiConvert.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.StringTokenizer;
import java.nio.CharBuffer;

/**
 *  BidiConvert is a convenience class to facilitate invocation of the
 *  Bidi layout transformations by converter code in the JVM. <p>
 *  It contains 2 public methods:  toUnicode() and toSbcs().
 *  <p>
 *  Method toUnicode() must be called  by the ByteToChar converters
 *  after converting the SBCS byte data into Unicode.
 *  <p>
 *  Method toSbcs() must be called by the CharToByte converters before
 *  converting the Unicode string data into byte data.
 *
 */

class BidiConvert
{
/**
 *  Default Bidi parms for Unicode string
 */
    final static char[] DEFAULT_U = new char[] {'I', 'L', 'Y', 'N', 'N', 'U', 'A', 'A', 'A', 'A'};
/**
 *  Default Bidi parms for SBCS string
 */
    final static char[] DEFAULT_S = new char[] {'V', 'L', 'N', 'S', 'N', 'U', 'A', 'A', 'A', 'A'};
/**
 *  Character for default option in Bidi parms
 */
    final static char DEFCHAR = '-';

    // The 5 following members are used to cache the parms and result of
    // isMatchingEncoding(), because it will typically be called several times
    // with the same data, from within isBidiData() and from within toSbcs()
    // or toUnicode().
    private String lastEncoding;
    private String lastBidiParms;
    private boolean lastMatching;
    private BidiFlagSet bfs_U;
    private BidiFlagSet bfs_S;


/**
 *  This method isolates a part with format "X(YYYY)" within a string, where "X"
 *  is a one-letter prefix followed by a left parenthesis, and "YYYY" is an
 *  arbitrary string.
 *  @param  str         The string to scan.
 *  @param  prefix      The one-letter prefix with the left parenthesis.
 *  @return the string within parentheses following the prefix.
 */
    private String getPart(String str, String prefix)
    {
        String          part;
        StringTokenizer stok;

        stok = new StringTokenizer(str, ",)");
        part = "";
        while (stok.hasMoreTokens())
        {
            part = stok.nextToken();
            if (part.startsWith(prefix))
                return part.substring(2);
        }
        return "";
    }

/**
 *  This method isolates a part with format "X(YYYY)" within a string, where "X"
 *  is a one-letter prefix followed by a left parenthesis, and "YYYY" is an
 *  arbitrary string.  It returns it as a char array, completed to the
 *  maximum number of elements from a default array.
 *  @param  str         The string to scan.
 *  @param  prefix      The one-letter prefix with the left parenthesis.
 *  @param  defChars    The default char array
 *  @return the string within parentheses following the prefix.
 */
    private char[] getChars(String str, String prefix, char[] defChars)
    {
        String          part;
        int             len, lenDef;
        char[]          ans;

        part = getPart(str, prefix);
        len = part.length();
        lenDef = defChars.length;
        if (len > lenDef)  len = lenDef;
        ans = new char[lenDef];
        part.getChars(0, len, ans, 0);
        for (int i = 0; i < lenDef; i++)
            if ((i >= len) || (DEFCHAR == ans[i]))  ans[i] = defChars[i];
        return ans;
    }

/**
 *  This method checks if the encoding of the conversion is included in a
 *  list of encodings within bidiParms.  If positive, or if bidiParms
 *  contains no list of encodings, return true; otherwise return false.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @return true if encoding appears in bidiParms, or if bidiParms ignores encodings.
 */
    private boolean isMatchingEncoding(String encoding, String bidiParms)
    {
        String          part, enc;
        StringTokenizer stok;

        if ((encoding == lastEncoding) && (bidiParms == lastBidiParms))
            return lastMatching;

        lastEncoding = encoding;
        lastBidiParms = bidiParms;
        bfs_U = null;
        bfs_S = null;
        part = getPart(bidiParms, "C(");
        if (part.equals(""))  return (lastMatching = true);

        stok = new StringTokenizer(part, ";");
        while (stok.hasMoreTokens())
        {
            enc = stok.nextToken();
            if (enc.equals(encoding))  return (lastMatching = true);
        }
        return (lastMatching = false);
    }

/**
 *  This method checks the orientation if RTL in either the U or S part of Bidi parms.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @return             true if either orientation is RTL.
 */
    private boolean isFlagRtl(String bidiParms)
    {
        BidiFlag bf;

        if (null == bfs_U)  bfs_U = new BidiFlagSet(getChars(bidiParms, "U(", DEFAULT_U));
        bf = bfs_U.getOrientation();
        if (BidiFlag.ORIENTATION_RTL == bf)  return true;
        if (BidiFlag.ORIENTATION_CONTEXT_RTL == bf)  return true;
        if (null == bfs_S)  bfs_S = new BidiFlagSet(getChars(bidiParms, "S(", DEFAULT_S));
        bf = bfs_S.getOrientation();
        if (BidiFlag.ORIENTATION_RTL == bf)  return true;
        if (BidiFlag.ORIENTATION_CONTEXT_RTL == bf)  return true;
        return false;
    }

/**
 *  This method checks if a char array contains R or AL characters.
 *  @param  input       The input char array containing text in Unicode.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @return             true if the string needs Bidi transformation.
 */
    private boolean isBidiData(char[] input, int inStart, int inEnd)
    {
        char c;

        for (int i = inStart; i < inEnd; i++)
        {
            c = input[i];
            // Hebrew, Arabic, Syria, Thaana
            if ((c >= 0x0590) && (c <= 0x07BF))  return true;
            // Hebrew Presentation Forms and Arabic Presentation Forms-A
            if ((c >= 0xFB1D) && (c <= 0xFDFF))  return true;
            // Arabic Presentation Forms-B
            if ((c >= 0xFE70) && (c <= 0xFEFC))  return true;
            // RLM
            if (c == 0x200F)  return true;
            // RLE, RLO
            if ((c == 0x202B) || (c == 0x202E))  return true;
        }
        return false;
    }

/**
 *  This method checks if the environment allows Bidi transformations for the current encoding.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return             true if the environment allows Bidi transformations for the current encoding.
 */
    public boolean isBidiEnv(String bidiParms, String encoding)
    {
        if (encoding.equals("Cp850"))  return false;
        Mlog.putLine( "isBidiEnv " +  bidiParms + " encoding=" + encoding );
//        if (bidiParms == null)  return false;
        if (bidiParms == null)  return Mlog.logReturn( false );
//        if (bidiParms.equals("NO"))  return false;
        if (bidiParms.equals("NO"))  return Mlog.logReturn( false );
//        if (!isMatchingEncoding(encoding, bidiParms))  return false;
        if (!isMatchingEncoding(encoding, bidiParms))  return Mlog.logReturn( false );
//        return true;
        return Mlog.logReturn( true );
    }

/**
 *  This method checks if a char array needs Bidi transformation.
 *  @param  input       The input char array containing text in Unicode.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return             true if the string needs Bidi transformation.
 */
    public boolean isBidiData(char[] input, int inStart, int inEnd, String bidiParms, String encoding)
    {
        if (false == isBidiEnv( bidiParms, encoding ))  return false;
        Mlog.putLine( "isBidiData_char[]  inStart=" + inStart + "  inEnd=" + inEnd );
        Mlog.putHexLine( input, inStart, inEnd, true );
//        if (inEnd <= inStart)  return false
        if (inEnd <= inStart)  return Mlog.logReturn( false );
//        if (isFlagRtl(bidiParms))  return true;
        if (isFlagRtl(bidiParms))  return Mlog.logReturn( true );
//        return isBidiData( input, inStart, inEnd );
        return Mlog.logReturn( isBidiData( input, inStart, inEnd ) );
    }

/**
 *  This method checks if a string needs Bidi transformation.
 *  @param  input       The input string.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return             true if the string needs Bidi transformation.
 */
    public boolean isBidiData(String input, String bidiParms, String encoding)
    {
        if (false == isBidiEnv( bidiParms, encoding ))  return false;
        Mlog.putLine( "isBidiData_String" );
        Mlog.putHexLine( input, true );
//        if (input.length() <= 0)  return false
        if (input.length() <= 0)  return Mlog.logReturn( false );
//        if (isFlagRtl(bidiParms))  return true;
        if (isFlagRtl(bidiParms))  return Mlog.logReturn( true );
        char[] text = input.toCharArray();
//        return isBidiData( text, 0, text.length );
        return Mlog.logReturn( isBidiData( text, 0, text.length ) );
    }

/**
 *  This method checks if a CharBuffer needs Bidi transformation.
 *  @param  input       The input buffer.
 *  @param  UseBuffer   flag to prevent use of char array in CharBuffer.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return             true if the buffer needs Bidi transformation.
 */
    public boolean isBidiData(CharBuffer input, boolean UseBuffer, String bidiParms, String encoding)
    {
        char[]          text;
        boolean         flag;

        if (false == isBidiEnv( bidiParms, encoding ))  return false;
        Mlog.putLine( "isBidiData_CharBuffer  UseBuffer=" + UseBuffer +
                      "  position=" + input.position() + "  limit=" + input.limit() );
        Mlog.putHexLine( input.toString(), true );
//        if (!input.hasRemaining())  return false
        if (!input.hasRemaining())  return Mlog.logReturn( false );
//        if (isFlagRtl(bidiParms))  return true;
        if (isFlagRtl(bidiParms))  return Mlog.logReturn( true );

        if (input.hasArray() && !UseBuffer)
        {
            text = input.array();
            int offset = input.arrayOffset();
            flag = isBidiData( text, offset + input.position(), offset + input.limit() );
//            return flag;
            return Mlog.logReturn( flag );
        }
        // We need to save the buffer state, but don't want to use mark() since
        // the caller may have set his own mark.
        int oldPos = input.position();
        text = new char[1];
        flag = false;
        while (input.hasRemaining())
        {
            text[0] = input.get();
            if (isBidiData( text, 0, 1 ))
            {
                flag = true;
                break;
            }
        }
        // Reset the buffer to its original state
        input.position( oldPos );
//        return flag;
        return Mlog.logReturn( flag );
    }

/**
 *  This method creates a char array from a subset of another char array.
 *  @param  input       The input char array.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @return             The char array containing characters from inStart to inEnd.
 */
    private char[] subchars(char[] input, int inStart, int inEnd)
    {
        int len = inEnd - inStart;
        char[] result = new char[len];
        System.arraycopy( input, inStart, result, 0, len );
        return result;
    }

/**
 *  This method converts a char array from the Bidi attributes used outside the
 *  JVM to those used inside the JVM.
 *  @param  input       The input char array.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @return The input char array transformed to the layout specified by bidiParms.
 */
    private char[] toUnicode(char[] input, int inStart, int inEnd, String bidiParms)
    {
        char[]          charsU, charsS;
        BidiTransform   bdx;
        BidiText        src, dst;

        charsU = getChars(bidiParms, "U(", DEFAULT_U);
        charsS = getChars(bidiParms, "S(", DEFAULT_S);
        bdx = new BidiTransform();
        if (null == bfs_U)  bfs_U = new BidiFlagSet(charsU);
        bdx.flags = bfs_U;
        bdx.roundTrip = ('R' == charsU[5]);
        bdx.options = new ArabicOptionSet(charsU);

        if (null == bfs_S)  bfs_S = new BidiFlagSet(charsS);
        src = new BidiText( bfs_S );
        src.setCharsRef( input, inStart, inEnd - inStart );
        dst = src.transform(bdx);
        return dst.data;
    }

/**
 *  This method converts a char array from the Bidi attributes used outside the
 *  JVM to those used inside the JVM.
 *  @param  input       The input char array.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return The input char array transformed to the layout specified by bidiParms.
 */
    public char[] toUnicode(char[] input, int inStart, int inEnd, String bidiParms, String encoding)
    {
        Mlog.printStackTrace( "toUnicode_char[]") ;
        Mlog.putLine( "toUnicode_char[]  inStart=" + inStart + "  inEnd=" + inEnd );
        Mlog.putHexLine( input, inStart, inEnd, true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return subchars( input, inStart, inEnd);
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( subchars( input, inStart, inEnd) );
//        return toUnicode( input, inStart, inEnd, bidiParms );
        return Mlog.logReturn( toUnicode( input, inStart, inEnd, bidiParms ) );
    }

/**
 *  This method converts a string from the Bidi attributes used outside the
 *  JVM to those used inside the JVM.
 *  @param  input       The input string.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return The input string transformed to the layout specified by bidiParms.
 */
    public String toUnicode(String input, String bidiParms, String encoding)
    {
        Mlog.printStackTrace( "toUnicode_String" );
        Mlog.putLine( "toUnicode_String input:" );
        Mlog.putHexLine( input, true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return input;
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( input );
//        return new String( toUnicode(input.toCharArray(), 0, input.length(), bidiParms) );
        return new String( Mlog.logReturn( toUnicode(input.toCharArray(), 0, input.length(), bidiParms ) ) );
    }

/**
 *  This method converts a CharBuffer from the Bidi attributes used outside the
 *  JVM to those used inside the JVM.
 *  @param  input       The input buffer.
 *  @param  UseBuffer   flag to prevent use of char array in CharBuffer.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return another CharBuffer with its data input string transformed to the layout specified by bidiParms.
 */
    public CharBuffer toUnicode(CharBuffer input, boolean UseBuffer, String bidiParms, String encoding)
    {
        char[]          text;

        Mlog.printStackTrace( "toUnicode_CharBuffer" );
        Mlog.putLine( "toUnicode_CharBuffer  UseBuffer=" + UseBuffer +
                      "  position=" + input.position() + "  limit=" + input.limit() );
        Mlog.putHexLine( input.toString(), true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return input;
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( input );

        if (input.hasArray() && !UseBuffer)
        {
            text = input.array();
            int offset = input.arrayOffset();
            text = toUnicode( text, offset + input.position(), offset + input.limit(), bidiParms );
            Mlog.putLine( "after array: position=" + input.position() + "  limit=" + input.limit() );
//            return CharBuffer.wrap( text );
            return Mlog.logReturn( CharBuffer.wrap( text ) );
        }
        // We need to save the buffer state, but don't want to use mark() since
        // the caller may have set his own mark.
        int oldPos = input.position();
        // Since the Bidi layout code is geared towards char arrays, we have no
        // option but to extract the data for the buffer into an array
        text = new char[input.remaining()];
        input.get( text );
        // Reset the buffer to its original state
        input.position( oldPos );
        text = toUnicode( text, 0, text.length, bidiParms );
        Mlog.putLine( "after buffer: position=" + input.position() + "  limit=" + input.limit() );
//        return CharBuffer.wrap( text );
        return Mlog.logReturn( CharBuffer.wrap( text ) );
    }

/**
 *  This method converts a char array from the Bidi attributes used inside the
 *  JVM to those used outside the JVM.
 *  @param  input       The input char array.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @return The input char array transformed to the layout specified by bidiParms.
 */
    private char[] toSbcs(char[] input, int inStart, int inEnd, String bidiParms)
    {
        char[]          charsU, charsS;
        BidiTransform   bdx;
        BidiText        src, dst;

        charsU = getChars(bidiParms, "U(", DEFAULT_U);
        charsS = getChars(bidiParms, "S(", DEFAULT_S);
        bdx = new BidiTransform();
        if (null == bfs_S)  bfs_S = new BidiFlagSet(charsS);
        bdx.flags = bfs_S;
        bdx.roundTrip = ('R' == charsS[5]);
        bdx.options = new ArabicOptionSet(charsS);

        if (null == bfs_U)  bfs_U = new BidiFlagSet(charsU);
        src = new BidiText( bfs_U );
        src.setCharsRef( input, inStart, inEnd - inStart );
        dst = src.transform(bdx);
        return dst.data;
    }

/**
 *  This method converts a char array from the Bidi attributes used inside the
 *  JVM to those used outside the JVM.
 *  @param  input       The input char array.
 *  @param  inStart     Offset in input array.
 *  @param  inEnd       Offset of last byte to be converted.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return The input char array transformed to the layout specified by bidiParms.
 */
    public char[] toSbcs(char[] input, int inStart, int inEnd, String bidiParms, String encoding)
    {
        BidiTransform   bdx;
        BidiText        src, dst;

        Mlog.printStackTrace( "toSbcs_char[]" );
        Mlog.putLine( "toSbcs_char[]  inStart=" + inStart + "  inEnd=" + inEnd );
        Mlog.putHexLine( input, inStart, inEnd, true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return subchars( input, inStart, inEnd);
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( subchars( input, inStart, inEnd) );
//        return toSbcs( input, inStart, inEnd, bidiParms );
        return Mlog.logReturn( toSbcs( input, inStart, inEnd, bidiParms ) );
    }

/**
 *  This method converts a string from the Bidi attributes used inside the
 *  JVM to those used outside the JVM.
 *  @param  input       The input string.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return The input string transformed to the layout specified by bidiParms.
 */
    public String toSbcs(String input, String bidiParms, String encoding)
    {
        Mlog.printStackTrace( "toSbcs_String input:" );
        Mlog.putHexLine( input, true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return input;
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( input );
//        return new String( toSbcs( input.toCharArray(), 0, input.length(), bidiParms ) );
        return Mlog.logReturn( new String( toSbcs( input.toCharArray(), 0, input.length(), bidiParms ) ) );
    }

/**
 *  This method converts a CharBuffer from the Bidi attributes used inside the
 *  JVM to those used outside the JVM.
 *  @param  input       The input buffer.
 *  @param  UseBuffer   flag to prevent use of char array in CharBuffer.
 *  @param  bidiParms   The environment variable containing the specifications of the Bidi attributes.
 *  @param  encoding    The encoding of the data outside the JVM.
 *  @return another CharBuffer with its data input string transformed to the layout specified by bidiParms.
 */
    public CharBuffer toSbcs(CharBuffer input, boolean UseBuffer, String bidiParms, String encoding)
    {
        char[]          text;

        Mlog.printStackTrace( "toSbcs_CharBuffer" );
        Mlog.putLine( "toSbcs_CharBuffer  UseBuffer=" + UseBuffer +
                      "  position=" + input.position() + "  limit=" + input.limit() );
        Mlog.putHexLine( input.toString(), true );
//        if (false == isBidiEnv( bidiParms, encoding ))  return input;
        if (false == isBidiEnv( bidiParms, encoding ))
            return Mlog.logReturn( input );

        if (input.hasArray() && !UseBuffer)
        {
            text = input.array();
            int offset = input.arrayOffset();
            text = toSbcs( text, offset + input.position(), offset + input.limit(), bidiParms );
            Mlog.putLine( "after array: position=" + input.position() + "  limit=" + input.limit() );
//            return CharBuffer.wrap( text );
            return Mlog.logReturn( CharBuffer.wrap( text ) );
        }
        // We need to save the buffer state, but don't want to use mark() since
        // the caller may have set his own mark.
        int oldPos = input.position();
        // Since the Bidi layout code is geared towards char arrays, we have no
        // option but to extract the data for the buffer into an array
        text = new char[input.remaining()];
        input.get( text );
        // Reset the buffer to its original state
        input.position( oldPos );
        text = toSbcs( text, 0, text.length, bidiParms );
        Mlog.putLine( "after buffer: position=" + input.position() + "  limit=" + input.limit() );
//        return CharBuffer.wrap( text );
        return Mlog.logReturn( CharBuffer.wrap( text ) );
    }

}
