///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: HTMLText.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
/**
*  The HTMLText class encapsulates HTML text attributes.
*
*  <P>This example creates an HTMLText object and sets its attributes.
*  
*  <P><BLOCKQUOTE><PRE>
*  HTMLText text = new HTMLText("IBM");
*  text.setBold(true);
*  text.setSize(3);
*  System.out.println(text.getTag());
*  </PRE></BLOCKQUOTE>
*
*  Here is the output of the tag:
*  <P><BLOCKQUOTE><PRE>
*  &lt;font size="3"&gt;&lt;b&gt;IBM&lt;/b&gt;&lt;/font&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLText objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class HTMLText implements HTMLTagElement, HTMLConstants, Serializable   
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String text_;                    // The text to tag.   
   private String alignment_;               // The horizontal alignment.  "left", "center", "right" or "justify".
   private Color color_;	            // The font color attribute.
   private int size_ = 0;                   // The font size attribute.  
   private boolean bold_ = false;           // The bold style attribute.
   private boolean fixed_ = false;          // The fixed pitch style attribute.
   private boolean italic_ = false;         // The italic style attribute.
   private boolean underscore_ = false;     // The underline style attribute.

   transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

   /**
   *  Constructs a default HTMLText object.
   **/
   public HTMLText()
   {
       super();
   }

   /**
   *  Constructs an HTMLText object with the specified <i>text</i>.
   *  @param text The text.
   **/
   public HTMLText(String text)
   {
      this();

      try
      {
         setText(text);   
      }
      catch (PropertyVetoException e)
      { /* do nothing */ } 
   }

   /**
   *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> 
   *  method is called each time the value of any bound property is changed.
   *  @see #removePropertyChangeListener
   *  @param listener The PropertyChangeListener.
   **/
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      changes_.addPropertyChangeListener(listener);
   }

   /**
   *  Adds the VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b> 
   *  method is called each time the value of any constrained property is changed.
   *  @see #removeVetoableChangeListener
   *  @param listener The VetoableChangeListener.
   **/
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      vetos_.addVetoableChangeListener(listener);
   }

   /**
   *  Returns the horizontal alignment.
   *  @return The horizontal alignment.  One of the following constants
   *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
   *  @see com.ibm.as400.util.html.HTMLConstants
   **/
   public String getAlignment()
   {
       return alignment_;
   }

   /**
   *  Returns the color used to paint the text.
   *  @return The Color object representing the text color.
   **/
   public Color getColor()				// @A1
   {
      return color_;
   }

   /**
   *  Returns the end text alignment tag.
   *  @return The end text alignment tag or an empty String
   *  if the alignment is not set.
   **/
   String getEndTextAlignmentTag()
   {
      if (alignment_ != null)
         return ("</div>");
      else
         return "";
   }

   /**
   *  Returns the end font tag.
   *  @Return The end font size tag or an empty String
   *  if the size is not set.
   **/
   String getEndTextFontTag()
   {
       if (size_ != 0 || color_ != null)
       {
           return ("</font>");
       }
       else
       {
           return "";
       }
   }

   /**
   *  Returns the end tags for the text styles.
   *  The tags are bold, italic, underline, and fixed.
   *  @return The end text style tag string or an empty String
   *  if the style attributes are not set.
   **/
   String getEndTextStyleTag()
   {
       StringBuffer tag = new StringBuffer();

       if (fixed_)
       {
           tag.append("</tt>");
       }
       if (underscore_)
       {
           tag.append("</u>");
       }
       if (italic_)
       {
           tag.append("</i>");
       }
       if (bold_)
       {
           tag.append("</b>");
       }

       return new String(tag);
   }

   /**
   *  Returns the font color attribute.
   *  @return The font color attribute or an empty String if the color is not set.
   **/
   String getFontColorAttribute()		// @A1
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   Retrieving color attribute tag...");

      StringBuffer colorBuffer = new StringBuffer("");
      if (color_ != null)
      {
         colorBuffer.append(" color=\"#");
         String rgb = Integer.toHexString(color_.getRGB());
	 colorBuffer.append(new String(rgb.substring(2)));	// don't want 0xff at beginning of RGB string.
	 colorBuffer.append("\"");		
      }
      return new String(colorBuffer);
   }

   /**
   *  Returns the font size attribute.
   *  @Return The font size attribute or an empty String
   *  if the size is not set.
   **/
   String getFontSizeAttribute()
   {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving size attribute tag...");

       StringBuffer tag = new StringBuffer("");
       if (size_ != 0)
       {    
           tag.append(" size=\"");
           tag.append(size_);
           tag.append("\"");           
       }
       return new String(tag);
   }

   /**
     * Returns the font text size.
     * The default value is 0 (browser default).
     * @return The font size.
     **/
   public int getSize()
   {
       return size_;
   }

   /**
   *  Returns the text tag.  The alignment tag is not included.
   *  @return The tag.
   **/
   public String getTag()      
   {
      return getTag(text_, false);
   }

   /**
   *  Returns the text tag.
   *  @param useAlignment true if the alignment tag should be included; false otherwise.
   *  @return The tag.
   **/
   public String getTag(boolean useAlignment)
   {
      return getTag(text_, useAlignment);
   }

   /**
   *  Returns the text tag with the specified <i>text</i>.
   *  The alignment tag is not included.
   *  @param text The text.
   *  @return The tag.
   **/
   public String getTag(String text)
   {
      return getTag(text, false);
   }

   /**
   *  Returns the text tag with the specified <i>text</i>.
   *  @param text The text.
   *  @param useAlignment true if the alignment tag should be included; false otherwise.
   *  @return The tag.
   **/
   public String getTag(String text, boolean useAlignment)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLText tag...");

      if (text == null)
         throw new NullPointerException("text");
 
      StringBuffer tag = new StringBuffer();
      
      if (useAlignment) 
         tag.append(getTextAlignmentTag());
      tag.append(getTextFontTag());
      tag.append(getTextStyleTag());
      tag.append(text);
      tag.append(getEndTextStyleTag());
      tag.append(getEndTextFontTag());
      if (useAlignment) 
         tag.append(getEndTextAlignmentTag());
      
      return new String(tag);

   }

   /**
   *  Returns the text.
   *  @return The text.
   **/
   public String getText()      
   {
      return text_;
   }

   /**
   *  Returns the text alignment tag.
   *  @return The text alignment tag or an empty String
   *  if the alignment is not set.
   **/
   String getTextAlignmentTag()
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   Retrieving alignment attribute tag...");

      if (alignment_ != null)
      {
         StringBuffer tag = new StringBuffer();
         tag.append("<div align=\"");
         tag.append(alignment_);
         tag.append("\">");
         return new String(tag);
      }
      else
         return "";
   }

   /**
   *  Returns the font attribute tag.
   *  @return The font attribute tag or an empty String if the font attributes are not set.
   **/
   String getTextFontTag()			// @A1
   {
      StringBuffer tag = new StringBuffer("");
      if (size_ != 0 || color_ != null)
      {
         tag.append("<font");
         tag.append(getFontSizeAttribute());
         tag.append(getFontColorAttribute());
         tag.append(">");
      }
      return new String(tag);
   }

   /**
   *  Returns the text style tags.  The tags are
   *  bold, italic, underline, and fixed.
   *  @return The text style tag string or an empty String
   *  if the style attributes are not set.
   **/
   String getTextStyleTag()
   {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving style attribute tag...");

       StringBuffer tag = new StringBuffer();

       if (bold_)
       {
           tag.append("<b>");
       }
       if (italic_)
       {
           tag.append("<i>");
       }
       if (underscore_)
       {
           tag.append("<u>");
       }
       if (fixed_)
       {
           tag.append("<tt>");
       }

       return new String(tag);
   } 
   /**
   *  Indicates if bold is on.
   *  @return true if bold, false otherwise.
   **/
   public boolean isBold()
   {
       return bold_;
   }

   /**
   *  Indicates if fixed pitch font is on.
   *  @return true if fixed, false otherwise.
   **/
   public boolean isFixed()
   {
       return fixed_;
   }

   /**
   *  Indicates if italic is on.
   *  @return true if italic, false otherwise.
   **/
   public boolean isItalic()
   {
       return italic_;
   }

   /**
   *  Indicates if underline is on.
   *  @return true if underline, false otherwise.
   **/
   public boolean isUnderscore()
   {
       return underscore_;
   }

   /**
   *  Deserializes and initializes transient data.
   **/
   private void readObject(java.io.ObjectInputStream in)         
       throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
   }

   /**
   *  Removes the PropertyChangeListener from the internal list.
   *  If the PropertyChangeListener is not on the list, nothing is done.
   *  @see #addPropertyChangeListener
   *  @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      changes_.removePropertyChangeListener(listener);
   }

   /**
   *  Removes the VetoableChangeListener from the internal list.
   *  If the VetoableChangeListener is not on the list, nothing is done.
   *  @see #addVetoableChangeListener
   *  @param listener The VetoableChangeListener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      vetos_.removeVetoableChangeListener(listener);
   }

   /**
   *  Sets the horizontal alignment.
   *  @param alignment The horizontal alignment.  One of the following constants
   *  defined in HTMLConstants:  LEFT, CENTER, RIGHT, or JUSTIFY.
   *  @exception PropertyVetoException If the change is vetoed.
   *  @see com.ibm.as400.util.html.HTMLConstants
   **/
   public void setAlignment(String alignment) throws PropertyVetoException
   {
      if (alignment == null)
      {
         throw new NullPointerException("alignment");
      }
      else if (alignment.equalsIgnoreCase(LEFT) || 
               alignment.equalsIgnoreCase(CENTER) || 
               alignment.equalsIgnoreCase(RIGHT) ||
               alignment.equalsIgnoreCase(JUSTIFY))
      {
         String old = alignment_;
         vetos_.fireVetoableChange("alignment", old, alignment );
         
         alignment_ = alignment;
          
         changes_.firePropertyChange("alignment", old, alignment );

      }
      else
      {
         throw new ExtendedIllegalArgumentException("alignment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
   }

   /**
   *  Sets bold on or off.  The default is false.
   *  @param bold true if on, false if off.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setBold(boolean bold) throws PropertyVetoException
   {
      Boolean oldBold = new Boolean(bold_);
      Boolean newBold = new Boolean(bold);

      vetos_.fireVetoableChange("bold", oldBold, newBold);
      
      bold_ = bold;
      
      changes_.firePropertyChange("bold", oldBold, newBold);
   }

   /**
   *  Sets the color used to paint the text.
   *  The default text color is determined by the browser's color settings).
   *  How the color is rendered is browser dependent.
   *  @param color The Color object.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setColor(Color color) throws PropertyVetoException		// @A1
   {      
      Color oldColor = color_;

      vetos_.fireVetoableChange("color", oldColor, color);

      color_ = color;

      changes_.firePropertyChange("color", oldColor, color);
   }

   /**
   *  Sets fixed pitch font on or off.  The default is false.
   *  @param fixed true if on, false if off.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setFixed(boolean fixed) throws PropertyVetoException
   {
      Boolean oldFixed = new Boolean(fixed_);
      Boolean newFixed = new Boolean(fixed);

      vetos_.fireVetoableChange("fixed", oldFixed, newFixed);
      
      fixed_ = fixed;
      
      changes_.firePropertyChange("fixed", oldFixed, newFixed);
   }

   /**
   *  Sets italic on or off.  The default is false.
   *  @param italic true if on, false if off.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setItalic(boolean italic) throws PropertyVetoException
   {
      Boolean oldItalic = new Boolean(italic_);
      Boolean newItalic = new Boolean(italic);

      vetos_.fireVetoableChange("italic", oldItalic, newItalic);
      
      italic_ = italic;
      
      changes_.firePropertyChange("italic", oldItalic, newItalic);
   }

   /**
   *  Sets the text font size.  Valid values are: 0 to 7.
   *  The default value is 0 (use browser default).
   *  @param size The font size.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setSize(int size) throws PropertyVetoException
   {
      if (size < 0 || size > 7)      
         throw new ExtendedIllegalArgumentException("size", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

      Integer oldSize = new Integer(size_);
      Integer newSize = new Integer(size);

      vetos_.fireVetoableChange("size", oldSize, newSize );
      
      size_ = size;       

      changes_.firePropertyChange("size", oldSize, newSize );
   }

   /**
   *  Sets the text.
   *  @param text The text.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setText(String text) throws PropertyVetoException
   {
      if (text == null)
         throw new NullPointerException("text");

      String old = text_;
      vetos_.fireVetoableChange("text", old, text );
      
      text_ = text;

      changes_.firePropertyChange("text", old, text );
   }

   /**
   *  Sets underline on or off.  The default is false.
   *  @param underscore true if on, false if off.
   *  @exception PropertyVetoException If the change is vetoed.
   **/
   public void setUnderscore(boolean underscore) throws PropertyVetoException
   {
      Boolean oldUnderscore = new Boolean(underscore_);
      Boolean newUnderscore = new Boolean(underscore);
      
      vetos_.fireVetoableChange("underscore", oldUnderscore, newUnderscore);
      
      underscore_ = underscore;

      changes_.firePropertyChange("underscore", oldUnderscore, newUnderscore);
   }

   /**
   *  Returns the HTML text tag.
   *  @return The tag.
   **/
   public String toString()
   {
      return getTag();
   }
}
