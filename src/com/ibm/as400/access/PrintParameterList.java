///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintParameterList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException; // @A1A

/**
 * The  PrintParameterList class is used to group a set of attributes or
 * parameters for use on other network print class methods.
 * If a parameter has not been previously set, it is appended to the list;
 * otherwise, the parameter is overridden.
 *
 *
 *@see PrintObject
 **/

// public class that we'll use to "wrap" the NPCPAttribute class in
// exposing what is needed to set a list of parameters
public class PrintParameterList
implements java.io.Serializable   // @A1A
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    private NPCPAttribute attrCP_;


   /**
     * Constructs a PrintParameterList object.
     *
     **/
    public PrintParameterList()
    {
        attrCP_ = new NPCPAttribute();
    }

    //
    // get access to attribute code point
    //  used internally when we are ready to send this code point
    //
    NPCPAttribute getAttrCodePoint()
    {
        return attrCP_;
    }

    
   /**
     * Returns a float parameter.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the documentation of the specific network print classes for the attribute IDs
     * that are valid for each particular class.
     *
     * @return The value of the attribute.
     **/
    public Float getFloatParameter(int attributeID)
    {
	return attrCP_.getFloatValue(attributeID);

    }

   /**
     * Returns an integer parameter.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the documentation of network print classes for what attribute IDs
     * are valid for each particular class.
     *
     * @return The value of the attribute.
     **/
    public Integer getIntegerParameter(int attributeID)
    {
	return attrCP_.getIntValue(attributeID);
    }

    /**
     * Returns a string parameter.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the documentation of network print classes for what attribute IDs
     * are valid for each particular class.
     *
     * @return The value of the attribute.
     **/
    public String getStringParameter(int attributeID)
    {
	return attrCP_.getStringValue(attributeID);
    }

    /* Allows object to be deserialized */                  // @A1A
    private void readObject(java.io.ObjectInputStream in)   // @A1A
        throws IOException, ClassNotFoundException          // @A1A
    {                                                       // @A1A
        in.defaultReadObject();                             // @A1A
    }                                                       // @A1A

    /**
     * Sets a string parameter.
     *
     * @param attributeID Identifies which attribute to set.
     * See the documentation of network print classes for what attribute IDs
     * are valid for each particular class.
     * @param value The new value for the attribute.
     *
     **/
    public void setParameter(int attributeID, String value)
    {
        attrCP_.setAttrValue(attributeID, value);
    }

   /**
     * Sets an integer parameter.
     *
     * @param attributeID Identifies which attribute to set.
     * See the documentation of network print classes for what attribute IDs
     * are valid for each particular class.
     * @param value The new value for the attribute.
     *
     **/
    public void setParameter(int attributeID, int value)
    {
        attrCP_.setAttrValue(attributeID, value);
    }

   /**
     * Sets a float parameter.
     *
     * @param attributeID Identifies which attribute to set.
     * See the documentation of network print classes for what attribute IDs
     * are valid for each particular class.
     * @param value The new value for the attribute.
     *
     **/
    public void setParameter(int attributeID, float value)
    {
        attrCP_.setAttrValue(attributeID, value);
    }

} // Print Parameter List class

