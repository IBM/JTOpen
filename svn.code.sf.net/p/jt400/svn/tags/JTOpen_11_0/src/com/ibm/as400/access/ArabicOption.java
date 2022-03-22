///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ArabicOption.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

 /**
 *  This class represents ArabicOption objects and provides initial values
 *  for each shaping option.
 *  <p>
 *  Arabic text has some special characters that can be converted to different formats, which are characterized
 *  by four Arabic options, these options are stored in four Arabic objects.
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
 *  concerns for this class because it only defines static
 *  final instances.
 *
 */

class ArabicOption
{
/**
*  Lam-Alef option values:
*/

/* *
 *  Value identifying Resize buffer.
 *  <p>
 *  When converting from visual to implicit code pages, Lam-Alef characters
 *  are expanded to Lam plus Alef, while when converting from implicit to
 *  visual code pages, every sequence of Lam followed by Alef is contracted
 *  to a Lam-ALef character. The buffer is enlarged or reduced as appropriate.
 */
    /*public*/ static final ArabicOption    LAMALEF_RESIZE_BUFFER
            = new ArabicOption(ArabicOptionSet.ILAMALEF_RESIZE_BUFFER, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying Near.
 *  <p>
 *  When converting from visual to implicit code pages, Lam-Alef characters
 *  are expanded to Lam plus Alef consuming the blank space next to it.
 *  If no blank space is available, the Lam-Alef character remains as is in
 *  the Unicode uFExx  range.
 *  <p>
 *  When converting from implicit to visual code pages, Lam plus Alef
 *  sequences are compressed to a unique Lam-Alef character; the space
 *  resulting from Lam-Alef compression is positioned  next to each
 *  generated Lam-Alef character.
 */
    public static final ArabicOption    LAMALEF_NEAR
            = new ArabicOption(ArabicOptionSet.ILAMALEF_NEAR, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying At Begin.
 *  <p>
 *  When converting from visual to implicit code pages, Lam-Alef characters
 *  are expanded to Lam plus Alef consuming a blank space at the absolute
 *  beginning of the buffer. If no blank space is available, the Lam-Alef
 *  character remains as is in the Unicode uFExx range.
 *  <p>
 *  When converting from implicit to visual code pages, Lam plus Alef
 *  sequences are compressed to a unique Lam-Alef character; the space
 *  resulting from Lam-Alef compression is positioned at the absolute
 *  beginning of the buffer.
 */
    public static final ArabicOption    LAMALEF_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ILAMALEF_ATBEGIN, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying At End.
 *  <p>
 *  When converting from visual to implicit code pages, Lam-Alef characters
 *  are expanded to Lam plus Alef consuming a blank space at the absolute
 *  end of the buffer. If no blank space is available, the Lam-Alef
 *  character remains as is in the Unicode uFExx range.
 *  <p>
 *  When converting from implicit to visual code pages, Lam plus Alef
 *  sequences are compressed to a unique Lam-Alef character; the space
 *  resulting from Lam-Alef compression is positioned at the absolute
 *  end of the buffer.
 */
    public static final ArabicOption    LAMALEF_ATEND
            = new ArabicOption(ArabicOptionSet.ILAMALEF_ATEND, ArabicOptionSet.LAMALEF_MASK);
/**
 *  Value identifying Auto.
 *  <p>
 *  When converting from visual to implicit code pages, Lam-Alef characters
 *  are expanded to Lam plus Alef consuming a blank space at the beginning
 *  of the buffer with respect to the orientation, i.e. buffer[0] in case
 *  of left-to-right and buffer[length - 1] in case of right-to-left.
 *  If no blank space is available, the Lam-Alef
 *  character remains as is in the Unicode uFExx range.
 *  <p>
 *  When converting from implicit to visual code pages, Lam plus Alef
 *  sequences are compressed to a unique Lam-Alef character; the space
 *  resulting from Lam-Alef compression is positioned at the beginning
 *  of the buffer with respect to the orientation.
 */
    public static final ArabicOption    LAMALEF_AUTO
            = new ArabicOption(ArabicOptionSet.ILAMALEF_AUTO, ArabicOptionSet.LAMALEF_MASK);

/**
*  Seen option values:
*/
/**
 *  Value identifying Near.
 *  <p>
 *  Conversion from visual to implicit converts final forms of the Seen
 *  family represented by two characters (the three quarters shape and the
 *  Tail character) to corresponding final forms represented by one
 *  character and a space replacing the Tail. This space is positioned next
 *  to the Seen final form.
 *  <p>
 *  In conversion from implicit to visual, each final form of characters
 *  in the Seen family (represented by one character) is converted to the
 *  the corresponding final form of the Seen family that is represented by
 *  two characters, consuming the space next to the Seen character. If there
 *  is no space available, it will be converted to one character only which is
 *  the three quarters shape Seen.
 */
    public static final ArabicOption    SEEN_NEAR
            = new ArabicOption(ArabicOptionSet.ISEEN_NEAR, ArabicOptionSet.SEEN_MASK);
/* *
 *  Value identifying At Begin.
 *  <p>
 *  Conversion from visual to implicit converts final forms of the Seen
 *  family represented by two characters (the three quarters shape and the
 *  Tail character) to corresponding final forms represented by one
 *  character and a space replacing the Tail. The resulting space is moved
 *  to the absolute beginning of the buffer.
 *  <p>
 *  In conversion from implicit to visual, each final form of characters
 *  in the Seen family (represented by one character) is converted to the
 *  the corresponding final form of the Seen family that is represented by
 *  two characters, consuming a space at the absolute beginning of the buffer.
 */
    /*public*/ static final ArabicOption    SEEN_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ISEEN_ATBEGIN, ArabicOptionSet.SEEN_MASK);

/* *
 *  Value identifying At End.
 *  <p>
 *  Conversion from visual to implicit converts final forms of the Seen
 *  family represented by two characters (the three quarters shape and the
 *  Tail character) to corresponding final forms represented by one
 *  character and a space replacing the Tail. The resulting space is moved
 *  to the absolute end of the buffer.
 *  <p>
 *  In conversion from implicit to visual, each final form of characters
 *  in the Seen family (represented by one character) is converted to the
 *  the corresponding final form of the Seen family that is represented by
 *  two characters, consuming a space at the absolute end of the buffer.
 */
    /*public*/ static final ArabicOption    SEEN_ATEND
            = new ArabicOption(ArabicOptionSet.ISEEN_ATEND, ArabicOptionSet.SEEN_MASK);

/**
 *  Value identifying Auto.
 *  <p>
 *  Same behavior as NEAR for this release
 */
    public static final ArabicOption    SEEN_AUTO
            = new ArabicOption(ArabicOptionSet.ISEEN_AUTO, ArabicOptionSet.SEEN_MASK);

/**
*  Yeh Hamza option values:
*/
/**
 *  Value identifying Near.
 *  <p>
 *  Conversion from visual to implicit converts each Yeh character followed
 *  by a Hamza character to a Yeh-Hamza character; the space resulting from
 *  the contraction is positioned next to the Yeh-Hamza character.
 *  <p>
 *  In conversion from implicit to visual, each Yeh-Hamza character is
 *  expanded to two characters (Yeh and Hamza), consuming the space located
 *  next to the original Yeh-Hamza character. If there is no space available,
 *  it will be converted to one character which is Yeh.
 */
    public static final ArabicOption    YEHHAMZA_TWO_CELL_NEAR
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_NEAR, ArabicOptionSet.YEHHAMZA_MASK);
/* *
 *  Value identifying At Begin.
 *  <p>
 *  In conversion from visual to implicit, each Yeh character followed
 *  by a Hamza character is contracted to a Yeh-Hamza character (one
 *  character); the resulting space is positioned at the absolute beginning
 *  of the buffer.
 *  <p>
 *  In conversion from implicit to visual, each Yeh-Hamza character is
 *  expanded to two characters (Yeh and Hamza), consuming the space located
 *  at the absolute beginning of the buffer.
 */
    /*public*/ static final ArabicOption    YEHHAMZA_TWO_CELL_ATBEGIN
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_ATBEGIN, ArabicOptionSet.YEHHAMZA_MASK);

