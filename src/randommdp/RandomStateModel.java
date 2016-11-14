package randommdp;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import randommdp.state.RandomMDPState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ngopalan on 11/8/16.
 */
public class RandomStateModel implements FullStateModel {
    Random rand;
    int numStates;
    int numActions;
    //P(s,a,s') = double
    double[][][] transitionTrue;

    //R(s,a) = double
//    double[][] rewardsTrue;

    public RandomStateModel(Random rand, int numstates, int numActions){
        this.rand = rand;
        this.numStates = numstates;
        this.numActions= numActions;
        this.transitionTrue = new double[numstates][numActions][numstates];

        createTransitionModel();
    }

    public RandomStateModel(Random rand, int numstates, int numActions, double[][][] inputModel){
        this.rand = rand;
        this.numStates = numstates;
        this.numActions= numActions;
        this.transitionTrue = inputModel;
    }

    private void createTransitionModel() {


        for(int i=0;i<numStates;i++){
            for(int j=0;j<numActions;j++){
                // sample a random k from the previous list and remove the element

                List<Integer> kList = new ArrayList<Integer>();
                for (int c = 0; c < 10; c++) {
                    kList.add(c);
                }
                // assign a random reward
//                rewardsTrue[i][j] = rand.nextDouble();

                double sum = 0.;
                for(int x=0;x<numStates/2;x++) {
                    int location = rand.nextInt(kList.size());
                    int k = kList.get(location);
                    kList.remove(location);
                    double tempRand = rand.nextDouble();
                    transitionTrue[i][j][k] = tempRand;
                    sum+=tempRand;

                }

                for(int k=0;k<numStates;k++){
                    // normalized transition function!!
                    transitionTrue[i][j][k] = transitionTrue[i][j][k]/sum;
                }


            }
        }
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State state, Action action) {
        Integer actionIndex = Integer.parseInt(action.actionName());
        Integer stateIndex = ((RandomMDPState)state).statenum.number;
        List<StateTransitionProb> retProbList = new ArrayList<StateTransitionProb>();
        for(int k=0;k<numStates;k++){
//            System.out.println(k);
//            System.out.println(actionIndex);
//            System.out.println(stateIndex);
//            System.out.println(transitionTrue[stateIndex][actionIndex][k]);
            retProbList.add(new StateTransitionProb(new RandomMDPState(k),transitionTrue[stateIndex][actionIndex][k]));
        }
        return retProbList;
    }

    @Override
    public State sample(State state, Action action) {
        return FullStateModel.Helper.sampleByEnumeration(this,state,action);
    }



}
