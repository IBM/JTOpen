package com.ibm.as400.access.jdbcClient;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;

public class ReflectionUtil {
  static Object dummyObject = null; 
  static Class objectClass = null;
  static {
dummyObject = new Object();
objectClass = dummyObject.getClass(); 
  } 

  public static void handleIte(java.lang.reflect.InvocationTargetException ite) throws Exception  {
Throwable target = ite.getTargetException();
if (target instanceof Exception) { 
    throw (Exception) target; 
} else {
    target.printStackTrace();
    throw new Exception("Throwable "+target.toString()+" encountered.  See STDOUT for stack trace ");
} 

  }

  /**
   * call a method which returns an Object
   *
   * Examples
   *
   *  Object o = callMethod_O(ds, ... "); 
   */ 
   public static Object callMethod_O(Object o, String methodName, Class argType, Object p1 ) throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      // System.out.println("Class of object is "+thisClass); 
      Class argTypes[] = new Class[1]; 
      argTypes[0] = argType; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[1];
      args[0] = p1;
      try {
          // System.out.println("Calling method"); 
          Object outObject = method.invoke(o, args);
          // System.out.println("outObject is "+outObject); 
          return  outObject; 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return ""; 
      } 


   } 


  
 /**
  * call a method which returns an Object
  *
  * Examples
  *
  *  Object o = callMethod_O(ds, ... "); 
  */ 
  public static Object callMethod_O(Object o, String methodName, Class[] argTypes, Object p1, Object p2 ) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 

method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[2];
args[0] = p1;
args[1] = p2;
try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return  outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 


  } 


 /**
  * call a method which returns an Object
  *
  * Examples
  *
  *  Object o = callMethod_O(ds, ... "); 
  */ 
  public static Object callMethod_O(Object o, String methodName, Class[] argTypes, Object p1, Object p2, Object p3 ) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 

method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
  
Object[] args = new Object[3];
args[0] = p1;
args[1] = p2;
args[2] = p3;
try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 


  } 


 /**
  * call a method which returns an Object
  *
  * Examples
  *
  *  Object o = callMethod_O(ds, ... "); 
  */ 
  public static Object callMethod_O(Object o, String methodName, Class[] argTypes, Object p1, Object p2, Object p3, Object p4 ) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 

method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[4];
args[0] = p1;
args[1] = p2;
args[2] = p3;
args[3] = p4; 
try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 


  } 

 /**
  * call a method which returns an Object
  *
  * Examples
  *
  *  Object o = callMethod_O(ds, ... "); 
  */ 
  public static Object callMethod_O(Object o, String methodName)  throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 
Class[] argTypes = new Class[0];
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[0];
try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 

  } 

  
  /**
   * call a method which returns an Object
   *
   * Examples
   *
   *  Object o = callMethod_O(ds, ... "); 
   */ 
   public static Object callMethod_O(Object o, String methodName, int i )  throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      // System.out.println("Class of object is "+thisClass); 
      Class[] argTypes = new Class[1];
      argTypes[0] = Integer.TYPE ;
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[1];
      args[0] = new Integer(i); 
      try {
          // System.out.println("Calling method"); 
          Object outObject = method.invoke(o, args);
          // System.out.println("outObject is "+outObject); 
          return outObject; 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return ""; 
      } 

   } 


 


 /**
  * call a method which returns an Object
  *
  * Examples
  *
  *  Object o = callMethod_O(ds, ... "); 
  */ 
  public static Object callMethod_O(Object o, String methodName, Class c)  throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 
Class[] argTypes = new Class[1];
argTypes[0] = Class.forName("java.lang.Class"); 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = c;

