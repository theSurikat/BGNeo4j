import javafx.util.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import bgn4j.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Accumulator acc = new Accumulator();
        Request requester = new Request("172.22.150.74", 6067);
        try {
            Response processor = new Response(6067);
            processor.start();
            requester.SendString("This is a test string");
        } catch(IOException e) {
            e.printStackTrace();
        }

        /*
        String DB_PATH = "/Users/granteverett/Documents/databases/";
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File(DB_PATH) );
        registerShutdownHook( graphDb );
        /*
        Node firstNode;
        Node secondNode;
        Relationship relationship;
        System.out.println("Hello World!");


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
        */
        /*
        try ( Transaction tx = graphDb.beginTx() )
        {
            Iterable<Node> nodes = graphDb.getAllNodes();
            Node firstNode = null;
            for (Node node : nodes) {
                firstNode = node;
                break;
            }
            List<Pair<Node, List<String>>> output = acc.setStartNode(firstNode).addOperation("friends").run();
            for (Pair<Node, List<String>> cur : output) {
                System.out.println(cur.getKey().getProperty("message"));
            }
            //Iterable<Relationship> rels = graphDb.getAllRelationships();
            //for (Relationship rel : rels) {
            //    System.out.println(rel.getProperty( "message" ));
            //}
            //System.out.print( firstNode.getProperty( "message" ) );
            //System.out.print( relationship.getProperty( "message" ) );
            //System.out.print( secondNode.getProperty( "message" ) );
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
