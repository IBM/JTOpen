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
 *  Provides a composite data type representing a structure of AS400DataType objects.
 **/
public class AS400Structure implements AS400DataType
{
    static final long serialVersionUID = 4L;

    private AS400DataType[] elements_ = null;
    private transient Object elementsLock_;

    private transient Object[] defaultValue_;

    private boolean allowChanges_ = true;  // For beans: allow changes after null constructor until conversion method called

    /**
     * Constructs an AS400Structure object.  The setMembers() method must be called before any conversion methods or getByteLength()  on this object.
     **/
    public AS400Structure()
    {
      initializeTransient();
    }

    /**
     * Constructs an AS400Structure object.
     * @param members The data types of the members of the structure.
     **/
    public AS400Structure(AS400DataType[] members)
    {
     if (members == null) throw new NullPointerException("members");
     initializeTransient();
     AS400DataType[] newMembers = new AS400DataType[members.length];
     for (int i=0; i<members.length; ++i)
     {
         // store only clones
         newMembers[i] = (AS400DataType)members[i].clone();
     }
     allowChanges_ = false;
     elements_ = newMembers;
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

         if (elements_ == null) return nObj;  // Short cut out if nothing to clone

         synchronized (elementsLock_)
         {
           // Create new array for clone
           AS400DataType[] newMembers = new AS400DataType[elements_.length];
           for (int i=0; i<elements_.length; ++i)
           {
             // clone all the elements
             newMembers[i] = (AS400DataType)elements_[i].clone(); // Data Types do not throw exception
           }
           nObj.elements_ = newMembers;
           return nObj;
         }
     }
     catch (CloneNotSupportedException e)
     {
         Trace.log(Trace.ERROR, "Unexpected cloning error", e);
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
    }

    /**
     * Returns the byte length of the data type.  The members of this structure must be set before calling this method.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int getByteLength()
    {
      // Check for valid state
      if (elements_ == null)
      {
        throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      synchronized (elementsLock_)
      {
        allowChanges_ = false;  // Check before setting so don't have unfixable object

        // Length is sum of the elements length
        int totalSize = 0;
        for (int i = 0; i < elements_.length; ++i)
        {
          totalSize += elements_[i].getByteLength();
        }
        return totalSize;
      }
    }

    /**
     * Returns a Java object representing the default value of the data type.
     * @return An Object array (<tt>Object[]</tt>) containing the default values for the members of the structure.  The returned array contains one element for each member, in correct sequence.
     **/
    public Object getDefaultValue()
    {
      synchronized (elementsLock_)
      {
        if (defaultValue_ == null)
        {
          int numElements = elements_.length;
          defaultValue_ = new Object[numElements];
          for (int i = 0; i < numElements; i++) {
            defaultValue_[i] = elements_[i].getDefaultValue();
          }
        }
        return defaultValue_;
      }
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
      if (elements_ == null) return null;  // if elements unset, return null

      synchronized (elementsLock_)
      {
        // Create new array to return
        AS400DataType[] newMembers = new AS400DataType[elements_.length];
        for (int i=0; i<elements_.length; ++i)
        {
          // clone all the sub elements
          newMembers[i] = (AS400DataType)elements_[i].clone();
        }
        return newMembers;
      }
    }

