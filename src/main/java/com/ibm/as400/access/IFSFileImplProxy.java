///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @D7 - 07/25/2007 - Add allowSortedRequests to the listDirectoryDetails()
//                    method to resolve problem of issuing PWFS List Attributes 
//                    request with both "Sort" indication and "RestartByID" 
//                    which is documented to be an invalid combination.
// @D8 - 04/03/2008 - Add clearCachedAttributes() to clear impl cache attributes. 
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * Provides a local proxy implementation for the IFSFile class.
 **/
class IFSFileImplProxy extends AbstractProxyImpl implements IFSFileImpl
{
    IFSFileImplProxy() {
        super("IFSFile");
    }

    @Override
    public boolean canExecute() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsBoolean(pxId_, "canExecute");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public boolean canRead() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsBoolean(pxId_, "canRead");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public boolean canWrite() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsBoolean(pxId_, "canWrite");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public void clearCachedAttributes()
    {
        try {
            connection_.callMethod(pxId_, "clearCachedAttributes");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    @Override
    public boolean copyTo(String path, boolean replace)
            throws IOException, AS400SecurityException, ObjectAlreadyExistsException
    {
        try {
            return ((Boolean) connection_.callMethod(pxId_, "copyTo", 
                    new Class[] { String.class, Boolean.TYPE },
                    new Object[] { path, Boolean.valueOf(replace) }).getReturnValue()).booleanValue();
        }
        catch (InvocationTargetException e) {
            throw rethrow3a(e);
        }
    }
  
    @Override
    public long created() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethod(pxId_, "created").getReturnValueLong();
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public int createNewFile() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsInt(pxId_, "createNewFile");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public int delete() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsInt(pxId_, "delete");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public int exists() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethodReturnsInt(pxId_, "exists");
        }
        catch (InvocationTargetException e) {
            throw rethrow2(e);
        }
    }

    @Override
    public int getCCSID(boolean retrieveAll) throws IOException, AS400SecurityException {
        return getCCSID();
    }