/* *
 *  Value identifying At End.
 *  <p>
 *  In conversion from visual to implicit, each Yeh character followed
 *  by a Hamza character is contracted to a Yeh-Hamza character (one
 *  character); the resulting space is positioned at the absolute end
 *  of the buffer.
 *  <p>
 *  In conversion from implicit to visual, each Yeh-Hamza character is
 *  expanded to two characters (Yeh and Hamza), consuming the space located
 *  at the absolute end of the buffer.
 */
    /*public*/ static final ArabicOption    YEHHAMZA_TWO_CELL_ATEND
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_TWO_CELL_ATEND, ArabicOptionSet.YEHHAMZA_MASK);
/* *
 *  Value identifying One cell.
 *  <p>
 *  In conversion from visual to implicit, each Yeh character followed
 *  by a Hamza character is contracted to a Yeh-Hamza character (one
 *  character); the resulting space is positioned next to the generated
 *  character.
 *  <p>
 *  In conversion from implicit to visual, each Yeh-Hamza character is
 *  expanded to two characters (Yeh and Hamza), consuming the space located
 *  next to the original Yeh-Hamza character.
 */
    /*public*/ static final ArabicOption    YEHHAMZA_ONE_CELL
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_ONE_CELL, ArabicOptionSet.YEHHAMZA_MASK);