try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 

  } 

  /**
   * call a method which returns an Object
   *
   * Examples
   *
   *  Object o = callMethod_O(ds, ... "); 
   */ 
   public static Object callMethod_OS(Object o, String methodName, String s)  throws Exception {

      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      // System.out.println("Class of object is "+thisClass); 
      Class[] argTypes = new Class[1];
      argTypes[0] = Class.forName("java.lang.String"); 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[1];
      args[0] = s;

      try {
          // System.out.println("Calling method"); 
          Object outObject = method.invoke(o, args);
          // System.out.println("outObject is "+outObject); 
          return outObject; 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return ""; 
      } 

   } 

   public static Object callMethod_OSS(Object o, String methodName, String s1, String s2)  throws Exception {

     java.lang.reflect.Method method;

     Class thisClass = o.getClass();
     // System.out.println("Class of object is "+thisClass); 
     Class[] argTypes = new Class[2];
     argTypes[0] = Class.forName("java.lang.String");
     argTypes[1] = argTypes[0]; 
     method = thisClass.getMethod(methodName, argTypes); 
     method.setAccessible(true); //allow toolbox proxy methods to be invoked
     Object[] args = new Object[2];
     args[0] = s1;
     args[1] = s2;
     try {
         // System.out.println("Calling method"); 
         Object outObject = method.invoke(o, args);
         // System.out.println("outObject is "+outObject); 
         return outObject; 
     } catch (java.lang.reflect.InvocationTargetException ite) {
         handleIte(ite);
         return ""; 
     } 

  } 

   //created for Connection.createArrayOf()
   public static Object callMethod_OSA(Object o, String methodName, String s1, Object[] o2)  throws Exception {

     java.lang.reflect.Method method;

     Class thisClass = o.getClass();
     // System.out.println("Class of object is "+thisClass); 
     Class[] argTypes = new Class[2];
     argTypes[0] = Class.forName("java.lang.String");
     argTypes[1] = Object[].class; //parm type is Object[]
     
     method = thisClass.getMethod(methodName, argTypes); 
     method.setAccessible(true); //allow toolbox proxy methods to be invoked
     Object[] args = new Object[2];
     args[0] = s1;
     args[1] = o2;
     try {
         // System.out.println("Calling method"); 
         Object outObject = method.invoke(o, args);
         // System.out.println("outObject is "+outObject); 
         return outObject; 
     } catch (java.lang.reflect.InvocationTargetException ite) {
         handleIte(ite);
         return ""; 
     } 

  } 
   
   public static Object callMethod_OSSS(Object o, String methodName, String s1, String s2, String s3)  throws Exception {

     java.lang.reflect.Method method;

     Class thisClass = o.getClass();
     // System.out.println("Class of object is "+thisClass); 
     Class[] argTypes = new Class[3];
     argTypes[0] = Class.forName("java.lang.String");
     argTypes[1] = argTypes[0]; 
     argTypes[2] = argTypes[0]; 
     method = thisClass.getMethod(methodName, argTypes); 
     method.setAccessible(true); //allow toolbox proxy methods to be invoked
     Object[] args = new Object[3];
     args[0] = s1;
     args[1] = s2;
     args[2] = s3;
     try {
         // System.out.println("Calling method"); 
         Object outObject = method.invoke(o, args);
         // System.out.println("outObject is "+outObject); 
         return outObject; 
     } catch (java.lang.reflect.InvocationTargetException ite) {
         handleIte(ite);
         return ""; 
     } 

  } 

   public static Object callMethod_OSSSS(Object o, String methodName, String s1, String s2, String s3, String s4)  throws Exception {

     java.lang.reflect.Method method;

     Class thisClass = o.getClass();
     // System.out.println("Class of object is "+thisClass); 
     Class[] argTypes = new Class[4];
     argTypes[0] = Class.forName("java.lang.String");
     argTypes[1] = argTypes[0]; 
     argTypes[2] = argTypes[0]; 
     argTypes[3] = argTypes[0]; 
     method = thisClass.getMethod(methodName, argTypes); 
     method.setAccessible(true); //allow toolbox proxy methods to be invoked
     Object[] args = new Object[4];
     args[0] = s1;
     args[1] = s2;
     args[2] = s3;
     args[3] = s4;
     try {
         // System.out.println("Calling method"); 
         Object outObject = method.invoke(o, args);
         // System.out.println("outObject is "+outObject); 
         return outObject; 
     } catch (java.lang.reflect.InvocationTargetException ite) {
         handleIte(ite);
         return ""; 
     } 

  } 


   
   
   /**
    * call a method which returns an Object
    *
    * Examples
    *
    *  Object o = callMethod_O(ds, ... "); 
    */ 
    public static Object callMethod_O(Object o, String methodName, String s, Object parm2)  throws Exception {

       java.lang.reflect.Method method;

       Class thisClass = o.getClass();
       // System.out.println("Class of object is "+thisClass); 
       Class[] argTypes = new Class[2];
       argTypes[0] = Class.forName("java.lang.String"); 
       method = null; 
       Object[] args = new Object[2];
       args[0] = s;
       args[1] = parm2; 
       
       
       if (parm2 == null) {
         throw new Exception("Unable to handle null parameter");
       }
       Class checkClass = parm2.getClass();
       String tryArgs = "";
       while (method == null && checkClass != null) {
         try {
           tryArgs += "(Ljava/lang/String;" + checkClass.getName() + ") ";
           argTypes[1] = checkClass;
           method = thisClass.getMethod(methodName, argTypes);
           method.setAccessible(true); //allow toolbox proxy methods to be invoked

         } catch (Exception e) {
        
         }
         if (method == null) {
           if (checkClass.getName().equals("java.lang.Object")) {
             checkClass = null;

           } else {
             checkClass = checkClass.getSuperclass();
           }

         } else {
           // System.out.println("Found method "+method); 
         }
       }

       if (checkClass == null) {
         checkClass = parm2.getClass();
         // Did not find a base class.. Now look for implements
         while (method == null && checkClass != null) {
           try {

             // Find the implementes for the class
             Class[] interfaces = checkClass.getInterfaces();
             for (int j = 0; method == null && j < interfaces.length; j++) {
               tryArgs += "(Ljava/lang/String;" + interfaces[j].getName() + ") ";
   argTypes[1] = interfaces[j];
   try { 
       method = thisClass.getMethod(methodName, argTypes);
       method.setAccessible(true); //allow toolbox proxy methods to be invoked
   } catch (NoSuchMethodException e) {
       // keep going 
   } 
             }
           } catch (Exception e) {
          
             e.printStackTrace();
           }
           if (method == null) {
             if (checkClass.getName().equals("java.lang.Object")) {
               checkClass = null;

             } else {
               checkClass = checkClass.getSuperclass();
             }
           } else {
             // System.out.println("Found method "+method); 
           }
           
         }

       }

       if (method == null) { 
         throw new Exception("Unable to find method:  tried "+tryArgs); 
       }


       try {
           // System.out.println("Calling method"); 
           Object outObject = method.invoke(o, args);
           // System.out.println("outObject is "+outObject); 
           return outObject; 
       } catch (java.lang.reflect.InvocationTargetException ite) {
           handleIte(ite);
           return ""; 
       } 

    } 

    /**
     * call a method which returns an Object
     *
     * Examples
     *
     *  Object o = callMethod_O(ds, ... "); 
     */ 
     public static Object callMethod_O(Object o, String methodName, long i, long j )  throws Exception {
        java.lang.reflect.Method method;

        Class thisClass = o.getClass();
        // System.out.println("Class of object is "+thisClass); 
        Class[] argTypes = new Class[2];
        argTypes[0] = Long.TYPE ;
        argTypes[1] = Long.TYPE ;
        method = thisClass.getMethod(methodName, argTypes); 
        method.setAccessible(true); //allow toolbox proxy methods to be invoked
        Object[] args = new Object[2];
        args[0] = new Long(i); 
        args[1] = new Long(j); 
        try {
            // System.out.println("Calling method"); 
            Object outObject = method.invoke(o, args);
            // System.out.println("outObject is "+outObject); 
            return outObject; 
        } catch (java.lang.reflect.InvocationTargetException ite) {
            handleIte(ite);
            return ""; 
        } 

     } 





 /**
  * call a method which returns a string
  *
  * Examples
  *
  *  String property = callMethod_S(ds, "getTranslateHex"); 
  */ 
  public static String callMethod_S(Object o, String methodName) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
