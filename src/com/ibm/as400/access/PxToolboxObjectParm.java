///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxToolboxObjectParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import java.util.Vector;



/**
The PxToolboxObjectParm class represents a
serialized Toolbox object parameter in a proxy datastream.  We can't
just serialize Toolbox objects, since they may contain proxy objects.
Otherwise, the contained proxy objects would be serialized along with
it, which is incorrect.
**/
class PxToolboxObjectParm 
extends PxCompDS
implements PxParm
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static boolean DEBUG_ = false;

    private Object      value_;



    public PxToolboxObjectParm ()
    { 
        super (ProxyConstants.DS_TOOLBOX_OBJECT_PARM);
    }



    public PxToolboxObjectParm (Object value)
    {
        super (ProxyConstants.DS_TOOLBOX_OBJECT_PARM);
        value_ = value;

        if (DEBUG_)
            System.out.println("Creating toolbox object parm: " + value + " (" + value.getClass() + ").");

        // First we can just serialize this object.
        addParm(new PxSerializedObjectParm(value));

        // Next we will pass along a list of all contained objects
        // (no matter how deep) which are proxified objects.
        Vector fieldNames = new Vector();
        Vector proxyImpls = new Vector();
        analyze(value, "", fieldNames, proxyImpls);

        // Send the number of contained proxy objects.
        int proxyCount = proxyImpls.size();
        addParm(new PxIntParm(proxyCount));
        
        // Send each proxy object, name then value.
        for (int i = 0; i < proxyCount; ++i) {
            addParm(new PxStringParm((String)fieldNames.elementAt(i)));
            addParm(new PxPxObjectParm(((ProxyImpl)proxyImpls.elementAt(i)).getPxId()));
        }
    }


    
    private static void analyze(Object objectValue, String prefix, Vector fieldNames, Vector proxyImpls)
    {
        if (DEBUG_) {
            System.out.println("Analyzing " + objectValue + " (" + objectValue.getClass() + ").");
            System.out.println("Prefix=" + prefix + ".");
        }

        // getDeclaredFields() is not allowed in applets.  If it throws             // @A1A
        // an exception, we will assume that there is nothing interesting           // @A1A
        // in the data.  If there is, it will potentially be lost (i.e.             // @A1A
        // not everything that works for applications will work for applets.        // @A1A
        try {                                                                       // @A1A

            // First, analyze all of our declared fields, then all of
            // our superclass's, etc.
            for (Class clazz = objectValue.getClass();
                 ! clazz.equals(Object.class);
                 clazz = clazz.getSuperclass()) 
            {
                Field[] declaredFields = clazz.getDeclaredFields();            
                if (declaredFields != null) {
                    for (int i = 0; i < declaredFields.length; ++i) {
                        String fieldName  = declaredFields[i].getName();
        
                        if (DEBUG_)
                            System.out.println("Field name=" + fieldName + ".");
        
                        // If the field is transient, then do nothing.
                        if (! Modifier.isTransient(declaredFields[i].getModifiers())) {
    
                            // Note that this check depends on the assumption that
                            // contained proxy objects be declared at least package
                            // scope (otherwise we will catch an IllegalAccessException).  
                            // I know this is not a great assumption, but
                            // it makes the implementation easier.
                            try {
                                Object fieldValue = declaredFields[i].get(objectValue);            
                                if (fieldValue != null) {
            
                                    if (DEBUG_)
                                        System.out.println("Field value=" + fieldValue + " (" + fieldValue.getClass() + ").");
                
                                    // Only be concerned with non-static Toolbox fields.
                                    if (!Modifier.isStatic(declaredFields[i].getModifiers()) 
                                        && fieldValue.getClass().getName().startsWith("com.ibm.as400.access")) {
                
                                        // If the field value is a proxy object, then add it to the list.
                                        // Otherwise, analyze recursively.
                                        if (fieldValue instanceof ProxyImpl) {
                                            fieldNames.addElement(prefix + fieldName);
                                            proxyImpls.addElement((ProxyImpl)fieldValue);
                                        }
                                        else if (fieldValue.getClass().getName().startsWith("com.ibm.as400.access"))
                                            analyze(fieldValue, fieldValue + ".", fieldNames, proxyImpls);
                                    }
                                }
                            }
                            catch (IllegalAccessException e) {
                                // Ignore.
                                if (DEBUG_)
                                    System.out.println("Ignored IllegalAccessException:" + e.getMessage());
                            }
                        }
                    }
                }
            }
        }                                                                           // @A1A
        catch (Exception e) {                                                       // @A1A
            if (Trace.isTraceErrorOn())                                             // @A1A
                Trace.log(Trace.ERROR, "Exception when analyzing Toolbox parm", e); // @A1A
        }                                                                           // @A1A
    }



/**
Returns a new copy of this datastream.

@return A new copy of this datastream.

@exception CloneNotSupportedException   If the object cannot be cloned.
**/
// 
// Implementation note:  This method is necessary in order to do 
//                       a deep copy of the internal object.  Otherwise,
//                       we run into problems with multiple threads.
    public Object clone ()
        throws CloneNotSupportedException
    {
        value_ = null;
        return super.clone();
    }



    public Object getObjectValue ()
    {
        return value_;
    }


    public void readFrom(InputStream input, PxDSFactory factory)
    throws IOException
    {
        super.readFrom(input, factory);

        // Deserialize the object.
        value_ = getParm(0).getObjectValue();

        // Number of contained objects ("corrections").
        int proxyCount = ((PxIntParm)getParm(1)).getIntValue();
        int parmIndex = 1;
        for (int i = 1; i <= proxyCount; ++i) {
            String fieldName = ((PxStringParm)getParm(++parmIndex)).getStringValue();
            Object impl = ((PxPxObjectParm)getParm(++parmIndex)).getObjectValue();

            // Set the field.
            Object fieldValue = value_;
            Field field = null;

            // Navigate down the containment hierarchy to 
            // get the right field.
            StringTokenizer tokenizer = new StringTokenizer(fieldName, ".");
            while (tokenizer.hasMoreTokens()) {

                // Get the next field to navigate to.
                String token = tokenizer.nextToken();
                Class clazz = fieldValue.getClass();
                
                try {
                    field = clazz.getDeclaredField(token);
                    if (field == null) {                       
                        // This should never happen!
                        if (Trace.isTraceErrorOn())
                            Trace.log(Trace.ERROR, "Error reading toolbox parm: field set to null");
                    }
                }
                catch(NoSuchFieldException e) {
                    // This should never happen!
                    if (Trace.isTraceErrorOn())
                        Trace.log(Trace.ERROR, "Error reading toolbox parm", e);
                }

                // Get the value of that field.                
                try {
                    if (tokenizer.hasMoreTokens())
                        fieldValue = field.get(fieldValue);
                }
                catch(IllegalAccessException e) {                    
                    // This should never happen!
                    if (Trace.isTraceErrorOn())
                        Trace.log(Trace.ERROR, "Error reading toolbox parm", e);
                }
            }

            // At this point, field is set to the field that we need to set.
            try {
                field.set(fieldValue, impl);
            }
            catch(IllegalAccessException e) {                    
                // This should never happen!
                if (Trace.isTraceErrorOn())
                    Trace.log(Trace.ERROR, "Error reading toolbox parm", e);
            }
        }
    }
                              

    public String toString ()
    {
        
        return super.toString () + " (" + value_ + ")";
    }



}
