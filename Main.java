import com.sun.corba.se.impl.orbutil.graph.Graph;
import javafx.util.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import bgn4j.*;
import org.neo4j.kernel.impl.api.store.RelationshipIterator;
import org.neo4j.kernel.impl.core.ExternalNodeProxy;
import org.neo4j.kernel.impl.core.RelationshipProxy;

import javax.management.relation.Relation;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class Main {
    public static void main(String[] args)
    {
        String DB_PATH = "BGNeo4j/databases/attempt1/";
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DB_PATH) );
        registerShutdownHook( graphDb );

        //Which machine are we on?
        InetAddress localIp = null, machine1 = null, machine2 = null;
        byte machineId = 2;
        System.out.print("Getting Localhost and Machine1 and Machine2...");
        try
        {
            localIp = InetAddress.getLocalHost();
            machine1 = InetAddress.getByName("172.22.150.74");
            machine2 = InetAddress.getByName("172.22.150.75");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("complete.");
        // Check if we are machine 1 or 2
        System.out.print("Checking if machine 1...");
        if(Arrays.equals(localIp.getAddress(), machine1.getAddress()))
        {
            System.out.print("on machine 1...");
            machineId = 0;

        }
        else if(Arrays.equals(localIp.getAddress(), machine2.getAddress()))
        {
            System.out.print("on machine 2...setting machine ID");
            machineId = 1;
        }
        System.out.println("complete.");

        //Initialize the DB based on machine
        initDb(graphDb, machineId);

        if(machineId == 0)
        {
            //Start accumulator if we are machine 1
            System.out.print("Starting accumulator...");
            try ( Transaction tx = graphDb.beginTx() )
            {
                List<String> startOperations = new ArrayList<String>() {};
                startOperations.add("friends");
                Accumulator acc = new Accumulator(graphDb.getNodeById(1, false, (byte) 0), startOperations);
                acc.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("complete.");
        }

        System.out.print("Server starting...");
        try
        {
            Server processor = new Server(6067);
            processor.start();
            System.out.println("complete");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void initDb(GraphDatabaseService graphDb, byte machineId)
    {
        Node firstNode, externalNode;
        Relationship relationship, externalRelationship;
        long temp;

        //Try a transaction
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Database operations go here
            firstNode = graphDb.createNode();
            temp = firstNode.getId();
            firstNode.setProperty( "message", "Hello, " );
            //Make the external relationship
            //Make it to the machine that we are not
            //This is only for between machine 0 and 1
            externalRelationship = firstNode.createRelationshipTo(1, RelTypes.KNOWS,
                    (machineId == (byte) 0) ? (byte) 1 : (byte) 0);
            externalNode = externalRelationship.getOtherNode(firstNode);
            System.out.println("External node test print");
            System.out.println(externalNode.getClass());
            System.out.println(((ExternalNodeProxy) externalNode).getMachineId());
            System.out.println(((ExternalNodeProxy) externalNode).getId());
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx())
        {
            //Get the first node in our database
            firstNode = graphDb.getNodeById(temp, false, (byte) 0);
            System.out.print("Got first node: " + firstNode.getId());
            //Now we get the external Node
            final Node testNode = firstNode;
            firstNode.getRelationships().forEach(relationship1 -> {
                Node node = (ExternalNodeProxy)relationship1.getOtherNode(testNode);
                System.out.println("External node test print, read from second transaction");
                System.out.println(((ExternalNodeProxy) node).getMachineId());
            });
        }
    }

    private static enum RelTypes implements RelationshipType
    {
        KNOWS
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
}

/* RANDOM STUFF
        List<Pair<Long, List<String>>> requestData = new ArrayList<>();
        List<String> test = new ArrayList<>();
        requestData.add(new Pair<>((long) 1, test));
        requestData.add(new Pair<>((long) 2, test));
        requestData.add(new Pair<>((long) 3, test));
        requestData.add(new Pair<>((long) 4, test));
        requestData.add(new Pair<>((long) 5, test));

        //Try a transaction
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Database operations go here
            firstNode = graphDb.createNode();
            firstNode.setProperty( "message", "Hello, " );
            secondNode = graphDb.createNode();
            secondNode.setProperty( "message", "World!" );

            relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
            relationship.setProperty( "message", "brave Neo4j " );
            tx.success();
        }


        try ( Transaction tx = graphDb.beginTx() )
        {
            Iterable<Node> nodes = graphDb.getAllNodes();
            for (Node node : nodes)
            {
                firstNode = node;
                break;
            }
            List<Pair<Node, List<String>>> output = acc.setStartNode(firstNode).addOperation("friends").run();
            List<Pair<Long, List<String>>> requestData = new ArrayList<Pair<Long, List<String>>>() {};

            for(Pair<Node, List<String>> cur : output)
            {
                Node temp = cur.getKey();
                Pair<Long, List<String>> newPair =  new Pair<>(temp.getId(), cur.getValue());
                requestData.add(newPair);
            }

            try {

            } catch(IOException e) {
                e.printStackTrace();
            }
            //Iterable<Relationship> rels = graphDb.getAllRelationships();
            //for (Relationship rel : rels) {
            //    System.out.println(rel.getProperty( "message" ));
            //}
            //System.out.print( firstNode.getProperty( "message" ) );
            //System.out.print( relationship.getProperty( "message" ) );
            //System.out.print( secondNode.getProperty( "message" ) );
        }
            private static enum RelTypes implements RelationshipType
    {
        KNOWS
    }
        */