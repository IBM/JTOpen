///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400BidiTransform.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400BidiTransform class provides layout transformations that
 *  allow the conversion of Bidi text in AS/400 format (after its 
 *  conversion to Unicode) to Bidi text in Java format, or
 *  vice-versa.
 *  <p>
 *  Bidi text is a combination of a sequence of characters and a set of
 *  Bidi flags. That text (Arabic or Hebrew) has characters which are read 
 *  from right to left. That text might also be mixed with numbers which 
 *  are read from left to right, and possibly also mixed with Latin 
 *  characters. Conversion support is needed to display text properly 
 *  with the correct order and shape. 
 *  <p>
 *  Bidi text from an AS/400 system may be represented
 *  by a combination of a String (the characters) and a CCSID
 *  (which implies a set of Bidi flags specific to that CCSID).
 *  <p>
 *  <b>Multi-threading considerations:</b> different threads may use the
 *  same AS400BidiTransform object if they have the same transformation
 *  needs, as follows:
 *  <ul>
 *    <li>same CCSID for the AS/400 data
 *    <li>same string type for the AS/400 data (if the default string 
 *        type of the CCSID is used, this will result from using the same CCSID)
 *    <li>same orientation for Java data (if the Java data orientation is
 *        derived from the AS/400 string type, this will result from using the
 *        same string type for AS/400 data)
 *  </ul>
 *  Otherwise, each thread must use its own instances of this class.
 *
 *  The following example illustrate how to transform bidi text:
 *  <p>
 *  <BLOCKQUOTE><PRE>
 *  // Java data to AS/400 layout:
 *  AS400BidiTransform abt;
 *  abt = new AS400BidiTransform(424);
 *  String dst = abt.toAS400Layout("some bidi string");
 *  <p>
 *  // Specifying a new CCSID for an existing AS400BidiTransform object:
 *  abt.setAS400Ccsid(62234);                    // 420 RTL //
 *  String dst = abt.toAS400Layout("some bidi string");
 *  <p>
 *  // Specifying a non-default string type for a given CCSID:
 *  abt.setAS400StringType(BidiStringType.ST4);  // Vis LTR //
 *  String dst = abt.toAS400Layout("some bidi string");
 *  <p>
 *  // Specifying a non-default string type for Java data:
 *  abt.setJavaStringType(BidiStringType.ST11);  // Imp Context LTR //
 *  String dst = abt.toAS400Layout("some bidi string");
 *  <p>
 *  // How to transform AS/400 data to Java layout:
 *  abt.setJavaStringType(BidiStringType.ST6);   // Imp RTL //
 *  String dst = abt.toJavaLayout("some bidi string");
 *  </PRE></BLOCKQUOTE>
 *
 **/

public class AS400BidiTransform
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


