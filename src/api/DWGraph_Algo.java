package api;

import com.google.gson.*;
import gameClient.util.Point3D;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This interface represents a Directed positive Weighted Graph Theory Algorithms including:
 * 0. clone(); (copy)
 * 1. init(graph);
 * 2. isConnected(); // strongly (all ordered pais connected)
 * 3. double shortestPathDist(int src, int dest);
 * 4. List<node_data> shortestPath(int src, int dest);
 * 5. Save(file); // JSON file
 * 6. Load(file); // JSON file
 * @author ko tal
 */
public class DWGraph_Algo implements dw_graph_algorithms{
    private DWGraph_DS myGraph;

    //Creates a HashSet which contains all the visited nodes.
    private HashSet<node_data> visited = new HashSet<>();

    public DWGraph_Algo() {
        this.myGraph = new DWGraph_DS();
    }

    public DWGraph_Algo(directed_weighted_graph g) {
        init(g);
    }

    /**
     * Init the graph on which this set of algorithms operates on.
     * @param g
     */
    @Override
    public void init(directed_weighted_graph g) {
        this.myGraph = (DWGraph_DS) g;
    }

    /**
     * Return the underlying graph of which this class works.
     * @return
     */
    @Override
    public directed_weighted_graph getGraph() {
        return myGraph;
    }

    /**
     * Compute a deep copy of this weighted graph.
     * @return
     */
    @Override
    public directed_weighted_graph copy() {
        return myGraph.copy();
    }

