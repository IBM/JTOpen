///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileListRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.IFSJavaFile;                    // $B4A
import com.ibm.as400.util.servlet.ListMetaData;             // $B4A
import com.ibm.as400.util.servlet.ListRowData;              // $B4A
import com.ibm.as400.util.servlet.RowMetaDataType;     // $B4A
import com.ibm.as400.util.servlet.RowDataException;     // $B4A

import java.io.File;
import java.text.Collator;                                 // $B4A
import java.util.Date;                                      // $B4A
import java.util.Vector;                                   // $B4A
import java.text.SimpleDateFormat;                  // $B4A
import java.beans.PropertyVetoException;        // $B4A
import javax.servlet.http.HttpServletRequest;


/**
*  The FileListRenderer class renders the name field for directories and files
*  in a FileListElement.
*
*  If the behavior of the default FileListRenderer is not desired, subclass
*  FileListRenderer and override the appropriate methods until the
*  FileListElement achieves the desired behavior.
*
*  Subclassing FileListRenderer will allow your servlet to include/exclude
*  or change the action of any directory or file in the FileListElement.
*  For example, if a servlet did not want users to see any *.exe files,
*  A subclass of FileListRenderer would be created and the new class
*  would override the getFileName() method to figure out if the File object
*  passed to it was a *.exe file, if it is, null could be returned, which
*  would indicate that the file should not be displayed.
*
*  <P>Overriding the getRowData method will allow the addition
*  of columns in the row data and also enable the reordering of the
*  columns.
*
*  <P>
*  This example creates an FileListElement object with a renderer:
*  <P>
*  <PRE>
*   // Create a FileListElement.
*  FileListElement fileList = new FileListElement(sys, httpservletrequest);
*  <p>
*  // Set the renderer specific to this servlet, which extends
*  // FileListRenderer and overrides applicable methods.
*  fileList.setRenderer(new myFileListRenderer(request));
*  </PRE>
**/
public class FileListRenderer
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private HttpServletRequest request_;
    private String uri_;
    private String reqPath_;              
    private StringBuffer sharePath_;            // @B1A
    private StringBuffer shareName_;            // @B1A


    private SimpleDateFormat   formatter_ = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");  // @B4A

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;         // @A5A     //$B4A

    // The FileListElement default column headers.
    private static String name = loader_.getText("PROP_FLE_NAME_NAME");                    // @A5A    //$B4A
    private static String size = loader_.getText("PROP_FLE_NAME_SIZE");                    // @A5A        //$B4A
    private static String type = loader_.getText("PROP_FLE_NAME_TYPE");                    // @A5A       //$B4A
    private static String modified = loader_.getText("PROP_FLE_NAME_MODIFIED");            // @A5A   //$B4A
    /**
     *  Constructs a FileListRenderer with the specified <i>request</i>.
     *
     *  @param request The Http servlet request.
     **/
    public FileListRenderer(HttpServletRequest request)
    {
        if (request == null)
            throw new NullPointerException("request");


        /* @B2A
           According to the JSDK, HttpServletRequest.getServletPath() should
           return the path to the servlet as a root relative path so that it
           can be used to generate self-referencing URLs.  This is equivalent
           to the CGI environment variable SCRIPT_NAME.  However, some webservers
           only return the servlet name preceded by a slash (/) when your
           appliation server has a path other than slash (/) configured.
           (Note that the only application server that can have slash (/)
           for a path is the default application server.)
           
           request.getServletPath();
    
           should return: /servlet/name vs. /name
    
           What follows is a circumvention to accomplish the same thing.
    
           The following code strips the path information from the
           request URI.  
        */
        uri_ = request.getRequestURI();                                  // @B2A

        String servletPath = request.getServletPath();               // @B3A

        int i = uri_.indexOf(servletPath);                                  // @B3A

        uri_ = uri_.substring(0, i + servletPath.length());            // @B3C

        reqPath_ = request.getPathInfo();                             
    }


    /**
     *  Constructs a FileListRenderer with the specified <i>request</i>, NetServer <i>sharePath</i>,
     *  and the NetServer <i>shareName</i>.
     *
     *  @param request   The Http servlet request.
     *  @param shareName The NetServer share name.
     *  @param sharePath The NetServer share path.
     **/
    public FileListRenderer(HttpServletRequest request, String shareName, String sharePath)
    {
        this(request);

        if (sharePath == null)                                       // @B1A
            throw new NullPointerException("sharePath");             // @B1A

        if (shareName == null)                                       // @B1A
            throw new NullPointerException("shareName");             // @B1A

        sharePath_ = new StringBuffer(sharePath);                    // @B1A
        shareName_ = new StringBuffer(shareName);                    // @B1A

        if (Trace.isTraceOn())                                                  // @B1A
        {
            // @B1A
            Trace.log(Trace.INFORMATION, "Renderer sharePath: " + shareName_);  // @B1A
            Trace.log(Trace.INFORMATION, "Renderer shareName: " + sharePath_);  // @B1A
        }                                                                       // @B1A
    }


    /**
     *  Return the directory name string.  A link to the calling servlet with the
     *  directory included in the path info by default.  If the directory should 
     *  not be added to the FileListElement, a null string should be returned.
     *
     *  @param file The File.
     *
     *  @return The directory name string.
     **/
    public String getDirectoryName(File file)
    {
        if (file == null)
            throw new NullPointerException("file");

        String name = file.getName();

        StringBuffer buffer = new StringBuffer("<a href=\"");
        buffer.append(uri_);
        buffer.append(URLEncoder.encode(reqPath_.replace('\\','/'), false));        // @A1C
        buffer.append(reqPath_.endsWith("/") ? "" :"/");
        buffer.append(URLEncoder.encode(name, false));                           // @A1C
        buffer.append("\">");
        buffer.append(name);
        buffer.append("</a>");

        return buffer.toString();
    }


    /**
     *  Return the file name string.  The file name will be returned by default.  
     *  If the file should not be displayed in the FileListElement, a null string 
     *  should be returned.
     *
     *  @param file The File.
     *
     *  @return The file name string.
     **/
    public String getFileName(File file)
    {
        if (file == null)
            throw new NullPointerException("file");

        return file.getName();
    }

    /**
     *  Return the parent directory name string.  A link to the calling servlet with the 
     *  parent directory included in the path info will be returned by default.  If the 
     *  parent should not be display in the FileListElement, a null string should be returned.
     *
     *  @param file The File.
     *
     *  @return The parent name string.
     **/
    public String getParentName(File file)
    {
        if (file == null)
            throw new NullPointerException("file");
        String parent = file.getParent();

        if (parent != null)                                                                         // @A2A
        {
            if (sharePath_ != null)                                                                 // @B1A
            {
                // @B1A
                try                                                                                 // @B1A
                {
                    parent = shareName_.append(parent.substring(sharePath_.length(), parent.length())).toString();   // @B1A
                }
                // This exception will get thrown when the parent is the only directory in the      // @B1A
                // path.  Thus we know that we are at the beginning of the share.                   // @B1A
                catch (StringIndexOutOfBoundsException e)                                            // @B1A
                {
                    // @B1A
                    // If the parent directory length is shorter than the share path                // @B1A
                    // then don't display the parent of the share.                                  // @B1A
                    if (parent.length() < sharePath_.length())                                      // @B1A
                        return null;                                                                // @B1A
                }                                                                                   // @B1A
            }                                                                                       // @B1A
            // @B1A
            if (Trace.isTraceOn())                                                                  // @B1A
                Trace.log(Trace.INFORMATION, "Renderer parent: " + parent);                         // @B1A

            StringBuffer buffer = new StringBuffer("<a href=\"");
            buffer.append(uri_);
            buffer.append(parent.startsWith("\\") || parent.startsWith("/") ? "" : "/");            // @A2A
            buffer.append(parent!=null ? URLEncoder.encode(parent.replace('\\','/'), false) : "");  // @A1C
            buffer.append("\">../ (Parent Directory)</a>");

            return buffer.toString();
        }
        else                                                                                        // @A2A
            return null;                                                                            // @A2A
    }


    // $B4A
    /**
     *  Return the row data to be displayed in the FileListElement.
     *
     *  @param file The File.
     *  @param sort true if the elements are sorted; false otherwise.
     *              The default is true.
     *  @param collator The Collator.
     *
     *  @return ListRowData The row data.
     *
     *  @exception RowDataException If a row data error occurs.
     **/
    public ListRowData getRowData(File file, boolean sort, Collator collator) throws RowDataException
    {
        // This method used to be in the FileListElement class but was moved into the 
        //  the rederer class so that the user could have more control over the columns
        //  by being able to add, and reorder the columns (just to name a few).
        ListMetaData metaData = new ListMetaData(4);

        metaData.setColumnName(0, "Name");
        metaData.setColumnLabel(0, name);
        metaData.setColumnType(0, RowMetaDataType.STRING_DATA_TYPE);

        metaData.setColumnName(1, "Size");                                      // @A3C
        metaData.setColumnLabel(1, size);                                         // @A3C @A5C
        metaData.setColumnType(1, RowMetaDataType.INTEGER_DATA_TYPE);           // @A3C

        metaData.setColumnName(2, "Type");
        metaData.setColumnLabel(2, type);                                       // @A5C
        metaData.setColumnType(2, RowMetaDataType.STRING_DATA_TYPE);      

        metaData.setColumnName(3, "Modified");                                  // @A3C
        metaData.setColumnLabel(3, modified);                                     // @A3C @A5C
        metaData.setColumnType(3, RowMetaDataType.STRING_DATA_TYPE);            // @A3C

        ListRowData rowData = new ListRowData();

        try
        {
            rowData.setMetaData(metaData);      
        }
        catch (PropertyVetoException e)
        { /* Ignore */
        }

        // Get the string to display from the renderer.  This allows          // @A4A
        // the servlet more flexibility as to which files to display             // @A4A
        // and how to display them.                                                    // @A4A
        String parentName = getParentName(file);                                // @A4A  $B4C

        if (parentName != null)                                                            // @A4A
        {
            Object[] row = new Object[4];

            row[0] = parentName.replace('\\','/');                                   // @A4C
            row[1] = "";
            row[2] = "";
            row[3] = "";

            rowData.addRow(row);
        }

        File[] dirList = null;                                                    // @B3A
        File[] fileList = null;                                                   // @B3A

        if (file instanceof IFSJavaFile)                                   //$A1A
        {
            // @B3A
            // When we are using IFSJavaFile objects, we can use
            // the listFiles() method becuase it is not dependant on any
            // JDK1.2 code.  Using listFiles() will also cache information
            // like if it is a directory, so we don't flow another call to the 
            // server to find that out.  We can then build both the 
            // directory and file list at the same time.

            File[] filesAndDirs = ((IFSJavaFile) file).listFiles();        // @B3A

            // The vector of directories.
            Vector dv = new Vector();                                        // @B3A

            // The vector of files.
            Vector fv = new Vector();                                         // @B3A

            for (int i=0; i<filesAndDirs.length; i++)                        // @B3A
            {
                // Determine if the file is a directory or not and       // @B3A
                // add it to the appropriate directory.                     // @B3A
                if (filesAndDirs[i].isDirectory())                               // @B3A
                    dv.addElement(filesAndDirs[i]);                          // @B3A
                else                                                                    // @B3A
                    fv.addElement(filesAndDirs[i]);                           // @B3A
            }

            // Initialize the File arraya.                                        // @B3A
            dirList = new File[dv.size()];                                       // @B3A
            fileList = new File[fv.size()];                                       // @B3A

            // Copy the vectors into their appropriate array.           // @B3A
            dv.copyInto(dirList);                                                  // @B3A
            fv.copyInto(fileList);                                                  // @B3A
        }
        else   // If we are dealing with normal File objects and not IFSJavaFile objects.   //$A1A
        {
            // $A1D
            // We don't want to require webservers to use JDK1.2 because
            // most webserver JVM's are slower to upgrade to the latest JDK level.
            // The most efficient way to create these file objects is to use
            // the listFiles(filter) method in JDK1.2 which would be done
            // like the following, instead of using the list(filter) method
            // and then converting the returned string arrary into the appropriate
            // File array.
            // File[] dirList = file.listFiles(dirFilter);
            //
            // @B3A
            // We can however, use the listFiles() method on an IFSJavaFile
            // object because that is not dependant on any JDK1.2 code.
            // Using the listFiles() method on IFSJavaFile objects will
            // also cache information (ie - is it a directory) so we don't
            // have to flow another call to the server to find that information
            // out all the time.  

            // Get the list of files that satisfy the directory filter.
            // Build the File array of Directories.
            String[] dlist = file.list(new DirFilter());                        

            dirList = new File[dlist.length];

            for (int i=0; i<dlist.length; ++i)
            {
                dirList[i] = new File(file, dlist[i]);                             //$A1A
            }

            // Get the list of files that satisfy the file filter.
            // Build the File array of files.
            String[] flist = file.list(new HTMLFileFilter());                                   

            fileList = new File[flist.length];

            for (int i=0; i<flist.length; ++i)
            {
                fileList[i] = new File(file, flist[i]);                             //$A1A
            }
        }

        if (dirList != null)                                                           // @A6A  // @B3C
        {
            if (sort)                                                                    // @A2A
                HTMLTree.sort2(collator, dirList);                          // @A2A  @B3C

            //$A1A
            for (int i=0; i<dirList.length; i++)
            {
                // Get the string to display from the renderer.  This allows          // @A4A
                // the servlet more flexibility as to which files to display          // @A4A
                // and how to display them.                                           // @A4A
                String dirName = getDirectoryName(dirList[i]);                  // @A4A  $B4C

                if (dirName != null)                                                  // @A4A
                {
                    Object[] row = new Object[4];                  

                    Date d = new Date(dirList[i].lastModified());                      // @A4C

                    row[0] = dirName.replace('\\','/');                                // @A4C
                    row[1] = "";                                                       // @A3C
                    row[2] = "Directory";
                    row[3] = formatter_.format(d);                                     // @A3C

                    rowData.addRow(row);
                }
            }
        }

        if (fileList != null)                                                            // @A6A  // @B3C
        {
            if (sort)                                                                     // @A2A
                HTMLTree.sort2(collator, fileList);                           // @A2A    @B3C

            for (int i=0; i<fileList.length; i++)
            {
                // Get the string to display from the renderer.  This allows          // @A4A
                // the servlet more flexibility as to which files to display             // @A4A
                // and how to display them.                                                    // @A4A
                String fileName = getFileName(fileList[i]);                                 // @A4A  $B4C

                if (fileName != null)                                                                 // @A4A
                {
                    Object[] row = new Object[4];          

                    Date d = new Date(fileList[i].lastModified());                     

                    row[0] = fileName.replace('\\','/');                         // @A4C
                    row[1] = new Long(fileList[i].length());                             
                    row[2] = "File";                                                   // @A3C
                    row[3] = formatter_.format(d);                                       

                    rowData.addRow(row);          
                }
            }
        }

        return rowData;
    }
}
