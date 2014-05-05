package com.ikeirnez.communicationsframework.implementation.standard.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.ikeirnez.communicationsframework.api.connection.Connection;
import com.ikeirnez.communicationsframework.api.packets.Packet;
import com.ikeirnez.communicationsframework.implementation.handlers.PacketHandler;
import com.ikeirnez.communicationsframework.implementation.IConnectionManager;

/**
 * Created by iKeirNez on 06/04/2014.
 */
public abstract class IConnection implements Connection {

  protected final IConnectionManager connectionManager;
  private final InetSocketAddress socketAddress;
  private final String hostName;
  private final int port;

  public PacketHandler packetHandler = null;
  public final Logger logger;
  public AtomicBoolean firstConnect = new AtomicBoolean(true);
  public final List<Packet> connectQueue = Collections.synchronizedList(new ArrayList<Packet>()); // packets to be sent when connection is (re)gained
  public AtomicBoolean closing = new AtomicBoolean(false), expectingDisconnect = new AtomicBoolean(false);

  public IConnection(IConnectionManager connectionManager, String hostName, int port) {
    this.connectionManager = connectionManager;
    this.hostName = hostName;
    this.port = port;
    socketAddress = new InetSocketAddress(hostName, port);

    connectionManager.connections.add(this);
    logger = Logger.getLogger("Connection (" + hostName + ":" + port + ")");
  }

  @Override
  public boolean isConnected( ) {
    return (packetHandler != null) && packetHandler.connected();
  }

  @Override
  public void sendPacket(Packet packet) {
    sendPacket(packet, true);
  }

  @Override
  public void sendPacket(Packet packet, boolean queueIfNotConnected) {
    if (!isConnected()) {
      if (queueIfNotConnected) {
        connectQueue.add(packet);
      }

      return;
    }

    packetHandler.send(packet);
  }

  @Override
  public void close( ) throws IOException {
    close(true);
  }

  public void close(boolean print) throws IOException {
    if (print) {
      logger.info("Closing...");
    }

    closing.set(true);
    connectionManager.connections.remove(this);

    if (packetHandler != null) {
      packetHandler.close();
    }
  }

  public IConnectionManager getConnectionManager( ) {
    return connectionManager;
  }

  @Override
  public InetSocketAddress getSocketAddress( ) {
    return socketAddress;
  }

  @Override
  public String getHostName( ) {
    return hostName;
  }

  @Override
  public int getPort( ) {
    return port;
  }
}