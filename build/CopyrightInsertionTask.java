import java.io.*;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;


public class CopyrightInsertionTask extends MatchingTask
{
  private static final String copyrightStringPart1_ = "Copyright (C) ";
  private static final String copyrightStringPart2_ = "1997-2004";
  private static final String copyrightStringPart3_ = " International Business Machines Corporation and others.";
  private static final String copyrightString_ = copyrightStringPart1_ + copyrightStringPart2_ + copyrightStringPart3_;
  private static final int copyrightStringLength_ = copyrightString_.length();

  private boolean verbose_;

  private File destDir_;  // directory where the *.class files are located

  private int numFilesProcessed_ = 0; // count of class files processed
  private int numFilesStamped_ = 0; // count of class files stamped
  private int numFilesAlreadyStamped_ = 0; // count of class files already stamped

  public void setDestdir(File destDir)
  {
    destDir_ = destDir;
  }

  public void setVerbose(boolean verbose)
  {
    verbose_ = verbose;
  }


  // Executes the task.
  public void execute() throws BuildException
  {
    DirectoryScanner scanner = getDirectoryScanner(destDir_);
    String[] classFileNames = scanner.getIncludedFiles();

    for (int i=0; i<classFileNames.length; ++i)
    {
      File classFile = new File(destDir_, classFileNames[i]);

      if (verbose_) System.out.println("Processing "+classFileNames[i]);

      insertCopyrightString(classFile);

      numFilesProcessed_++;
    }

    System.out.println("Number of class files processed: " + numFilesProcessed_);
    System.out.println("Number of class files stamped: " + numFilesStamped_);
    System.out.println("Number of class files already stamped: " + numFilesAlreadyStamped_);
  }
  private void insertCopyrightString(File classFile) throws BuildException {
    try
    {
      long start = System.currentTimeMillis();
      DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(classFile)));
      byte[] prePoolBytes = new byte[8];
      dis.read(prePoolBytes);
      int constantPoolCount = dis.readUnsignedShort();
      Constant[] entries = new Constant[constantPoolCount];
      // There is no 0th entry.
      for (int i=1; i<constantPoolCount; ++i)
      {
        int tag = dis.readUnsignedByte();
        entries[i] = new Constant(tag, dis);
        entries[i].read();
        String name = entries[i].getName();
        if (name != null &&
            name.length() == copyrightStringLength_ &&
            name.startsWith(copyrightStringPart1_) && // tolerate different dates
            name.endsWith(copyrightStringPart3_))
        {
          dis.close();
          if (verbose_) System.out.println("Already exists. Found it at index "+i);
          numFilesAlreadyStamped_++;
          return;
        }
        if (tag == Constant.CONSTANT_Long_info ||
            tag == Constant.CONSTANT_Double_info)
        {
          // These take up 2 entries in the constant pool.
          //entries[++i] = new Constant(tag, dis);
          ++i;
        }
      }
      int bytesLeft = dis.available();
      byte[] postPoolBytes = new byte[bytesLeft];
      dis.read(postPoolBytes);
      dis.close();

