///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ArabicOption.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

 /**
 *  Arabic text has some special characters that can be converted to different formats, which are characterized
 *  by four Arabic options, these options are stored in four Arabic objects.
 *  These four options constitute an ArabicOptionSet.
 *  <p>
 *  An ArabicOptionSet object contains a value for each of the four Arabic objects
 *  which represent the Arabic options.  The four options are:
 *  <ul>
 *  <li> Lam-Alef handling (Resize buffer, Near, At Begin, At End or Auto)
 *  <li> Seen handling (Near, At Begin, At End or Auto)
 *  <li> Yeh Hamza handling (One cell, Near, At Begin, At End or Auto)
 *  <li> Tashkeel handling (Keep, Customized with Zero Width, Customized With Width, Customized At Begin,
 *  Customized At end, or Auto)
 *  </ul>
 *  The Arabic options values are pre-defined in this class. Each one represents one
 *  possible value of one Arabic option.
 *
 *  <p><b>Multi-threading considerations:</b> There are no multi-threading
 *  concerns for this class, since it only defines static
 *  final instances.
 *
 */

class ArabicOption
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

/**
*  Lam-Alef option values:
*/

/**
 *  Value identifying Resize buffer
 */
    public static final ArabicOption    LAMALEF_RESIZE_BUFFER
            = new ArabicOption(ArabicOptionSet.ILAMALEF_RESIZE_BUFFER, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying Near
 */
    public static final ArabicOption    LAMALEF_NEAR
            = new ArabicOption(ArabicOptionSet.ILAMALEF_NEAR, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying At Begin
 */
    public static final ArabicOption    LAMALEF_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ILAMALEF_ATBEGIN, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying At End
 */
    public static final ArabicOption    LAMALEF_ATEND
            = new ArabicOption(ArabicOptionSet.ILAMALEF_ATEND, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying Auto
 */
    public static final ArabicOption    LAMALEF_AUTO
            = new ArabicOption(ArabicOptionSet.ILAMALEF_AUTO, ArabicOptionSet.LAMALEF_MASK);

/**
*  Seen option values:
*/
/**
 *  Value identifying Near
 */
    public static final ArabicOption    SEEN_NEAR
            = new ArabicOption(ArabicOptionSet.ISEEN_NEAR, ArabicOptionSet.SEEN_MASK);
/**
 *  Value identifying At Begin
 */
    public static final ArabicOption    SEEN_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ISEEN_ATBEGIN, ArabicOptionSet.SEEN_MASK);

/**
 *  Value identifying At End
 */
    public static final ArabicOption    SEEN_ATEND
            = new ArabicOption(ArabicOptionSet.ISEEN_ATEND, ArabicOptionSet.SEEN_MASK);
/**

/**
 *  Value identifying Auto
 */
    public static final ArabicOption    SEEN_AUTO
            = new ArabicOption(ArabicOptionSet.ISEEN_AUTO, ArabicOptionSet.SEEN_MASK);

/**
*  Yeh Hamza option values:
*/
/**
 *  Value identifying Near
 */
    public static final ArabicOption    YEHHAMZA_TWO_CELL_NEAR
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_NEAR, ArabicOptionSet.YEHHAMZA_MASK);
/**
 *  Value identifying At Begin
 */
    public static final ArabicOption    YEHHAMZA_TWO_CELL_ATBEGIN
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_ATBEGIN, ArabicOptionSet.YEHHAMZA_MASK);

/**
 *  Value identifying At End
 */
    public static final ArabicOption    YEHHAMZA_TWO_CELL_ATEND
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_ATEND, ArabicOptionSet.YEHHAMZA_MASK);
/**
 *  Value identifying One cell
 */
    public static final ArabicOption    YEHHAMZA_ONE_CELL
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_ONE_CELL, ArabicOptionSet.YEHHAMZA_MASK);

/**
 *  Value identifying Auto
 */
    public static final ArabicOption    YEHHAMZA_AUTO
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_AUTO, ArabicOptionSet.YEHHAMZA_MASK);

/**
*  Tashkeel option values:
*/

/**
 *  Value identifying Keep Tashkeel (No special processing is done)
 */
    public static final ArabicOption    TASHKEEL_KEEP
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_KEEP, ArabicOptionSet.TASHKEEL_MASK);

/**
 *  Value identifying Customized With Zero Width
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_WITHZEROWIDTH
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_WITHZEROWIDTH, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Customized With Width
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_WITHWIDTH
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_WITHWIDTH, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Customized At Begin
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_ATBEGIN, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Customized At end
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_ATEND
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_ATEND, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Auto
 */
    public static final ArabicOption    TASHKEEL_AUTO
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_AUTO, ArabicOptionSet.TASHKEEL_MASK);

    int value;
    int mask;

    private ArabicOption(int initValue, int initMask)
    {
        value = initValue;
        mask = initMask;
    }
}

