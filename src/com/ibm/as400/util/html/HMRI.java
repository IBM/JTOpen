///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.ListResourceBundle;

/**
Locale-specific objects for the AS/400 Toolbox for Java.
**/
public class HMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {

           // #TRANNOTE Before you add a new resource, please make
           // #TRANNOTE sure you are not duplicating another.  The
           // #TRANNOTE goal is to keep the amount of translatable
           // #TRANNOTE text down.
           // #TRANNOTE
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE



           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for events.
           // #TRANNOTE Descriptions start with EVT_DESC_ prefix, display
           // #TRANNOTE names start with EVT_NAME.
           // #TRANNOTE #####################################################

      { "EVT_DESC_SC_EVENT", "A section completed event has occurred." },

           // #TRANNOTE A section completed event has occurred.
      { "EVT_NAME_SC_EVENT", "sectionCompletedEvent" },
      { "EVT_DESC_RD_EVENT", "A row data event has occurred." },

           // #TRANNOTE A row data event has occurred.
      { "EVT_NAME_RD_EVENT", "rowDataEvent" },

           // #TRANNOTE A bound property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_CHANGE", "propertyChange" },
      { "EVT_DESC_PROPERTY_CHANGE", "A bound property has changed." },

           // #TRANNOTE A constrained property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_VETO", "vetoableChange" },
      { "EVT_DESC_PROPERTY_VETO", "A constrained property has changed." },


           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for properties.
           // #TRANNOTE Descriptions start with PROP_<component>_DESC_ or
           // #TRANNOTE PROP_DESC_ prefix, display names start with PROP_NAME_
           // #TRANNOTE or with PROP_<component>_NAME_.
           // #TRANNOTE #####################################################

      { "PROP_NAME_NAME", "name" },
      { "PROP_FI_DESC_NAME", "The name of the Form Input." },
      { "PROP_RG_DESC_NAME", "The name of the Radio Group." },
      { "PROP_SF_DESC_NAME", "The name of the Select Form." },
      { "PROP_TA_DESC_NAME", "The name of the Text Area." },
      { "PROP_HHLNK_DESC_NAME", "The bookmark name for the resource link." },

      { "PROP_NAME_SIZE", "size" },
      { "PROP_FI_DESC_SIZE", "The width of the input field." },
      { "PROP_SF_DESC_SIZE", "The number of visible options." },
      { "PROP_HTXT_DESC_SIZE", "The size of the text." },
      { "PROP_DESC_PANELSIZE", "The number of elements in the layout." },

      { "PROP_NAME_VALUE", "value" },
      { "PROP_FI_DESC_VALUE", "The initial value of the input field." },
      { "PROP_SO_DESC_VALUE", "The value used when the form is submitted." },

      { "PROP_NAME_COLUMNS", "columns" },
      { "PROP_GL_DESC_COLUMNS", "The number of columns in the layout." },
      { "PROP_TA_DESC_COLUMNS", "The number of visible columns in the text area." },

           // #TRANNOTE Denotes a Web URL (Uniform Resource Locator) or Web Page
      { "PROP_NAME_URL", "URL" },
      { "PROP_DESC_URL", "The ACTION URL address of the form handler on the server." },
      { "PROP_NAME_METHOD", "method" },
      { "PROP_DESC_METHOD", "The HTTP method used for sending form contents to the server." },

      { "PROP_NAME_TARGET", "target" },
      { "PROP_HF_DESC_TARGET", "The target frame for the form response." },
      { "PROP_HHLNK_DESC_TARGET", "The target frame for the resource link." },

      { "PROP_NAME_HIDDENPARAMETERLIST", "hiddenParameterList" },
      { "PROP_DESC_HIDDENPARAMETERLIST", "The hidden parameter list for the form." },

      { "PROP_NAME_LINK", "link" },
      { "PROP_DESC_LINK", "The Uniform Resource Identifier (URI) for the resource link." },
      { "PROP_NAME_PROPERTIES", "properties" },
      { "PROP_DESC_PROPERTIES", "The properties for the resource link." },

