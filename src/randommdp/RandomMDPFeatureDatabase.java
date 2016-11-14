package randommdp;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import policysearch.FeatureDatabase;
import randommdp.state.RandomMDPState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ngopalan on 11/13/16.
 */
public class RandomMDPFeatureDatabase implements FeatureDatabase {


    List<ActionType> atList = new ArrayList<ActionType>();
    HashMap<String, Integer> atHash = new HashMap<String, Integer>();
    int beliefVectorSize = 0;
    int states = 0;

    public RandomMDPFeatureDatabase(List<ActionType> atList, int states) {
        this.atList = atList;
        this.states = states;
        int count =0;
        for(ActionType at:atList){
            atHash.put(at.typeName(),count);
            count++;
        }
        beliefVectorSize = atList.size()*(states);
//        beliefVectorSize = atList.size()*(4);
    }

    @Override
    public double[] getActionFeaturesSets(State s, Action a) {
        double[] featureVector = new double[beliefVectorSize];
        int actionIndex = atHash.get(a.actionName());
        int i =((RandomMDPState) s).statenum.number;
//        String binString = Integer.toBinaryString(i);
//        for(int j = 0, n = binString.length() ; j < n ; j++) {
//            char c = binString.charAt(j);
//            if(c=='1'){
//                featureVector[actionIndex*(4)+j]=1.;
//            }
//        }
        featureVector[actionIndex*(states)+i]=1.;
//        featureVector[actionIndex*(height+width)+width+agent.y]=1.;
        return featureVector;
    }

    @Override
    public int getSizeOfBeliefFeatureVector() {
        return beliefVectorSize;
    }
}
