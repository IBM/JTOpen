///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.Hashtable;
import com.ibm.as400.access.*;

/**
  * The IFSFileDialog class represents a
  * file dialog for the IFS file classes.  This dialog allows the user to
  * traverse the file system and select a file.  The text on the Cancel button
  * and the OK button can be set by the calling application.
  *
  * A filter list can be provided by the caller.
  *
  * This dialog is designed to emulate the Windows file dialog.
  *
  * Usage:
  * <pre>
  *      AS400 sys = new AS400("system1");
  *      IFSFileDialog fd = new IFSFileDialog(this, "File Open", sys);
  *      FileFilter[] filterList = {new FileFilter("All files (*.*)", "*.*"),
  *                                 new FileFilter("Executables (*.exe)", "*.exe"),
  *                                 new FileFilter("HTML files (*.html)", "*.html"),
  *                                 new FileFilter("HTML files (*.htm)", "*.htm"),
  *                                 new FileFilter("Images (*.gif)", "*.gif"),
  *                                 new FileFilter("Text files (*.txt)", "*.txt")};
  *      fd.setFileFilter(filterList, 2);
  *      if (fd.show() == IFSFileDialog.OK)
  *      {
  *         String s = fd.getFileName();        // get file name
  *         String p = fd.getPath();            // get path
  *         String a = fd.getAbsolutePath();    // get fully qualified file
  *      }
  * </pre>
  * @deprecated Use <tt>com.ibm.as400.access.IFSSystemView</tt> instead.
  **/
public class IFSFileDialog extends Dialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //{{DECLARE_CONTROLS
    private Label label1;
            IFSList directoryList;              // Private. @A3C
    private Label label2;
            IFSList fileList;                   // Private. @A3C
    private Label label3;
            TextField fileName;                 // Private. @A3C
    private Label label4;
            Choice fileType;                    // Private. @A3C
            Button open;                        // Private. @A3C
            Button cancel;                      // Private. @A3C
    private Label currentDirectory;
            TextField status;                   // Private. @A3C
    //}}

            AS400 sys_;                         // Private. @A3C
            int state_;                         // Private. @A3C
            Hashtable filters_;                 // Private. @A3C

    private Listeners listener_;

    // MRI.
    private static String READY_TEXT = ResourceLoader.getPrintText ("READY"); //@A6A
    private static String WORKING_TEXT = ResourceLoader.getText ("EVT_NAME_WORKING"); //@A6A

    /**
      * Dialog still active.
      **/
    public static final int ACTIVE = 0;
    /**
      * Dialog was dismissed with the Ok button.
      **/
    public static final int OK = 1;
    /**
      * Dialog was dismissed with the Cancel button.
      **/
    public static final int CANCEL = 2;

