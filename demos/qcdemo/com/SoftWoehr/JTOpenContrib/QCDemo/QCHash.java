/* QCHash.java -- contain AS400 instances
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */
package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.util.*;
import com.ibm.as400.access.*;

public class QCHash  extends Object
{
  private Hashtable serverInstances = new Hashtable();

public QCHash () {}

  /** Returns a string representation of the Hash and its members.
   * @return A String representation.
   */
  public String toString() {
    StringBuffer result = new StringBuffer();
    for (Enumeration e = serverInstances.elements() ; e.hasMoreElements() ;)
    {
      result.append(e.nextElement().toString());
    }                                                            /* End for*/
    result.append(super.toString());
    return new String(result);
  }

  protected void finalize () throws Throwable
  {             /* Called by garbage collector in case no longer referenced*/
    super.finalize();
  }

  /** Clear all keys */
  public void clear () throws UnsupportedOperationException
{ serverInstances.clear(); }

  /** Map in an object */
  public AS400 put(String key, AS400 object) {
    return (AS400) serverInstances.put(key, object);
  }

  /** Get a server by name */
  public AS400 get(String key) {
    return (AS400) serverInstances.get(key);
  }

  /** Remove a server by name */
  public AS400 remove(String key) {
    return (AS400) serverInstances.remove(key);
  }

  /** Contains the server named? */
  public boolean containsServerNamed (String key) {
    return serverInstances.containsKey(key);
  }

  /** Contains reference to the identical unique AS400 object */
  public boolean containsServer(AS400 as400) {
    return serverInstances.containsValue(as400);
  }

  /** Number of elements */
public int size () { return serverInstances.size();}

  /** Enumeration of keys */
public Enumeration keys () { return serverInstances.keys();}

  /** Enumeration of elements elements */
public Enumeration elements () { return serverInstances.elements();}

  /** Is empty? */
public boolean isEmpty () { return serverInstances.isEmpty();}

  /** Test QCHash */
  public static void main (String argv[]) {
    AS400 moe = new AS400("MOE");
    AS400 joe = new AS400("JOE");

    QCHash qchash = new QCHash();

    System.out.println("Adding MOE");
    qchash.put("Moe", moe);

    System.out.println("Adding JOE");
    qchash.put("Joe", joe);

    System.out.println("Does the QCHash contain Moe?");
    if (qchash.containsServerNamed("Moe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                             /* End if*/

    System.out.println("Does the QCHash contain Joe?");
    if (qchash.containsServerNamed("Joe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                             /* End if*/

    System.out.println("Joe as string:");
    System.out.println(qchash.get("Joe").toString());

    System.out.println("Moe as string:");
    System.out.println(qchash.get("Moe").toString());

    System.out.println("QCHash to string.");
    System.out.println(qchash.toString());

    System.out.println("Removing Moe");
    qchash.remove("Moe");

    System.out.println("Does the QCHash contain Moe?");
    if (qchash.containsServerNamed("Moe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                             /* End if*/

    System.out.println("Does the QCHash contain Joe?");
    if (qchash.containsServerNamed("Joe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                            /* End if*/

    System.out.println("Removing Joe");
    qchash.remove("Joe");

    System.out.println("Does the QCHash contain Moe?");
    if (qchash.containsServerNamed("Moe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                            /* End if*/

    System.out.println("Does the QCHash contain Joe?");
    if (qchash.containsServerNamed("Joe"))
    {
      System.out.println("Yes.");
    }
    else
    {
      System.out.println("No.");
    }                                                            /* End if*/

    System.out.println("");
    System.out.println("");
    System.out.println("");
    System.out.println("");

  }
}                                                    /* End of QCHash class*/