// System.out.println("Class of object is "+thisClass); 
Class[] argTypes = new Class[0];
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[0];
try {
    // System.out.println("Calling method"); 
    Object outObject = method.invoke(o, args);
    // System.out.println("outObject is "+outObject); 
    return (String) outObject; 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return ""; 
} 


  } 

  /**
   * call a method which returns a string
   *
   * Examples
   *
   *  String property = callMethod_S(ds, "getTranslateHex"); 
   */ 
   public static String callMethod_S(Object o, String methodName, int i) throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      // System.out.println("Class of object is "+thisClass); 
      Class[] argTypes = new Class[1];
      argTypes[0] = java.lang.Integer.TYPE; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[1];
      args[0] = new Integer(i); 
      try {
          // System.out.println("Calling method"); 
          Object outObject = method.invoke(o, args);
          // System.out.println("outObject is "+outObject); 
          return (String) outObject; 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return ""; 
      } 


   } 

  
   
   
   
  /**
   * call a method which returns a string
   *
   * Examples
   *
   *  String property = JDReflectionUtil.callMethod_S(outNClob, "getSubString", 1, outLength); 
   */ 
   public static String callMethod_S(Object o, String methodName, long l, int j) throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      // System.out.println("Class of object is "+thisClass); 
      Class[] argTypes = new Class[2];
      argTypes[0] = java.lang.Long.TYPE; 
      argTypes[1] = java.lang.Integer.TYPE; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[2];
      args[0] = new Long(l); 
      args[1] = new Integer(j); 
      try {
          // System.out.println("Calling method"); 
          Object outObject = method.invoke(o, args);
          // System.out.println("outObject is "+outObject); 
          return (String) outObject; 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return ""; 
      } 


   } 

  
 /**
  * call a method which returns an integer
  *
  * Examples
  *
  *  int value = callMethod_I(ds, "getMaximumPrecision"); 
  */ 
  public static int callMethod_I(Object o, String methodName) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[0];
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[0];
try { 
    Integer i = (Integer) method.invoke(o, args); 
    return i.intValue();
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return 0; 
} 

  }

 /**
  * call a method which returns an integer
  *
  * Examples
  *
  *  int value = callMethod_I(ds, "getMaximumPrecision"); 
  */ 
  public static int callMethod_I(Object o, String methodName, int parm) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[1];
