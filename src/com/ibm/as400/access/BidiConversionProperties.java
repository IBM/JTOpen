///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiConversionOptions.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004 International Business Machines Corporation and
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
    private static final String copyright = "Copyright (C) 2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    // String type.
    private int bidiStringType_ = BidiStringType.DEFAULT;
    // Implicit LTR-RTL reordering property.
    private boolean bidiImplicitReordering_ = true;
    // Numeric ordering round trip property.
    private boolean bidiNumericOrderingRoundTrip_ = false;

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

    // Internal method to copy all the options from one object to another.
    void copyValues(BidiConversionProperties properties)
    {
        bidiStringType_ = properties.bidiStringType_;
        bidiImplicitReordering_ = properties.bidiImplicitReordering_;
        bidiNumericOrderingRoundTrip_ = properties.bidiNumericOrderingRoundTrip_;
    }

    /**
     Sets the bidi string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.  This option is set to BidiStringType.DEFAULT by default.
     @param  bidiStringType  The bidi string type.
     **/
    public void setBidiStringType(int bidiStringType)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi string type:", bidiStringType);
        if (bidiStringType != BidiStringType.DEFAULT && (bidiStringType < BidiStringType.ST4 || bidiStringType > BidiStringType.ST11))
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
     Sets the bidi implicit LTR-RTL reordering property.  This property is true by default.
     @param  bidiImplicitReordering  true to use the bidi implicit reordering; false otherwise.
     **/
    public void setBidiImplicitReordering(boolean bidiImplicitReordering)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi implicit LTR-RTL reordering:", bidiImplicitReordering);
        bidiImplicitReordering_ = bidiImplicitReordering;
    }

    /**
     Indicates the value of the bidi implicit LTR-RTL reordering property.
     @return  true if the bidi implicit LTR-RTL reordering property is enabled; false otherwise.
     **/
    public boolean isBidiImplicitReordering()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi implicit LTR-RTL reordering:", bidiImplicitReordering_);
        return bidiImplicitReordering_;
    }

    /**
     Sets the bidi numeric ordering round trip property.  This property is false by default.
     @param  bidiImplicitReordering  true to use the bidi numeric ordering round trip property; false otherwise.
     **/
    public void setBidiNumericOrderingRoundTrip(boolean bidiNumericOrderingRoundTrip)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting bidi numeric ordering round trip:", bidiNumericOrderingRoundTrip);
        bidiNumericOrderingRoundTrip_ = bidiNumericOrderingRoundTrip;
    }

    /**
     Indicates the value of the bidi numeric ordering round trip property.
     @return  true if the bidi numeric ordering round trip property is enabled; false otherwise.
     **/
    public boolean isBidiNumericOrderingRoundTrip()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if bidi numeric ordering round trip:", bidiNumericOrderingRoundTrip_);
        return bidiNumericOrderingRoundTrip_;
    }
}
