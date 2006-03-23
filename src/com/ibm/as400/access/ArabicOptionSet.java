///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ArabicOptionSet.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

 /**
 *  This class represents an ArabicOptionSet object defining the shaping
 *  attributes to be used during Bidi Layout Transformation process.
 *  It also defines the available values for each option.
 *  <p>
 *  Arabic text has some special characters that can be converted to different formats, which are characterized
 *  by four Bidi options, these options are stored in four Arabic objects.
 *  These four options constitute an ArabicOptionSet.
 *  <p>
 *  An ArabicOptionSet object contains a value for each of the four Arabic objects
 *  which represent the Arabic options.  The four options are:
 *  <ul>
 *  <li> Lam-Alef handling (Near, At Begin, At End or Auto)
 *  <li> Seen handling (Near or Auto)
 *  <li> Yeh Hamza handling (Near or Auto)
 *  <li> Tashkeel handling (Keep, Customized At Begin, Customized At End, or Auto)
 *  </ul>
 *  The Arabic options values are pre-defined in this class. Each one represents one
 *  possible value of one Arabic option.
 *  <p>
 *  For more information on Arabic Shaping options, see:
 *  <a href="http://www-128.ibm.com/developerworks/java/jdk/bidirectional/JAVABIDI.html">Bidirectional support in IBM SDK: A user guide</a>
 *
 *  <p><b>Multi-threading considerations:</b> There are no multi-threading
 *  concerns for this class, since it only defines static
 *  final instances.
 *
 */

class ArabicOptionSet
{
/**
 *  Mask to apply on an ArabicOptionSet value to isolate the
 *  Lam-Alef option
 */
    static final int    LAMALEF_MASK                = 0x0F000000;

/**
 *  Value identifying LAMALEF_AUTO
 */
    static final int    ILAMALEF_AUTO               = 0x01000000;
/**
 *  Value identifying LAMALEF_NEAR
 */
    static final int    ILAMALEF_NEAR               = 0x03000000;
/**
 *  Value identifying LAMALEF_ATBEGIN
 */
    static final int    ILAMALEF_ATBEGIN            = 0x05000000;
/**
 *  Value identifying LAMALEF_ATEND
 */
    static final int    ILAMALEF_ATEND              = 0x07000000;
/* *
 *  Value identifying LAMALEF_RESIZE_BUFEER
 */
    static final int    ILAMALEF_RESIZE_BUFFER    = 0x09000000;

//-------

/**
 *  Mask to apply on an ArabicOptionSet value to isolate the
 *  Seen option
 */
    static final int    SEEN_MASK                   = 0x00700000;

/**
 *  Value identifying SEEN_AUTO
 */
    static final int    ISEEN_AUTO                  = 0x00100000;
/* *
 *  Value identifying SEEN_ATBEGIN
 */
    static final int    ISEEN_ATBEGIN               = 0x00300000;
/* *
 *  Value identifying SEEN_ATEND
 */
    static final int    ISEEN_ATEND                 = 0x00500000;
/**
 *  Value identifying SEEN_NEAR
 */
    static final int    ISEEN_NEAR              = 0x00700000;

//--------

/**
 *  Mask to apply on an ArabicOptionSet value to isolate the
 *  Yeh Hamza option
 */
    static final int    YEHHAMZA_MASK              = 0x000F0000;

/**
 *  Value identifying YEHHAMZA_AUTO
 */
    static final int    IYEHHAMZA_AUTO             = 0x00010000;
/* *
 *  Value identifying YEHHAMZA_TWO_CELL_ATBEGIN
 */
    static final int    IYEHHAMZA_TWO_CELL_ATBEGIN = 0x00030000;
/* *
 *  Value identifying YEHHAMZA_TWO_CELL_ATEND
 */
    static final int    IYEHHAMZA_TWO_CELL_ATEND   = 0x00050000;
/**
 *  Value identifying YEHHAMZA_TWO_CELL_NEAR
 */
    static final int    IYEHHAMZA_TWO_CELL_NEAR    = 0x00070000;
/* *
 *  Value identifying YEHHAMZA_ONE_CELL
 */
    static final int    IYEHHAMZA_ONE_CELL         = 0x00090000;
//--------

/**
 *  Mask to apply on an ArabicOptionSet value to isolate the
 *  Tashkeel option
 */
    static final int    TASHKEEL_MASK                       = 0x00000F00;

/**
 *  Value identifying TASHKEEL_AUTO
 */
    static final int    ITASHKEEL_AUTO                      = 0x00000100;
/* *
 *  Value identifying TASHKEEL_CUSTOMIZED_WITHZEROWIDTH
 */
    static final int    ITASHKEEL_CUSTOMIZED_WITHZEROWIDTH  = 0x00000300;
/* *
 *  Value identifying TASHKEEL_CUSTOMIZED_WITHWIDTH
 */
    static final int    ITASHKEEL_CUSTOMIZED_WITHWIDTH      = 0x00000500;
/**
 *  Value identifying TASHKEEL_CUSTOMIZED_ATBEGIN
 */
    static final int    ITASHKEEL_CUSTOMIZED_ATBEGIN        = 0x00000700;
/**
 *  Value identifying TASHKEEL_CUSTOMIZED_ATEND
 */
    static final int    ITASHKEEL_CUSTOMIZED_ATEND          = 0x00000900;
/**
 *  Value identifying TASHKEEL_KEEP
 */
    static final int    ITASHKEEL_KEEP                      = 0x00000B00;


//  This is the default value for uninitialized ArabicOptionSet.
    static final int    DEFAULT = ILAMALEF_AUTO | ISEEN_AUTO |
                                  IYEHHAMZA_AUTO | ITASHKEEL_AUTO;

