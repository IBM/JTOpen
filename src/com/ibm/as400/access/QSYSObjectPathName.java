///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QSYSObjectPathName.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
// @D3D import java.util.MissingResourceException;                      // @D1A
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
The QSYSObjectPathName class provides
an integrated file system path name that represents an object in the QSYS
library file system.
<p>
QSYSObjectPathName objects generate the following events:
    PropertyChangeEvent.
<p>
This object can be used in two ways:
<ul compact>
<li>To parse the integrated file system path name to ensure correct
syntax and to extract the library, object, member, and object type
<li>To build an integrated file system path name from a library,
object, and member or object type.
</ul>
IllegalPathNameExceptions are thrown if errors are found.
<p>
Objects in the QSYS file system have integrated file system names with the
following format for objects or members in a library other than QSYS:
<pre>
   /QSYS.LIB/<i>library</i>.LIB/<i>object</i>.<i>type</i>
   /QSYS.LIB/<i>library</i>.LIB/<i>object</i>.FILE/<i>member</i>.MBR
</pre>
For objects or members that reside in QSYS, this format is used:
<pre>
   /QSYS.LIB/<i>object</i>.<i>type</i>
   /QSYS.LIB/<i>object</i>.FILE/<i>member</i>.MBR
</pre>
For example:
<pre>
   /QSYS.LIB/QGPL.LIB/CRTLIB.CMD
   /QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE/PAYABLE.MBR
   /QSYS.LIB/CRTLIB.CMD
   /QSYS.LIB/ACCOUNTS.FILE/PAYABLE.MBR
</pre>
<p>
In an integrated file system path name, 
special values, such as <i>*ALL</i>, that begin with an asterisk
are not depicted with an asterisk but with leading and trailing
percent signs (<i>%ALL%</i>). In the
integrated file system, an asterisk is a wildcard character.  The following
special values are recognized by this class:
<pre>
Library name: %ALL%(*ALL), %ALLUSR%(*ALLUSR), %CURLIB%(*CURLIB),
              %LIBL%(*LIBL), %USRLIBL%(*USRLIBL)
Object name:  %ALL%(*ALL)
Member name:  %ALL%(*ALL), %FILE%(*FILE), %FIRST%(*FIRST), %LAST%(*LAST)
              %NONE%(*NONE)
</pre>
<p>
The path name will be in uppercase.  If case needs
to be preserved for a library, object, or member name, quotation marks should
be used around the names.  For example,
<pre>
  QSYSObjectPathName path = new
        QSYSObjectPathName("/QSYS.LIB/\"MixedCase\".LIB/\"lowercase\".FILE");
</pre>
Examples:
<ul>
<li>This code will extract the pieces of an integrated file system name
that represents a file.
<pre>
  QSYSObjectPathName path = new
     QSYSObjectPathName("/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE");
  System.out.println(path.getLibraryName());  // will print "QGPL"
  System.out.println(path.getObjectName());   // will print "ACCOUNTS"
  System.out.println(path.getObjectType());   // will print "FILE"
</pre>
<li>This code will extract the pieces of an integrated file system name
that represents a member.
<pre>
  QSYSObjectPathName path = new
     QSYSObjectPathName("/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE/PAYABLE.MBR");
  System.out.println(path.getLibraryName());  // will print "QGPL"
  System.out.println(path.getObjectName());   // will print "ACCOUNTS"
  System.out.println(path.getMemberName());   // will print "PAYABLE"
  System.out.println(path.getObjectType());   // will print "MBR"
</pre>
<li>This code will build an integrated file system name for a file.
<pre>
  QSYSObjectPathName path = new
     QSYSObjectPathName("QGPL", "ACCOUNTS", "FILE");
  // will print "/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE"
  System.out.println(path.getPath());
</pre>
<li>This code will build an integrated file system name for a member.
<pre>
  QSYSObjectPathName path = new
     QSYSObjectPathName("QGPL", "ACCOUNTS", "PAYABLE", "MBR");
  // will print "/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE/PAYABLE.MBR"
  System.out.println(path.getPath());
</pre>
</ul>
**/
public class QSYSObjectPathName extends Object
                                implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


String fullPathName_="";      // full IFS path
String libraryName_="";       // library name
String objectName_="";        // object name
String memberName_="";        // member name
String objectType_="";        // object type
private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
transient private Vector vetoListeners_ = new Vector();


