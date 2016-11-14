package randommdp;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import randommdp.state.RandomMDPState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 11/9/16.
 */
public class RandomStateRF implements RewardFunction {
    double[][] rewardsTrue;
    Random rand;
    int numStates;
    int numActions;

    public RandomStateRF(Random rand, int numState, int numActions) {

        this.rand = rand;
        this.numStates = numState;
        this.numActions = numActions;
        createRewards();
    }

    @Override
    public double reward(State s, Action a, State state1) {

        Integer actionIndex = Integer.parseInt(a.actionName());
        Integer stateIndex = ((RandomMDPState)s).statenum.number;
        return rewardsTrue[stateIndex][actionIndex];

    }

    private void createRewards() {

        this.rewardsTrue = new double[numStates][numActions];

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numActions; j++) {
                // sample a random k from the previous list and remove the element

                List<Integer> kList = new ArrayList<Integer>();
                for (int c = 0; c < 10; c++) {
                    kList.add(c);
                }
                // assign a random reward
                rewardsTrue[i][j] = rand.nextDouble();
            }
        }


    }
}
