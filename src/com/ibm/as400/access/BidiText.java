///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: BidiText.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  Bidi text is a combination of a sequence of characters and a set of
 *  Bidi flags which represent Bidi attributes.
 *  <p>
 *  Layout transformations allow to convert a given instance of Bidi text
 *  into another instance with possibly different Bidi flags while
 *  conserving the semantics of the text.
 *  <p>
 *  A BidiText object contains a BidiFlagSet to store the Bidi flags
 *  characterizing its character data.
 *  The characters are contained in a character array.  This array may
 *  contain more data than the Bidi text to process.  The part of the array
 *  to process is called the "interesting" data, and is defined by its length
 *  and its offset from the beginning of the character array.
 *
 *  <p><b>Multi-threading considerations:</b>
 *  Each thread must use its own instances of this class.
 *
 **/

class BidiText
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


/**
 *  BidiFlagSet qualifying the character data
 */
    public BidiFlagSet  flags;
/**
 *  character array containing the data
 */
    public char[]       data;
/**
 *  offset of the "interesting" data within the character array
 */
    public int          offset;
/**
 *  length of the "interesting" data within the character array
 */
    public int          count;

/**
 *  Constructor with no arguments to create a flags member with
 *  a DEFAULT value.  There is no data and no character array,
 *  but a default BidiFlagSet is created and referred to in "flags".
 */
    public BidiText()
    {
        this.flags = new BidiFlagSet();
    }

/**
 *  Constructs a BidiText object based on an existing
 *  BidiFlagSet and the data in a character array.
 *  <p>The argument flags and data are duplicated.  Changing them will not
 *  affect the BidiText instance.
 *  @param  initFlags   The Bidi flags of the data.
 *  @param  initData    The character data.
 */
    public BidiText(BidiFlagSet initFlags, char[] initData)
    {
        this(initFlags, initData, 0, initData.length, initData.length);
    }

/**
 *  Constructs a BidiText object based on an existing
 *  BidiFlagSet and the data in part of a character array.
 *  <p>The argument flags and data are duplicated.  Changing them will not
 *  affect the BidiText instance.
 *  @param  initFlags   The Bidi flags of the data.
 *  @param  initData    The character data.
 *  @param  offset      The offset of the "interesting" data in initData.
 *  @param  length      The length of the "interesting" data in initData.
 *  @param  capacity    The length of the created character array (may be
 *                          larger than "length" to reserve space for
 *                          adding more data).
 */
    public BidiText(BidiFlagSet initFlags,
                    char[] initData,
                    int offset,
                    int length,
                    int capacity)
    {
        this.flags = new BidiFlagSet(initFlags);
        this.data = new char[capacity];
        System.arraycopy(initData, offset, this.data, 0, length);
        this.count = length;
    }

/**
 *  Constructs a BidiText object based on an existing
 *  BidiFlagSet and the data in a string.
 *  <p>The argument flags and data are duplicated.  Changing them will not
 *  affect the BidiText instance.
 *  @param  initFlags   The Bidi flags of the data.
 *  @param  str         The character data.
 */
    public BidiText(BidiFlagSet initFlags, String str)
    {
        this.flags = new BidiFlagSet(initFlags);
        this.count = str.length();
        // it would be nicer to use str.toCharArray, but it has bugs on
        // Arabic NT
        this.data = new char[count];
        str.getChars(0, count, this.data, 0);
    }

/**
 *  Compare two BidiText objects.
 *  Two BidiText objects are considered equal if they have the same Bidi flags
 *  and the same "interesting" character data,
 *  @param  other       The BidiText to compare to this.
 *  @return true if the BidiText objects are equal, false otherwise.
 */
    public boolean equals(BidiText other)
    {
        if (other == null)  return false;
        if (this.count != other.count)  return false;
        if (this.flags.value != other.flags.value)  return false;
        for (int i = 0; i < this.count; i++)
        {
            if (this.data[this.offset + i] !=
                other.data[other.offset + i])  return false;
        }
        return true;
    }

/**
 *  Replace the character data <em>reference</em> in the BidiText object.
 *  Note that the data is not duplicated, only its
 *  reference is written in the BidiText object.
 *  <p>This method avoids the overhead of creating a character array
 *  and copying the source data to it, when the source data is already
 *  contained in a character array.
 *  <p>This method can be used after creating a BidiText object with no
 *  arguments, or to reuse a BidiText object with new data.
 *  <p>This is a convenience method.  It is also possible to manipulate
 *  directly the data, offset and count members of the BidiText instance.
 *  @param  newData     A reference to the character data.
 *  @param  newOffset   The offset of the "interesting" data in newData.
 *  @param  newLength   The length of the "interesting" data in newData.
 */
    public void setCharsRef(char[] newData, int newOffset, int newLength)
    {
        this.data = newData;
        this.offset = newOffset;
        this.count = newLength;
    }

