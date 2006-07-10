///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400BidiTransform.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The AS400BidiTransform class provides layout transformations that allow the conversion of Bidi text in server format (after its conversion to Unicode) to Bidi text in Java format, or vice-versa.
 <p>Bidi text is a combination of a sequence of characters and a set of Bidi flags.  That text (Arabic or Hebrew) has characters which are read from right to left.  That text might also be mixed with numbers which are read from left to right, and possibly also mixed with Latin characters.  Conversion support is needed to display text properly with the correct order and shape.
 <p>Bidi text from an i5/OS system may be represented by a combination of a String (the characters) and a CCSID (which implies a set of Bidi flags specific to that CCSID).
 <p><b>Multi-threading considerations:</b> different threads may use the same AS400BidiTransform object if they have the same transformation needs, as follows:
 <ul>
 <li>Same CCSID for the server data.
 <li>Same string type for the server data (if the default string type of the CCSID is used, this will result from using the same CCSID).
 <li>Same orientation for Java data (if the Java data orientation is derived from the server string type, this will result from using the same string type for server data).
 </ul>
 <p>Otherwise, each thread must use its own instances of this class.
 <p>The following example illustrate how to transform bidi text:
 <blockquote><pre>
 * // Java data to server layout:
 * AS400BidiTransform abt;
 * abt = new AS400BidiTransform(424);
 * String dst = abt.toAS400Layout("some bidi string");
 *
 * // Specifying a new CCSID for an existing AS400BidiTransform object:
 * abt.setAS400Ccsid(62234);                    // 420 RTL //
 * String dst = abt.toAS400Layout("some bidi string");
 *
 * // Specifying a non-default string type for a given CCSID:
 * abt.setAS400StringType(BidiStringType.ST4);  // Vis LTR //
 * String dst = abt.toAS400Layout("some bidi string");
 *
 * // Specifying a non-default string type for Java data:
 * abt.setJavaStringType(BidiStringType.ST11);  // Imp Context LTR //
 * String dst = abt.toAS400Layout("some bidi string");
 *
 * // How to transform server data to Java layout:
 * abt.setJavaStringType(BidiStringType.ST6);   // Imp RTL //
 * String dst = abt.toJavaLayout("some bidi string");
 </pre></blockquote>
 **/
