package randommdp;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import randommdp.state.RandomMDPState;

import java.util.Random;

/**
 * Created by ngopalan on 11/8/16.
 */
public class RandomMDPDomain implements DomainGenerator{


    Random rand;
    int numStates = 10;
    int numActions =2;

    public static final String ACTION_0 = "0";
    public static final String ACTION_1 = "1";

    public RewardFunction trf;
    public SampleStateModel rtm;

    public RandomMDPDomain(Random rand, SampleStateModel rtm, RewardFunction trf){
        this.rand = rand;
        this.rtm = rtm;
        this.trf = trf;
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain d = new OOSADomain();
        d.addStateClass(RandomMDPState.CLASS_NUM, RandomMDPState.NumberObjectClass.class);


        d.addActionTypes(
                new UniversalActionType(ACTION_0),
                new UniversalActionType(ACTION_1));


        FactoredModel model = new FactoredModel(rtm, trf, new NullTermination());
        d.setModel(model);


        return d;
    }

}