    /**
     * Returns the data type of the member of the structure at the specified index.  The member array of this structure must be set before calling this method.
     * @param index The index into the structure for the member.  It must be greater than or equal to zero and less than or equal to the number of members in the data type.
     * @return The data type of the member of the structure.
     **/
    public AS400DataType getMembers(int index)
    {
      if (elements_ == null)
      {
        throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      if (index < 0 || index >= elements_.length)
      {
        throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      synchronized (elementsLock_)
      {
        return (AS400DataType)elements_[index].clone();  // only return clones, let this line throw ArrayIndexExceptions
      }
    }


    /**
     Initialize the transient data.
     **/
    void initializeTransient()
    {
      elementsLock_ = new Object();
      defaultValue_ = null;
    }


    /**
     *Deserializes and initializes transient data.
     */
    private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException, ClassNotFoundException
    {
      in.defaultReadObject();
      initializeTransient();
    }

    /**
     * Sets the data types of the members of the structure.  This method must be called after a call to the null constructor and before a call to any of the conversion methods.
     * @param members The data types of the members of the structure.
     **/
    public void setMembers(AS400DataType[] members)
    {
     if (members == null) throw new NullPointerException("members");
     if (!allowChanges_)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }
     AS400DataType[] newMembers = new AS400DataType[members.length];
     for (int i=0; i<members.length; ++i)
     {
         // only store clones
         newMembers[i] = (AS400DataType)members[i].clone();
     }

     synchronized (elementsLock_)
     {
       elements_ = newMembers;
       defaultValue_ = null;  // discard any previous default value
     }
    }

    /**
     * Sets the data type of the member of the structure at the specified index.  This method must be called after a call to the null constructor and before a call to any of the conversion methods.  The member array of this structure must be set before calling this method.
     * @param index The index into the structure for the member.  It must be greater than or equal to zero and less than or equal to the number of members in the data type.
     * @param member The data type of the member of the structure.
     **/
    public void setMembers(int index, AS400DataType member)
    {
     if (member == null) throw new NullPointerException("member");
     // check state
     if (elements_ == null)
     {
       throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     if (index < 0 || index >= elements_.length)
     {
       throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
     }
     if (!allowChanges_)
     {
         throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }

     synchronized (elementsLock_)
     {
       elements_[index] = (AS400DataType)member.clone();  // only store clones, let this line throw ArrayIndexException
       if (defaultValue_ != null) {
         defaultValue_[index] = elements_[index].getDefaultValue();
       }
     }
    }

    /**
     * Converts the specified Java object to IBM i format.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @return The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
     byte[] as400Value = new byte[getByteLength()];  // let getByteLength check state
     toBytes(javaValue, as400Value, 0);
     return as400Value;
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type. It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value)
    {
     return toBytes(javaValue, as400Value, 0);
    }

    /**
     * Converts the specified Java object into IBM i format in the specified byte array.  The members of this structure must be set before calling this method.
     * @param javaValue The object corresponding to the data type.  It must be an Object array, the array must contain the correct number of elements, and each element must be of the correct type.
     * @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] as400Value, int offset)
    {
      // Check for valid state
      if (elements_ == null)
      {
        throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      synchronized (elementsLock_)
      {
        allowChanges_ = false;  // Check before setting so don't have unfixable object

        int numElements = elements_.length;
        Object[] arrayValue = (Object[])javaValue;  // let this line to throw ClassCastException
        if (arrayValue.length != numElements)  // Check for correct number of elements
        {
          throw new ExtendedIllegalArgumentException("javaValue (" + javaValue.toString() + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        // Convert structure by iterating over elements, allow elements to do own validity checking
        for (int i = 0; i < numElements; ++i)
        {
          offset += elements_[i].toBytes(arrayValue[i], as400Value, offset);
        }
        return getByteLength();
      }
    }

    /**
     * Converts the specified IBM i data type to a Java object.  The members of this structure must be set before calling this method.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @return The array of Objects.  Each element of this array is a Java object of the corresponding type of a member of this AS400Structure object.
     **/
    public Object toObject(byte[] as400Value)
    {
     return toObject(as400Value, 0);
    }

    /**
     * Converts the specified IBM i data type to a Java object.  The members of this structure must be set before calling this method.
     * @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
     * @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     * @return The array of Objects.  Each element of this array is a Java object of the corresponding type of a member of this AS400Structure object.
     **/
    public Object toObject(byte[] as400Value, int offset)
    {
      // Check for valid state
      if (elements_ == null)
      {
        throw new ExtendedIllegalStateException("Members", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      synchronized (elementsLock_)
      {
        allowChanges_ = false;  // Check before setting so don't have unfixable object
        int numElements = elements_.length;

        Object[] returnArray = new Object[numElements];

        // Convert structure by iterating over elements, allow elements to do own validity checking
        for (int i = 0; i < numElements; ++i)
        {
          returnArray[i] = elements_[i].toObject(as400Value, offset);
          offset += elements_[i].getByteLength();
        }
        return returnArray;
      }
    }
}
