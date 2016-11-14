package randommdp;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyEvaluation;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;

import java.util.List;

/**
 * Created by ngopalan on 11/9/16.
 */
public class PolicyEvaluationFromAllStates extends PolicyEvaluation {
    public PolicyEvaluationFromAllStates(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxEvalDelta, double maxEvalIterations) {
        super(domain, gamma, hashingFactory, maxEvalDelta, maxEvalIterations);
    }

    public void evaluatePolicy(List<State> states, EnumerablePolicy p){
        for(State s :states){
            this.performReachabilityFrom(s);
        }
        evaluatePolicy(p);
    }

}
