package com.iKeirNez.networking.threads;

import com.iKeirNez.networking.connections.Connection;
import com.iKeirNez.networking.packets.Packet;
import lombok.Getter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by iKeirNez on 04/04/2014.
 */
public class OutgoingThread extends Thread implements Runnable {

    @Getter private boolean running = true, closed = false;
    private Connection connection;
    private BlockingQueue<Packet> packetQueue = new ArrayBlockingQueue<>(1024);

    public OutgoingThread(String title, Connection connection){
        super(title);
        this.connection = connection;
    }

    @Override
    public void run(){
        closed = false;

        try {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(connection.getSocket().getOutputStream()))) {
                try {
                    while (running){
                        Packet packet = packetQueue.take();
                        objectOutputStream.writeObject(packet);
                        objectOutputStream.flush();
                    }
                } catch (InterruptedException e) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        closed = true;
    }

    public void addToQueue(Packet packet){
        packetQueue.add(packet);
    }

    public void awaitTermination(){
        running = false;

        if (packetQueue.size() == 0){ // stops this running forever waiting on a packet
            interrupt();
        }
    }

}
