package policysearchtest;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import policysearch.PolicySearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 11/12/16.
 */
public class GridWorldPolicySearch {

    public static void main(String[] args) {
        int height = 5;
        final int width = 5;
        GridWorldDomain gw = new GridWorldDomain(height,width);
//        gw.setMapToFourRooms(); //four rooms layout
        gw.setProbSucceedTransitionDynamics(0.8); //stochastic transitions with 0.8 success rate
        final TerminalFunction tf = new GridWorldTerminalFunction(height-1, width-1);

        RewardFunction rf = new RewardFunction() {
            @Override
            public double reward(State state, Action action, State state1) {
                if(tf.isTerminal(state1)){
                    return 100.;
                }
                return -1;
            }
        };

        gw.setTf(tf);
        gw.setRf(rf);
        OOSADomain domain = gw.generateDomain();

        State s = new GridWorldState(new GridAgent(0, 0), new GridLocation(height-1, width-1, "loc0"));
        StateEnumerator senum = new StateEnumerator(domain, new SimpleHashableStateFactory());
        senum.findReachableStatesAndEnumerate(s);

        GridWorldFeatureDatabase gwf = new GridWorldFeatureDatabase(domain.getActionTypes(),height,width);

        PolicySearch p = new PolicySearch(domain, gwf,senum, 10);
        p.setEpsilon(10000);
        p.setLearningRate(1);


//        PolicySearch p = new PolicySearch(domain, gwf, senum, 0.01);
//        p.setEpsilon(100);
//        p.setLearningRate(1000);

        Policy policy  = p.planFromState(s);
        SimulatedEnvironment e = new SimulatedEnvironment(domain,s);
        Episode epi = PolicyUtils.rollout(policy, e);


        //create visualizer and explorer
        Visualizer v = GridWorldVisualizer.getVisualizer(gw.getMap());
        List<Episode> epiList = new ArrayList<Episode>();
        epiList.add(epi);
        new EpisodeSequenceVisualizer(v, domain,epiList);
//        VisualExplorer exp = new VisualExplorer(domain, v, s);



    }
}
