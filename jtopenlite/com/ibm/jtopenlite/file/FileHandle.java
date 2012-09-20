///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  FileHandle.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.file; 

public final class FileHandle implements Comparable<FileHandle>
{
  public static final int OPEN_READ_ONLY = 1;
  public static final int OPEN_WRITE_ONLY = 2;
  public static final int OPEN_READ_WRITE = 3;

  public static final int SHARE_ALL = 0;
  public static final int SHARE_WRITE = 1;
  public static final int SHARE_READ = 2;
  public static final int SHARE_NONE = 3;

  private String name_;
  private int openType_;
  private int share_;
  private boolean open_;
  private int handle_;
  private long id_;
  private int dataCCSID_;
  private long created_;
  private long modified_;
  private long accessed_;
  private long size_;
  private int version_;

  private int lastStatus_;

  private long currentOffset_ = 0;

  private boolean symlink_;
  private boolean directory_;
  private String path_;

  private FileHandle()
  {
  }

  public static FileHandle createEmptyHandle()
  {
    return new FileHandle();
  }

  public boolean equals(Object obj)
  {
    if (obj != null && obj instanceof FileHandle)
    {
      FileHandle h = (FileHandle)obj;
      return h.handle_ == this.handle_ &&
             h.id_ == this.id_ &&
             h.size_ == this.size_ &&
             ((h.getPath() == null && this.getPath() == null) ||
              (h.getPath() != null && this.getPath() != null && h.getPath().equals(this.getPath())));
    }
    return false;
  }

  public int hashCode()
  {
    return getPath() == null ? super.hashCode() : getPath().hashCode();
  }

  public int compareTo(FileHandle h)
  {
    String p1 = this.getPath();
    String p2 = h.getPath();
    if (p1 == null && p2 == null) return 0;
    if (p1 == null) return -1;
    if (p2 == null) return 1;
    return p1.compareTo(p2);
  }

  void setSymlink(boolean b)
  {
    symlink_ = b;
  }

  public boolean isSymlink()
  {
    return symlink_;
  }

  void setDirectory(boolean b)
  {
    directory_ = b;
  }

  public boolean isDirectory()
  {
    return directory_;
  }

  void setPath(String path)
  {
    path_ = path;
  }

  public String getPath()
  {
    return path_ == null ? name_ : path_;
  }

  /**
   * Returns the current byte offset in the file where the next read or write operation will take place.
  **/
  public long getOffset()
  {
    return currentOffset_;
  }

  /**
   * Sets the byte offset in the file for the next read or write operation; the offset is automatically
   * incremented after normal read or write operations.
  **/
  public void setOffset(long offset)
  {
    currentOffset_ = offset;
  }

  void setShareOption(int share)
  {
    share_ = share;
  }

  public int getShareOption()
  {
    return share_;
  }

  void setOpenType(int type)
  {
    openType_ = type;
  }

  public int getOpenType()
  {
    return openType_;
  }

  void setOpen(boolean b)
  {
    open_ = b;
  }

  public boolean isOpen()
  {
    return open_;
  }

  void setName(String name)
  {
    name_ = name;
  }

  public String getName()
  {
    return name_;
  }

  void setHandle(int handle)
  {
    handle_ = handle;
  }

  int getHandle()
  {
    return handle_;
  }

  void setID(long id)
  {
    id_ = id;
  }

  long getID()
  {
    return id_;
  }

  void setDataCCSID(int ccsid)
  {
    dataCCSID_ = ccsid;
  }

  public int getDataCCSID()
  {
    return dataCCSID_;
  }

  void setCreateDate(long d)
  {
    created_ = d;
  }

  public long getTimestampCreated()
  {
    return created_;
  }

  void setModifyDate(long d)
  {
    modified_ = d;
  }

  public long getTimestampModified()
  {
    return modified_;
  }

  void setAccessDate(long d)
  {
    accessed_ = d;
  }

  public long getTimestampAccessed()
  {
    return accessed_;
  }

  void setSize(long s)
  {
    size_ = s;
  }

  public long getSize()
  {
    return size_;
  }

  void setVersion(int v)
  {
    version_ = v;
  }

  int getVersion()
  {
    return version_;
  }

  public int getLastStatus()
  {
    return lastStatus_;
  }

  void setLastStatus(int s)
  {
    lastStatus_ = s;
  }

  public String toString()
  {
    String path = getPath();
    return path == null ? super.toString() : path;
  }
}
