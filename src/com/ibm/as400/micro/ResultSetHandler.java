///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResultSetHandler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 *  The ResultSetHandler class is designed to handle all interactions
 *  needed by the JDBC-ME driver with the JDBC ResultSet interface.
 **/
class ResultSetHandler
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    JdbcMeService service_;
    MicroDataInputStream in_;
    MicroDataOutputStream out_;


    /**
    Constructor.  Creates a new JDBC-ME handler for ResultSet
    objects.
    **/
    public ResultSetHandler(JdbcMeService jdbcme, MicroDataInputStream in, MicroDataOutputStream out)
    {
        service_ = jdbcme;
        in_ = in;
        out_ = out;
    }       


    /**
    The process function routes the function id and the ResultSet
    to the proper handler.
    **/
    public void process(ResultSet rs, int funcId) throws IOException
    {
        switch (funcId)
        {
        case MEConstants.RS_CLOSE:
            close(rs);
            break;
        case MEConstants.RS_DELETE_ROW:
            deleteRow(rs);
            break;
        case MEConstants.RS_INSERT_ROW:
            insertRow(rs);
            break;
        case MEConstants.RS_NEXT:
            next(rs);
            break;
        case MEConstants.RS_PREVIOUS:
            previous(rs);
            break;
        case MEConstants.RS_UPDATE_ROW:
            updateRow(rs);
            break;
            // TODO:  from here down...
            // returns data
        case MEConstants.RS_ABSOLUTE:
            absolute(rs);
            break;
            // true/false
        case MEConstants.RS_AFTER_LAST:
            afterLast(rs);
            break;
            // true/false
        case MEConstants.RS_BEFORE_FIRST:
            beforeFirst(rs);
            break;
            // returns data
        case MEConstants.RS_FIRST:
            first(rs);
            break;
            // true/false
        case MEConstants.RS_IS_AFTER_LAST:
            isAfterLast(rs);
            break;
            // true/false
        case MEConstants.RS_IS_BEFORE_FIRST:
            isBeforeFirst(rs);
            break;
            // true/false
        case MEConstants.RS_IS_FIRST:
            isFirst(rs);
            break;
            // true/false
        case MEConstants.RS_IS_LAST:
            isLast(rs);
            break;
            // returns data
        case MEConstants.RS_LAST:
            last(rs);
            break;
            // returns data
        case MEConstants.RS_RELATIVE:
            relative(rs);
            break;
        default:
            // TODO:  This is an exception condition...
            System.out.println("Error - ResultSet Function ID not recognized - function code: " + funcId);
            break;
        }
    }



    /**
    Closes the ResultSet object.  Unlike most of the methods of this class,
    if an exception occurs while closing the ResultSet, this method
    will not report it back to the caller in any way.  It is simply
    swollowed and a message is logged concerning the failure.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       - nothing more
    <P>
 
    @param rs    The ResultSet object to close.
    **/
    public void close(ResultSet rs) throws IOException
    {
        try
        {
            rs.close();
        }
        catch (SQLException e)
        {
            System.out.println("Exception caught trying to close ResultSet object " + rs);
            e.printStackTrace();
        }
        try
        {
            // Try this seperately...
            service_.removeResultSet(rs);  // DOMINO
        }
        catch (SQLException e)
        {
            System.out.println("Exception caught trying to remove ResultSet " +
                               "object from tracking");
            e.printStackTrace();
        }
    }



    /**
    Deletes the current row from the ResultSet object.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - whether or not the delete was successful.
          1     - the function worked.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void deleteRow(ResultSet rs) throws IOException
    {
        try
        {
            rs.deleteRow();
            out_.writeInt(1);  // Today, we will always say we worked today.
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Inserts a new row into the table the ResultSet is for.
    <P>
    The data flow is as follows:
    <input>
       <list>   - a value for all of the columns in the table.
    <output>
       int - whether or not the insert was successful.
          1     - the function worked.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void insertRow(ResultSet rs) throws IOException
    {
        ResultSetMetaData rsmd = null;
        int count;

        try
        {
            rs.moveToInsertRow();
            rsmd = rs.getMetaData();
            count = rsmd.getColumnCount();
            for (int i = 0; i < count; i++)
            {
                if (rsmd.getColumnType(i+1) == Types.INTEGER)
                {
                    rs.updateInt(i+1, in_.readInt());
                }
                else
                {
                    rs.updateString(i+1, in_.readUTF());
                }
            }
            // Update the row.
            rs.insertRow();
            out_.writeInt(1); // Today we will always say this worked today.
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches the next row from the database for the current ResultSet.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void next(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.next())
            {
                out_.writeInt(1);

                writeRow(rs);
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches the previous row from the database for the current ResultSet.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void previous(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.previous())
            {
                out_.writeInt(1);

                writeRow(rs);  // output the current row.
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches the first row from the database for the current ResultSet.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void first(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.first())
            {
                out_.writeInt(1);

                writeRow(rs);
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches the last row from the database for the current ResultSet.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void last(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.last())
            {
                out_.writeInt(1);

                writeRow(rs);
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches a row relative to the current position in the result set.
    <P>
    The data flow is as follows:
    <input>
       int - the relative record number.
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void relative(ResultSet rs) throws IOException
    {
        try
        {
            int value = in_.readInt();
            if (rs.relative(value))
            {
                out_.writeInt(1);

                writeRow(rs);
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Fetches a row relative to the begining of the result set.
    <P>
    The data flow is as follows:
    <input>
       int - the absolute record number.
    <output>
       int - whether or not the fetch was successful.
          1     - data was fetched and is in the stream
                   See the writeRow method for the layout of this section
                   of the data stream...
          0     - there was no more data to fetch
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void absolute(ResultSet rs) throws IOException
    {
        try
        {
            int value = in_.readInt();
            if (rs.absolute(value))
            {
                out_.writeInt(1);

                writeRow(rs);
            }
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Positions the cursor before the first row in the ResultSet. This
    basically allows you to 'start over' with the ResultSet.
    <P>
    The data flow is as follows:
    <input>
       none.
    <output>
       int - whether or not the positioning was successful.
          1     - success
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void beforeFirst(ResultSet rs) throws IOException
    {
        try
        {
            rs.beforeFirst();
            out_.writeInt(1);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Positions the cursor after the last row in the ResultSet.
    <P>
    The data flow is as follows:
    <input>
       none.
    <output>
       int - whether or not the positioning was successful.
          1     - success
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void afterLast(ResultSet rs) throws IOException
    {
        try
        {
            rs.afterLast();
            out_.writeInt(1);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Returns whether or not the cursor is after the last row
    in the result set.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - whether or not the fetch was successful.
          1     - the cursor is after the last row in the result set.
          0     - the cursor is not after the last row in the result set.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void isAfterLast(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.isAfterLast())
                out_.writeInt(1);
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Returns whether or not the cursor is before the first row
    in the result set.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - whether or not the fetch was successful.
          1     - the cursor is before the first row in the result set.
          0     - the cursor is not before the first row in the result set.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void isBeforeFirst(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.isBeforeFirst())
                out_.writeInt(1);
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Returns whether or not the cursor is on the first row
    in the result set.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - whether or not the fetch was successful.
          1     - the cursor is on the first row in the result set.
          0     - the cursor is not on the first row in the result set.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void isFirst(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.isFirst())
                out_.writeInt(1);
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Returns whether or not the cursor is on the last row
    in the result set.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - whether or not the fetch was successful.
          1     - the cursor is on the last row in the result set.
          0     - the cursor is not on the last row in the result set.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void isLast(ResultSet rs) throws IOException
    {
        try
        {
            if (rs.isLast())
                out_.writeInt(1);
            else
                out_.writeInt(0);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Updates the current row into the table the ResultSet is for.
    <P>
    The data flow is as follows:
    <input>
       <list>   - a value for all of the columns in the table.
    <output>
       int - whether or not the update was successful.
          1     - the function worked.
          -1    - an exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void updateRow(ResultSet rs) throws IOException
    {
        ResultSetMetaData rsmd = null;
        int count;

        try
        {
            // Just in case we are sitting around on the insert row...
            // I am not sure if I can get away with such 'skimpy' logic
            // here... there might be wholes where we return the wrong thing.
            rs.moveToCurrentRow();
            rsmd = rs.getMetaData();
            count = rsmd.getColumnCount();
            for (int i = 0; i < count; i++)
            {
                if (rsmd.getColumnType(i+1) == Types.INTEGER)
                    rs.updateInt(i+1, in_.readInt());
                else
                    rs.updateString(i+1, in_.readUTF());
            }
            // Update the row.
            rs.updateRow();
            out_.writeInt(1); // We will always say this worked today.
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }



    /**
    Outputs the current row of the database.  This method is going to return
    data in this format:
 
    <bit list> 1 if the column is null, 0 otherwise.
    based on data types flag:
    ALL:
    <list>   - the data values for the columns
                - null columns come back as 0 or empty strings in this list.
    LIMITED:
    <list>   - the data values for the columns (as all ints or strings).
                - null columns come back as 0 or emtpy strings in this list.
    STRING:
    <list>   - the data values for the columns (as all strings).
                - null columns come back as emtpy strings in this list.
    **/
    protected void writeRow(ResultSet rs) throws IOException
    {
        ResultSetMetaData rsmd = null;
        int count;

        try
        {
            rsmd = rs.getMetaData();
            count = rsmd.getColumnCount();

            // Figure out the bitmap for null values.
            // Note: The performance of this solution kinda sucks.
            //       We need to fetch all the data values into local
            //       storage and the build the bitmap followed by the
            //       return values.  Fetching everything twice is not
            //       acceptable.
            // BitMap bm = new BitMap(count);
            // for (int i = 0; i < count; i++) {
            //    if (rs.getString(i + 1) == null)
            //       bm.set(i, true);
            // }
            // bm.write(out);
            switch (service_.getDataFlowType())
            {
            case MEConstants.DATA_FLOW_LIMITED:
                for (int i = 0; i < count; i++)
                {
                    if (rsmd.getColumnType(i + 1) == Types.INTEGER)
                        out_.writeInt(rs.getInt(i+1));
                    else
                        out_.writeUTF(rs.getString(i+1));
                }
                break;
            case MEConstants.DATA_FLOW_STRINGS_ONLY:
                System.out.println("DATA_FLOW_STRINGS_ONLY: This isn't implemented yet.");
                break;
            case MEConstants.DATA_FLOW_ALL:
                System.out.println("DATA_FLOW_ALL: This isn't implemented yet.");
                break;
            default:
                System.out.println("getDataFlowType(): Just what the heck did you ask for here? " + service_.getDataFlowType());
                break;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Everything is lost... there was an SQLException after the primary function was executed.... stream probably corrupted.");
        }
    }

}
