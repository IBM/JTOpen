///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeOfflineData.java
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
 * An offline data repository that represents a
 * a data store that is generic, regardless of
 * J2ME profile and JVM details.
 * <p>
 * Note that the index of the first record in a
 * JdbcMeOfflineData object is 0, not 1 as in the MIDP
 * implementation of RecordStore.
 * <p>
 * For example.<br><pre>
 *  MIDP - The 'name' is a unique string of up to 32
 *         unique characters identifying a
 *         javax.microedition.rms.RecordStore object
 *         the offline data object returned, then
 *         encapsulates the RecordStore object.
 * 
 *  PALM - The 'name' is effectively just a visual
 *         key, while the offline data is uniquely
 *         identified by the 'dbCreator' and the
 *         'dbType' parameters.
 *  </pre>
 **/
abstract public class JdbcMeOfflineData
{
    private final static int       J2ME_UNKNOWN = 0;
    private final static int       J2ME_PALM    = 1;
    private final static int       J2ME_MIDP    = 2;

    private static  int whichProfile_ = J2ME_UNKNOWN;

    static 
    {
        try
        {
            Class.forName("javax.microedition.rms.RecordStore");
            whichProfile_ = J2ME_MIDP;
        }
        catch (ClassNotFoundException e)
        {
            // Not MIDP

            // If both profiles are loaded on the device, the whichProfile_ variable
            // would always point to the PALM, which might cause problems when
            // performing database actions.  So only try to load the kjava.Database
            // if the rms.RecordStore failed.
            try
            {
                Class.forName("com.sun.kjava.Database");
                whichProfile_ = J2ME_PALM;
            }
            catch (ClassNotFoundException e2)
            {
                // Not Palm
            }
        }
    }


    /**
     *  Create an offline data repository, destroying the current one if it exists.
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *
     *  @return The specific JdbcMeOffline implementation.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public static JdbcMeOfflineData create(String name, int dbCreator, int dbType) throws JdbcMeException 
    {
        try
        {
            switch (whichProfile_)
            {
            case J2ME_PALM:
                return JdbcMeOfflinePalmData.create(name, dbCreator, dbType);
            case J2ME_MIDP:
                return JdbcMeOfflineMidpData.create(name, dbCreator, dbType);
            }
        }
        catch (JdbcMeException e)
        {
            throw e;
        }

        throw new JdbcMeException("Unsupported J2ME profile(" + whichProfile_ + ")", null);
    }


    /**
     *  Open or create an offline data repository
     *  Various parameters in this method are used or
     *  ignored depending on which J2ME profile
     *  is currently being used by this implementation.
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *  @param createIfNecessary Create the database if one has not already been created.
     *
     *  @return The specific JdbcMeOfflineData implementation.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public static JdbcMeOfflineData open(String name, int dbCreator, int dbType, boolean createIfNecessary) throws JdbcMeException 
    {
        try
        {
            switch (whichProfile_)
            {
            case J2ME_PALM:
                return new JdbcMeOfflinePalmData(name, dbCreator, dbType, createIfNecessary);
            case J2ME_MIDP:
                return new JdbcMeOfflineMidpData(name, dbCreator, dbType, createIfNecessary);
            }
        }
        catch (JdbcMeException e)
        {
            throw e;
        }

        throw new JdbcMeException("Unsupported J2ME profile (" + whichProfile_ + ")", null);
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
    abstract public byte[] getRecord(int index) throws JdbcMeException;

    /**
     *  Get the current number of records in the offline
     *  data store.
     *
     *  @return The number of records.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    abstract public int size() throws JdbcMeException;

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
    abstract public void addRecord(byte rec[], int offset, int length) throws JdbcMeException;

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
    abstract public void setRecord(int index, byte rec[], int offset, int length) throws JdbcMeException;

    /**
     *  Delete the record specified from the offline data store.
     *
     *  @param index The record to delete.  The first record is record 0.  The last record is this.size()-1;
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    abstract public void deleteRecord(int index) throws JdbcMeException;

    /**
     * Close the offline data store, releasing resources
     * required by the platform specific data store.
     **/
    abstract public void close();
}
