package randommdp;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.*;


//public class VITutorial extends MDPSolver implements Planner, QProvider {
//
//    protected Map<HashableState, Double> valueFunction;
//    protected ValueFunction vinit;
//    protected int numIterations;
//
//
//    public VITutorial(SADomain domain, double gamma,
//                      HashableStateFactory hashingFactory, ValueFunction vinit, int numIterations){
//        this.solverInit(domain, gamma, hashingFactory);
//        this.vinit = vinit;
//        this.numIterations = numIterations;
//        this.valueFunction = new HashMap<HashableState, Double>();
//    }
//
//    @Override
//    public double value(State s) {
//        Double d = this.valueFunction.get(hashingFactory.hashState(s));
//        if(d == null){
//            return vinit.value(s);
//        }
//        return d;
//    }
//
//    @Override
//    public List<QValue> qValues(State s) {
//        List<Action> applicableActions = this.applicableActions(s);
//        List<QValue> qs = new ArrayList<QValue>(applicableActions.size());
//        for(Action a : applicableActions){
//            qs.add(new QValue(s, a, this.qValue(s, a)));
//        }
//        return qs;
//    }
//
//    @Override
//    public double qValue(State s, Action a) {
//
//        if(this.model.terminal(s)){
//            return 0.;
//        }
//
//        //what are the possible outcomes?
//        List<TransitionProb> tps = ((FullModel)this.model).transitions(s, a);
//
//        //aggregate over each possible outcome
//        double q = 0.;
//        for(TransitionProb tp : tps){
//            //what is reward for this transition?
//            double r = tp.eo.r;
//
//            //what is the value for the next state?
//            double vp = this.valueFunction.get(this.hashingFactory.hashState(tp.eo.op));
//
//            //add contribution weighted by transition probability and
//            //discounting the next state
//            q += tp.p * (r + this.gamma * vp);
//        }
//
//        return q;
//    }
//
//    @Override
//    public GreedyQPolicy planFromState(State initialState) {
//
//        HashableState hashedInitialState = this.hashingFactory.hashState(initialState);
//        if(this.valueFunction.containsKey(hashedInitialState)){
//            return new GreedyQPolicy(this); //already performed planning here!
//        }
//
//        //if the state is new, then find all reachable states from it first
//        this.performReachabilityFrom(initialState);
//
//        //now perform multiple iterations over the whole state space
//        for(int i = 0; i < this.numIterations; i++){
//            //iterate over each state
//            for(HashableState sh : this.valueFunction.keySet()){
//                //update its value using the bellman equation
//                this.valueFunction.put(sh, QProvider.Helper.maxQ(this, sh.s()));
//            }
//        }
//
//        return new GreedyQPolicy(this);
//
//    }
//
//    @Override
//    public void resetSolver() {
//        this.valueFunction.clear();
//    }
//
//    public void performReachabilityFrom(State seedState){
//
//        Set<HashableState> hashedStates = StateReachability.getReachableHashedStates(seedState, this.domain, this.hashingFactory);
//
//        //initialize the value function for all states
//        for(HashableState hs : hashedStates){
//            if(!this.valueFunction.containsKey(hs)){
//                this.valueFunction.put(hs, this.vinit.value(hs.s()));
//            }
//        }
//
//    }
//
//
//    public static void main(String [] args){
//
//        GridWorldDomain gwd = new GridWorldDomain(11, 11);
//        gwd.setTf(new GridWorldTerminalFunction(10, 10));
//        gwd.setMapToFourRooms();
//
//        //only go in intended directon 80% of the time
//        gwd.setProbSucceedTransitionDynamics(0.8);
//
//        SADomain domain = gwd.generateDomain();
//
//        //get initial state with agent in 0,0
//        State s = new GridWorldState(new GridAgent(0, 0));
//
//        //setup vi with 0.99 discount factor, a value
//        //function initialization that initializes all states to value 0, and which will
//        //run for 30 iterations over the state space
//        VITutorial vi = new VITutorial(domain, 0.99, new SimpleHashableStateFactory(),
//                new ConstantValueFunction(0.0), 30);
//
//        //run planning from our initial state
//        Policy p = vi.planFromState(s);
//
//        //evaluate the policy with one roll out visualize the trajectory
//        Episode ea = PolicyUtils.rollout(p, s, domain.getModel());
//
//        Visualizer v = GridWorldVisualizer.getVisualizer(gwd.getMap());
//        new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));
//
//    }
//
//}

