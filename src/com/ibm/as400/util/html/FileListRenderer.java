///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileListRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import java.io.File;
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
}
