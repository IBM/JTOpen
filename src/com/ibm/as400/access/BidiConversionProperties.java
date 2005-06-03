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
    // Only remove marks on the J2A transform.
    boolean removeMarksOnImplicitToVisual_ = false;
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

    BidiConversionProperties(int bidiStringType, BidiTransform transform, boolean removeMarkersOnImplicitToVisual)
    {
        // String type.
        setBidiStringType(bidiStringType);

        // Copy options.
        copyOptionsTo(transform, transformOptions_);

        // Set remove markers special case option.
        removeMarksOnImplicitToVisual_ = removeMarkersOnImplicitToVisual;
        // The remove markers should not be true in general, only in J2A case.
        if (removeMarkersOnImplicitToVisual)
        {
            transformOptions_.removeMarkers = false;
        }

        // Copy output results.
        transformOptions_.dstToSrcMap = transform.dstToSrcMap;
        transformOptions_.srcToDstMap = transform.srcToDstMap;
        transformOptions_.propertyMap = transform.propertyMap;
        transformOptions_.inpCount = transform.inpCount;
        transformOptions_.outCount = transform.outCount;
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
     Remove the directional marks only when transforming from logical to visual.
     @param  removeMarks  true to remove the directional marks only when transforming from logical to visual; false otherwise.
     **/
    public void setBidiRemoveMarksOnImplicitToVisual(boolean removeMarks)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi remove the directional marks only when transforming from logical to visual property:", removeMarks);
        removeMarksOnImplicitToVisual_ = removeMarks;
    }

    /**
     Indicates the value of the bidi remove the directional marks only when transforming from logical to visual property.
     @return  true if the remove the directional marks only when transforming from logical to visual property is enabled; false otherwise.
     **/
    public boolean isBidiRemoveMarksOnImplicitToVisual()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if remove the directional marks only when transforming from logical to visual property is set:", removeMarksOnImplicitToVisual_);
        return removeMarksOnImplicitToVisual_;
    }

    BidiTransform getTransformOptions()
    {
        return transformOptions_;
    }

    // Internal method to copy all the options from one object to another.
    void copyOptionsTo(BidiTransform destination)
    {
        copyOptionsTo(transformOptions_, destination);
    }

    // Internal method to copy all the options from one object to another.
    static void copyOptionsTo(BidiTransform sourceTransform, BidiTransform destTransform)
    {
        destTransform.impToImp = sourceTransform.impToImp;
        destTransform.roundTrip = sourceTransform.roundTrip;
        destTransform.winCompatible = sourceTransform.winCompatible;
        destTransform.insertMarkers = sourceTransform.insertMarkers;
        destTransform.removeMarkers = sourceTransform.removeMarkers;
        destTransform.options = sourceTransform.options;
        destTransform.wordBreak = sourceTransform.wordBreak;
        destTransform.destinationRequired = sourceTransform.destinationRequired;
        destTransform.srcToDstMapRequired = sourceTransform.srcToDstMapRequired;
        destTransform.dstToSrcMapRequired = sourceTransform.dstToSrcMapRequired;
        destTransform.propertyMapRequired = sourceTransform.propertyMapRequired;
        destTransform.continuation = sourceTransform.continuation;
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
     Sets the bidi window compatibility  property.  This property is false by default.
     <p>If this option is true, the reordering algorithm is modified to perform more closely like Windows.  In particular, logical string "12ABC" in LTR orientation (where ABC represent Arabic or Hebrew letters) is reordered as "CBA12" instead of "12CBA".  Also, logical string "abc 123 45" (where all digits represent Hindi numbers) is reordered as "abc 123 45" instead of "abc 45 123".
     @param  bidiWindowCompatibility  true to use the window compatibility property; false otherwise.
     **/
    public void setBidiWindowCompatibility(boolean bidiWindowCompatibility)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi window compatibility:", bidiWindowCompatibility);
        transformOptions_.winCompatible = bidiWindowCompatibility;
    }

    /**
     Indicates the value of the bidi window compatibility property.
     @return  true if the bidi window compatibility  property is enabled; false otherwise.
     **/
    public boolean isBidiWindowCompatibility()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi window compatibility:", transformOptions_.winCompatible);
        return transformOptions_.winCompatible;
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
     Sets the bidi consider white space to always follow base orientation property.  This property is false by default.
     @param  wordBreak  true to consider white space to always follow base orientation; false otherwise.
     **/
    public void setBidiWordBreak(boolean wordBreak)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi word break:", wordBreak);
        transformOptions_.wordBreak = wordBreak;
    }

    /**
     Indicates the value of the bidi consider white space to always follow base orientation property.
     @return  true if the bidi consider white space to always follow base orientation property is enabled; false otherwise.
     **/
    public boolean isBidiWordBreak()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi consider white space to always follow base orientation:", transformOptions_.wordBreak);
        return transformOptions_.wordBreak;
    }

    /**
     Sets the bidi destination required property.  This property is true by default.
     @param  destinationRequired  true if the destination is required; false otherwise.
     **/
    public void setBidiDestinationRequired(boolean destinationRequired)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi destination required:", destinationRequired);
        transformOptions_.destinationRequired = destinationRequired;
    }

    /**
     Indicates the value of the bidi destination required property.
     @return  true if the bidi destination required property is enabled; false otherwise.
     **/
    public boolean isBidiDestinationRequired()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi destination required:", transformOptions_.destinationRequired);
        return transformOptions_.destinationRequired;
    }

    /**
     Sets the create a source to destination mapping property.  This property is false by default.
     @param  srcToDstMapRequired  true to use the bidi create a source to destination mapping property; false otherwise.
     **/
    public void setBidiCreateSourceToDestinationMapping(boolean srcToDstMapRequired)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi create a source to destination mapping property:", srcToDstMapRequired);
        transformOptions_.srcToDstMapRequired = srcToDstMapRequired;
    }

    /**
     Indicates the value of the bidi create a source to destination mapping property.
     @return  true if the bidi create a source to destination mapping property is enabled; false otherwise.
     **/
    public boolean isBidiCreateSourceToDestinationMapping()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if create a source to destination mapping property is set:", transformOptions_.srcToDstMapRequired);
        return transformOptions_.srcToDstMapRequired;
    }

    /**
     Sets the create a destination to source mapping property.  This property is false by default.
     @param  dstToSrcMapRequired  true to use the create a destination to source mapping property; false otherwise.
     **/
    public void setBidiCreateDestinationToSourceMapping(boolean dstToSrcMapRequired)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi create a destination to source mapping property:", dstToSrcMapRequired);
        transformOptions_.dstToSrcMapRequired = dstToSrcMapRequired;
    }

    /**
     Indicates the value of the bidi create a destination to source mapping property.
     @return  true if the bidi create a destination to source mapping property is enabled; false otherwise.
     **/
    public boolean isBidiCreateDestinationToSourceMapping()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if create a destination to source mapping property is set:", transformOptions_.dstToSrcMapRequired);
        return transformOptions_.dstToSrcMapRequired;
    }

    /**
     Sets the create a property map property.  This property is false by default.
     @param  propertyMapRequired  true to use the create a property map property; false otherwise.
     **/
    public void setBidiCreatePropertyMap(boolean propertyMapRequired)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi create property map property:", propertyMapRequired);
        transformOptions_.propertyMapRequired = propertyMapRequired;
    }

    /**
     Indicates the value of the bidi create a property map property.
     @return  true if the bidi create a property map property is enabled; false otherwise.
     **/
    public boolean isBidiCreatePropertyMap()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi create property map property is set:", transformOptions_.propertyMapRequired);
        return transformOptions_.propertyMapRequired;
    }

    /**
     Output value: number of characters processed in the source data by the last transform.
     **/
    public int getInputCount()
    {
        return transformOptions_.inpCount;
    }

    /**
     Output value: number of characters written in the destination data by the last transform
     **/
    public int getOutputCount()
    {
        return transformOptions_.outCount;
    }

    /**
     Output value: source-to-destination map from the last transform with srcToDstMapRequired specified; if this option was not specified, the content of srcToDstMap should be ignored.
     <p>If when starting a transformation this field refers to a large enough array of integers, this array will be re-used to put the new map.  Otherwise a new array will be created.
     <p>This map has a number for each character processed in the source data by the last transform.  This number is the index of where this character is moved in the character array of the destination BidiText.  If the removeMarkers option was specified and LRM or RLM markers have been removed from the destination text, the corresponding elements of srcToDstMap will contain -1.
     <p>Note that the allocated array may have more elements than the number of characters processed in the source BidiText.  In that case, the extra elements should be ignored.  The number of relevant elements can be found from getInputCount()..
     **/
    public int[] getSourceToDestinationMap()
    {
        return transformOptions_.srcToDstMap;
    }

    /**
     Output value: destination-to-source map from the last transform with dstToSrcMapRequired specified; if this option was not specified, the content of dstToSrcMap should be ignored.
     <p>If when starting a transformation this field refers to a large enough array of integers, this array will be re-used to put the new map.  Otherwise a new array will be created.
     <p>This map has a number for each character in the "interesting" data of the destination BidiText.  This number is the index of the source character from which the destination character originates.  This index is relative to the beginning of the "interesting" data.  If the offset of the source BidiText is not zero, index 0 does not indicate the first character of the data array, but the character at position "offset".  If the insertMarkers option was specified and LRM or RLM markers have been added, the corresponding elements of dstToSrcMap will contain -1.
     <p>Note that the allocated array may have more elements than the number of characters in the "interesting" part of the destination BidiText.  In that case, the extra elements should be ignored.  The number of relevant elements can be found from getOutputCount().
     **/
    public int[] getDestinationToSourceMap()
    {
        return transformOptions_.dstToSrcMap;
    }

    /**
     Output value: property map from the last transform with propertyMapRequired specified; if this option was not specified, the content of propertyMap should be ignored.
     <p>If when starting a transformation this field refers to a large enough array of bytes, this array will be re-used to put the new map.  Otherwise a new array will be created.
     <p>This map has a byte for each character processed in the source data by the last transform.  The 6 lower bits of each property element is the Bidi level of the corresponding input character.  The highest bit is a new-cell indicator for composed character environments: a value of 0 indicates a zero-length composing character element, and a value of 1 indicates an element that begins a new cell.
     <p>Note: the content of this map has no simple interpretation if the bidi implicit reordering property is true.
     <p>Note also that the allocated array may have more elements than the number of characters processed in the source BidiText.  In that case, the extra elements should be ignored.  The number of relevant elements can be found from getInputCount().
     **/
    public byte[] getPropertyMap()
    {
        return transformOptions_.propertyMap;
    }
}
