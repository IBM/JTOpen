package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400CredentialListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
/**
 * The AS400CredentialListener interface provides an
 * interface for receiving AS400CredentialEvents.
 *
 */
public interface AS400CredentialListener {

/**
 * Invoked when a create has been performed.
 *
 * @param event
 *		The credential event.
 *		
 */
void created(AS400CredentialEvent event);
/**
 * Invoked when a destroy has been performed.
 *
 * @param event
 *		The credential event.
 *		
 */
void destroyed(AS400CredentialEvent event);
/**
 * Invoked when a refresh has been performed.
 *
 * @param event
 *		The credential event.
 *		
 */
void refreshed(AS400CredentialEvent event);
/**
 * Invoked when a credential has been used to change the
 * thread identity.
 *
 * @param event
 *		The credential event.
 *		
 */
void swapped(AS400CredentialEvent event);
}
