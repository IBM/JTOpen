///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSLoadBalancer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;



/**
The PSLoadBalancer class handles load balancing
for a proxy server.
**/
class PSLoadBalancer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private PSLoad     load_   = null;
    private Vector     peers_  = null;                                  // @A1A
  


/**
Constructs a PSLoadBalancer object.

@param  load        The load.
@param  peers       The semicolon-delimied list of peers.                   
**/
    public PSLoadBalancer (PSLoad load)
    {
        load_   = load;
        peers_  = new Vector();                                         // @A1A
    }



/**
Decides whether to accept a connection from a proxy
client.

@param  rejections  The number of rejections that the
                    client has already had.
@return             null if accepted, "" if rejected
                    with no peer available, and
                    the name of a peer if rejected.                    
**/
    public String accept (int rejections)
    {
        String peer = null;

        // If the number of active connections is less
        // than the balance threshold, then accept the
        // connection.
        int activeConnections = load_.getActiveConnections ();
        if (activeConnections < load_.getEffectiveBalanceThreshold ())
            peer = null;        

        // If the number of active connections is between
        // the balance threshold and the max connections,
        // then:
        //
        // * If the request has already been rejected, then
        //   accept it, otherwise do load balancing including
        //   this server.
        else if (activeConnections < load_.getEffectiveMaxConnections ()) {
            if (rejections > 0)
                peer = null;
            else
                peer = getLeastBusyPeer (true);
        }

        // If the number of active connections is greater
        // than or equal to the max connections, then do
        // load balancing NOT including this server.
        else
            peer = getLeastBusyPeer (false);

        return peer;
    }



/**
Returns the name of the least busy peer.  This asks each
peer how busy it is.

@param includeThis  true to include this proxy server,
                    or false otherwise.
@return The name of the least busy peer, null if this
        is the least busy peer, or "" if no peers are
        available.
**/
    private String getLeastBusyPeer (boolean includeThis)
    {
        double smallestBusyFactor   = Double.MAX_VALUE;
        String leastBusyPeer        = "";
        
        // Ask each peer about its current load.
        Vector peersClone = (Vector)peers_.clone();                                 // @A1A
        Enumeration enum = peersClone.elements();                                   // @A1A
        while(enum.hasMoreElements()) {                                             // @A1C
            String peer = (String)enum.nextElement();                               // @A1A

            // Get the load from the peer.
            PSLoad load;
            try {
                PxPeerConnection peerConnection = new PxPeerConnection (peer);      // @A1A
                load = peerConnection.load ();
                peerConnection.close ();
            }
            catch(ProxyException e) {            
                Verbose.println (ResourceBundleLoader.getText("PROXY_PEER_NOT_RESPONDING", peer));  // @A1A
                peers_.removeElement(peer);                                         // @A1A
                // @A1D if (Trace.isTraceErrorOn())
                // @A1D     Trace.log(Trace.ERROR, "Peer proxy server not responding", e);
                continue;
            }

            // Determine if it is less busy than the previous. 
            double busyFactor = load.getBusyFactor ();
            if (busyFactor < smallestBusyFactor) {
                smallestBusyFactor = busyFactor;
                leastBusyPeer = peer;                                               // @A1A
            }
        }
        
        // Check this one if appropriate.
        if (includeThis) {
            if (load_.getBusyFactor () < smallestBusyFactor)
                leastBusyPeer = null;
        }

        return leastBusyPeer;
    }



/**
Returns a list of peer proxy servers for use in load balancing.

@return A list of peer proxy servers for use in load balancing.
**/
    public String[] getPeers ()
    {
        synchronized(peers_) {                                                  // @A1A
            String[] peersAsArray = new String[peers_.size()];                  // @A1A
            peers_.copyInto(peersAsArray);                                      // @A1A        
            return peersAsArray;                                                // @A1C
        }
    }



/**
Normalizes the list of peers from the String list
to an array.  Also eliminates duplicates.

@param  peers       The semicolon-delimied list of peers.                   
**/
    public void setPeers (String peers)
    {        
        StringTokenizer tokenizer = new StringTokenizer (peers, ";, ");
        int count = tokenizer.countTokens ();
        Vector temp = new Vector (count);
        while (tokenizer.hasMoreTokens ()) {
            String nextPeer = tokenizer.nextToken ();            
            if (! temp.contains (nextPeer))
                temp.addElement (nextPeer);
        }
        peers_ = (Vector)temp.clone();                                          // @A1C
    }



/**
Sets the list of peer proxy servers for use in load
balancing.  In some cases, connections to the proxy
server will be reassigned to a peer.  The default is
not to do load balancing.

@param peers    The list of peer proxy servers for 
                use in load balancing, or null to
                not do load balancing.
**/
    public void setPeers (String[] peers)
    {
        Vector temp = new Vector (peers.length);                                // @A1A
        for(int i = 0; i < peers.length; ++i) {                                 // @A1A
            if (! temp.contains(peers[i]))                                      // @A1A
                temp.addElement(peers[i]);                                      // @A1A
        }                                                                       // @A1A
        peers_ = (Vector)temp.clone();                                          // @A1C
    }




}