/**
 *  Extract the character data from a BidiText in character array format
 *  @return A string containing a copy of the "interesting" data.
 */
    public char[] toCharArray()
    {
        char[] copyChars = new char[this.count];
        System.arraycopy(this.data, this.offset, copyChars, 0, count);
        return copyChars;
    }

/**
 *  Extract the character data from a BidiText in string format
 *  @return A string containing a copy of the "interesting" data.
 */
    public String toString()
    {
        return new String(this.data, this.offset, this.count);
    }

/**
 *  Transform the data in the "this" BidiText object and return the resulting
 *  BidiText object.
 *  The transformation is done according to the Bidi flags of the source
 *  BidiText and the Bidi flags specified in the argument.
 *  <p>The source BidiText is never modified by the transform.
 *  <p>The destination BidiText has its Bidi flags set to those of the
 *  argument.
 *  <p>A typical usage of this method could be:
 *  <pre>
 *  BidiText        src = new BidiText();       // source text
 *  BidiFlagSet     dstFlags;                   // Bidi flags for destination
 *  BidiText        dst;                        // destination reference
 *  src.flags.setAllFlags( {flag values for source} );
 *  dstFlags.setAllFlags( {flag values for destination} );
 *  // assign values to src.data, src.offset, src.count
 *  dst = src.transform(dstFlags);
 *  </pre>
 *  @param  dstFlags    Bidi flags of the destination BidiText.
 *  @return A BidiText which is the transformation of the "this" BidiText.
 */
    public BidiText transform(BidiFlagSet dstFlags)
    {
        BidiTransform bdx = new BidiTransform();
        bdx.flags.setAllFlags(dstFlags);
        return this.transform(bdx);
    }

/**
 *  Transform the data in the "this" BidiText object and return the resulting
 *  BidiText object.
 *  The transformation is done according to the Bidi flags of the source
 *  BidiText and the Bidi flags specified in the BidiTransform argument.
 *  <p>The source BidiText is never modified by the transform.
 *  <p>The destination BidiText has its Bidi flags set to those of the
 *  argument.
 *  <p>Output fields of the BidiTransform object are set by this method.
 *  srcToDstMap, DstToSrcMap and propertyMap may be set by this method if the
 *  corresponding options have been required in BidiTransform when calling it.
 *  <p>By default, transformed output data is written to the destination
 *  BidiText character array but no maps are created.
 *  <p>A typical usage of this method could be:
 *  <pre>
 *  BidiTransform   bdx = new BidiTransform();
 *  BidiText        src = new BidiText();       // source text
 *  BidiText        dst;                        // destination reference
 *  src.flags.setAllFlags( {flag values for source} );
 *  bdx.flags.setAllFlags( {flag values for destination} );
 *  // assign values to src.data, src.offset, src.count
 *  dst = src.transform(bdx);
 *  </pre>
 *  <p>This method is still in construction.  Currently only the default
 *  options are implemented.  No maps are created.
 *  @param  bdx         The BidiTransform object defining the transformation.
 *  @return A BidiText which is the transformation of the "this" BidiText.
 *  If destination data is not required, a null is returned.
 */
    public BidiText transform(BidiTransform bdx)
    {
        char[] data;
        BidiText dst = new BidiText();
        dst.flags.setAllFlags(bdx.flags);
        dst.data = new char[this.count];
        if (bdx.myOrder == null)   bdx.myOrder = new BidiOrder();
        bdx.myOrder.order(this, dst);

        if (this.flags.getText() != dst.flags.getText())
        {
            if (bdx.myShape == null)
            {
                bdx.myShape = new BidiShape();
                bdx.flags1 = new BidiFlagSet();
                bdx.flags2 = new BidiFlagSet();
            }
            bdx.flags1.setAllFlags(this.flags);
            bdx.flags2.setAllFlags(dst.flags);
            // The following flag settings are because the shape method
            // goes from LTR if the out orientation is RTL and the
            // in orientation is different from the out orientation.

//            if (dst.flags.getType() == BidiFlag.TYPE_IMPLICIT || //@BD1
//Commented by Heba M Naguib
            if (dst.flags.getType() == BidiFlag.TYPE_VISUAL &&
                dst.flags.getOrientation() == BidiFlag.ORIENTATION_RTL)
            {
                bdx.flags1.setOneFlag(BidiFlag.ORIENTATION_LTR);
                bdx.flags2.setOneFlag(BidiFlag.ORIENTATION_RTL);
            }
//Commented Heba M Naguib //@BD1
/*            else {//@BD1
                bdx.flags1.setOneFlag(BidiFlag.ORIENTATION_LTR);//@BD1
                bdx.flags2.setOneFlag(BidiFlag.ORIENTATION_LTR);//@BD1
            }
*/
            bdx.myShape.shape(bdx.flags1, bdx.flags2, dst.data);
        }
        return dst;
    }

}
