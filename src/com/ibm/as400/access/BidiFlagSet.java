///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiFlagSet.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.StringTokenizer;

/**
 *  Bidi text can be stored in different formats, which are characterized
 *  by 5 Bidi attributes, whose values may be stored in 5 Bidi flags.
 *  These 5 flags constitute a BidiFlagSet.
 *  <p>
 *  A BidiFlagSet object contains a value for each of the 5 Bidi flags
 *  which represent the Bidi attributes.  The 5 attributes are:
 *  <ul>
 *  <li> type of text (Implicit or Visual)
 *  <li> orientation (LTR, RTL, Contextual LTR, Contextual RTL)
 *  <li> symmetric swapping (Yes or No)
 *  <li> numeral shapes (Nominal, National, Contextual or Any)
 *  <li> text shapes (Nominal, Shaped, Initial, Middle, Final or Isolated)
 *  </ul>
 *  <p><b>Multi-threading considerations:</b> Different threads may use
 *  the same BidiFlagSet instance if they all mean it to represent
 *  identical values of the Bidi attributes. For different values of the
 *  Bidi attributes, distinct instances of this class must be used.
 *
 */

class BidiFlagSet
{
/**
 *  Mask to apply on a BidiFlagSet value to isolate the
 *  type-of-text flag
 */
    static final int    TYPE_MASK               = 0x03000000;
/**
 *  Value identifying Implicit type of text
 */
    static final int    ITYPE_IMPLICIT          = 0x01000000;
/**
 *  Value identifying Visual type of text
 */
    static final int    ITYPE_VISUAL            = 0x03000000;

/**
 *  Mask to apply on a BidiFlagSet value to isolate the
 *  orientation flag
 */
    static final int    ORIENTATION_MASK        = 0x00700000;
/**
 *  Value identifying LTR orientation
 */
    static final int    IORIENTATION_LTR        = 0x00100000;
/**
 *  Value identifying RTL orientation
 */
    static final int    IORIENTATION_RTL        = 0x00300000;
/**
 *  Value identifying Contextual orientation with default to LTR
 */
    static final int    IORIENTATION_CONTEXT_LTR = 0x00500000;
/**
 *  Value identifying Contextual orientation with default to RTL
 */
    static final int    IORIENTATION_CONTEXT_RTL = 0x00700000;

/**
 *  Mask to apply on a BidiFlagSet value to isolate the
 *  Symmetric Swapping flag
 */
    static final int    SWAP_MASK               = 0x00030000;
/**
 *  Value identifying that Symmetric Swapping has been applied
 */
    static final int    ISWAP_YES               = 0x00010000;
/**
 *  Value identifying that Symmetric Swapping has not been applied
 */
    static final int    ISWAP_NO                = 0x00030000;

/**
 *  Mask to apply on a BidiFlagSet value to isolate the
 *  Numeral Shapes flag
 */
    static final int    NUMERALS_MASK           = 0x00000700;
/**
 *  Value identifying that Numeral Shapes are Nominal
 */
    static final int    INUMERALS_NOMINAL       = 0x00000100;
/**
 *  Value identifying that Numeral Shapes are National
 */
    static final int    INUMERALS_NATIONAL      = 0x00000300;
/**
 *  Value identifying that Numeral Shapes are Contextual (Nominal or National
 *  depending on context)
 */
    static final int    INUMERALS_CONTEXTUAL    = 0x00000500;
/**
 *  Value identifying that Numeral Shapes can be Nominal or National
 */
    static final int    INUMERALS_ANY           = 0x00000700;

/**
 *  Mask to apply on a BidiFlagSet value to isolate the
 *  Text Shapes flag
 */
    static final int    TEXT_MASK               = 0x0000000F;
/**
 *  Value identifying that the text is stored in Nominal characters
 */
    static final int    ITEXT_NOMINAL           = 0x00000001;
/**
 *  Value identifying that the text is stored in Shaped characters
 */
    static final int    ITEXT_SHAPED            = 0x00000003;
/**
 *  Value identifying that the text must be displayed in Initial characters
 */
    static final int    ITEXT_INITIAL           = 0x00000005;
/**
 *  Value identifying that the text must be displayed in Middle characters
 */
    static final int    ITEXT_MIDDLE            = 0x00000007;
/**
 *  Value identifying that the text must be displayed in Final characters
 */
    static final int    ITEXT_FINAL             = 0x00000009;
/**
 *  Value identifying that the text must be displayed in Isolated characters
 */
    static final int    ITEXT_ISOLATED          = 0x0000000B;


//  This is the default value for uninitialized BidiFlagSet.
    static final int    DEFAULT = ITYPE_IMPLICIT | IORIENTATION_LTR |
                                  ISWAP_YES | INUMERALS_NOMINAL | ITEXT_NOMINAL;

//  This is the only field in this class.
    int value;

/**
 *  Constructs a BidiFlagSet with the default value.
 *  The default is:
 *  <pre>
 *      Type: implicit  Orientation: LTR    Swapping: YES
 *      Numeral Shapes: Nominal     Text Shapes: Nominal
 *  </pre>
 */
    public BidiFlagSet()
    {
        this.value = DEFAULT;
    }

/**
 *  Constructs a BidiFlagSet based on an existing BidiFlagSet.
 *  @param  model   The BidiFlagSet which is copied.
 */
    public BidiFlagSet(BidiFlagSet model)
    {
        this.value = model.value;
    }

/**
 *  Constructs a BidiFlagSet from one BidiFlag.  The other Bidi
 *  flags are set to their default.
 *  <p>Example:
 *  <pre>
 *  BidiFlagset bdfs = new BidiFlagSet(ORIENTATION_RTL);
 *  </pre>
 *
 *  @param  flag    The BidiFlag which is explicitly specified.
 */
    public BidiFlagSet(BidiFlag flag)
    {
        this.value = flag.value | DEFAULT;
    }

/**
 *  Constructs a BidiFlagSet from two BidiFlags.  The other Bidi
 *  flags are set to their default.
 *  If there is a contradiction or a duplication between the arguments,
 *  then an IllegalArgumentException is thrown.
 *  <p>Example:
 *  <pre>
 *  BidiFlagset bdfs = new BidiFlagSet(ORIENTATION_RTL, TYPE_VISUAL);
 *  </pre>
 *
 *  @param  flag1   The first BidiFlag which is explicitly specified.
 *  @param  flag2   The second BidiFlag which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public BidiFlagSet(BidiFlag flag1, BidiFlag flag2)
    {
        this.value = flag1.value;
        if ((this.value & flag2.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag2.value | DEFAULT;
    }

/**
 *  Constructs a BidiFlagSet from three BidiFlags.  The other Bidi
 *  flags are set to their default.
 *  If there is a contradiction or a duplication between the arguments,
 *  then an IllegalArgumentException is thrown.
 *  <p>Example:
 *  <pre>
 *  BidiFlagset bdfs = new BidiFlagSet(ORIENTATION_RTL, TYPE_VISUAL, SWAP_YES);
 *  </pre>
 *
 *
 *  @param  flag1   The first BidiFlag which is explicitly specified.
 *  @param  flag2   The second BidiFlag which is explicitly specified.
 *  @param  flag3   The third BidiFlag which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public BidiFlagSet(BidiFlag flag1, BidiFlag flag2, BidiFlag flag3)
    {
        this.value = flag1.value;
        if ((this.value & flag2.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag2.value;
        if ((this.value & flag3.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag3.value | DEFAULT;
    }

/**
 *  Constructs a BidiFlagSet from four BidiFlags.  The other Bidi
 *  flag is set to its default.
 *  If there is a contradiction or a duplication between the arguments,
 *  then an IllegalArgumentException is thrown.
 *
 *  @param  flag1   The first BidiFlag which is explicitly specified.
 *  @param  flag2   The second BidiFlag which is explicitly specified.
 *  @param  flag3   The third BidiFlag which is explicitly specified.
 *  @param  flag4   The fourth BidiFlag which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public BidiFlagSet(BidiFlag flag1, BidiFlag flag2,
                       BidiFlag flag3, BidiFlag flag4)
    {
        this.value = flag1.value;
        if ((this.value & flag2.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag2.value;
        if ((this.value & flag3.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag3.value;
        if ((this.value & flag4.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag4.value | DEFAULT;
    }

/**
 *  Constructs a BidiFlagSet from five BidiFlags.
 *  If there is a contradiction or a duplication between the arguments,
 *  then an IllegalArgumentException is thrown.
 *
 *  @param  flag1   The first BidiFlag which is explicitly specified.
 *  @param  flag2   The second BidiFlag which is explicitly specified.
 *  @param  flag3   The third BidiFlag which is explicitly specified.
 *  @param  flag4   The fourth BidiFlag which is explicitly specified.
 *  @param  flag5   The fifth BidiFlag which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or duplicate
 */
    public BidiFlagSet(BidiFlag flag1, BidiFlag flag2, BidiFlag flag3,
                       BidiFlag flag4, BidiFlag flag5)
    {
        this.value = flag1.value;
        if ((this.value & flag2.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag2.value;
        if ((this.value & flag3.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag3.value;
        if ((this.value & flag4.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag4.value;
        if ((this.value & flag5.value) != 0)  throw new IllegalArgumentException();
        this.value |= flag5.value;
    }

/**
 *  Constructs a BidiFlagSet from a char array.
 *  The content of the array must follow the specification for the "S" and
 *  "U parts of the BIDI environment variable, as follows:
 *  <ul>
 *  <li>character 1: type of text = I (Implicit) or V (Visual)
 *  <li>character 2: orientation = L (LTR), R (RTL), C (Contextual LTR) or D (Contextual RTL)
 *  <li>character 3: swapping = Y (Swapping ON) or N (Swapping OFF)
 *  <li>character 4:  text shaping = N (Nominal), S (Shaped), I (Initial), M (Middle), F (Final), B (Isolated)
 *  <li>character 5: numeral shaping = N (Nominal), H (National), C (Contextual), A (Any)
 *  <li>character 6: bidi algorithm = U (Unicode), R (Roundtrip)
 *  <li>character 7: Lamalef mode = G (Grow), S(Shrink), N (Near), B (Begin), E (End), W (groW with space), A (Auto)
 *  <li>character 8: SeenTail mode = O (One cell), N (Near), B (Begin), E (End), A (Auto)
 *  <li>character 9: Yeh Hamza mode = O (One cell), N (Near), B (Begin), E (End), A (Auto)
 *  <li>character 10: Tashkeel mode = Z (Zero width), W (with Width), B (Begin), E (End), A (Auto)
 *  </ul>
 *  <p>Only characters 1 to 5 are used to build the BidiFlagSet.
 *  <p>
 *  @param  chars       A character array.
 */
    public BidiFlagSet(char[] chars)
    {
        int newValue = DEFAULT;
        int len = chars.length;

        while (len > 0)
        {
            if ('V' == chars[0])
                newValue = (newValue & (~TYPE_MASK)) | ITYPE_VISUAL;
            else if ('I' == chars[0])
                newValue = (newValue & (~TYPE_MASK)) | ITYPE_IMPLICIT;

            if (len <= 1)  break;
            if ('L' == chars[1])
                newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_LTR;
            else if ('R' == chars[1])
                newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_RTL;
            else if ('C' == chars[1])
                newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_CONTEXT_LTR;
            else if ('D' == chars[1])
                newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_CONTEXT_RTL;

            if (len <= 2)  break;
            if ('Y' == chars[2])
                newValue = (newValue & (~SWAP_MASK)) | ISWAP_YES;
            else if ('N' == chars[2])
                newValue = (newValue & (~SWAP_MASK)) | ISWAP_NO;

            if (len <= 3)  break;
            if ('N' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_NOMINAL;
            else if ('S' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_SHAPED;
            else if ('I' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_INITIAL;
            else if ('M' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_MIDDLE;
            else if ('F' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_FINAL;
            else if ('B' == chars[3])
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_ISOLATED;

            if (len <= 4)  break;
            if ('N' == chars[4])
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_NOMINAL;
            else if ('H' == chars[4])
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_NATIONAL;
            else if ('C' == chars[4])
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_CONTEXTUAL;
            else if ('A' == chars[4])
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_ANY;
            break;
        }

        this.value = newValue;
    }

/**
 *  Constructs a BidiFlagSet from a string.
 *  The content of the string must follow the syntax of the modifiers
 *  specified in X/Open standard "Portable Layout Services".
 *  <p>The string contains sequences in the form "keyword=value"
 *  separated by commas.
 *  <p>
 *  This format is compatible with the result of the toString method.
 *  This format is useful when readibility is more important than efficiency.
 *  <p>
 *  The supported keywords are: typeoftext, orientation, context, swapping,
 *  numerals and shaping.
 *  <p>
 *  The following keywords are ignored but do not cause a syntax error: @ls,
 *  implicitalg, checkmode, shapcharset.
 *  <p>Example:
 *  <pre>
 *  typeoftext=implicit, orientation=rtl, swap=yes, shaping=nominal, numerals=nominal
 *  </pre>
 *  <p>
 *  @param  str         A string in the format "flag=value [,...]".
 *  @exception  IllegalArgumentException    If the syntax of the data is invalid.
 *  @see    #toString
 */
    public BidiFlagSet(String str) throws IllegalArgumentException
    {
        int n = parseBidiFlagSet(str);
        if (n < 0)  throw new IllegalArgumentException();
        this.value = n;
    }

/**
 *  Compare two BidiFlagSets.
 *  Two BidiFlagSets are considered equal if they represent the same values
 *  for the 5 Bidi flags.
 *  @param  other       The BidiFlagSet to compare to this.
 *  @return true if the BidiFlagSets are equal, false otherwise.
 */
    public boolean equals(BidiFlagSet other)
    {
        if (other == null)  return false;
        return this.value == other.value;
    }

/**
 *  Returns the Numeral Shapes flag from a BidiFlagSet.
 *  @return The value of the numeral shapes flag.
 *  <p> The expected value is one of NUMERALS_NOMINAL, NUMERALS_NATIONAL,
 *  NUMERALS_CONTEXTUAL, NUMERALS_ANY.
 *  <br>It can be tested as in the following example:
 *  <pre>
 *  if (getNumerals(myFlags) == NUMERALS_NATIONAL) . . .
 *  </pre>
 */
    public BidiFlag getNumerals()
    {
        switch(this.value & NUMERALS_MASK)
        {
        case INUMERALS_NOMINAL:         return BidiFlag.NUMERALS_NOMINAL;
        case INUMERALS_NATIONAL:        return BidiFlag.NUMERALS_NATIONAL;
        case INUMERALS_CONTEXTUAL:      return BidiFlag.NUMERALS_CONTEXTUAL;
        case INUMERALS_ANY:             return BidiFlag.NUMERALS_ANY;
        }
        return BidiFlag.NUMERALS_NOMINAL;
    }

/**
 *  Returns the Orientation flag from a BidiFlagSet.
 *  @return The value of the orientation flag.
 *  <p> The expected value is one of ORIENTATION_LTR, ORIENTATION_RTL,
 *  ORIENTATION_CONTEXT_LTR, ORIENTATION_CONTEXT_RTL.
 *  <br>It can be tested as in the following example:
 *  <pre>
 *  if (getOrientation(myFlags) == ORIENTATION_RTL) . . .
 *  </pre>
 */
    public BidiFlag getOrientation()
    {
        switch(this.value & ORIENTATION_MASK)
        {
        case IORIENTATION_LTR:          return BidiFlag.ORIENTATION_LTR;
        case IORIENTATION_RTL:          return BidiFlag.ORIENTATION_RTL;
        case IORIENTATION_CONTEXT_LTR:  return BidiFlag.ORIENTATION_CONTEXT_LTR;
        case IORIENTATION_CONTEXT_RTL:  return BidiFlag.ORIENTATION_CONTEXT_RTL;
        }
        return BidiFlag.ORIENTATION_LTR;
    }

/**
 *  Returns the Symmetric Swapping flag from a BidiFlagSet.
 *  @return The value of the symmetric swapping flag.
 *  <p> The expected value is one of SWAP_YES, SWAP_NO.
 *  <br>It can be tested as in the following example:
 *  <pre>
 *  if (getSwap(myFlags) == SWAP_YES) . . .
 *  </pre>
 */
    public BidiFlag getSwap()
    {
        switch(this.value & SWAP_MASK)
        {
        case ISWAP_YES:                 return BidiFlag.SWAP_YES;
        case ISWAP_NO:                  return BidiFlag.SWAP_NO;
        }
        return BidiFlag.SWAP_YES;
    }

/**
 *  Returns the Text Shapes flag from a BidiFlagSet.
 *  @return The value of the text shapes flag.
 *  <p> The expected value is one of TEXT_NOMINAL, TEXT_SHAPED,
 *  TEXT_INITIAL, TEXT_MIDDLE, TEXT_FINAL, TEXT_ISOLATED.
 *  <br>It can be tested as in the following example:
 *  <pre>
 *  if (getText(myFlags) == TEXT_MIDDLE) . . .
 *  </pre>
 */
    public BidiFlag getText()
    {
        switch(this.value & TEXT_MASK)
        {
        case ITEXT_NOMINAL:             return BidiFlag.TEXT_NOMINAL;
        case ITEXT_SHAPED:              return BidiFlag.TEXT_SHAPED;
        case ITEXT_INITIAL:             return BidiFlag.TEXT_INITIAL;
        case ITEXT_MIDDLE:              return BidiFlag.TEXT_MIDDLE;
        case ITEXT_FINAL:               return BidiFlag.TEXT_FINAL;
        case ITEXT_ISOLATED:            return BidiFlag.TEXT_ISOLATED;
        }
        return BidiFlag.TEXT_NOMINAL;
    }

/**
 *  Returns the Type-of-Text flag from a BidiFlagSet.
 *  @return The value of the type-of-text flag.
 *  <p> The expected value is one of TYPE_IMPLICIT, TYPE_VISUAL.
 *  <br>It can be tested as in the following example:
 *  <pre>
 *  if (getType(myFlags) == TYPE_VISUAL) . . .
 *  </pre>
 */
    public BidiFlag getType()
    {
        switch(this.value & TYPE_MASK)
        {
        case ITYPE_IMPLICIT:            return BidiFlag.TYPE_IMPLICIT;
        case ITYPE_VISUAL:              return BidiFlag.TYPE_VISUAL;
        }
        return BidiFlag.TYPE_IMPLICIT;
    }

/**
 *  hashcode for a BidiFlagSet.
 *  The hashcode of a BidiFlagSet is the same as the hashcode of its
 *  value.
 *  @return             A hashcode value.
 */
    public int hashCode()
    {
        return this.value;
    }

/**
 *  Compute the value of a BidiFlagSet specified as text.
 *  The text must follow the syntax of modifiers detailed in X/Open
 *  "Portable Layout Services".
 *  This format is compatible with the result of the toString method.
 *  This format is useful when readibility is more important than efficiency.
 *  <p>
 *  The supported keywords are: typeoftext, orientation, context, swapping,
 *  numerals and shaping.
 *  <p>
 *  The following keywords are ignored but do not cause a syntax error: @ls,
 *  implicitalg, checkmode, shapcharset.
 *  @param  str         a string in the format "flag=value [,...]"
 *  @return The value of the BidiFlagSet object if str is correct;
 *  if the syntax is invalid, the return value is a negative number
 *  whose absolute value is the ordinal (starting from 1) of the first
 *  invalid word token (disregarding punctuation).
 *  @see    #toString
 */
    private static int parseBidiFlagSet(String str)
    {
        int newValue = 0;
        int counter = 0;
        String token, keyword = "";
        StringTokenizer stok = new StringTokenizer(str, "\t ,=");

        while (stok.hasMoreElements()) {
            token = stok.nextToken();
            counter--;
            if (token.equals("@ls"))  continue;
            if (token.equals("implicitalg") ||
                token.equals("checkmode")   ||
                token.equals("shapcharset") ) {
                keyword = "allowed";
                continue;
            }
            if (token.equals("typeoftext")  ||
                token.equals("orientation") ||
                token.equals("context")     ||
                token.equals("swapping")    ||
                token.equals("numerals")    ||
                token.equals("shaping") ) {
                keyword = token;
                continue;
            }
            if (token.equals("visual")) {
                if (!keyword.equals("typeoftext")) return counter;
                newValue = (newValue & (~TYPE_MASK)) | ITYPE_VISUAL;
                continue;
            }
            if (token.equals("implicit")) {
                if (!keyword.equals("typeoftext")) return counter;
                newValue = (newValue & (~TYPE_MASK)) | ITYPE_IMPLICIT;
                continue;
            }
            if (token.equals("ltr")) {
                if (keyword.equals("orientation"))
                {
                    newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_LTR;
                    continue;
                }
                if (keyword.equals("context"))
                {
                    if ((newValue & ORIENTATION_MASK) != IORIENTATION_LTR &&
                        (newValue & ORIENTATION_MASK) != IORIENTATION_RTL)
                        newValue = (newValue & (~ORIENTATION_MASK)) |
                                    IORIENTATION_CONTEXT_LTR;
                    continue;
                }
                return counter;
            }
            if (token.equals("rtl")) {
                if (keyword.equals("orientation"))
                {
                    newValue = (newValue & (~ORIENTATION_MASK)) | IORIENTATION_RTL;
                    continue;
                }
                if (keyword.equals("context"))
                {
                    if ((newValue & ORIENTATION_MASK) != IORIENTATION_LTR &&
                        (newValue & ORIENTATION_MASK) != IORIENTATION_RTL)
                        newValue = (newValue & (~ORIENTATION_MASK)) |
                                    IORIENTATION_CONTEXT_RTL;
                    continue;
                }
                return counter;
            }
            if (token.equals("contextual")) {
                if (keyword.equals("orientation"))
                {
                    if ((newValue & ORIENTATION_MASK) == IORIENTATION_CONTEXT_RTL)
                        continue;
                    newValue = ((newValue & (~ORIENTATION_MASK)) |
                                IORIENTATION_CONTEXT_LTR);
                    continue;
                }
                if (keyword.equals("numerals"))
                {
                    newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_CONTEXTUAL;
                    continue;
                }
                return counter;
            }
            if (token.equals("yes")) {
                if (!keyword.equals("swapping")) return counter;
                newValue = (newValue & (~SWAP_MASK)) | ISWAP_YES;
                continue;
            }
            if (token.equals("no")) {
                if (!keyword.equals("swapping")) return counter;
                newValue = (newValue & (~SWAP_MASK)) | ISWAP_NO;
                continue;
            }
            if (token.equals("nominal")) {
                if (keyword.equals("numerals"))
                {
                    newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_NOMINAL;
                    continue;
                }
                if (keyword.equals("shaping"))
                {
                    newValue = (newValue & (~TEXT_MASK)) | ITEXT_NOMINAL;
                    continue;
                }
                return counter;
            }
            if (token.equals("national")) {
                if (!keyword.equals("numerals")) return counter;
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_NATIONAL;
                continue;
            }
            if (token.equals("any")) {
                if (!keyword.equals("numerals")) return counter;
                newValue = (newValue & (~NUMERALS_MASK)) | INUMERALS_ANY;
                continue;
            }
            if (token.equals("shaped")) {
                if (!keyword.equals("shaping")) return counter;
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_SHAPED;
                continue;
            }
            if (token.equals("shform1")) {
                if (!keyword.equals("shaping")) return counter;
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_INITIAL;
                continue;
            }
            if (token.equals("shform2")) {
                if (!keyword.equals("shaping")) return counter;
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_MIDDLE;
                continue;
            }
            if (token.equals("shform3")) {
                if (!keyword.equals("shaping")) return counter;
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_FINAL;
                continue;
            }
            if (token.equals("shform4")) {
                if (!keyword.equals("shaping")) return counter;
                newValue = (newValue & (~TEXT_MASK)) | ITEXT_ISOLATED;
                continue;
            }
            if (keyword.equals("allowed"))  continue;
            return counter;
        }
        return newValue | DEFAULT;
    }

/**
 *  Set all bidi flags based on another BidiFlagSet.
 *  @param  model       The BidiFlagSet which is copied.
 */
    public void setAllFlags(BidiFlagSet model)
    {
        this.value = model.value;
    }

/**
 *  Set all bidi flags from a string.
 *  The content of the string must follow the syntax of modifiers
 *  specified in X/Open standard "Portable Layout Services".
 *  <p>The string contains sequences of the form "keyword=value"
 *  separated by commas.
 *  <p>
 *  This format is compatible with the result of the toString method.
 *  This format is useful when readibility is more important than efficiency.
 *  <p>
 *  The supported keywords are: typeoftext, orientation, context, swapping,
 *  numerals and shaping.
 *  <p>
 *  The following keywords are ignored but do not cause a syntax error: @ls,
 *  implicitalg, checkmode, shapcharset.
 *  <p>Example:
 *  <pre>
 *  typeoftext=visual, orientation=ltr, swap=no, shaping=shaped, numerals=contextual
 *  </pre>
 *  <p>
 *  @param  str         A string in the format "flag=value [,...]".
 *  @exception  IllegalArgumentException    If the syntax of the data is invalid.
 *  @see    #toString
 */
    public void setAllFlags(String str) throws IllegalArgumentException
    {
        int n = parseBidiFlagSet(str);
        if (n < 0)  throw new IllegalArgumentException();
        this.value = n;
    }

/**
 *  Set all the Bidi flags in 2 BidiFlagSets based on a string.
 *  The content of the string must follow the syntax of modifiers
 *  specified in X/Open standard "Portable Layout Services".
 *  This may be used to specify in one operation the Bidi flags of the source
 *  and the destination of a transformation.
 *  <p>The string contains sequences of the form "keyword=value" or
 *  "keyword=value1:value2", with a separating comma.
 *  Each keyword is followed by one or two values.  In the first case,
 *  this value applies to both source and destination.  In the second case,
 *  the two values are separated by a colon; the first value applies to
 *  the source and the second value to the destination.
 *  <p>
 *  The supported keywords are: typeoftext, orientation, context, swapping,
 *  numerals and shaping.
 *  <p>
 *  The following keywords are ignored but do not cause a syntax error: @ls,
 *  implicitalg, checkmode, shapcharset.
 *  <p>Example: the source flags are "implicit, ltr, swapping on"; the
 *  target flags are "visual, ltr, no swapping".  The string will be:
 *  <pre>
 *  typeoftext=implicit:visual, orientation=ltr, swapping=yes:no
 *  </pre>
 *  @param  flags1      The first BidiFlagSet to be set.
 *  @param  flags2      The second BidiFlagSet to be set.
 *  @param  str         A string in the format "flag=value [,...]".
 *
 *  @exception  IllegalArgumentException    If the syntax of the data is invalid.
 */
    public static void set2AllFlags(BidiFlagSet flags1, BidiFlagSet flags2,
                                    String str) throws IllegalArgumentException
    {
        int capacity = str.length();
        StringBuffer sb1 = new StringBuffer(capacity);
        StringBuffer sb2 = new StringBuffer(capacity);
        StringTokenizer stok = new StringTokenizer(str, "\t ,=:", true);
        String token;
        int sb2Length = 0;
        boolean colon = false;

        while (stok.hasMoreElements()) {
            token = stok.nextToken();
            if (token.equals(" ") || token.equals("\t"))
            {
                sb1.append(token);
                sb2.append(token);
                continue;
            }
            if (token.equals(",") || token.equals("="))
            {
                colon = false;
                sb1.append(token);
                sb2.append(token);
                continue;
            }
            if (token.equals(":"))
            {
                colon = true;
                sb2.setLength(sb2Length);
                continue;
            }
            if (!colon)  sb1.append(token);
            sb2Length = sb2.length();
            sb2.append(token);
            colon = false;
        }

        flags1.setAllFlags(sb1.toString());
        flags2.setAllFlags(sb2.toString());
    }

/**
 *  Sets a new value for one of the Bidi flags in a set without changing
 *  the other Bidi flags.
 *  <p>The new value must be one of the pre-defined values for BidiFlag.
 *
 *  @param  newFlags    The new value requested for one of the flags.
 */
    public void setOneFlag(BidiFlag newFlag)
    {
        this.value = (this.value & (~newFlag.mask)) | newFlag.value;
    }

/**
 *  Create a string that represents the various Bidi flags grouped in
 *  a BidiFlagSet.  This may be useful for instance for debugging.
 *  <p>
 *  The format is compatible with the syntax of modifiers in X/Open standard
 *  "Portable Layout Services".
 *  <p>
 *  For each flag, an expression of the form "keyword=value" is added
 *  to the string.  Adjacent expressions are separated by a comma and a
 *  space.
 *  <p>The keywords and their respective sets of values are:
 *  <pre>
 *  KEYWORD         VALUES
 *  -----------     ---------------
 *  typeoftext      implicit visual
 *  orientation     ltr rtl contextual
 *  context         ltr rtl
 *  swapping        yes no
 *  numerals        nominal national contextual any
 *  shaping         nominal shaped shform1 shform2 shform3 shform4
 *  </pre>
 *  @return A human readable form of the flag values.
 */
    public String toString()
    {
        return BidiFlagSet.toString(this.value);
    }

/**
 *  Class method parallel to the instance method with the same name.
 *  @param  value       The value of a BidiFlagSet.
 *  @return A human readable form of the flag values.
 */
    static String toString(int value)
    {
        final   String[] typeStrings = {"implicit", "visual"};
        final   String[] orientationStrings = {"ltr", "rtl",
            "contextual, context=ltr", "contextual, context=rtl"};
        final   String[] swapStrings = {"yes", "no"};
        final   String[] numeralsStrings = {"nominal", "national",
                                            "contextual", "any"};
        final   String[] textStrings = {"nominal", "shaped", "shform1",
            "shform2", "shform3", "shform4", "invalid", "invalid"};

        return  "typeoftext=" + typeStrings[(value & TYPE_MASK)>>25] +
                ", " +
                "orientation=" +
                orientationStrings[(value & ORIENTATION_MASK)>>21] +
                ", " +
                "swapping=" + swapStrings[(value & SWAP_MASK)>>17] +
                ", " +
                "numerals=" + numeralsStrings[(value & NUMERALS_MASK)>>9] +
                ", " +
                "shaping=" + textStrings[(value & TEXT_MASK)>>1];
    }
}

