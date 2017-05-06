package bgn4j;

import java.io.IOException;
import java.net.Socket;

import javafx.util.Pair;

import java.util.List;

public class Client extends ConnectionHandler
{
    // inputs
    List<Pair<Long, List<String>>> query;
    // output
    List<Pair<Long, List<String>>> result;

    public Client(String hostname, int port, List<Pair<Long, List<String>>> query) throws IOException
    {
        super(new Socket(hostname, port));
        this.query = query;
    }

    public void runClient() throws IOException, ClassNotFoundException
    {
        //Connect to the server
        this.sendList(query);
        result = this.receiveList();
        System.out.println("Client Result");
        for (Pair<Long, List<String>> cur : result) {
            System.out.println(cur.getKey());
        }
        socket.close();
    }

    @Override
    public void run()
    {
        try
        {
            runClient();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public List<Pair<Long, List<String>>> getResult()
    {
        return result;
    }
}