    @Override
    public int getCCSID() throws IOException, AS400SecurityException 
    {
        try {
            return connection_.callMethodReturnsInt(pxId_, "getCCSID");
        } 
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    @Override
    public long getAvailableSpace(boolean forUserOnly) throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethod(pxId_, "getAvailableSpace", 
                    new Class[] { Boolean.TYPE },
                    new Object[] { Boolean.valueOf(forUserOnly) }).getReturnValueLong();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    @Override
    public long getTotalSpace(boolean forUserOnly) throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethod(pxId_, "getTotalSpace", 
                    new Class[] { Boolean.TYPE },
                    new Object[] { Boolean.valueOf(forUserOnly) }).getReturnValueLong();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    @Override
    public String getOwnerName(boolean retrieveAll) throws IOException, AS400SecurityException {
        return getOwnerName();
    }

    @Override
    public String getOwnerName() throws IOException, AS400SecurityException
    {
        try {
            return (String) connection_.callMethod(pxId_, "getOwnerName").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    @Override
    public String getOwnerNameByUserHandle(boolean forceRetrieve) throws IOException, AS400SecurityException {
        try {
            return (String) connection_.callMethod(pxId_, "getOwnerNameByUserHandle").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    @Override
    public int getASP() throws IOException, AS400SecurityException
    {
        try {
            return connection_.callMethod(pxId_, "getASP").getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

  @Override
  public String getFileSystemType() throws IOException, AS400SecurityException
  {
      try {
          return (String) connection_.callMethod(pxId_, "getFileSystemType").getReturnValue();
      } 
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public long getOwnerUID() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "getOwnerUID").getReturnValueLong();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public String getPathPointedTo() throws IOException, AS400SecurityException
  {
      try {
          return (String) connection_.callMethod(pxId_, "getPathPointedTo").getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public String getSubtype() throws IOException, AS400SecurityException
  {
      try {
          return (String) connection_.callMethod(pxId_, "getSubtype").getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public int isDirectory() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsInt(pxId_, "isDirectory");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public int isFile() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsInt(pxId_, "isFile");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public boolean isHidden() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsBoolean(pxId_, "isHidden");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public boolean isReadOnly() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsBoolean(pxId_, "isReadOnly");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public boolean isSourcePhysicalFile() throws IOException, AS400SecurityException, AS400Exception
  {
      try {
          return connection_.callMethodReturnsBoolean(pxId_, "isSymbolicLink");
      }
      catch (InvocationTargetException e) {
          throw rethrow3(e);
      }
  }

  @Override
  public boolean isSymbolicLink() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsBoolean(pxId_, "isSymbolicLink");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public long lastAccessed() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "lastAccessed").getReturnValueLong();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public long lastModified() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "lastModified").getReturnValueLong();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public long length() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "length").getReturnValueLong();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public String[] listDirectoryContents(String directoryPath) throws IOException, AS400SecurityException
  {
      try {
          return (String[]) connection_.callMethod(pxId_, "listDirectoryContents", 
                  new Class[] { String.class },
                  new Object[] { directoryPath }).getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public IFSCachedAttributes[] listDirectoryDetails(String directoryPattern, String directoryPath, int maxGetCount, String restartName) 
          throws IOException, AS400SecurityException
  {
      try {
          return (IFSCachedAttributes[]) connection_
                  .callMethod(pxId_, "listDirectoryDetails",
                          new Class[] { String.class, String.class, Integer.TYPE, String.class },
                          new Object[] { directoryPattern, directoryPath, Integer.valueOf(maxGetCount), restartName })
                  .getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public IFSCachedAttributes[] listDirectoryDetails(String directoryPattern, String directoryPath, int maxGetCount,  byte[] restartID, boolean allowSortedRequests)
          throws IOException, AS400SecurityException
  {
      try {
          return (IFSCachedAttributes[]) connection_.callMethod (pxId_, "listDirectoryDetails",
                  new Class[] { String.class, String.class, Integer.TYPE, byte[].class, Boolean.TYPE },
                  new Object[] { directoryPattern, directoryPath, Integer.valueOf(maxGetCount), restartID, Boolean.valueOf(allowSortedRequests) })
                  .getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw rethrow2 (e);
      }
  }

  @Override
  public int mkdir(String directory) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "mkdir", new Class[] { String.class }, new Object[] { directory }).getReturnValueInt();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public int mkdirs() throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethodReturnsInt(pxId_, "mkdirs");
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }

  @Override
  public int renameTo(IFSFileImpl file) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "renameTo", new Class[] { IFSFileImpl.class }, new Object[] { file }).getReturnValueInt();
      }
      catch (InvocationTargetException e) {
          throw rethrow2(e);
      }
  }


  private static InternalErrorException rethrow2(InvocationTargetException e) throws IOException, AS400SecurityException
  {
      Throwable e2 = e.getTargetException();
      if (e2 instanceof IOException)
          throw (IOException) e2;
      else if (e2 instanceof AS400SecurityException)
          throw (AS400SecurityException) e2;

      return ProxyClientConnection.rethrow(e);
  }

  private static InternalErrorException rethrow3 (InvocationTargetException e) throws IOException, AS400SecurityException, AS400Exception
  {
      Throwable e2 = e.getTargetException();
      if (e2 instanceof IOException)
          throw (IOException) e2;
      else if (e2 instanceof AS400SecurityException)
          throw (AS400SecurityException) e2;
      else if (e2 instanceof AS400Exception)
          throw (AS400Exception) e2;

      return ProxyClientConnection.rethrow(e);
  }

  private static InternalErrorException rethrow3a (InvocationTargetException e) throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
      Throwable e2 = e.getTargetException();
      if (e2 instanceof IOException)
          throw (IOException) e2;
      else if (e2 instanceof AS400SecurityException)
          throw (AS400SecurityException) e2;
      else if (e2 instanceof ObjectAlreadyExistsException)
          throw (ObjectAlreadyExistsException) e2;

      return ProxyClientConnection.rethrow(e);
  }

  @Override
  public boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "setAccess", 
                  new Class[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE },
                  new Object[] { Integer.valueOf(accessType), Boolean.valueOf(enableAccess), Boolean.valueOf(ownerOnly) })
                  .getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public boolean setCCSID(int ccsid) throws IOException, AS400SecurityException
  {
      try {
          return connection_
                  .callMethod(pxId_, "setCCSID", 
                          new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(ccsid) })
                  .getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public boolean setFixedAttributes(int attributes) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "setFixedAttributes", 
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(attributes) }).getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public boolean setHidden(boolean attribute) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "setHidden", 
                  new Class[] { Boolean.TYPE },
                  new Object[] { Boolean.valueOf(attribute) }).getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }

  @Override
  public boolean setLastModified(long time) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod (pxId_, "setLastModified",
                  new Class[] { Long.TYPE },
                  new Object[] { Long.valueOf(time)}).getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2 (e);
      }
  }

  @Override
  public boolean setLength(int length) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod (pxId_, "setLength",
                                     new Class[] { Integer.TYPE },
                                     new Object[] { Integer.valueOf(length) }).getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2 (e);
      }
  }

  @Override
  public void setPatternMatching(int patternMatching)
  {
      try {
          connection_.callMethod (pxId_, "setPatternMatching",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(patternMatching) });
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow (e);
      }
  }

  @Override
  public boolean setReadOnly(boolean attribute) throws IOException, AS400SecurityException
  {
      try {
          return connection_.callMethod(pxId_, "setReadOnly",
                  new Class[] { Boolean.TYPE },
                  new Object[] { Boolean.valueOf(attribute) }).getReturnValueBoolean();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2 (e);
      }
  }

  @Override
  public void setPath(String path)
  {
      try {
          connection_.callMethod(pxId_, "setPath", 
                  new Class[] { String.class }, new Object[] { path });
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow(e);
      }
  }

  @Override
  public void setSorted(boolean sort)
  {
      try {
          connection_.callMethod (pxId_, "setSorted",
                  new Class[] { Boolean.TYPE },
                  new Object[] { Boolean.valueOf(sort) });
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow (e);
      }
  }

  @Override
  public void setSystem(AS400Impl system)
  {
      try {
          connection_.callMethod (pxId_, "setSystem",
                  new Class[] { AS400Impl.class },
                  new Object[] { system });
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow (e);
      }
  }

  @Override
  public String getDescription() throws AS400SecurityException, IOException
  {
      try {
          return (String) connection_.callMethod(pxId_, "getDescription").getReturnValue();
      }
      catch (InvocationTargetException e) {
          throw ProxyClientConnection.rethrow2(e);
      }
  }
}
