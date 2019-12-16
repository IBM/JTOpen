package com.SoftWoehr.JTOpenContrib.QCDemo;

import java.beans.*;

public class QCExplorerPaneBeanInfo extends SimpleBeanInfo {
            
  // Property identifiers //GEN-FIRST:Properties
  private static final int PROPERTY_parent = 0;
  private static final int PROPERTY_focusCycleRoot = 1;
  private static final int PROPERTY_class = 2;
  private static final int PROPERTY_validateRoot = 3;
  private static final int PROPERTY_visible = 4;
  private static final int PROPERTY_actionMap = 5;
  private static final int PROPERTY_toolTipText = 6;
  private static final int PROPERTY_focusTraversable = 7;
  private static final int PROPERTY_inputMethodRequests = 8;
  private static final int PROPERTY_insets = 9;
  private static final int PROPERTY_requestFocusEnabled = 10;
  private static final int PROPERTY_actionContext = 11;
  private static final int PROPERTY_visibleRect = 12;
  private static final int PROPERTY_minimumSize = 13;
  private static final int PROPERTY_locale = 14;
  private static final int PROPERTY_paintingTile = 15;
  private static final int PROPERTY_showing = 16;
  private static final int PROPERTY_componentOrientation = 17;
  private static final int PROPERTY_inputVerifier = 18;
  private static final int PROPERTY_y = 19;
  private static final int PROPERTY_root = 20;
  private static final int PROPERTY_x = 21;
  private static final int PROPERTY_treeModel = 22;
  private static final int PROPERTY_selectedObject = 23;
  private static final int PROPERTY_dropTarget = 24;
  private static final int PROPERTY_layout = 25;
  private static final int PROPERTY_toolkit = 26;
  private static final int PROPERTY_components = 27;
  private static final int PROPERTY_background = 28;
  private static final int PROPERTY_foreground = 29;
  private static final int PROPERTY_allowActions = 30;
  private static final int PROPERTY_valid = 31;
  private static final int PROPERTY_maximumSizeSet = 32;
  private static final int PROPERTY_inputContext = 33;
  private static final int PROPERTY_detailsColumnModel = 34;
  private static final int PROPERTY_bounds = 35;
  private static final int PROPERTY_rootPane = 36;
  private static final int PROPERTY_accessibleContext = 37;
  private static final int PROPERTY_name = 38;
  private static final int PROPERTY_graphics = 39;
  private static final int PROPERTY_treeSelectionModel = 40;
  private static final int PROPERTY_font = 41;
  private static final int PROPERTY_displayable = 42;
  private static final int PROPERTY_preferredSize = 43;
  private static final int PROPERTY_doubleBuffered = 44;
  private static final int PROPERTY_lightweight = 45;
  private static final int PROPERTY_enabled = 46;
  private static final int PROPERTY_UIClassID = 47;
  private static final int PROPERTY_border = 48;
  private static final int PROPERTY_minimumSizeSet = 49;
  private static final int PROPERTY_autoscrolls = 50;
  private static final int PROPERTY_colorModel = 51;
  private static final int PROPERTY_confirm = 52;
  private static final int PROPERTY_locationOnScreen = 53;
  private static final int PROPERTY_debugGraphicsOptions = 54;
  private static final int PROPERTY_height = 55;
  private static final int PROPERTY_detailsRoot = 56;
  private static final int PROPERTY_cursor = 57;
  private static final int PROPERTY_width = 58;
  private static final int PROPERTY_graphicsConfiguration = 59;
  private static final int PROPERTY_opaque = 60;
  private static final int PROPERTY_detailsSelectionModel = 61;
  private static final int PROPERTY_selectedObjects = 62;
  private static final int PROPERTY_registeredKeyStrokes = 63;
  private static final int PROPERTY_componentCount = 64;
  private static final int PROPERTY_maximumSize = 65;
  private static final int PROPERTY_treeLock = 66;
  private static final int PROPERTY_nextFocusableComponent = 67;
  private static final int PROPERTY_verifyInputWhenFocusTarget = 68;
  private static final int PROPERTY_managingFocus = 69;
  private static final int PROPERTY_peer = 70;
  private static final int PROPERTY_optimizedDrawingEnabled = 71;
  private static final int PROPERTY_topLevelAncestor = 72;
  private static final int PROPERTY_preferredSizeSet = 73;
  private static final int PROPERTY_alignmentY = 74;
  private static final int PROPERTY_alignmentX = 75;
  private static final int PROPERTY_detailsModel = 76;
  private static final int PROPERTY_component = 77;

  // Property array 
  private static PropertyDescriptor[] properties = new PropertyDescriptor[78];

