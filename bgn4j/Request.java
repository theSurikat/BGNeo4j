package bgn4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;

/**
 * Created by granteverett on 5/3/17.
 */
public class Request
{
    private String hostname;
    private int port;

    public Request(String hostname, int port)
    {
        this.hostname = hostname;
        this.port = port;
    }

    public void SendString(String data) {
        try {
            //Connect to the server
            System.out.println("Connecting to " + hostname + " on port " + port);
            Socket requester =  new Socket(hostname, port);

            //Set output stream
            System.out.println("Just connected to " + requester.getRemoteSocketAddress());
            DataOutputStream out = new DataOutputStream(requester.getOutputStream());

            //Send First Data String
            out.writeByte(1);
            out.writeUTF(data);
            out.flush();

            //Send the exit message
            out.writeByte(-1);
            out.flush();

            out.close();
            requester.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
