///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RowData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.Vector;
/**
*  The RowData class defines a representation for describing and accessing
*  a list of data.
*
*  <P>The RowData object maintains a position in the list pointing to its current row
*  of data.  The initial position is before the first row in the list.  Row indexes are
*  numbered starting with 0.
*
*  <P>The number, types, and properties of the list's columns are provided
*  by the <A href="RowMetaData.html">RowMetaData</A> object
*  returned by the <i>getMetaData</i> method.
*
*  <P>Individual data objects in the current row can have properties, or a list of objects,
*  that can be associated with the data.  Properties can be set with the <i>setObjectProperties</i>
*  method.
*
*  <P>RowData objects generate the following events:
*  <UL>
*  <LI>PropertyChangeEvent</LI>
*  <LI>VetoableChangeEvent</LI>
*  </UL>
**/
public abstract class RowData implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // @B3C
    protected Vector rows_;          // The row data.                 
    protected Vector rowProperties_;           // The row data properties, where each column in the row contains a Vector of properties. @B6C

    transient int maxLoad_;          // The maximum resource list items to load at 1 time (equals the max table size).
    transient int position_;         // The current position in the list.
    transient PropertyChangeSupport changes_; // The list of property change listeners.
    transient VetoableChangeSupport vetos_;   // The list of vetoable change listeners.

    /**
    *  Constructs a default RowData object.
    **/
    public RowData()
    {
        rows_ = new Vector();
        rowProperties_ = new Vector();

        // Initialize the transient data.
        initializeTransient();
    }

    /**
    *  Sets the position to the specified <i>rowIndex</i>.
    *  Attempting to move beyond the first row will move to the position before the first row.
    *  Attempting to move beyond the last row will move to the position after the last row.
    *
    *  @param rowIndex The row index (0-based).  The rowIndex must be zero or a positive integer which
    *  is less than the number of rows.
    *  @return true if the requested position exists; false otherwise.
    **/
    public boolean absolute(int rowIndex)
    {
        if (length() == 0 || rowIndex < 0)  // position before first row.         //$B1C
        {
            position_ = -1;
            return false;
        }                                                                         //$B1C
        else if (rowIndex >= length())
        {
            position_ = length();     // position after last row.                  //$B1C
            return false;
        }
        else
        {
            position_ = rowIndex;        // valid new position.
            return true;
        }
    }

    /**
    *  Adds a PropertyChangeListener.
    *  The specified PropertyChangeListener's <b>propertyChange</b>
    *  method is called each time the value of any bound property is changed.
    *  @see #removePropertyChangeListener
    *  @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS
        changes_.addPropertyChangeListener(listener);
    }
    /**
    *  Adds the VetoableChangeListener.
    *  The specified VetoableChangeListener's <b>vetoableChange</b>
    *  method is called each time the value of any constrained property is changed.
    *  @see #removeVetoableChangeListener
    *  @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
        vetos_.addVetoableChangeListener(listener);
    }

    /**
    *  Sets the position after the last row in the list.
    *  This is a valid position after the end of the list.
    **/
    public void afterLast()
    {
        // Validate the list is not empty.
//@B4D        validateRowList("Attempting to set the position in the list");

        position_ = length();                               //$B1C
    }

    /**
    *  Sets the position before the first row in the list.
    *  This is a valid position before the beginning of the list.
    **/
    public void beforeFirst()
    {
        // Validate the list is not empty.
//@B4D        validateRowList("Attempting to set the position in the list");

        position_ = -1;
    }

    /**
    *  Sets the position to the first row in the list.
    *
    *  @return true if the requested position exists; false if the list is empty.
    **/
    public boolean first()
    {
        if (length() != 0)                                 //$B1C
        {
            position_ = 0;
            return true;
        }
        else
            return false;
    }

    /**
    *  Returns the current row position.
    *
    *  @return The row position (0-based).
    **/
    public int getCurrentPosition()
    {
        return position_;
    }

    /**
    *  Returns the meta data.
    *
    *  @return The meta data.
    *  @exception RowDataException If a row data error occurs.
    **/
    public abstract RowMetaData getMetaData() throws RowDataException;

    /**
    *  Returns the current row's column data specified at <i>columnIndex</i>.
    *
    *  @param columnIndex The column index (0-based).
    *  @return The column object.
    *  @exception RowDataException If a row data error occurs.
    **/
    public Object getObject(int columnIndex) throws RowDataException
    {
        // Validate that the list is not empty.
        validateRowList("Attempting to get the column object");

        // Get the current row.
        validateListPosition("Attempting to get the current object");

        Object[] row = (Object[])rows_.elementAt(position_);

        // Validate the column parameter.
        if ( columnIndex < 0 || columnIndex >= row.length )
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return row[columnIndex];
    }

    /**
    *  Returns the data object's property list at the specified <i>columnIndex</i>.
    *
    *  @param columnIndex The column index (0-based).
    *  @return The property list for the column data object.
    *  @see com.ibm.as400.util.servlet.RowData#setObjectProperties
    **/
    public Vector getObjectProperties(int columnIndex)
    {
        // Validate that the list is not empty.
        validateRowList("Attempting to get the column object's properties");

        // Get the current row.
        validateListPosition("Attempting to get the current object's properties");

        Vector[] propertyList = (Vector[])rowProperties_.elementAt(position_);

        // Validate the columnIndex parameter.
        if ( columnIndex < 0 || columnIndex >= propertyList.length )
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return propertyList[columnIndex];
    }


    /**
    *  Returns the current row's property list.
    *  Each Vector in the properties list corresponds to the
    *  appropriate data object's list of properties.
    *  @return The property lists for each data object in the row.
    **/
    public Vector[] getRowProperties()
    {
        // Validate that the list is not empty.
        validateRowList("Attempting to get the row's property list");

        // Get the current row.
        validateListPosition("Attempting to get the row's property list");

        return(Vector[])rowProperties_.elementAt(position_);
    }

    /**
    *  Initializes the transient data.
    **/
    private void initializeTransient()
    {
        maxLoad_ = 0;
        position_ = -1;         // Set the initial position before the first row.
        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
    }

    /**
    *  Indicates whether the current position is after the last row in the list.
    *  This is a valid position after the end of the list.
    *  @return true if the position is after the last row; false otherwise.
    **/
    public boolean isAfterLast()
    {
        if (position_ == length())                                    //$B1C
            return true;
        else
            return false;
    }

    /**
    *  Indicates whether the current position is before the first row in the list.
    *  This is a valid position before the beginning of the list.
    *  @return true if the position is before the first row and the list is not empty; false otherwise.
    **/
    public boolean isBeforeFirst()
    {
        if (length() != 0 && position_ == -1)                         //$B1C
            return true;
        else
            return false;
    }

    /**
    *  Indicates whether the current position is the first row in the list.
    *
    *  @return true if the position is the first row; false otherwise.
    **/
    public boolean isFirst()
    {
        if (position_ == 0)
            return true;
        else
            return false;
    }

    /**
    *  Indicates whether the current position is the last row in the list.
    *
    *  @return true if the position is the last row; false otherwise.
    **/
    public boolean isLast()
    {
        if (length() != 0 && position_ == (length() -1))              //$B1C
            return true;
        else
            return false;
    }

    /**
    *  Sets the position to the last row in the list.
    *
    *  @return true if the requested position exists; false if the list is empty.
    **/
    public boolean last()
    {                                                                //$B1C
        if (length() != 0)
        {
            position_ = length() - 1;                                  //$B1C
            return true;
        }
        else
            return false;
    }

    /**
    *  Returns the number of rows in the list.
    *
    *  @return The number of rows.
    **/
    public int length()
    {
        return rows_.size();
    }

    /**
    *  Sets the position to the next row in the list.
    *  The list is initially positioned before its first row.  The first call
    *  to next makes the first row the current row, the second call makes
    *  the second row the current row, etc.  Moving beyond last row in the list will
    *  result in a position after the last row.
    *  @return true if the requested position exists in the list; false if there are no more rows.
    **/
    public boolean next()
    {
        if (length() == 0)                                            //$B1C
            return false;
        else
        {                                                             //$B1C
            if (position_ < length())
                position_++;        // update position if the current position is valid.

            if (position_ == length())                                 //$B1C
                return false;        // new position is after last row.
            else
                return true;
        }
    }

    /**
    *  Sets the position to the previous row in the list.
    *  Moving beyond the first row in the list will result in a position before the first row.
    *  @return true if the requested position exists in the list; false otherwise.
    **/
    public boolean previous()
    {
        if (position_- 1 >= -1)    // current position must be valid. (i.e. empty list - invalid)
            position_--;

        if (position_ == -1)    // new position is before first row.
            return false;
        else
            return true;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException, RowDataException
    {
        in.defaultReadObject();

        initializeTransient();
    }

    /**
    *  Sets the position a relative <i>numberOfRows</i> based on the current position.
    *  Moving beyond the first/last row in the list will result in a position
    *  before/after the first/last row.
    *
    *  @param numberOfRows The number of rows to move, either positive or negative.
    *  @return true if the requested position exists; false otherwise.
    **/
    public boolean relative(int numberOfRows)
    {
        // Determine new position;
        int temp = position_ + numberOfRows;

        // Check if new position is valid.
        if (length() == 0 || temp < 0)     // position before first row.         //$B1C
        {
            position_ = -1;
            return false;
        }                                                                        //$B1C
        else if (temp >= length())      // position after last row.
        {
            position_ = length();                                                 //$B1C
            return false;
        }
        else              // Valid new position.
        {
            position_ = temp;
            return true;
        }
    }

    /**
    *  Removes the PropertyChangeListener from the internal list.
    *  If the PropertyChangeListener is not on the list, nothing is done.
    *  @see #addPropertyChangeListener
    *  @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS
    }

    /**
    *  Removes the VetoableChangeListener from the internal list.
    *  If the VetoableChangeListener is not on the list, nothing is done.
    *  @see #addVetoableChangeListener
    *  @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS
    }

    /**
    *  Sets the data object's <i>properties</i> at the specified <i>columnIndex</i>.
    *  Object properties are user defined objects that can be associated with
    *  the data in the row.
    *
    *  @param properties The properties.
    *  @param columnIndex The column index (0-based).
    *  @see com.ibm.as400.util.servlet.RowData#getObjectProperties
    **/
    public void setObjectProperties(Vector properties, int columnIndex)
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Setting the column object's properties.");

        // Validate that the list is not empty.
        validateRowList("Attempting to set a column object's properties");

        // Validate the properties parameter.
        if (properties == null)
            throw new NullPointerException("properties");

        // Validate the current row.
        validateListPosition("Attempting to set a column object's properties");

        // Get the column parameter list.
        Vector[] propertyList = (Vector[])rowProperties_.elementAt(position_);

        // Validate the columnIndex parameter.
        if (columnIndex < 0 || columnIndex >= propertyList.length)
            throw new ExtendedIllegalArgumentException("columnIndex", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        // Set the properties.
        propertyList[columnIndex] = properties;
        rowProperties_.setElementAt(propertyList, position_);
    }

    /**
    *  Validates the position in the list.
    *  @param attemptedAction The attempted action on the list.
    **/
    void validateListPosition(String attemptedAction)
    {
        if (isBeforeFirst() || isAfterLast())
        {
            Trace.log(Trace.ERROR, attemptedAction + " when the position in the list is invalid.");
            throw new ExtendedIllegalStateException("position", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }

    /**
    *  Validate the row list is not empty.
    *  @param attemptedAction The attempted action on the list.
    **/
    void validateRowList(String attemptedAction)
    {
        // Validate that the list is not empty.
        if (length() == 0)                                            //$B1C
        {
            Trace.log(Trace.ERROR, attemptedAction + " before adding a row to the list.");
            throw new ExtendedIllegalStateException("rows", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }

}