  static {
    try {
      properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", QCExplorerPane.class, "getParent", null );
      properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", QCExplorerPane.class, "isFocusCycleRoot", null );
      properties[PROPERTY_class] = new PropertyDescriptor ( "class", QCExplorerPane.class, "getClass", null );
      properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", QCExplorerPane.class, "isValidateRoot", null );
      properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", QCExplorerPane.class, "isVisible", "setVisible" );
      properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", QCExplorerPane.class, "getActionMap", "setActionMap" );
      properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", QCExplorerPane.class, "getToolTipText", "setToolTipText" );
      properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", QCExplorerPane.class, "isFocusTraversable", null );
      properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", QCExplorerPane.class, "getInputMethodRequests", null );
      properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", QCExplorerPane.class, "getInsets", null );
      properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", QCExplorerPane.class, "isRequestFocusEnabled", "setRequestFocusEnabled" );
      properties[PROPERTY_actionContext] = new PropertyDescriptor ( "actionContext", QCExplorerPane.class, "getActionContext", null );
      properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", QCExplorerPane.class, "getVisibleRect", null );
      properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", QCExplorerPane.class, "getMinimumSize", "setMinimumSize" );
      properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", QCExplorerPane.class, "getLocale", "setLocale" );
      properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", QCExplorerPane.class, "isPaintingTile", null );
      properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", QCExplorerPane.class, "isShowing", null );
      properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", QCExplorerPane.class, "getComponentOrientation", "setComponentOrientation" );
      properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", QCExplorerPane.class, "getInputVerifier", "setInputVerifier" );
      properties[PROPERTY_y] = new PropertyDescriptor ( "y", QCExplorerPane.class, "getY", null );
      properties[PROPERTY_root] = new PropertyDescriptor ( "root", QCExplorerPane.class, "getRoot", "setRoot" );
      properties[PROPERTY_x] = new PropertyDescriptor ( "x", QCExplorerPane.class, "getX", null );
      properties[PROPERTY_treeModel] = new PropertyDescriptor ( "treeModel", QCExplorerPane.class, "getTreeModel", null );
      properties[PROPERTY_selectedObject] = new PropertyDescriptor ( "selectedObject", QCExplorerPane.class, "getSelectedObject", null );
      properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", QCExplorerPane.class, "getDropTarget", "setDropTarget" );
      properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", QCExplorerPane.class, "getLayout", "setLayout" );
      properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", QCExplorerPane.class, "getToolkit", null );
      properties[PROPERTY_components] = new PropertyDescriptor ( "components", QCExplorerPane.class, "getComponents", null );
      properties[PROPERTY_background] = new PropertyDescriptor ( "background", QCExplorerPane.class, "getBackground", "setBackground" );
      properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", QCExplorerPane.class, "getForeground", "setForeground" );
      properties[PROPERTY_allowActions] = new PropertyDescriptor ( "allowActions", QCExplorerPane.class, "getAllowActions", "setAllowActions" );
      properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", QCExplorerPane.class, "isValid", null );
      properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", QCExplorerPane.class, "isMaximumSizeSet", null );
      properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", QCExplorerPane.class, "getInputContext", null );
      properties[PROPERTY_detailsColumnModel] = new PropertyDescriptor ( "detailsColumnModel", QCExplorerPane.class, "getDetailsColumnModel", null );
      properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", QCExplorerPane.class, "getBounds", "setBounds" );
      properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", QCExplorerPane.class, "getRootPane", null );
      properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", QCExplorerPane.class, "getAccessibleContext", null );
      properties[PROPERTY_name] = new PropertyDescriptor ( "name", QCExplorerPane.class, "getName", "setName" );
      properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", QCExplorerPane.class, "getGraphics", null );
      properties[PROPERTY_treeSelectionModel] = new PropertyDescriptor ( "treeSelectionModel", QCExplorerPane.class, "getTreeSelectionModel", "setTreeSelectionModel" );
      properties[PROPERTY_font] = new PropertyDescriptor ( "font", QCExplorerPane.class, "getFont", "setFont" );
      properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", QCExplorerPane.class, "isDisplayable", null );
      properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", QCExplorerPane.class, "getPreferredSize", "setPreferredSize" );
      properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", QCExplorerPane.class, "isDoubleBuffered", "setDoubleBuffered" );
      properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", QCExplorerPane.class, "isLightweight", null );
      properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", QCExplorerPane.class, "isEnabled", "setEnabled" );
      properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", QCExplorerPane.class, "getUIClassID", null );
      properties[PROPERTY_border] = new PropertyDescriptor ( "border", QCExplorerPane.class, "getBorder", "setBorder" );
      properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", QCExplorerPane.class, "isMinimumSizeSet", null );
      properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", QCExplorerPane.class, "getAutoscrolls", "setAutoscrolls" );
      properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", QCExplorerPane.class, "getColorModel", null );
      properties[PROPERTY_confirm] = new PropertyDescriptor ( "confirm", QCExplorerPane.class, "getConfirm", "setConfirm" );
      properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", QCExplorerPane.class, "getLocationOnScreen", null );
      properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", QCExplorerPane.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" );
      properties[PROPERTY_height] = new PropertyDescriptor ( "height", QCExplorerPane.class, "getHeight", null );
      properties[PROPERTY_detailsRoot] = new PropertyDescriptor ( "detailsRoot", QCExplorerPane.class, "getDetailsRoot", null );
      properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", QCExplorerPane.class, "getCursor", "setCursor" );
      properties[PROPERTY_width] = new PropertyDescriptor ( "width", QCExplorerPane.class, "getWidth", null );
      properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", QCExplorerPane.class, "getGraphicsConfiguration", null );
      properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", QCExplorerPane.class, "isOpaque", "setOpaque" );
      properties[PROPERTY_detailsSelectionModel] = new PropertyDescriptor ( "detailsSelectionModel", QCExplorerPane.class, "getDetailsSelectionModel", "setDetailsSelectionModel" );
      properties[PROPERTY_selectedObjects] = new PropertyDescriptor ( "selectedObjects", QCExplorerPane.class, "getSelectedObjects", null );
      properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", QCExplorerPane.class, "getRegisteredKeyStrokes", null );
      properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", QCExplorerPane.class, "getComponentCount", null );
      properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", QCExplorerPane.class, "getMaximumSize", "setMaximumSize" );
      properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", QCExplorerPane.class, "getTreeLock", null );
      properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", QCExplorerPane.class, "getNextFocusableComponent", "setNextFocusableComponent" );
      properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", QCExplorerPane.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" );
      properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", QCExplorerPane.class, "isManagingFocus", null );
      properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", QCExplorerPane.class, "getPeer", null );
      properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", QCExplorerPane.class, "isOptimizedDrawingEnabled", null );
      properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", QCExplorerPane.class, "getTopLevelAncestor", null );
      properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", QCExplorerPane.class, "isPreferredSizeSet", null );
      properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", QCExplorerPane.class, "getAlignmentY", "setAlignmentY" );
      properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", QCExplorerPane.class, "getAlignmentX", "setAlignmentX" );
      properties[PROPERTY_detailsModel] = new PropertyDescriptor ( "detailsModel", QCExplorerPane.class, "getDetailsModel", null );
      properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", QCExplorerPane.class, null, null, "getComponent", null );
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
  private static final int EVENT_focusListener = 5;
  private static final int EVENT_mouseListener = 6;
  private static final int EVENT_listSelectionListener = 7;
  private static final int EVENT_treeSelectionListener = 8;
  private static final int EVENT_propertyChangeListener = 9;
  private static final int EVENT_keyListener = 10;
  private static final int EVENT_hierarchyListener = 11;
  private static final int EVENT_containerListener = 12;
  private static final int EVENT_vetoableChangeListener = 13;
  private static final int EVENT_errorListener = 14;