//    Full Q-Learning Code

        import burlap.behavior.policy.EpsilonGreedy;
        import burlap.behavior.policy.Policy;
        import burlap.behavior.singleagent.Episode;
        import burlap.behavior.singleagent.MDPSolver;
        import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
        import burlap.behavior.singleagent.learning.LearningAgent;
        import burlap.behavior.valuefunction.ConstantValueFunction;
        import burlap.behavior.valuefunction.QFunction;
        import burlap.behavior.valuefunction.QProvider;
        import burlap.behavior.valuefunction.QValue;
        import burlap.domain.singleagent.gridworld.GridWorldDomain;
        import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
        import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
        import burlap.domain.singleagent.gridworld.state.GridAgent;
        import burlap.domain.singleagent.gridworld.state.GridWorldState;
        import burlap.mdp.core.action.Action;
        import burlap.mdp.core.state.State;
        import burlap.mdp.singleagent.SADomain;
        import burlap.mdp.singleagent.environment.Environment;
        import burlap.mdp.singleagent.environment.EnvironmentOutcome;
        import burlap.mdp.singleagent.environment.SimulatedEnvironment;
        import burlap.statehashing.HashableState;
        import burlap.statehashing.HashableStateFactory;
        import burlap.statehashing.simple.SimpleHashableStateFactory;
        import burlap.visualizer.Visualizer;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class QLTutorial extends MDPSolver implements LearningAgent, QProvider {

    Map<HashableState, List<QValue>> qValues;
    QFunction qinit;
    double learningRate;
    Policy learningPolicy;

    public QLTutorial(SADomain domain, double gamma, HashableStateFactory hashingFactory,
                      QFunction qinit, double learningRate, double epsilon){

        this.solverInit(domain, gamma, hashingFactory);
        this.qinit = qinit;
        this.learningRate = learningRate;
        this.qValues = new HashMap<HashableState, List<QValue>>();
        this.learningPolicy = new EpsilonGreedy(this, epsilon);

    }

    @Override
    public Episode runLearningEpisode(Environment env) {
        return this.runLearningEpisode(env, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        //initialize our episode object with the initial state of the environment
        Episode e = new Episode(env.currentObservation());

        //behave until a terminal state or max steps is reached
        State curState = env.currentObservation();
        int steps = 0;
        while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

            //select an action
            Action a = this.learningPolicy.action(curState);

            //take the action and observe outcome
            EnvironmentOutcome eo = env.executeAction(a);

            //record result
            e.transition(eo);

            //get the max Q value of the resulting state if it's not terminal, 0 otherwise
            double maxQ = eo.terminated ? 0. : this.value(eo.op);

            //update the old Q-value
            QValue oldQ = this.storedQ(curState, a);
            oldQ.q = oldQ.q + this.learningRate * (eo.r + this.gamma * maxQ - oldQ.q);


            //update state pointer to next environment state observed
            curState = eo.op;
            steps++;

        }

        return e;
    }

    @Override
    public void resetSolver() {
        this.qValues.clear();
    }

    @Override
    public List<QValue> qValues(State s) {
        //first get hashed state
        HashableState sh = this.hashingFactory.hashState(s);

        //check if we already have stored values
        List<QValue> qs = this.qValues.get(sh);

        //create and add initialized Q-values if we don't have them stored for this state
        if(qs == null){
            List<Action> actions = this.applicableActions(s);
            qs = new ArrayList<QValue>(actions.size());
            //create a Q-value for each action
            for(Action a : actions){
                //add q with initialized value
                qs.add(new QValue(s, a, this.qinit.qValue(s, a)));
            }
            //store this for later
            this.qValues.put(sh, qs);
        }

        return qs;
    }

    @Override
    public double qValue(State s, Action a) {
        return storedQ(s, a).q;
    }


    protected QValue storedQ(State s, Action a){
        //first get all Q-values
        List<QValue> qs = this.qValues(s);

        //iterate through stored Q-values to find a match for the input action
        for(QValue q : qs){
            if(q.a.equals(a)){
                return q;
            }
        }

        throw new RuntimeException("Could not find matching Q-value.");
    }

    @Override
    public double value(State s) {
        return QProvider.Helper.maxQ(this, s);
    }


    public static void main(String[] args) {

        GridWorldDomain gwd = new GridWorldDomain(11, 11);
        gwd.setMapToFourRooms();
        gwd.setProbSucceedTransitionDynamics(0.8);
//        gwd.setTf(new GridWorldTerminalFunction(10, 10));

        RewardFunction rf = new RewardFunction() {
            @Override
            public double reward(State state, Action action, State state1) {
                GridWorldState s = (GridWorldState)state1;
                if(s.agent.x==10 && s.agent.y==10){return 1.;}
                return 0;
            }
        };
        gwd.setRf(rf);
        SADomain domain = gwd.generateDomain();

        //get initial state with agent in 0,0
        State s = new GridWorldState(new GridAgent(0, 0));

        //create environment
        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);

        //create Q-learning
        QLTutorial agent = new QLTutorial(domain, 0.99, new SimpleHashableStateFactory(),
                new ConstantValueFunction(), 0.1, 0.1);

        //run Q-learning and store results in a list
        List<Episode> episodes = new ArrayList<Episode>(1000);
        for(int i = 0; i < 1000; i++){
            System.out.println(i);
            episodes.add(agent.runLearningEpisode(env,100));
            env.resetEnvironment();
        }

        Visualizer v = GridWorldVisualizer.getVisualizer(gwd.getMap());
        new EpisodeSequenceVisualizer(v, domain, episodes);

    }

}

