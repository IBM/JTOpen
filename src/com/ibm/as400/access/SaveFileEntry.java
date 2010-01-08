package com.ibm.as400.access;

import java.io.Serializable;
import java.util.Date;

/**
 Represents an entry in a save file.
 Instances of this class are created by {@link SaveFile#listEntries() SaveFile.listEntries()}
**/

public class SaveFileEntry
implements Serializable, Comparable
{
  static final long serialVersionUID = 4L;

  private String objName_;
  private String libSaved_;
  private String objType_;
  private String extObjAttr_;
  private Date saveDateTime_;
  private int objSize_;
  private int objSizeMult_;
  private int asp_;
  private String dataSaved_;
  private String objOwner_;
  private String dloName_;
  private String folder_;
  private String desc_;
  private String aspDevName_;  // Note: This field was added in V5R2.

  SaveFileEntry(String objName, String libSaved, String objType, String extObjAttr, Date saveDateTime, int objSize, int objSizeMult, int asp, String dataSaved, String objOwner, String dloName, String folder, String desc, String aspDevName)
  {
    // Note: Since SaveFileEntry objects are created exclusively by other Toolbox classes (SaveFile in particular), we can guarantee that none of the arguments will be null-valued.
    objName_ = objName;
    libSaved_ = libSaved;
    objType_ = objType;
    extObjAttr_ = extObjAttr;
    saveDateTime_ = saveDateTime;
    objSize_ = objSize;
    objSizeMult_ = objSizeMult;
    asp_ = asp;
    dataSaved_ = dataSaved;
    objOwner_ = objOwner;
    dloName_ = dloName;
    folder_ = folder;
    desc_ = desc;
    aspDevName_ = aspDevName;
  }

  /**
   Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
   @exception ClassCastException if obj is not an instance of SaveFileEntry.
   **/
  public int compareTo(Object obj)
  {
    return objName_.compareTo(((SaveFileEntry)obj).getName());
  }


  /**
   Determines whether this object is equal to another object.
   @return <tt>true</tt> if the two instances are equal
   **/
  public boolean equals(Object obj)
  {
    try
    {
      SaveFileEntry other = (SaveFileEntry)obj;

      // Note: Since SaveFileEntry objects can only be created internally by the Toolbox, we can guarantee that all instance variables are non-null.
      if      (!objName_.equals(other.getName())) return false;
      else if (!libSaved_.equals(other.getLibrary())) return false;
      else if (!objType_.equals(other.getType())) return false;
      else if (!dloName_.equals(other.getDLOName())) return false;
      else if (!folder_.equals(other.getFolder())) return false;
      else if (!aspDevName_.equals(other.getASPDevice())) return false;
      else if (asp_ != other.getASP()) return false;
      else return true;
    }
    catch (Throwable e) {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, e);
      return false;
    }
  }


  /**
   Returns the name of the object saved.  If the object is a DLO object, this field will contain the system name of the object.
   @return The object name.
   **/
  public String getName()
  {
    return objName_;
  }

  /**
   Returns the name of the library from which the object is saved.
   @return the object library.
   **/
  public String getLibrary()
  {
    return libSaved_;
  }

  /**
   Returns the type of object.  For examples: *LIB, *PGM, or *FILE.
   @return The object type.
   **/
  public String getType()
  {
    return objType_;
  }


  /**
   Returns extended information about the object type.  If there is no extended information, returns an empty String.
   @return The object's extended information.
   **/
  public String getExtendedObjectAttribute()
  {
    return extObjAttr_;
  }

  /**
   Returns the time at which the object was saved.
   @return The object save date.
   **/
  public Date getSaveDate()
  {
    return (Date)saveDateTime_.clone();  // return a copy
  }

  /**
   Returns the total size of the object in bytes.
   @return The object size in bytes.
   **/
  public long getSize()
  {
    return ((long)objSize_ * (long)objSizeMult_);
    // From the API reference ("List Save File" (QSRLSAVF) API):
    //
    // Object size:  The size of the object in units of the size multiplier. The true object size is equal to or smaller than the object size multiplied by the object size multiplier.
    //
    // Object size multiplier:  The value to multiply the object size by to get the true size. The value is 1 if the object is smaller than or equal to 999 999 999 bytes, 1024 if it is larger than 999 999 999 but smaller than or equal to 4 294 967 295, and 4096 if larger than 4 294 967 295.
  }

  /**
   Returns the auxiliary storage pool (ASP) of the object when it was saved.
   @return The auxiliary storage pool.
   **/
  public int getASP()
  {
    return asp_;
  }

  /**
   Indicates whether the data for this object was saved with the object.
   @return true if the data was saved; false otherwise.
   **/
  public boolean isDataSaved()
  {
    return (dataSaved_.equals("1") ? true : false);
  }

  /**
   Returns the name of the object owner's user profile.
   @return The object owner.
   **/
  public String getOwner()
  {
    return objOwner_;
  }

  /**
   Returns the document library object (DLO) name.  This is the name of the document, folder, or mail object that was saved.  If the save file does not contain DLO information, this field will be an empty String.
   @return The DLO name.
   **/
  public String getDLOName()
  {
    return dloName_;
  }

  /**
   Returns the name of the folder that was saved.  If the object is not a *FLR or *DOC object, this field will be empty.  For *DOC and *FLR objects, this field will be set to the qualified name of the folder or to *NONE.
   @return The folder.
   **/
  public String getFolder()
  {
    return folder_;
  }

  /**
   Returns the text description of the object.
   @return The description.
   **/
  public String getDescription()
  {
    return desc_;
  }

  /**
   Returns the auxiliary storage pool device name.  This is the name of the independent auxiliary storage pool (ASP) device of the object when it was saved.
   @return The ASP device name.
   **/
  public String getASPDevice()
  {
    return aspDevName_;
  }


  /**
   Returns the String representation of this object.
   @return  The String representation of this object.
   **/
  public String toString()
  {
    return "SaveFileEntry (name: " + objName_ + "; library: "+ libSaved_+ "; type: "+ objType_ + "): " + super.toString();
  }

}
