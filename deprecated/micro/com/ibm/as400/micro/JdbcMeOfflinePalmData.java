///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeOfflinePalmData.java
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
 * The offline data representation on a Palm.
 **/
class JdbcMeOfflinePalmData extends JdbcMeOfflineData
{
    private Database DB_ = null;
    
    JdbcMeOfflinePalmData(String name, int dbCreator, int dbType, boolean createIfNecessary) throws JdbcMeException 
    {
        DB_ = new com.sun.kjava.Database(dbType, dbCreator, com.sun.kjava.Database.READWRITE);
        
        if (!DB_.isOpen())
        {
            if (createIfNecessary)
            {
                boolean created = com.sun.kjava.Database.create(0, name, dbCreator, dbType, false);
                
                DB_ = new com.sun.kjava.Database (dbType, dbCreator, com.sun.kjava.Database.READWRITE);
                
                if (!DB_.isOpen())
                    throw new JdbcMeException("Couldn't create Offline Palm DB " + name, null);
                else
                    return;
            }
            throw new JdbcMeException("Can't open Offline Palm DB " + name, null);
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
        Database DB = new com.sun.kjava.Database(dbType, dbCreator, com.sun.kjava.Database.READWRITE);
        
        if (DB.isOpen())
        {
            for (int i = DB.getNumberOfRecords(); i >= 0; --i)
            {
                DB.deleteRecord(i);
            }
            
            if (DB.getNumberOfRecords() != 0)
                throw new JdbcMeException("Couldn't delete existing Offline Palm DB " + name, null);

            DB.close();
        }
        
        return new JdbcMeOfflinePalmData(name, dbCreator, dbType, true);
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
        // com.sun.kjava.Database first index is 0 as required.
        try
        {
            return DB_.getRecord(index);
        }
        catch (Exception e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

     /**
     *  Get the current number of records in the offline
     *  data store.
     *
     *  @return The number of records.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public int size() throws JdbcMeException 
    {
        try
        {
            return DB_.getNumberOfRecords();
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
            if (offset != 0 && length != rec.length)
            {
                byte  data[] = new byte[length];
                System.arraycopy(rec,offset,data,0,length);
                rec = data;
            }
            DB_.addRecord(rec);
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
            if (offset != 0 && length != rec.length)
            {
                byte  data[] = new byte[length];
                System.arraycopy(rec,offset,data,0,length);
                rec = data;
            }
            // com.sun.kjava.Database first index is 0 as required.
            DB_.setRecord(index, rec);
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
            // com.sun.kjava.Database first index is 0 as required.
            DB_.deleteRecord(index);
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
            DB_.close();
        }
        catch (Exception e)
        {
            // ignore.
        }
    }
}
