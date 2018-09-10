///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QualifiedValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.IllegalPathNameException;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;
import java.io.Serializable;
import java.util.Date;



/**
The QualifiedValueMap class maps between a logical value
and a variety of qualified name formats for a physical value.
**/
class QualifiedValueMap 
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Date formats.
    public static final int FORMAT_20       = 20;       // OOOOOOOOOOLLLLLLLLLL
    public static final int FORMAT_CL       = 13;       // LLLLLLLLLL/OOOOOOOOOO



    // Private data.
    private int format_                             = -1;
    private String type_                            = null;




/**
Constructs a QualifiedValueMap object.

@param format   The format.
@param type     The object type.
**/
    public QualifiedValueMap(int format, String type)
    {
        if ((format != FORMAT_20) && (format != FORMAT_CL))
            throw new ExtendedIllegalArgumentException("format", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (type == null)
            throw new NullPointerException("type");

        format_ = format;
        type_ = type;
    }



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue, AS400 system)
    {
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");        
        if (!(logicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)logicalValue;
        if (asString.length() == 0)
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        QSYSObjectPathName asPath;
        StringBuffer buffer = new StringBuffer();

        switch(format_) {

        case FORMAT_20:
            // If a special value has been specified...
            if (asString.length() <= 10)
                buffer.append(normalize(asString, 20));
            else {
                try {
                    asPath = new QSYSObjectPathName(asString);
                    buffer.append(normalize(asPath.getObjectName(), 10));
                    buffer.append(normalize(asPath.getLibraryName(), 10));
                }
                catch(IllegalPathNameException e) {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, "Bad path name", e);
                    throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
            break;

        case FORMAT_CL:
            // If a special value has been specified...
            if (asString.length() <= 10)
                buffer.append(asString);
            else {
                try {
                    asPath = new QSYSObjectPathName(asString);
                    buffer.append(asPath.getLibraryName());
                    buffer.append('/');
                    buffer.append(asPath.getObjectName());                }
                catch(IllegalPathNameException e) {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, "Bad path name", e);
                    throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
            break;

        // If the format specified is bad...
        default:
            throw new ExtendedIllegalStateException("format", ExtendedIllegalStateException.UNKNOWN);
        }

        return buffer.toString();
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue, AS400 system)
    {
        // Validate the physical value.
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)physicalValue;

        switch(format_) {

        case FORMAT_20:
            // If a special value has been specified...
            if (asString.length() <= 10)            
                return asString;
            else
                return QSYSObjectPathName.toPath(asString.substring(10).trim(),
                                                 asString.substring(0,10).trim(),
                                                 type_);

        case FORMAT_CL:
            return QSYSObjectPathName.toPath(asString.substring(11,21).trim(),
                                             asString.substring(0,10).trim(),
                                             type_);

        // If the format specified is bad...
        default:
            throw new ExtendedIllegalStateException("format", ExtendedIllegalStateException.UNKNOWN);
        }

    }



/**
Normalizes a String to a fixed length.  This will truncate or pad
with spaces as needed.

@param source   The source String.
@param length   The fixed length.
@return         The normalized String.
**/
    private static String normalize(String source, int length)
    {
        int sourceLength = source.length();
        if (sourceLength < length) {
            StringBuffer buffer = new StringBuffer(source);
            for(int i = sourceLength; i < length; ++i)
                buffer.append(' ');
            return buffer.toString();
        }
        else if (sourceLength > length)
            return source.substring(0, length);
        else
            return source;
    }



}