      // Add the copyright to the constant pool at the end.
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(classFile)));
      dos.write(prePoolBytes, 0, prePoolBytes.length);
      dos.writeShort(constantPoolCount+1);
      for (int i=1; i<constantPoolCount; ++i)
      {
        entries[i].write(dos);
        int tag = entries[i].getType();
        if (tag == Constant.CONSTANT_Long_info ||
            tag == Constant.CONSTANT_Double_info)
        {
          ++i;
        }
      }
      dos.writeByte(Constant.CONSTANT_Utf8_info);
      dos.writeUTF(copyrightString_);
      dos.write(postPoolBytes, 0, postPoolBytes.length);
      dos.flush();
      dos.close();
      long end = System.currentTimeMillis();
      if (verbose_) System.out.println("Stamped. Time: "+(end-start)+" ms");
      numFilesStamped_++;
    }
    catch (Exception e) { throw new BuildException(e); }
  }




  final class Constant
  {
    public static final int CONSTANT_Class_info = 7;
    public static final int CONSTANT_Fieldref_info = 9;
    public static final int CONSTANT_Methodref_info = 10;
    public static final int CONSTANT_InterfaceMethodref_info = 11;
    public static final int CONSTANT_String_info = 8;
    public static final int CONSTANT_Integer_info = 3;
    public static final int CONSTANT_Float_info = 4;
    public static final int CONSTANT_Long_info = 5;
    public static final int CONSTANT_Double_info = 6;
    public static final int CONSTANT_NameAndType_info = 12;
    public static final int CONSTANT_Utf8_info = 1;

    private int type_ = -1;
    private DataInputStream dataIn_ = null;
    private DataOutputStream dataOut_ = null;

    private int data1_ = -1;
    private int data2_ = -1;

    private int[] bytes_ = null;
    private String name_ = null;

    public Constant(int type, DataInputStream dataIn)
    {
      type_ = type;
      dataIn_ = dataIn;
    }

    public void read()
      throws IOException
    {
      switch(type_)
      {
        case CONSTANT_Utf8_info:
          data1_ = read(2);
          bytes_ = new int[data1_];
          for (int i=0; i<data1_; ++i)
          {
            bytes_[i] = read(1);
          }
          parseName();
          break;
        case CONSTANT_Class_info:
        case CONSTANT_String_info:
          data1_ = read(2);
          break;
        case CONSTANT_Double_info:
        case CONSTANT_Long_info:
          // These take up 2 entries in the constant pool.
          // The 2nd one is unusable. This has been noted as
          // a bad design choice by the JVM makers.
          data1_ = read(4);
          data2_ = read(4);
          break;
        case CONSTANT_Float_info:
        case CONSTANT_Integer_info:
          data1_ = read(4);
          break;
        case CONSTANT_Fieldref_info:
        case CONSTANT_InterfaceMethodref_info:
        case CONSTANT_Methodref_info:
        case CONSTANT_NameAndType_info:
          data1_ = read(2);
          data2_ = read(2);
          break;
        default:
          throw new IllegalArgumentException("wrong type: "+type_);
      }
    }

    public void write(DataOutputStream dataOut)
      throws IOException
    {
      dataOut_ = dataOut;
      write(1, type_);

      switch(type_)
      {
        case CONSTANT_Utf8_info:
          write(2, data1_);
          for (int i=0; i<data1_; ++i)
          {
            write(1, bytes_[i]);
          }
          break;
        case CONSTANT_Class_info:
        case CONSTANT_String_info:
          write(2, data1_);
          break;
        case CONSTANT_Double_info:
        case CONSTANT_Long_info:
          write(4, data1_);
          write(4, data2_);
          break;
        case CONSTANT_Float_info:
        case CONSTANT_Integer_info:
          write(4, data1_);
          break;
        case CONSTANT_Fieldref_info:
        case CONSTANT_InterfaceMethodref_info:
        case CONSTANT_Methodref_info:
        case CONSTANT_NameAndType_info:
          write(2, data1_);
          write(2, data2_);
          break;
        default:
          throw new IllegalArgumentException("wrong type: "+type_);
      }
    }

    public int read(int size)
      throws IOException
    {
      switch(size)
      {
        case 1:
          return dataIn_.readUnsignedByte();
        case 2:
          return dataIn_.readUnsignedShort();
        case 4:
          return dataIn_.readInt();
        default:
          throw new IllegalArgumentException("number of bytes to read: "+size);
      }
    }

    public void write(int size, int data)
      throws IOException
    {
      switch(size)
      {
        case 1:
          dataOut_.writeByte(data);
          break;
        case 2:
          dataOut_.writeShort(data);
          break;
        case 4:
          dataOut_.writeInt(data);
          break;
        default:
          throw new IllegalArgumentException("number of bytes to write: "+size);
      }
    }


    public int getType()
    {
      return type_;
    }

    public String getName()
    {
      if (type_ == CONSTANT_Utf8_info && name_ == null)
      {
        parseName();
      }
      return name_;
    }

    private void parseName()
    {
      int length = data1_;
      int[] bytes = bytes_;
      if (bytes == null) return;

      StringBuffer name = new StringBuffer();
      int i=0;
      while (i<length)
      {
        int ch;
        byte a = (byte)bytes[i];
        validate(a);
        if (a > 0x7F) // not a single-byte char
        {
          i++;
          byte b = (byte)bytes[i];
          validate(b);
          if (b > 0xBF) // not a double-byte char
          {
            i++;
            byte c = (byte)bytes[i];
            validate(c);
            ch = ((a & 0xf) << 12) + ((b & 0x3f) << 6) + (c & 0x3f);
          }
          else // is a double-byte
          {
            ch = ((a & 0x1f) << 6) + (b & 0x3f);
          }
        }
        else // is a single-byte
        {
          ch = a;
        }
        name.append((char)ch);
        i++;
      }
      name_ = name.toString();
    }

    private /*static*/ void validate(byte x)
    {
      if (x==0x0 || x>=0xf0)
        throw new NullPointerException("Invalid byte: "+x);
    }
  }

}
