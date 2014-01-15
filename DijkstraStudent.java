import HW3.*;

import java.io.IOException;
import java.util.LinkedList;

public class DijkstraStudent extends DisjkstraAbstract {

	/**
	 * Use this for your own testing purposes. Feel free to change it as you
	 * wish, as we will write a different one for grading
	 */
	public static void main(String args[]) {
		try {
			DijkstraStudent solver = new DijkstraStudent();
			int[][] L;
			int startState = 0;
			L = solver.readData("graph2.txt");
			// you can change the source state as you wish
			solver.runDijkstra(L, startState);

			// display results
			System.out.println("The routes are:");
			for (int i = 0; i < solver.Nodes.length; i++) {
				if (i != startState) {
					solver.displayPath(solver.Nodes[i]);
				}
			}
			System.out
					.println("Actually, there is a little green man furiously typing this output.");

		} catch (IOException e) {
			System.out
					.println("There was a problem reading the input file. Please make sure you are giving the right path to the file and that the file is written in the right format");
		}
	}

	/**
	 * Runs Dijskstra's algorithm to compute shortest distances form a given
	 * vertex of a graph
	 * 
	 * @param L
	 *            : 2D array where the value at index (i,j) holds the cost on
	 *            the edge from i to j. A cost of 0 is representative of a lack
	 *            of an edge from i to j.
	 * @param pStartIdx
	 *            : Index of start state of paths that are to be computed.
	 */
	public void runDijkstra(int[][] L, int pStartIdx) {
		// import data to work on
		initializeData(L);

		// initialize priority queue with the start node
		PriorityQueue Q = new PriorityQueue();
		insert(Nodes[pStartIdx], Q);
		Nodes[pStartIdx].dist = 0;
		// while the priority queue is non-empty
		while (!Q.isEmpty()) {
			// extract the next node for which we know the shortest path
			GraphNode d = extractMin(Q);
			for (GraphNode v : d.neighbours) {
				switch (v.status) {
				case UNPROCESSED: // add to Q
					v.dist = d.dist + L[d.pos][v.pos];
					v.path = d;
					insert(v, Q);
					break;
				case IN_HEAP: // change priority in Q
					int newDist = d.dist + L[d.pos][v.pos];
					if (newDist < v.dist) {
						v.path = d;
						changePriority(v, newDist);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	public void insert(GraphNode pData, PriorityQueue pPQ) {
		//System.out.println("Inserting!" + pData.name);
		PQNode insert = new PQNode();
		insert.graphNode = pData;
		pData.nodeInPQ = insert;
		pData.status = GraphNode.Status.IN_HEAP;
		// if the queue is empty, then we add it as head and last in heap
		if (pPQ.isEmpty()) {
			pPQ.head = insert;
			pPQ.lastInHeap = insert;
		} else {
			// otherwise, we go to the end of the heap and add it there
			PQNode curr = pPQ.lastInHeap;
			pPQ.lastInHeap.next = insert;
			pPQ.lastInHeap = insert;
			insert.prev = curr;
			if (curr == pPQ.head) {
				if (curr.left == null) {
					curr.left = insert;
					insert.parent = curr;
				} else {
					curr.right = insert;
					insert.parent = curr;
				}
			} else {
				// we then link it to its parent, as it is either the sibling of
				// the last node
				if (curr.parent.right == null) {
					curr.parent.right = insert;
					insert.parent = curr.parent;
					// else, it is the first child of the node next to the
					// parent
				} else {
					curr.parent.prev.left = insert;
					insert.parent = curr.prev;
				}
				//System.out.println("Inserting!");
				heapifyUp(insert);
			}
		}

	}

	public GraphNode extractMin(PriorityQueue pPQ) {
		GraphNode min = pPQ.head.graphNode;
		pPQ.head.graphNode.status = GraphNode.Status.KNOWN;
		// if head is the same as tail, then there is only 1 node
		if (pPQ.head == pPQ.lastInHeap) {
			pPQ.head = null;
			pPQ.lastInHeap = null;
		}  else {
			//we replace the top with the last element
			pPQ.head.graphNode = pPQ.lastInHeap.graphNode;
			pPQ.head.graphNode.nodeInPQ = pPQ.lastInHeap.graphNode.nodeInPQ;
			//we then the node at the end, corresponding to where the last element was 
			if (pPQ.lastInHeap.parent.left == pPQ.lastInHeap) {
				pPQ.lastInHeap.parent.left = null;
			} else {
				pPQ.lastInHeap.parent.right = null;
				System.out.println(pPQ.lastInHeap.parent.right);
			}
			pPQ.lastInHeap = pPQ.lastInHeap.prev;
			pPQ.lastInHeap.next = null;
			//finally, we heapify down the root
			heapifyDown(pPQ.head);
		}
		return min;
	}

	public void changePriority(GraphNode pData, int pNewPriority) {
		pData.dist = pNewPriority;
		// assumes that priority will only be changed if it is lower than the
		// previous
		heapifyUp(pData.nodeInPQ);
	}

	public void displayPath(GraphNode v) {
		
		GraphNode prev = v.path;
		String path = "";
		while (prev != null) {
			path = prev.name + " " + path;
			prev = prev.path;
		}
		System.out.print("The shortest path to " + v.name + " is: " + path);
		System.out.println("Distance is: " + v.dist);

	}

	public void heapifyDown(PQNode root) {
		PQNode smallestChild;
		// if both children are null then we're done
		while (root.left != null || root.right != null) {
			// if left is null then only right child exists
			if (root.left == null) {
				smallestChild = root.right;
				// if right is null then only left exists
			} else if (root.right == null) {
				smallestChild = root.left;
			} else {
				// else we find the biggest child
				if (root.right.graphNode.dist < root.left.graphNode.dist) {
					smallestChild = root.right;
				} else {
					smallestChild = root.left;
				}
			}
			// we swap the node with the biggest child
			GraphNode temp = root.graphNode;
			PQNode tempInPQ = root.graphNode.nodeInPQ;
			root.graphNode = smallestChild.graphNode;
			root.graphNode.nodeInPQ = smallestChild.graphNode.nodeInPQ;
			smallestChild.graphNode = temp;
			smallestChild.graphNode.nodeInPQ = tempInPQ;
			root = smallestChild;
		}

	}

	public void heapifyUp(PQNode leaf) {
		// if the node is the root then we're done
		while (leaf.parent != null) {
			// if the parent is bigger than the child, we swap them and keep
			// going
			if (leaf.parent.graphNode.dist > leaf.graphNode.dist) {
				GraphNode temp = leaf.graphNode;
				PQNode tempInPQ = leaf;
				leaf.graphNode = leaf.parent.graphNode;
				leaf.graphNode.nodeInPQ = leaf.parent.graphNode.nodeInPQ;
				leaf.parent.graphNode = temp;
				leaf.parent.graphNode.nodeInPQ = tempInPQ;
				leaf = leaf.parent;
			} else {
				// if the parent is smaller then we're done
				return;
			}
		}
	}
}
