///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  QSYSObjectPathName.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// Usage notes:
//
//    For IBM Toolbox for Java developers, this class is generally used in one
//    of the following ways:
//
//     1) QSYSObjectPathName ifs = new QSYSObjectPathName(myIFSString);
//        if (!ifs.getObjectType().equals("MYTYPE1") &&
//            !ifs.getObjectType().equals("MYTYPE2"))
//           throw new IllegalPathNameException(myIFSString,
//                 IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
//
//     2) QSYSObjectPathName ifs
//           = new QSYSObjectPathName(myIFSString, "MYTYPE");
//
//    For public usage, see the class prolog.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.util.EventListener;

/**
 The QSYSObjectPathName class provides an integrated file system path name that represents an object in the QSYS library file system.
 <p>QSYSObjectPathName objects generate the following events:
 <ul>
 <li>PropertyChangeEvent
 <li>VetoableChangeEvent
 </ul>
 <p>This object can be used in two ways:
 <ul compact>
 <li>To parse the integrated file system path name to ensure correct syntax and to extract the library, object, member, and object type.
 <li>To build an integrated file system path name from a library, object, and member or object type.
 </ul>
 IllegalPathNameExceptions are thrown if errors are found.
 <p>Objects in the QSYS file system have integrated file system names with the following format for objects or members in a library other than QSYS:
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
 <p>In an integrated file system path name, special values, such as <i>*ALL</i>, that begin with an asterisk are not depicted with an asterisk but with leading and trailing percent signs (<i>%ALL%</i>). In the integrated file system, an asterisk is a wildcard character.  The following special values are recognized by this class:
 <pre>
 Library name: %ALL%(*ALL), %ALLUSR%(*ALLUSR), %CURLIB%(*CURLIB), %LIBL%(*LIBL), %USRLIBL%(*USRLIBL)
 Object name:  %ALL%(*ALL)
 Member name:  %ALL%(*ALL), %FILE%(*FILE), %FIRST%(*FIRST), %LAST%(*LAST) %NONE%(*NONE)
 </pre>
 <p>The path name will be in uppercase.  If case needs to be preserved for a library, object, or member name, quotation marks should be used around the names.  For example:
 <pre>
 QSYSObjectPathName path = new QSYSObjectPathName("/QSYS.LIB/\"MixedCase\".LIB/\"lowercase\".FILE");
 </pre>
 Examples:
 <ul>
 <li>This code will extract the pieces of an integrated file system name that represents a file:
 <pre>
 QSYSObjectPathName path = new QSYSObjectPathName("/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE");
 System.out.println(path.getLibraryName());  // Will print "QGPL"
 System.out.println(path.getObjectName());   // Will print "ACCOUNTS"
 System.out.println(path.getObjectType());   // Will print "FILE"
 </pre>
 <li>This code will extract the pieces of an integrated file system name that represents a member:
 <pre>
 QSYSObjectPathName path = new QSYSObjectPathName("/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE/PAYABLE.MBR");
 System.out.println(path.getLibraryName());  // Will print "QGPL"
 System.out.println(path.getObjectName());   // Will print "ACCOUNTS"
 System.out.println(path.getMemberName());   // Will print "PAYABLE"
 System.out.println(path.getObjectType());   // Will print "MBR"
 </pre>
 <li>This code will build an integrated file system name for a file:
 <pre>
 QSYSObjectPathName path = new QSYSObjectPathName("QGPL", "ACCOUNTS", "FILE");
 // Will print "/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE"
 System.out.println(path.getPath());
 </pre>
 <li>This code will build an integrated file system name for a member:
 <pre>
 QSYSObjectPathName path = new QSYSObjectPathName("QGPL", "ACCOUNTS", "PAYABLE", "MBR");
 // Will print "/QSYS.LIB/QGPL.LIB/ACCOUNTS.FILE/PAYABLE.MBR"
 System.out.println(path.getPath());
 </pre>
 </ul>
 **/
