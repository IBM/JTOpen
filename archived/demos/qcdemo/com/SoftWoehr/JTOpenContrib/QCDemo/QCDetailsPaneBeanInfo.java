package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCDetailsPaneBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_locale = 0;
  private static final int PROPERTY_alignmentY = 1;
  private static final int PROPERTY_alignmentX = 2;
  private static final int PROPERTY_treeLock = 3;
  private static final int PROPERTY_focusTraversable = 4;
  private static final int PROPERTY_displayable = 5;
  private static final int PROPERTY_topLevelAncestor = 6;
  private static final int PROPERTY_showing = 7;
  private static final int PROPERTY_allowActions = 8;
  private static final int PROPERTY_paintingTile = 9;
  private static final int PROPERTY_opaque = 10;
  private static final int PROPERTY_y = 11;
  private static final int PROPERTY_inputVerifier = 12;
  private static final int PROPERTY_x = 13;
  private static final int PROPERTY_verifyInputWhenFocusTarget = 14;
  private static final int PROPERTY_graphics = 15;
  private static final int PROPERTY_toolTipText = 16;
  private static final int PROPERTY_UIClassID = 17;
  private static final int PROPERTY_bounds = 18;
  private static final int PROPERTY_selectionModel = 19;
  private static final int PROPERTY_border = 20;
  private static final int PROPERTY_background = 21;
  private static final int PROPERTY_requestFocusEnabled = 22;
  private static final int PROPERTY_width = 23;
  private static final int PROPERTY_model = 24;
  private static final int PROPERTY_class = 25;
  private static final int PROPERTY_components = 26;
  private static final int PROPERTY_selectedObject = 27;
  private static final int PROPERTY_maximumSize = 28;
  private static final int PROPERTY_validateRoot = 29;
  private static final int PROPERTY_focusCycleRoot = 30;
  private static final int PROPERTY_height = 31;
  private static final int PROPERTY_toolkit = 32;
  private static final int PROPERTY_inputContext = 33;
  private static final int PROPERTY_name = 34;
  private static final int PROPERTY_root = 35;
  private static final int PROPERTY_visibleRect = 36;
  private static final int PROPERTY_rowSelectionAllowed = 37;
  private static final int PROPERTY_enabled = 38;
  private static final int PROPERTY_actionContext = 39;
  private static final int PROPERTY_debugGraphicsOptions = 40;
  private static final int PROPERTY_inputMethodRequests = 41;
  private static final int PROPERTY_colorModel = 42;
  private static final int PROPERTY_foreground = 43;
  private static final int PROPERTY_visible = 44;
  private static final int PROPERTY_font = 45;
  private static final int PROPERTY_optimizedDrawingEnabled = 46;
  private static final int PROPERTY_actionMap = 47;
  private static final int PROPERTY_parent = 48;
  private static final int PROPERTY_preferredSize = 49;
  private static final int PROPERTY_doubleBuffered = 50;
  private static final int PROPERTY_lightweight = 51;
  private static final int PROPERTY_valid = 52;
  private static final int PROPERTY_minimumSizeSet = 53;
  private static final int PROPERTY_columnModel = 54;
  private static final int PROPERTY_peer = 55;
  private static final int PROPERTY_autoscrolls = 56;
  private static final int PROPERTY_minimumSize = 57;
  private static final int PROPERTY_registeredKeyStrokes = 58;
  private static final int PROPERTY_graphicsConfiguration = 59;
  private static final int PROPERTY_layout = 60;
  private static final int PROPERTY_nextFocusableComponent = 61;
  private static final int PROPERTY_maximumSizeSet = 62;
  private static final int PROPERTY_locationOnScreen = 63;
  private static final int PROPERTY_preferredSizeSet = 64;
  private static final int PROPERTY_cursor = 65;
  private static final int PROPERTY_managingFocus = 66;
  private static final int PROPERTY_insets = 67;
  private static final int PROPERTY_componentOrientation = 68;
  private static final int PROPERTY_componentCount = 69;
  private static final int PROPERTY_rootPane = 70;
  private static final int PROPERTY_accessibleContext = 71;
  private static final int PROPERTY_selectedObjects = 72;
  private static final int PROPERTY_dropTarget = 73;
  private static final int PROPERTY_confirm = 74;
  private static final int PROPERTY_component = 75;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[76];

  static {
    try {
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCDetailsPane.class, "getLocale", "setLocale" );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCDetailsPane.class, "getAlignmentY", "setAlignmentY" );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCDetailsPane.class, "getAlignmentX", "setAlignmentX" );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCDetailsPane.class, "getTreeLock", null );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCDetailsPane.class, "isFocusTraversable", null );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCDetailsPane.class, "isDisplayable", null );
      properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", QCDetailsPane.class, "getTopLevelAncestor", null );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCDetailsPane.class, "isShowing", null );
      properties[PROPERTY_allowActions] = new PropertyDescriptor ( "allowActions", QCDetailsPane.class, "getAllowActions", "setAllowActions" );
      properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", QCDetailsPane.class, "isPaintingTile", null );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCDetailsPane.class, "isOpaque", "setOpaque" );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCDetailsPane.class, "getY", null );
      properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", QCDetailsPane.class, "getInputVerifier", "setInputVerifier" );
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCDetailsPane.class, "getX", null );
      properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", QCDetailsPane.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCDetailsPane.class, "getGraphics", null );
      properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", QCDetailsPane.class, "getToolTipText", "setToolTipText" );
      properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", QCDetailsPane.class, "getUIClassID", null );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCDetailsPane.class, "getBounds", "setBounds" );
      properties[PROPERTY_selectionModel] = new PropertyDescriptor ( "selectionModel", QCDetailsPane.class, "getSelectionModel", "setSelectionModel" );
      properties[PROPERTY_border] = new PropertyDescriptor ( "border", QCDetailsPane.class, "getBorder", "setBorder" );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCDetailsPane.class, "getBackground", "setBackground" );
      properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", QCDetailsPane.class, "isRequestFocusEnabled", "setRequestFocusEnabled" );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCDetailsPane.class, "getWidth", null );
      properties[PROPERTY_model] = new PropertyDescriptor ( "model", QCDetailsPane.class, "getModel", null );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCDetailsPane.class, "getClass", null );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCDetailsPane.class, "getComponents", null );
      properties[PROPERTY_selectedObject] = new PropertyDescriptor ( "selectedObject", QCDetailsPane.class, "getSelectedObject", null );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCDetailsPane.class, "getMaximumSize", "setMaximumSize" );
      properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", QCDetailsPane.class, "isValidateRoot", null );
      properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", QCDetailsPane.class, "isFocusCycleRoot", null );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCDetailsPane.class, "getHeight", null );
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCDetailsPane.class, "getToolkit", null );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCDetailsPane.class, "getInputContext", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCDetailsPane.class, "getName", "setName" );
      properties[PROPERTY_root] = new PropertyDescriptor ( "root", QCDetailsPane.class, "getRoot", "setRoot" );
      properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", QCDetailsPane.class, "getVisibleRect", null );
      properties[PROPERTY_rowSelectionAllowed] = new PropertyDescriptor ( "rowSelectionAllowed", QCDetailsPane.class, "getRowSelectionAllowed", "setRowSelectionAllowed" );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCDetailsPane.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_actionContext] = new PropertyDescriptor ( "actionContext", QCDetailsPane.class, "getActionContext", null );
      properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", QCDetailsPane.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCDetailsPane.class, "getInputMethodRequests", null );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCDetailsPane.class, "getColorModel", null );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCDetailsPane.class, "getForeground", "setForeground" );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCDetailsPane.class, "isVisible", "setVisible" );
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCDetailsPane.class, "getFont", "setFont" );
      properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", QCDetailsPane.class, "isOptimizedDrawingEnabled", null );
      properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", QCDetailsPane.class, "getActionMap", "setActionMap" );
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCDetailsPane.class, "getParent", null );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCDetailsPane.class, "getPreferredSize", "setPreferredSize" );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCDetailsPane.class, "isDoubleBuffered", "setDoubleBuffered" );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCDetailsPane.class, "isLightweight", null );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCDetailsPane.class, "isValid", null );
      properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", QCDetailsPane.class, "isMinimumSizeSet", null );
      properties[PROPERTY_columnModel] = new PropertyDescriptor ( "columnModel", QCDetailsPane.class, "getColumnModel", null );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCDetailsPane.class, "getPeer", null );
      properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", QCDetailsPane.class, "getAutoscrolls", "setAutoscrolls" );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCDetailsPane.class, "getMinimumSize", "setMinimumSize" );
      properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", QCDetailsPane.class, "getRegisteredKeyStrokes", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCDetailsPane.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCDetailsPane.class, "getLayout", "setLayout" );
      properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", QCDetailsPane.class, "getNextFocusableComponent", "setNextFocusableComponent" );
      properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", QCDetailsPane.class, "isMaximumSizeSet", null );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCDetailsPane.class, "getLocationOnScreen", null );
      properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", QCDetailsPane.class, "isPreferredSizeSet", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCDetailsPane.class, "getCursor", "setCursor" );
      properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", QCDetailsPane.class, "isManagingFocus", null );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCDetailsPane.class, "getInsets", null );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCDetailsPane.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCDetailsPane.class, "getComponentCount", null );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCDetailsPane.class, "getRootPane", null );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCDetailsPane.class, "getAccessibleContext", null );
      properties[PROPERTY_selectedObjects] = new PropertyDescriptor ( "selectedObjects", QCDetailsPane.class, "getSelectedObjects", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCDetailsPane.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_confirm] = new PropertyDescriptor ( "confirm", QCDetailsPane.class, "getConfirm", "setConfirm" );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCDetailsPane.class, null, null, "getComponent", null );
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
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCDetailsPane.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCDetailsPane.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( QCDetailsPane.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[0], "addAncestorListener", "removeAncestorListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCDetailsPane.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCDetailsPane.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCDetailsPane.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCDetailsPane.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_listSelectionListener] = new EventSetDescriptor ( QCDetailsPane.class, "listSelectionListener", javax.swing.event.ListSelectionListener.class, new String[0], "addListSelectionListener", "removeListSelectionListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCDetailsPane.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCDetailsPane.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCDetailsPane.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCDetailsPane.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
      eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( QCDetailsPane.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[0], "addVetoableChangeListener", "removeVetoableChangeListener" );
      eventSets[EVENT_errorListener] = new EventSetDescriptor ( QCDetailsPane.class, "errorListener", com.ibm.as400.vaccess.ErrorListener.class, new String[0], "addErrorListener", "removeErrorListener" );
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
