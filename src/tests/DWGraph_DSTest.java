package tests;

import api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DWGraph_DSTest {
    private static Random rnd = new Random();
    public final double INFINITY = Double.POSITIVE_INFINITY;
    public directed_weighted_graph myGraph = new DWGraph_DS();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 5; i++)
            myGraph.addNode(new NodeData(i));

        myGraph.connect(0,1,3);
        myGraph.connect(0,2,1);
        myGraph.connect(1,2,20);
        myGraph.connect(1,3,5);
        myGraph.connect(1,4,7);
    }

    @AfterEach
    void tearDown() {
        Collection<node_data> vertices = myGraph.getV();
        for (node_data currNode : vertices){
            currNode.setInfo("WHITE");
        }
    }

    @Test
    void addNode() {
        int firstMC = myGraph.getMC();
        int numOfNodes = myGraph.nodeSize();
        myGraph.addNode(new NodeData(2));
        assertEquals(firstMC,myGraph.getMC());
        assertEquals(numOfNodes,myGraph.nodeSize());
        myGraph.addNode(new NodeData(6));
        assertEquals(firstMC+1,myGraph.getMC());
        assertEquals(numOfNodes+1,myGraph.nodeSize());
    }

    @Test
    void getNode() {
        assertNotNull(myGraph.getNode(2));
        assertNull(myGraph.getNode(8));
    }

    @Test
    void getEdge() {
        myGraph.connect(2,3,100);
        double w = myGraph.getEdge(2,3).getWeight();
        assertEquals(w, 100);
        assertNotNull(myGraph.getEdge(2,3));
        myGraph.removeEdge(2,3);
        assertNull(myGraph.getEdge(2,3));
        myGraph.connect(0,4,150);
        assertNotNull(myGraph.getEdge(0,4));
        myGraph.removeEdge(0,4);
        assertNull(myGraph.getEdge(0,4));
        myGraph.getEdge(200,140);
        assertNull(myGraph.getEdge(200,140));
        assertNull(myGraph.getEdge(4,4));
    }

    @Test
    void connect() {
        int numOfEdges = myGraph.edgeSize();
        myGraph.connect(2,4,100);
        assertNotNull(myGraph.getEdge(2,4));
        assertEquals(myGraph.edgeSize(),numOfEdges+1);
        myGraph.removeEdge(2,4);
        assertNull(myGraph.getEdge(2,4));
        assertEquals(myGraph.edgeSize(),numOfEdges);
        myGraph.connect(11,14,55);
        assertNull(myGraph.getEdge(11,14));
    }

    @Test
    void getV() {
        Collection<node_data> vertices = myGraph.getV();
        int expected = vertices.size();
        int actual = myGraph.nodeSize();
        assertEquals(expected, actual);
    }

    @Test
    void getE() {
        Collection<edge_data> neighbors = myGraph.getE(1);
        HashSet<edge_data> actual = new HashSet<>();
        actual.add(myGraph.getEdge(1,2));
        actual.add(myGraph.getEdge(1,3));
        actual.add(myGraph.getEdge(1,4));
        assertEquals(neighbors,actual);
    }

    @Test
    void removeNode() {
        int numOfNodes = myGraph.nodeSize();
        int numOfEdges = myGraph.edgeSize();
        myGraph.removeNode(1);
        assertNull(myGraph.getNode(1));
        assertEquals(numOfNodes-1,myGraph.nodeSize());
        assertEquals(numOfEdges-3,myGraph.edgeSize());
    }

    @Test
    void removeEdge() {
        myGraph.removeEdge(0,2);
        assertNull(myGraph.getEdge(0,2));
    }

    @Test
    void nodeSize() {
        myGraph.removeNode(2);
        assertEquals(myGraph.nodeSize(),4);
    }

    @Test
    void edgeSize() {
        myGraph.removeEdge(0,2);
        assertEquals(myGraph.edgeSize(),4);
    }

    @Test
    void getMC() {
        int firstMC = myGraph.getMC();
        myGraph.addNode(new NodeData(6));
        assertEquals(firstMC + 1, myGraph.getMC());
        myGraph.connect(3, 3, 30);
        assertEquals(firstMC + 1, myGraph.getMC());
        myGraph.removeEdge(2, 3);
        assertEquals(firstMC + 1, myGraph.getMC());
        myGraph.removeNode(6);
        assertEquals(firstMC + 2, myGraph.getMC());
    }

//    @Test
//    void testMillion() {
//        long start = new Date().getTime();
//        directed_weighted_graph millionGraph = new DWGraph_DS();
//        int i = 0;
//        while (millionGraph.nodeSize() < 1000000){
//            node_data newNode = new NodeData(i);
//            millionGraph.addNode(newNode);
//            i++;
//        }
//        while (millionGraph.edgeSize() < 10000000) {
//            int node1 = rnd.nextInt(1000000);
//            int node2 = rnd.nextInt(1000000);
//            double weight  = node2 % 10 + 1;
//            millionGraph.connect(node1, node2, weight);
//        }
//        long end = new Date().getTime();
//        double runtime = (end-start)/1000.0;
//        System.out.println("runtime: " + runtime);
//
//        assertEquals(1000000, millionGraph.nodeSize());
//        assertEquals(10000000, millionGraph.edgeSize());
//    }
}