    /**
     * Returns true if and only if (iff) there is a valid path from each node to each other node.
     * @return
     */
    @Override
    public boolean isConnected() {
      /*
        Checks if the graph is not empty, then runs the BFS algorithm on the first node in the graph.
        After that, checks if all of the nodes have been visited by comparing the number of nodes in
        the graph to the number of the nodes that have been marked as visited.
        If they are not equals then return false.
         */
        if (myGraph.nodeSize() > 1) {
            node_data start = myGraph.getNode(0);
            BFS(myGraph, start);
            if (myGraph.nodeSize() == visited.size()){
                DWGraph_DS G_t = (DWGraph_DS) this.myGraph.getTransposeGraph();
                node_data start_t = G_t.getNode(0);
                BFS(G_t,start_t);
                return G_t.nodeSize() == visited.size();
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * returns the length of the shortest path between src to dest
     * Note: if no such path --> returns -1
     * @param src - start node
     * @param dest - end (target) node
     * @return
     */
    @Override
    public double shortestPathDist(int src, int dest) {
        NodeData result;
        if (myGraph.getV().isEmpty() || myGraph.getNode(src) == null || myGraph.getNode(dest) == null)
            return -1;
        if (src == dest || myGraph.nodeSize() == 1)
            return 0;
        Dijkstra(src);
        if (myGraph.getNode(dest).getTag() == 0) {
            System.out.println("this graph is not connected");
            return -1;
        } else {
            result = (NodeData) myGraph.getNode(dest);
            return result.getSinker();
        }
    }

    /**
     * returns the the shortest path between src to dest - as an ordered List of nodes:
     * src--> n1-->n2-->...dest
     * Note if no such path --> returns null;
     * @param src - start node
     * @param dest - end (target) node
     * @return
     */
    @Override
    public List<node_data> shortestPath(int src, int dest) {
        //Creates an ArrayList which is used to contain the path.
        List<node_data> path = new ArrayList<>();

        /*
        Calls the Dijkstra method to check if there exists a pathway between both of the given nodes.
        If the Dijkstra function returned a positive number, then adds all the numbers in the info of
        the destination node to the array (by calling isNumeric method).
        Then adds the destination node to the list and returns the path.
         */
        double result = shortestPathDist(src, dest);
        if (result > -1) {

            //Return the source if both of the src and the dest are equals.
            if (result == 0) {
                path.add(this.getGraph().getNode(src));
                return path;
            }

            node_data destination = myGraph.getNode(dest);
            String str = destination.getInfo();
            String[] arr = str.split("->");
            for (String temp : arr) {
                if (isNumeric(temp)) {
                    int key = Integer.parseInt(temp);
                    path.add(getGraph().getNode(key));
                }
            }
            path.add(destination);
            return path;
        }
        return null;
    }

    public ArrayList<ArrayList<node_data>> connectedComponents(){
        ArrayList<node_data> stack = new ArrayList<>();
        ArrayList<ArrayList<node_data>> result = new ArrayList<>();

        myGraph.clear();
        for (node_data currNode : myGraph.Nodes.values())
            if (currNode.getTag() == 0)
                DFS(myGraph,currNode.getKey(),stack);

        DWGraph_DS graph_t = (DWGraph_DS)this.myGraph.getTransposeGraph();
        myGraph.clear();
        while (!stack.isEmpty()){
            node_data currNode = stack.get(stack.size()-1);
            stack.remove(stack.size()-1);
            if (currNode.getTag() == 0){
                ArrayList<node_data> scc = new ArrayList<>();
                DFS(graph_t,currNode.getKey(),scc);
                result.add(scc);
            }
        }
        return result;
    }

    public ArrayList<node_data> connectedComponent(int key){
        ArrayList<ArrayList<node_data>> result;
        result = connectedComponents();
        node_data selectedNode = new NodeData(key);
        for (ArrayList<node_data> stack: result)
            if (stack.contains(selectedNode))
                return stack;

        return null;
    }

    /**
     * Saves this weighted directional graph to the given
     * file name - in JSON format
     * @param file - the file name (may include a relative path).
     * @return true - iff the file was successfully saved
     */
    @Override
    public boolean save(String file) {
        boolean ans = false;
        try {
            JSONObject graph = new JSONObject();
            JSONArray nodes = new JSONArray();
            JSONArray edges = new JSONArray();
            Collection<NodeData> vertices = this.myGraph.getV().stream().map(n -> (NodeData) n).collect(Collectors.toCollection(HashSet::new));
            for (NodeData currNode : vertices)
                nodes.put(currNode.toJSON());

            graph.put("Nodes",nodes);
            for (HashMap<Integer, edge_data> h : this.myGraph.Edges.values()) {
                Collection<EdgeData> edgesCollection = h.values().stream().map(e -> (EdgeData) e).collect(Collectors.toCollection(HashSet::new));
                for (EdgeData currEdge : edgesCollection)
                    edges.put(currEdge.toJSON());

            }
            graph.put("Edges",edges);

            PrintWriter pw = new PrintWriter(file);
            pw.write(graph.toString());
            pw.close();
            ans = true;

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
        return ans;
    }

    /**
     * This method load a graph to this graph algorithm.
     * if the file was successfully loaded - the underlying graph
     * of this class will be changed (to the loaded one), in case the
     * graph was not loaded the original graph should remain "as is".
     * @param file - file name of JSON file
     * @return true - iff the graph was successfully loaded.
     */
    @Override
    public boolean load(String file) {
        boolean result = false;

        JsonDeserializer<DWGraph_DS> deserializer = new JsonDeserializer<DWGraph_DS>() {
            @Override
            public DWGraph_DS deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonArray edgesArr = jsonObject.get("Edges").getAsJsonArray();
                ArrayList<EdgeData> edgeList = new ArrayList<>();

                for (JsonElement je : edgesArr){
                    int src = je.getAsJsonObject().get("src").getAsInt();
                    double w = je.getAsJsonObject().get("w").getAsDouble();
                    int dest = je.getAsJsonObject().get("dest").getAsInt();
                    EdgeData newEdge = new EdgeData(src,dest,w);
                    edgeList.add(newEdge);
                }

                JsonArray nodesArr = jsonObject.get("Nodes").getAsJsonArray();
                ArrayList<NodeData> nodesList = new ArrayList<>();

                for (JsonElement je : nodesArr){
                    int id = je.getAsJsonObject().get("id").getAsInt();
                    String pos_str = je.getAsJsonObject().get("pos").getAsString();
                    String[] arr = pos_str.split(",");
                    Point3D pos = new Point3D(arr[0],arr[1],arr[2]);
                    NodeData newNode = new NodeData(id,pos);
                    nodesList.add(newNode);
                }
                return new DWGraph_DS(nodesList, edgeList);
            }
        };

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DWGraph_DS.class,deserializer);
        Gson customGson = builder.create();
        DWGraph_DS loadGraph = customGson.fromJson(file,DWGraph_DS.class);
        this.init(loadGraph);
//            System.out.println(loadGraph);
        result = true;

        return result;
    }

    public boolean loadFromFile(String file) {
        boolean result = false;

        try {
            FileReader reader = new FileReader(file);
            JsonDeserializer<DWGraph_DS> deserializer = new JsonDeserializer<DWGraph_DS>() {
                @Override
                public DWGraph_DS deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonArray edgesArr = jsonObject.get("Edges").getAsJsonArray();
                    ArrayList<EdgeData> edgeList = new ArrayList<>();

                    for (JsonElement je : edgesArr){
                        int src = je.getAsJsonObject().get("src").getAsInt();
                        double w = je.getAsJsonObject().get("w").getAsDouble();
                        int dest = je.getAsJsonObject().get("dest").getAsInt();
                        EdgeData newEdge = new EdgeData(src,dest,w);
                        edgeList.add(newEdge);
                    }

                    JsonArray nodesArr = jsonObject.get("Nodes").getAsJsonArray();
                    ArrayList<NodeData> nodesList = new ArrayList<>();

                    for (JsonElement je : nodesArr){
                        int id = je.getAsJsonObject().get("id").getAsInt();
                        String pos_str = je.getAsJsonObject().get("pos").getAsString();
                        String[] arr = pos_str.split(",");
                        Point3D pos = new Point3D(arr[0],arr[1],arr[2]);
                        NodeData newNode = new NodeData(id,pos);
                        nodesList.add(newNode);
                    }
                    return new DWGraph_DS(nodesList, edgeList);
                }
            };

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DWGraph_DS.class,deserializer);
            Gson customGson = builder.create();
            DWGraph_DS loadGraph = customGson.fromJson(reader,DWGraph_DS.class);
            this.init(loadGraph);
//            System.out.println(loadGraph);
            result = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* HELPFUL TOOLS */
    /**
     * The method gets a string and checks if its contains a number
     *
     * @param str a string
     * @return true id the string contains a number
     */
    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return myGraph.toString();
    }

    /*ALGORITHMS TOOLS*/
    /**
     * Put the given vertex in the priority queue,
     * priority queue sort the vertices by they tags value
     * for each vertex we sum the current vertex's tag with his connected edge's weight
     * each time we poll vertex with the minimal value in the priority queue
     * go over all its neighbors, select the neighbor with the minimal value and put it in the priority queue
     * mark all the vertex we passed,
     * if there is a path with a minimal weight we will discover it and select this path
     * each vertex we finished passing out of the priority queue
     * @param src - The vertex we put in the priority queue
     */
    private void Dijkstra(int src){
        myGraph.clear();
        PriorityQueue<NodeData> priorityQueue = new PriorityQueue<>(myGraph.nodeSize(), new Node_Comparator());
        NodeData startNode = (NodeData) myGraph.getNode(src);
        startNode.setTag(0);
        startNode.setSinker(0);
        priorityQueue.add(startNode);

        while (!priorityQueue.isEmpty()){
            NodeData currNode = priorityQueue.poll();
            double currNodeSinker = currNode.getSinker();
            if (currNode.getTag() == 0){ // if node is not visited

                Collection<edge_data> edges = myGraph.getE(currNode.getKey());
                for (edge_data edge : edges) {
                    NodeData neighbor = (NodeData) myGraph.getNode(edge.getDest());
                    double edgeWeight = edge.getWeight();
                    if (currNodeSinker + edgeWeight < neighbor.getSinker()){
                        neighbor.setSinker(currNodeSinker + edgeWeight);
                        String key = String.valueOf(currNode.getKey());
                        neighbor.setInfo(currNode.getInfo() + "->" + key);
                        if (neighbor.getTag() == 0)
                            priorityQueue.add(neighbor);
                    }
                }
                currNode.setTag(1); // marked
            }
        }
    }

    /**
     * The Breadth-first search (BFS) is an algorithm for traversing or searching
     * tree or graph data structures. It starts at the given node in the graph,
     * and explores all of the neighbor nodes at the present depth prior to moving on
     * to the nodes at the next depth level.
     *
     * @param g      the graph which the search will run.
     * @param source the node from which the search will start.
     */
    private void BFS(directed_weighted_graph g, node_data source) {
        //Clears the visited HashSet.
        visited.clear();

        /*
        Creates a queue which will contain the nodes that need to traverse (by their order).
         */
        Queue<node_data> queue = new LinkedList<>();
        queue.add(source);

        /*
        While the queue is not empty, the algorithm takes the first node and traverses all its neighbors.
        If this neighbor is not yet visited, it adds to the queue and marks as visited.
        After the algorithm finishes gaining with all the neighbors, it marks the current node as visited
        and continues to the next node in the queue.
         */
        while (!queue.isEmpty()) {
            node_data current = queue.poll();
            for (edge_data neighbor : g.getE(current.getKey())) {
                node_data temp = g.getNode(neighbor.getDest());
                if (!visited.contains(temp)) {
                    queue.add(temp);
                    visited.add(temp);
                }
            }
            visited.add(current);
        }
    }

    public void DFS(directed_weighted_graph graph, int src, ArrayList<node_data> stack) {
        Stack<node_data> dfsStack = new Stack<>();
        node_data src_node = myGraph.getNode(src);
        src_node.setTag(1);
        dfsStack.add(src_node);

        while(!dfsStack.isEmpty()){
            node_data currNode = dfsStack.pop();
            Collection<edge_data> edges = myGraph.getE(currNode.getKey());
            for (edge_data neighbour_edge : edges)
                if ((graph.getNode(neighbour_edge.getDest())).getTag() == 0){
                    graph.getNode(neighbour_edge.getDest()).setTag(1);
                    dfsStack.add(graph.getNode(neighbour_edge.getDest()));
                    break;
                }
            stack.add(currNode);
        }
    }
}
