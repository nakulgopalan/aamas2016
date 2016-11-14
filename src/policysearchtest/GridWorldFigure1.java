//package policysearchtest;
//
//import burlap.behavior.policy.*;
//import burlap.behavior.singleagent.Episode;
//import burlap.behavior.singleagent.auxiliary.StateEnumerator;
//import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyEvaluation;
//import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
//import burlap.behavior.valuefunction.ConstantValueFunction;
//import burlap.debugtools.DPrint;
//import burlap.debugtools.RandomFactory;
//import burlap.domain.singleagent.gridworld.GridWorldDomain;
//import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
//import burlap.domain.singleagent.gridworld.state.GridAgent;
//import burlap.domain.singleagent.gridworld.state.GridLocation;
//import burlap.domain.singleagent.gridworld.state.GridWorldState;
//import burlap.mdp.auxiliary.StateGenerator;
//import burlap.mdp.auxiliary.StateMapping;
//import burlap.mdp.auxiliary.common.NullTermination;
//import burlap.mdp.core.TerminalFunction;
//import burlap.mdp.core.action.Action;
//import burlap.mdp.core.action.ActionType;
//import burlap.mdp.core.state.State;
//import burlap.mdp.singleagent.common.UniformCostRF;
//import burlap.mdp.singleagent.environment.SimulatedEnvironment;
//import burlap.mdp.singleagent.model.FactoredModel;
//import burlap.mdp.singleagent.model.RewardFunction;
//import burlap.mdp.singleagent.oo.OOSADomain;
//import burlap.statehashing.simple.SimpleHashableStateFactory;
//import policysearch.PolicySearchWithoutDisplay;
//import randommdp.state.RandomMDPState;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
///**
// * Created by ngopalan on 11/9/16.
// */
//public class GridWorldFigure1 {
//
//    static int numStates = 25;
//    static int numActions = 4;
//
//    public static void main(String[] args) {
//
//
//        DPrint.toggleCode(837493, false);
//
//
////        int n = 2;
//        double gammaEval = 0.99;
//
//        double policyEps = 0.4;
//
//
//        double epsLearn = 0.1;
//
//        boolean policysearch = true;
//
//        double learningRate = 100.;
//        double ridgeFactor = 1.0E-5;
//
//        int[] sampleRate = {2,5,10,20};
//
//
//        for(int i =0;i<args.length;i++){
//            String str = args[i];
////            System.out.println(str);
//            if(str.equals("-l")){
//                learningRate = Double.parseDouble(args[i+1]);
//            }
//            if(str.equals("-r")){
//                ridgeFactor = Double.parseDouble(args[i+1]);
//            }
//        }
//
//        final Random rand = RandomFactory.getMapped(0);
//
//
//        int height = 5;
//        final int width = 5;
//        GridWorldDomain gw = new GridWorldDomain(height,width);
////        gw.setMapToFourRooms(); //four rooms layout
//        gw.setProbSucceedTransitionDynamics(0.8); //stochastic transitions with 0.8 success rate
//        final TerminalFunction tf = new GridWorldTerminalFunction(height-1, width-1);
//
//        RewardFunction rf = new RewardFunction() {
//            @Override
//            public double reward(State state, Action action, State state1) {
//                if(tf.isTerminal(state1)){
//                    return 100.;
//                }
//                return -1;
//            }
//        };
//
//        gw.setTf(tf);
//        gw.setRf(rf);
//        OOSADomain d = gw.generateDomain();
//
//        State s = new GridWorldState(new GridAgent(0, 0), new GridLocation(height-1, width-1, "loc0"));
//        StateEnumerator senum = new StateEnumerator(d, new SimpleHashableStateFactory());
//        senum.findReachableStatesAndEnumerate(s);
//        numStates = senum.numStatesEnumerated();
//        numActions = d.getActionTypes().size();
//
////        FactoredModel originalModel = (FactoredModel)d.getModel();
//
//
//        for(int count =0;count<4;count++) {
//            int n = sampleRate[count];
//
//
//            double[][][] sampledTransitionTable = createSampleTransitions(n, d);
//
//
//            double[][][] regularizedTransitionTable = regularize(sampledTransitionTable, policyEps);
//
//
////        double[][][] transitionTableTrue = ((RandomStateModel)originalModel).transitionTrue;
//
////        double sum = 0.;
////        for(int i=0;i<numStates;i++){
////            for(int j=0;j<numActions;j++){
////                for(int k=0;k<numStates;k++){
////                    sum+= Math.abs(sampledTransitionTable[i][j][k] - transitionTableTrue[i][j][k]);
////                }
////            }
////
////        }
//
////        System.out.println("table diff: " + sum);
//
//            RandomStateModel sampledRegularizedModel = new RandomStateModel(rand, numStates, numActions, regularizedTransitionTable);
//
//            // domain has sampled model
//            d.setModel(new FactoredModel(sampledRegularizedModel, trf, new NullTermination()));
//
//            // solve QLearning with this model with an epsilon of 0.1 with episode length 100
//
//            StateGenerator stateGen = new StateGenerator() {
//                @Override
//                public State generateState() {
//                    int i = rand.nextInt(numStates);
//                    return new RandomMDPState(i);
//                }
//            };
//
//
//            StateEnumerator senum = new StateEnumerator(d, new SimpleHashableStateFactory());
//
//
//            List<State> states = new ArrayList<State>();
//            for (int i = 0; i < numStates; i++) {
//                State s = new RandomMDPState(i);
//                states.add(s);
//                senum.findReachableStatesAndEnumerate(s);
//            }
//
//
//            // learn Q values with sampled model
//            SimulatedEnvironment env = new SimulatedEnvironment(d, stateGen);
//            EnumerablePolicy p;
//            if (!policysearch) {
//
//
//                QLTutorial agent = new QLTutorial(d, gammaEval, new SimpleHashableStateFactory(),
//                        new ConstantValueFunction(), 0.1, epsLearn);
//
//                for (int episodeNum = 0; episodeNum < 1000; episodeNum++) {
//                    agent.runLearningEpisode(env, 100);
//                    env.resetEnvironment();
//                }
//
//                p = new GreedyQPolicy(agent);
//
//            } else {
//
//                RandomMDPFeatureDatabase rmf = new RandomMDPFeatureDatabase(d.getActionTypes(), numStates);
//
//                PolicySearchWithoutDisplay ps = new PolicySearchWithoutDisplay(d, rmf, senum, ridgeFactor);
//                ps.setEpsilon(1000);
//                ps.setLearningRate(learningRate);
//
//
////        PolicySearch p = new PolicySearch(domain, gwf, senum, 0.01);
////        p.setEpsilon(100);
////        p.setLearningRate(1000);
//
//                State s = senum.getStateForEnumerationId(rand.nextInt(numStates));
//
//                p = (EnumerablePolicy) ps.planFromState(s);
////            SimulatedEnvironment e = new SimulatedEnvironment(d,s);
////            Episode epi = PolicyUtils.rollout(p, e);
//            }
//            // run an epsilon greedy policy on top of the Q-Values learned
//
////        System.out.println("was here@@!!");
//
//
////        d.setModel(new FactoredModel(originalModel.getStateModel(),originalModel.rewardFunction(), originalModel.terminalFunction()));
//
//            PolicyEvaluationFromAllStates pEvalTrain = new PolicyEvaluationFromAllStates(d, gammaEval, new SimpleHashableStateFactory(), 0.1, 10000);
//
//            DPrint.toggleCode(pEvalTrain.getDebugCode(), false);
//            pEvalTrain.evaluatePolicy(states, p);
//
//            double trainSum = 0.;
//            for (int i = 0; i < numStates; i++) {
////            System.out.println("calcNew for the noisy model training loss");
//                trainSum += pEvalTrain.value(states.get(i));
//
//            }
//
//            d.setModel(new FactoredModel(originalModel, trf, new NullTermination()));
//            PolicyEvaluationFromAllStates pEvalTest = new PolicyEvaluationFromAllStates(domainGen.generateDomain(), gammaEval, new SimpleHashableStateFactory(), 0.1, 10000);
//            DPrint.toggleCode(pEvalTest.getDebugCode(), false);
//            pEvalTest.evaluatePolicy(states, p);
//
//            double testSum = 0.;
//            for (int i = 0; i < numStates; i++) {
////            System.out.println("test loss calc!");
//                testSum += pEvalTest.value(states.get(i));
//            }
//
//
//            System.out.println(-1 * trainSum / 10);
//            System.out.println(-1 * testSum / 10);
//
//        }
//
//        System.out.println("train sum \n test sum \n learningRate = "+learningRate+ "\n ridgeFactor = "+ridgeFactor);
//
////        d.setModel(new FactoredModel(sampledRegularizedModel, originalModel.rewardFunction(), originalModel.terminalFunction()));
//
//
//
//        //create Q-learning
//
//
//        // get policy from each state
//
//        // do policy evaluation
//
////        RewardFunction envrf = new RewardFunction() {
////
////            @Override
////            public double reward(State state, Action action, State state1) {
////                return rtm.getReward(state,action) + 0.1 * rand.nextGaussian();
////            }
////        };
//
//    }
//
//    private static double[][][] regularize(double[][][] sampledTransitionTable, double epsilon) {
//        int iMax = sampledTransitionTable.length;
//        int jMax = sampledTransitionTable[0].length;
//        int kMax = sampledTransitionTable[0][0].length;
//
//        double[][][] regularizedTable = new double[iMax][jMax][kMax];
//
//
//        for(int i=0;i<iMax;i++){
//            for(int j=0;j<jMax;j++){
//                double sum = 0.;
//                for(int k=0;k<kMax;k++){
//                    regularizedTable[i][j][k] = sampledTransitionTable[i][j][k] + epsilon;
//                    sum = sum + sampledTransitionTable[i][j][k] + epsilon;
//                }
//
//                for(int k=0;k<kMax;k++){
//                    regularizedTable[i][j][k] = regularizedTable[i][j][k]/sum;
//                }
//            }
//
//        }
//
//        return regularizedTable;
//    }
//
//    private static double[][][] createSampleTransitions(int n, OOSADomain d) {
//        double[][][] newSampledTransitionTable = new double[numStates][numActions][numStates];
//
//        FactoredModel originalModel = (FactoredModel)d.getModel();
//        for(int i=0;i<numStates;i++){
//            State s = new RandomMDPState(i);
//            for (int j=0;j<numActions;j++){
//                ActionType at = d.getAction(""+j);
//                Action a = at.allApplicableActions(s).get(0);
//                for(int c=0;c<n;c++) {
//                    int k = ((RandomMDPState) originalModel.sample(s, a).op).statenum.number;
//                    newSampledTransitionTable[i][j][k] += 1./n;
//                }
//            }
//        }
//        return newSampledTransitionTable;
//    }
//
//}
