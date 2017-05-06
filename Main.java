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
        Node firstNode, secondNode;
        Relationship relationship, externalRelationship;

        String DB_PATH = "BGNeo4j/databases/attempt1/";
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DB_PATH) );
        registerShutdownHook( graphDb );
        long temp;
        //Try a transaction
        try ( Transaction tx = graphDb.beginTx() )
        {
            // Database operations go here
            firstNode = graphDb.createNode();
            temp = firstNode.getId();
            firstNode.setProperty( "message", "Hello, " );
            secondNode = graphDb.createNode();
            secondNode.setProperty( "message", "World!" );

            relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
            relationship.setProperty( "message", "brave Neo4j " );
            externalRelationship = firstNode.createRelationshipTo(19, RelTypes.KNOWS, (byte) 1);
            secondNode = externalRelationship.getOtherNode(firstNode);
            System.out.println(secondNode.getClass());
            System.out.println(((ExternalNodeProxy) secondNode).getMachineId());
            System.out.println(((ExternalNodeProxy) secondNode).getId());
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx())
        {
            firstNode = graphDb.getNodeById(temp, false, (byte) 0);
            System.out.println(firstNode.getId());
            final Node testNode = firstNode;
            firstNode.getRelationships().forEach(relationship1 -> {

                Node node = (ExternalNodeProxy)relationship1.getOtherNode(testNode);
            });
        }

        /*
        // Only in machine01 in order to start the request
        InetAddress localIp = null, targetIp = null;
        System.out.print("Getting Localhost...");
        try
        {
            localIp = InetAddress.getLocalHost();
            targetIp = InetAddress.getByName("172.22.150.74");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("complete.");
        // Check if we are machine 1
        System.out.print("Checking if machine 1...");
        if(Arrays.equals(localIp.getAddress(), targetIp.getAddress()))
        {
            //Start Accumulator for testing
            System.out.print("on machine 1...starting accumulator");
            Accumulator acc = new Accumulator();
        }
        System.out.println("complete.");

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
        */
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