  // EventSet array
  private static EventSetDescriptor[] eventSets = new EventSetDescriptor[15];

  static {
    try {
      eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( QCExplorerPane.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[0], "addMouseMotionListener", "removeMouseMotionListener" );
      eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( QCExplorerPane.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[0], "addAncestorListener", "removeAncestorListener" );
      eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( QCExplorerPane.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[0], "addInputMethodListener", "removeInputMethodListener" );
      eventSets[EVENT_componentListener] = new EventSetDescriptor ( QCExplorerPane.class, "componentListener", java.awt.event.ComponentListener.class, new String[0], "addComponentListener", "removeComponentListener" );
      eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( QCExplorerPane.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[0], "addHierarchyBoundsListener", "removeHierarchyBoundsListener" );
      eventSets[EVENT_focusListener] = new EventSetDescriptor ( QCExplorerPane.class, "focusListener", java.awt.event.FocusListener.class, new String[0], "addFocusListener", "removeFocusListener" );
      eventSets[EVENT_mouseListener] = new EventSetDescriptor ( QCExplorerPane.class, "mouseListener", java.awt.event.MouseListener.class, new String[0], "addMouseListener", "removeMouseListener" );
      eventSets[EVENT_listSelectionListener] = new EventSetDescriptor ( QCExplorerPane.class, "listSelectionListener", javax.swing.event.ListSelectionListener.class, new String[0], "addListSelectionListener", "removeListSelectionListener" );
      eventSets[EVENT_treeSelectionListener] = new EventSetDescriptor ( QCExplorerPane.class, "treeSelectionListener", javax.swing.event.TreeSelectionListener.class, new String[0], "addTreeSelectionListener", "removeTreeSelectionListener" );
      eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( QCExplorerPane.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
      eventSets[EVENT_keyListener] = new EventSetDescriptor ( QCExplorerPane.class, "keyListener", java.awt.event.KeyListener.class, new String[0], "addKeyListener", "removeKeyListener" );
      eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( QCExplorerPane.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[0], "addHierarchyListener", "removeHierarchyListener" );
      eventSets[EVENT_containerListener] = new EventSetDescriptor ( QCExplorerPane.class, "containerListener", java.awt.event.ContainerListener.class, new String[0], "addContainerListener", "removeContainerListener" );
      eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( QCExplorerPane.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[0], "addVetoableChangeListener", "removeVetoableChangeListener" );
      eventSets[EVENT_errorListener] = new EventSetDescriptor ( QCExplorerPane.class, "errorListener", com.ibm.as400.vaccess.ErrorListener.class, new String[0], "addErrorListener", "removeErrorListener" );
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
