package bgn4j;

import java.io.*;
import java.net.*;
import java.io.ObjectInputStream;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Server extends Thread
{
    private ServerSocket server;

    public Server(int port) throws IOException
    {
        this.server = new ServerSocket(port);
    }

    public void runServer()
    {
        while(true)
        {
            try
            {
                Socket socket = this.server.accept();
                System.out.println("Request from " + socket.getRemoteSocketAddress().toString());
                // start a new ConnectionHandler to start new thread
                ConnectionHandler handler = new ConnectionHandler(socket);
                handler.start();
            }
            catch(SocketTimeoutException s)
            {
                System.out.println("Timeout!");
                break;
            }
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public void run() {
        runServer();
    }
}