      { "PROP_NAME_TEXT", "text" },
      { "PROP_HT_DESC_TEXT", "The text displayed in the HTML document." },
      { "PROP_SO_DESC_TEXT", "The option text." },
      { "PROP_TA_DESC_TEXT", "The initial text of the text area." },
      { "PROP_HHLNK_DESC_TEXT", "The text representation for the resource link." },
      { "PROP_HTXT_DESC_TEXT", "The text value of the HTML text." },

      { "PROP_NAME_TITLE", "title" },
      { "PROP_DESC_TITLE", "The title for the resource link." },

      { "PROP_NAME_ALIGNMENT", "alignment" },
      { "PROP_DESC_ALIGNMENT", "The alignment of the text following the image." },
      { "PROP_HTBL_DESC_ALIGNMENT", "The horizontal alignment of the table." },
      { "PROP_HTCAP_DESC_ALIGNMENT", "The alignment of the table caption." },
      { "PROP_HTXT_DESC_ALIGNMENT", "The horizontal alignment of the text." },

      { "PROP_NAME_HEIGHT", "height" },
      { "PROP_DESC_HEIGHT", "The height of the image." },
      { "PROP_HTCELL_DESC_HEIGHT", "The height of the table cell." },

      { "PROP_NAME_SOURCE", "source" },
      { "PROP_DESC_SOURCE", "The source URL for the image." },

      { "PROP_NAME_WIDTH", "width" },
      { "PROP_DESC_WIDTH", "The width of the image." },
      { "PROP_HTCELL_DESC_WIDTH", "The width of the table cell." },
      { "PROP_HTBL_DESC_WIDTH", "The width of the table." },

      { "PROP_NAME_LABEL", "label" },
      { "PROP_LF_DESC_LABEL", "The text label." },
      { "PROP_TF_DESC_LABEL", "The viewable text label for the toggle." },

      { "PROP_NAME_ACTION", "action" },                                             //$A3A
      { "PROP_DESC_ACTION", "The script to execute when the button is pressed." },  //$A3A

      { "PROP_NAME_MULTIPLE", "multiple" },
      { "PROP_DESC_MULTIPLE", "Specifies whether multiple selections can be made." },

      { "PROP_NAME_SELECTED", "selected" },
      { "PROP_DESC_SELECTED", "Specifies whether the option defaults as being selected." },

           // #TRANNOTE Specifies the maximum length of the text field.
      { "PROP_NAME_MAXLENGTH", "maxLength" },
      { "PROP_DESC_MAXLENGTH", "The maximum number of characters permitted in the text field." },

      { "PROP_NAME_ROWS", "rows" },
      { "PROP_DESC_ROWS", "The number of visible rows in the text area." },

      { "PROP_NAME_CHECKED", "checked" },
      { "PROP_DESC_CHECKED", "Specifies whether the toggle initializes to being checked." },

      { "PROP_NAME_COUNT", "count" },
      { "PROP_DESC_COUNT", "The number of elements in the option layout." },
      { "PROP_HTROW_DESC_COUNT", "The number of columns in the table row." },

      { "PROP_NAME_BOLD", "bold" },
      { "PROP_DESC_BOLD", "The bold style attribute of the text." },
      { "PROP_NAME_COLOR", "color" },                                 // @A4
      { "PROP_DESC_COLOR", "The color attribute of the text." },
      { "PROP_NAME_FIXED", "fixed" },
      { "PROP_DESC_FIXED", "The fixed font style attribute of the text." },
      { "PROP_NAME_ITALIC", "italic" },
      { "PROP_DESC_ITALIC", "The italic style attribute of the text." },
      { "PROP_NAME_UNDERSCORE", "underscore" },
      { "PROP_DESC_UNDERSCORE", "The underscore style attribute of the text." },

      { "PROP_NAME_CSPAN", "columnSpan" },
      { "PROP_DESC_CSPAN", "The column span of the table cell." },
      { "PROP_NAME_RSPAN", "rowSpan" },
      { "PROP_DESC_RSPAN", "The row span of the table cell." },

      { "PROP_NAME_WRAP", "wrap" },
      { "PROP_DESC_WRAP", "The HTML linebreaking convention of the table cell." },

      { "PROP_NAME_HALIGN", "horizontalAlignment" },
      { "PROP_HTCELL_DESC_HALIGN", "The horizontal alignment of the table cell." },
      { "PROP_HTROW_DESC_HALIGN", "The horizontal alignment of the table row." },