public class QSYSObjectPathName implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    // Full IFS path name.
    private String path_ = "";
    // Library name.
    private String libraryName_ = "";
    // Object name.
    private String objectName_ = "";
    // Member name.
    private String memberName_ = "";
    // Object type.
    private String objectType_ = "";

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a QSYSObjectPathName object.  It creates an integrated file system path name for an object in the QSYS file system.
     **/
    public QSYSObjectPathName()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing QSYSObjectPathName object.");
    }

    /**
     Constructs a QSYSObjectPathName object.  It creates an integrated file system path name for an object in the QSYS file system.
     @param  path  The fully qualified integrated file system name of an object in the QSYS file system.
     **/
    public QSYSObjectPathName(String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing QSYSObjectPathName object, path: " + path);
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        parse(path);
    }

    // Constructs a QSYSObjectPathName object.  It creates an integrated file system path name for an object in the QSYS file system.
    // @param  path  The fully qualified integrated file system name of an object in the QSYS file system.
    // @param  objectType  The type of object <i>path</i> should represent.
    QSYSObjectPathName(String path, String objectType)
    {
        this(path);
        // Ensure that the type is correct.
        if (!objectType_.equals(objectType.toUpperCase()))
        {
            Trace.log(Trace.ERROR, "Object type is not valid, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
        }
    }

    /**
     Constructs a QSYSObjectPathName object.  It builds an integrated file system path name to represent the object.
     <pre>
     QSYSObjectPathName ifsName = new QSYSObjectPathName("library", "name", "type");
     // This line will print "/QSYS.LIB/LIBRARY.LIB/NAME.TYPE".
     System.out.println(ifsName.getPath());
     </pre>
     @param  libraryName  The library in which the object exists.  It must be 1-10 characters.
     @param  objectName  The name of the object.  It must be 1-10 characters.
     @param  objectType  The type of the object.  It must be 1-6 characters.  This is the iSeries server abbreviation for the type of object, for example, LIB for library, or CMD for command.  Types can be found by prompting for the OBJTYPE parameter on commands such as WRKOBJ.
     **/
    public QSYSObjectPathName(String libraryName, String objectName, String objectType)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing QSYSObjectPathName object, libraryName: " + libraryName + " objectName: " + objectName + " objectType: " + objectType);
        // Verify arguments will make a valid QSYS IFS path name.
        checkLibraryName(libraryName);
        checkObjectName(objectName);
        checkObjectType(objectType);

          // If quoted, store as mixed case, else store as uppercase.
          libraryName_ = libraryName.charAt(0) == '\"' ? libraryName : libraryName.toUpperCase();
        // If quoted, store as mixed case, else store as uppercase.
        objectName_ = objectName.charAt(0) == '\"' ? objectName : objectName.toUpperCase();
        objectType_ = objectType.toUpperCase();

        path_ = buildPathName(libraryName_, objectName_, "", objectType_);
    }

    /**
     Constructs a QSYSObjectPathName object.  It builds an integrated file system path name to represent the member.
     <pre>
     QSYSObjectPathName ifsName = new QSYSObjectPathName("library", "name", "member", "MBR");
     // This line will print "/QSYS.LIB/LIBRARY.LIB/NAME.FILE/MEMBER.MBR".
     System.out.println(ifsName.getPath());
     </pre>
     @param  libraryName  The library in which the object exists.  It must be 1-10 characters.
     @param  objectName  The name of the object.  It must be 1-10 characters.
     @param  memberName  The name of the member.  It must be 1-10 characters.
     @param  objectType  The type of the object.  This must be "MBR".
     **/
    public QSYSObjectPathName(String libraryName, String objectName, String memberName, String objectType)
    {
        // Note the code in this method is similar to that in the toPath method, and code changes here should most likely be reflected there as well.
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing QSYSObjectPathName object, libraryName: " + libraryName + " objectName: " + objectName + " memberName: " + memberName + " objectType: " + objectType);

        // Verify arguments will make a valid QSYS IFS path name.
        checkLibraryName(libraryName);
        checkObjectName(objectName);
        checkMemberName(memberName);
        checkObjectTypeIsMember(objectType);

        // If quoted, store as mixed case, else store as uppercase.
        libraryName_ = libraryName.charAt(0) == '\"' ? libraryName : libraryName.toUpperCase();
        // If quoted, store as mixed case, else store as uppercase.
        objectName_ = objectName.charAt(0) == '\"' ? objectName : objectName.toUpperCase();
        memberName_ = memberName.charAt(0) == '\"' ? memberName : memberName.toUpperCase();
        objectType_ = "MBR";

        path_ = buildPathName(libraryName_, objectName_, memberName_, objectType_);
    }

    /**
     Adds a listener to be notified when the value of any bound property is changed.  The <b>propertyChange</b> method will be called.
     @param  listener  The listener object.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        checkListener(listener);
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Adds a listener to be notified when the value of any constrained property is changed.  The <b>vetoableChange</b> method will be called.
     @param  listener  The listener object.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        checkListener(listener);
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    // Builds the full path name from the pieces (library name, object name, object type, member name), if enough data is available.  Returns an empty string if not successful.
    // @return  The integrated file system path name.  An empty String will be returned if the path cannot be created.
    private static String buildPathName(String libraryName, String objectName, String memberName, String objectType)
    {
        if (!libraryName.equals("") && !objectName.equals("") && !objectType.equals("") && (!memberName.equals("") || !objectType.equals("MBR")))
        {
            StringBuffer result = new StringBuffer(64);
            result.append("/QSYS.LIB/");
            if (!libraryName.equals("QSYS"))
            {
                result.append(convertLibraryName(libraryName));
                result.append(".LIB/");
            }
            result.append(convertObjectName(objectName));
            if (!memberName.equals(""))
            {
                result.append(".FILE/");
                result.append(convertMemberName(memberName));
                result.append(".MBR");
            }
            else
            {
                result.append(".");
                result.append(objectType);
            }
            return result.toString();
        }
        return "";
    }

    static void checkObjectTypeIsMember(String objectType)
    {
        if (objectType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'objectType' is null.");
            throw new NullPointerException("objectType");
        }
        if (!objectType.toUpperCase().equals("MBR"))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'objectType' is not valid: " + objectType);
            throw new ExtendedIllegalArgumentException("objectType (" + objectType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }
    static void checkMemberName(String memberName)
    {
        if (memberName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'memberName' is null.");
            throw new NullPointerException("memberName");
        }
        if (memberName.length() < 1 || memberName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'memberName' is not valid: '" + memberName + "'");
            throw new ExtendedIllegalArgumentException("memberName (" + memberName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }
    static void checkListener(EventListener listener)
    {
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
    }
    static void checkLibraryName(String libraryName)
    {
        if (libraryName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'libraryName' is null.");
            throw new NullPointerException("libraryName");
        }
        if (libraryName.length() < 1 || libraryName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'libraryName' is not valid: '" + libraryName + "'");
            throw new ExtendedIllegalArgumentException("libraryName (" + libraryName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }
    static void checkObjectName(String objectName)
    {
        if (objectName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'objectName' is null.");
            throw new NullPointerException("objectName");
        }
        if (objectName.length() < 1 || objectName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'objectName' is not valid: '" + objectName + "'");
            throw new ExtendedIllegalArgumentException("objectName (" + objectName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }
    static void checkObjectType(String objectType)
    {
        if (objectType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'objectType' is null.");
            throw new NullPointerException("objectType");
        }
        if (objectType.length() < 1 || objectType.length() > 6)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'objectType' is not valid: '" + objectType + "'");
            throw new ExtendedIllegalArgumentException("objectType (" + objectType + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
    }

    // Converts the library name to the integrated file system pathname value by handling special values.
    // @param  libraryName  The library in which the object exists.
    // @return  The library name in integrated file system path name form.
    private static String convertLibraryName(String libraryName)
    {
        // Check to see if we have a special value.
        if (libraryName.equals("*LIBL")) return "%LIBL%";
        if (libraryName.equals("*CURLIB")) return "%CURLIB%";
        if (libraryName.equals("*USRLIBL")) return "%USRLIBL%";
        if (libraryName.equals("*ALL")) return "%ALL%";
        if (libraryName.equals("*ALLUSR")) return "%ALLUSR%";
        return libraryName;
    }

    // Converts the member name to the integrated file system pathname value by handling special values.
    // @param  memberName  The name of the member.
    // @return  The member name in integrated file system path name form.
    private static String convertMemberName(String memberName)
    {
        // Check to see if we have a special value.
        if (memberName.equals("*FIRST")) return "%FIRST%";
        if (memberName.equals("*LAST")) return "%LAST%";
        if (memberName.equals("*FILE")) return "%FILE%";
        if (memberName.equals("*ALL")) return "%ALL%";
        if (memberName.equals("*NONE")) return "%NONE%";
        return memberName;
    }

    // Converts the object name to the integrated file system pathname value by handling special values.
    // @param  objectName  The name of the object.
    // @return  The object name in the integrated file system path name form.
    static private String convertObjectName(String objectName)
    {
        // Check for special value.
        if (objectName.equals("*ALL")) return "%ALL%";
        return objectName;
    }

    /**
     Returns the library in which the object resides.
     @return  The name of the library.
     **/
    public String getLibraryName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting library name: " + libraryName_);
        return libraryName_;
    }

    /**
     Returns the name of the member.  If this object does not represent a member, an empty string is returned.
     @return  The name of the member.
     **/
    public String getMemberName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting member name: " + memberName_);
        return memberName_;
    }

    /**
     Returns the name of the object this path name represents.  If this object represents a member, the object name is the name of the file in which the member exists.
     @return  The name of the object.
     **/
    public String getObjectName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting object name: " + objectName_);
        return objectName_;
    }

    /**
     Returns type of object this path name represents.  Type is the iSeries server abbreviation for the type of object, for example, LIB for library, or CMD for command.  Types can be found by prompting for the OBJTYPE parameter on commands such as WRKOBJ.
     @return  The type of the object.
     **/
    public String getObjectType()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting object type: " + objectType_);
        return objectType_;
    }

    /**
     Returns the fully qualified integrated file system path name.
     @return  The fully qualified integrated file system path name.
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting path: " + path_);
        return path_;
    }

    // Extracts the library name, object name, object type, and member name from the full integrated file system path string.  Verifies the full integrated file system path is in fact a valid integrated file system path name for an object in the QSYS file system.  Fills in instance data.
    // @param  path  The integrated file system path name to parse.
    private void parse(String path)
    {
        // Uppercase to simplify parsing.
        String upperCasePath = path.toUpperCase();

        //------------------------------------------------------
        // Process the prefix.
        //------------------------------------------------------

        // Required prefix.
        if (upperCasePath.indexOf("/QSYS.LIB/") != 0)
        {
            Trace.log(Trace.ERROR, "Object not in QSYS file system, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.QSYS_PREFIX_MISSING);
        }

        //------------------------------------------------------
        // Process library.
        //------------------------------------------------------

        // Move to start of library.
        int currentOffset = 10;
        // Find suffix after library name.
        int nextOffset = upperCasePath.indexOf(".LIB/", currentOffset);
        // If a qualifying library name was specified.
        if (nextOffset > currentOffset)
        {
            // If quoted, store as mixed case, else store as uppercase.
            libraryName_ = upperCasePath.charAt(currentOffset) == '\"' ? path.substring(currentOffset, nextOffset) : upperCasePath.substring(currentOffset, nextOffset);
            // Disallow /QSYS.LIB/QSYS.LIB.
            if (libraryName_.equals("QSYS"))
            {
                Trace.log(Trace.ERROR, "Object in library QSYS specified incorrectly, path: '" + path + "'");
                throw new IllegalPathNameException(path, IllegalPathNameException.QSYS_SYNTAX_NOT_VALID);
            }
            if (libraryName_.charAt(0) == '%')
            {
                // Possibly a "special" library name.
                if (libraryName_.equals("%LIBL%")) libraryName_ = "*LIBL";
                else if (libraryName_.equals("%CURLIB%")) libraryName_ = "*CURLIB";
                else if (libraryName_.equals("%USRLIBL%")) libraryName_ = "*USRLIBL";
                else if (libraryName_.equals("%ALL%")) libraryName_ = "*ALL";
                else if (libraryName_.equals("%ALLUSR%")) libraryName_ = "*ALLUSR";
            }
            // Move past ".LIB/" to the first character of object name.
            currentOffset = nextOffset + 5;
        }
        else if (nextOffset == -1)
        {
            // No qualifying library name was specified, set library name to QSYS.
            libraryName_ = "QSYS";
        }
        else
        {
            // A ".LIB" immediately followed "/QSYS.LIB/".
            Trace.log(Trace.ERROR, "Length of the library name is not valid, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.LIBRARY_LENGTH_NOT_VALID);
        }
        // If name is > 10 chars.
        if (libraryName_.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of the library name is not valid, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.LIBRARY_LENGTH_NOT_VALID);
        }

        //------------------------------------------------------
        // Process object type.
        //------------------------------------------------------

        // Find last period in path name (object.type delimiter).
        nextOffset = upperCasePath.lastIndexOf('.');
        // If no type specified or type > 6 chars.
        if (nextOffset < currentOffset || upperCasePath.length() - nextOffset - 1 > 6)
        {
            Trace.log(Trace.ERROR, "Length of the object type is not valid, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.TYPE_LENGTH_NOT_VALID);
        }
        objectType_ = upperCasePath.substring(nextOffset + 1);

        //------------------------------------------------------
        // Process member name.
        //------------------------------------------------------

        // Only needs to be done if the type is MBR.
        if (objectType_.equals("MBR"))
        {
            // Find end of the object (file) name.
            int memberOffset = upperCasePath.lastIndexOf(".FILE/", nextOffset);
            // If not found or not in object name portion of path.
            if (memberOffset == -1 || memberOffset < currentOffset)
            {
                Trace.log(Trace.ERROR, "Member is not contained in a file, path: '" + path + "'");
                throw new IllegalPathNameException(path, IllegalPathNameException.MEMBER_WITHOUT_FILE);
            }
            // Move to first char of member name.
            memberOffset += 6;

            // Check that member name is 1-10 chars
            if (nextOffset < memberOffset || nextOffset - memberOffset > 10)
            {
                Trace.log(Trace.ERROR, "Length of the member name is not valid, path: '" + path + "'");
                throw new IllegalPathNameException(path, IllegalPathNameException.MEMBER_LENGTH_NOT_VALID);
            }
            // The member name is syntactically correct.
            // If quoted, store as mixed case, else store as uppercase.
            memberName_ = upperCasePath.charAt(memberOffset) == '\"' ? path.substring(memberOffset, nextOffset) : upperCasePath.substring(memberOffset, nextOffset);
            if (memberName_.charAt(0) == '%')
            {
                // Check for special member values.
                if (memberName_.equals("%FIRST%")) memberName_ = "*FIRST";
                else if (memberName_.equals("%LAST%")) memberName_ = "*LAST";
                else if (memberName_.equals("%FILE%")) memberName_ = "*FILE";
                else if (memberName_.equals("%ALL%")) memberName_ = "*ALL";
                else if (memberName_.equals("%NONE%")) memberName_ = "*NONE";
            }
            // Move to period after object name.
            nextOffset = memberOffset - 6;
        }

        //------------------------------------------------------
        // Process object name.
        //------------------------------------------------------

        // Try to catch the case where the library was specified incorrectly, by testing if the 'object name' has a slash in it but is not quoted, and the library was thought to be QSYS.
        if (libraryName_.equals("QSYS") && upperCasePath.substring(currentOffset, nextOffset).indexOf('/') != -1 && upperCasePath.charAt(currentOffset) != '\"')
        {
            Trace.log(Trace.ERROR, "Library not specified correctly, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.LIBRARY_SPECIFICATION_NOT_VALID);
        }
        // Check that object name is 1-10 chars.
        if (nextOffset < currentOffset || nextOffset - currentOffset > 10)
        {
            Trace.log(Trace.ERROR, "Length of the object name is not valid, path: '" + path + "'");
            throw new IllegalPathNameException(path, IllegalPathNameException.OBJECT_LENGTH_NOT_VALID);
        }
        // The object name is syntactically correct.
        // If quoted, store as mixed case, else store as uppercase.
        objectName_ = upperCasePath.charAt(currentOffset) == '\"' ? path.substring(currentOffset, nextOffset) : upperCasePath.substring(currentOffset, nextOffset);
        // Check for special object values.
        if (objectName_.equals("%ALL%")) objectName_ = "*ALL";

        // Fill in path_ instance var (with correct case).
        path_ = buildPathName(libraryName_, objectName_, memberName_, objectType_);
    }

    /**
     Removes a listener from the change list.  If the listener is not on the list, do nothing.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        checkListener(listener);
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes a listener from the veto list.  If the listener is not on the list, do nothing.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        checkListener(listener);
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Sets the library in which the object resides.  This is a bound and constrained property.  Note that changes to this property also affect the pathName property.
     @param  libraryName  The library in which the object exists.  It must be 1-10 characters.
     @exception  PropertyVetoException  If the change was vetoed.
     **/
    public void setLibraryName(String libraryName) throws PropertyVetoException
    {
        // Check for valid parameter.
        checkLibraryName(libraryName);

        // Process the library name, if quoted, store as mixed case, else store as uppercase.
        String newLibraryName = libraryName.charAt(0) == '\"' ? libraryName : libraryName.toUpperCase();
        // Build new path.
        String newPath = buildPathName(newLibraryName, objectName_, memberName_, objectType_);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Make the changes.
            libraryName_ = newLibraryName;
            path_ = newPath;
        }
        else
        {
            String oldLibraryName = libraryName_;
            String oldPath = path_;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("libraryName", oldLibraryName, newLibraryName);
                vetoableChangeListeners_.fireVetoableChange("path", oldPath, newPath);
            }

            // Make the changes.
            libraryName_ = newLibraryName;
            path_ = newPath;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("libraryName", oldLibraryName, newLibraryName);
                propertyChangeListeners_.firePropertyChange("path", oldPath, newPath);
            }
        }
    }

    /**
     Sets the name of the member.  If a value other than an empty String ("") is specified, the object type is set to MBR.  This is a bound and constrained property.  Note that changes to this property also affect the objectType and pathName properties.
     @param  memberName  The name of the member.  It must be 10 characters or less.  An empty String ("") can be passed to indicate this object does not represent a member.
     @exception  PropertyVetoException  If the change was vetoed.
     **/
    public void setMemberName(String memberName) throws PropertyVetoException
    {
        // Check for valid parameter.
        if (memberName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'memberName' is null.");
            throw new NullPointerException("memberName");
        }
        if (memberName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'memberName' is not valid: '" + memberName + "'");
            throw new ExtendedIllegalArgumentException("memberName (" + memberName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        // If not empty and quoted, store as mixed case, else store as uppercase.
        String newMemberName = memberName.length() != 0 && memberName.charAt(0) == '\"' ? memberName : memberName.toUpperCase();
        // The type of an object with a member is always MBR.
        String newObjectType = memberName.length() != 0 ? "MBR" : objectType_;
        // Build new path name.
        String newPath = buildPathName(libraryName_, objectName_, newMemberName, newObjectType);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Make the changes.
            memberName_ = newMemberName;
            objectType_ = newObjectType;
            path_ = newPath;
        }
        else
        {
            String oldMemberName = memberName_;
            String oldObjectType = objectType_;
            String oldPath = path_;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("memberName", oldMemberName, newMemberName);
                vetoableChangeListeners_.fireVetoableChange("objectType", oldObjectType, newObjectType);
                vetoableChangeListeners_.fireVetoableChange("path", oldPath, newPath);
            }

            // Make the changes.
            memberName_ = newMemberName;
            objectType_ = newObjectType;
            path_ = newPath;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("memberName", oldMemberName, newMemberName);
                propertyChangeListeners_.firePropertyChange("objectType", oldObjectType, newObjectType);
                propertyChangeListeners_.firePropertyChange("path", oldPath, newPath);
            }
        }
    }

    /**
     Sets the name of the object this path name represents.  If this object represents a member, the object name is the name of the file that the member is in.  This is a bound and constrained property.  Note that changes to this property also affect the pathName property.
     @param  objectName  The name of the object.  It must be 1-10 characters.
     @exception  PropertyVetoException  If the change was vetoed.
     **/
    public void setObjectName(String objectName) throws PropertyVetoException
    {
        // Check for valid parameter.
        checkObjectName(objectName);

        // Process object name. if quoted, store as mixed case else store as uppercase.
        String newObjectName = objectName.charAt(0) == '\"' ? objectName : objectName.toUpperCase();
        // Build new path name.
        String newPath = buildPathName(libraryName_, newObjectName, memberName_, objectType_);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Make the changes.
            objectName_ = newObjectName;
            path_ = newPath;
        }
        else
        {
            String oldObjectName = objectName_;
            String oldPath = path_;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("objectName", oldObjectName, newObjectName);
                vetoableChangeListeners_.fireVetoableChange("path", oldPath, newPath);
            }

            // Make the changes.
            objectName_ = newObjectName;
            path_ = newPath;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("objectName", oldObjectName, newObjectName);
                propertyChangeListeners_.firePropertyChange("path", oldPath, newPath);
            }
        }
    }

    /**
     Sets type of object this path name represents.  If the type is not MBR, the member name property will be set to an empty string.  The value will be uppercased.  This is a bound and constrained property.  Note that changes to this property also affect the memberName and pathName properties.
     @param  objectType  The type of the object. It must be 1-6 characters.  This is the iSeries server abbreviation for the type of object, for example, LIB for library, or CMD for command.  Types can be found by prompting for the OBJTYPE parameter on commands such as WRKOBJ.
     @exception  PropertyVetoException  If the change was vetoed.
     **/
    public void setObjectType(String objectType) throws PropertyVetoException
    {
        // Check for valid parameter.
        checkObjectType(objectType);

        // Process object type.
        String newObjectType = objectType.toUpperCase();
        String newMemberName = newObjectType.equals("MBR") ? memberName_ : "";
        // Build new path name.
        String newPath = buildPathName(libraryName_, objectName_, newMemberName, newObjectType);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Make the changes.
            memberName_ = newMemberName;
            objectType_ = newObjectType;
            path_ = newPath;
        }
        else
        {
            String oldMemberName = memberName_;
            String oldObjectType = objectType_;
            String oldPath = path_;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("memberName", oldMemberName, newMemberName);
                vetoableChangeListeners_.fireVetoableChange("objectType", oldObjectType, newObjectType);
                vetoableChangeListeners_.fireVetoableChange("path", oldPath, newPath);
            }

            // Make the changes.
            objectType_ = newObjectType;
            memberName_ = newMemberName;
            path_ = newPath;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("memberName", oldMemberName, newMemberName);
                propertyChangeListeners_.firePropertyChange("objectType", oldObjectType, newObjectType);
                propertyChangeListeners_.firePropertyChange("path", oldPath, newPath);
            }
        }
    }

    /**
     Sets the integrated file system path name for this object.  This is a bound and constrained property.  Note that changes to this property also affect the libraryName, memberName, objectName, and objectType properties.
     @param  path  The fully qualified integrated file system name of an object in the QSYS file system.
     @exception  PropertyVetoException  If the change was vetoed.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        // Verify path is valid IFS path name.
        QSYSObjectPathName ifs = new QSYSObjectPathName(path);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Make the changes.
            path_ = ifs.path_;
            libraryName_ = ifs.libraryName_;
            objectName_ = ifs.objectName_;
            memberName_ = ifs.memberName_;
            objectType_ = ifs.objectType_;
        }
        else
        {
            String newPath = ifs.path_;
            String newLibraryName = ifs.libraryName_;
            String newObjectName = ifs.objectName_;
            String newMemberName = ifs.memberName_;
            String newObjectType = ifs.objectType_;

            String oldPath = path_;
            String oldLibraryName = libraryName_;
            String oldObjectName = objectName_;
            String oldMemberName = memberName_;
            String oldObjectType = objectType_;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("path", oldPath, newPath);
                vetoableChangeListeners_.fireVetoableChange("libraryName", oldLibraryName, newLibraryName);
                vetoableChangeListeners_.fireVetoableChange("objectName", oldObjectName, newObjectName);
                vetoableChangeListeners_.fireVetoableChange("memberName", oldMemberName, newMemberName);
                vetoableChangeListeners_.fireVetoableChange("objectType", oldObjectType, newObjectType);
            }

            // Make the changes.
            path_ = newPath;
            libraryName_ = newLibraryName;
            objectName_ = newObjectName;
            memberName_ = newMemberName;
            objectType_ = newObjectType;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("path", oldPath, newPath);
                propertyChangeListeners_.firePropertyChange("libraryName", oldLibraryName, newLibraryName);
                propertyChangeListeners_.firePropertyChange("objectName", oldObjectName, newObjectName);
                propertyChangeListeners_.firePropertyChange("memberName", oldMemberName, newMemberName);
                propertyChangeListeners_.firePropertyChange("objectType", oldObjectType, newObjectType);
            }
        }
    }

    /**
     Builds an integrated file system path name to represent the object.
     @param  libraryName  The library the object is in. It must be 1-10 characters.
     @param  objectName  The name of the object.  It must be 1-10 characters.
     @param  objectType  The type of the object.  It must be 1-6 characters.  This is the iSeries server abbreviation for the type of object, for example, LIB for library, or CMD for command.  Types can be found by prompting for the OBJTYPE parameter on commands such as WRKOBJ.
     @return  The integrated file system name for the object.
     **/
    public static String toPath(String libraryName, String objectName, String objectType)
    {
        // Note the code in this method is similar to that in the construct method, and code changes here should most likely be reflected there as well.

        // Verify arguments will make a valid QSYS IFS path name.
        checkLibraryName(libraryName);
        checkObjectName(objectName);
        checkObjectType(objectType);

        libraryName = libraryName.charAt(0) == '\"' ? libraryName : libraryName.toUpperCase();
        objectName = objectName.charAt(0) == '\"' ? objectName : objectName.toUpperCase();
        objectType = objectType.toUpperCase();

        return buildPathName(libraryName, objectName, "", objectType);
    }

    /**
     Builds an integrated file system path name to represent the member.
     @param  libraryName  The library the object is in.  It must be 1-10 characters.
     @param  objectName  The name of the object.  It must be 1-10 characters.
     @param  memberName  The name of the member.  It must be 1-10 characters.
     @param  objectType  The type of the object.  This must be "MBR".
     @return  The integrated file system name for the object.
     **/
    public static String toPath(String libraryName, String objectName, String memberName, String objectType)
    {
        // Note the code in this method is similar to that in the 4-parm ctor, and code changes here should most likely be reflected there as well.

        // Verify arguments will make a valid QSYS IFS path name.
        checkLibraryName(libraryName);
        checkObjectName(objectName);
        checkMemberName(memberName);
        checkObjectTypeIsMember(objectType);

        libraryName = libraryName.charAt(0) == '\"' ? libraryName : libraryName.toUpperCase();
        objectName = objectName.charAt(0) == '\"' ? objectName : objectName.toUpperCase();
        memberName = memberName.charAt(0) == '\"' ? memberName : memberName.toUpperCase();
        objectType = objectType.toUpperCase();

        return buildPathName(libraryName, objectName, memberName, objectType);
    }
}