/**
 *  Value identifying Auto.
 *  <p>
 *  Same behavior as NEAR for this release
 */
    public static final ArabicOption    YEHHAMZA_AUTO
            = new ArabicOption(ArabicOptionSet.IYEHHAMZA_AUTO, ArabicOptionSet.YEHHAMZA_MASK);

/**
*  Tashkeel option values:
*/

/**
 *  Value identifying Keep Tashkeel.
 *  <p>
 *  No special processing is done
 */
    public static final ArabicOption    TASHKEEL_KEEP
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_KEEP, ArabicOptionSet.TASHKEEL_MASK);

/* *
 *  Value identifying Customized With Zero Width.
 *  <p>
 *  All Tashkeel characters are converted to the corresponding non-spacing
 * (zero-width) characters.
 */
    /*public*/ static final ArabicOption    TASHKEEL_CUSTOMIZED_WITHZEROWIDTH
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_WITHZEROWIDTH, ArabicOptionSet.TASHKEEL_MASK);
/* *
 *  Value identifying Customized With Width.
 *  <p>
 *  All Tashkeel characters are converted to the corresponding spacing
 *  characters. This option is not available in case of visual to implicit
 *  conversion because Tashkeel characters in the Arabic u06xx range all
 *  represent non-spacing (zero-width) characters.
 */
    /*public*/ static final ArabicOption    TASHKEEL_CUSTOMIZED_WITHWIDTH
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_WITHWIDTH, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Customized At Begin.
 *  <p>
 *  All Tashkeel characters except for Shadda are replaced by spaces.
 *  The resulting spaces are moved to the absolute beginning of the buffer.
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_ATBEGIN
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_ATBEGIN, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Customized At End.
 *  <p>
 *  All Tashkeel characters except for Shadda are replaced by spaces.
 *  The resulting spaces are moved to the absolute end of the buffer.
 */
    public static final ArabicOption    TASHKEEL_CUSTOMIZED_ATEND
            = new ArabicOption(ArabicOptionSet.ITASHKEEL_CUSTOMIZED_ATEND, ArabicOptionSet.TASHKEEL_MASK);
/**
 *  Value identifying Auto.
 *  <p>
 *  Same behavior as KEEP for this release
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