/*----------------------  Private Variables  -------------------------*/

    //@P0C - Changed from chars to ints
    private final static int ST1  = 1 ;
    private final static int ST2  = 2 ;
    private final static int ST3  = 3 ;
    private final static int ST4  = 4 ;
    private final static int ST5  = 5 ;
    private final static int ST6  = 6 ;
    private final static int ST7  = 7 ;
    private final static int ST8  = 8 ;
    private final static int ST9  = 9 ;
    private final static int ST10 = 10;
    private final static int ST11 = 11;
    private final static int ST12 = 12;
    private final static int ST13 = 13;
    private final static int ST14 = 14;

    private final static int[][] ccsidTable = //@P0C
        {
        //                 CCSID    String-Type
        /*Arabic.420*/   {   420,   ST4  },
        /*Hebrew.424*/   {   424,   ST4  },

        /*Hebrew.856*/   {   856,   ST5  },
        /*Hebrew.862*/   {   862,   ST4  },
        /*Arabic.864*/   {   864,   ST5  },
        /*Hebrew.867*/   {   867,   ST4  },
        /*Hebrew.916*/   {   916,   ST5  },
        /*Arabic.1046*/  {  1046,   ST5  },
        /*Arabic.1089*/  {  1089,   ST5  },
        /*Hebrew.1255*/  {  1255,   ST5  },
        /*Arabic.1256*/  {  1256,   ST5  },
        /*Hebrew.916*/   {  5012,   ST5  },
        /*Hebrew.1255*/  {  5351,   ST5  },
        /*Arabic.1256*/  {  5352,   ST5  },
        /*Arabic.420*/   {  8612,   ST5  },
        /*Hebrew.424*/   {  8616,   ST10 },
        /*Arabic.1046*/  {  9238,   ST5  },
        /*Arabic.420*/   { 12708,   ST7  },
        /*Hebrew.424*/   { 12712,   ST10 },

        /*Unicode.10646*/{ 13488,   ST10 },
        /*Arabic.420*/   { 16804,   ST4  },
        /*Arabic.864*/   { 17248,   ST5  },
        /*Unicode.10646*/{ 61952,   ST10 },

        /*Hebrew.856*/   { 62208,   ST4  },
        /*Hebrew.862*/   { 62209,   ST10 },
        /*Hebrew.916*/   { 62210,   ST4  },
        /*Hebrew.424*/   { 62211,   ST5  },
        /*Hebrew.867*/   { 62212,   ST10 },
        /*Hebrew.862*/   { 62213,   ST5  },
        /*Hebrew.867*/   { 62214,   ST5  },
        /*Hebrew.1255*/  { 62215,   ST4  },
        /*Hebrew.867*/   { 62216,   ST6  },
        /*Hebrew.867*/   { 62217,   ST8  },
        /*Arabic.864*/   { 62218,   ST4  },
        /*Hebrew.867*/   { 62219,   ST11 },
        /*Hebrew.856*/   { 62220,   ST6  },
        /*Hebrew.862*/   { 62221,   ST6  },
        /*Hebrew.916*/   { 62222,   ST6  },
        /*Hebrew.1255*/  { 62223,   ST6  },
        /*Arabic.420*/   { 62224,   ST6  },
        /*Arabic.864*/   { 62225,   ST6  },
        /*Arabic.1046*/  { 62226,   ST6  },
        /*Arabic.1089*/  { 62227,   ST6  },
        /*Arabic.1256*/  { 62228,   ST6  },
        /*Hebrew.424*/   { 62229,   ST8  },
        /*Hebrew.856*/   { 62230,   ST8  },
        /*Hebrew.862*/   { 62231,   ST8  },
        /*Hebrew.916*/   { 62232,   ST8  },
        /*Arabic.420*/   { 62233,   ST8  },
        /*Arabic.420*/   { 62234,   ST9  },
        /*Hebrew.424*/   { 62235,   ST6  },
        /*Hebrew.856*/   { 62236,   ST10 },
        /*Hebrew.1255*/  { 62237,   ST8  },
        /*Hebrew.916*/   { 62238,   ST10 },
        /*Hebrew.1255*/  { 62239,   ST10 },
        /*Hebrew.424*/   { 62240,   ST11 },
        /*Hebrew.856*/   { 62241,   ST11 },
        /*Hebrew.862*/   { 62242,   ST11 },
        /*Hebrew.916*/   { 62243,   ST11 },
        /*Hebrew.1255*/  { 62244,   ST11 },
        /*Hebrew.424*/   { 62245,   ST10 }
        };

    private final static int ccsidMax = ccsidTable.length - 1;

    final static BidiFlagSet[] flagSet = new BidiFlagSet[12];
    final static BidiFlagSet normalFlagSet = initFlagSet((char)0);

    int as400Ccsid;                     /* AS/400 CCSID */
    int as400Type;                      /* string type of AS/400 data */
    int javaType;                       /* string type of Java data */
    BidiTransform bdxJ2A = new BidiTransform(); /* from Java to AS/400 */
    BidiTransform bdxA2J = new BidiTransform(); /* from AS/400 to Java */


/*======================  Constructors  ==============================*/

