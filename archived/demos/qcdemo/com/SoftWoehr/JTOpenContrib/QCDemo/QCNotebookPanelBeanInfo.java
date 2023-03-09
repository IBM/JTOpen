package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCNotebookPanelBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_font = 0;
  private static final int PROPERTY_componentCount = 1;
  private static final int PROPERTY_height = 2;
  private static final int PROPERTY_width = 3;
  private static final int PROPERTY_insets = 4;
  private static final int PROPERTY_opaque = 5;
  private static final int PROPERTY_displayable = 6;
  private static final int PROPERTY_optimizedDrawingEnabled = 7;
  private static final int PROPERTY_name = 8;
  private static final int PROPERTY_managingFocus = 9;
  private static final int PROPERTY_dropTarget = 10;
  private static final int PROPERTY_parent = 11;
  private static final int PROPERTY_layout = 12;
  private static final int PROPERTY_y = 13;
  private static final int PROPERTY_x = 14;
  private static final int PROPERTY_graphicsConfiguration = 15;
  private static final int PROPERTY_treeLock = 16;
  private static final int PROPERTY_bounds = 17;
  private static final int PROPERTY_minimumSize = 18;
  private static final int PROPERTY_registeredKeyStrokes = 19;
  private static final int PROPERTY_minimumSizeSet = 20;
  private static final int PROPERTY_inputContext = 21;
  private static final int PROPERTY_UIClassID = 22;
  private static final int PROPERTY_showing = 23;
  private static final int PROPERTY_paintingTile = 24;
  private static final int PROPERTY_cursor = 25;
  private static final int PROPERTY_enabled = 26;
  private static final int PROPERTY_colorModel = 27;
  private static final int PROPERTY_rootPane = 28;
  private static final int PROPERTY_accessibleContext = 29;
  private static final int PROPERTY_focusCycleRoot = 30;
  private static final int PROPERTY_visible = 31;
  private static final int PROPERTY_valid = 32;
  private static final int PROPERTY_componentOrientation = 33;
  private static final int PROPERTY_autoscrolls = 34;
  private static final int PROPERTY_nextFocusableComponent = 35;
  private static final int PROPERTY_background = 36;
  private static final int PROPERTY_debugGraphicsOptions = 37;
  private static final int PROPERTY_lightweight = 38;
  private static final int PROPERTY_doubleBuffered = 39;
  private static final int PROPERTY_graphics = 40;
  private static final int PROPERTY_topLevelAncestor = 41;
  private static final int PROPERTY_validateRoot = 42;
  private static final int PROPERTY_locale = 43;
  private static final int PROPERTY_toolkit = 44;
  private static final int PROPERTY_preferredSize = 45;
  private static final int PROPERTY_visibleRect = 46;
  private static final int PROPERTY_peer = 47;
  private static final int PROPERTY_components = 48;
  private static final int PROPERTY_toolTipText = 49;
  private static final int PROPERTY_verifyInputWhenFocusTarget = 50;
  private static final int PROPERTY_maximumSizeSet = 51;
  private static final int PROPERTY_requestFocusEnabled = 52;
  private static final int PROPERTY_actionMap = 53;
  private static final int PROPERTY_inputVerifier = 54;
  private static final int PROPERTY_focusTraversable = 55;
  private static final int PROPERTY_preferredSizeSet = 56;
  private static final int PROPERTY_foreground = 57;
  private static final int PROPERTY_maximumSize = 58;
  private static final int PROPERTY_class = 59;
  private static final int PROPERTY_inputMethodRequests = 60;
  private static final int PROPERTY_alignmentY = 61;
  private static final int PROPERTY_alignmentX = 62;
  private static final int PROPERTY_locationOnScreen = 63;
  private static final int PROPERTY_border = 64;
  private static final int PROPERTY_component = 65;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[66];

  static {
    try {
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCNotebookPanel.class, "getFont", "setFont" );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCNotebookPanel.class, "getComponentCount", null );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCNotebookPanel.class, "getHeight", null );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCNotebookPanel.class, "getWidth", null );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCNotebookPanel.class, "getInsets", null );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCNotebookPanel.class, "isOpaque", "setOpaque" );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCNotebookPanel.class, "isDisplayable", null );
      properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", QCNotebookPanel.class, "isOptimizedDrawingEnabled", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCNotebookPanel.class, "getName", "setName" );
      properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", QCNotebookPanel.class, "isManagingFocus", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCNotebookPanel.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCNotebookPanel.class, "getParent", null );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCNotebookPanel.class, "getLayout", "setLayout" );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCNotebookPanel.class, "getY", null );
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCNotebookPanel.class, "getX", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCNotebookPanel.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCNotebookPanel.class, "getTreeLock", null );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCNotebookPanel.class, "getBounds", "setBounds" );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCNotebookPanel.class, "getMinimumSize", "setMinimumSize" );
      properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", QCNotebookPanel.class, "getRegisteredKeyStrokes", null );
      properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", QCNotebookPanel.class, "isMinimumSizeSet", null );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCNotebookPanel.class, "getInputContext", null );
      properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", QCNotebookPanel.class, "getUIClassID", null );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCNotebookPanel.class, "isShowing", null );
      properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", QCNotebookPanel.class, "isPaintingTile", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCNotebookPanel.class, "getCursor", "setCursor" );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCNotebookPanel.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCNotebookPanel.class, "getColorModel", null );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCNotebookPanel.class, "getRootPane", null );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCNotebookPanel.class, "getAccessibleContext", null );
      properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", QCNotebookPanel.class, "isFocusCycleRoot", null );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCNotebookPanel.class, "isVisible", "setVisible" );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCNotebookPanel.class, "isValid", null );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCNotebookPanel.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", QCNotebookPanel.class, "getAutoscrolls", "setAutoscrolls" );
      properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", QCNotebookPanel.class, "getNextFocusableComponent", "setNextFocusableComponent" );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCNotebookPanel.class, "getBackground", "setBackground" );
      properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", QCNotebookPanel.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCNotebookPanel.class, "isLightweight", null );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCNotebookPanel.class, "isDoubleBuffered", "setDoubleBuffered" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCNotebookPanel.class, "getGraphics", null );
      properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", QCNotebookPanel.class, "getTopLevelAncestor", null );
      properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", QCNotebookPanel.class, "isValidateRoot", null );
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCNotebookPanel.class, "getLocale", "setLocale" );
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCNotebookPanel.class, "getToolkit", null );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCNotebookPanel.class, "getPreferredSize", "setPreferredSize" );
      properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", QCNotebookPanel.class, "getVisibleRect", null );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCNotebookPanel.class, "getPeer", null );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCNotebookPanel.class, "getComponents", null );
      properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", QCNotebookPanel.class, "getToolTipText", "setToolTipText" );
      properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", QCNotebookPanel.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" );
      properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", QCNotebookPanel.class, "isMaximumSizeSet", null );
      properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", QCNotebookPanel.class, "isRequestFocusEnabled", "setRequestFocusEnabled" );
      properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", QCNotebookPanel.class, "getActionMap", "setActionMap" );
      properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", QCNotebookPanel.class, "getInputVerifier", "setInputVerifier" );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCNotebookPanel.class, "isFocusTraversable", null );
      properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", QCNotebookPanel.class, "isPreferredSizeSet", null );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCNotebookPanel.class, "getForeground", "setForeground" );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCNotebookPanel.class, "getMaximumSize", "setMaximumSize" );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCNotebookPanel.class, "getClass", null );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCNotebookPanel.class, "getInputMethodRequests", null );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCNotebookPanel.class, "getAlignmentY", "setAlignmentY" );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCNotebookPanel.class, "getAlignmentX", "setAlignmentX" );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCNotebookPanel.class, "getLocationOnScreen", null );
      properties[PROPERTY_border] = new PropertyDescriptor ( "border", QCNotebookPanel.class, "getBorder", "setBorder" );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCNotebookPanel.class, null, null, "getComponent", null );
    }
    catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
  
  // Here you can add code for customizing the properties array.  

}//GEN-LAST:Properties

  // EventSet identifiers//GEN-FIRST:Events
  private static final int EVENT_mouseMotionListener = 0;
  private static final int EVENT_ancestorListener = 1;
  private static final int EVENT_inputMethodListener = 2;
  private static final int EVENT_componentListener = 3;
  private static final int EVENT_hierarchyBoundsListener = 4;
  private static final int EVENT_mouseListener = 5;
  private static final int EVENT_focusListener = 6;
  private static final int EVENT_propertyChangeListener = 7;
  private static final int EVENT_keyListener = 8;
  private static final int EVENT_hierarchyListener = 9;
  private static final int EVENT_containerListener = 10;
  private static final int EVENT_vetoableChangeListener = 11;

  // EventSet array
  private static EventSetDescriptor[] eventSets = new EventSetDescriptor[12];

  static {
    try {
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCNotebookPanel.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( QCNotebookPanel.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[0], "addAncestorListener", "removeAncestorListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCNotebookPanel.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCNotebookPanel.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCNotebookPanel.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCNotebookPanel.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCNotebookPanel.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCNotebookPanel.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCNotebookPanel.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCNotebookPanel.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCNotebookPanel.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
      eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( QCNotebookPanel.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[0], "addVetoableChangeListener", "removeVetoableChangeListener" );
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
