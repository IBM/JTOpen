///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeOfflineMidpData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import javax.microedition.rms.*;
import com.sun.kjava.Database;
import java.sql.*;

/**
 * The offline data representation on a MIDP profile.
 **/
class JdbcMeOfflineMidpData extends JdbcMeOfflineData
{
    private RecordStore DB_;

    JdbcMeOfflineMidpData(String name, int dbCreator, int dbType, boolean createIfNecessary) throws JdbcMeException 
    {
        try
        {
            DB_ = RecordStore.openRecordStore(name, createIfNecessary);
            // Must throw an exception on filed to open.
            if (DB_ == null)
                throw new JdbcMeException("Not open " + name + "createIfNecessary=" + createIfNecessary, null);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Create an offline data repository, destroying the current one if it exists.
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public static JdbcMeOfflineData create(String name, int dbCreator, int dbType) throws JdbcMeException 
    {
        try
        {
            try
            {
                RecordStore.deleteRecordStore(name);
            }
            catch (RecordStoreNotFoundException ex)
            { }

            return new JdbcMeOfflineMidpData(name, dbCreator, dbType, true);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Get a record from the offline data store.
     *
     *  @param index The record to return.  The first record is record 0.
     *  The last record is this.size()-1;
     *
     *  @return The record.
     *  
     *  @exception JdbcMeException If an error occurs.
     **/
    public byte[] getRecord(int index) throws JdbcMeException 
    {
        try
        {
            // javax.microedition.rms.RecordStore first index is 1, not 0.
            return DB_.getRecord(index+1);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     * Get the current number of records in the offline
     * data store.
     *
     *  @return The number of records.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public int size() throws JdbcMeException 
    {
        try
        {
            return DB_.getNumRecords();
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Add a record to the offline data store from the
     *  specified bytes of the byte array. The record
     *  is added at the end of the data store.
     *
     *  @param rec The byte data.
     *  @param offset The offset into the data.
     *  @param length The length of the data.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void addRecord(byte rec[], int offset, int length) throws JdbcMeException 
    {
        try
        {
            DB_.addRecord(rec, offset, length);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
    *  Set the record content to the specified bytes from the byte array.
    *
    *  @param index The record to set.  The first record is record 0.  The last record is this.size()-1;
    *  @param rec The byte data.
    *  @param offset The offset into the data.
    *  @param length The length of the data.
    *
    *  @exception JdbcMeException If an error occurs.
    **/
    public void setRecord(int index, byte rec[], int offset, int length) throws JdbcMeException 
    {
        try
        {
            // javax.microedition.rms.RecordStore first index is 1, not 0.
            DB_.setRecord(index+1, rec, offset, length);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

     /**
     *  Delete the record specified from the offline data store.
     *
     *  @param index The record to delete.  The first record is record 0.  The last record is this.size()-1;
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void deleteRecord(int index) throws JdbcMeException 
    {
        try
        {
            // javax.microedition.rms.RecordStore first index is 1, not 0.
            DB_.deleteRecord(index+1);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     * Close the offline data store, releasing resources
     * required by the platform specific data store.
     **/
    public void close()
    {
        try
        {
            DB_.closeRecordStore();
        }
        catch (Exception e)
        {
            // ignore.
        }
    }
}