/* @D3D
// If you add a type here, make sure and add MRI for it with the                    // @D1A
// key "TYPE_name".                                                                 // @D1A
private static final String[] supportedObjectTypes_ = {                             // @D1A
        "ALRTBL", "AUTL",                                                           // @D1A
        "BLKS", "BNDDIR",                                                           // @D1A
        "CFGL", "CHTFMT", "CLD", "CLS", "CMD", "CNNL", "COSD",                      // @D1A
        "CRG", "CRQD", "CSI", "CSPMAP", "CSPTBL", "CTLD", "CTLSTS",                 // @D1A
        "DDIR", "DEVD", "DEVSTS", "DIR", "DOC", "DSTMF", "DTAARA",                  // @D1A
        "DTADCT", "DTAQ",                                                           // @D1A
        "EDTD", "EXITRG",                                                           // @D1A
        "FCT", "FILE", "FLR", "FNTRSC", "FNTTBL", "FORMDF", "FTR",                  // @D1A
        "GSS",                                                                      // @D1A
        "IGCDCT", "IGCSRT", "IGCTBL", "IPXD",                                       // @D1A
        "JOBD", "JOBQ", "JOBSCD", "JRN", "JRNRCV",                                  // @D1A
        "LIB", "LIND", "LINSTS", "LOCALE",                                          // @D1A
        "M36", "M36CFG", "MEDDFN", "MENU", "MGTCOL", "MODD", "MODULE",              // @D1A
        "MSGF", "MSGQ",                                                             // @D1A
        "NETF", "NODGRP", "NODL", "NTBD", "NWID", "NWISTS", "NWSD",                 // @D1A
        "OOPOOL", "OUTQ", "OVL",                                                    // @D1A
        "PAGDFN", "PAGSEG", "PDG", "PGM", "PNLGRP", "PRDAVL",                       // @D1A
        "PRDDFN", "PRDLOD", "PSFCFG",                                               // @D1A
        "QMFORM", "QMQRY", "QRYDFN",                                                // @D1A
        "RCT",                                                                      // @D1A
        "S36", "SBSD", "SBSSTS", "SCHIDX", "SOCKET", "SPADCT",                      // @D1A
        "SQLPKG", "SQLUDT", "SRVPGM", "SSND", "STMF", "SVRSTG", "SYMLNK",           // @D1A
        "TBL",                                                                      // @D1A
        "USRIDX", "USRPRF", "USRQ", "USRSPC",                                       // @D1A
        "VLDL",                                                                     // @D1A
        "WSCST"                                                                     // @D1A
        };                                                                          // @D1A
    
private static final String fileType_ = "FILE";                                     // @D1A
private static final String pgmType_ = "PGM";                                       // @D1A
private static final String[] noAttributes_ = new String[0];                        // @D1A

// If you add a attribute here, make sure and add MRI for it with the               // @D1A
// key "TYPE_FILE_name".                                                            // @D1A
private static final String[] supportedFileAttributes_ = {                          // @D1A
        "CMNF", "DKTF", "DSPF", "ICFF", "LF", "PF",                                 // @D1A
        "PRTF", "SAVF", "TAPF" };                                                   // @D1A

// If you add a attribute here, make sure and add MRI for it with the               // @D1A
// key "TYPE_PGM_name".                                                             // @D1A
private static final String[] supportedPgmAttributes_ = {                           // @D1A
        "RPG", "CLP", "C", "PAS", "CBL", "BAS", "PLI", "FTN", "CLE" };              // @D1A @D2C
*/        


/**
Constructs an QSYSObjectPathName object.
It creates an integrated file system path name for an object in the QSYS
file system.
**/
public QSYSObjectPathName()
{
  super();
}


/**
Constructs an QSYSObjectPathName object.
It creates an integrated file system path name for an object in the QSYS
file system.

@param     path      The fully qualified integrated file system name of an
                     object in the QSYS file system.

**/
public QSYSObjectPathName(String path)
{
  super();
  if (path == null)
      throw new NullPointerException("path");
  parse(path);
}


