///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400DataType.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 *  The AS400DataType interface provides an interface for conversions between Java objects and byte arrays representing AS/400 data types.
 **/
public interface AS400DataType extends Cloneable, Serializable
{



    static final long serialVersionUID = 4L;



    /**
     * Creates a new AS400DataType object that is identical to the current instance.
     * @return The new object.
     **/
    public abstract Object clone();  // Implementers must provide a clone() method that is public and does not throw CloneNotSupported Exception

    /**
     * Returns the byte length of the data type.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public abstract int getByteLength();

    /**
     * Returns a Java object representing the default value of the data type.
     * @return The Object of the corresponding data type.
     **/
    public abstract Object getDefaultValue();

    /**
     * Converts the specified Java object to AS/400 format.
     * @param javaValue The object corresponding to the data type.  It must be an instance of the correct type.
     * @return The AS/400 representation of the data type.
     **/
    public abstract byte[] toBytes(Object javaValue);

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type.  It must be an instance of the correct type.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public abstract int toBytes(Object javaValue, byte[] as400Value);

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.
     * @param javaValue The object corresponding to the data type. It must be an instance of the correct type.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public abstract int toBytes(Object javaValue, byte[] as400Value, int offset);

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The object corresponding to the data type.
     **/
    public abstract Object toObject(byte[] as400Value);

    /**
     * Converts the specified AS/400 data type to a Java object.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The object corresponding to the data type.
     **/
    public abstract Object toObject(byte[] as400Value, int offset);
}
