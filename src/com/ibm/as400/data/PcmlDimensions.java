///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDimensions.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.io.Serializable;                                        // @C1A

import java.util.Vector;

class PcmlDimensions extends Object implements Serializable {       // @C1C
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = -8169008879805188674L;	    // @C1A

    private Vector v;

    PcmlDimensions() 
    {
        v = new Vector(5, 5);
    }

    PcmlDimensions(int nbrDimensions) 
    {
        v = new Vector(nbrDimensions, 5);
    }

    PcmlDimensions(int[] values) 
    {
        v = new Vector(values.length, 5);
        add(values);
    }

    PcmlDimensions(PcmlDimensions values) 
    {
        v = new Vector(values.size(), 5);
        add(values);
    }

    int[] asArray() 
    {
        int[] values = new int[v.size()];
        for (int i=0; i < v.size(); i++) 
        {
            values[i] = ((Integer)v.elementAt(i)).intValue();
        }
        return values;
    }

    public String toString() 
    {
        StringBuffer buf = new StringBuffer(v.size()*10);
        buf.append("{");
        for (int i=0; i < v.size(); i++) 
        {
            buf.append( v.elementAt(i) );
            if (i < v.size()-1) 
                buf.append( ", " );
        }
        buf.append("}");
        return buf.toString();
    }

    int at(int index) throws ArrayIndexOutOfBoundsException 
    {
        return ((Integer)v.elementAt(index)).intValue();
    }

    Integer integerAt(int index) throws ArrayIndexOutOfBoundsException 
    {
        return (Integer) v.elementAt(index);
    }

    void add(Integer value) 
    {
        v.addElement(value);
    }

    void add(int value) 
    {
        add(new Integer(value));
    }

    void add(int[] values) 
    {
        v.ensureCapacity(v.size() + values.length);
        for (int i=0; i < values.length; i++) 
        {
            v.addElement(new Integer(values[i]));
        }
    }

    void add(PcmlDimensions values) 
    {
        v.ensureCapacity(v.size() + values.size());
        for (int i=0; i < values.size(); i++) 
        {
            v.addElement(values.integerAt(i));
        }
    }

    void remove() 
    {
        v.removeElementAt(v.size() - 1);
    }

    int size() 
    {
        return v.size();
    }

}
