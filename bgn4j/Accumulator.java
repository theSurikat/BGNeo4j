package bgn4j;

import javafx.util.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.impl.core.*;
import java.util.ArrayList;
import java.util.List;

public class Accumulator
{
    private List<Pair<Node, List<String>>> nodeSet;
    private Node startNode;
    private List<String> startOperations;

    public Accumulator() {
        this.startOperations = new ArrayList<>();
    };

    public Accumulator(Node startNode, List<String> startOperations)
    {
        this.startNode = startNode;
        this.startOperations = startOperations;
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
        // TODO add reconcile, this is just for testing
        finishedNodeSet.addAll(externalNodeSet);
        return finishedNodeSet;
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
}