/**
 *  Constructs an AS400BidiTransform object assuming that
 *  the AS/400 Bidi text conforms to a given CCSID.
 *  Typically this will be the CCSID of the system.
 *  <p>
 *  The given CCSID has a default string type which defines a set of Bidi
 *  flags.  The orientation implied by this string type is applied to
 *  both the AS/400 data layout and the Java data layout.
 *
 *  @param  as400Ccsid  The CCSID of the AS/400 data.
 */
    public AS400BidiTransform(int as400Ccsid)
    {
        bdxA2J.flags = new BidiFlagSet(normalFlagSet);
        setAS400Ccsid(as400Ccsid);
    }

/*======================  Public  Methods  ===========================*/

/**
 *  Indicates if a given CCSID may apply to Bidi data.  This is the case for
 *  Arabic and Hebrew CCSIDs, and for Unicode (which can encode anything).
 *  <p>
 *  If a CCSID is not Bidi, there is no need to perform layout
 *  transformations when converting AS/400 data to Java data and vice-versa.
 *
 *  @param  ccsid       the CCSID to check.
 *
 *  @return true if the given CCSID may apply to Bidi data, false otherwse.
 */
    public static boolean isBidiCcsid(int ccsid)
    {
//@P0C - this whole thing
// 110 ms        
//        return (getStringType(ccsid) > 0);

        
// 140 ms        
/*
        for (int i=0; i<ccsidTable.length; ++i)
        {
          if (ccsid == ccsidTable[i][0]) return true;
        }
        return false;
*/        
        
// 70 ms

        switch(ccsid)
        {
          case 420:
          case 424:
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
           return true;
          default:
            return false;
        }

    
// 171 ms using ConvTable improvement, otherwise 580 ms
/*    ConvTable ct;
    try 
    {
      ct = ConvTable.getTable(ccsid, null);
      return ct instanceof ConvTableBidiMap;        
    }
    catch(java.io.UnsupportedEncodingException uee)
    {
      return false;
    }
*/        
    }


/*====================================================================*/

/**
 *  Sets the CCSID.  
 *  <p>
 *  The given CCSID has a default string type which defines a set of Bidi
 *  flags.  The orientation implied by this string type is applied to
 *  both the AS/400 data layout and the Java data layout.
 *
 *  @param  as400Ccsid  The CCSID of the AS/400 data.
 */
    public void setAS400Ccsid(int as400Ccsid)
    {
        this.as400Ccsid = as400Ccsid;
        setAS400StringType(getStringType((char)as400Ccsid));
    }

/*====================================================================*/

/**
 *  Returns the current CCSID of AS/400 data.
 *
 *  @return The CCSID for the AS/400 data.
 */
    public int getAS400Ccsid()
    {
        return as400Ccsid;
    }

/*====================================================================*/

