package com.iKeirNez.packetapi.connections;

import com.iKeirNez.packetapi.HookType;
import com.iKeirNez.packetapi.threads.IncomingThread;
import com.iKeirNez.packetapi.threads.OutgoingThread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by iKeirNez on 04/04/2014.
 */
public class ServerConnection extends Connection {

    private ServerSocket serverSocket;
    private InetAddress inetAddress;

    public long lastHeartbeat = System.currentTimeMillis();
    public boolean offline = false;

    protected ServerConnection(ConnectionManager connectionManager, String clientAddress, int port) throws IOException {
        super(connectionManager, clientAddress, port);
        this.inetAddress = clientAddress != null ? InetAddress.getByName(clientAddress) : null;
        serverSocket = new ServerSocket(port);

        new Thread(() -> {
            while (socket == null){
                try {
                    Socket tempSocket = serverSocket.accept();

                    if (inetAddress == null || tempSocket.getInetAddress().equals(inetAddress)){ // only allow connection if it comes from specified address
                        socket = tempSocket;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            incomingThread = new IncomingThread("[Server] Incoming: " + clientAddress + ":" + port, connectionManager, this);
            outgoingThread = new OutgoingThread("[Server] Outgoing: " + clientAddress + ":" + port, this);

            incomingThread.start();
            outgoingThread.start();

            connected = true;
            connectionManager.callHook(this, HookType.CONNECTED);
        }).start();
    }

    @Override
    public void close(){
        super.close();

        try {
            serverSocket.close();
        } catch (IOException e) {}
    }

}
