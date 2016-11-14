package policysearch;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.valuefunction.QProvider;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
//
import org.math.plot.*;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.RandomFactory;

public class PolicySearch extends MDPSolver implements Planner, QProvider, QFunction{

    private FeatureDatabase featureDatabase;
    /**learning rate	*/
    private ConstantLR alpha = new ConstantLR(.01);//0.0000000005
    /**basis function decay rate*/
    private ConstantLR gamma = new ConstantLR(0.99);
    /** storing sum of basis functions for all episodes*/
    private List<double[]> beliefStateBasisFunctions = new ArrayList<double[]>();
    //	/** storing reward sums from previous episodes */
    //	private List<Double> rewardSumsAllEpisodes = new ArrayList<Double>();
    /** storing reward sums from previous episodes and trials*/
    private List<ArrayList<Double>> rewardSumsAllEpisodesAndTrials = new ArrayList<ArrayList<Double>>();
    /**number of episode that a trial is run */
    int episodeNum = 100;
    /** number of trials before learning weights */
    int trialNum = 100;
    /** list of actions */
    List<ActionType> actionList = new ArrayList<ActionType>();
    /** list of grounded actions */
    ArrayList<Action> gaList = new ArrayList<Action>();
    /** list of weights**/
    double[] basisFunctionWeights;
    /** Random Factory Generator */
    private Random randomNumber = RandomFactory.getMapped(0);
    /** if difference in critif weight between iterations under epsilon then actor weights updated */
    private double epsilon = Math.pow(10, 1);

    private int maxEpisodeLength =100;

    //observation Terminal Function
    private TerminalFunction tf;
    private RewardFunction rf;

    private boolean test = true;
    StateEnumerator senum;

    protected double ridgeFactor=0.;

    public PolicySearch(OOSADomain d, FeatureDatabase fd, int trialNum, StateEnumerator se, double ridgeFactor){
        this.featureDatabase = fd;
        this.domain = d;
        this.rf = ((FactoredModel)d.getModel()).rewardFunction();
        this.tf = ((FactoredModel)d.getModel()).terminalFunction();
        this.episodeNum = fd.getSizeOfBeliefFeatureVector() +1;
        this.trialNum = trialNum;
        this.senum = se;
        this.ridgeFactor = ridgeFactor;
        init(d);

    }

    public PolicySearch(OOSADomain d, FeatureDatabase fd, StateEnumerator se, double ridgeFactor){
        this.featureDatabase = fd;
        this.domain = d;
        this.rf = ((FactoredModel)d.getModel()).rewardFunction();
        this.tf = ((FactoredModel)d.getModel()).terminalFunction();
        this.episodeNum = fd.getSizeOfBeliefFeatureVector()+1;
        this.ridgeFactor = ridgeFactor;
        this.senum = se;
        init(d);
    }

    public void setLearningRate(double LR){
        this.alpha.learningRate = LR;
    }

    public void setEpsilon(double epsilon){
        this.epsilon = epsilon;
    }

	/*
	@Override
	public QValue getQ(State s, AbstractGroundedAction ga) {
		// Auto-generated method stub

		return null;
	}

	@Override
	public List<QValue> getQs(State s) {
		//  Auto-generated method stub
		return null;
	}
	 */