/**
 *  Set the explicit string type for AS/400 data.
 *  Each CCSID has a default CDRA string type, which defines a set of Bidi
 *  flags.  This method may be used to specify Bidi flags different from
 *  those implied by the CCSID.
 *  <p>
 *  The orientation implied by the new given string type is applied to
 *  both the AS/400 data layout and the Java data layout.  The new string
 *  type is applied to the Java data layout by calling 
 *  setJavaStringType(as400Type).
 *
 *  @param  as400Type   The string type to apply to AS/400 data.  The parameter
 *                      string type should always be one of the constants
 *                      defined in BidiStringType.
 *
 *  @see com.ibm.as400.access.BidiStringType
 */
    public void setAS400StringType(int as400Type)
    {
        if (as400Type != BidiStringType.DEFAULT && (as400Type < BidiStringType.ST4 || as400Type > BidiStringType.ST11))
        {  
           Trace.log(Trace.ERROR, "Attempting to set the as400 string type to an invalid Bidi sting type.");
           throw new ExtendedIllegalArgumentException("as400Type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
           

        this.as400Type = as400Type;
        if (flagSet[as400Type] == null)
            flagSet[as400Type] = initFlagSet((char)as400Type);
        bdxJ2A.flags = flagSet[as400Type];
        setJavaStringType(as400Type);
    }

/*====================================================================*/

/**
 *  Returns the current string type of AS/400 data.
 *
 *  @return The string type of the AS/400 data.
 */
    public int getAS400StringType()
    {
        return as400Type;
    }

/*====================================================================*/

/**
 *  Set the explicit string type for Java data.
 *  The parameter string type should always be one of the following 
 *  defined in BidiStringType:   ST5 (LTR), ST6 (RTL),
 *  ST10 (Contextual LTR), or ST11 (Contextual RTL).
 *  In fact, only the orientation of the given string type is used to
 *  modify the Bidi flags to apply to the Java data.  The other Bidi flags
 *  of the Java data always conform to the Unicode standard.
 *
 *  @see com.ibm.as400.access.BidiStringType
 *
 *  @param  javaType    The string type to apply to Java data.
 */
    public void setJavaStringType(int javaType)
    {
        if (javaType != BidiStringType.DEFAULT && (javaType < BidiStringType.ST4 || javaType > BidiStringType.ST11))
        {
           Trace.log(Trace.ERROR, "Attempting to set the java string type to an invalid Bidi sting type.");
           throw new ExtendedIllegalArgumentException("javaType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (flagSet[javaType] == null)
            flagSet[javaType] = initFlagSet((char)javaType);
        BidiFlag orient = flagSet[javaType].getOrientation();
        // Copy orientation from javaType to bdxA2J.flags
        bdxA2J.flags.setOneFlag(orient);
        if (orient == BidiFlag.ORIENTATION_LTR)
            this.javaType = 5;
        else if (orient == BidiFlag.ORIENTATION_RTL)
            this.javaType = 6;
        else if (orient == BidiFlag.ORIENTATION_CONTEXT_LTR)
            this.javaType = 10;
        else if (orient == BidiFlag.ORIENTATION_CONTEXT_RTL)
            this.javaType = 11;
        else  this.javaType = 5;        /* must never happen */
    }

/*====================================================================*/

/**
 *  Returns the current string type of Java data.
 *
 *  @return The string type of the Java data.
 */
    public int getJavaStringType()
    {
        return javaType;
    }

/*====================================================================*/

/**
 *  Convert data from the AS/400 layout to the Java layout.
 *
 *  @param  as400Text   The AS/400 string to convert.
 *
 *  @return The same text in standard Java Bidi layout.
 */
    public String toJavaLayout(String as400Text)
    {
        BidiText src = new BidiText(bdxJ2A.flags, as400Text);
        return src.transform(bdxA2J).toString();
    }

/*====================================================================*/

/**
 *  Convert data from the Java layout to the AS/400 layout.
 *
 *  @param  javaText    The Java string to convert.
 *
 *  @return The same text in AS/400 Bidi layout.
 */
    public String toAS400Layout(String javaText)
    {
        BidiText src = new BidiText(bdxA2J.flags, javaText);
        return src.transform(bdxJ2A).toString();
    }



    public static int getStringType(int ccsid) //@P0C
    /* Return default string type for parm ccsid based on ccsidTable */
    {
        int low, high, mid;

        low = 0;
        high = ccsidMax;

        while (low < high)
        {
            mid = (low + high) / 2;
            if (ccsid < ccsidTable[mid][0])
                high = mid - 1;
            else if (ccsid > ccsidTable[mid][0] )
                low = mid + 1;
            else
                return ccsidTable[mid][1];
        }
        if ((low == high) && (ccsid == ccsidTable[low][0]))
            return ccsidTable[low][1];
        return 0;
    }

/*----------------------  Private Methods  ---------------------------*/

    static BidiFlagSet initFlagSet(char stringType)
    /* Return BidiFlagSet according to String Type */
    {
        switch (stringType)
        {
        case ST4:
            return  new BidiFlagSet(BidiFlag.TYPE_VISUAL,
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
