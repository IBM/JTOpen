///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiFlag.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

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
 *  <li> numeral shapes (Nominal, National or Contextual)
 *  <li> text shapes (Nominal, Shaped, Initial, Middle, Final or Isolated)
 *  </ul>
 *  The BidiFlags are pre-defined in this class.  Each one represents one
 *  possible value of one Bidi attribute.  All possible values are defined,
 *  so there is no need (or possibility) to add new ones.
 *
 *  <p><b>Multi-threading considerations:</b> There are no multi-threading
 *  concerns for this class, since it only defines static
 *  final instances.
 *
 */

class BidiFlag
 {
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

/**
 *  Value identifying Implicit type of text
 */
    public static final BidiFlag    TYPE_IMPLICIT
            = new BidiFlag(BidiFlagSet.ITYPE_IMPLICIT, BidiFlagSet.TYPE_MASK);
/**
 *  Value identifying Visual type of text
 */
    public static final BidiFlag    TYPE_VISUAL
            = new BidiFlag(BidiFlagSet.ITYPE_VISUAL, BidiFlagSet.TYPE_MASK);

/**
 *  Value identifying LTR orientation
 */
    public static final BidiFlag    ORIENTATION_LTR
            = new BidiFlag(BidiFlagSet.IORIENTATION_LTR, BidiFlagSet.ORIENTATION_MASK);
/**
 *  Value identifying RTL orientation
 */
    public static final BidiFlag    ORIENTATION_RTL
            = new BidiFlag(BidiFlagSet.IORIENTATION_RTL, BidiFlagSet.ORIENTATION_MASK);
/**
 *  Value identifying Contextual orientation with default to LTR
 */
    public static final BidiFlag    ORIENTATION_CONTEXT_LTR
            = new BidiFlag(BidiFlagSet.IORIENTATION_CONTEXT_LTR, BidiFlagSet.ORIENTATION_MASK);
/**
 *  Value identifying Contextual orientation with default to RTL
 */
    public static final BidiFlag    ORIENTATION_CONTEXT_RTL
            = new BidiFlag(BidiFlagSet.IORIENTATION_CONTEXT_RTL, BidiFlagSet.ORIENTATION_MASK);

/**
 *  Value identifying that Symmetric Swapping has been applied
 */
    public static final BidiFlag    SWAP_YES
            = new BidiFlag(BidiFlagSet.ISWAP_YES, BidiFlagSet.SWAP_MASK);
/**
 *  Value identifying that Symmetric Swapping has not been applied
 */
    public static final BidiFlag    SWAP_NO
            = new BidiFlag(BidiFlagSet.ISWAP_NO, BidiFlagSet.SWAP_MASK);

/**
 *  Value identifying that Numeral Shapes are Nominal
 */
    public static final BidiFlag    NUMERALS_NOMINAL
            = new BidiFlag(BidiFlagSet.INUMERALS_NOMINAL, BidiFlagSet.NUMERALS_MASK);
/**
 *  Value identifying that Numeral Shapes are National
 */
    public static final BidiFlag    NUMERALS_NATIONAL
            = new BidiFlag(BidiFlagSet.INUMERALS_NATIONAL, BidiFlagSet.NUMERALS_MASK);
/**
 *  Value identifying that Numeral Shapes are Contextual (Nominal or National
 *  depending on context)
 */
    public static final BidiFlag    NUMERALS_CONTEXTUAL
            = new BidiFlag(BidiFlagSet.INUMERALS_CONTEXTUAL, BidiFlagSet.NUMERALS_MASK);
/**
 *  Value identifying that Numeral Shapes may be Nominal or National
 */
    public static final BidiFlag    NUMERALS_ANY
            = new BidiFlag(BidiFlagSet.INUMERALS_ANY, BidiFlagSet.NUMERALS_MASK);

/**
 *  Value identifying that the text is stored in Nominal characters
 */
    public static final BidiFlag    TEXT_NOMINAL
            = new BidiFlag(BidiFlagSet.ITEXT_NOMINAL, BidiFlagSet.TEXT_MASK);
/**
 *  Value identifying that the text is stored in Shaped characters
 */
    public static final BidiFlag    TEXT_SHAPED
            = new BidiFlag(BidiFlagSet.ITEXT_SHAPED, BidiFlagSet.TEXT_MASK);
/**
 *  Value identifying that the text must be displayed in Initial characters
 */
    public static final BidiFlag    TEXT_INITIAL
            = new BidiFlag(BidiFlagSet.ITEXT_INITIAL, BidiFlagSet.TEXT_MASK);
/**
 *  Value identifying that the text must be displayed in Middle characters
 */
    public static final BidiFlag    TEXT_MIDDLE
            = new BidiFlag(BidiFlagSet.ITEXT_MIDDLE, BidiFlagSet.TEXT_MASK);
/**
 *  Value identifying that the text must be displayed in Final characters
 */
    public static final BidiFlag    TEXT_FINAL
            = new BidiFlag(BidiFlagSet.ITEXT_FINAL, BidiFlagSet.TEXT_MASK);
/**
 *  Value identifying that the text must be displayed in Isolated characters
 */
    public static final BidiFlag    TEXT_ISOLATED
            = new BidiFlag(BidiFlagSet.ITEXT_ISOLATED, BidiFlagSet.TEXT_MASK);

    int value;
    int mask;       //  This makes BidiFlagSet.setOneFlag more efficient

    private BidiFlag(int initValue, int initMask)
    {
        value = initValue;
        mask = initMask;
    }
}