argTypes[0] = java.lang.Integer.TYPE; 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = new Integer(parm);
try { 
    Integer i = (Integer) method.invoke(o, args); 
    return i.intValue();
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return 0; 
} 

  }

  public static int callMethod_I(Object o, String methodName, Object parm1) throws Exception {
    java.lang.reflect.Method method = null;

    Class thisClass = o.getClass();
    Class[] argTypes = new Class[1];

    if (parm1 == null) {
      throw new Exception("Unable to handle null parameter");
    }
    Class checkClass = parm1.getClass();
    String tryArgs = "";
    while (method == null && checkClass != null) {
      try {
        tryArgs += "(" + checkClass.getName() + ") ";
        argTypes[0] = checkClass;
        method = thisClass.getMethod(methodName, argTypes);
        method.setAccessible(true); //allow toolbox proxy methods to be invoked

      } catch (Exception e) {
     
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

    if (checkClass == null) {
      checkClass = parm1.getClass();
      // Did not find a base class.. Now look for implements
      while (method == null && checkClass != null) {
        try {

          // Find the implementes for the class
          Class[] interfaces = checkClass.getInterfaces();
          for (int j = 0; method == null && j < interfaces.length; j++) {
            tryArgs += "(" + interfaces[j].getName() + ") ";
            argTypes[0] = interfaces[j];
      try { 
    method = thisClass.getMethod(methodName, argTypes);
    method.setAccessible(true); //allow toolbox proxy methods to be invoked
      } catch (NoSuchMethodException e) {
      } 
          }
        } catch (Exception e) {
       
          e.printStackTrace();
        }
        if (method == null) {
          if (checkClass.getName().equals("java.lang.Object")) {
            checkClass = null;

          } else {
            checkClass = checkClass.getSuperclass();
          }

        }
      }

    }

    if (method == null) { 
      throw new Exception("Unable to find method:  tried "+tryArgs); 
    }
    Object[] args = new Object[1];
    args[0] = parm1; 
    try {
      Integer i = (Integer) method.invoke(o, args);
      return i.intValue(); 
    } catch (java.lang.reflect.InvocationTargetException ite) {
      handleIte(ite);

    }
    return 0; 

  } 


  
  /**
   * call a method which returns an long
   *
   * Examples
   *
   *  int value = callMethod_L(ds, "length"); 
   */ 
   public static long callMethod_L(Object o, String methodName) throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      Class[] argTypes = new Class[0];
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[0];
      try { 
          Long l = (Long) method.invoke(o, args); 
          return l.longValue();
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return 0; 
      } 

   }



 /**
  * call a method which returns a boolean
  *
  * Examples
  *
  *  boolean value = callMethod_B(ds, "getReturnExtendedMetaData"); 
  */ 
  public static boolean callMethod_B(Object o, String methodName) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[0];
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[0];
try { 
    Boolean b = (Boolean) method.invoke(o, args); 
    return b.booleanValue();
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return false; 
} 

  } 


  /**
   * call a method which returns a boolean
   *
   * Examples
   *
   *  boolean value = callMethod_B(connection, "isValid", 60)
   *  
   */ 
   public static boolean callMethod_B(Object o, String methodName, int i ) throws Exception {
      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      Class[] argTypes = new Class[1];
      argTypes[0] = Integer.TYPE; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[1];
      args[0] = new Integer(i);  
      try { 
          Boolean b = (Boolean) method.invoke(o, args); 
          return b.booleanValue();
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          return false; 
      } 
   } 

  
 /**
  * call a method which returns a boolean
  *
  * Examples
  *
  *  boolean value = callMethod_B(ds, "isWrapperFor", Class.forName("java.lang.String"); 
  */ 
  public static boolean callMethod_B(Object o, String methodName, Class x ) throws Exception {
java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[1];
argTypes[0] = Class.forName("java.lang.Class"); 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = x; 
try { 
    Boolean b = (Boolean) method.invoke(o, args); 
    return b.booleanValue();
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    return false; 
} 
  } 

  public static boolean callMethod_B(Object o, String methodName, Object parm1) throws Exception {
    java.lang.reflect.Method method = null;

    Class thisClass = o.getClass();
    Class[] argTypes = new Class[1];

    if (parm1 == null) {
      throw new Exception("Unable to handle null parameter");
    }
    Class checkClass = parm1.getClass();
    String tryArgs = "";
    while (method == null && checkClass != null) {
      try {
        tryArgs += "(" + checkClass.getName() + ") ";
        argTypes[0] = checkClass;
        method = thisClass.getMethod(methodName, argTypes);
        method.setAccessible(true); //allow toolbox proxy methods to be invoked

      } catch (Exception e) {
     
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

    if (checkClass == null) {
      checkClass = parm1.getClass();
      // Did not find a base class.. Now look for implements
      while (method == null && checkClass != null) {
        try {

          // Find the implementes for the class
          Class[] interfaces = checkClass.getInterfaces();
          for (int j = 0; method == null && j < interfaces.length; j++) {
            tryArgs += "(" + interfaces[j].getName() + ") ";
            argTypes[0] = interfaces[j];
      try { 
    method = thisClass.getMethod(methodName, argTypes);
    method.setAccessible(true); //allow toolbox proxy methods to be invoked
      } catch (NoSuchMethodException e) {
       // keep going 
      } 

          }
        } catch (Exception e) {
       
          e.printStackTrace();
        }
        if (method == null) {
          if (checkClass.getName().equals("java.lang.Object")) {
            checkClass = null;

          } else {
            checkClass = checkClass.getSuperclass();
          }

        }
      }

    }

    if (method == null) { 
      throw new Exception("Unable to find method:  tried "+tryArgs); 
    }
    Object[] args = new Object[1];
    args[0] = parm1; 
    try {
      Boolean b = (Boolean) method.invoke(o, args);
      return b.booleanValue(); 
    } catch (java.lang.reflect.InvocationTargetException ite) {
      handleIte(ite);

    }
    return false; 

  } 



 /**
  * call a method which returns nothing.  The parameter types and values are passed. 
  *
  * Examples
  *
  *  callMethod_V(ds, "setReturnExtendedMetaData", argTypes, args); 
  */

  public static void callMethod_V(Object o, String methodName, Class[] argTypes, Object[] args  ) throws Exception {
java.lang.reflect.Method method;
      Class thisClass = o.getClass();

method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
try { 
    method.invoke(o, args);
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite); 
} 
  } 




 /**
  * call a method which returns nothing and is passed nothing 
  *
  * Examples
  *
  *  callMethod_V(ds, "close"); 
  */

  public static void callMethod_V(Object o, String methodName) throws Exception {

java.lang.reflect.Method method;
      Class thisClass = o.getClass();

Class[] argTypes = new Class[0];
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[0];
try { 
    method.invoke(o, args); 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

} 
  } 



 /**
  * call a method which returns nothing, but is passed an int
  *
  * Examples
  *
  *  callMethod_V(ds, "setMaximumPrecision", 34); 
  */

  public static void callMethod_V(Object o, String methodName, int parm1) throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[1];
argTypes[0] = java.lang.Integer.TYPE; 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = new Integer(parm1);
try { 
    method.invoke(o, args);
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite); 
} 

  }

  /**
   * call a method which returns nothing, but is passed an int and a string  String
   *
   * Examples
   *
   *  callMethod_V(ps, "setNString", 1, "character"); 
   */

   public static void callMethod_V(Object o, String methodName, int parm1, String parm2) throws Exception {

      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      Class[] argTypes = new Class[2];
      argTypes[0] = java.lang.Integer.TYPE;
      argTypes[1] = String.class;  
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[2];
      args[0] = new Integer(parm1);
      args[1] = parm2; 
      try { 
          method.invoke(o, args); 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);

      } 
   } 

   /**
    * call a method which returns nothing, but is passed an string and a string  String
    *
    * Examples
    *
    *  callMethod_V(ps, "setNString", "col1", "character"); 
    */

    public static void callMethod_V(Object o, String methodName, String parm1, String parm2) throws Exception {

       java.lang.reflect.Method method;

       Class thisClass = o.getClass();
       Class[] argTypes = new Class[2];
       argTypes[0] = String.class; 
       argTypes[1] = String.class;  
       method = thisClass.getMethod(methodName, argTypes); 
       method.setAccessible(true); //allow toolbox proxy methods to be invoked
       Object[] args = new Object[2];
       args[0] = parm1;
       args[1] = parm2; 
       try { 
           method.invoke(o, args); 
       } catch (java.lang.reflect.InvocationTargetException ite) {
           handleIte(ite);

       } 
    } 

  
  
  
 /**
  * call a method which returns nothing, but is passed an boolean
  *
  * Examples
  *
  *  callMethod_V(ds, "setReturnExtendedMetaData", true); 
  */

  public static void callMethod_V(Object o, String methodName, boolean parm1) throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[1];
