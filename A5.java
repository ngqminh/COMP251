import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.*;

import HW5.*;
import HW5.LakeEnvir.Action;
import HW5.LakeEnvir.RandomState;
import HW5.LakeEnvir.State;

public class ValueIterationStudent implements ValueIterationInterface {

	// used to create the Graphical Interface
	static JFrame frame;
	double discount = 0.09;

	/**
	 * Main function: feel free to change
	 * 
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String args[]) {
		// the environment we create with default parameters
		LakeEnvir.LakeParameters P = new LakeEnvir.LakeParameters();

		// create the Graphical Interface
		frame = new JFrame();
		frame.setTitle("DrawRect");
		frame.setSize(P.GIScale * (P.horizontalLength + 2), P.GIScale
				* (P.verticalLength + 2));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		Container contentPane = frame.getContentPane();
		LakeEnvir d = new LakeEnvir(P);
		contentPane.add(d);
		frame.show();

		// simulate the algorithms
		// for(Algotype algo : Algotype.values()) {
		// simulateAlgo(algo, d);
		// }
		simulateAlgo(Algotype.SYNCH, d);
	}

	/**
	 * the types of algorithms to be tested
	 */
	public enum Algotype {
		SYNCH, ASYNCH, ASYNCH_PUSH
	};

	/**
	 * Method used to simulate the policy resulting from the value function at
	 * each iteration of "value iteration". This is done on different types of
	 * algorithms (Feel free to change)
	 * 
	 * @param pAlgo
	 *            : type of algorithm to simulate
	 * @param pLake
	 *            : environment to run on
	 */
	private static void simulateAlgo(Algotype pAlgo, LakeEnvir pLake) {

		ValueFunction V = new ValueFunction(pLake);
		ValueIterationInterface vi = new ValueIterationStudent();
		for (int i = 0; i < 20; i++) {
			Policy pi = vi.getPolicy(V);
			pLake.repaint();
			pLake.drawPolicy(pi);
			System.out.println("Done drawing policy!");
			pLake.simulatePolicy(
					pLake.new State(pLake.horizLength() - 1,
							pLake.vertLength() - 1), pi, 30);

			System.out.println("Done simulating policy!");
			try {
				java.lang.Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			switch (pAlgo) {
			case SYNCH:
				vi.synchValueIteration(V);
				break;
			case ASYNCH:
				vi.asynchValueIteration(V);
				break;
			case ASYNCH_PUSH:
				vi.pushValueIteration(V);
				break;
			}
		}
	}

	@Override
	public void synchValueIteration(ValueFunction V) {
		// we set every state s in S
		State[][] states = new State[V.lake.horizLength()][V.lake.vertLength()];
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				states[i][j] = V.lake.new State(i, j);
			}
		}
		double[][] d = new double[V.lake.horizLength()][V.lake.vertLength()];
		//we store the previous iterations
		double[][] prev = new double[V.lake.horizLength()][V.lake.vertLength()];
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				prev[i][j] = V.getValue(states[i][j]);
			}
		}

		// every tile
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				double max = Double.NEGATIVE_INFINITY;
				State s = states[i][j];
				// for every state, V(s) = max {sum P(s, a, s')*[R(s, a, s') +
				// discount*Vprev(s')]}
				for (Action a : Action.values()) {
					double term = 0;
					for (LakeEnvir.RandomState.Outcome o : V.lake.getNextState(s, a).getPossibleOutcomes()) {
						term += o.weight*(V.lake.getReward(s, a, o.state) + discount*V.getValue(o.state));
					}
					if (term > max)
						max = term;
				}
				d[i][j] = max;
			}
		}
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				if(prev[i][j] != d[i][j]) {
					V.updateValue(states[i][j], d[i][j]);
				}
			}
		}

	}

	@Override
	public void asynchValueIteration(ValueFunction V) {
		// we set every state s in S
		double[][] d = new double[V.lake.horizLength()][V.lake.vertLength()];
		State[][] states = new State[V.lake.horizLength()][V.lake.vertLength()];
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				states[i][j] = V.lake.new State(i, j);
			}
		}
		// every tile
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				double max = Double.NEGATIVE_INFINITY;
				State s = states[i][j];
				// for every state, V(s) = max {sum P(s, a, s')*[R(s, a, s') +
				// discount*Vprev(s')]}
				for (Action a : Action.values()) {
					double term = 0;
					for (LakeEnvir.RandomState.Outcome o : V.lake.getNextState(
							s, a).getPossibleOutcomes()) {
						term += o.weight
								* (V.lake.getReward(s, a, o.state) + discount
										* V.getValue(o.state));
					}
					if (term > max)
						max = term;
				}
				d[i][j] = max;
			}
		}
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				if(V.getValue(states[i][j]) != d[i][j]) {
					V.updateValue(states[i][j], d[i][j]);
				}
			}
		}

	}

	@Override
	public void pushValueIteration(ValueFunction V) {
		// TODO Auto-generated method stub

	}

	@Override
	public Policy getPolicy(ValueFunction V) {
		final Action[][] pi = new Action[V.lake.horizLength()][V.lake
				.vertLength()];
		for (int i = 0; i < V.lake.horizLength(); i++) {
			for (int j = 0; j < V.lake.vertLength(); j++) {
				State s = V.lake.new State(i, j);
				double max = Double.NEGATIVE_INFINITY;
				Action best = null;

				for (Action a : Action.values()) {
					double term = 0;
					for (LakeEnvir.RandomState.Outcome o : V.lake.getNextState(
							s, a).getPossibleOutcomes()) {
						term += o.weight
								* (V.lake.getReward(s, a, o.state) + discount
										* V.getValue(o.state));
					}
					if (term > max) {
						max = term;
						best = a;
					}
				}
				pi[s.hPos()][s.vPos()] = best;
			}
		}

		Policy p = new Policy() {

			public Action getAction(State s) {
				return pi[s.hPos()][s.vPos()];
			}
		};
		return p;
	}
}