///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400Array.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400Array class provides a composite data type representing an array of AS400DataType objects.
 **/
public class AS400Array implements AS400DataType
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    static final long serialVersionUID = 4L;



    private AS400DataType arrayType = null;
    private int arraySize = -1;
    private boolean allowChanges = true;  // For beans: allow changes after null constructor until conversion method called

    /**
     * Constructs an AS400Array object.  The setNumberOfElements() and setType() methods must be called before a call to any conversion methods or the getByteLength() on this object.
     **/
    public AS400Array()
    {
    }

    /**
     * Constructs an AS400Array object.
     * @param type The type of the array.
     * @param size The number of elements in the array.  It must be greater than or equal to zero.
     **/
    public AS400Array(AS400DataType type, int size)
    {
     if (size < 0)
     {
         throw new ExtendedIllegalArgumentException("size (" + String.valueOf(size) + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }
     this.allowChanges = false;
     // only store clones
     this.arrayType = (AS400DataType)type.clone(); // let this line throw NullPointerException
     this.arraySize = size;
    }

    /**
     * Creates a new AS400Array object that is identical to the current instance.
     * @return The new object.
     **/
    public Object clone()
    {
     try
     {
         AS400Array nObj = (AS400Array)super.clone();  // Object.clone does not throw exception
         nObj.arrayType = (this.arrayType == null) ?
           null :
           (AS400DataType)this.arrayType.clone(); // Data Types do not throw exception
         return nObj;
     }
     catch (CloneNotSupportedException e)
     {
         Trace.log(Trace.ERROR, "Unexpected cloning error", e);
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
    }

    /**
     * Returns the byte length of the data type.  The type and number of elements in this array must be set before calling this method.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int getByteLength()
    {
     AS400DataType element = this.arrayType;
     int size = this.arraySize;

     // Check for valid state
     if (element == null)
     {
         throw new ExtendedIllegalStateException("Type", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     if (size == -1)
     {
         throw new ExtendedIllegalStateException("NumberOfElements", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges = false;  // Check before setting so don't have unfixable object

     // Length is number of elements * size of the element
     return this.arraySize*this.arrayType.getByteLength();
    }

    /**
     * Returns a Java object that represents the default value of the data type.
     * @return The zero-length Object array.
     **/
    public Object getDefaultValue()
    {
     int size = ( this.arraySize == -1 ? 0 : this.arraySize );
     Object[] returnArray = new Object[size];
     Object elementDefaultValue = ( this.arrayType == null ? null : this.arrayType.getDefaultValue() );

     for (int i = 0; i < size; ++i)
     {
         returnArray[i] = elementDefaultValue;
     }
     return returnArray;
    }

    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_ARRAY TYPE_ARRAY}.
     * @return Returns AS400DataType.TYPE_ARRAY.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_ARRAY;
    }

    /**
     * Returns the number of elements in the data type.
     * @return The number of elements in the array data type.  If the number of elements has not been set, negative one (-1) is returned.
     **/
    public int getNumberOfElements()
    {
     return this.arraySize;
    }

    /**
     * Returns the type of the array.
     * @return The type of this array data type.  If the array type has not been set, null is returned.
     **/
    public AS400DataType getType()
    {
     return (this.arrayType == null) ?
       null :
       (AS400DataType)this.arrayType.clone();  // only give out clones
    }

    /**
     * Sets the number of elements in the data type.  This method must be called after a call to the null constructor and before a call to any of the conversion methods or getByteLength().
     * @param size The number of elements in the array.  It must be greater than or equal to zero.
     **/
    public void setNumberOfElements(int size)
    {
     if (!this.allowChanges)
     {
         throw new ExtendedIllegalStateException("NumberOfElements", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }
     if (size < 0)
     {
         throw new ExtendedIllegalArgumentException("size (" + String.valueOf(size) + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }
     this.arraySize = size;
    }

    /**
     * Sets the type of the array.  This method must be called after a call to the null constructor and before a call to any of the conversion methods or getByteLength().
     * @param type The type of the array.
     **/
    public void setType(AS400DataType type)
    {
     if (!this.allowChanges)
     {
         throw new ExtendedIllegalStateException("Type", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }
     // only store clones
     this.arrayType = (AS400DataType)type.clone(); // Let this line throw NullPointerException
    }

    /**
     * Converts the specified Java object to AS/400 format.  The type and number of elements in this array must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array; the array must contain the correct number of elements, and each element must be of the correct type.
     * @return The AS/400 representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[this.getByteLength()];  // let getByteLength check state
     this.toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.  The type and number of elements in this array must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array; the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into AS/400 format in the specified byte array.  The type and number of elements in this array must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array; the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in AS/400 format.  There must be enough space to hold the AS/400 value.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The number of bytes in the AS/400 representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     AS400DataType element = this.arrayType;
     int size = this.arraySize;

     // Check for valid state
     if (element == null)
     {
         throw new ExtendedIllegalStateException("Type", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     if (size == -1)
     {
         throw new ExtendedIllegalStateException("NumberOfElements", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges = false;  // Check before setting so don't have unfixable object

     Object[] javaArray = (Object[])javaValue;  // let this line to throw ClassCastException
     if (javaArray.length != size)  // Check for correct number of elements
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }
     // Convert array by iterating over elements, allow elements to do own validity checking
     for (int i = 0; i < size; ++i)
     {
         offset += element.toBytes(javaArray[i], as400Value, offset);
     }
     return element.getByteLength()*size;
    }

    /**
     * Converts the specified AS/400 data type to a Java object.  The type and number of elements in this array must be set before calling this method.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @return The array of Object.  Each element of this array is an Object corresponding to the type of this AS400Array object.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified AS/400 data type to a Java object.  The type and number of elements in this array must be set before calling this method.
     * @param as400Value The array containing the data type in AS/400 format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the AS/400 value.  It must be greater than or equal to zero.
     * @return The array of Object.  Each element of this array is an Object corresponding to the type of this AS400Array object.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     AS400DataType element = this.arrayType;
     int size = this.arraySize;

     // Check for valid state
     if (element == null)
     {
         throw new ExtendedIllegalStateException("Type", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     if (size == -1)
     {
         throw new ExtendedIllegalStateException("NumberOfElements", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges = false;  // Check before setting so don't have unfixable object

     int elementSize = element.getByteLength();
     Object[] returnArray = new Object[size];
     // Convert array by iterating over elements, allow elements to do own validity checking
     for (int i = 0; i < size; ++i)
     {
         returnArray[i] = element.toObject(as400Value, offset);
         offset += elementSize;
     }
     return returnArray;
    }
}