/**
Constructs an QSYSObjectPathName object.
It creates an integrated file system path name for an object in the QSYS
file system.

@param     path      The fully qualified integrated file system name of an
                     object in the QSYS file system.
@param     expectedObjectType The type of object <i>path</i> should
                     represent.
**/
QSYSObjectPathName(String path,
                   String expectedObjectType)
{
  super();
  parse(path);
  // Ensure that the type is correct.
  if (!getObjectType().equals(expectedObjectType.toUpperCase()))
  {
      throw new IllegalPathNameException(path,
          IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
  }
}


/**
Constructs an QSYSObjectPathName object.
It builds an integrated file system path name to represent the object.
<pre>
QSYSObjectPathName ifsName = new
         QSYSObjectPathName("library", "name", "type");
// This line will print "/QSYS.LIB/LIBRARY.LIB/NAME.TYPE".
System.out.println(ifsName.getPath());
</pre>

@param     library   The library in which the object exists.  It must be 1-10 characters.
@param     object    The name of the object.  It must be 1-10 characters.
@param     type      The type of the object.  It must be 1-6 characters.
                     This is the AS/400 abbreviation for the type of object,
                     for example, LIB for library, or CMD for command.
                     Types can be found by prompting for the OBJTYPE
                     parameter on commands such as WRKOBJ.
**/
public QSYSObjectPathName(String library,
                          String object,
                          String type)
{
  super();
  construct(library, object, type);
}


/**
Constructs an QSYSObjectPathName object.
It builds an integrated file system path name to represent the member.
<pre>
QSYSObjectPathName ifsName = new
         QSYSObjectPathName("library", "name", "member", "MBR");
// This line will print "/QSYS.LIB/LIBRARY.LIB/NAME.FILE/MEMBER.MBR".
System.out.println(ifsName.getPath());
</pre>

@param     library   The library in which the object exists.  It must be 1-10 characters.
@param     object    The name of the object.  It must be 1-10 characters.
@param     member    The name of the member.  It must be 1-10 characters.
@param     type      The type of the object.  This must be MBR.
**/
// Note the code in this method is similar to that in the toPath method,
// and code changes here should most likely be reflected there as well.
public QSYSObjectPathName(String library,
                          String object,
                          String member,
                          String type)
{
  super();

  // Verify arguments will make a valid QSYS IFS path name.
  if (member==null)
    throw new NullPointerException("member");
  if (type==null)
    throw new NullPointerException("type");
  if (member.length() > 10 || member.length() < 1)
    throw new ExtendedIllegalArgumentException("member(" + member + ")",
       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (!type.toUpperCase().equals("MBR"))
      throw new ExtendedIllegalArgumentException("type(" + type + ")",
        ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

  // build string except for member name
  construct(library, object, "FILE");
  StringBuffer result = new StringBuffer(fullPathName_);

  // add member name
  result.append("/");
  if (member.charAt(0) == '\"')   // if quoted, store as mixed case
    memberName_ = member;
  else                             // store as uppercase
    memberName_ = member.toUpperCase();
  result.append(convertMemberName(memberName_));
  result.append(".MBR");

  // Change type to MBR
  objectType_ = "MBR";

  fullPathName_ = result.toString();
}


/**
Adds a listener to be notified when the value of any bound
property is changed.
The <b>propertyChange</b> method will be called.
@see #removePropertyChangeListener
@param listener The PropertyChangeListener.
**/
public void addPropertyChangeListener(PropertyChangeListener listener)
{
    if (listener == null)
      throw new NullPointerException("listener");
    changes_.addPropertyChangeListener(listener);
}


/**
Adds a listener to be notified when the value of any constrained
property is changed.
The <b>vetoableChange</b> method will be called.
@see #removeVetoableChangeListener
@param listener The VetoableChangeListener.
**/
public void addVetoableChangeListener(VetoableChangeListener listener)
{
    if (listener == null)
      throw new NullPointerException("listener");
    vetoListeners_.addElement(listener);
}


/**
Builds the full path name from the pieces (library, object name, type,
member name), if enough data is available.  Returns an empty string
if not successful.

@return The integrated file system path name.  An empty String
will be returned if the path cannot be created.
**/
private String buildPathName(String library, String object,
                             String member, String type)
{
    if ( !library.equals("") &&
         !object.equals("") &&
         !type.equals("") &&
        (!member.equals("") || !type.equals("MBR")) )
    {
        StringBuffer result = new StringBuffer("/QSYS.LIB/");
        if (!library.equals("QSYS"))
        {
            result.append(convertLibraryName(library));
            result.append(".LIB/");
        }
        result.append(convertObjectName(object));
        if (!member.equals(""))
        {
            result.append(".FILE/");
            result.append(convertMemberName(member));
            result.append(".MBR");
        }
        else
        {
            result.append(".");
            result.append(type);
        }
        return result.toString();
    }
    return "";
}


/**
Builds an integrated file system path name to represent the object.
It fills in the instance variables as it builds.

@param     library   The library in which the object exists.  It must be 1-10 characters.
@param     object    The name of the object.  It must be 1-10 characters.
@param     type      The type of the object.  It must be 1-6 characters.
                     This is the AS400 abbreviation for the type of object,
                     for example, LIB for library, or CMD for command.
                     Types can be found by prompting for the OBJTYPE
                     parameter on commands such as WRKOBJ.
**/
// Note the code in this method is similar to that in the toPath method,
// and code changes here should most likely be reflected there as well.
private void construct(String library,
                       String object,
                       String type)
{
  // Verify arguments will make a valid QSYS IFS path name.
  if (library==null)
    throw new NullPointerException("library");
  if (object==null)
    throw new NullPointerException("object");
  if (type==null)
    throw new NullPointerException("type");
  if (library.length() > 10 || library.length() < 1)
    throw new ExtendedIllegalArgumentException("library(" + library + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (object.length() > 10 || object.length() < 1)
    throw new ExtendedIllegalArgumentException("object(" + object + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (type.length() > 6 || type.length() < 1)
    throw new ExtendedIllegalArgumentException("type(" + type + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

  // Build up path name.
  // Start with required prefix.
  StringBuffer result = new StringBuffer("/QSYS.LIB/");

  //Process the library name.
  if (library.charAt(0) == '\"')   // if quoted, store as mixed case
    libraryName_ = library;
  else                             // store as uppercase
    libraryName_ = library.toUpperCase();
  // If library is QSYS, don't add to path, otherwise...
  if (!libraryName_.equals("QSYS"))
    result.append(convertLibraryName(libraryName_) + ".LIB/");

  // Add the object name.
  if (object.charAt(0) == '\"')   // if quoted, store as mixed case
    objectName_ = object;
  else                             // store as uppercase
    objectName_ = object.toUpperCase();
  result.append(convertObjectName(objectName_));

  // Add the object type.
  objectType_ = type.toUpperCase();
  result.append("." + objectType_);

  fullPathName_ = result.toString();
}


/**
Converts the library name to the integrated file system pathname value by
handling special values.

@param     library   The Library in which the object exists.

@return  Library The name in integrated file system path name form.
**/
static private String convertLibraryName(String library)
{
  // check to see if we have a special value
  if      (library.equals("*LIBL"))      return "%LIBL%";
  else if (library.equals("*CURLIB"))    return "%CURLIB%";
  else if (library.equals("*USRLIBL"))   return "%USRLIBL%";
  else if (library.equals("*ALL"))       return "%ALL%";
  else if (library.equals("*ALLUSR"))    return "%ALLUSR%";

  return library;
}


/**
Converts the member name to the integrated file system pathname value. by
handling special values.

@return  The member name in integrated file system path name form.
**/
static private String convertMemberName(String member)
{
  // check to see if we have a special value
  if      (member.equals("*FIRST"))     return "%FIRST%";
  else if (member.equals("*LAST"))      return "%LAST%";
  else if (member.equals("*FILE"))      return "%FILE%";
  else if (member.equals("*ALL"))       return "%ALL%";
  else if (member.equals("*NONE"))      return "%NONE%";

  return member;
}


/**
Converts the object name to the integrated file system pathname value by
handling special values.

@param     object    The name of the object.

@return  The object name in the integrated file system path name form.
**/
static private String convertObjectName(String object)
{
  // check for special value
  if (object.equals("*ALL"))    return "%ALL%";

  return object;
}


/**
Fires the veto events to all listeners.  If the change is rejected,
rollback any changes that have been made (ignore any vetoes on the
rollback).  The veto events are fired on our own 
rather than using VetoableChangeSupport because
 several items change at a time
and there may be a need to rollback several changes.

@param changes The changes to be sent to the vetoers.

@throws PropertyVetoException If a change is vetoed.
**/
void fireVetos(PropertyChangeEvent[] changes)
    throws PropertyVetoException
{
  if (vetoListeners_.size() > 0)
  {
    Vector targets;
    synchronized (this) {
        targets = (Vector) vetoListeners_.clone();
    }
    PropertyChangeEvent change;
    // loop through changes
    for (int j = 0; j < changes.length; j++)
    {
      change = changes[j];
      // only fire events if the old and new values are not the same
      if (!change.getOldValue().equals(change.getNewValue()))
      {
        // loop through veto listeners
        for (int i = 0; i < targets.size(); i++)
        {
          try
          {
            ((VetoableChangeListener)(targets.elementAt(i))).vetoableChange(change);
          }
          catch (PropertyVetoException e)
          {
            PropertyChangeEvent rollback;
            // rollback this change, reverse the old and new values
            rollback = new PropertyChangeEvent(this, change.getPropertyName(),
                change.getNewValue(), change.getOldValue());
            // notify those listeners already informed of the original change
            for (; i>=0; --i)
            {
              try
              {
                ((VetoableChangeListener)(targets.elementAt(i))).vetoableChange(rollback);
              }
              catch(PropertyVetoException z) {} // ignore any vetos on rollback
            }
            // loop through previous changes
            for (--j; j>=0; --j)
            {
              change = changes[j];
              // rollback if the old and new value are different
              if (!change.getOldValue().equals(change.getNewValue()))
              {
                // rollback this change, reverse the old and new values
                rollback = new PropertyChangeEvent(this, change.getPropertyName(),
                  change.getNewValue(), change.getOldValue());
                // loop through listeners
                for (i = 0; i < targets.size(); i++)
                {
                  try
                  {
                    ((VetoableChangeListener)(targets.elementAt(i))).vetoableChange(rollback);
                  }
                  catch(PropertyVetoException z) {} // ignore any vetos on rollback
                }
              } // end if old and new values are different
            } // end loop through any previous changes
            throw e;  // rethrow original PropertyVetoException
          } // end catch PropertyVetoException
        } // end loop through listeners
      } // end fire events if old and new value is different
    } // end loop through changes
  } // end if at least one listener
}



/**
Returns the library in which the object resides.

@return The name of the library.
**/
public String getLibraryName()
{
  return libraryName_;
}


// @D1A
/**
Returns a localized description of the object type.

@return The localized description.
**/
/* @D3D
public String getLocalizedObjectType()
{
    return QSYSObjectTypeTable.getLocalizedObjectType(objectType_, null);   // @D3C
}
*/



// @D1A
/**
Returns a localized description of an object type.

@param type                 The object type.
@return The localized description.
**/
/* @D3D
public static String getLocalizedObjectType(String type)
{
    return QSYSObjectTypeTable.getLocalizedObjectType(type, null); // @D3C
}
*/



// @D3A - Note: We need to remove this method.
// @D1A
/**
Returns a localized description of an object type.

@param type                 The object type.
@param attribute            The attribute, or null if none.
@return                     The localized description, or the type name
                            if no localized desription can be determined.
**/
//
// Design note:  I thought about allowing attribute == "" to mean
//               the same thing as null, but I decided against it, in case
//               we ever need to differentiate between a type with no 
//               attribute and a type with an attribute not specified.
//
/* @D3D 
public static String getLocalizedObjectType(String type, String attribute)
{
    return QSYSObjectTypeTable.getLocalizedObjectType(type, attribute);                    // @D3A
    // /* @D3D
    if (type == null)
        throw new NullPointerException("type");

    // Concoct the MRI key.
    StringBuffer buffer = new StringBuffer();
    buffer.append("TYPE_");

    // Add the type.  Uppercase it and remove a * if included in the first character.
    if (type.length() > 0) {
        if (type.charAt(0) == '*') {
            if (type.length() > 1)
                buffer.append(type.substring(1).toUpperCase());
            else
                throw new ExtendedIllegalArgumentException("type(" + type + ")",
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        else
            buffer.append(type.toUpperCase());
    }

    // Add the attribute if included.
    if (attribute != null) {
        buffer.append('_');
        buffer.append(attribute.toUpperCase());
    }

    String mriKey = buffer.toString();

    // Get the MRI.
    try {
        return ResourceBundleLoader.getText(mriKey);
    }
    catch(MissingResourceException e) {
        
        // If there was an attribute specified, assume the attribute is             @D2A
        // not in our list and try without an attribute specified.                  @D2A
        if (attribute != null)                                                   // @D2A
            return getLocalizedObjectType(type, null);                           // @D2A
        else                                                                     // @D2A
            return type;                                                         // @D2A

        // @D2D throw new ExtendedIllegalArgumentException("type(" + type + ")",
        // @D2D    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  //  * /
}
*/



/**
Returns the name of the member.  If this object does not represent a
member, an empty string is returned.

@return The name of the member.
**/
public String getMemberName()
{
  return memberName_;
}


/**
Returns the name of the object this path name represents.  If this
object represents a member, the object name is the name of the file
in which the member exists.

@return The name of the object.
**/
public String getObjectName()
{
  return objectName_;
}


/**
Returns type of object this path name represents.
Type is the AS/400 abbreviation for the type of object,
for example, LIB for library, or CMD for command.
Types can be found by prompting for the OBJTYPE
parameter on commands such as WRKOBJ.

@return The type of the object.
**/
public String getObjectType()
{
  return objectType_;
}


/**
Returns the fully qualified integrated file system path name.

@return The fully qualified integrated file system path name.
**/
public String getPath()
{
  return fullPathName_;
}



// @D1A
/**
Returns a list of the supported attributes for an object type.  

@param type                 The object type.
@return The list of the supported attributes.
**/
/* @D3D
public static String[] getSupportedAttributes(String type)
{
    return QSYSObjectTypeTable.getSupportedAttributes(type);    // @D3A
    / * @D3D
    if (type.equals(fileType_))
        return supportedFileAttributes_;
    else if (type.equals(pgmType_))
        return supportedPgmAttributes_;
    else
        return noAttributes_;
        * /
}
*/



// @D1A
/**
Returns a list of the supported object types.  

@return The list of the supported object types.
**/
/* @D3D
public static String[] getSupportedObjectTypes()
{
    return QSYSObjectTypeTable.getSupportedObjectTypes();  // @D3C
}
*/



/**
Extracts the library name, object name, object type, and member name
from the full integrated file system path string.  Verifies the full
integrated file system path is in fact
a valid integrated file system path name for an object
in the QSYS file system.  Fills
in instance data.

@param path The integrated file system path name to parse.
**/
private void parse(String path)
{
  int x,textOffset; // Temporary indexes used in parsing the path name.
  int textLength;   // Temporary string length for parsing.
  String ucPath = path.toUpperCase(); // Uppercase to simplify parsing.

  //------------------------------------------------------
  // Process the prefix.
  if (ucPath.indexOf("/QSYS.LIB/") != 0) // Required prefix
  {
      throw new IllegalPathNameException(path,
          IllegalPathNameException.QSYS_PREFIX_MISSING);
  }

  //------------------------------------------------------
  // Process library.
  textOffset = 10; // Move to start of library
  x = ucPath.indexOf(".LIB/",textOffset); // Find suffix after library name
  if (x > textOffset)	// If a qualifying library name was specified...
  {
    if (ucPath.charAt(textOffset) == '%')
    { // Possibly a "special" library name
      libraryName_ = ucPath.substring(textOffset, x);
      if      (libraryName_.equals("%LIBL%"))      libraryName_ = "*LIBL";
      else if (libraryName_.equals("%CURLIB%"))    libraryName_ = "*CURLIB";
      else if (libraryName_.equals("%USRLIBL%"))   libraryName_ = "*USRLIBL";
      else if (libraryName_.equals("%ALL%"))       libraryName_ = "*ALL";
      else if (libraryName_.equals("%ALLUSR%"))    libraryName_ = "*ALLUSR";
      // If none of the above, then we assume that the library
      // name is not "special" and simply take what we get...
      else libraryName_ = path.substring(textOffset, x-textOffset);
    }
    else if (ucPath.charAt(textOffset)=='\"')  // A quoted string...
      // Use whatever case the user provided.
      libraryName_ = path.substring(textOffset, x);
    else // Not a quoted string...
    { // Roll the libraryName_ to upper case for OS/400 API calls.
      libraryName_ = ucPath.substring(textOffset, x);
      // Disallow /QSYS.LIB/QSYS.LIB...
      if (libraryName_.equals("QSYS"))
      {
        throw new IllegalPathNameException(path,
          IllegalPathNameException.QSYS_SYNTAX_NOT_VALID);
      }
    }
    // Move past ".LIB/" to the first character of object name.
    textOffset = x + 5;
  }
  else if (x == -1)
    // No qualifying library name was specified...
    libraryName_ = "QSYS";  // Set library name to QSYS.
  else
  { // A ".LIB" immediately followed "/QSYS.LIB/".
    throw new IllegalPathNameException(path,
      IllegalPathNameException.LIBRARY_LENGTH_NOT_VALID);
  }
  if (libraryName_.length() > 10)  // If name is > 10 chars
  {
    throw new IllegalPathNameException(path,
      IllegalPathNameException.LIBRARY_LENGTH_NOT_VALID);
  }

  //------------------------------------------------------
  // Process object type.
  // Find last period in path name (object.type delimiter)
  x = ucPath.lastIndexOf('.');
  if (x < textOffset ||             // no type specified
      (ucPath.length()-x-1) > 6)    // type > 6 chars
  {
    throw new IllegalPathNameException(path,
      IllegalPathNameException.TYPE_LENGTH_NOT_VALID);
  }
  else
  {
    objectType_ = ucPath.substring(x+1); // note: uppercase
  }


  //------------------------------------------------------
  // Process member name.
  // Only needs to be done if the type is MBR.
  if (objectType_.equals("MBR"))
  {
    // Find end of the object (file) name
    int z = ucPath.lastIndexOf(".FILE/", x);
    if (z == -1  ||         // not found
        z < textOffset)    // not in object name portion of path
    {
      throw new IllegalPathNameException(path,
        IllegalPathNameException.MEMBER_WITHOUT_FILE);
    }
    z += 6;  // Move to first char of member name.

    // Check that member name is 1-10 chars
    if (!((x > z) && ((textLength=x-z) < 11)))
    {
      throw new IllegalPathNameException(path,
        IllegalPathNameException.MEMBER_LENGTH_NOT_VALID);
    }
    // The member name is syntactically correct.
    if (ucPath.charAt(z)=='\"')
      // A quoted string, use whatever case the user provided.
      memberName_ = path.substring(z, x);
    else
    {
      // Not a quoted string, roll to upper case for OS/400 API calls.
      memberName_ = ucPath.substring(z, x);
      // Check for special member values.
      if      (memberName_.equals("%FIRST%"))  memberName_ = "*FIRST";
      else if (memberName_.equals("%LAST%"))   memberName_ = "*LAST";
      else if (memberName_.equals("%FILE%"))   memberName_ = "*FILE";
      else if (memberName_.equals("%ALL%"))    memberName_ = "*ALL";
      else if (memberName_.equals("%NONE%"))   memberName_ = "*NONE";
    }

    x = z-6;   // Move x to period after object name.
  }

  //------------------------------------------------------
  // Process object name.
  // Try to catch the case where the library was specified
  // incorrectly, by testing if the 'object name' has a slash in
  // it but is not quoted, and the library was thought to be QSYS.
  if (libraryName_.equals("QSYS") &&
      ucPath.substring(textOffset, x).indexOf('/') != -1 &&
      ucPath.charAt(textOffset)!='\"')
  {
    throw new IllegalPathNameException(path,
      IllegalPathNameException.LIBRARY_SPECIFICATION_NOT_VALID);
  }
  // Check that object name is 1-10 chars
  if (!((x > textOffset) && ((textLength=x-textOffset) < 11)))
  {
    throw new IllegalPathNameException(path,
      IllegalPathNameException.OBJECT_LENGTH_NOT_VALID);
  }
  // The object name is syntactically correct.
  if (ucPath.charAt(textOffset)=='\"')
    // A quoted string, use whatever case the user provided.
    objectName_ = path.substring(textOffset, x);
  else
  {
    // Not a quoted string, roll to upper case for OS/400 API calls.
    objectName_ = ucPath.substring(textOffset, x);
    // Check for special object values.
    if      (objectName_.equals("%ALL%"))  objectName_ = "*ALL";
  }

  // Fill in _fullPathName instance var (with correct case)
  fullPathName_ = buildPathName(libraryName_, objectName_,
                                memberName_, objectType_);
}


/**
Restores the state of this object from an object input stream.
Used when deserializing an object.
@param in The input stream of the object being deserialized
@throws IOException If an error occurs while communicating with the AS/400.
@throws ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
{
    // Restore the non-static and non-transient fields.
    in.defaultReadObject();
    // Initialize the transient fields.
    vetoListeners_ = new Vector();
}


/**
Removes a listener from the change list.
If the listener is not on the list, do nothing.
@see #addPropertyChangeListener
@param listener The PropertyChangeListener.
**/
public void removePropertyChangeListener(PropertyChangeListener listener)
{
    if (listener == null)
      throw new NullPointerException("listener");
    changes_.removePropertyChangeListener(listener);
}


/**
Removes a listener from the veto list.
If the listener is not on the list, do nothing.
@see #addVetoableChangeListener
@param listener The VetoableChangeListener.
**/
public void removeVetoableChangeListener(VetoableChangeListener listener)
{
    if (listener == null)
      throw new NullPointerException("listener");
    vetoListeners_.removeElement(listener);
}


/**
Sets the library in which the object resides.
This is a bound and constrained property.
Note that changes to this property also affect the pathName property.

@param     library   The library in which the object exists.  It must be 1-10 characters.

@exception PropertyVetoException If the change was vetoed.
**/
public void setLibraryName(String library)
    throws PropertyVetoException
{
  // check for valid parm
  if (library==null)
  {
     throw new NullPointerException("library");
  }
  if (library.length() > 10 || library.length() < 1)
  {
     throw new ExtendedIllegalArgumentException("library(" + library + ")",
         ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }

  // Synchronize until instance variables updated to avoid
  // another setter being called which would mess up fullPathName_.
  synchronized (fullPathName_)
  {
      //Process the library name.
      String newLibraryName;;
      if (library.charAt(0) == '\"')   // if quoted, store as mixed case
        newLibraryName = library;
      else                             // store as uppercase
        newLibraryName = library.toUpperCase();
      // Build new path name.
      String newPathName = buildPathName(newLibraryName, objectName_,
                                         memberName_, objectType_);

      // OK the changes with vetoers.
      PropertyChangeEvent[] changes = new PropertyChangeEvent[2];
      PropertyChangeEvent change = new PropertyChangeEvent(this,
                 "libraryName", libraryName_, newLibraryName);
      changes[0] = change;
      change = new PropertyChangeEvent(this,
                 "path", fullPathName_, newPathName);
      changes[1] = change;
      // Tell the vetoers about the changes.  If anyone objects,
      // an exception will be thrown.
      fireVetos(changes);

      // Make the changes
      libraryName_ = newLibraryName;
      fullPathName_ = newPathName;
  }  // end synchronize block

  // Notify listeners that we made the changes.
  changes_.firePropertyChange(null, null, null);
}


/**
Sets the name of the member.  If a value other than an empty String ("")
is specified, the object type is set to MBR.
This is a bound and constrained property.
Note that changes to this property also affect the
objectType and pathName properties.

@param     member    The name of the member. It must be 10 characters or less.
                     An empty String ("") can be passed to indicate this
                     object does not represent a member.

@exception PropertyVetoException If the change was vetoed.
**/
public void setMemberName(String member)
    throws PropertyVetoException
{
  // check for valid parm
  if (member==null)
  {
     throw new NullPointerException("member");
  }

  // Synchronize until instance variables updated to avoid
  // another setter being called which would mess up fullPathName_.
  synchronized (fullPathName_)
  {
      String newMemberName;
      String newObjectType = objectType_;

      if (!member.equals(""))
      {
        // check for valid parm
        if (member.length() > 10)
        {
           throw new ExtendedIllegalArgumentException("member(" + member + ")",
               ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // the type of an object with a member is always MBR
        newObjectType = "MBR";
        if (member.charAt(0) == '\"')   // if quoted, store as mixed case
          newMemberName = member;
        else                             // store as uppercase
          newMemberName = member.toUpperCase();
      }
      else
        newMemberName = "";
      // Build new path name.
      String newPathName = buildPathName(libraryName_, objectName_,
                                     newMemberName, newObjectType);

      // OK the changes with vetoers.
      PropertyChangeEvent[] changes = new PropertyChangeEvent[3];
      PropertyChangeEvent change = new PropertyChangeEvent(this,
                 "memberName", memberName_, newMemberName);
      changes[0] = change;
      change = new PropertyChangeEvent(this,
                 "objectType", objectType_, newObjectType);
      changes[1] = change;
      change = new PropertyChangeEvent(this,
                 "path", fullPathName_, newPathName);
      changes[2] = change;
      // Tell the vetoers about the changes.  If anyone objects,
      // an exception will be thrown.
      fireVetos(changes);

      // Make the changes
      memberName_ = newMemberName;
      objectType_ = newObjectType;
      fullPathName_ = newPathName;
  }  // end synchronize block

  // Notify listeners that we made the changes.
  changes_.firePropertyChange(null, null, null);
}


/**
Sets the name of the object this path name represents.  If this
object represents a member, the object name is the name of the file
that the member is in.
This is a bound and constrained property.
Note that changes to this property also affect the pathName property.

@param     object    The name of the object.  It must be 1-10 characters.

@exception PropertyVetoException If the change was vetoed.
**/
public void setObjectName(String object)
    throws PropertyVetoException
{
  // check for valid parm
  if (object==null)
  {
     throw new NullPointerException("object");
  }
  if (object.length() > 10 || object.length() < 1)
  {
     throw new ExtendedIllegalArgumentException("object(" + object + ")",
       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }

  // Synchronize until instance variables updated to avoid
  // another setter being called which would mess up fullPathName_.
  synchronized (fullPathName_)
  {
      // Process object name.
      String newObjectName;
      if (object.charAt(0) == '\"')   // if quoted, store as mixed case
        newObjectName = object;
      else                             // store as uppercase
        newObjectName = object.toUpperCase();
      // Build new path name.
      String newPathName = buildPathName(libraryName_, newObjectName,
                                     memberName_, objectType_);

      // OK the changes with vetoers.
      PropertyChangeEvent[] changes = new PropertyChangeEvent[2];
      PropertyChangeEvent change = new PropertyChangeEvent(this,
                 "objectName", objectName_, newObjectName);
      changes[0] = change;
      change = new PropertyChangeEvent(this,
                 "path", fullPathName_, newPathName);
      changes[1] = change;
      // Tell the vetoers about the changes.  If anyone objects,
      // an exception will be thrown.
      fireVetos(changes);

      // Make the changes
      objectName_ = newObjectName;
      fullPathName_ = newPathName;
  }  // end synchronize block

  // Notify listeners that we made the changes.
  changes_.firePropertyChange(null, null, null);
}


/**
Sets type of object this path name represents.  If the type is not MBR,
the member name property will be set to an empty string.
The value will be uppercased.
This is a bound and constrained property.
Note that changes to this property also affect the
memberName and pathName properties.

@param     type      The type of the object. It must be 1-6 characters.
                     This is the AS/400 abbreviation for the type of object,
                     for example, LIB for library, or CMD for command.
                     Types can be found by prompting for the OBJTYPE
                     parameter on commands such as WRKOBJ.

@exception PropertyVetoException If the change was vetoed.
**/
public void setObjectType(String type)
    throws PropertyVetoException
{
  // check for valid parm
  if (type==null)
  {
    throw new NullPointerException("type");
  }
  if (type.length() > 6 || type.length() < 1)
  {
    throw new ExtendedIllegalArgumentException("type(" + type + ")",
       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  }

  // Synchronize until instance variables updated to avoid
  // another setter being called which would mess up fullPathName_.
  synchronized (fullPathName_)
  {
      // Process object type.
      String newMemberName = memberName_;
      String newObjectType = type.toUpperCase();
      if (!newObjectType.equals("MBR"))
          newMemberName = "";
      // Build new path name.
      String newPathName = buildPathName(libraryName_, objectName_,
                                     newMemberName, newObjectType);

      // OK the changes with vetoers.
      PropertyChangeEvent[] changes = new PropertyChangeEvent[3];
      PropertyChangeEvent change = new PropertyChangeEvent(this,
                 "objectType", objectType_, newObjectType);
      changes[0] = change;
      change = new PropertyChangeEvent(this,
                "memberName", memberName_, newMemberName);
      changes[1] = change;
      change = new PropertyChangeEvent(this,
                 "path", fullPathName_, newPathName);
      changes[2] = change;
      // Tell the vetoers about the changes.  If anyone objects,
      // an exception will be thrown.
      fireVetos(changes);

      // Make the changes
      objectType_ = newObjectType;
      memberName_ = newMemberName;
      fullPathName_ = newPathName;
  }  // end synchronize block

  // Notify listeners that we made the changes.
  changes_.firePropertyChange(null, null, null);
}


/**
Sets the integrated file system path name for this object.
This is a bound and constrained property.
Note that changes to this property also affect the libraryName,
memberName, objectName, and objectType properties.

@param     path      The fully qualified integrated file system name of an
                     object in the QSYS file system.

@exception PropertyVetoException If the change was vetoed.
**/
public void setPath(String path)
    throws PropertyVetoException
{
  // check for valid parm
  if (path==null)
  {
    throw new NullPointerException("path");
  }

  // Synchronize until instance variables updated to avoid
  // another setter being called which would mess up fullPathName_.
  synchronized (fullPathName_)
  {
      // Save existing values in case parse fails, we can set back to
      // valid values.
      String oldPathName = fullPathName_;
      String oldLibraryName = libraryName_;
      String oldObjectName = objectName_;
      String oldMemberName = memberName_;
      String oldObjectType = objectType_;
      memberName_ = "";  // set member name back to nothing in case not member
      // Try out new path name
      try {
        parse(path);
      }
      catch (IllegalPathNameException e)
      {
        // restore old values to instance variables
        fullPathName_=oldPathName;
        libraryName_=oldLibraryName;
        objectName_=oldObjectName;
        memberName_=oldMemberName;
        objectType_=oldObjectType;
        // rethrow exception
        throw e;
      }

      // Must set back object to old state before calling vetoers.
      // If change is not vetoed, we will reset the values to the
      // new values.
      String newPathName = fullPathName_;
      String newLibraryName = libraryName_;
      String newObjectName = objectName_;
      String newMemberName = memberName_;
      String newObjectType = objectType_;
      // restore old values to instance variables
      fullPathName_=oldPathName;
      libraryName_=oldLibraryName;
      objectName_=oldObjectName;
      memberName_=oldMemberName;
      objectType_=oldObjectType;

      // OK the changes with vetoers.
      PropertyChangeEvent[] changes = new PropertyChangeEvent[5];
      PropertyChangeEvent change = new PropertyChangeEvent(this,
                 "path", fullPathName_, newPathName);
      changes[0] = change;
      change = new PropertyChangeEvent(this,
                 "libraryName", libraryName_, newLibraryName);
      changes[1] = change;
      change = new PropertyChangeEvent(this,
                 "objectName", objectName_, newObjectName);
      changes[2] = change;
      change = new PropertyChangeEvent(this,
                 "memberName", memberName_, newMemberName);
      changes[3] = change;
      change = new PropertyChangeEvent(this,
                 "objectType", objectType_, newObjectType);
      changes[4] = change;
      // Tell the vetoers about the changes.  If anyone objects,
      // an exception will be thrown.
      fireVetos(changes);

      // Make the changes
      libraryName_ = newLibraryName;
      objectName_ = newObjectName;
      memberName_ = newMemberName;
      objectType_ = newObjectType;
      fullPathName_ = newPathName;
  }  // end synchronize block

  // Notify listeners that we made the changes.
  changes_.firePropertyChange(null, null, null);
}


/**
Builds an integrated file system path name to represent the object.

@param     library   The library the object is in. It must be 1-10 characters.
@param     object    The name of the object.  It must be 1-10 characters.
@param     type      The type of the object.  It must be 1-6 characters.
                     This is the AS/400 abbreviation for the type of object,
                     for example, LIB for library, or CMD for command.
                     Types can be found by prompting for the OBJTYPE
                     parameter on commands such as WRKOBJ.

@return The integrated file system name for the object.
**/
// Note the code in this method is similar to that in the construct
// method, and code changes here should most likely be reflected there as well.
static public String toPath(String library,
                            String object,
                            String type)
{
  // Verify arguments will make a valid QSYS IFS path name.
  if (library==null)
    throw new NullPointerException("library");
  if (object==null)
    throw new NullPointerException("object");
  if (type==null)
    throw new NullPointerException("type");
  if (library.length() > 10 || library.length() < 1)
    throw new ExtendedIllegalArgumentException("library(" + library + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (object.length() > 10 || object.length() < 1)
    throw new ExtendedIllegalArgumentException("object(" + object + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (type.length() > 6 || type.length() < 1)
    throw new ExtendedIllegalArgumentException("type(" + type + ")",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

  // Build up path name.
  // Start with required prefix.
  StringBuffer result = new StringBuffer("/QSYS.LIB/");

  //Process the library name.
  if (library.charAt(0) == '\"')   // if quoted, store as mixed case
  {
    result.append(library);
    result.append(".LIB/");
  }
  else                             // store as uppercase
  {
    library = library.toUpperCase();
    // If library is QSYS, don't add to path, otherwise...
    if (!library.equals("QSYS"))
    {
       result.append(convertLibraryName(library));
       result.append(".LIB/");
    }
  }

  // Add the object name.
  if (object.charAt(0) == '\"')   // if quoted, store as mixed case
    result.append(object);
  else                             // store as uppercase
    result.append(convertObjectName(object.toUpperCase()));

  // Add the object type.
  result.append("." + type.toUpperCase());

  return result.toString();
}


/**
Builds an integrated file system path name to represent the member.

@param     library   The library the object is in.  It must be 1-10 characters.
@param     object    The name of the object.  It must be 1-10 characters.
@param     member    The name of the member.  It must be 1-10 characters.
@param     type      The type of the object.  This must be MBR.

@return The integrated file system name for the object.
**/
// Note the code in this method is similar to that in the 4-parm ctor,
// and code changes here should most likely be reflected there as well.
static public String toPath(String library,
                            String object,
                            String member,
                            String type)
{
  // Verify arguments will make a valid QSYS IFS path name.
  if (member==null)
    throw new NullPointerException("member");
  if (type==null)
    throw new NullPointerException("type");
  if (member.length() > 10 || member.length() < 1)
    throw new ExtendedIllegalArgumentException("member(" + member + ")",
       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
  if (!type.toUpperCase().equals("MBR"))
      throw new ExtendedIllegalArgumentException("type(" + type + ")",
        ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

  // build string except for member name
  StringBuffer result = new StringBuffer(toPath(library, object, "FILE"));

  // add member name
  result.append("/");
  if (member.charAt(0) == '\"')   // if quoted, store as mixed case
    result.append(member);
  else                             // store as uppercase
    result.append(convertMemberName(member.toUpperCase()));
  result.append(".MBR");

  return result.toString();
}


}

