///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDataVector.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.io.IOException;                                         // @C1A
import java.io.ObjectInputStream;                                   // @C1A
import java.io.Serializable;

import java.util.Vector;

class PcmlDataVector extends Vector {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = -8169008879805188674L;	    // @C1A

    private PcmlData m_owner;         // PcmlData node that owns this vector
    
    private PcmlDimensions m_indices; // Indices into owning PcmlNode's vectors of PcmlDataValues

    private long     m_dimTs;         // Timestamp when this vector was redimensioned

    // Default constructor
    PcmlDataVector(PcmlData owner, PcmlDimensions indices) 
    {
    	super();
        m_owner = owner;
        m_indices = new PcmlDimensions(indices);
        m_dimTs = PcmlDocument.getCorrellationID();                 // @C2C
    }

    PcmlDataVector(int size, PcmlData owner, PcmlDimensions indices) 
    {
    	super(size);
        m_owner = owner;
        m_indices = new PcmlDimensions(indices);
        m_dimTs = PcmlDocument.getCorrellationID();                 // @C2C
    }

	// Custom deserialization
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException                  // @C1A
    {                                                               // @C1A
        // Default deserialization
        in.defaultReadObject();                                     // @C1A
    }                                                               // @C1A

	// Custom deserialization post-processing
	// This processing cannot be done during readObject() because
	// references to parent objects in the document are not yet set
	// due to the recursive nature of deserialization.
    void readObjectPostprocessing()                                 // @C1A
    {                                                               // @C1A
    	Object item;                                                // @C1A
    	
    	// Set dimension timestamp
        m_dimTs = m_owner.getDoc().getDeserializationTimestamp();   // @C1A
        
        // Recursively perform post processing for 
        // all nested PcmlDataVectors and PcmlDataValues
    	for (int i = 0; i < size(); i++)                            // @C1A
    	{                                                           // @C1A
    		item = elementAt(i);                                    // @C1A
    		if (item instanceof PcmlDataValues)                     // @C1A
            {                                                       // @C1A
  				((PcmlDataValues) item).readObjectPostprocessing(); // @C1A
    		}                                                       // @C1A
    		else if (item instanceof PcmlDataVector)                // @C1A
            {                                                       // @C1A
                ((PcmlDataVector) item).readObjectPostprocessing(); // @C1A
            }                                                       // @C1A
    	}                                                           // @C1A
    }                                                               // @C1A
    
    // Get Timestamp of data
    long getTimestamp() 
    {
        return m_dimTs;
    }

    // Flush the values held by this vector
    void flushValues() 
    {
    	Object item;
    	for (int i = 0; i < size(); i++) 
    	{
    		item = elementAt(i);
    		if (item instanceof PcmlDataValues) 
            {
  				((PcmlDataValues) item).flushValues();
    		}
    		else if (item instanceof PcmlDataVector)
            {
                ((PcmlDataVector) item).flushValues();
            }
            setElementAt(null, i);
    	}
    }

    // Redimension the vector
    void redimension(int newSize) 
    {
        flushValues();
        m_dimTs = PcmlDocument.getCorrellationID();                 // @C2C
        if (newSize > capacity())
        {
            ensureCapacity(newSize);
            setSize(newSize);
        }
        else 
        {
            setSize(newSize);
            trimToSize();
        }
    }

    // Return the PcmlDataValues object at the index
    PcmlDataValues valuesAt(int i)
    {
    	return (PcmlDataValues) elementAt(i);
    }

    // Return the PcmlDataValues object at the index
    PcmlDataVector vectorAt(int i) 
    {
    	return (PcmlDataVector) elementAt(i);
    }
    
    // Create a PcmlDataValues object for each entry in the vector
    void populateWithValues() 
    {
    	PcmlDataValues item;
    	PcmlDimensions newIndices = new PcmlDimensions(m_indices);
    	for (int i = 0; i < size(); i++) 
    	{
    		newIndices.add(i);
    		item = valuesAt(i);
    		if (item == null) 
    		{
    			item = new PcmlDataValues(m_owner, newIndices);
    			setElementAt(item, i);
    		}
    		else 
    		{
    			item.flushValues();
    		}
    		newIndices.remove();
    	}
    }
}