      { "PROP_NAME_VALIGN", "verticalAlignment" },
      { "PROP_HTCELL_DESC_VALIGN", "The vertical alignment of the table cell." },
      { "PROP_HTROW_DESC_VALIGN", "The vertical alignment of the table row." },
      { "PROP_RG_DESC_VALIGN", "The vertical alignment of the radio group." },         //$A2A

      { "PROP_NAME_HPERCENT", "heightInPercent" },
      { "PROP_DESC_HPERCENT", "The height unit in pixels or percent of the table cell." },
      { "PROP_NAME_WPERCENT", "widthInPercent" },
      { "PROP_HTCELL_DESC_WPERCENT", "The width unit of the table cell in pixels or percent." },
      { "PROP_HTBL_DESC_WPERCENT", "The width unit of the table in pixels or percent." },

      { "PROP_NAME_BORDERWIDTH", "borderWidth" },
      { "PROP_DESC_BORDERWIDTH", "The border width of the table." },
      { "PROP_NAME_CAPTION", "caption" },
      { "PROP_DESC_CAPTION", "The caption of the table." },
      { "PROP_NAME_DEFAULTROW", "defaultRow" },
      { "PROP_DESC_DEFAULTROW", "The default row of the table." },
      { "PROP_NAME_DEFAULTCELL", "defaultCell" },
      { "PROP_DESC_DEFAULTCELL", "The default cell of the table." },
      { "PROP_NAME_HEADER", "header" },
      { "PROP_DESC_HEADER", "The column headers of the table." },
      { "PROP_NAME_CELLPADDING", "cellPadding" },
      { "PROP_DESC_CELLPADDING", "The cell padding of the table." },
      { "PROP_NAME_CELLSPACING", "cellSpacing" },
      { "PROP_DESC_CELLSPACING", "The cell spacing of the table." },
      { "PROP_NAME_HEADERINUSE", "headerInUse" },
      { "PROP_DESC_HEADERINUSE", "Indicates whether the table header is in use." },

      { "PROP_NAME_ITEMDATA", "itemData" },                                                //$B1A
      { "PROP_HLI_DESC_ITEMDATA", "The data in the HTML list item." },                     //$B1A

      { "PROP_NAME_COMPACT", "compact" },                                                  //$B1A
      { "PROP_HL_DESC_COMPACT", "Indicates whether the list is compact." },                //$B1A

      { "PROP_NAME_ITEMS", "items" },                                                      //$B1A
      { "PROP_HL_DESC_ITEMS", "The items in the HTML list." },                             //$B1A

      { "PROP_NAME_TYPE", "type" },                                                        //$B1A
      { "PROP_OUL_DESC_TYPE", "The labeling scheme of the HTML list." },                   //$B1A
      { "PROP_OULI_DESC_TYPE", "The labeling scheme of the HTML list item." },             //$B1A

      { "PROP_NAME_START", "start" },                                                               //$B1A
      { "PROP_OL_DESC_START", "The starting number to use in incrementing the list structure." },   //$B1A

      { "PROP_OLI_DESC_VALUE", "A number, other than the incremented value, for the current list item." },  //$B1A

      { "PROP_NAME_ALIGN", "align" },                                              //$B1A
      { "PROP_HH_DESC_ALIGN", "The alignment of the HTML heading." },              //$B1A
      { "PROP_HA_DESC_ALIGN", "The horizontal alignment for a block of HTML." },   //$B1A

      { "PROP_NAME_LEVEL", "level" },                                              //$B1A
      { "PROP_HH_DESC_LEVEL", "The level of importance of the HTML header." },     //$B1A

      { "PROP_HH_DESC_TEXT", "The text displayed in the HTML heading." },          //$B1A


      { "PROP_HS_DESC_NAME", "The name of the servlet class file." },              //$B2A
      { "PROP_HS_DESC_TEXT", "The alternate text for the servlet to display." },   //$B2A
      { "PROP_HP_DESC_NAME", "The name of the parameter." },                       //$B2A $B4C
      { "PROP_HP_DESC_VALUE", "The value of the parameter." },                     //$B2A $B4C

