///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiConversionOptions.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 The BidiConversionProperties class provides a set of properties that can be used to control the conversion of character set data.
 **/
public class BidiConversionProperties implements Serializable
{
    static final long serialVersionUID = 4L;

    // String type.
    private int bidiStringType_ = BidiStringType.DEFAULT;
    // All of the options to affect BIDI transforms.
    BidiTransform transformOptions_ = new BidiTransform();

    /**
     Constructs a BidiConversionProperties object.
     **/
    public BidiConversionProperties()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing BidiConversionProperties.");
    }

    BidiConversionProperties(int bidiStringType)
    {
        setBidiStringType(bidiStringType);
    }

    BidiConversionProperties(int bidiStringType, BidiTransform transform)
    {
        setBidiStringType(bidiStringType);
        copyOptionsTo(transform, transformOptions_);
    }

    // Internal method to copy all the options from one object to another.
    void copyValues(BidiConversionProperties properties)
    {
        bidiStringType_ = properties.bidiStringType_;
        transformOptions_ = properties.transformOptions_;
    }

    // Internal method to copy all the options from one object to another.
    void copyOptionsTo(BidiTransform destination)
    {
        copyOptionsTo(transformOptions_, destination);
    }

    // Internal method to copy all the options from one object to another.
    static void copyOptionsTo(BidiTransform sourceTransform, BidiTransform destTransform)
    {
        destTransform.continuation = sourceTransform.continuation;
        destTransform.destinationRequired = sourceTransform.destinationRequired;
        destTransform.dstToSrcMapRequired = sourceTransform.dstToSrcMapRequired;
        destTransform.impToImp = sourceTransform.impToImp;
        destTransform.insertMarkers = sourceTransform.insertMarkers;
        destTransform.options = sourceTransform.options;
        destTransform.propertyMapRequired = sourceTransform.propertyMapRequired;
        destTransform.removeMarkers = sourceTransform.removeMarkers;
        destTransform.roundTrip = sourceTransform.roundTrip;
        destTransform.srcToDstMapRequired = sourceTransform.srcToDstMapRequired;
        destTransform.winCompatible = sourceTransform.winCompatible;
        destTransform.wordBreak = sourceTransform.wordBreak;
    }

    /**
     Sets the bidi string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.  This option is set to BidiStringType.DEFAULT by default.
     @param  bidiStringType  The bidi string type.
     **/
    public void setBidiStringType(int bidiStringType)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi string type:", bidiStringType);
        if (bidiStringType != BidiStringType.NONE && bidiStringType != BidiStringType.DEFAULT && (bidiStringType < BidiStringType.ST4 || bidiStringType > BidiStringType.ST11))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'bidiStringType' is not valid:", bidiStringType);
            throw new ExtendedIllegalArgumentException("bidiStringType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        bidiStringType_ = bidiStringType;
    }

    BidiTransform getTransformOptions()
    {
        return transformOptions_;
    }

    /**
     Gets the bidi string type.
     @return  The bidi string type.
     **/
    public int getBidiStringType()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting bidi string type:", bidiStringType_);
        return bidiStringType_;
    }

    /**
     Sets the bidi implicit LTR-RTL reordering property.  This property is true by default.
     @param  bidiImplicitReordering  true to use the bidi implicit reordering; false otherwise.
     **/
    public void setBidiImplicitReordering(boolean bidiImplicitReordering)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi implicit LTR-RTL reordering:", bidiImplicitReordering);
        transformOptions_.impToImp = bidiImplicitReordering;
    }

    /**
     Indicates the value of the bidi implicit LTR-RTL reordering property.
     @return  true if the bidi implicit LTR-RTL reordering property is enabled; false otherwise.
     **/
    public boolean isBidiImplicitReordering()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi implicit LTR-RTL reordering:", transformOptions_.impToImp);
        return transformOptions_.impToImp;
    }

    /**
     Sets the bidi numeric ordering round trip property.  This property is false by default.
     @param  bidiNumericOrderingRoundTrip  true to use the bidi numeric ordering round trip property; false otherwise.
     **/
    public void setBidiNumericOrderingRoundTrip(boolean bidiNumericOrderingRoundTrip)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi numeric ordering round trip:", bidiNumericOrderingRoundTrip);
        transformOptions_.roundTrip = bidiNumericOrderingRoundTrip;
    }

    /**
     Indicates the value of the bidi numeric ordering round trip property.
     @return  true if the bidi numeric ordering round trip property is enabled; false otherwise.
     **/
    public boolean isBidiNumericOrderingRoundTrip()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi numeric ordering round trip:", transformOptions_.roundTrip);
        return transformOptions_.roundTrip;
    }

    /**
     Sets the bidi insert directional marks property.  This property is false by default.  Insert directional marks when going from visual to implicit to guarantee correct roundtrip back to visual.
     @param  bidiInsertDirectionalMarks  true to use the insert directional marks property; false otherwise.
     **/
    public void setBidiInsertDirectionalMarks(boolean bidiInsertDirectionalMarks)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi insert directional marks:", bidiInsertDirectionalMarks);
        transformOptions_.insertMarkers = bidiInsertDirectionalMarks;
    }

    /**
     Indicates the value of the bidi insert directional marks property.
     @return  true if the bidi insert directional marks property is enabled; false otherwise.
     **/
    public boolean isBidiInsertDirectionalMarks()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi insert directional marks:", transformOptions_.insertMarkers);
        return transformOptions_.insertMarkers;
    }

    /**
     Sets the bidi remove directional marks property.  This property is false by default.  Remove directional marks when going from implict to visual.
     @param  bidiRemoveDirectionalMarks  true to use the remove directional marks property; false otherwise.
     **/
    public void setBidiRemoveDirectionalMarks(boolean bidiRemoveDirectionalMarks)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi remove directional marks:", bidiRemoveDirectionalMarks);
        transformOptions_.removeMarkers = bidiRemoveDirectionalMarks;
    }

    /**
     Indicates the value of the bidi remove directional marks property.
     @return  true if the bidi remove directional marks property is enabled; false otherwise.
     **/
    public boolean isBidiRemoveDirectionalMarks()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi remove directional marks:", transformOptions_.removeMarkers);
        return transformOptions_.removeMarkers;
    }

    /**
     Sets the bidi window compatibility  property.  This property is false by default.
     <p>If this option is true, the reordering algorithm is modified to perform more closely like Windows.  In particular, logical string "12ABC" in LTR orientation (where ABC represent Arabic or Hebrew letters) is reordered as "CBA12" instead of "12CBA".  Also, logical string "abc 123 45" (where all digits represent Hindi numbers) is reordered as "abc 123 45" instead of "abc 45 123".
     @param  bidiWindowCompatibility  true to use the window compatibility property; false otherwise.
     **/
    public void setBidiWindowCompatibility(boolean bidiWindowCompatibility)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi window compatibility marks:", bidiWindowCompatibility);
        transformOptions_.removeMarkers = bidiWindowCompatibility;
    }

    /**
     Indicates the value of the bidi window compatibility property.
     @return  true if the bidi window compatibility  property is enabled; false otherwise.
     **/
    public boolean isBidiWindowCompatibility()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi window compatibility marks:", transformOptions_.removeMarkers);
        return transformOptions_.removeMarkers;
    }

    /**
     Output value: source-to-destination map from the last transform with srcToDstMapRequired specified; may be null if no such request.
     <p>If when starting a transformation this field refers to a large enough array of integers, this array will be re-used to put the new map.  Otherwise a new array will be created.
     <p>This map has a number for each character in the "interesting" data of the source BidiText.  This number is the index of where this character is moved in the character array of the destination BidiText.
     **/
    public int[] getSourceToDestinationMap()
    {
        return transformOptions_.srcToDstMap;
    }

    /**
     Sets the create a source to destination mapping property.  This property is false by default.
     @param  srcToDstMapRequired  true to use the window compatibility marks property; false otherwise.
     **/
    public void setBidiCreateSourceToDestinationMapping(boolean srcToDstMapRequired)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi window compatibility marks:", srcToDstMapRequired);
        transformOptions_.srcToDstMapRequired = srcToDstMapRequired;
    }

    /**
     Indicates the value of the bidi create a source to destination mapping property.
     @return  true if the bidi create a source to destination mapping  property is enabled; false otherwise.
     **/
    public boolean isBidiCreateSourceToDestinationMapping()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi window compatibility marks:", transformOptions_.srcToDstMapRequired);
        return transformOptions_.srcToDstMapRequired;
    }
}
