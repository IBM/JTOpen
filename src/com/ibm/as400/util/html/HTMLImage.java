///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResetFormInput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyChangeSupport;


/**
*  The HTMLImage class represents an image tag within an HTML page.
*  <P>
*  This example creates a HTMLImage tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLImage.
*  HTMLImage image = new HTMLImage("http://myWebPage/pic.gif", "alternate text");
*  image.setHeight(50);
*  image.setWidth(50);
*  System.out.println(image);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLImage tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;img src=&quot;http://myWebPage/pic.gif&quot; alt=&quot;alternate text&quot; height=&quot;50&quot; width=&quot;50&quot; /&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>
*  The equivalent tag using XSL Formatting Objects looks like the following:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:block&gt;
*  &lt;fo:external-graphic src="file:http://myWebPage/pic.gif" content-height="50px" content-width="50px"/&gt;
*  &lt;/fo:block&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLImage objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLImage extends HTMLTagAttributes implements HTMLConstants, java.io.Serializable 
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
    static final long serialVersionUID = -7792796091910634206L;

    // Private data.
    private String name_;
    private String source_;
    private String align_;
    private String alt_;
    private int border_ = -1;
    private int height_ = 0;
    private int hspace_ = 0;
    private int width_ = 0;
    private int vspace_ = 0;
    private boolean useFO_ = false;  //Indicates if XSL-FO tags are outputted.                  //@A1A


    /**
     *  Constructs a default HTMLImage object.
     **/
    public HTMLImage()
    {
        super();
    }


    /**
     *  Constructs an HTMLImage object with the specified <i>source</i> and alternate text.
     *
     *  @param source The absolute or relative URL.
     *  @param alt The alternate text.
     **/
    public HTMLImage( String source, String alt )
    {
        setSrc(source);
        setAlt(alt);
    }

    /**
     *  Returns the alignment of the image.
     *
     *  @return The alignment.
     **/
    public String getAlign()
    {
        return align_;
    }

    /**
     *  Returns the alternate text.
     *
     *  @return The alternate text.
     **/
    public String getAlt()
    {
        return alt_;
    }
    

    /**
     *  Returns the thickness of the  border around the image.
     *
     *  @return The border.
     **/
    public int getBorder()
    {
        return border_;
    }


    /**
     *  Returns the height of the image.
     *
     *  @return The height.
     **/
    public int getHeight()
    {
        return height_;
    }


    /**
     *  Returns the horizontal space around the image in pixels.
     *
     *  @return The horizontal space.
     **/
    public int getHSpace()
    {
        return hspace_;
    }


    /**
     * Returns the unique name of the image.
     *
     * @return The name.
     **/
    public String getName()
    {
        return name_;
    }

    
    /**
     *  Returns the absolute or relative URL to reference the image.
     *  
     *  @return The source.
     **/
    public String getSrc()
    {
        return source_;
    }


    /**
     *  Returns the vertical space around the Image in pixels.
     *
     *  @return The vertical space.
     **/
    public int getVSpace()
    {
        return vspace_;
    }


    /**
     *  Sets the <i>width</i> of the image in pixels.
     *
     *  @return The width.
     **/
    public int getWidth()
    {
        return width_;
    }

    /**
     * Returns the element tag.
     *
     * @return The tag.
     **/
    public String getTag()
    {
        if(useFO_)                      //@A1A
            return getFOTag();          //@A1A

        if (source_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting image source.");
            throw new ExtendedIllegalStateException("source", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if (alt_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting image alt.");
            throw new ExtendedIllegalStateException("alt", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer tag = new StringBuffer( "<img" );

        tag.append(" src=\"");
        tag.append( source_ );
        tag.append("\"");

        tag.append(" alt=\"");
        tag.append( alt_ );
        tag.append("\"");

        if (align_ != null)
        {
            if (align_.equals(HTMLConstants.LEFT))
                tag.append(" align=\"left\"");
            else if (align_.equals(HTMLConstants.RIGHT))
                tag.append(" align=\"right\"");
            else if (align_.equals(HTMLConstants.TOP))
                tag.append(" align=\"top\"");
            else if (align_.equals(HTMLConstants.TEXTTOP))
                tag.append(" align=\"texttop\"");
            else if (align_.equals(HTMLConstants.MIDDLE))
                tag.append(" align=\"middle\"");
            else if (align_.equals(HTMLConstants.ABSMIDDLE))
                tag.append(" align=\"absmiddle\"");
            else if (align_.equals(HTMLConstants.BASELINE))
                tag.append(" align=\"baseline\"");
            else if (align_.equals(HTMLConstants.BOTTOM))
                tag.append(" align=\"bottom\"");
            else if (align_.equals(HTMLConstants.ABSBOTTOM))
                tag.append(" align=\"absbottom\"");
        }

        if (name_ != null)
        {
            tag.append(" name=\"");
            tag.append( name_ );
            tag.append("\"");
        }

        if (border_ > -1)
        {
            tag.append(" border=\"");
            tag.append( border_ );
            tag.append("\"");

        }

        if (height_ > 0)
        {
            tag.append(" height=\"");
            tag.append( height_ );
            tag.append("\"");
        }

        if (width_ > 0)
        {
            tag.append(" width=\"");
            tag.append( width_ );
            tag.append("\"");
        }

        if (hspace_ > 0)
        {
            tag.append(" hspace=\"");
            tag.append( hspace_ );
            tag.append("\"");
        }

        if (vspace_ > 0)
        {
            tag.append(" vspace=\"");
            tag.append( vspace_ );
            tag.append("\"");
        }

        tag.append( getAttributeString() );

        tag.append( " />" );

        return tag.toString();
    }


    /**
     * Returns the element tag for the XSL-FO image.
     * The alternate text and name attributes are not supported by XSL-FO.
     * @return The tag.
     **/
    public String getFOTag()                //@A1A
    {
        //Save current state of useFO_
        boolean useFO = useFO_;

        //Indicate Formatting Object tags are outputted.
        setUseFO(true);

        if (source_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting image source.");
            throw new ExtendedIllegalStateException("source", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer tag = new StringBuffer("");                

        tag.append("<fo:block" );
        if (align_ != null)
        {
            if(align_.equals(HTMLConstants.LEFT))
                tag.append(" text-align='start'");
            else if(align_.equals(HTMLConstants.RIGHT))
                tag.append(" text-align='end'");
            else if(align_.equals(HTMLConstants.CENTER))
                tag.append(" text-align='center'");
        }
        tag.append(">\n");
        tag.append("<fo:external-graphic" );
        tag.append(" src=\"file:");
        tag.append( source_ );
        tag.append("\"");

        if (border_ > -1)
        {
            tag.append(" border-width=\"");
            tag.append( border_ );
            tag.append("mm\"");
            tag.append(" border-style='solid'");
        }

        if (height_ > 0)
        {
            tag.append(" content-height=\"");
            tag.append( height_ );
            tag.append("px\"");
        }

        if (width_ > 0)
        {
            tag.append(" content-width=\"");
            tag.append( width_ );
            tag.append("px\"");
        }

        if (hspace_ > 0)
        {
            tag.append(" width=\"");
            tag.append( hspace_ );
            tag.append("px\"");
        }

        if (vspace_ > 0)
        {
            tag.append(" height=\"");
            tag.append( vspace_ );
            tag.append("px\"");
        }

        tag.append("/>\n");
        tag.append("</fo:block>\n");
        
        //Set useFO_ to previous state.
        setUseFO(useFO);

        return tag.toString();
    }

    /**
     *  Returns if Formatting Object tags are outputted.
     *  The default value is false.
     *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
     **/
    public boolean isUseFO()                                                //@A1A
    {
        return useFO_;
    }

    /**
     *  Deserializes and initializes transient data.
     **/
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        //@CRS changes_ = new PropertyChangeSupport(this);
    }


    /**
      *  Sets the alignment of text following the image tag relative to the graphic on screen.
      *
      *  @param align The alignment.  One of the following constants
      *  defined in HTMLConstants:  LEFT, RIGHT, TOP, TEXTTOP, MIDDLE, ABSMIDDLE, BASELINE, BOTTOM, or  ABSBOTTOM .
      *
      *  @see com.ibm.as400.util.html.HTMLConstants
      **/             
    public void setAlign(String align)
    {
        if (align == null)
            throw new NullPointerException("align");

        // If align is not one of the valid HTMLConstants, throw an exception.
        if ( !(align.equals(HTMLConstants.LEFT))  && !(align.equals(HTMLConstants.RIGHT)) && !(align.equals(HTMLConstants.TOP)) && 
             !(align.equals(HTMLConstants.TEXTTOP)) && !(align.equals(HTMLConstants.MIDDLE)) && !(align.equals(HTMLConstants.ABSMIDDLE)) && 
             !(align.equals(HTMLConstants.BASELINE)) && !(align.equals(HTMLConstants.BOTTOM)) && !(align.equals(HTMLConstants.BOTTOM)) && 
             !(align.equals(HTMLConstants.ABSBOTTOM)) )
        {
            throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = align_;

        align_ = align;

        if (changes_ != null) changes_.firePropertyChange("align", old, align ); //@CRS
    }


    /**
     *  Sets the alternate text to be displayed in place of the image.  This text will be displayed 
     *  for browsers that can not handle this ability or that have disabled this ability. 
     *
     *  @param alt The alternate text.
     **/
    public void setAlt( String alt )
    {
        if (alt == null)
            throw new NullPointerException("alt");

        String old = alt_;

        alt_ = alt;

        if (changes_ != null) changes_.firePropertyChange("alt", old, alt ); //@CRS
    }


    /**
     *  Sets the thickness of the <i>border</i> around the image.
     *
     *  @param border The border thickness.
     **/
    public void setBorder( int border )
    {
        if (border < 0)
            throw new ExtendedIllegalArgumentException("border", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = border_;

        border_ = border;

        if (changes_ != null) changes_.firePropertyChange("border", new Integer(old), new Integer(border) ); //@CRS
    }


    /**
     *  Sets the <i>height</i> of the image in pixels.
     *
     *  @param height The height.
     **/
    public void setHeight( int height )
    {
        if (height < 0 )
            throw new ExtendedIllegalArgumentException("height", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = height_;

        height_ = height;

        if (changes_ != null) changes_.firePropertyChange("height", new Integer(old), new Integer(height) ); //@CRS
    }


    /**
     *  Sets the horizontal space around the image in pixels.
     *
     *  @param hspace The horizontal space.
     **/
    public void setHSpace( int hspace )
    {
        if (hspace < 0)
            throw new ExtendedIllegalArgumentException("hspace", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = hspace_;

        hspace_ = hspace;

        if (changes_ != null) changes_.firePropertyChange("hspace", new Integer(old), new Integer(hspace) ); //@CRS
    }


    /**
     * Set the a unique <i>name</i> of the image.
     *
     * @param name The name.
     **/
    public void setName( String name )
    {
        if (name == null)
            throw new NullPointerException("name");

        String old = name_;

        name_ = name;

        if (changes_ != null) changes_.firePropertyChange("name", old, name ); //@CRS
    }   


    /**
     *  Sets the absolute or relative URL to reference the image.
     *
     *  @param source The source.
     **/
    public void setSrc( String source )
    {
        if (source == null)
            throw new NullPointerException("source");

        String old = source_;

        source_ = source;

        if (changes_ != null) changes_.firePropertyChange("source", old, source ); //@CRS
    }
    

    /**
     *  Sets the vertical space around the image in pixels.
     *
     *  @param vspace The vertical space.
     **/
    public void setVSpace( int vspace )
    {
        if (vspace < 0 )
            throw new ExtendedIllegalArgumentException("vspace", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = vspace_;

        vspace_ = vspace;

        if (changes_ != null) changes_.firePropertyChange("vspace", new Integer(old), new Integer(vspace) ); //@CRS
    }
    

    /**
     *  Sets the <i>width</i> of the image in pixels.
     *
     *  @param width The width.
     **/
    public void setWidth( int width )
    {
        if (width < 0 )
            throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = width_;

        width_ = width;

        if (changes_ != null) changes_.firePropertyChange("width", new Integer(old), new Integer(width) ); //@CRS
    }

    /** 
    * Sets if Formatting Object tags should be used.
    *  The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML. 
    **/    
    public void setUseFO(boolean useFO)                                //@A1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
     *  Returns a String representation for the HTMLImage tag.
     *
     *  @return The tag. 
     **/
    public String toString()
    {
        return getTag();
    }
}
