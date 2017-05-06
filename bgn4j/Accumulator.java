package bgn4j;

import javafx.util.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.impl.core.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Accumulator
{
    private InetAddress ips[];
    private List<Pair<Node, List<String>>> nodeSet;
    private Node startNode;
    private List<String> startOperations;

    public Accumulator() {
        this.startOperations = new ArrayList<>();
        this.setIps();
    }

    public Accumulator(Node startNode, List<String> startOperations)
    {
        this.startNode = startNode;
        this.startOperations = startOperations;
        this.setIps();
    }

    public Accumulator(GraphDatabaseService graphdb, List<Pair<Long, List<String>>> nodeIdSet)
    {
        Stream<Pair<Long, List<String>>> parallelStream = nodeIdSet.parallelStream();
        this.nodeSet = parallelStream.map(nodeId -> new Pair<Node, List<String>>(
                graphdb.getNodeById(nodeId.getKey(), false, (byte) 0), nodeId.getValue())).collect(Collectors.toList());
        /*for (Pair<Long, List<String>> nodeId : nodeIdSet)
        {
            this.nodeSet.add(new Pair<Node, List<String>>(
                    graphdb.getNodeById(nodeId.getKey(), false, (byte) 0), nodeId.getValue()));
        }*/
        this.setIps();
    }

    public Accumulator setNodeSet(List<Pair<Node, List<String>>> nodeSet)
    {
        this.nodeSet = nodeSet;
        return this;
    }

    public Accumulator setStartNode(Node startNode)
    {
        this.startNode = startNode;
        return this;
    }

    public Accumulator setStartOperations(List<String> startOperations)
    {
        this.startOperations = startOperations;
        return this;
    }

    public Accumulator addOperation(String operation)
    {
        this.startOperations.add(operation);
        return this;
    }

    public List<Pair<Node, List<String>>> run()
    {
        // init because we may not have been given the pairs already
        if (nodeSet == null)
        {
            nodeSet = new ArrayList<Pair<Node, List<String>>>() {};
            nodeSet.add(new Pair<Node, List<String>>(startNode, startOperations));
        }

        // create external node set so we have a proper stopping case
        List<Pair<Node, List<String>>> externalNodeSet = new ArrayList<>();
        List<Pair<Node, List<String>>> finishedNodeSet = new ArrayList<>();

        // keep going while we have nodes in the set
        while (!nodeSet.isEmpty())
        {
            // get the first node from the list
            Pair<Node, List<String>> curPair = nodeSet.get(0);
            nodeSet.remove(0);
            if (curPair.getKey().getClass() == ExternalNodeProxy.class)
                // if external then set aside for later
                externalNodeSet.add(curPair);
            if (curPair.getValue().isEmpty())
                // if operations is empty then we are done
                finishedNodeSet.add(curPair);
            else
                // if internal do the operations
                nodeSet.addAll(operate(curPair.getKey(), curPair.getValue()));
        }

        finishedNodeSet.addAll(reconcile(externalNodeSet));
        return finishedNodeSet;
    }

    public List<Pair<Node, List<String>>> reconcile(List<Pair<Node, List<String>>> externalNodeSet)
    {
        Stream<Pair<Node, List<String>>> externalNodeSetStream = externalNodeSet.parallelStream();
        List<List<Pair<Long, List<String>>>> machines = new ArrayList<List<Pair<Long, List<String>>>>(5);
        for (int i = 0; i < 5; i++)
        {
            final int num = i;
            machines.add(externalNodeSetStream
                            .filter(pair -> ((ExternalNodeProxy) pair.getKey()).getMachineId() == num)
                            .map(pair -> new Pair<Long, List<String>>(pair.getKey().getId(), pair.getValue()))
                            .collect(Collectors.toList())
            );
        }

        Client clients[] = new Client[5];
        for (int i = 0; i < 5; i++)
        {
            if (machines.get(i).size() != 0)
            {
                try
                {
                    clients[i] = new Client(ips[i].getHostName(), 6067, machines.get(i));
                    clients[i].start();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < 5; i++)
        {
            try
            {
                clients[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //List<Pair<Long, List<String>>> resultIdSet = new ArrayList<>();
        List<Pair<Node, List<String>>> resultSet = new ArrayList<>();

        for (int i = 0; i < 5; i++)
        {
            final int num = i;
            Stream<Pair<Long, List<String>>> clientStream = clients[i].getResult().parallelStream();
            resultSet.addAll(clientStream
                    .map(pair -> new Pair<Node, List<String>>(new ExternalNodeProxy(num, pair.getKey()), pair.getValue()))
                    .collect(Collectors.toList()));
        }

        return resultSet;
    }

    public List<Pair<Node, List<String>>> operate(Node node, List<String> operations)
    {
        // get the first operation and trim for new pairs
        String operation = operations.get(0);
        operations.remove(0);

        // return list
        List<Pair<Node, List<String>>> nextNodes = new ArrayList<Pair<Node, List<String>>>();

        // different operations
        switch (operation) {
            case "friends":
                for (Relationship rel : node.getRelationships(Direction.OUTGOING))
                {
                    nextNodes.add(new Pair<>(rel.getOtherNode(node), operations));
                }
                break;
            default: break;
        }
        return nextNodes;
    }

    private void setIps()
    {
        try
        {
            this.ips = new InetAddress[]{
                    InetAddress.getByName("172.22.150.74"), //machine01
                    InetAddress.getByName("172.22.150.75"), //machine02
                    InetAddress.getByName("172.22.150.76"),
                    InetAddress.getByName("172.22.150.77"),
                    InetAddress.getByName("172.22.150.78")  //machine5
            };

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
