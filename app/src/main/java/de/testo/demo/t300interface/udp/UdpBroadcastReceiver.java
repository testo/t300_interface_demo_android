package de.testo.demo.t300interface.udp;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Work in the background
 * UDP-Broadcast-Receiver
 */
abstract public class UdpBroadcastReceiver {
    private String TAG = "UdpBroadcastReceiver";

    private Thread UDPBroadcastThread = new Thread(new Runnable() {
        public void run() {
            try {
                // 0.0.0.0 listens to all addresses, you could also listen to a specific broadcast address if needed
                InetAddress broadcastIP = InetAddress.getByName("0.0.0.0");
                // 53955 is the default port for the testo udp broadcast
                Integer port = 53955;
                while (mShouldListenForBroadcast) {
                    System.out.println("Thread - listenForBroadcast? " + Thread.currentThread().getId() + mShouldListenForBroadcast);
                    listenForBroadcast(broadcastIP, port);
                }

            } catch (Exception e) {
                Log.i(TAG, "Stopped listening for UDP packets");
                Log.i(TAG, e.getMessage());
                e.printStackTrace();

            }
        }
    });

    private DatagramSocket mSocket;
    private volatile boolean mShouldListenForBroadcast = true;
    private boolean mShouldRestartSocketListen = true;

    /**
     * startUdpListener() starts the thread that listens for UDP packets
     */
    public void startUdpListener() {

        if (!UDPBroadcastThread.isAlive())
        {
            mShouldListenForBroadcast = true;
            Log.d(TAG, "Starting to listen for UDP packets");
            UDPBroadcastThread.start();
        }
        else
        {
            mShouldListenForBroadcast = false;

            try {
                UDPBroadcastThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mShouldListenForBroadcast = true;
            Log.d(TAG, "Starting to listen for UDP packets");
            UDPBroadcastThread.start();
        }
    }

    /**
     *
     * @param broadcastIP is the IP address where the socket will try to listen to, use 0.0.0.0 to
     * listen on all devicesFoundByReceiver
     * @param port        Testo default port is 53955
     * @throws Exception
     */
    private void listenForBroadcast(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[15000];
        if (mSocket == null || mSocket.isClosed()) {
            mSocket = new DatagramSocket(port, broadcastIP);
            mSocket.setBroadcast(true);
        }
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.i(TAG, "Waiting for UDP broadcast");
        mSocket.receive(packet);

        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();

        Log.i(TAG, "Received UDP broadcast from " + senderIP + ", message: " + message);
        onBroadcastReceived(message, senderIP);

        mSocket.close();

        Log.d(TAG, "socket.isClosed():" + mSocket.isClosed());
    }

    abstract public void onBroadcastReceived(final String message, final String sender);

    /**
     * UDPListener stop listen for UDP
     */

    public void stopUdpListener(boolean waitForJoin) {
        mShouldListenForBroadcast = false;
        mShouldRestartSocketListen = false;
        if (UDPBroadcastThread.isAlive() && waitForJoin)
        {
            try {
                UDPBroadcastThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

