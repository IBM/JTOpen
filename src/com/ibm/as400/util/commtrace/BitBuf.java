///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: BitBuf.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.io.StringWriter;

/**
 * The BitBuf class is an abstraction for an arbitrarily long string of bits.<br>
 * Its methods allow bit shifting, substring extraction, converting substrings
 * to bytes, shorts, longs, or floats, and representing a bit string as a 
 * sequence of binary or hexadecimal digits.<br>
 * A new BitBuf can be constructed from another BitBuf or from a byte array.
 */
class BitBuf {
    private byte data[];
    private int bitlen; // used if not a byte-boundary;
    private static StringBuffer binbyte = new StringBuffer("01234567");
    private static StringBuffer hexbyte = new StringBuffer("FF");
    private final int masks[] = { 128, 64, 32, 16, 8, 4, 2, 1 };
    private final char hexchars[] =
        {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F' };

	/**
	 * Constructs a BitBuf from a byte array.
	 * @param bytes     The byte array. 
	 */
    public BitBuf(byte[] bytes) {
        data = (byte[]) bytes.clone();
        bitlen = data.length * 8;
    }

    /**
     * Constructor for making a BitBuf from an integer array.
     * Note that only the lowest 8 bits of each integer are
     * used.  This is useful for code such as<br>
     * <pre>
     *      int ary[] = {0x01, 0xFF, 0x5E};
     *      BitBuf b = new BitBuf(ary);
     * </pre>
     * which would be bulkier if a byte array were used instead
     * because of the need for implicit type casting of each
     * element of array.
     * @param ints 	    The integer array
     */
    public BitBuf(int[] ints) {
        data = new byte[ints.length];
        for (int i = 0; i < ints.length; i++)
            data[i] = (byte) ints[i];
        bitlen = data.length * 8;
    }

    /**
     * Constructs an BitBuf of length 8 from a single byte.
     * @param byte	    The byte
     */
    public BitBuf(byte b) {
        data = new byte[1];
        data[0] = b;
        bitlen = data.length * 8;
    }

    /**
     * Constructs a zero-filled BitBuf of length i*8.
     * @param int	    The length of the BitBuf.
     */
    public BitBuf(int i) {
        data = new byte[i];
        bitlen = data.length * 8;
    }

    /**
     * Constructs a BitBuf from a substring of another BitBuf.
     * @param b	    The BitBuf to copy bytes from.
     * @param bitstart The bit to start copying at.
     * @param bitlength the number of bits to copy.
     */
    public BitBuf(BitBuf b, int bitstart, int bitlength) {
        if (bitlength < 0)
            bitlength = 0;
        bitlen = bitlength;
        int newlen = (bitlength + 7) / 8;
        data = new byte[newlen];
        for (int x = 0; x < newlen; x++)
            data[x] = b.getOctet(bitstart + 8 * x);
        truncate();
    }


    /**
     * Returns a string of binary digits representing the 8 bits starting at bit i.
     * @param i	    the bit to start the binary string at.
     * @return	    String
     */
    public String byteAsBin(int i) {
        int x;
        for (x = 7; x >= 0; x--)
            binbyte.setCharAt(x, ((data[i] & 0xFF & masks[x]) > 0) ? '1' : '0');
        return binbyte.toString();
    }

    /**
     * Returns a string of hex digits representing the 8 bits starting at bit i.
     * @param i	    the bit to start the hex string at.
     * @return	    String
     */
    public String byteAsHex(int i) {
        hexbyte.setCharAt(0, hexchars[data[i] >>> 4 & 0xF]);
        hexbyte.setCharAt(1, hexchars[data[i] & 0xF]);
        return hexbyte.toString();
    }

    /**
     * Returns a clone of a BitBuf casted as an Object.
     * @return	    Object a clone of this BitBuf.
     */
    public Object clone() {
        BitBuf b = new BitBuf(data);
        return b;
    }

    /**
     * Returns a boolean representing one bit (at offset bit) of the BitBuf.
     * @param bit   the offset bit to return a boolean of.
     * @return	    true is on, false if off 
     */
    public boolean getBitAsBool(int bit) {
        int byteloc = bit / 8;
        int bitloc = bit % 8;
        if ((data[byteloc] & 0xFF & masks[bitloc]) > 0)
            return true;
        else
            return false;
    }

    /**
     * Returns a byte representing one bit (at offset bit) of the BitBuf.
     * @param bit   the offset bit.
     * @return	    the byte at this offset.
     */
    public byte getBitAsByte(int bit) {
        int byteloc = bit / 8;
        int bitloc = bit % 8;
        if ((data[byteloc] & 0xFF & masks[bitloc]) > 0)
            return (byte) 1;
        else
            return (byte) 0;
    }

    /**
     * Returns the length of the BitBuf in bits.
     * @return Th Length of this BitBuf in bits.
     */
    public int getBitSize() {
        return bitlen;
    }

    /**
     * Returns a clone of the byte array used to store the data in a BitBuf.
     * @return	    byte[] clone of the data in this BitBuf.
     */
    public byte[] getBytes() {
        return (byte[]) data.clone();
    }

    /**
     * Returns the length of the BitBuf in bytes.  For example, a BitBuf
     * with a bit length of 3 would have a byte length of 1.  
     * @return	    length of this BitBuf in bytes
     */
    public int getByteSize() {
        return data.length;
    }

    /**
     * Returns a byte consisting of the 8 bits in the BitBuf starting at startbit.
     * @param startbit	bit to start the byte at
     * @return byte 
     */
    public byte getOctet(int startbit) {
        // Gets 8 bits starting at the bit location bitloc
        byte b;
        if ((startbit >= bitlen) || (startbit < -8))
            b = 0;
        else
            if (startbit < 0)
                b = (byte) ((data[0] & 0xFF) >>> (0 - startbit));
            else {
                int byteloc = startbit / 8;
                int bitshift = startbit % 8;
                b = data[byteloc];
                b <<= bitshift;
                if ((bitshift > 0) && (startbit + 8 < bitlen))
                    b |= (data[byteloc + 1] & 0xFF) >>> (8 - bitshift);
            }
        return b;
    }

    /**
     * Shifts the BitBuf left by x bits, shifting zeros in on the right.
     * @param int length to shift to the left
     */
    public void shiftLeft(int x) {
	    shiftBufferLeft(x);
    }
    
    /**
     * Shifts the BitBuf right by x bits, shifting zeros in on the left.<br>
     * Slack bits remain zero (meaning that 11101 shifted right by 2 is 00111, 
     * even though internally the pattern would be stored using 8 bytes)
     * @param int 	   length to shift to the right
     */
    public void shiftRight(int x) {
        shiftBufferRight(x);
        truncate();
    }
    
    /**
     * Efficient left-shifting of entire buffer.<br>
     * First, byte-resolution shifting is done in one pass, 
     * then bit-resolution in one more pass through the buffer.
     * @param length	    length to shift left
     */
    public void shiftBufferLeft(int d) {
        int byteshift = d / 8;
        int bitshift = d % 8;
        if (byteshift > 0)
            for (int i = 0; i < data.length; i++)
                if (i < (data.length - byteshift))
                    data[i] = data[i + byteshift];
                else
                    data[i] = 0;
        if (bitshift > 0)
            for (int i = 0; i < data.length; i++)
                if (i < (data.length - 1))
                    data[i] =
                        (byte) ((data[i] << bitshift) | ((data[i + 1] & 0xFF) >>> (8 - bitshift)));
                else
                    data[i] <<= bitshift;
    }
   
    /**
     * Efficient right-shifting of entire buffer.<br>
     * First, byte-resolution shifting is done in one pass, 
     * then bit-resolution in one more pass through the buffer.
     * @param length	    length to shift right
     */
    public void shiftBufferRight(int d) {
        int byteshift = d / 8;
        int bitshift = d % 8;
        if (byteshift > 0)
            for (int i = data.length; i-- > 0;)
                if (i >= byteshift)
                    data[i] = data[i - byteshift];
                else
                    data[i] = 0;
        if (bitshift > 0)
            for (int i = data.length; i-- > 0;)
                if (i > 0)
                    data[i] =
                        (byte) (((data[i] & 0xFF) >>> bitshift) | (data[i - 1] << (8 - bitshift)));
                else
                    data[i] = (byte) ((data[i] & 0xFF) >>> bitshift);
    }
    
    
    /**
     * Returns a new BitBuf consisting of all bits from offset s and beyond.
     * @param offset	    offset at which to slice
     * @return The new BitBuf containing the subset of data
     */
    public BitBuf slice(int s) {
        return new BitBuf(this, s, this.bitlen - s);
    }

    /**
     * Returns a new BitBuf consisting of l bits starting with offset s.
     * @param offset	    offset at which to slice
     * @param length	    how may bits to slice
     * @return The new BitBuf containing the subset of data
     */
    public BitBuf slice(int s, int l) {
        return new BitBuf(this, s, l);
    }

    /**
     * Returns a binary string representing the BitBuf, no spacing
     * @return String
     */
    public String toBinString() {
        return toBinString(0, "");
    }
    
    /**
     * Returns a binary string representing the BitBuf. The separator
     * string is inserted into the result after every groupsize bytes.
     * @return String
     */
    public String toBinString(int groupsize, String separator) {
        StringWriter out = new StringWriter();
        for (int x = 0; x < data.length; x++) {
            out.write(byteAsBin(x));
            if ((groupsize != 0) && (x % groupsize == 0))
                out.write(separator);
        }
        return out.toString();
    }
    
    /**
     * Returns a binary string representing the bit buf. Every 8 bits
     * is separated by the string sep.
     * @return String
     */
    public String toBinString(String sep) {
        return toBinString(1, sep);
    }

    /**
     * Returns the rightmost 8 bits of the BitBuf as a byte.
     * @return byte
     */
    public byte toByte() {
        return getOctet(bitlen - 8);
    }

    /**
     * Returns a hex string representing the BitBuf, no spacing.
     * @return String
     */
    public String toHexString() {
        return toHexString(0, "");
    }

    /**
     * Returns a hex string representing the BitBuf.  The separator
     * string is inserted into the result after every groupsize bytes.
     * @return String
     */
    public String toHexString(int groupsize, String separator) {
        StringWriter out = new StringWriter();
        for (int x = 0; x < data.length; x++) {
            if ((x != 0) && (groupsize > 0) && (x % groupsize == 0))
                out.write(separator);
            out.write(byteAsHex(x));
        }
        return out.toString();
    }

    /**
     * Returns a hex string representing the bit buf. Every 2 hex digits
     * are separated by the string sep.
     * @return String
     */
    public String toHexString(String sep) {
        return toHexString(1, sep);
    }

    /**
     * Works like toHexString(grouplen, separator) except that if the bitlen
     * of the BitBuf is not a multiple of 8, the slack bits will be on the left
     * instead of the right.
     * @return String
     */
    public String toHexStringJustified(int grouplen, String separator) {
        BitBuf b = (BitBuf) this.clone();
        int slack = b.bitlen % 8;
        if (slack > 0)
            b.shiftBufferRight(8 - slack);
        return b.toHexString(grouplen, separator);
    }

    /**
     * Returns the rightmost 32 bits of the BitBuf as an int.
     * @return int
     */
    public int toInt() {
        long r = 0;
        for (int i = 0; i < 4; i++)
            r |= (getOctet(bitlen - 8 * (i + 1)) & 0xFF) << (i * 8);
        return (int) r;
    }

    /**
     * Returns the rightmost 64 bits of the BitBuf as a long. 
     * @return long
     */
    public long toLong() {
        long r = 0;
        for (int i = 0; i < 8; i++)
            r |= (long) (getOctet(bitlen - 8 * (i + 1)) & 0xFF) << (i * 8);
        return r;
    }

    /**
     * Returns the rightmost 16 bits of the BitBuf as an int.
     * @return short
     */
    public short toShort() {
        long r = 0;
        for (int i = 0; i < 2; i++)
            r |= (getOctet(bitlen - 8 * (i + 1)) & 0xFF) << (i * 8);
        return (short) r;
    }

    /**
     * Set unused bits on right to 0 (for situations like a string bit len of 6, which still takes 1 byte).
     */
    private void truncate() {
        int tailbits = bitlen % 8;
        if (tailbits > 0)
            data[data.length - 1] &= ~((1 << (8 - tailbits)) - 1);
    }

}