      { "PROP_NAME_LOCATION", "location" },                                                            //$B2A
      { "PROP_HS_DESC_LOCATION", "The remote location from which the servlet should be loaded." },     //$B2A

      { "PROP_DESC_SERVLET_ALTTEXT", "If you see this text, the web server providing this page does not support the SERVLET tag." },     //$B2A

      { "PROP_NAME_LANGUAGE", "language" },                                                             //$B3A
      { "PROP_DESC_LANGUAGE", "The primary language to be used by the contents of the element." },      //$B3A

      { "PROP_NAME_DIRECTION", "direction" },                                                           //$B3A
      { "PROP_DESC_DIRECTION", "The direction of the text interpretation." },                           //$B3A

      { "PROP_HH_DESC_TITLE", "The title of the HTML document." },                                      //$B4A

      { "PROP_HM_DESC_NAME", "The name of the property." },                                             //$B4A

      { "PROP_NAME_HTTPEQUIV", "HTTP-EQUIV" },                                                          //$B4A
      { "PROP_HM_DESC_HTTPEQUIV", "The HTTP-EQUIV meta information." },                                 //$B4A

      { "PROP_NAME_CONTENT", "content" },                                                               //$B4A
      { "PROP_HM_DESC_CONTENT", "The value of a named property." },                                     //$B4A

      { "PROP_HM_DESC_URL", "The url to reload after the time specified in the content attribute." },   //$B4A


      { "PROP_NAME_ARCHIVE", "archive" },                                              //$B6A
      { "PROP_HA_DESC_ARCHIVE", "One or more archives containing classes and other resources that will be used by the applet." },  //$B6A

      { "PROP_NAME_CODE", "code" },                                                    //$B6A
      { "PROP_HA_DESC_CODE", "The name of the applet class." },                        //$B6A

      { "PROP_NAME_CODEBASE", "codebase" },                                            //$B6A
      { "PROP_HA_DESC_CODEBASE", "The base URL of the applet." },                      //$B6A

      { "PROP_HA_DESC_WIDTH", "The width of the applet in pixels." },                  //$B6A
      { "PROP_HA_DESC_HEIGHT", "The height of the applet in pixels." },                //$B6A
      { "PROP_DESC_APPLET_ALTTEXT", "If you see this text, the browser does not support the APPLET tag or the applet has failed to load." },  //$B6A

      { "PROP_NAME_ICONURL", "iconUrl" },                                                           //$B7A
      { "PROP_DESC_ICONURL", "The URL for the expanded and collapsed icon." },                      //$B7A
      { "PROP_HTE_DESC_TEXT", "The text displayed in the HTMLTreeElement." },                       //$B7A
      { "PROP_NAME_TEXTURL", "textUrl" },                                                           //$B7A
      { "PROP_DESC_TEXTURL", "The URL for the HTMLTreeElement text." },                             //$B7A
      { "PROP_NAME_REQUEST", "request" },                                                           //$B7A
      { "PROP_DESC_REQUEST", "The http servlet request." },                                         //$B7A
      { "PROP_NAME_SYSTEM",  "system" },                                                            //$B8A
      { "PROP_DESC_SYSTEM",  "The server on which the objects reside." },                           //$B8A
      { "PROP_NAME_RENDERER", "renderer" },                                                         //$B8A @B9C
      { "PROP_DESC_RENDERER", "The renderer used to display FileListElement data." },               //$B8A @B9C
      { "PROP_NAME_TABLE", "table" },                                                               //$B8A
      { "PROP_DESC_TABLE", "The HTML table used to display FileListElement data." },                //$B8A

      { "PROP_NAME_ATTRIBUTES", "attributes" },                                                     // @Z1A
      { "PROP_DESC_ATTRIBUTES", "The additional HTML tag attributes." },                            // @Z1A

      { "PROP_FLE_NAME_NAME", "Name" },                                                             // @C1A
      { "PROP_FLE_NAME_SIZE", "Size" },                                                             // @C1A
      { "PROP_FLE_NAME_TYPE", "Type" },                                                             // @C1A
      { "PROP_FLE_NAME_MODIFIED", "Modified" }                                                      // @C1A

   };

}
