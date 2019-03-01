///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Mlog.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2005-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import sun.misc.MessageUtils;

/**
 *  Mlog is a debugging tool.
 *
 *  <p><b>Multi-threading considerations:</b>
 *  Probably not thread-safe if several threads have access to the same
 *  instance.
 *
 */

import java.nio.CharBuffer;

/* *
 * This class is used for tracing purposes
 *
 */
class Mlog
{
    final static char[] hexa = new char[] {'0','1','2','3','4','5','6','7',
                                           '8','9','a','b','c','d','e','f'};

    static int loRange = 100;
    static int hiRange = 100;
    static int cnt = 1;

    static boolean logFlag;

    public static boolean logEnable( boolean newFlag )
    {
        boolean oldFlag = logFlag;
        logFlag = newFlag;
        return oldFlag;
    }

    public static void putLine( String s )
    {
        if (!logFlag)  return;
        int len = s.length();
        if (len <= (loRange + hiRange))
            MessageUtils.out( cnt++ + ": " + s );
        else
        {
            MessageUtils.out( cnt++ + ": " + s.substring( 0, loRange ) );
            MessageUtils.out( ". . . " + (len - loRange - hiRange) + " chars omitted . . ." );
            MessageUtils.out( s.substring( len - hiRange, len ) );
        }
    }

    public static String toHex( char[] chars, boolean compact )
    {
        return toHex( chars, 0, chars.length, compact );
    }

    public static String toHex( char[] input, int offset, int limit, boolean compact )
    {
        if (limit <= offset)  return "";
        int len = limit - offset;
        char[] chars = new char[len * 5];
        int k = 0;
        int c;
        for (int i = offset; i < limit; i++)
        {
            c = input[i];
            if (c < 0x100)
            {
                if (!compact)
                {
                    chars[k++] = '0';
                    chars[k++] = '0';
                }
            }
            else
            {
                chars[k++] = hexa[c >> 12];
                chars[k++] = hexa[(c & 0x0F00) >> 8];
            }
            chars[k++] = hexa[(c & 0x00F0) >> 4];
            chars[k++] = hexa[c & 0x000F];
            chars[k++] = ' ';
        }
        return new String( chars, 0, k - 1 );
    }

    public static String toHex( String s, boolean compact )
    {
        return toHex( s.toCharArray(), 0, s.length(), compact );
    }

    public static void putHexLine( char[] input, boolean compact )
    {
        if (logFlag)  putHexLine( input, 0, input.length, compact );
    }

    public static void putHexLine( char[] input, int offset, int limit, boolean compact )
    {
        if (!logFlag)  return;
        int len = limit - offset;
        if (len <= (loRange + hiRange))
        {
            MessageUtils.out( cnt   + ": " + toHex( input, offset, limit, compact ) );
            MessageUtils.out( cnt++ + ": " + new String( input, offset, limit - offset ) );
        }
        else
        {
            MessageUtils.out( cnt   + ": " + toHex( input, offset, offset + loRange, compact ) );
            MessageUtils.out( ". . . " + (len - loRange - hiRange) + " chars omitted . . ." );
            MessageUtils.out( toHex( input, limit - hiRange, limit, compact ) );
            MessageUtils.out( cnt++ + ": " + new String( input, offset, loRange ) );
            MessageUtils.out( ". . . " + (len - loRange - hiRange) + " chars omitted . . ." );
            MessageUtils.out( new String( input, limit - hiRange, hiRange ) );
        }
    }

    public static void putHexLine( String s )
    {
        if (logFlag)  putHexLine( s.toCharArray(), false );
    }

    public static void putHexLine( String s, boolean compact )
    {
        if (logFlag)  putHexLine( s.toCharArray(), compact );
    }

    public static boolean logReturn( boolean flag )
    {
        if (logFlag)  putLine( "return flag = " + flag );
        return flag;
    }

    public static char[] logReturn( char[] chars )
    {
        if (logFlag)
        {
            putLine( "return chars = " + toHex( chars, true ) );
            cnt--;
            putLine( "return chars = " + new String( chars ) );
        }
        return chars;
    }

    public static String logReturn( String s )
    {
        if (logFlag)
        {
            putLine( "return string = " + toHex( s, true ) );
            cnt--;
            putLine( "return string = " + s );
        }
        return s;
    }

    public static CharBuffer logReturn( CharBuffer cb )
    {
        if (logFlag)
        {
            putLine( "return buffer = " + toHex( cb.toString(), true ) );
            cnt--;
            putLine( "return buffer = " + cb.toString() );
        }
        return cb;
    }

    public static void printStackTrace( String s )
    {
        if (!logFlag)  return;
        Exception e = new Exception();
        StackTraceElement[] stack = e.getStackTrace();
        putLine( "Entering:  " + s );
        for (int i = 1; i < stack.length; i++)
        {
            cnt--;
            putLine( "   " + stack[i].toString() );
        }
    }

}