/**
Constructs an IFSFileDialog object.
@param parent The parent.
@param title The title of the dialog.
@param system The system.
**/
    public IFSFileDialog(Frame parent, String title, AS400 system)
    {
        super(parent, title, true);

        listener_ = new Listeners();

         //{{INIT_CONTROLS
//        setLayout(null);                               // @A2D
//        addNotify();                                   // @A2D


        //
        // Start of @A2C
        //

        // Create a GridBagLayout manager
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(8, 8, 0, 8);
        setLayout(layout);

        // Set the background color to light gray.
        setBackground(Color.lightGray);

        String text = ResourceLoader.getText ("IFS_DIRECTORY"); //@A6a
        label1 = new Label(text, Label.LEFT); // @A4C @A6c
        add(label1, layout, constraints, 0, 0, 3, 1);

        directoryList = new IFSList();
        constraints.ipadx = 40;
        constraints.ipady = 90;
        add(directoryList, layout, constraints, 0, 1, 3, 5);

        text = ResourceLoader.getText ("IFS_FILE"); //@A6a
        label2 = new Label(text, Label.LEFT); //@A6c
        constraints.ipadx = 0;
        constraints.ipady = 0;
        add(label2, layout, constraints, 3, 0, 3, 1);

        fileList = new IFSList();
        constraints.ipadx = 40;
        constraints.ipady = 90;
        add(fileList, layout, constraints, 3, 1, 3, 5);

        text = ResourceLoader.getText ("IFS_FILE_NAME"); //@A6a
        label3 = new Label(text);  //@A6c
        constraints.ipadx = 0;
        constraints.ipady = 0;
        add(label3, layout, constraints, 0, 7, 1, 1);

        fileName = new TextField(21);
        add(fileName, layout, constraints, 1, 7, 5, 1);

        text = ResourceLoader.getPrintText ("TYPE"); //@A6a
        label4 = new Label(text);  //@A6c
        add(label4, layout, constraints, 0, 8, 1, 1);

        fileType = new Choice();
        add(fileType, layout, constraints, 1, 8, 5, 1);

        text = ResourceLoader.getPrintText ("OPEN"); //@A6a
        open = new Button(text);  //@A6c
        constraints.ipadx = 20;
        add(open, layout, constraints, 6, 0, 3, 1);

        text = ResourceLoader.getText ("DLG_CANCEL"); //@A6a
        cancel = new Button(text);  //@A6c
        add(cancel, layout, constraints, 6, 1, 3, 1);

        currentDirectory = new Label("/");
        constraints.ipadx = 0;
        constraints.ipady = 0;
        add(currentDirectory, layout, constraints, 0, 6, 6, 1);

        status = new TextField(44);
        add(status, layout, constraints, 0, 9, 9, 1);


        // Arrange the components in the dialog.
        pack();

        //
        // End of @A2C

        //}}

        status.setEditable(false);

        sys_ = system;
        directoryList.setSystem(sys_);
        fileList.setSystem(sys_);
        directoryList.setSort(true);
        fileList.setSort(true);
        directoryList.setListType(IFSList.DIRECTORYONLY);
        fileList.setListType(IFSList.FILEONLY);

        filters_ = new Hashtable();
        text = ResourceLoader.getText ("IFS_ALL_FILES_FILTER"); //@A6a
        filters_.put(text, "*.*");  //@A6c
        fileType.addItem(text);  //@A6c

        open.addActionListener(listener_);
        cancel.addActionListener(listener_);
        directoryList.addActionListener(listener_);
        fileList.addActionListener(listener_);
        fileList.addItemListener(listener_);
        addWindowListener(listener_);
        fileType.addItemListener(listener_);

        label1.addKeyListener(listener_);
        directoryList.addKeyListener(listener_);
        label2.addKeyListener(listener_);
        fileList.addKeyListener(listener_);
        label3.addKeyListener(listener_);
        fileName.addKeyListener(listener_);
        label4.addKeyListener(listener_);
        fileType.addKeyListener(listener_);
        open.addKeyListener(listener_);
        cancel.addKeyListener(listener_);
        currentDirectory.addKeyListener(listener_);
        status.addKeyListener(listener_);
        addKeyListener(listener_);

        directoryList.addErrorListener(listener_);
        fileList.addErrorListener(listener_);

        status.setText(READY_TEXT);  //@A6c

        setResizable(false);

        state_ = ACTIVE;
    }

    /**
     This methods adds a user interface component to the specified
     GridBagLayout manager using the specified constraints.
     @param component the user interface component to add
     @param layout the GridBagLayout manager
     @param constraints the constraints for the component
     @param x the x coordinate of the leftmost cell of the component
     @param y the y coordinate of the topmost cell of the component
     @param width the horizontal measurement of the component in cells
     @param height the vertical measurement of the component in cells
     @return none
     **/
    protected void add(Component          component,
                       GridBagLayout      layout,
                       GridBagConstraints constraints,
                       int                x,
                       int                y,
                       int                width,
                       int                height)
    {
      constraints.gridx = x;
      constraints.gridy = y;
      constraints.gridwidth = width;
      constraints.gridheight = height;
      layout.setConstraints(component, constraints);
      add(component);
    }


    /**
      * Returns the absolute path for the file that was selected.
      *
      * @return The fully qualified path, including the file name.
      **/
    public String getAbsolutePath()
    {
        if (state_ != CANCEL)
        {
            return getDirectory() + "/" + getFileName();
        }
        else
        {
            return new String("");
        }
    }

    /**
      * Returns the text for the Cancel button.
      *
      * @return The text for the Cancel button.
      **/
    public String getCancelButtonText()
    {
        return cancel.getLabel();
    }

    /**
      * Returns the selected file name.
      *
      * @return The file name.
      **/
    public String getFileName()
    {
        if (state_ != CANCEL)
        {
            return fileName.getText();
        }
        else
        {
            return new String("");
        }
    }

    /**
      * Returns the filter that was selected.
      *
      * @return The file filter that was selected.
      **/
    public FileFilter getFileFilter()
    {
        String desc = fileType.getSelectedItem();
        String p = (String)filters_.get(desc);

        return new FileFilter(desc, p);
    }

    /**
      * Returns the text for the Ok button.
      *
      * @return The text for the Ok button.
      **/
    public String getOkButtonText()
    {
        return open.getLabel();
    }

    /**
      * Returns the path for the file selected.  The file name is not
      * part of the path.
      *
      * @return The path for the selected file.
      **/
    public String getDirectory()
    {
        if (state_ != CANCEL)
        {
            return directoryList.getPath();
        }
        else
        {
            return new String("");
        }
    }

    /**
      * Returns the system for this dialog.
      *
      * @return The object that represents the system.
      **/
    public AS400 getSystem()
    {
        return sys_;
    }

    /**
      * Sets the text for the Cancel button.
      *
      * @param buttonText The text to use for the Cancel button.
      **/
    public void setCancelButtonText(String buttonText)
    {
        cancel.setLabel(buttonText);
    }

    /**
      * Sets the file name field.
      *
      * @param filename The name of the file.
      **/
    public void setFileName(String filename)
    {
        fileName.setText(filename);
    }

    /**
      * Sets the filter list.
      *
      * @param filterList The list of filters to be listed in the choice control.
      * @param defaultFilter The index into the list that is to be used as the default (zero-based).
      **/
    public void setFileFilter(FileFilter[] filterList, int defaultFilter)
    {
        fileType.removeAll();
        filters_ = new Hashtable();
        for (int i=0; i<filterList.length; i++)
        {
            fileType.addItem(filterList[i].getDescription());
            filters_.put(filterList[i].getDescription(),
                         filterList[i].getPattern());
        }
        fileType.select(defaultFilter);

        fileList.setFilter(filterList[defaultFilter].getPattern());
    }

    /**
      * Sets the button text for the Ok button.
      *
      * @param buttonText The text to use for the Ok button.
      **/
    public void setOkButtonText(String buttonText)
    {
        open.setLabel(buttonText);
    }

    /**
      * Sets the path to be used.
      *
      * @param path The path to use.
      **/
    public void setDirectory(String path)
    {
        directoryList.setPath(path);
        fileList.setPath(path);
    }

    void selectDirList()                        // Private. @A3C
    {
        // directory list double clicked
        String path = directoryList.getPath();
        String dir = directoryList.getSelectedItem();

        // @A1A
        // Added code to make sure the selected directory entry is not
        // NULL before processing it. Also, disable the directory and
        // file lists when the contents of the selected directory are
        // being retrieved. This way, we will prevent the user from
        // interacting with the lists while we are getting the diretory
        // contents, thus dis-allowing any non-meaningful list selection
        // events from being generated.

        if (dir == null) {                              // @A1A
            return;                                     // @A1A
        }                                               // @A1A

        directoryList.setEnabled(false);                // @A1A
        fileList.setEnabled(false);                     // @A1A

        if (dir.compareTo(".") == 0)
        {
            status.setText(WORKING_TEXT);  //@A6c
            ///status.setText("Retrieving list of files...");  @A6d
            try
            {
                fileList.populateList();
                status.setText(READY_TEXT);  //@A6c
            }
            catch (Exception e)
            {
                status.setText(e.toString());
            }
            showCurrentDir(fileList.getPath());
        }
        else if (dir.compareTo("..") == 0)
        {
            String s;
            int i = path.lastIndexOf("/");

            if (i != 0)
            {
                s = path.substring(0, i);
            }
            else
            {
                s = "/";
            }

            try
            {
                directoryList.setPath(s);
                status.setText(WORKING_TEXT);  //@A6c
                ///status.setText("Retrieving list of directories..."); @A6d
                directoryList.populateList();
                fileList.setPath(s);
                ///status.setText("Retrieving list of files...");  @A6d
                fileList.populateList();
                status.setText(READY_TEXT);  //@A6c
                showCurrentDir(s);
            }
            catch (Exception e)
            {
                status.setText(e.toString());
            }

        }
        else
        {
            String newPath;

            if (path.compareTo("/") != 0)
            {
                newPath = path + "/" + dir;
            }
            else
            {
                newPath = path + dir;
            }

            try
            {
                directoryList.setPath(newPath);
                fileList.setPath(newPath);
                status.setText(WORKING_TEXT);  //@A6c
                ///status.setText("Retrieving list of directories...");  @A6d
                directoryList.populateList();
                ///status.setText("Retrieving list of files...");  @A6d
                fileList.populateList();
                status.setText(READY_TEXT);  //@A6c
                showCurrentDir(newPath);
            }
            catch (Exception e)
            {
                status.setText(e.toString());
            }
        }

        //@A1A
        // Re-enable the directory and file lists now since we've
        // successfully retrieved the contents of the selected directory.

        directoryList.setEnabled(true);                 // @A1A
        fileList.setEnabled(true);                      // @A1A
    }

    /**
      * Shows the dialog.
     **/
