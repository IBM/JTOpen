///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ImageFormInput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyVetoException;

/**
*  The ImageFormInput class represents a image input type in an HTML form.
*  The trailing slash &quot;/&quot; on the ImageFormInput tag allows it to conform to
*  the XHTML specification.
*  <P>
*  Here is an example of a ImageFormInput tag:<BR>
*  &lt;input type=&quot;image&quot; name=&quot;myPicture&quot; src=&quot;image.gif&quot; 
*  align=&quot;top&quot; height=&quot;100&quot; width=&quot;100&quot; /&gt;
*
*  <p>ImageFormInput objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class ImageFormInput extends FormInput implements HTMLConstants
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private int height_ = 0;                  // The image height.
    private int width_ = 0;                   // The image width.
    private String align_;                    // The image text alignment.
    private String source_;                   // The image source URL.


    
    /**
    *  Constructs a default ImageFormInput object.
    **/
    public ImageFormInput()
    {
       super();
       align_ = TOP;                    // default alignment

    }

    /**
    *  Constructs a ImageFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public ImageFormInput(String name)
    {
       super(name);                     // default alignment
       align_ = TOP;
    }

    /**
    *  Constructs a ImageFormInput object with the specified control <i>name</i> and 
    *  image <i>source</i>.
    *  @param name The control name of the input field.
    *  @param source The source URL of the image.
    **/
    public ImageFormInput(String name, String source)
    {
       super(name);
       align_ = TOP;              // default alignment
       try {
          setSource(source);
       }
       catch (PropertyVetoException e)
       {
       }
    }

    /**
    *  Returns the alignment of the text following the image.
    *  @return The alignment.  One of the following constants
    *  defined in HTMLConstants:  BASELINE, BOTTOM, ABSBOTTOM, LEFT, RIGHT,
    *  MIDDLE, ABSMIDDLE, TOP, or TEXTOP.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants  
    **/
    public String getAlignment()
    {
       return align_;
    }

    /**
    *  Returns the attribute tag.
    *  @return The tag.
    **/
    private String getAttributeTag()
    {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving attribute tag.");

       StringBuffer s = new StringBuffer("");

       if ((source_ != null) && (source_.length() > 0))
          s.append(" src=\"" + source_ + "\"");    

       s.append(" align=\"");
       s.append(align_);
       s.append("\"");

       if (height_ > 0)
       {
          if (Trace.isTraceOn())
             Trace.log(Trace.INFORMATION, "   Using height attribute tag.");

          s.append(" height=\"" + height_ + "\"");    
       }
       if (width_ > 0)
       {
          if (Trace.isTraceOn())
             Trace.log(Trace.INFORMATION, "   Using width attribute tag.");

          s.append(" width=\"" + width_ + "\"");    
       }

       return s.toString();
    }

    /**
    *  Returns the height of the image in pixels.
    *  @return The height.
    **/
    public int getHeight()
    {
       return height_;
    }

    /**
    *  Returns the source URL for the image.
    *  @return The URL.
    **/
    public String getSource()
    {
       return source_;
    }

    /**
    *  Returns the tag for the image form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating ImageFormInput tag...");

        if (getName() == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
           throw new ExtendedIllegalStateException(
               "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (getSource() == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting source.");
           throw new ExtendedIllegalStateException(
               "source", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<input type=\"image\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(true));
        s.append(getAttributeTag());
        s.append(getLanguageAttributeTag());                                        //$B1A
        s.append(getDirectionAttributeTag());                                       //$B1A
        s.append(getAttributeString());                                             // @Z1A
        s.append(" />");

        return s.toString();
    }

    /**
    *  Returns the width of the image in pixels.
    *  @return The width.
    **/
    public int getWidth()
    {
       return width_;
    }

  
    /**
    *  Sets the alignment of the text following the image.  The default
    *  alignment is <i>top</i>.
    *  @param align The alignment.  One of the following constants
    *  defined in HTMLConstants:  BASELINE, BOTTOM, ABSBOTTOM, LEFT, RIGHT,
    *  MIDDLE, ABSMIDDLE, TOP, or TEXTOP.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setAlignment(String align)
      throws PropertyVetoException
    {  
       if (align == null)
          throw new NullPointerException("align");
       
       // If align is not one of the valid HTMLConstants, throw an exception.
       if ( !(align.equals(BASELINE))  && !(align.equals(BOTTOM)) && !(align.equals(ABSBOTTOM)) && 
            !(align.equals(LEFT))      && !(align.equals(RIGHT))  && !(align.equals(MIDDLE))    && 
            !(align.equals(ABSMIDDLE)) && !(align.equals(TOP))    && !(align.equals(TEXTTOP)) ) 
       {
          throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       
       String old = align_;
       vetos_.fireVetoableChange("align", old, align);

       align_ = align;

       changes_.firePropertyChange("align", old, align);
    }

    /**
    *  Sets the height of the image in pixels.
    *  @param height The height.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setHeight(int height)
      throws PropertyVetoException
    {
       if (height < 0 )
           throw new ExtendedIllegalArgumentException("height", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

       int old = height_;
       vetos_.fireVetoableChange("height", new Integer(old), new Integer(height) );

       height_ = height;

       changes_.firePropertyChange("height", new Integer(old), new Integer(height) );
    }

    /**
    *  Sets the source URL for the image.
    *  @param source The URL.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setSource(String source)
      throws PropertyVetoException
    {
       if (source == null)
          throw new NullPointerException("source");

       if (source.length() == 0)
          throw new ExtendedIllegalArgumentException("source", 
                            ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

       String old = source_;
       vetos_.fireVetoableChange("source", old, source );

       source_ = source;

       changes_.firePropertyChange("source", old, source );
    }
    
    /**
    *  Sets the width of the image in pixels.
    *  @param width The width.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setWidth(int width)
      throws PropertyVetoException
    {
       if (width < 0)
          throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

       int old = width_;
       vetos_.fireVetoableChange("width", new Integer(old), new Integer(width) );

       width_ = width;

       changes_.firePropertyChange("width", new Integer(old), new Integer(width) );
    }

}
