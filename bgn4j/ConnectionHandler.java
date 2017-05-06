package bgn4j;
import org.neo4j.graphdb.*;
import javafx.util.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.core.ExternalNodeProxy;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by granteverett on 5/4/17.
 */
public class ConnectionHandler extends Thread
{
    protected final Socket socket;

    public ConnectionHandler(Socket socket)
    {
        this.socket = socket;
    }

    protected void sendList(List<Pair<Long, List<String>>> dataOut) throws IOException
    {
        //Get the streams
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        //Now we have streams, send the data
        //Send the data
        out.writeObject(dataOut);
        out.flush();
    }

    protected List<Pair<Long, List<String>>> receiveList() throws IOException, ClassNotFoundException
    {
        List<Pair<Long, List<String>>> nodeIds = new ArrayList<>();
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        nodeIds = (List<Pair<Long, List<String>>>) in.readObject();
        return nodeIds;
    }

    private void runConnectionHandler() throws IOException, ClassNotFoundException
    {
        List<Pair<Long, List<String>>> newQuery = this.receiveList();
        List<Pair<Long, List<String>>> queryAnswerIds;
        List<Pair<Node, List<String>>> queryAnswer;

        // TODO Do the things with the accumulator
        String DB_PATH = "BGNeo4j/databases/attempt1/";
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DB_PATH) );
        registerShutdownHook( graphDb );

        Accumulator acc = new Accumulator(graphDb, newQuery);
        try ( Transaction tx = graphDb.beginTx() )
        {
            queryAnswer = acc.run();
        }
        Stream<Pair<Node, List<String>>> externalNodeSetStream = queryAnswer.parallelStream();
        queryAnswerIds = externalNodeSetStream
                .map(pair -> new Pair<Long, List<String>>(pair.getKey().getId(), pair.getValue()))
                .collect(Collectors.toList());

        graphDb.shutdown();

        this.sendList(queryAnswerIds);
        socket.close();
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    @Override
    public void run()
    {
        try
        {
            runConnectionHandler();
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
}
