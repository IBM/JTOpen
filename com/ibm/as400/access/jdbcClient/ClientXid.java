package com.ibm.as400.access.jdbcClient;

import javax.transaction.xa.Xid;

public class ClientXid implements Xid {
  protected int            formatId                   = 42;
  protected byte           gtrid[]                    = new byte[8];
  protected byte           bqual[]                    = new byte[28];

  private static Object lock = new Object(); 
  private static long gtridGenerator = 0xD1E3F4E7C9C40000L; 
  private static long bqualGenerator = System.currentTimeMillis(); 
  public static void initializeNew(ClientXid xid) {
      long thisGtrid; 
      long thisBqual; 
      synchronized (lock) {
        thisGtrid = gtridGenerator;
        thisBqual = bqualGenerator; 
        gtridGenerator ++; 
        bqualGenerator++; 
      }
      
      xid.gtrid[0] = (byte)(0xFF & (thisGtrid >> 56));
      xid.gtrid[1] = (byte)(0xFF & (thisGtrid >> 48));
      xid.gtrid[2] = (byte)(0xFF & (thisGtrid >> 40));
      xid.gtrid[3] = (byte)(0xFF & (thisGtrid >> 32));
      xid.gtrid[4] = (byte)(0xFF & (thisGtrid >> 24));
      xid.gtrid[5] = (byte)(0xFF & (thisGtrid >> 16));
      xid.gtrid[6] = (byte)(0xFF & (thisGtrid >>  8));
      xid.gtrid[7] = (byte)(0xFF & (thisGtrid >>  0));
      
      xid.bqual[0] = (byte)(0xFF & (thisBqual >> 56));
      xid.bqual[1] = (byte)(0xFF & (thisBqual >> 48));
      xid.bqual[2] = (byte)(0xFF & (thisBqual >> 40));
      xid.bqual[3] = (byte)(0xFF & (thisBqual >> 32));
      xid.bqual[4] = (byte)(0xFF & (thisBqual >> 24));
      xid.bqual[5] = (byte)(0xFF & (thisBqual >> 16));
      xid.bqual[6] = (byte)(0xFF & (thisBqual >>  8));
      xid.bqual[7] = (byte)(0xFF & (thisBqual >>  0));
      
      
      byte[] b = "JDBCCLIENT".getBytes(); // use default encoding
      System.arraycopy(b, 0, xid.bqual, 8, b.length); 

  }
  
  public ClientXid() {
      initializeNew(this); 
  }
  
  public int getFormatId() {
    return formatId; 
  }

  public byte[] getGlobalTransactionId() {
    return gtrid;
   
  }

  public byte[] getBranchQualifier() {
    return bqual;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < gtrid.length; i++) {
      int unsignedInt = 0xFF & gtrid[i];
      if (unsignedInt < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(unsignedInt));
    }
    sb.append('-');
    for (int i = 0; i < bqual.length; i++) {
      int unsignedInt = 0xFF & bqual[i];
      if (unsignedInt < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(unsignedInt));
    }

    return sb.toString();
  }
}
