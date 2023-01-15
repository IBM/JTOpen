package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCListPaneBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_toolkit = 0;
  private static final int PROPERTY_rootPane = 1;
  private static final int PROPERTY_visible = 2;
  private static final int PROPERTY_topLevelAncestor = 3;
  private static final int PROPERTY_treeLock = 4;
  private static final int PROPERTY_maximumSize = 5;
  private static final int PROPERTY_peer = 6;
  private static final int PROPERTY_nextFocusableComponent = 7;
  private static final int PROPERTY_componentOrientation = 8;
  private static final int PROPERTY_focusCycleRoot = 9;
  private static final int PROPERTY_validateRoot = 10;
  private static final int PROPERTY_selectionModel = 11;
  private static final int PROPERTY_colorModel = 12;
  private static final int PROPERTY_model = 13;
  private static final int PROPERTY_components = 14;
  private static final int PROPERTY_y = 15;
  private static final int PROPERTY_x = 16;
  private static final int PROPERTY_enabled = 17;
  private static final int PROPERTY_opaque = 18;
  private static final int PROPERTY_valid = 19;
  private static final int PROPERTY_verifyInputWhenFocusTarget = 20;
  private static final int PROPERTY_bounds = 21;
  private static final int PROPERTY_inputVerifier = 22;
  private static final int PROPERTY_autoscrolls = 23;
  private static final int PROPERTY_optimizedDrawingEnabled = 24;
  private static final int PROPERTY_displayable = 25;
  private static final int PROPERTY_allowActions = 26;
  private static final int PROPERTY_toolTipText = 27;
  private static final int PROPERTY_locale = 28;
  private static final int PROPERTY_locationOnScreen = 29;
  private static final int PROPERTY_layout = 30;
  private static final int PROPERTY_requestFocusEnabled = 31;
  private static final int PROPERTY_parent = 32;
  private static final int PROPERTY_minimumSizeSet = 33;
  private static final int PROPERTY_foreground = 34;
  private static final int PROPERTY_font = 35;
  private static final int PROPERTY_graphics = 36;
  private static final int PROPERTY_UIClassID = 37;
  private static final int PROPERTY_width = 38;
  private static final int PROPERTY_class = 39;
  private static final int PROPERTY_lightweight = 40;
  private static final int PROPERTY_accessibleContext = 41;
  private static final int PROPERTY_name = 42;
  private static final int PROPERTY_showing = 43;
  private static final int PROPERTY_confirm = 44;
  private static final int PROPERTY_componentCount = 45;
  private static final int PROPERTY_border = 46;
  private static final int PROPERTY_minimumSize = 47;
  private static final int PROPERTY_registeredKeyStrokes = 48;
  private static final int PROPERTY_doubleBuffered = 49;
  private static final int PROPERTY_debugGraphicsOptions = 50;
  private static final int PROPERTY_height = 51;
  private static final int PROPERTY_insets = 52;
  private static final int PROPERTY_paintingTile = 53;
  private static final int PROPERTY_focusTraversable = 54;
  private static final int PROPERTY_alignmentY = 55;
  private static final int PROPERTY_alignmentX = 56;
  private static final int PROPERTY_root = 57;
  private static final int PROPERTY_selectedObjects = 58;
  private static final int PROPERTY_dropTarget = 59;
  private static final int PROPERTY_managingFocus = 60;
  private static final int PROPERTY_preferredSize = 61;
  private static final int PROPERTY_selectedObject = 62;
  private static final int PROPERTY_background = 63;
  private static final int PROPERTY_inputContext = 64;
  private static final int PROPERTY_preferredSizeSet = 65;
  private static final int PROPERTY_cursor = 66;
  private static final int PROPERTY_visibleRowCount = 67;
  private static final int PROPERTY_maximumSizeSet = 68;
  private static final int PROPERTY_actionMap = 69;
  private static final int PROPERTY_actionContext = 70;
  private static final int PROPERTY_inputMethodRequests = 71;
  private static final int PROPERTY_graphicsConfiguration = 72;
  private static final int PROPERTY_visibleRect = 73;
  private static final int PROPERTY_component = 74;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[75];

  static {
    try {
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCListPane.class, "getToolkit", null );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCListPane.class, "getRootPane", null );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCListPane.class, "isVisible", "setVisible" );
      properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", QCListPane.class, "getTopLevelAncestor", null );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCListPane.class, "getTreeLock", null );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCListPane.class, "getMaximumSize", "setMaximumSize" );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCListPane.class, "getPeer", null );
      properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", QCListPane.class, "getNextFocusableComponent", "setNextFocusableComponent" );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCListPane.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", QCListPane.class, "isFocusCycleRoot", null );
      properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", QCListPane.class, "isValidateRoot", null );
      properties[PROPERTY_selectionModel] = new PropertyDescriptor ( "selectionModel", QCListPane.class, "getSelectionModel", "setSelectionModel" );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCListPane.class, "getColorModel", null );
      properties[PROPERTY_model] = new PropertyDescriptor ( "model", QCListPane.class, "getModel", null );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCListPane.class, "getComponents", null );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCListPane.class, "getY", null );
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCListPane.class, "getX", null );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCListPane.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCListPane.class, "isOpaque", "setOpaque" );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCListPane.class, "isValid", null );
      properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", QCListPane.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCListPane.class, "getBounds", "setBounds" );
      properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", QCListPane.class, "getInputVerifier", "setInputVerifier" );
      properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", QCListPane.class, "getAutoscrolls", "setAutoscrolls" );
      properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", QCListPane.class, "isOptimizedDrawingEnabled", null );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCListPane.class, "isDisplayable", null );
      properties[PROPERTY_allowActions] = new PropertyDescriptor ( "allowActions", QCListPane.class, "getAllowActions", "setAllowActions" );
      properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", QCListPane.class, "getToolTipText", "setToolTipText" );
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCListPane.class, "getLocale", "setLocale" );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCListPane.class, "getLocationOnScreen", null );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCListPane.class, "getLayout", "setLayout" );
      properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", QCListPane.class, "isRequestFocusEnabled", "setRequestFocusEnabled" );
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCListPane.class, "getParent", null );
      properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", QCListPane.class, "isMinimumSizeSet", null );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCListPane.class, "getForeground", "setForeground" );
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCListPane.class, "getFont", "setFont" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCListPane.class, "getGraphics", null );
      properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", QCListPane.class, "getUIClassID", null );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCListPane.class, "getWidth", null );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCListPane.class, "getClass", null );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCListPane.class, "isLightweight", null );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCListPane.class, "getAccessibleContext", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCListPane.class, "getName", "setName" );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCListPane.class, "isShowing", null );
      properties[PROPERTY_confirm] = new PropertyDescriptor ( "confirm", QCListPane.class, "getConfirm", "setConfirm" );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCListPane.class, "getComponentCount", null );
      properties[PROPERTY_border] = new PropertyDescriptor ( "border", QCListPane.class, "getBorder", "setBorder" );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCListPane.class, "getMinimumSize", "setMinimumSize" );
      properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", QCListPane.class, "getRegisteredKeyStrokes", null );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCListPane.class, "isDoubleBuffered", "setDoubleBuffered" );
      properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", QCListPane.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCListPane.class, "getHeight", null );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCListPane.class, "getInsets", null );
      properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", QCListPane.class, "isPaintingTile", null );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCListPane.class, "isFocusTraversable", null );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCListPane.class, "getAlignmentY", "setAlignmentY" );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCListPane.class, "getAlignmentX", "setAlignmentX" );
      properties[PROPERTY_root] = new PropertyDescriptor ( "root", QCListPane.class, "getRoot", "setRoot" );
      properties[PROPERTY_selectedObjects] = new PropertyDescriptor ( "selectedObjects", QCListPane.class, "getSelectedObjects", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCListPane.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", QCListPane.class, "isManagingFocus", null );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCListPane.class, "getPreferredSize", "setPreferredSize" );
      properties[PROPERTY_selectedObject] = new PropertyDescriptor ( "selectedObject", QCListPane.class, "getSelectedObject", null );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCListPane.class, "getBackground", "setBackground" );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCListPane.class, "getInputContext", null );
      properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", QCListPane.class, "isPreferredSizeSet", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCListPane.class, "getCursor", "setCursor" );
      properties[PROPERTY_visibleRowCount] = new PropertyDescriptor ( "visibleRowCount", QCListPane.class, "getVisibleRowCount", "setVisibleRowCount" );
      properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", QCListPane.class, "isMaximumSizeSet", null );
      properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", QCListPane.class, "getActionMap", "setActionMap" );
      properties[PROPERTY_actionContext] = new PropertyDescriptor ( "actionContext", QCListPane.class, "getActionContext", null );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCListPane.class, "getInputMethodRequests", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCListPane.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", QCListPane.class, "getVisibleRect", null );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCListPane.class, null, null, "getComponent", null );
    }
    catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
  
  // Here you can add code for customizing the properties array.  

}//GEN-LAST:Properties

  // EventSet identifiers//GEN-FIRST:Events
  private static final int EVENT_mouseMotionListener = 0;
  private static final int EVENT_inputMethodListener = 1;
  private static final int EVENT_ancestorListener = 2;
  private static final int EVENT_componentListener = 3;
  private static final int EVENT_hierarchyBoundsListener = 4;
  private static final int EVENT_mouseListener = 5;
  private static final int EVENT_focusListener = 6;
  private static final int EVENT_listSelectionListener = 7;
  private static final int EVENT_propertyChangeListener = 8;
  private static final int EVENT_keyListener = 9;
  private static final int EVENT_hierarchyListener = 10;
  private static final int EVENT_containerListener = 11;
  private static final int EVENT_vetoableChangeListener = 12;
  private static final int EVENT_errorListener = 13;

  // EventSet array
  private static EventSetDescriptor[] eventSets = new EventSetDescriptor[14];

  static {
    try {
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCListPane.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCListPane.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( QCListPane.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[0], "addAncestorListener", "removeAncestorListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCListPane.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCListPane.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCListPane.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCListPane.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_listSelectionListener] = new EventSetDescriptor ( QCListPane.class, "listSelectionListener", javax.swing.event.ListSelectionListener.class, new String[0], "addListSelectionListener", "removeListSelectionListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCListPane.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCListPane.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCListPane.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCListPane.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
      eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( QCListPane.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[0], "addVetoableChangeListener", "removeVetoableChangeListener" );
      eventSets[EVENT_errorListener] = new EventSetDescriptor ( QCListPane.class, "errorListener", com.ibm.as400.vaccess.ErrorListener.class, new String[0], "addErrorListener", "removeErrorListener" );
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
