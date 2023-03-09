package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCTopFrameBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_x = 0;
  private static final int PROPERTY_owner = 1;
  private static final int PROPERTY_background = 2;
  private static final int PROPERTY_rootPane = 3;
  private static final int PROPERTY_dropTarget = 4;
  private static final int PROPERTY_graphics = 5;
  private static final int PROPERTY_name = 6;
  private static final int PROPERTY_locationOnScreen = 7;
  private static final int PROPERTY_bounds = 8;
  private static final int PROPERTY_components = 9;
  private static final int PROPERTY_layout = 10;
  private static final int PROPERTY_JMenuBar = 11;
  private static final int PROPERTY_glassPane = 12;
  private static final int PROPERTY_accessibleContext = 13;
  private static final int PROPERTY_defaultCloseOperation = 14;
  private static final int PROPERTY_font = 15;
  private static final int PROPERTY_cursorType = 16;
  private static final int PROPERTY_cursor = 17;
  private static final int PROPERTY_preferredSize = 18;
  private static final int PROPERTY_focusTraversable = 19;
  private static final int PROPERTY_width = 20;
  private static final int PROPERTY_iconImage = 21;
  private static final int PROPERTY_maximumSize = 22;
  private static final int PROPERTY_enabled = 23;
  private static final int PROPERTY_treeLock = 24;
  private static final int PROPERTY_ownedWindows = 25;
  private static final int PROPERTY_visible = 26;
  private static final int PROPERTY_insets = 27;
  private static final int PROPERTY_resizable = 28;
  private static final int PROPERTY_contentPane = 29;
  private static final int PROPERTY_state = 30;
  private static final int PROPERTY_title = 31;
  private static final int PROPERTY_lightweight = 32;
  private static final int PROPERTY_componentCount = 33;
  private static final int PROPERTY_menuBar = 34;
  private static final int PROPERTY_parent = 35;
  private static final int PROPERTY_componentOrientation = 36;
  private static final int PROPERTY_opaque = 37;
  private static final int PROPERTY_showing = 38;
  private static final int PROPERTY_inputContext = 39;
  private static final int PROPERTY_locale = 40;
  private static final int PROPERTY_valid = 41;
  private static final int PROPERTY_foreground = 42;
  private static final int PROPERTY_colorModel = 43;
  private static final int PROPERTY_warningString = 44;
  private static final int PROPERTY_doubleBuffered = 45;
  private static final int PROPERTY_class = 46;
  private static final int PROPERTY_layeredPane = 47;
  private static final int PROPERTY_minimumSize = 48;
  private static final int PROPERTY_toolkit = 49;
  private static final int PROPERTY_peer = 50;
  private static final int PROPERTY_inputMethodRequests = 51;
  private static final int PROPERTY_height = 52;
  private static final int PROPERTY_displayable = 53;
  private static final int PROPERTY_alignmentY = 54;
  private static final int PROPERTY_graphicsConfiguration = 55;
  private static final int PROPERTY_alignmentX = 56;
  private static final int PROPERTY_focusOwner = 57;
  private static final int PROPERTY_y = 58;
  private static final int PROPERTY_component = 59;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[60];

  static {
    try {
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCTopFrame.class, "getX", null );
      properties[PROPERTY_owner] = new PropertyDescriptor ( "owner", QCTopFrame.class, "getOwner", null );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCTopFrame.class, "getBackground", "setBackground" );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCTopFrame.class, "getRootPane", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCTopFrame.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCTopFrame.class, "getGraphics", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCTopFrame.class, "getName", "setName" );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCTopFrame.class, "getLocationOnScreen", null );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCTopFrame.class, "getBounds", "setBounds" );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCTopFrame.class, "getComponents", null );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCTopFrame.class, "getLayout", "setLayout" );
      properties[PROPERTY_JMenuBar] = new PropertyDescriptor ( "JMenuBar", QCTopFrame.class, "getJMenuBar", "setJMenuBar" );
      properties[PROPERTY_glassPane] = new PropertyDescriptor ( "glassPane", QCTopFrame.class, "getGlassPane", "setGlassPane" );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCTopFrame.class, "getAccessibleContext", null );
      properties[PROPERTY_defaultCloseOperation] = new PropertyDescriptor ( "defaultCloseOperation", QCTopFrame.class, "getDefaultCloseOperation", "setDefaultCloseOperation" );
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCTopFrame.class, "getFont", "setFont" );
      properties[PROPERTY_cursorType] = new PropertyDescriptor ( "cursorType", QCTopFrame.class, "getCursorType", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCTopFrame.class, "getCursor", "setCursor" );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCTopFrame.class, "getPreferredSize", null );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCTopFrame.class, "isFocusTraversable", null );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCTopFrame.class, "getWidth", null );
      properties[PROPERTY_iconImage] = new PropertyDescriptor ( "iconImage", QCTopFrame.class, "getIconImage", "setIconImage" );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCTopFrame.class, "getMaximumSize", null );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCTopFrame.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCTopFrame.class, "getTreeLock", null );
      properties[PROPERTY_ownedWindows] = new PropertyDescriptor ( "ownedWindows", QCTopFrame.class, "getOwnedWindows", null );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCTopFrame.class, "isVisible", "setVisible" );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCTopFrame.class, "getInsets", null );
      properties[PROPERTY_resizable] = new PropertyDescriptor ( "resizable", QCTopFrame.class, "isResizable", "setResizable" );
      properties[PROPERTY_contentPane] = new PropertyDescriptor ( "contentPane", QCTopFrame.class, "getContentPane", "setContentPane" );
      properties[PROPERTY_state] = new PropertyDescriptor ( "state", QCTopFrame.class, "getState", "setState" );
      properties[PROPERTY_title] = new PropertyDescriptor ( "title", QCTopFrame.class, "getTitle", "setTitle" );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCTopFrame.class, "isLightweight", null );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCTopFrame.class, "getComponentCount", null );
      properties[PROPERTY_menuBar] = new PropertyDescriptor ( "menuBar", QCTopFrame.class, "getMenuBar", "setMenuBar" );
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCTopFrame.class, "getParent", null );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCTopFrame.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCTopFrame.class, "isOpaque", null );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCTopFrame.class, "isShowing", null );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCTopFrame.class, "getInputContext", null );
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCTopFrame.class, "getLocale", "setLocale" );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCTopFrame.class, "isValid", null );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCTopFrame.class, "getForeground", "setForeground" );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCTopFrame.class, "getColorModel", null );
      properties[PROPERTY_warningString] = new PropertyDescriptor ( "warningString", QCTopFrame.class, "getWarningString", null );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCTopFrame.class, "isDoubleBuffered", null );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCTopFrame.class, "getClass", null );
      properties[PROPERTY_layeredPane] = new PropertyDescriptor ( "layeredPane", QCTopFrame.class, "getLayeredPane", "setLayeredPane" );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCTopFrame.class, "getMinimumSize", null );
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCTopFrame.class, "getToolkit", null );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCTopFrame.class, "getPeer", null );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCTopFrame.class, "getInputMethodRequests", null );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCTopFrame.class, "getHeight", null );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCTopFrame.class, "isDisplayable", null );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCTopFrame.class, "getAlignmentY", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCTopFrame.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCTopFrame.class, "getAlignmentX", null );
      properties[PROPERTY_focusOwner] = new PropertyDescriptor ( "focusOwner", QCTopFrame.class, "getFocusOwner", null );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCTopFrame.class, "getY", null );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCTopFrame.class, null, null, "getComponent", null );
    }
    catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
  
  // Here you can add code for customizing the properties array.  

}//GEN-LAST:Properties

  // EventSet identifiers//GEN-FIRST:Events
  private static final int EVENT_mouseMotionListener = 0;
  private static final int EVENT_inputMethodListener = 1;
  private static final int EVENT_componentListener = 2;
  private static final int EVENT_hierarchyBoundsListener = 3;
  private static final int EVENT_mouseListener = 4;
  private static final int EVENT_focusListener = 5;
  private static final int EVENT_propertyChangeListener = 6;
  private static final int EVENT_windowListener = 7;
  private static final int EVENT_keyListener = 8;
  private static final int EVENT_hierarchyListener = 9;
  private static final int EVENT_containerListener = 10;

  // EventSet array
  private static EventSetDescriptor[] eventSets = new EventSetDescriptor[11];

  static {
    try {
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCTopFrame.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCTopFrame.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCTopFrame.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCTopFrame.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCTopFrame.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCTopFrame.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCTopFrame.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_windowListener] = new EventSetDescriptor ( QCTopFrame.class, "windowListener", java.awt.event.WindowListener.class, new String[0], "addWindowListener", "removeWindowListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCTopFrame.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCTopFrame.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCTopFrame.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
    }
    catch( IntrospectionException e) {}//GEN-HEADEREND:Events

  // Here you can add code for customizing the event sets array.  

}//GEN-LAST:Events

  private static java.awt.Image iconColor16 = null; //GEN-BEGIN:IconsDef
  private static java.awt.Image iconColor32 = null;
  private static java.awt.Image iconMono16 = null;
  private static java.awt.Image iconMono32 = null; //GEN-END:IconsDef
  private static String iconNameC16 = null;//GEN-BEGIN:Icons
  private static String iconNameC32 = null;
  private static String iconNameM16 = null;
  private static String iconNameM32 = null;//GEN-END:Icons
                                                 
  private static int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
  private static int defaultEventIndex = -1;//GEN-END:Idx


  /**
   * Gets the beans <code>PropertyDescriptor</code>s.
   * 
   * @return An array of PropertyDescriptors describing the editable
   * properties supported by this bean.  May return null if the
   * information should be obtained by automatic analysis.
   * <p>
   * If a property is indexed, then its entry in the result array will
   * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
   * A client of getPropertyDescriptors can use "instanceof" to check
   * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
   */
  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }

  /**
   * Gets the beans <code>EventSetDescriptor</code>s.
   * 
   * @return  An array of EventSetDescriptors describing the kinds of 
   * events fired by this bean.  May return null if the information
   * should be obtained by automatic analysis.
   */
  public EventSetDescriptor[] getEventSetDescriptors() {
    return eventSets;
  }

  /**
   * A bean may have a "default" property that is the property that will
   * mostly commonly be initially chosen for update by human's who are 
   * customizing the bean.
   * @return  Index of default property in the PropertyDescriptor array
   * 		returned by getPropertyDescriptors.
   * <P>	Returns -1 if there is no default property.
   */
  public int getDefaultPropertyIndex() {
    return defaultPropertyIndex;
  }

  /**
   * A bean may have a "default" event that is the event that will
   * mostly commonly be used by human's when using the bean. 
   * @return Index of default event in the EventSetDescriptor array
   *		returned by getEventSetDescriptors.
   * <P>	Returns -1 if there is no default event.
   */
  public int getDefaultEventIndex() {
    return defaultPropertyIndex;
  }

  /**
   * This method returns an image object that can be used to
   * represent the bean in toolboxes, toolbars, etc.   Icon images
   * will typically be GIFs, but may in future include other formats.
   * <p>
   * Beans aren't required to provide icons and may return null from
   * this method.
   * <p>
   * There are four possible flavors of icons (16x16 color,
   * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
   * support a single icon we recommend supporting 16x16 color.
   * <p>
   * We recommend that icons have a "transparent" background
   * so they can be rendered onto an existing background.
   *
   * @param  iconKind  The kind of icon requested.  This should be
   *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32, 
   *    ICON_MONO_16x16, or ICON_MONO_32x32.
   * @return  An image object representing the requested icon.  May
   *    return null if no suitable icon is available.
   */
  public java.awt.Image getIcon(int iconKind) {
    switch ( iconKind ) {
      case ICON_COLOR_16x16:
        if ( iconNameC16 == null )
          return null;
        else {
          if( iconColor16 == null )
            iconColor16 = loadImage( iconNameC16 );
          return iconColor16;
          }
      case ICON_COLOR_32x32:
        if ( iconNameC32 == null )
          return null;
        else {
          if( iconColor32 == null )
            iconColor32 = loadImage( iconNameC32 );
          return iconColor32;
          }
      case ICON_MONO_16x16:
        if ( iconNameM16 == null )
          return null;
        else {
          if( iconMono16 == null )
            iconMono16 = loadImage( iconNameM16 );
          return iconMono16;
          }
      case ICON_MONO_32x32:
        if ( iconNameM32 == null )
          return null;
        else {
          if( iconNameM32 == null )
            iconMono32 = loadImage( iconNameM32 );
          return iconMono32;
          }
    }
    return null;
  }

}
