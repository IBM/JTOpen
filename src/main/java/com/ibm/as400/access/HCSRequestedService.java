package com.ibm.as400.access;

public class HCSRequestedService
{
  private int requestedService;
  private byte[] connReqID;
  private SocketContainer HSConnSockets;
  
  public HCSRequestedService(int reqService)
  {
    this.requestedService = reqService;
    this.connReqID        = new byte[64];
  }
  
  public void setConnReqID(byte[] newConnReqID) {
    this.connReqID = newConnReqID;
  }
  
  public void setHSConnectionSockets(SocketContainer newHSConnSockets) {
    this.HSConnSockets = newHSConnSockets;
  }
  
  public int getRequestedServiceID() {
    return this.requestedService;
  }
  
  public byte[] getConnReqID() {
    return this.connReqID;
  }
  
  public SocketContainer getHSConnectionSockets() {
    return HSConnSockets;
  }
}