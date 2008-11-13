///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Structure.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The AS400Structure class provides a composite data type representing a structure of AS400DataType objects.
 **/
public class AS400Structure implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private AS400DataType[] elements_ = null;
    private static Object[] defaultValue_ = null;
    private static final Object defaultValueLock_ = new Object();
    private boolean allowChanges_ = true;  // For beans: allow changes after null constructor until conversion method called

    /**
     * Constructs an AS400Structure object.  The setMembers() method must be called before any conversion methods or getByteLength()  on this object.
     **/
    public AS400Structure()
    {
    }

    /**
     * Constructs an AS400Structure object.
     * @param members The data types of the members of the structure.
     **/
    public AS400Structure(AS400DataType[] members)
    {
     AS400DataType[] newMembers = new AS400DataType[members.length];  // let this line throw NullPointerException
     for (int i=0; i<members.length; ++i)
     {
         // store only clones
         newMembers[i] = (AS400DataType)members[i].clone();  // let this line throw NullPointerException
     }
     this.allowChanges_ = false;
     this.elements_ = newMembers;
    }

    /**
     * Creates a new AS400Structure object that is identical to the current instance.
     * @return The new object.
     **/
    public Object clone()
    {
     try
     {
         AS400Structure nObj = (AS400Structure)super.clone();  // Object.clone does not throw exception
         AS400DataType[] myMembers = this.elements_;

         if (myMembers == null) return nObj;  // Short cut out if nothing to clone

         // Create new array for clone
         AS400DataType[] newMembers = new AS400DataType[myMembers.length];
         for (int i=0; i<myMembers.length; ++i)
         {
          // clone all the elements
          newMembers[i] = (AS400DataType)myMembers[i].clone(); // Data Types do not throw exception
         }
         nObj.elements_ = newMembers;
         return nObj;
     }
     catch (CloneNotSupportedException e)
     {
         Trace.log(Trace.ERROR, "Unexpected cloning error", e);
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
    }

    /**
     * Returns the byte length of the data type.  The members of this structure must be set before calling this method.
     * @return The number of bytes in the i5/OS representation of the data type.
     **/
    public int getByteLength()
    {
     AS400DataType[] elementIterator = this.elements_;
     // Check for valid state
     if (elementIterator == null)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges_ = false;  // Check before setting so don't have unfixable object

     // Length is sum of the elements length
     int numElements = elementIterator.length;
     int totalSize = 0;
     for (int i = 0; i < numElements; ++i)
     {
         totalSize += elementIterator[i].getByteLength();
     }
     return totalSize;
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return An Object array (<tt>Object[]</tt>) containing the default values for the members of the structure.  The returned array contains one element for each member, in correct sequence.
     **/
    public Object getDefaultValue()
    {
      if (defaultValue_ == null)
      {
        synchronized (defaultValueLock_)
        {
          if (defaultValue_ == null)
          {
            int numElements = elements_.length;
            defaultValue_ = new Object[numElements];
            for (int i = 0; i < numElements; i++) {
              defaultValue_[i] = elements_[i].getDefaultValue();
            }
          }
        }
      }

      return defaultValue_;
    }


    /**
     * Returns {@link com.ibm.as400.access.AS400DataType#TYPE_STRUCTURE TYPE_STRUCTURE}.
     * @return <tt>AS400DataType.TYPE_STRUCTURE</tt>.
    **/
    public int getInstanceType()
    {
      return AS400DataType.TYPE_STRUCTURE;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>Object[].class</tt>.
     **/
    public Class getJavaType()
    {
      return Object[].class;
    }

    /**
     * Returns the number of members in the data type.
     * @return The number of members in the structure data type.  If the members have not been set, negative one (-1) is returned.
     **/
    public int getNumberOfMembers()
    {
     return (this.elements_ == null) ?
       -1 :
       this.elements_.length;
    }

    /**
     * Returns the data types of the members of the structure.
     * @return The data types of the members of the structure.  If the members have not been set, null is returned.
     **/
    public AS400DataType[] getMembers()
    {
     AS400DataType[] myMembers = this.elements_;

     if (myMembers == null) return null;  // if elements unset, return null

     // Create new array to return
     AS400DataType[] newMembers = new AS400DataType[myMembers.length];
     for (int i=0; i<myMembers.length; ++i)
     {
         // clone all the sub elements
         newMembers[i] = (AS400DataType)myMembers[i].clone();
     }
     return newMembers;
    }

    /**
     * Returns the data type of the member of the structure at the specified index.  The member array of this structure must be set before calling this method.
     * @param index The index into the structure for the member.  It must be greater than or equal to zero and less than or equal to the number of members in the data type.
     * @return The data type of the member of the structure.
     **/
    public AS400DataType getMembers(int index)
    {
     if (this.elements_ == null)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     return (AS400DataType)this.elements_[index].clone();  // only return clones, let this line throw ArrayIndexExceptions
    }

    /**
     * Sets the data types of the members of the structure.  This method must be called after a call to the null constructor and before a call to any of the conversion methods.
     * @param members The data types of the members of the structure.
     **/
    public void setMembers(AS400DataType[] members)
    {
     if (!this.allowChanges_)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }
     AS400DataType[] newMembers = new AS400DataType[members.length];  // let this line throw NullPointerException
     for (int i=0; i<members.length; ++i)
     {
         // only store clones
         newMembers[i] = (AS400DataType)members[i].clone();  // let this line throw NullPointerException
     }
     this.elements_ = newMembers;
    }

    /**
     * Sets the data type of the member of the structure at the specified index.  This method must be called after a call to the null constructor and before a call to any of the conversion methods.  The member array of this structure must be set before calling this method.
     * @param index The index into the structure for the member.  It must be greater than or equal to zero and less than or equal to the number of members in the data type.
     * @param member The data type of the member of the structure.
     **/
    public void setMembers(int index, AS400DataType member)
    {
     if (!this.allowChanges_)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }
     // check state
     if (this.elements_ == null)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.elements_[index] = (AS400DataType)member.clone();  // only store clones, let this line throw ArrayIndexException
    }

    /**
     * Converts the specified Java object to i5/OS format.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @return The i5/OS representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[this.getByteLength()];  // let getByteLength check state
     this.toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into i5/OS format in the specified byte array.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type. It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     * @return The number of bytes in the i5/OS representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return this.toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into i5/OS format in the specified byte array.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in i5/OS format.  There must be enough space to hold the i5/OS value.
     * @param offset The offset into the byte array for the start of the i5/OS value.  It must be greater than or equal to zero.
     * @return The number of bytes in the i5/OS representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
     AS400DataType[] elementIterator = this.elements_;

     // Check for valid state
     if (elementIterator == null)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges_ = false;;  // Check before setting so don't have unfixable object

     int numElements = elementIterator.length;
     Object[] arrayValue = (Object[])javaValue;  // let this line to throw ClassCastException
     if (arrayValue.length != numElements)  // Check for correct number of elements
     {
         throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
     }

     // Convert structure by iterating over elements, allow elements to do own validity checking
     for (int i = 0; i < numElements; ++i)
     {
         offset += elementIterator[i].toBytes(arrayValue[i], as400Value, offset);
     }
     return this.getByteLength();
    }

    /**
     * Converts the specified i5/OS data type to a Java object.  The members of this structure must be set before calling this method.
     * @param as400Value The array containing the data type in i5/OS format.  The entire data type must be represented.
     * @return The array of Objects.  Each element of this array is a Java object of the corresponding type of a member of this AS400Structure object.
     **/
    public Object toObject(byte[] as400Value)
    {
     return this.toObject(as400Value, 0);
    }

    /**
     * Converts the specified i5/OS data type to a Java object.  The members of this structure must be set before calling this method.
     * @param as400Value The array containing the data type in i5/OS format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the i5/OS value.  It must be greater than or equal to zero.
     * @return The array of Objects.  Each element of this array is a Java object of the corresponding type of a member of this AS400Structure object.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
     AS400DataType[] elementIterator = this.elements_;
     // Check for valid state
     if (elementIterator == null)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     this.allowChanges_ = false;  // Check before setting so don't have unfixable object
     int numElements = elementIterator.length;

     Object[] returnArray = new Object[numElements];

     // Convert structure by iterating over elements, allow elements to do own validity checking
     for (int i = 0; i < numElements; ++i)
     {
         returnArray[i] = elementIterator[i].toObject(as400Value, offset);
         offset += elementIterator[i].getByteLength();
     }
     return returnArray;
    }
}