    int value;

/**
 *  Constructs an ArabicOptionSet with the default value.
 *  The default is:
 *  <pre>
 *  Auto for all options
 *  </pre>
 */
    public ArabicOptionSet()
    {
        this.value = DEFAULT;
    }

/**
 *  Constructs an ArabicOptionSet based on an existing ArabicOptionSet.
 *  @param  set   The ArabicOptionSet which is copied.
 */
    public ArabicOptionSet(ArabicOptionSet set)
    {
        this.value = set.value;
    }

/**
 *  Constructs an ArabicOptionSet from one ArabicOption. The other Arabic
 *  Options are set to their default.
 *  <p>Example:
 *  <pre>
 *  ArabicOptionset bdOpts = new ArabicOptionSet(LAMALEF_NEAR);
 *  </pre>
 *
 *  @param  option    The ArabicOption which is explicitly specified.
 */
    public ArabicOptionSet(ArabicOption option)
    {
        this.value = option.value | DEFAULT;
    }

/**
 *  Constructs an ArabicOptionSet from two ArabicOptions. The other Arabic
 *  options are set to their default.
 *  <p>Example:
 *  <pre>
 *  ArabicOptionset bdOpts = new ArabicOptionSet(LAMALEF_NEAR, SEEN_NEAR);
 *  </pre>
 *
 *  @param  option1   The first ArabicOption which is explicitly specified.
 *  @param  option2   The second ArabicOption which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public ArabicOptionSet(ArabicOption option1, ArabicOption option2)
    {
        this.value = option1.value;
        if ((this.value & option2.value) != 0)  throw new IllegalArgumentException();
        this.value |= option2.value | DEFAULT;
    }

/**
 *  Constructs an ArabicOptionSet from three ArabicOptions.  The other Arabic
 *  option is set to its default.
 *  <p>Example:
 *  <pre>
 *  ArabicOptionset bdOpts = new ArabicOptionSet(LAMALEF_NEAR, SEEN_NEAR, YEHHAMZA_TWO_CELL_NEAR);
 *  </pre>
 *
 *
 *  @param  option1   The first ArabicOption which is explicitly specified.
 *  @param  option2   The second ArabicOption which is explicitly specified.
 *  @param  option3   The third ArabicOption which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public ArabicOptionSet(ArabicOption option1, ArabicOption option2, ArabicOption option3)
    {
        this.value = option1.value;
        if ((this.value & option2.value) != 0)  throw new IllegalArgumentException();
        this.value |= option2.value;
        if ((this.value & option3.value) != 0)  throw new IllegalArgumentException();
        this.value |= option3.value | DEFAULT;
    }

/**
 *  Constructs an ArabicOptionSet from four ArabicOptions.
 *
 *  @param  option1   The first ArabicOption which is explicitly specified.
 *  @param  option2   The second ArabicOption which is explicitly specified.
 *  @param  option3   The third ArabicOption which is explicitly specified.
 *  @param  option4   The fourth ArabicOption which is explicitly specified.
 *
 *  @exception  IllegalArgumentException    If the arguments conflict or are duplicates.
 */
    public ArabicOptionSet(ArabicOption option1, ArabicOption option2,
                       ArabicOption option3, ArabicOption option4)
    {
        this.value = option1.value;
        if ((this.value & option2.value) != 0)  throw new IllegalArgumentException();
        this.value |= option2.value;
        if ((this.value & option3.value) != 0)  throw new IllegalArgumentException();
        this.value |= option3.value;
        if ((this.value & option4.value) != 0)  throw new IllegalArgumentException();
        this.value |= option4.value;
    }

/**
 *  Constructs an ArabicOptionSet from a char array.
 *  The content of the array must follow the specification for the "S" and
 *  "U parts of the BIDI environment variable, as follows:
 *  <ul>
 *  <li>character 1: type of text = I (Implicit) or V (Visual)
 *  <li>character 2: orientation = L (LTR), R (RTL), C (Contextual LTR) or D (Contextual RTL)
 *  <li>character 3: swapping = Y (Swapping ON) or N (Swapping OFF)
 *  <li>character 4: text shaping = N (Nominal), S (Shaped), I (Initial), M (Middle), F (Final) or B (Isolated)
 *  <li>character 5: numeral shaping = N (Nominal), H (National) or C (Contextual)
 *  <li>character 6: bidi algorithm = U (Unicode) or R (Roundtrip)
 *  <li>character 7: Lamalef mode = N (Near), B (At Begin), E (At End) or A (Auto)
 *  <li>character 8: SeenTail mode = N (Near) or A (Auto)
 *  <li>character 9: Yeh Hamza mode = N (Near) or A (Auto)
 *  <li>character 10:Tashkeel mode = K (Keep), B (Customized at Begin), E (Customized at  End) or A (Auto)
 *  </ul>
 *  <p>Only characters 7 to 10 are used to build the ArabicOptionSet.
 *  <p>
 *  @param  chars  character array in Convert parms format. It contains the output options specified in the Bidi
 *  environment variable
 */
    public ArabicOptionSet(char chars[])
    {

        int newValue = DEFAULT;
        int len = chars.length;

        while (len > 6)
        {
            if ('R' == chars[6])
                newValue = (newValue & (~LAMALEF_MASK)) | ILAMALEF_RESIZE_BUFFER;
            else if ('N' == chars[6])
                newValue = (newValue & (~LAMALEF_MASK)) | ILAMALEF_NEAR;
            else if ('B' == chars[6])
                newValue = (newValue & (~LAMALEF_MASK)) | ILAMALEF_ATBEGIN;
            else if ('E' == chars[6])
                newValue = (newValue & (~LAMALEF_MASK)) | ILAMALEF_ATEND;
            else if ('A' == chars[6])
                newValue = (newValue & (~LAMALEF_MASK)) | ILAMALEF_AUTO;

            if (len <= 7)  break;

            if ('N' == chars[7])
                newValue = (newValue & (~SEEN_MASK)) | ISEEN_NEAR;
            else if ('B' == chars[7])
                newValue = (newValue & (~SEEN_MASK)) | ISEEN_ATBEGIN;
            else if ('E' == chars[7])
                newValue = (newValue & (~SEEN_MASK)) | ISEEN_ATEND;
            else if ('A' == chars[7])
                newValue = (newValue & (~SEEN_MASK)) | ISEEN_AUTO;


            if (len <= 8)  break;

            if ('O' == chars[8])
                newValue = (newValue & (~YEHHAMZA_MASK)) | IYEHHAMZA_ONE_CELL;
            else if ('N' == chars[8])
                newValue = (newValue & (~YEHHAMZA_MASK)) | IYEHHAMZA_TWO_CELL_NEAR;
            else if ('B' == chars[8])
                newValue = (newValue & (~YEHHAMZA_MASK)) | IYEHHAMZA_TWO_CELL_ATBEGIN;
            else if ('E' == chars[8])
                newValue = (newValue & (~YEHHAMZA_MASK)) | IYEHHAMZA_TWO_CELL_ATEND;
            else if ('A' == chars[8])
                newValue = (newValue & (~YEHHAMZA_MASK)) | IYEHHAMZA_AUTO;

            if (len <= 9)  break;

            if ('K' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_KEEP;
            else if ('Z' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_CUSTOMIZED_WITHZEROWIDTH;
            else if ('W' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_CUSTOMIZED_WITHWIDTH;
            else if ('B' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_CUSTOMIZED_ATBEGIN;
            else if ('E' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_CUSTOMIZED_ATEND;
            else if ('A' == chars[9])
                newValue = (newValue & (~TASHKEEL_MASK)) | ITASHKEEL_AUTO;

            break;
        }

     this.value = newValue;

    }

/**
 *  Compares two ArabicOptionSets.
 *  Two ArabicOptionSets are considered equal if they represent the same values
 *  for the four Arabic options.
 *  @param  other       The ArabicOptionSet to compare to this.
 *  @return true if the ArabicOptionSets are equal, false otherwise.
 */
    public boolean equals(ArabicOptionSet other)
    {
        if (other == null)  return false;
        return this.value == other.value;
    }

/**
 *  Returns the Lam Alef option from an ArabicOptionSet.
 *  @return The value of the Lam Alef option.
 *  <p> The expected value is one of LAMALEF_NEAR, LAMALEF_ATBEGIN, LAMALEF_ATEND or LAMALEF_AUTO
 */
    public ArabicOption getLamAlefMode()
    {
        switch(this.value & LAMALEF_MASK)
        {
        case ILAMALEF_RESIZE_BUFFER:        return ArabicOption.LAMALEF_RESIZE_BUFFER;
        case ILAMALEF_NEAR:                 return ArabicOption.LAMALEF_NEAR;
        case ILAMALEF_ATBEGIN:              return ArabicOption.LAMALEF_ATBEGIN;
        case ILAMALEF_ATEND:                return ArabicOption.LAMALEF_ATEND;
        case ILAMALEF_AUTO:                 return ArabicOption.LAMALEF_AUTO;
        }
        return ArabicOption.LAMALEF_ATBEGIN;
    }

/**
 *  Returns the Seen option from an ArabicOptionSet.
 *  @return The value of the Seen option.
 *  <p> The expected value is one of SEEN_NEAR or SEEN_AUTO.
 */
    public ArabicOption getSeenMode()
    {
        switch(this.value & SEEN_MASK)
        {
        case ISEEN_NEAR:     return ArabicOption.SEEN_NEAR;
        case ISEEN_ATBEGIN:  return ArabicOption.SEEN_ATBEGIN;
        case ISEEN_ATEND:    return ArabicOption.SEEN_ATEND;
        case ISEEN_AUTO:     return ArabicOption.SEEN_AUTO;
        }
        return ArabicOption.SEEN_NEAR;
    }

/**
 *  Returns the Yeh Hamza option from an ArabicOptionSet.
 *  @return The value of the Yeh Hamza option.
 *  <p> The expected value is one of YEHHAMZA_TWO_CELL_NEAR or YEHHAMZA_AUTO.
 */
    public ArabicOption getYehHamzaMode()
    {
        switch(this.value & YEHHAMZA_MASK)
        {
        case IYEHHAMZA_TWO_CELL_NEAR:      return ArabicOption.YEHHAMZA_TWO_CELL_NEAR;
        case IYEHHAMZA_TWO_CELL_ATBEGIN:   return ArabicOption.YEHHAMZA_TWO_CELL_ATBEGIN;
        case IYEHHAMZA_TWO_CELL_ATEND:     return ArabicOption.YEHHAMZA_TWO_CELL_ATEND;
        case IYEHHAMZA_ONE_CELL:           return ArabicOption.YEHHAMZA_ONE_CELL;
        case IYEHHAMZA_AUTO:               return ArabicOption.YEHHAMZA_AUTO;
        }
        return ArabicOption.YEHHAMZA_TWO_CELL_NEAR;
    }

/**
 *  Returns the Tashkeel option from an ArabicOptionSet.
 *  @return The value of the Tashkeel option.
 *  <p> The expected value is one of TASHKEEL_KEEP, TASHKEEL_CUSTOMIZED_ATBEGIN,
 *  TASHKEEL_CUSTOMIZED_ATEND or TASHKEEL_AUTO.
 */
    public ArabicOption getTashkeelMode()
    {
        switch(this.value & TASHKEEL_MASK)
        {
            case ITASHKEEL_KEEP:                    return ArabicOption.TASHKEEL_KEEP;
            case ITASHKEEL_CUSTOMIZED_WITHZEROWIDTH:return ArabicOption.TASHKEEL_CUSTOMIZED_WITHZEROWIDTH;
            case ITASHKEEL_CUSTOMIZED_WITHWIDTH:    return ArabicOption.TASHKEEL_CUSTOMIZED_WITHWIDTH;
            case ITASHKEEL_CUSTOMIZED_ATBEGIN:      return ArabicOption.TASHKEEL_CUSTOMIZED_ATBEGIN;
            case ITASHKEEL_CUSTOMIZED_ATEND:        return ArabicOption.TASHKEEL_CUSTOMIZED_ATEND;
            case ITASHKEEL_AUTO:                    return ArabicOption.TASHKEEL_AUTO;
        }
        return ArabicOption.TASHKEEL_KEEP;
    }

/**
 *  Returns a hashcode for an ArabicOptionSet.
 *  The hashcode of a ArabicOptionSet is the same as the hashcode of its
 *  value.
 *  @return  A hashcode value.
 */
    public int hashCode()
    {
        return this.value;
    }

/**
 *  Sets all Arabic options based on another ArabicOptionSet.
 *  @param  set       The ArabicOptionSet which is copied.
 */
    public void setAllOptions(ArabicOptionSet set)
    {
        this.value = set.value;
    }

/**
 *  Sets a new value for one of the Bidi Options in a set without changing
 *  the other Arabic Options.
 *  <p>The new value must be one of the pre-defined values for ArabicOption.
 *
 *  @param  newoption    The new value requested for one of the options.
 */
    public void setOneOption(ArabicOption newoption)
    {
        this.value = (this.value & (~newoption.mask)) | newoption.value;
    }

}

