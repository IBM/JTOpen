///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPSystem.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

/**
  *NPSystem class - this class is used to represent systems from a network
  * print perspective.  There is ONE instance of this class for each system that
  * you are interested in.  There is a static method to get at the one instance
  * of this class based on the system name.
  * Use this instance to get AS400Server objects (these are the conversations to
  * this system).
  **/

class NPSystem extends Object implements Runnable
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // static data members
    private static final int CONVERSATION_TIMEOUT = 300000;         // 5 minutes in milleseconds
    private static Hashtable NPSystemTable_ = new Hashtable();

    // private members
    private AS400ImplRemote as400System_;
    private Vector availConversations_, inuseConversations_, deadConversations_;
    private Thread cleanupThread_;

    // The cleanup thread is only started if the AS400 object containing
    // the AS400Server objects allows threads to be started. EJB does not
    // allow objects to start threads.

   /**
     * private constructor - use the static getSystem() method to create one of these
     **/
    private NPSystem(AS400ImplRemote aSystem)
    {
        as400System_ = aSystem; 
        availConversations_ = new Vector();
        inuseConversations_ = new Vector();
        deadConversations_  = new Vector();
    }

    // protected members & methods

    //  public members & methods


    /**
     * static method to look up THE NPSystem for this server
     */
    static NPSystem getSystem(AS400ImplRemote aSystem)
    {
        NPSystem npSystem = null;
        // look for this NPSystem in the static hash table using the system
        // object as a hash key
        // We used to hash by system name string(using getSystemName()) but
        // that doesn't work quite right for 2 reasons:
        //   1. The system name might be null (user gets to fill in when we
        //       connect for the first time
        //   2. The user can create 2 AS400 objects to the same system name
        //       as a way to create more conversations to that system.
        // So, we now use the base hashCode() for the AS400 system as the key...
        // npSystem = (NPSystem)NPSystemTable.get(aSystem.getSystemName());
        npSystem = (NPSystem)NPSystemTable_.get(aSystem);
        if (npSystem == null)
        {
            // first time we've used this system - create it
            npSystem = new NPSystem(aSystem);
            NPSystemTable_.put(aSystem, npSystem);
        }
        return npSystem;
    }


    /**
     * Method to get an NPConversation (wrapper for AS400Server) for this system.
     * If you use this method you must be sure to return the conversation when you
     * are done with it by using returnConversation().  If you just want to make a
     * simple request (1 datastream up and 1 for the reply) then use the makeRequest()
     * method instead.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If security violation occurs during connection.
     * @exception ErrorCompletingRequestException If an error occurred on the server.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     **/
    synchronized NPConversation getConversation()
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException

    // note - this method needs to be syncrhonized to protect
    // the 3 vectors of conversations.

    {
        NPConversation conversation = null;
        // The loop is here to check if the conversation is connected,
        // if not we go back and get another one from our available or
        // dead list.
        do
	{
            if( availConversations_.isEmpty() )
            {
                // there are no available conversations - see if there
                // are any in the dead list that can be resurrected.
                if( !deadConversations_.isEmpty() )
                {
                    conversation = (NPConversation)deadConversations_.firstElement();
                    deadConversations_.removeElement(conversation);
                } else {
                    // none in the dead list either -create a new one
                    AS400Server server;
                    //--------------------------------------------------------
                    // no available conversations, create a new one
                    // If this is the first conversation we are getting we'll
                    // use the connect() method, if we are getting additional
                    // conversations on the same AS400 object we use the
                    // getNewConnection() method
                    //--------------------------------------------------------
                    if (inuseConversations_.isEmpty())
                    {
                        server = as400System_.getConnection(AS400.PRINT, false);
                       // server = as400System_.connect("as-netprt");
                    } else {
                        server = as400System_.getConnection(AS400.PRINT, true);
                       // server = as400System_.getNewConnection("as-netprt");

                        // Only start the cleanup thread if more than       
                        // one conversation has been started, -AND-          
                        // the system object is configured to start            
                        // threads.                                         

                        if( as400System_.isThreadUsed() )
                        {
                            startCleanupThread();
                        }
                    }

                    conversation = new NPConversation(as400System_, server);
                }
            } else {
                conversation = (NPConversation)availConversations_.firstElement();
                availConversations_.removeElement(conversation);
            }

            if( !(conversation.getServer().isConnected()) )
            {
                 Trace.log(Trace.DIAGNOSTIC, "Conversation was not connected.");
            }

        }
        while( !(conversation.getServer().isConnected()) );

        // add this conversation to the inuse vector.
        inuseConversations_.addElement(conversation);

        return conversation;
    }


   /**
     * method to return a NPConversation to the available list
     **/
   synchronized void returnConversation(NPConversation conversation)
   {
      // remove conversation from inuse vector.

      // If the conversation is still connected, and the AS400 object       
      // allows starting threads, add the conversation to the inuse
      // vector. Method cleanUpDeadConversations() will determine how 
      // long conversations are cached.

      // If the conversation is still connected, but the AS400 object       
      // does not allow starting threads, keep at least one conversation    
      // in the inuse or available vector.                                  

      int index = inuseConversations_.indexOf(conversation);
      if (index != -1)
      {
         // remove conversation from inuse vector.
         inuseConversations_.removeElementAt(index);

         // if still connected
         if( conversation.getServer().isConnected() )
         {
            // does AS400 object allow starting threads?                    
            if( as400System_.isThreadUsed() )                            
            {
               // clean up thread is running,
               // add conversation to available.
               availConversations_.addElement(conversation);
            } else {                                                     
               // clean up thread can not be run,                           
               // just keep one conversation available.                     
               if( availConversations_.size() < 1 )                      
               {                                                         
                  availConversations_.addElement(conversation);          
               } else {                                                  
                  as400System_.disconnectServer(conversation.getServer());
               }
            }
         }
      }
   }


    /**
      * get a conversation, make the single request and return
      * the conversation.
      * @exception AS400Exception If the server returns an error message.
      * @exception AS400SecurityException If security violation occurs during connection.
      * @exception ErrorCompletingRequestException If an error occurred on the server.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/
    int makeRequest(NPDataStream request, NPDataStream reply)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException
    {
        NPConversation conversation = getConversation();
        int rc;
        try
        {
           rc = conversation.makeRequest(request, reply);
        }
        catch (RequestNotSupportedException e) {
            throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
        }
        finally
        {
            // if we succeed or not we must return the conversation always
           returnConversation(conversation);
        }
        return rc;
    }


    // A2A New version of cleanUpDeadConversations (old version appears after)
    /**
      * private method called by background thread to remove any
      * conversations that we haven't used for while (since the
      * last time this function was called).
      * at least one existing conversation (dead, available, or in use) is left
      * @return true if there is more than 1 active conversations alive;
      *          false otherwise
      **/
    private synchronized boolean cleanUpDeadConversations()
    {
        // temporarily leave a dead conversation around (if one exists)...
        int numberOfConversationsToDestroy = deadConversations_.size() - 1;

        // determine the number of active conversations
        int activeConversations = inuseConversations_.size() + availConversations_.size();

        if (activeConversations > 0) {
            // At least one conversation exists, so destroy ALL dead conversations
            numberOfConversationsToDestroy++;
        }

        // Cycle through the deadConversation_ vector, eliminating conversations
        Enumeration e = deadConversations_.elements();
        for (int i = 0; i < (numberOfConversationsToDestroy); i++) {
             NPConversation conv = (NPConversation)e.nextElement();
             if (conv != null) {
                as400System_.disconnectServer(conv.getServer());
             }
        }

        if (e.hasMoreElements()) { // we left one dead conversation around
            availConversations_.addElement((NPConversation)e.nextElement());
        }

        // all available conversations become dead
        deadConversations_ = availConversations_;

        // start a new vector for available conversations
        availConversations_ = new Vector();

        return (boolean) (activeConversations > 1);
    }


    /*  A2D This version replaced 2/23/99
    /**
      * private method called by background thread to remove any
      * conversations that we haven't used for while (since the
      * last time this function was called).
      * @return true if there are more conversations alive
      *          false if all conversations are now gone
      **/
    /*
    private synchronized boolean cleanUpDeadConversations()
    {
        boolean fMoreConversations, fKillingConversations;

        //
        // for all NPConversations in the deadConversations list
        // drop the connection for each conversation (go through the
        // AS400 object to do this)
        //
        for (Enumeration e = deadConversations_.elements(); e.hasMoreElements();)
        {
             NPConversation conv = (NPConversation)e.nextElement();
             if (conv != null)
             {
                as400System_.disconnectServer(conv.getServer());
             }
        }
        deadConversations_ = availConversations_;
        availConversations_ = new Vector();
        fMoreConversations = !(deadConversations_.isEmpty()) || !(inuseConversations_.isEmpty());
        return fMoreConversations;
    }*/


    /**
       run() method for background thread
     **/
    public void run()
    {
        try
        {
           boolean fMoreConversations;
           do
           {
              java.lang.Thread.sleep(CONVERSATION_TIMEOUT);
               fMoreConversations = cleanUpDeadConversations();
           } while (fMoreConversations);

        }
        catch (InterruptedException e)
        {
            // someone interupted us,  we'll just end here
        }
        finally
        {

        }

        // return from the run() method to end the thread
    }


    /**
      Starts the clean up thread (if not already started).  There
      is one and only one read daemon thread per instance of the
      NPServer and it should be running while we have any AS400Servers
      open (any conversations).

      @see NPServer#run
    **/
    synchronized void startCleanupThread()
    {
      if (cleanupThread_ == null)
      {
        cleanupThread_ = new Thread(this);
        cleanupThread_.setDaemon(true);
        cleanupThread_.start();
      }
    }


    /**
      Stops the cleanup thread (if it is running).
        @see NPServer#run
        @see NPServer#startReadDaemon
    **/
    synchronized void stopCleanupThread()
    {
        if (cleanupThread_ != null)
        {
            cleanupThread_.stop();
            cleanupThread_ = null;
        }
    }

} // NPSystem class


