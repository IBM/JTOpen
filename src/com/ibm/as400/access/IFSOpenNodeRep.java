package com.ibm.as400.access;

public class IFSOpenNodeRep extends IFSDataStream {
	private static final String copyright = "Copyright (C) 2021 International Business Machines Corporation and others.";
	private static final int OBJECT_HANDLE_OFFSET = 22;
	
	/**
	Generate a new instance of this type.
	@return a reference to the new instance
	**/
	  public Object getNewDataStream()
	  {
	    return new IFSOpenNodeRep();
	  }

	/**
	Generates a hash code for this data stream.
	@return the hash code
	**/
	  public int hashCode()
	  {
	    return 0x800E;
	  }
	  
	  /**
	  Get the file handle.
	  @return the file handle.
	  **/
	  int getObjectHandle() {
	      return get32bit(OBJECT_HANDLE_OFFSET);
	  }

}
