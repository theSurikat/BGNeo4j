package bgn4j;

import java.io.*;
import java.net.*;
/**
 * Created by granteverett on 5/3/17.
 */
public class Response extends Thread
{
    private ServerSocket response;

    public Response(int port) throws IOException
    {
        this.response = new ServerSocket(port);
        this.response.setSoTimeout(10000);
    }

    public void waitForString() {
        while(true)
        {
            try
            {
                System.out.println("Waiting for request on port " + response.getLocalPort());
                Socket server = response.accept();

                DataInputStream in = new DataInputStream(server.getInputStream());

                boolean done = false;
                while(!done) {
                    byte messageType = in.readByte();

                    switch(messageType)
                    {
                        case 1:
                            System.out.println("Message Received: " + in.readUTF());
                            break;
                        default:
                            done = true;
                    }
                }

                in.close();
                server.close();
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

    public void main() {
        this.waitForString();
    }
}