    private void planFromBeliefStatistic(State initialState) {
        int updateCount = 0;
		Plot2DPanel criticWeightPlot = new Plot2DPanel();
		Plot2DPanel actorWeightPlot = new Plot2DPanel();

        int startStateID = senum.getEnumeratedID(initialState);

        double[] weightDiffArray = new double[this.trialNum-1];
        double[] actorWeightDiffArray = new double[this.trialNum-1];
        double[] weightCount = new double[this.trialNum-1];
        double[] actorWeightCount = new double[this.trialNum-1];
        double[] actualCriticWeights = new double[this.trialNum-1];

        // this is where all the learning happens




        ArrayList<double[]> trialWeights = new ArrayList<double[]>();
        ArrayList<double[]> actorWeights = new ArrayList<double[]>();

        RealMatrix identityMatRidgeReg = new BlockRealMatrix(this.featureDatabase.getSizeOfBeliefFeatureVector()+1,this.featureDatabase.getSizeOfBeliefFeatureVector()+1);//this is for ridge regression
        for(int i=0;i<this.featureDatabase.getSizeOfBeliefFeatureVector()+1;i++){
            double[] tempD = new double[this.featureDatabase.getSizeOfBeliefFeatureVector()+1];
            tempD[i] = ridgeFactor;
            identityMatRidgeReg.setRow(i, tempD);
        }

        for(int trialNum = 0;trialNum < this.trialNum;trialNum++){
            // storing rewards and basis functions for multiple episodes and then solving for weights, if the weights have converged then update
            //the actor weights each trial has a list episodes with rewards and basis function derivatives for episode
//			System.out.println("epiNum total " + this.episodeNum);
            double[][] trialFeatureDerivativeList = new double[this.episodeNum][this.featureDatabase.getSizeOfBeliefFeatureVector()];
            double[] trialRewardList = new double[this.episodeNum];
            for(int epiNum =0;epiNum < this.episodeNum;epiNum++){
				/*for each episode sample a start state, do an action get observation,
				update state, update belief if not terminal observation, store reward
				 */
                Double episodeR = 0.0;
                //				List<Double> epiRewards = new ArrayList<Double>();
                double[] episodeFeatureSum = new double[this.featureDatabase.getSizeOfBeliefFeatureVector()];
                State s;
                if(epiNum!=0){
                    s=senum.getStateForEnumerationId(this.randomNumber.nextInt(senum.numStatesEnumerated()));
                }
                else{
                    s=senum.getStateForEnumerationId(startStateID);
                }
                State ns = s.copy();
                int epiStepCount =0;
                while(!tf.isTerminal(ns)&& epiStepCount <this.maxEpisodeLength){
                    //					double[] tempBasisVector;

                    GaBvTuple tupleOutput = getPolicyActionAndDerivativeBeliefVector(s);
                    Action ga = tupleOutput.getGA();
                    for(int i = 0;i<this.featureDatabase.getSizeOfBeliefFeatureVector();i++){
                        episodeFeatureSum[i] = episodeFeatureSum[i] + Math.pow(this.gamma.learningRate , epiStepCount)*tupleOutput.getBV()[i];
                    }
//                    State ns = ga.executeIn(s);
                    ns = domain.getModel().sample(s,ga).op;
//                    obs = ((PODomain)this.domain).getObservationFunction().sampleObservation(ns, ga);
                    //					System.out.println(obs.getCompleteStateDescription());
                    //					System.out.println(ga.actionName());
                    //					System.out.println(ns.getCompleteStateDescription());

                    //					double prob = ((PODomain)this.domain).getObservationFunction().getObservationProbability(obs, s, ga);

                    //					System.out.println("Observation Probability for: Obs- " + obs.getCompleteStateDescription() + "\n action- " + ga.actionName() + "\n state: " + s.getCompleteStateDescription() + "\n prob: "+prob);

                    //					System.out.println("before");
                    //					for(int i = 0;i<updatebs.getBeliefVector().length;i++){
                    //						System.out.println("bf["+i + "] = " + updatebs.getBeliefVector()[i] );
                    //					}
                    //					System.out.println("after");
//                    updatebs = updatebs.getUpdatedBeliefState(obs, ga);
                    //					for(int i = 0;i<updatebs.getBeliefVector().length;i++){
                    //						System.out.println("bf["+i + "] = " + updatebs.getBeliefVector()[i] );
                    //					}
                    //					double beliefR = 0.0;
//                    for(int i=0;i<senum.numStatesEnumerated();i++){

                        double rTemp = this.rf.reward(s, ga, ns);
                        //						beliefR = beliefR + rTemp;
                        episodeR = episodeR +  Math.pow(this.gamma.learningRate , epiStepCount)* rTemp;
//                    }
                    //					epiRewards.add(beliefR);
                    s = ns.copy();
                    epiStepCount=epiStepCount+1;
                }

                trialRewardList[epiNum] = episodeR;
                trialFeatureDerivativeList[epiNum] = episodeFeatureSum;
                // adding complete episode rewards

                //				System.out.println("count: " + epiNum);
            }
            // per trial solve for weights of the critic w! save in a list test if changed if not changed by epsilon update actor weights


            //			System.out.println("here");

            //solving linear regression with a pseudo inverse




            // here we add a 1 for the constant parameter!
            double[][] featureVectorWithOnes = new double[trialFeatureDerivativeList.length][trialFeatureDerivativeList[0].length+1];
            for(int i=0;i<trialFeatureDerivativeList.length;i++){
                for(int j=0;j<trialFeatureDerivativeList[0].length;j++){
                    featureVectorWithOnes[i][j] = trialFeatureDerivativeList[i][j];
                }
                featureVectorWithOnes[i][trialFeatureDerivativeList[0].length] =1.;
            }

            RealMatrix featureMat =  MatrixUtils.createRealMatrix(featureVectorWithOnes);



            //			double[][] testTemp = featureMat.getData();
            //			for(int i=0;i<testTemp.length;i++){
            //				for(int j=0;j<testTemp[0].length;j++){
            //					System.out.println("testTemp mat["+i+"]["+j+"] = "+testTemp[i][j]);
            //				}
            //			}

            RealMatrix outputMat = MatrixUtils.createColumnRealMatrix(trialRewardList);
            //					new RealMatrix(trialFeatureDerivativeList);
            RealMatrix temp1 = featureMat.transpose().multiply(featureMat);	// A'A calculation
            // AX = Y => X = (A'A)^(-1)A'Y

            double[][] featureData = featureMat.getData();

//			for(int i=0;i<featureData .length;i++){
//				System.out.print("featureData: [");
//				for(int j=0;j<featureData [0].length;j++){
//					System.out.print(featureData[i][j] + ", ");
//				}
//				System.out.print("]\n");
//			}

            double[][] temp = temp1.getData();

//			for(int i=0;i<temp.length;i++){
//				for(int j=0;j<temp[0].length;j++){
//					System.out.println("mat["+i+"]["+j+"] = "+temp[i][j]);
//				}
//			}

            //(A'A)^(-1)A'Y
            RealMatrix tempWeightsMatrix = new QRDecomposition(temp1.add(identityMatRidgeReg))
                    .getSolver().getInverse().multiply(featureMat.transpose()).multiply(outputMat);


            double[] tempWeights = new double[featureVectorWithOnes[0].length-1];
            System.arraycopy(tempWeightsMatrix.getColumn(0), 0, tempWeights, 0, tempWeights.length);//sr.estimateRegressionParameters();

            //			RegressionResults r = sr.regress();
//			System.out.println("regression parameters: " + tempWeights.length+ " parameters added length " + trialFeatureDerivativeList[0].length + ", total samples: " +trialFeatureDerivativeList.length);

            if(trialNum > 0){
                double[] oldWeights =  trialWeights.get(trialNum-1);
                double[] oldActorWeights = actorWeights.get(trialNum-1);
                double tempSum = 0.0;
                double actorDiffSum = 0.;
                for(int i=0; i<oldWeights.length;i++){
                    tempSum = tempSum + Math.pow(tempWeights[i] - oldWeights[i], 2);
                    //					System.out.println("old weight w["+i+"] = " + oldWeights[i]);
                }

                double tempValue = Math.sqrt(tempSum);

                weightDiffArray[trialNum-1] = tempValue;
                weightCount[trialNum-1] =trialNum-1;
//				System.out.println("distance = " + Math.sqrt(tempSum));

                if(tempValue < this.epsilon){//tempValue < this.epsilon
                    updateCount = updateCount +1;
                    for(int i=0;i<this.basisFunctionWeights.length;i++){
                        actorDiffSum = actorDiffSum + this.alpha.learningRate * tempWeights[i];
//                        this.basisFunctionWeights[i] = (1-2*ridgeFactor) * this.basisFunctionWeights[i] + this.alpha.learningRate * tempWeights[i];
                        this.basisFunctionWeights[i] = this.basisFunctionWeights[i] + this.alpha.learningRate * tempWeights[i];
						System.out.println("Weight["+i+"] = " + this.basisFunctionWeights[i] );
                    }
                }
//				for(int i=0; i<this.basisFunctionWeights.length;i++){
//					actorDiffSum = actorDiffSum + Math.pow(this.basisFunctionWeights[i] - oldActorWeights[i], 2);
////					tempSum = tempSum + Math.pow(tempWeights[i] - oldWeights[i], 2);
//					//					System.out.println("old weight w["+i+"] = " + oldWeights[i]);
//				}
                actorWeightDiffArray[trialNum-1] = actorDiffSum;
                actorWeightCount[trialNum-1] =trialNum-1;
            }

            trialWeights.add(tempWeights);
            actorWeights.add(this.basisFunctionWeights);
//			System.out.println("Update count is: " + updateCount);


            //			this.rewardSumsAllEpisodesAndTrials.add(trialList);

        }

		criticWeightPlot.addLinePlot("critic weight plot", weightCount, weightDiffArray);
		actorWeightPlot.addLinePlot("weight plot", actorWeightCount, actorWeightDiffArray);

//         put the PlotPanel in a JFrame, as a JPanel
		if(this.test){
		JFrame frame = new JFrame("critic weight plot");
		frame.setSize(2000, 2000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(criticWeightPlot);
//		frame.pack();
		frame.setVisible(true);

		JFrame frame2= new JFrame("actor weight plot");
		frame2.setSize(2000, 2000);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.setContentPane(actorWeightPlot);
//		frame2.pack();
		frame2.setVisible(true);

		System.out.println("Update count is: "+updateCount);
		}


    }



    public void setEpisodeNum(int numEpi){
        this.episodeNum = numEpi;
    }

    public void setTest(boolean testVal){
        this.test = testVal;
    }

    /** this function takes in a belief state and gets the best possible action and a policy derivative for this action being chosen
     *  based on current policy parameters, no learning happening here.
     * @param s BeliefState
     * @return a tuple of GroundedAction and a double vector of derivative calculated using phi_a(bs) - \sum_{i \in A} phi_i(bs) * \pi(i|bs)
     */
    private GaBvTuple getPolicyActionAndDerivativeBeliefVector(State s){
        //		System.out.println("getting in");
        double[] policyProbArray = new double[this.gaList.size()];
        //		System.out.println("size of action list" + this.gaList.size());
        ArrayList<double[]> basisFunctionArray = new ArrayList<double[]>();
        //		double[] bsVector  = bs.getBeliefVector();
        //		for(int i = 0;i<bsVector.length;i++){
        //			System.out.println("bsVector index: " + i + ", value: " + bsVector[i]);
        //		}

        for(int gaIdx = 0; gaIdx <this.gaList.size();gaIdx++){
            Action ga  = this.gaList.get(gaIdx);
            double[] actionFeature = this.featureDatabase.getActionFeaturesSets(s, ga);
            basisFunctionArray.add(actionFeature.clone());
            double probSum = 0.0;
            for(int i =0;i<this.basisFunctionWeights.length;i++){
                probSum = probSum + actionFeature[i] * this.basisFunctionWeights[i];
                //				System.out.println("Basis fn actionFeature["+i+"]= " +actionFeature[i]);
            }
            double tempExp = Math.exp(probSum);
            if(Double.isInfinite(tempExp)){
                tempExp = Double.MAX_VALUE/(this.gaList.size()+1);
            }
            policyProbArray[gaIdx] = tempExp + 0.00001; // unnormalized probabilities here
        }

        //		for(int i = 0;i<policyProbArray.length;i++){
        //			System.out.println("policyProbArray index: " + i + ", value: " + policyProbArray[i]);
        //		}


        //		for(int i=0;i<this.basisFunctionWeights.length;i++){
        //			System.out.println("Basis fn weigths["+i+"]= " +this.basisFunctionWeights[i]);
        //		}

        double[] policyPDF = getPDFfromUnorm(policyProbArray);
        double[] policyCDF = getCDFfromPDF(policyPDF);
        double[] temporaryBasisFunctions = getProbabilisticSum(policyPDF, basisFunctionArray);

//		System.out.print("policyPDF Vector: [");
//
//		for(int j=0;j<policyPDF.length;j++){
//			System.out.print(policyPDF[j]+", ");
//		}
//
//		System.out.print("] \n");
//
//
//		System.out.print("temporaryBasisFunctions Vector before: [");
//
//		for(int j=0;j<temporaryBasisFunctions.length;j++){
//			System.out.print(temporaryBasisFunctions[j]+", ");
//		}
//
//		System.out.print("] \n");


        //		for(int testInt = 0;testInt<policyPDF.length;testInt++){
        //			System.out.println("unorm PolicyPDF prob value:" + policyProbArray[testInt]);
        //			System.out.println("PolicyPDF value:" + policyPDF[testInt]);
        //			System.out.println("PolicyCDF value:" + policyCDF[testInt]);
        ////			System.out.println("temporaryBasisFunctions value:" + temporaryBasisFunctions[testInt]);
        //		}
        //
        double tempRand = this.randomNumber.nextDouble();
        int policyActionIdx = -1;
        for(int i=0; i<this.gaList.size();i++){
            if(tempRand<=policyCDF[i]){
                policyActionIdx = i;
                break;
            }
        }
        if(policyActionIdx==-1){
            System.err.println("NaturalBeliefCritic: getPolicyAction: Something went wrong, the action index is -1");
            System.err.println("tempRand: " + tempRand);
            for(int i=0; i<this.gaList.size();i++){
                System.out.println("policyCDF["+i+"]" + policyCDF[i]);
                System.out.println("policyPDF["+i+"]" + policyPDF[i]);
                System.out.println("policyProbArray["+i+"]" + policyProbArray[i]);
            }
            System.exit(-1);
        }
        double[] derivativeOfActionParameters = this.featureDatabase.getActionFeaturesSets(s, this.gaList.get(policyActionIdx));

//		System.out.print("derivative Vector before: [");
//
//		for(int j=0;j<derivativeOfActionParameters.length;j++){
//			System.out.print(derivativeOfActionParameters[j]+", ");
//		}
//
//		System.out.print("] \n");




        for(int j=0;j<derivativeOfActionParameters.length;j++){
            derivativeOfActionParameters[j] = derivativeOfActionParameters[j] -  temporaryBasisFunctions[j];
        }

//		System.out.print("derivative Vector after: [");
//
//		for(int j=0;j<derivativeOfActionParameters.length;j++){
//			System.out.print(derivativeOfActionParameters[j]+", ");
//		}
//
//		System.out.print("] \n");

        return new GaBvTuple(this.gaList.get(policyActionIdx), derivativeOfActionParameters);
    }

    public List<QValue> getQValueForState(State s){
        List<QValue> qValueList = new ArrayList<QValue>();
        double[] policyProbArray = new double[this.gaList.size()];
        ArrayList<double[]> basisFunctionArray = new ArrayList<double[]>();
        for(int gaIdx = 0; gaIdx <this.gaList.size();gaIdx++){
            Action ga  = this.gaList.get(gaIdx);
            double[] actionFeature = this.featureDatabase.getActionFeaturesSets(s, ga);
            basisFunctionArray.add(actionFeature.clone());
            double probSum = 0.0;
            for(int i =0;i<this.basisFunctionWeights.length;i++){
                probSum = probSum + actionFeature[i] * this.basisFunctionWeights[i];
            }
            // unnormalized probabilities here
            double tempValue = Math.exp(probSum);
            policyProbArray[gaIdx] = tempValue;
            qValueList.add(new QValue(s,ga,tempValue ));
        }
//		double[] policyPDF = getPDFfromUnorm(policyProbArray);
//		double[] policyCDF = getCDFfromPDF(policyPDF);

//		for(int i=0; i<this.gaList.size();i++){
//
//			System.out.println("action: " + this.gaList.get(i).actionName() + "policyCDF["+i+"]" + policyCDF[i]);
//			System.out.println("policyPDF["+i+"]" + policyPDF[i]);
//			System.out.println("policyProbArray["+i+"]" + policyProbArray[i]);
//		}


//		int policyActionIdx = -1;


//		double max = Double.NEGATIVE_INFINITY;
//		for(int i=0; i<this.gaList.size();i++){
//			if(max<=policyPDF[i]){
//				policyActionIdx = i;
//				max = policyPDF[i];
//			}
//		}
//




//		double tempRand = this.randomNumber.nextDouble();
//		for(int i=0; i<this.gaList.size();i++){
//			if(tempRand<=policyCDF[i]){
//				policyActionIdx = i;
//				break;
//			}
//		}
        return qValueList;
    }

    private void init(OOSADomain d){
        this.basisFunctionWeights = new double[this.featureDatabase.getSizeOfBeliefFeatureVector()];
//		for(int i = 0; i<this.basisFunctionWeights.length;i++){
//			int sign = 1;
//			if(randomNumber.nextBoolean()==true){
//				sign = -1;
//			}
//			this.basisFunctionWeights[i] = sign * 50* this.randomNumber.nextDouble();
//		}
//        StateEnumerator senum = d.getStateEnumerator();
        Set<Action> gaSet = new HashSet<Action>();
        this.actionList.addAll(d.getActionTypes());
        for(ActionType a : this.actionList){
            for(int i=0;i<senum.numStatesEnumerated();i++){
                State s = senum.getStateForEnumerationId(i);
                gaSet.addAll(a.allApplicableActions(s));
            }
        }
        this.gaList.addAll(gaSet);

    }

    private double[] getPDFfromUnorm(double[] inputUnormPDF){
        double sumOfList = sumOfList(inputUnormPDF);
        double[] listPDF = new double[inputUnormPDF.length];
        for(int i =0;i<listPDF.length;i++){
            listPDF[i] = inputUnormPDF[i]/sumOfList;
        }
        return listPDF;
    }


    private double sumOfList(double[] inputList){
        double opSum = 0.0;
        for(int i =0;i<inputList.length;i++){
            opSum = opSum + inputList[i];
        }
        return opSum;
    }

    private double[] getCDFfromPDF(double[] inputPDF){
        double sum = 0.0;
        double[] returnCDF = new double[inputPDF.length];
        for(int i =0;i<inputPDF.length;i++){
            sum = sum + inputPDF[i];
            returnCDF[i] = sum;
        }
        //TO DO: remove test structure
        //		System.out.println("size of vector" + inputPDF.length);
        //		for(int i=0;i<inputPDF.length;i++){
        //			System.out.println("CDF: " + returnCDF[i]);
        //			System.out.println("PDF: " + inputPDF[i]);
        //		}

        return returnCDF;
    }

    public double[] getProbabilisticSum(double[] inputPDF, ArrayList<double[]> inputBasisArray){
        int sizeOfBasisFn = inputBasisArray.get(0).length;
        double[] outputSum = new double[sizeOfBasisFn];
        for(int i = 0;i< inputBasisArray.size();i++){
            double[] tempList = inputBasisArray.get(i);
            for(int j=0;j<outputSum.length;j++){
                outputSum[j] = outputSum[j] + tempList[j] * inputPDF[i];
            }
        }
        return outputSum;
    }



//    @Override
//    public Policy planFromState(State state) {
//        return null;
//    }



    public class GaBvTuple{
        private final Action x;
        private final double[] y;
        //		  private final Z z;
        public GaBvTuple(Action x, double[] y) {
            this.x = x;
            this.y = y;
        }
        public Action getGA(){
            return this.x;
        }
        public double[] getBV(){
            return this.y;
        }
    }

    public class Triple<X, Y, Z> {
        private final X x;
        private final Y y;
        private final Z z;
        public Triple(X x, Y y, Z z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public X getX(){
            return this.x;
        }
        public Y getY(){
            return this.y;
        }

        public Z getZ(){
            return this.z;
        }

    }

    @Override
    public double value(State s) {
        System.err.println("NBC produces probabilistic policy, and values of states are not precise.");
        return 1.;
    }

    @Override
    public List<QValue> qValues(State s) {
        return getQValueForState((State)s);
    }

    @Override
    public double qValue(State s, Action a) {
        List<QValue> qList = getQValueForState(s);
        for(QValue q:qList){
            if(q.a.equals(a)){
                return q.q;
            }
        }
        System.err.println("Action queried is not part of the planning set: " + a.actionName());
        return 0.;
    }

    @Override
    public Policy planFromState(State initialState) {
        planFromBeliefStatistic(initialState);
        return new GreedyQPolicy(this);
    }

    @Override
    public void resetSolver() {
        this.beliefStateBasisFunctions.clear();
        this.rewardSumsAllEpisodesAndTrials.clear();
        this.basisFunctionWeights = new double[this.basisFunctionWeights.length];
    }
}