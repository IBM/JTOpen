///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400ObjectFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.util.Hashtable;
import javax.naming.Context;           // JNDI
import javax.naming.Name;              // JNDI
import javax.naming.Reference;         // JNDI
import javax.naming.spi.ObjectFactory; // JNDI

/**
 * The AS400ObjectFactory is used by a JNDI service provider to reconstruct an
 * object when it is retrieved from JNDI.
 *
 * <p>
 * For Example:
 * <pre>
 * AS400ConnectionPool pool = null;
 * String objFactoryName = "com.ibm.as400.access.AS400ObjectFactory";
 * String className = "com.ibm.as400.access.AS400ConnectionPool";
 * Reference ref = new Reference(className, objFactoryName, "");
 * ref.add(new StringRefAddr("maxConnections", "10"));
 * try {
 *     ObjectFactory objectFactory = (ObjectFactory) Class.forName(objFactoryName).newInstance();
 *     pool = (AS400ConnectionPool) objectFactory.getObjectInstance(ref, null, null, null);
 *     AS400 as400 = pool.getConnection("myAS400", "myUserID", "myPassword".toCharArray());
 * } catch (Exception ex) {
 *     ex.printStackTrace();
 *     System.err.println("Exception caught: " + ex);
 * }
 * </pre>
 * The following classes implement the javax.naming.Referenceable interface.
 *
 * @see com.ibm.as400.access.AS400ConnectionPool
 *
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
public class AS400ObjectFactory implements ObjectFactory {

    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
     * Returns the object requested.
     *
     * @param referenceObject The object reference.
     * @param name The object name.
     * @param nameContext The context of the name.
     * @param environment The environment.
     * @return The object requested.
     * @exception Exception If an error occurs during object creation.
     *
     */
    @Override
    public Object getObjectInstance(Object referenceObject,
            Name name,
            Context nameContext,
            Hashtable<?, ?> environment) throws Exception {
        Reference reference = (Reference) referenceObject;

        if (reference.getClassName().equals(AS400ConnectionPool.class.getName())) {
            return new AS400ConnectionPool(reference);
        } else {
            if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this, "Lookup error. Class not found: " + reference.getClassName());
            }
            return null;
        }
    }
}
