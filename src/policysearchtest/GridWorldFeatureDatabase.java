package policysearchtest;

import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import policysearch.FeatureDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ngopalan on 11/12/16.
 */
public class GridWorldFeatureDatabase implements FeatureDatabase {


    List<ActionType> atList = new ArrayList<ActionType>();
    HashMap<String, Integer> atHash = new HashMap<String, Integer>();
    int beliefVectorSize = 0;
    int height = 0;
    int width = 0;

    public GridWorldFeatureDatabase(List<ActionType> atList, int height, int width) {
        this.atList = atList;
        this.height = height;
        this.width = width;
        int count =0;
        for(ActionType at:atList){
            atHash.put(at.typeName(),count);
            count++;
        }
        beliefVectorSize = atList.size()*(height+width);
    }

    @Override
    public double[] getActionFeaturesSets(State s, Action a) {
        double[] featureVector = new double[beliefVectorSize];
        int actionIndex = atHash.get(a.actionName());
        GridAgent agent = ((GridWorldState)s).agent;
        featureVector[actionIndex*(height+width)+agent.x]=1.;
        featureVector[actionIndex*(height+width)+width+agent.y]=1.;
        return featureVector;
    }

    @Override
    public int getSizeOfBeliefFeatureVector() {
        return beliefVectorSize;
    }
}