public class AS400BidiTransform
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private static final int ST1 = 1;
    private static final int ST2 = 2;
    private static final int ST3 = 3;
    private static final int ST4 = 4;
    private static final int ST5 = 5;
    private static final int ST6 = 6;
    private static final int ST7 = 7;
    private static final int ST8 = 8;
    private static final int ST9 = 9;
    private static final int ST10 = 10;
    private static final int ST11 = 11;
    private static final int ST12 = 12;
    private static final int ST13 = 13;
    private static final int ST14 = 14;

    private static final int[][] CCSID_TABLE =
    {
        // CCSID, String-Type
        { 420, ST4 },  // Arabic.420
        { 424, ST4 },  // Hebrew.424
        { 425, ST5 },  // Arabic/Latin EBCDIC defined for OS/390 open edition.
        { 856, ST5 },  // Hebrew.856
        { 862, ST4 },  // Hebrew.862
        { 864, ST5 },  // Arabic.864
        { 867, ST4 },  // Hebrew.867
        { 916, ST5 },  // Hebrew.916
        { 1046, ST5 },  // Arabic.1046
        { 1089, ST5 },  // Arabic.1089
        { 1255, ST5 },  // Hebrew.1255
        { 1256, ST5 },  // Arabic.1256
        { 5012, ST5 },  // Hebrew.916
        { 5351, ST5 },  // Hebrew.1255
        { 5352, ST5 },  // Arabic.1256
        { 8612, ST5 },  // Arabic.420
        { 8616, ST10 },  // Hebrew.424
        { 9238, ST5 },  // Arabic.1046
        { 12708, ST7 },  // Arabic.420
        { 12712, ST10 },  // Hebrew.424
        { 13488, ST10 },  // Unicode.10646
        { 16804, ST4 },  // Arabic.420
        { 17248, ST5 },  // Arabic.864
        { 61952, ST10 },  // Unicode.10646
        { 62208, ST4 },  // Hebrew.856
        { 62209, ST10 },  // Hebrew.862
        { 62210, ST4 },  // Hebrew.916
        { 62211, ST5 },  // Hebrew.424
        { 62212, ST10 },  // Hebrew.867
        { 62213, ST5 },  // Hebrew.862
        { 62214, ST5 },  // Hebrew.867
        { 62215, ST4 },  // Hebrew.1255
        { 62216, ST6 },  // Hebrew.867
        { 62217, ST8 },  // Hebrew.867
        { 62218, ST4 },  // Arabic.864
        { 62219, ST11 },  // Hebrew.867
        { 62220, ST6 },  // Hebrew.856
        { 62221, ST6 },  // Hebrew.862
        { 62222, ST6 },  // Hebrew.916
        { 62223, ST6 },  // Hebrew.1255
        { 62224, ST6 },  // Arabic.420
        { 62225, ST6 },  // Arabic.864
        { 62226, ST6 },  // Arabic.1046
        { 62227, ST6 },  // Arabic.1089
        { 62228, ST6 },  // Arabic.1256
        { 62229, ST8 },  // Hebrew.424
        { 62230, ST8 },  // Hebrew.856
        { 62231, ST8 },  // Hebrew.862
        { 62232, ST8 },  // Hebrew.916
        { 62233, ST8 },  // Arabic.420
        { 62234, ST9 },  // Arabic.420
        { 62235, ST6 },  // Hebrew.424
        { 62236, ST10 },  // Hebrew.856
        { 62237, ST8 },  // Hebrew.1255
        { 62238, ST10 },  // Hebrew.916
        { 62239, ST10 },  // Hebrew.1255
        { 62240, ST11 },  // Hebrew.424
        { 62241, ST11 },  // Hebrew.856
        { 62242, ST11 },  // Hebrew.862
        { 62243, ST11 },  // Hebrew.916
        { 62244, ST11 },  // Hebrew.1255
        { 62245, ST10 },  // Hebrew.424
        { 62251, ST6 }    // Arabic.425 - Used for OS/390.
    };

    private static final int CCSID_MAX = CCSID_TABLE.length - 1;

    private static final BidiFlagSet[] FLAG_SET = new BidiFlagSet[12];
    private static final BidiFlagSet NORMAL_FLAG_SET = initFlagSet(0);

    private int as400Ccsid_;  // Server CCSID.
    private int as400Type_;  // String type of server data.
    private int javaType_;  // String type of Java data.
    private BidiTransform bdxJ2A_ = new BidiTransform();  // From Java to server.
    private BidiTransform bdxA2J_ = new BidiTransform();  // From server to Java.
    private BidiTransform lastTransform_ = bdxA2J_;  // Keeps track of which transform was used last.

    /**
     Constructs an AS400BidiTransform object assuming that the server Bidi text conforms to a given CCSID.  Typically this will be the CCSID of the system.
     <p>The given CCSID has a default string type which defines a set of Bidi flags.  The orientation implied by this string type is applied to both the server data layout and the Java data layout.
     @param  as400Ccsid  The CCSID of the server data.
     **/
    public AS400BidiTransform(int as400Ccsid)
    {
        bdxA2J_.flags = new BidiFlagSet(NORMAL_FLAG_SET);
        setAS400Ccsid(as400Ccsid);
    }

    /**
     Indicates if a given CCSID may apply to Bidi data.  This is the case for Arabic and Hebrew CCSIDs, and for Unicode (which can encode anything).
     <p>If a CCSID is not Bidi, there is no need to perform layout transformations when converting server data to Java data and vice-versa.
     @param  ccsid  The CCSID to check.
     @return  true if the given CCSID may apply to Bidi data, false otherwse.
     **/
    public static boolean isBidiCcsid(int ccsid)
    {
        switch (ccsid)
        {
            case 420:
            case 424:
            case 425:
            case 856:
            case 862:
            case 864:
            case 867:
            case 916:
            case 1046:
            case 1089:
            case 1255:
            case 1256:
            case 5012:
            case 5351:
            case 5352:
            case 8612:
            case 8616:
            case 9238:
            case 12708:
            case 12712:
            case 13488:
            case 16804:
            case 17248:
            case 61952:
            case 62208:
            case 62209:
            case 62210:
            case 62211:
            case 62212:
            case 62213:
            case 62214:
            case 62215:
            case 62216:
            case 62217:
            case 62218:
            case 62219:
            case 62220:
            case 62221:
            case 62222:
            case 62223:
            case 62224:
            case 62225:
            case 62226:
            case 62227:
            case 62228:
            case 62229:
            case 62230:
            case 62231:
            case 62232:
            case 62233:
            case 62234:
            case 62235:
            case 62236:
            case 62237:
            case 62238:
            case 62239:
            case 62240:
            case 62241:
            case 62242:
            case 62243:
            case 62244:
            case 62245:
            case 62251:
                return true;
            default:
                return false;
        }
    }

    /**
     Indicates if a given CCSID has a visual string type.
     @param  ccsid  The CCSID to check.
     @return  true if the given CCSID has a visual string type, false otherwse.
     **/
    public static boolean isVisual(int ccsid)
    {
        int st = getStringType(ccsid);
        if (st == ST4 || st == ST7 || st == ST8 || st == ST9) return true;
        return false;
    }

    /**
     Sets the CCSID.
     <p>The given CCSID has a default string type which defines a set of Bidi flags.  The orientation implied by this string type is applied to both the server data layout and the Java data layout.
     @param  as400Ccsid  The CCSID of the server data.
     **/
    public void setAS400Ccsid(int as400Ccsid)
    {
        as400Ccsid_ = as400Ccsid;
        setAS400StringType(getStringType(as400Ccsid));
    }

    /**
     Returns the current CCSID of server data.
     @return  The CCSID for the server data.
     **/
    public int getAS400Ccsid()
    {
        return as400Ccsid_;
    }

    /**
     Set the explicit string type for server data.  Each CCSID has a default CDRA string type, which defines a set of Bidi flags.  This method may be used to specify Bidi flags different from those implied by the CCSID.
     <p>The orientation implied by the new given string type is applied to both the server data layout and the Java data layout.  The new string type is applied to the Java data layout by calling setJavaStringType(as400Type).
     @param  as400Type  The string type to apply to server data.  The parameter string type should always be one of the constants defined in BidiStringType.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public void setAS400StringType(int as400Type)
    {
        if (as400Type != BidiStringType.DEFAULT && (as400Type < BidiStringType.ST4 || as400Type > BidiStringType.ST11))
        {
            Trace.log(Trace.ERROR, "Attempting to set the as400 string type to an invalid Bidi sting type.");
            throw new ExtendedIllegalArgumentException("as400Type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        as400Type_ = as400Type;
        if (FLAG_SET[as400Type] == null) FLAG_SET[as400Type] = initFlagSet(as400Type);
        bdxJ2A_.flags = FLAG_SET[as400Type];
        setJavaStringType(as400Type);
    }

    /**
     Returns the current string type of server data.
     @return  The string type of the server data.
     **/
    public int getAS400StringType()
    {
        return as400Type_;
    }

    /**
     Set the explicit string type for Java data.  The parameter string type should always be one of the following defined in BidiStringType:   ST5 (LTR), ST6 (RTL), ST10 (Contextual LTR), or ST11 (Contextual RTL).  In fact, only the orientation of the given string type is used to modify the Bidi flags to apply to the Java data.  The other Bidi flags of the Java data always conform to the Unicode standard.
     @see  com.ibm.as400.access.BidiStringType
     @param  javaType  The string type to apply to Java data.
     **/
    public void setJavaStringType(int javaType)
    {
        if (javaType != BidiStringType.DEFAULT && (javaType < BidiStringType.ST4 || javaType > BidiStringType.ST11))
        {
           Trace.log(Trace.ERROR, "Attempting to set the java string type to an invalid Bidi sting type.");
           throw new ExtendedIllegalArgumentException("javaType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (FLAG_SET[javaType] == null) FLAG_SET[javaType] = initFlagSet(javaType);
        BidiFlag orient = FLAG_SET[javaType].getOrientation();
        // Copy orientation from javaType to bdxA2J_.flags.
        bdxA2J_.flags.setOneFlag(orient);
        if (orient == BidiFlag.ORIENTATION_LTR) javaType_ = 5;
        else if (orient == BidiFlag.ORIENTATION_RTL) javaType_ = 6;
        else if (orient == BidiFlag.ORIENTATION_CONTEXT_LTR) javaType_ = 10;
        else if (orient == BidiFlag.ORIENTATION_CONTEXT_RTL) javaType_ = 11;
        else javaType_ = 5;  // Must never happen.
    }

    /**
     Returns the current string type of Java data.
     @return  The string type of the Java data.
     **/
    public int getJavaStringType()
    {
        return javaType_;
    }

    /**
     Sets the bidi converion properties.
     @param  properties  The bidi conversion properties.
     **/
    public void setBidiConversionProperties(BidiConversionProperties properties)
    {
        properties.copyOptionsTo(bdxJ2A_);
        properties.copyOptionsTo(bdxA2J_);
        if (properties.isBidiRemoveMarksOnImplicitToVisual())
        {
            bdxJ2A_.removeMarkers=true;
            bdxA2J_.removeMarkers=false;
        }
        setJavaStringType(properties.getBidiStringType());
    }

    /**
     Returns the bidi converion properties.
     @return  The bidi converion properties.
     **/
    public BidiConversionProperties getBidiConversionProperties()
    {
        boolean removeMarksOnJ2A = (bdxJ2A_.removeMarkers==true && bdxA2J_.removeMarkers==false);
        return  new BidiConversionProperties(getJavaStringType(),lastTransform_, removeMarksOnJ2A);
    }

    /**
     Convert data from the server layout to the Java layout.
     @param  as400Text  The server string to convert.
     @return  The same text in standard Java Bidi layout.
     **/
    public String toJavaLayout(String as400Text)
    {
        lastTransform_ = bdxA2J_;
        BidiText src = new BidiText(bdxJ2A_.flags, as400Text);
        return src.transform(bdxA2J_).toString();
    }

    /**
     Convert data from the Java layout to the server layout.
     @param  javaText  The Java string to convert.
     @return  The same text in server Bidi layout.
     **/
    public String toAS400Layout(String javaText)
    {
        lastTransform_ = bdxJ2A_;
        BidiText src = new BidiText(bdxA2J_.flags, javaText);
        return src.transform(bdxJ2A_).toString();
    }

    public static int getStringType(int ccsid)
    {
        // Return default string type for parm ccsid based on CCSID_TABLE.
        int low = 0;
        int high = CCSID_MAX;

        while (low < high)
        {
            int mid = (low + high) / 2;
            if (ccsid < CCSID_TABLE[mid][0]) high = mid - 1;
            else if (ccsid > CCSID_TABLE[mid][0]) low = mid + 1;
            else return CCSID_TABLE[mid][1];
        }
        if ((low == high) && (ccsid == CCSID_TABLE[low][0])) return CCSID_TABLE[low][1];
        return 0;
    }

    // Return BidiFlagSet according to string type.
    private static BidiFlagSet initFlagSet(int stringType)
    {
        switch (stringType)
        {
            case ST4:
                return new BidiFlagSet(BidiFlag.TYPE_VISUAL,
                                       BidiFlag.NUMERALS_NOMINAL,
                                       BidiFlag.ORIENTATION_LTR,
                                       BidiFlag.TEXT_SHAPED,
                                       BidiFlag.SWAP_NO);
            case ST5:
                return  new BidiFlagSet(BidiFlag.TYPE_IMPLICIT,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_LTR,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_YES);
            case ST6:
                return  new BidiFlagSet(BidiFlag.TYPE_IMPLICIT,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_RTL,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_YES);
            case ST7:
                return  new BidiFlagSet(BidiFlag.TYPE_VISUAL,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_CONTEXT_LTR,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_NO);
            case ST8:
                return  new BidiFlagSet(BidiFlag.TYPE_VISUAL,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_RTL,
                                        BidiFlag.TEXT_SHAPED,
                                        BidiFlag.SWAP_NO);
            case ST9:
                return  new BidiFlagSet(BidiFlag.TYPE_VISUAL,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_RTL,
                                        BidiFlag.TEXT_SHAPED,
                                        BidiFlag.SWAP_YES);
            case ST10:
                return  new BidiFlagSet(BidiFlag.TYPE_IMPLICIT,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_CONTEXT_LTR,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_YES);
            case ST11:
                return  new BidiFlagSet(BidiFlag.TYPE_IMPLICIT,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_CONTEXT_RTL,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_YES);
            default:
                return  new BidiFlagSet(BidiFlag.TYPE_IMPLICIT,
                                        BidiFlag.NUMERALS_NOMINAL,
                                        BidiFlag.ORIENTATION_CONTEXT_LTR,
                                        BidiFlag.TEXT_NOMINAL,
                                        BidiFlag.SWAP_YES);
        }
    }
}