//    public void show()                                // @A1D
    public void setVisible()                            // @A1A
    {
      IFSList list = null;  // @A5a
        try
        {
          list = directoryList;
          list.populateList();

          list = fileList;
          list.populateList();

          list = directoryList;
          showCurrentDir(list.getPath());
        }
        catch (Exception e)
        {
          String pathAndFilter = null;   // @A5a
          if (list != null) {            // @A5a
            String text0 = ResourceLoader.getText ("PROP_NAME_PATH"); //@A6a
            String text1 = ResourceLoader.getText ("PROP_NAME_FILTER"); //@A6a
            pathAndFilter = "("+text0+"=" + list.getPath() + ", "+text1+"=" + list.getFilter()+") "; // @A5a @A6c
          }
          else {
            pathAndFilter = "";          // @A5a
          }
          status.setText(pathAndFilter + e.toString());  // @A5c
        }

     super.setVisible(true);                            // @A1A
//     super.show();                                    // @A1D
    }

/**
Shows the dialog and returns the current state.

@return The current state of the dialog.
**/
    public int showDialog()
    {
//        show();                                       // @A1D
        setVisible();                                   // @A1A
        return state_;
    }

    private void showCurrentDir(String path)
    {
        currentDirectory.setText("//" + sys_.getSystemName() + path);
    }

    class Listeners
    implements WindowListener, ActionListener, ItemListener, KeyListener,
               ErrorListener, Serializable
    {

        Listeners()
        {
            super();
        }

        /**
          * Handles events within the dialog.
          * Applications should not call this method.
          * It is an Action listener method to handle events within the dialog.
          *
          * @param e The event.
          **/
        public void actionPerformed(ActionEvent e)
        {
            Object source = e.getSource();

            if (source == cancel)
            {
                fileName.setText("");
                state_ = CANCEL;
                dispose();
            }

            if (source == open)
            {
                state_ = OK;
                dispose();
            }

            if (source == directoryList)
            {
                selectDirList();
            }

            if (source == fileList)
            {
                // file list double clicked
                String f = fileList.getSelectedItem();
                fileName.setText(f);
                state_ = OK;
                dispose();
            }
        }

        /**
            Displays errors that have occurred.
        **/
        public void errorOccurred(ErrorEvent e)
        {
            System.out.println(e.getException().toString());
            status.setText(e.getException().toString());
        }


        /**
          * Handles state changes.
          * Applications should not call this method.
          * It is an ItemListener method used by the dialog.
          *
          *
          * @param e The event.
          **/
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getSource() == fileList)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    String f = fileList.getSelectedItem();
                    fileName.setText(f);
                }
            }
            else if (e.getSource() == fileType)
            {
                String s = fileType.getSelectedItem();
                fileList.setFilter((String)filters_.get(s));
                status.setText(WORKING_TEXT);  //@A6c
                ///status.setText("Updating file list...");  @A6d
                try
                {
                    fileList.populateList();
                    status.setText(READY_TEXT);  //@A6c
                }
                catch (Exception ex)
                {
                    status.setText(ex.toString());
                }
            }
        }

        /**
          * Handles key pressed event.
          * Applications should not call this method.
          * It is a KeyListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void keyPressed(KeyEvent e)
        {
        }

        /**
          * Handles key released events.
          * Applications should not call this method.
          * It is a KeyListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void keyReleased(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ENTER)
            {
                if (e.getSource() == fileName)
                {
                    // check to see if we should treat it as a filter
                    String s = fileName.getText();
                    int i = s.lastIndexOf("/");
                    if (i == -1)
                    {
                        // not a path, so treat it like a filter
                        fileList.setFilter(fileName.getText());
                        status.setText(WORKING_TEXT);  //@A6c
                        ///status.setText("Updating file list...");  @A6d
                        try
                        {
                            fileList.populateList();
                            status.setText(READY_TEXT);  //@A6c
                        }
                        catch (Exception ex)
                        {
                            status.setText(ex.toString());
                        }
                    }
                    else
                    {
                        // path, so figure out if we have a file spec
                        IFSFile f = new IFSFile(sys_, s);
                        try
                        {
                            if (f.isDirectory())
                            {
                                fileList.setPath(s);
                                directoryList.setPath(s);
                                status.setText(WORKING_TEXT);  //@A6c
                                ///status.setText("Updating directory list..."); @A6d
                                directoryList.populateList();
                                ///status.setText("Updating file list...");  @A6d
                                fileList.populateList();
                                status.setText(READY_TEXT);  //@A6c
                                showCurrentDir(s);
                            }
                            else
                            {
                                // file, then we use the file name as filter
                                String newPath = s.substring(0, i);
                                directoryList.setPath(newPath);
                                fileList.setPath(newPath);

                                String newFilter = s.substring(i+1);
                                fileList.setFilter(newFilter);

                                status.setText(WORKING_TEXT);  //@A6c
                                ///status.setText("Updating directory list..."); @A6d
                                directoryList.populateList();
                                ///status.setText("Updating file list..."); @A6d
                                fileList.populateList();
                                status.setText(READY_TEXT);  //@A6c
                                showCurrentDir(newPath);
                            }
                        }
                        catch (Exception ex)
                        {
                            status.setText(ex.toString());
                            ex.printStackTrace();
                        }
                    }
                }
                else if (e.getSource() == directoryList)
                {
                    // enter pressed in directory list, act like double click
                    selectDirList();
                }
                else if (e.getSource() == cancel)
                {
                    fileName.setText("");
                    state_ = CANCEL;
                    dispose();
                }
                else
                {
                    // enter pressed in one of those controls that should
                    // act like the default button has been pressed
                    ///System.out.println("key event");             @A6d
                    String s = fileName.getText();
                    if ((s != null) && (s.length() > 0))
                    {
                        // we have a file name, so act like ok's hit
                        state_ = OK;
                        dispose();
                    }
                }
            }
        }

        /**
          * Handles a key typed event.
          * Applications should not call this method.
          * It is a KeyListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void keyTyped(KeyEvent e)
        {
        }

        /**
          * Handles a window activated event.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowActivated(WindowEvent e)
        {
        }

        /**
          * Handles window closed events.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowClosed(WindowEvent e)
        {
//            sys_.disconnectService(AS400.FILE);
        }

        /**
          * WindowListener method used by the dialog.  Applications should not
          * call this method.
          *
          * @param e The event.
          **/
        public void windowClosing(WindowEvent e)
        {
            fileName.setText("");
            state_ = CANCEL;
            dispose();
        }

        /**
          * Handles a window deactivated event.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowDeactivated(WindowEvent e)
        {
        }

        /**
          * Handles a windoe deiconified event.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowDeiconified(WindowEvent e)
        {
        }

        /**
          * Handles window iconified events.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowIconified(WindowEvent e)
        {
        }

        /**
          * Handles window opened event.
          * Applications should not call this method.
          * It is a WindowListener method used by the dialog.
          *
          * @param e The event.
          **/
        public void windowOpened(WindowEvent e)
        {
        }

        // @A6d  Deleted copyright() method.
    }
    // @A6d  Deleted copyright() method.
}

