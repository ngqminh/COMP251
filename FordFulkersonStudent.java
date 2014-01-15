import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import HW6.*;
import HW6.Residual.Path;

public class FordFulkersonStudent extends FordFulkAbstract{

	
	public static void main(String[] args) {		
		try {
			String todo = "graph2.txt";
			OriginalGraph G = new OriginalGraph(todo);
			FordFulkersonStudent ff = new FordFulkersonStudent();
			double max_flow = ff.maxFlow(G, 0, G.numS()-1);
			System.out.println("The max flow on the graph " +  todo + " is " + max_flow);
			System.out.println("If the answer is correct make sure you try it on some other graph" +
					". If you don't know how to input another graph, this course has a Discussion Board which you should use");
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	/**
	 * Computes the maximum flow between two states of a given graph, 
	 * using the Fold-Fulkerson algorithm
	 * @param G : the graph over which the algorithm is to be run
	 * @param s : index of the state of origin of the flow
	 * @param t : index of the state of destination of the flow
	 * @return the maximum flow of the network
	 */
	public int maxFlow(OriginalGraph G, int s, int t) {
		Residual residual = new Residual(G);
		Residual.Path p = getAugmentingPath(residual, s, t);
		while(p != null) {
			System.out.println(p);
			augment(residual, p);			
			System.out.println(residual);
			p = getAugmentingPath(residual, s, t);
		}
		return residual.extractFlow();
	}

	public void augment(Residual res, Path path) {
		//get the smallest flow
		int minFlow = Integer.MAX_VALUE;

		for(Residual.Edge e : path.pathElements()) {
			if (e.residual < minFlow && e.residual > 0) {
				minFlow = e.residual;
			}
		}

		for(Residual.Edge e : path.pathElements()) {
			
			//we flow along a path, and decrement from the residual graph
			if (e.opposite == null) {
				Residual.Edge newEdge = res.new Edge();
				newEdge.origin = e.destination;
				newEdge.destination = e.origin;
				newEdge.opposite = e;
				newEdge.residual = 0;
				res.graph.get(e.destination).add(newEdge);
				e.opposite = newEdge;
			}
			e.residual -= minFlow;
			// we increment, showing deals that can be broken
			e.opposite.residual += minFlow;
		}
		
	}

	public Path getAugmentingPath(Residual G, int s, int t) {
		//finds a path in the graph and returns it
		//outer ArrayList has the start nodes and inner LinkedList has the end nodes
		ArrayList<LinkedList<Residual.Edge>> graph = G.graph;
		Residual.Path path = G.new Path();
		boolean[] visited = new boolean[graph.size()];
		if(DFS(visited, path, graph, s, t)) {
			return path;
		} else {
			return null;
		}

	}
	
	public boolean DFS(boolean[] visited, Path p, ArrayList<LinkedList<Residual.Edge>> graph, int curr, int t) {
		// gets every neighbour of the current node
		visited[curr] = true;
		int[] neighbours = new int[graph.get(curr).size()];
		for (int i = 0; i < graph.get(curr).size(); i++) {
			Residual.Edge e = graph.get(curr).get(i);
			neighbours[i] = e.destination;
		}
		int i = 0;
		for (Residual.Edge residuals : graph.get(curr)) {
			// if an unvisited neighbour is found, we visit it and call the DFS on that node
			// we can only travel through the edge if it has a capacity greater than 0
			if(!visited[neighbours[i]] && residuals.residual > 0) {
				visited[neighbours[i]] = true;
				//we then travel to the destination of the edge, if the destination is found
				//we then add the edges into the path, since we know it goes from s to t
				if (DFS(visited, p, graph, residuals.destination, t) || residuals.destination == t) {
					p.addFirst(residuals);
					return true;
				}
				
			}
		i++;
		}
		return false;
	}
	
	//DFS made it easier to make a path without branching
	//actually now that I think about it, it would've been as easy with BFS
	//lol oops
	public void BFS(Path p, ArrayList<LinkedList<Residual.Edge>> graph, int s, int t) {
		Queue<Integer> q = new LinkedList<Integer>();
		boolean[] visited = new boolean[graph.size()];
		System.out.println(s);
		q.add(s);
		visited[s] = true;
		while (!q.isEmpty()) {
			int v = q.remove();
			for(Residual.Edge residuals : graph.get(v)) {
				int dest = residuals.destination;
				if (residuals.residual > 0) {
					if (!visited[dest]) {
						visited[dest] = true;
						
						q.add(dest);
						p.addLast(residuals);
					}
				}
			}
			
		}
		System.out.println(p.toString());
	}
	
}