argTypes[0] = java.lang.Boolean.TYPE; 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = new Boolean(parm1);
try { 
    method.invoke(o, args);
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite); 
} 

  } 


 /**
  * call a method which returns nothing, but is passed a byte array 
  *
  * Examples
  *
  *  callMethod_V(ds, "setTranslateHex", "character"); 
  */

  public static void callMethod_V(Object o, String methodName, byte[] parm1) throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[1];
if (parm1 == null) {
    byte[] dummy = new byte[0];
    argTypes[0] = dummy.getClass(); 
} else { 
    argTypes[0] = parm1.getClass();
}
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[1];
args[0] = parm1;
try { 
    method.invoke(o, args); 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

} 
  }

  
  /**
   * call a method which returns nothing, but is passed an integer and object
   * The method to be called is dynamically resolved 
   *
   * Examples
   *
   *  callMethod_V(ps, "psSetNClob", 1, "character"); 
   */

public static void callMethod_V(Object o, String methodName, int i, Object parm2) throws Exception {
  java.lang.reflect.Method method = null;

  Class thisClass = o.getClass();
  Class[] argTypes = new Class[2];
  argTypes[0] = Integer.TYPE;

  if (parm2 == null) {
    throw new Exception("Unable to handle null parameter");
  }
  Class checkClass = parm2.getClass();
  String tryArgs = "";
  while (method == null && checkClass != null) {
    try {
      tryArgs += "(int, " + checkClass.getName() + ") ";
      argTypes[1] = checkClass;
      method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked

    } catch (Exception e) {
   
    }
    if (method == null) {
      if (checkClass.getName().equals("java.lang.Object")) {
        checkClass = null;

      } else {
        checkClass = checkClass.getSuperclass();
      }

    }
  }

  if (checkClass == null) {
    checkClass = parm2.getClass();
    // Did not find a base class.. Now look for implements
    while (method == null && checkClass != null) {
      try {

        // Find the implementes for the class
        Class[] interfaces = checkClass.getInterfaces();
        for (int j = 0; method == null && j < interfaces.length; j++) {
          tryArgs += "(int, " + interfaces[j].getName() + ") ";
    argTypes[1] = interfaces[j];
    try { 
  method = thisClass.getMethod(methodName, argTypes);
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
    } catch (NoSuchMethodException e) {
       // keep going 
    } 

        }
      } catch (Exception e) {
     
        e.printStackTrace();
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

  }

  if (method == null) { 
    throw new Exception("Unable to find method:  tried "+tryArgs); 
  }
  Object[] args = new Object[2];
  args[0] = new Integer(i);
  args[1] = parm2;
  try {
    method.invoke(o, args);
  } catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

  }

} 


public static void callMethod_V(Object o, String methodName, Object parm1, int i) throws Exception {
  java.lang.reflect.Method method = null;

  Class thisClass = o.getClass();
  Class[] argTypes = new Class[2];
  argTypes[1] = Integer.TYPE;

  if (parm1 == null) {
    throw new Exception("Unable to handle null parameter");
  }
  Class checkClass = parm1.getClass();
  String tryArgs = "";
  while (method == null && checkClass != null) {
    try {
      tryArgs += "(" + checkClass.getName() + ",int) ";
      argTypes[0] = checkClass;
      method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked

    } catch (Exception e) {
   
    }
    if (method == null) {
      if (checkClass.getName().equals("java.lang.Object")) {
        checkClass = null;

      } else {
        checkClass = checkClass.getSuperclass();
      }

    }
  }

  if (checkClass == null) {
    checkClass = parm1.getClass();
    // Did not find a base class.. Now look for implements
    while (method == null && checkClass != null) {
      try {

        // Find the implementes for the class
        Class[] interfaces = checkClass.getInterfaces();
        for (int j = 0; method == null && j < interfaces.length; j++) {
          tryArgs += "(" + interfaces[j].getName() + ",int) ";
          argTypes[0] = interfaces[j];
    try { 
            method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
    } catch (NoSuchMethodException e) {
      // keep going 
    }

        }
      } catch (Exception e) {
     
        e.printStackTrace();
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

  }

  if (method == null) { 
    throw new Exception("Unable to find method:  tried "+tryArgs); 
  }
  Object[] args = new Object[2];
  args[0] = parm1; 
  args[1] = new Integer(i);
  try {
    method.invoke(o, args);
  } catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

  }

} 

public static void callMethod_V(Object o, String methodName, Object parm1, boolean b) throws Exception {
  java.lang.reflect.Method method = null;

  Class thisClass = o.getClass();
  Class[] argTypes = new Class[2];
  argTypes[1] = Boolean.TYPE;

  if (parm1 == null) {
    throw new Exception("Unable to handle null parameter");
  }
  Class checkClass = parm1.getClass();
  String tryArgs = "";
  while (method == null && checkClass != null) {
    try {
      tryArgs += "(" + checkClass.getName() + ",int) ";
      argTypes[0] = checkClass;
      method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked

    } catch (Exception e) {
   
    }
    if (method == null) {
      if (checkClass.getName().equals("java.lang.Object")) {
        checkClass = null;

      } else {
        checkClass = checkClass.getSuperclass();
      }

    }
  }

  if (checkClass == null) {
    checkClass = parm1.getClass();
    // Did not find a base class.. Now look for implements
    while (method == null && checkClass != null) {
      try {

        // Find the implementes for the class
        Class[] interfaces = checkClass.getInterfaces();
        for (int j = 0; method == null && j < interfaces.length; j++) {
          tryArgs += "(" + interfaces[j].getName() + ",int) ";
          argTypes[0] = interfaces[j];
    try { 
  method = thisClass.getMethod(methodName, argTypes);
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
    } catch (NoSuchMethodException e) {
       // keep going 
    } 

        }
      } catch (Exception e) {
     
        e.printStackTrace();
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

  }

  if (method == null) { 
    throw new Exception("Unable to find method:  tried "+tryArgs); 
  }
  Object[] args = new Object[2];
  args[0] = parm1; 
  args[1] = new Boolean(b);
  try {
    method.invoke(o, args);
  } catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

  }

} 


public static void callMethod_V(Object o, String methodName, Object parm1) throws Exception {
  java.lang.reflect.Method method = null;

  Class thisClass = o.getClass();
  Class[] argTypes = new Class[1];

  if (parm1 == null) {
    throw new Exception("Unable to handle null parameter");
  }
  Class checkClass = parm1.getClass();
  String tryArgs = "";
  while (method == null && checkClass != null) {
    try {
      tryArgs += "(" + checkClass.getName() + ",int) ";
      argTypes[0] = checkClass;
      method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked

    } catch (Exception e) {
   
    }
    if (method == null) {
      if (checkClass.getName().equals("java.lang.Object")) {
        checkClass = null;

      } else {
        checkClass = checkClass.getSuperclass();
      }

    }
  }

  if (checkClass == null) {
    checkClass = parm1.getClass();
    // Did not find a base class.. Now look for implements
    while (method == null && checkClass != null) {
      try {

        // Find the implementes for the class
        Class[] interfaces = checkClass.getInterfaces();
        for (int j = 0; method == null && j < interfaces.length; j++) {
          tryArgs += "(" + interfaces[j].getName() + ",int) ";
          argTypes[0] = interfaces[j];
    try { 
  method = thisClass.getMethod(methodName, argTypes);
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
    } catch (NoSuchMethodException e) {
       // keep going 
    } 

        }
      } catch (Exception e) {
     
        e.printStackTrace();
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

  }

  if (method == null) { 
    throw new Exception("Unable to find method:  tried "+tryArgs); 
  }
  Object[] args = new Object[1];
  args[0] = parm1; 
  try {
    method.invoke(o, args);
  } catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

  }

} 


public static void addInterfacesToHashtable(Hashtable interfacesHashtable, Class checkClass) {
    if (checkClass.isInterface()) {
  interfacesHashtable.put(checkClass, checkClass);
    }
    Class[] interfaces = checkClass.getInterfaces();
    for (int j = 0;  j < interfaces.length; j++) {
  addInterfacesToHashtable(interfacesHashtable, interfaces[j]); 
    }
}


public static void callMethod_V(Object o, String methodName, String parm1, Object parm2) throws Exception {
  java.lang.reflect.Method method = null;

  Class thisClass = o.getClass();
  Class[] argTypes = new Class[2];
  argTypes[0] = String.class; 
  
  if (parm2 == null) {
    throw new Exception("Unable to handle null parameter");
  }
  Class checkClass = parm2.getClass();
  String tryArgs = "";
  while (method == null && checkClass != null) {
try {
    tryArgs += "(String, Class:" + checkClass.getName() + " ) ";
    argTypes[1] = checkClass;
    method = thisClass.getMethod(methodName, argTypes);
    method.setAccessible(true); //allow toolbox proxy methods to be invoked

} catch (Exception e) {

}
if (method == null) {
    if (checkClass.getName().equals("java.lang.Object")) {
  checkClass = null;

    } else {
  checkClass = checkClass.getSuperclass();
    }

}
  }

  if (checkClass == null) {
    checkClass = parm2.getClass();
    // Did not find a base class.. Now look for implements
    while (method == null && checkClass != null) {
      try {

        // Find the implementes for the class
  Hashtable interfacesHashtable = new Hashtable();
  addInterfacesToHashtable(interfacesHashtable, checkClass);

  Enumeration keys = interfacesHashtable.keys(); 


  while(method == null && keys.hasMoreElements()) {
      Class x = (Class) keys.nextElement(); 
      tryArgs += "(String, Interface:" + x.getName() + ") ";
      argTypes[1] = x;
      try { 
    method = thisClass.getMethod(methodName, argTypes);
    method.setAccessible(true); //allow toolbox proxy methods to be invoked
      } catch (NoSuchMethodException e) {
       // keep going 
      } 

  }

  
      } catch (Exception e) {
     
        e.printStackTrace();
      }
      if (method == null) {
        if (checkClass.getName().equals("java.lang.Object")) {
          checkClass = null;

        } else {
          checkClass = checkClass.getSuperclass();
        }

      }
    }

  }

  if (method == null) { 
    throw new Exception("Unable to find method with name "+methodName+":  tried "+tryArgs); 
  }
  Object[] args = new Object[2];
  args[0] = parm1; 
  args[1] = parm2; 
  try {
    method.invoke(o, args);
  } catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

  }

} 





/**
 * call a method which returns nothing, but is passed a long and object
 * The method to be called is dynamically resolved 
 *
 * Examples
 *
 *  callMethod_V(nclob, "setString", 1, "character"); 
 */

public static void callMethod_V(Object o, String methodName, long l, Object parm2) throws Exception {
java.lang.reflect.Method method = null;

Class thisClass = o.getClass();
Class[] argTypes = new Class[2];
argTypes[0] = Long.TYPE;

if (parm2 == null) {
  throw new Exception("Unable to handle null parameter");
}
Class checkClass = parm2.getClass();
String tryArgs = "";
while (method == null && checkClass != null) {
  try {
    tryArgs += "(int, " + checkClass.getName() + ") ";
    argTypes[1] = checkClass;
    method = thisClass.getMethod(methodName, argTypes);
    method.setAccessible(true); //allow toolbox proxy methods to be invoked

  } catch (Exception e) {

  }
  if (method == null) {
    if (checkClass.getName().equals("java.lang.Object")) {
      checkClass = null;

    } else {
      checkClass = checkClass.getSuperclass();
    }

  }
}

if (method == null) {
  checkClass = parm2.getClass();
  // Did not find a base class.. Now look for implements
  while (method == null && checkClass != null) {
    try {

      // Find the implements for the class
      Class[] interfaces = checkClass.getInterfaces();
      for (int j = 0; method == null && j < interfaces.length; j++) {
        tryArgs += "(int, " + interfaces[j].getName() + ") ";
        argTypes[1] = interfaces[j];
  try { 
      method = thisClass.getMethod(methodName, argTypes);
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
  } catch (NoSuchMethodException e) {
       // keep going 
  } 

      }
    } catch (Exception e) {
   
    }
    if (method == null) {
      if (checkClass.getName().equals("java.lang.Object")) {
        checkClass = null;

      } else {
        checkClass = checkClass.getSuperclass();
      }

    }
  }

}

if (method == null) { 
  throw new Exception("Unable to find method:  tried "+tryArgs); 
}
Object[] args = new Object[2];
args[0] = new Long(l);
args[1] = parm2;
try {
  method.invoke(o, args);
} catch (java.lang.reflect.InvocationTargetException ite) {
  handleIte(ite);

}

} 

  
  
 /**
   * call a method which returns nothing, but is passed int, InputStream, long
   * 
   * Examples
   * 
   * JDReflectionUtil.callMethod_V(ps, "setBlob", 1, is, (long) 4);
   */

  public static void callMethod_V(Object o, String methodName, int parameterIndex, InputStream inputStream, long length) throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[3];
argTypes[0] = java.lang.Integer.TYPE;
argTypes[1] = Class.forName("java.io.InputStream");
argTypes[2] = java.lang.Long.TYPE; 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[3];
args[0] = new Integer(parameterIndex);
args[1] = inputStream;
args[2] = new Long(length); 
try { 
    method.invoke(o, args); 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

} 
  }
  

  /**
    * call a method which returns nothing, but is passed String, InputStream, long
    * 
    * Examples
    * 
    * JDReflectionUtil.callMethod_V(ps, "setBlob", "col1", is, (long) 4);
    */
  //@pdc adding _IS (inputStream) to method name so that abiguity errors get fixed on calls like callMethod_V(o, "aa", "bb", null, 1) //null type is not known.
  public static void callMethod_V_IS(Object o, String methodName, String parameterName, InputStream inputStream, long length) throws Exception {
      
      java.lang.reflect.Method method;
      
      Class thisClass = o.getClass();
      Class[] argTypes = new Class[3];
      argTypes[0] = String.class;
      argTypes[1] = Class.forName("java.io.InputStream");
      argTypes[2] = java.lang.Long.TYPE; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[3];
      args[0] = parameterName;
      args[1] = inputStream;
      args[2] = new Long(length); 
      try { 
          method.invoke(o, args); 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          
      } 
  }

/**
  * call a method which returns nothing, but is passed int, Reader, long 
  *
  * Examples
  *
  *  JDReflectionUtil.callMethod_V(ps, "setClob", 1, r, (long) 4);
  */

  public static void callMethod_V(Object o, String methodName, int parameterIndex, Reader reader, long length) throws Exception {

java.lang.reflect.Method method;

Class thisClass = o.getClass();
Class[] argTypes = new Class[3];
argTypes[0] = java.lang.Integer.TYPE;
argTypes[1] = Class.forName("java.io.Reader");
argTypes[2] = java.lang.Long.TYPE; 
method = thisClass.getMethod(methodName, argTypes); 
  method.setAccessible(true); //allow toolbox proxy methods to be invoked
Object[] args = new Object[3];
args[0] = new Integer(parameterIndex);
args[1] = reader;
args[2] = new Long(length); 
try { 
    method.invoke(o, args); 
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);

} 
  }

  /**
   * call a method which returns nothing, but is passed String, Reader, long 
   *
   * Examples
   *
   *  JDReflectionUtil.callMethod_V(ps, "setClob", "C1", r, (long) 4);
   */

   public static void callMethod_V(Object o, String methodName, String parameterName, Reader reader, long length) throws Exception {

      java.lang.reflect.Method method;

      Class thisClass = o.getClass();
      Class[] argTypes = new Class[3];
      argTypes[0] = String.class; 
      argTypes[1] = Class.forName("java.io.Reader");
      argTypes[2] = java.lang.Long.TYPE; 
      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[3];
      args[0] = parameterName;
      args[1] = reader;
      args[2] = new Long(length); 
      try { 
          method.invoke(o, args); 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);

      } 
   }


  /**
   * call a static method whihc returns an object.
   *
   * Examples
   *
   *  JDReflectionUtil.callStaticMethod_O("", "newInstance");
   */

   public static Object callStaticMethod_O(String classname, String methodName) throws Exception {

      Object returnObject = null; 
Class thisClass =  Class.forName(classname);

      java.lang.reflect.Method method;

      Class[] argTypes = new Class[0];

      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[0];
      try { 
          returnObject =  method.invoke(null, args); 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);

      } 
      return returnObject; 
   }



  /**
   * call a static method whihc returns an int.
   *
   * Examples
   *
   *  JDReflectionUtil.callStaticMethod_O("", "newInstance");
   */

   public static int callStaticMethod_I(String classname, String methodName) throws Exception {

      Object returnObject = null; 
Class thisClass =  Class.forName(classname);

      java.lang.reflect.Method method;

      Class[] argTypes = new Class[0];

      method = thisClass.getMethod(methodName, argTypes); 
      method.setAccessible(true); //allow toolbox proxy methods to be invoked
      Object[] args = new Object[0];
      try { 
          returnObject =  method.invoke(null, args); 
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);

      } 
      if (returnObject != null) { 
      return ((Integer)returnObject).intValue(); 
      } else {
        return 0; 
      }
   }





 /**
  * create an object using reflection
  * Examples
  *
  *  JDReflectionUtil.createObject("com.ibm.db2.jcc.DB2XADataSource")
  * 
  *  callMethod_V(ds, "setTranslateHex", "character"); 
  */

  public static Object createObject(String classname) throws Exception {

Class objectClass1 = Class.forName(classname);
Class[] noArgTypes = new Class[0];
Object[] noArgs    = new Object[0];
Object newObject =null; 
try { 
    Constructor constructor = objectClass1.getConstructor(noArgTypes);

    newObject = constructor.newInstance(noArgs);
} catch (java.lang.reflect.InvocationTargetException ite) {
    handleIte(ite);
    
} 
return newObject; 
  }


  /**
   * create an object using reflection
   * Examples
   *
   *  JDReflectionUtil.createObject("com.ibm.db2.app.DB2RowId", testArray)
   * 
   */

   public static Object createObject(String classname, byte[] arg) throws Exception {

      Class objectClass1 = Class.forName(classname);
      Class[] oneArgTypes = new Class[1];
      oneArgTypes[0] = Class.forName("[B"); 
      Object[] oneArgs    = new Object[1];
      oneArgs[0] = arg; 
      Object newObject =null; 
      try { 
          Constructor constructor = objectClass1.getDeclaredConstructor(oneArgTypes); //@pdc find protected constructor also

          constructor.setAccessible(true);  //@PDA allo call of protected.
          newObject = constructor.newInstance(oneArgs);
      } catch (java.lang.reflect.InvocationTargetException ite) {
          handleIte(ite);
          
      } 
      return newObject; 
   }

   /**
    * create an object using reflection
    * Examples
    *
    *  JDReflectionUtil.createObject("javax.xml.transform.stax.StAXSource", "javax.xml.stream.XMLStreamReader", xmlStreamReader); 
    *  
    */

    public static Object createObject(String classname, String parameterClass, Object arg) throws Exception {

       Class objectClass1 = Class.forName(classname);
       Class[] oneArgTypes = new Class[1];
       oneArgTypes[0] = Class.forName(parameterClass); 
       Object[] oneArgs    = new Object[1];
       oneArgs[0] = arg; 
       Object newObject =null; 
       try { 
           Constructor constructor = objectClass1.getDeclaredConstructor(oneArgTypes); 

           constructor.setAccessible(true);  
           newObject = constructor.newInstance(oneArgs);
       } catch (java.lang.reflect.InvocationTargetException ite) {
           handleIte(ite);
           
       } 
       return newObject; 
    }



  /**
   * get an integer  field 
   *
   * Examples
   *
   *  int value = getField_I(ds, "getMaximumPrecision"); 
   */ 
   public static int getField_I(Object o, String fieldName) throws Exception {
      java.lang.reflect.Field field;
      Class thisClass = o.getClass();
      field = thisClass.getField(fieldName);
      return         field.getInt(o);
   }




}
