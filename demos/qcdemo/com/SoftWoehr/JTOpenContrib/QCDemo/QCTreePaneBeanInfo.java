package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCTreePaneBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_nextFocusableComponent = 0;
  private static final int PROPERTY_validateRoot = 1;
  private static final int PROPERTY_font = 2;
  private static final int PROPERTY_selectionModel = 3;
  private static final int PROPERTY_colorModel = 4;
  private static final int PROPERTY_selectedObject = 5;
  private static final int PROPERTY_alignmentY = 6;
  private static final int PROPERTY_alignmentX = 7;
  private static final int PROPERTY_y = 8;
  private static final int PROPERTY_x = 9;
  private static final int PROPERTY_accessibleContext = 10;
  private static final int PROPERTY_layout = 11;
  private static final int PROPERTY_preferredSize = 12;
  private static final int PROPERTY_inputMethodRequests = 13;
  private static final int PROPERTY_verifyInputWhenFocusTarget = 14;
  private static final int PROPERTY_toolkit = 15;
  private static final int PROPERTY_displayable = 16;
  private static final int PROPERTY_class = 17;
  private static final int PROPERTY_root = 18;
  private static final int PROPERTY_autoscrolls = 19;
  private static final int PROPERTY_insets = 20;
  private static final int PROPERTY_cursor = 21;
  private static final int PROPERTY_showing = 22;
  private static final int PROPERTY_model = 23;
  private static final int PROPERTY_preferredSizeSet = 24;
  private static final int PROPERTY_components = 25;
  private static final int PROPERTY_focusTraversable = 26;
  private static final int PROPERTY_visible = 27;
  private static final int PROPERTY_background = 28;
  private static final int PROPERTY_UIClassID = 29;
  private static final int PROPERTY_graphicsConfiguration = 30;
  private static final int PROPERTY_inputContext = 31;
  private static final int PROPERTY_registeredKeyStrokes = 32;
  private static final int PROPERTY_valid = 33;
  private static final int PROPERTY_locale = 34;
  private static final int PROPERTY_componentOrientation = 35;
  private static final int PROPERTY_graphics = 36;
  private static final int PROPERTY_peer = 37;
  private static final int PROPERTY_locationOnScreen = 38;
  private static final int PROPERTY_name = 39;
  private static final int PROPERTY_foreground = 40;
  private static final int PROPERTY_actionMap = 41;
  private static final int PROPERTY_topLevelAncestor = 42;
  private static final int PROPERTY_paintingTile = 43;
  private static final int PROPERTY_focusCycleRoot = 44;
  private static final int PROPERTY_enabled = 45;
  private static final int PROPERTY_lightweight = 46;
  private static final int PROPERTY_dropTarget = 47;
  private static final int PROPERTY_opaque = 48;
  private static final int PROPERTY_componentCount = 49;
  private static final int PROPERTY_maximumSizeSet = 50;
  private static final int PROPERTY_confirm = 51;
  private static final int PROPERTY_toolTipText = 52;
  private static final int PROPERTY_height = 53;
  private static final int PROPERTY_doubleBuffered = 54;
  private static final int PROPERTY_border = 55;
  private static final int PROPERTY_minimumSizeSet = 56;
  private static final int PROPERTY_actionContext = 57;
  private static final int PROPERTY_treeLock = 58;
  private static final int PROPERTY_width = 59;
  private static final int PROPERTY_managingFocus = 60;
  private static final int PROPERTY_maximumSize = 61;
  private static final int PROPERTY_minimumSize = 62;
  private static final int PROPERTY_allowActions = 63;
  private static final int PROPERTY_visibleRect = 64;
  private static final int PROPERTY_debugGraphicsOptions = 65;
  private static final int PROPERTY_optimizedDrawingEnabled = 66;
  private static final int PROPERTY_requestFocusEnabled = 67;
  private static final int PROPERTY_parent = 68;
  private static final int PROPERTY_inputVerifier = 69;
  private static final int PROPERTY_rootPane = 70;
  private static final int PROPERTY_bounds = 71;
  private static final int PROPERTY_selectedObjects = 72;
  private static final int PROPERTY_component = 73;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[74];

  static {
    try {
      properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", QCTreePane.class, "getNextFocusableComponent", "setNextFocusableComponent" );
      properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", QCTreePane.class, "isValidateRoot", null );
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCTreePane.class, "getFont", "setFont" );
      properties[PROPERTY_selectionModel] = new PropertyDescriptor ( "selectionModel", QCTreePane.class, "getSelectionModel", "setSelectionModel" );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCTreePane.class, "getColorModel", null );
      properties[PROPERTY_selectedObject] = new PropertyDescriptor ( "selectedObject", QCTreePane.class, "getSelectedObject", null );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCTreePane.class, "getAlignmentY", "setAlignmentY" );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCTreePane.class, "getAlignmentX", "setAlignmentX" );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCTreePane.class, "getY", null );
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCTreePane.class, "getX", null );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCTreePane.class, "getAccessibleContext", null );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCTreePane.class, "getLayout", "setLayout" );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCTreePane.class, "getPreferredSize", "setPreferredSize" );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCTreePane.class, "getInputMethodRequests", null );
      properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", QCTreePane.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" );
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCTreePane.class, "getToolkit", null );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCTreePane.class, "isDisplayable", null );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCTreePane.class, "getClass", null );
      properties[PROPERTY_root] = new PropertyDescriptor ( "root", QCTreePane.class, "getRoot", "setRoot" );
      properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", QCTreePane.class, "getAutoscrolls", "setAutoscrolls" );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCTreePane.class, "getInsets", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCTreePane.class, "getCursor", "setCursor" );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCTreePane.class, "isShowing", null );
      properties[PROPERTY_model] = new PropertyDescriptor ( "model", QCTreePane.class, "getModel", null );
      properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", QCTreePane.class, "isPreferredSizeSet", null );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCTreePane.class, "getComponents", null );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCTreePane.class, "isFocusTraversable", null );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCTreePane.class, "isVisible", "setVisible" );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCTreePane.class, "getBackground", "setBackground" );
      properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", QCTreePane.class, "getUIClassID", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCTreePane.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCTreePane.class, "getInputContext", null );
      properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", QCTreePane.class, "getRegisteredKeyStrokes", null );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCTreePane.class, "isValid", null );
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCTreePane.class, "getLocale", "setLocale" );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCTreePane.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCTreePane.class, "getGraphics", null );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCTreePane.class, "getPeer", null );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCTreePane.class, "getLocationOnScreen", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCTreePane.class, "getName", "setName" );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCTreePane.class, "getForeground", "setForeground" );
      properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", QCTreePane.class, "getActionMap", "setActionMap" );
      properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", QCTreePane.class, "getTopLevelAncestor", null );
      properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", QCTreePane.class, "isPaintingTile", null );
      properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", QCTreePane.class, "isFocusCycleRoot", null );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCTreePane.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCTreePane.class, "isLightweight", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCTreePane.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCTreePane.class, "isOpaque", "setOpaque" );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCTreePane.class, "getComponentCount", null );
      properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", QCTreePane.class, "isMaximumSizeSet", null );
      properties[PROPERTY_confirm] = new PropertyDescriptor ( "confirm", QCTreePane.class, "getConfirm", "setConfirm" );
      properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", QCTreePane.class, "getToolTipText", "setToolTipText" );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCTreePane.class, "getHeight", null );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCTreePane.class, "isDoubleBuffered", "setDoubleBuffered" );
      properties[PROPERTY_border] = new PropertyDescriptor ( "border", QCTreePane.class, "getBorder", "setBorder" );
      properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", QCTreePane.class, "isMinimumSizeSet", null );
      properties[PROPERTY_actionContext] = new PropertyDescriptor ( "actionContext", QCTreePane.class, "getActionContext", null );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCTreePane.class, "getTreeLock", null );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCTreePane.class, "getWidth", null );
      properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", QCTreePane.class, "isManagingFocus", null );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCTreePane.class, "getMaximumSize", "setMaximumSize" );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCTreePane.class, "getMinimumSize", "setMinimumSize" );
      properties[PROPERTY_allowActions] = new PropertyDescriptor ( "allowActions", QCTreePane.class, "getAllowActions", "setAllowActions" );
      properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", QCTreePane.class, "getVisibleRect", null );
      properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", QCTreePane.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" );
      properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", QCTreePane.class, "isOptimizedDrawingEnabled", null );
      properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", QCTreePane.class, "isRequestFocusEnabled", "setRequestFocusEnabled" );
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCTreePane.class, "getParent", null );
      properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", QCTreePane.class, "getInputVerifier", "setInputVerifier" );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCTreePane.class, "getRootPane", null );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCTreePane.class, "getBounds", "setBounds" );
      properties[PROPERTY_selectedObjects] = new PropertyDescriptor ( "selectedObjects", QCTreePane.class, "getSelectedObjects", null );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCTreePane.class, null, null, "getComponent", null );
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
  private static final int EVENT_treeSelectionListener = 7;
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
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCTreePane.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCTreePane.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( QCTreePane.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[0], "addAncestorListener", "removeAncestorListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCTreePane.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCTreePane.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCTreePane.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCTreePane.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_treeSelectionListener] = new EventSetDescriptor ( QCTreePane.class, "treeSelectionListener", javax.swing.event.TreeSelectionListener.class, new String[0], "addTreeSelectionListener", "removeTreeSelectionListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCTreePane.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCTreePane.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCTreePane.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCTreePane.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
      eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( QCTreePane.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[0], "addVetoableChangeListener", "removeVetoableChangeListener" );
      eventSets[EVENT_errorListener] = new EventSetDescriptor ( QCTreePane.class, "errorListener", com.ibm.as400.vaccess.ErrorListener.class, new String[0], "addErrorListener", "removeErrorListener" );
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
