package randommdp.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;


import java.util.Arrays;
import java.util.List;

/**
 * Created by ngopalan on 11/8/16.
 */
public class RandomMDPState implements MutableOOState{

    public NumberObjectClass statenum;

    public static final String CLASS_NUM = "StateNumber";

    public RandomMDPState(int num){statenum = new NumberObjectClass(CLASS_NUM+0, num);}

    @Override
    public MutableOOState addObject(ObjectInstance objectInstance) {
        throw new RuntimeException("Cannot add objects to random state");
    }

    @Override
    public MutableOOState removeObject(String s) {
        throw new RuntimeException("Cannot remove objects from random state");
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Cannot rename objects: " + objectName);
    }

    @Override
    public int numObjects() {
        return 1;
    }

    @Override
    public ObjectInstance object(String objectName) {
        if(objectName.equals(CLASS_NUM)){
            return statenum.copy();
        }
        throw new RuntimeException("not an object in RandomMDPState");
    }

    @Override
    public List<ObjectInstance> objects() {
        return Arrays.<ObjectInstance>asList(statenum.copy());
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if(oclass.equals(CLASS_NUM)){
            return Arrays.<ObjectInstance>asList(statenum.copy());
        }
        return null;
    }

    @Override
    public MutableState set(Object variableKey, Object value) {

        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(statenum.name())) {
            if (key.obVarKey.equals(NumberObjectClass.NUM)) {
                return new RandomMDPState((Integer) value);
            }
        }

        throw new RuntimeException("Unknown variable key " + variableKey);
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if(key.obName.equals(statenum.name())){
            return statenum.get(key.obVarKey);
        }
        throw new RuntimeException("Cleanup State : cannot find object " + key.obName);
    }

    @Override
    public State copy() {
        return new RandomMDPState(statenum.number);
    }


    public class NumberObjectClass implements ObjectInstance{

        public String name;
        public Integer number;
        public static final String NUM = "number";

        public final List<Object> keys = Arrays.<Object>asList(NUM);


        public NumberObjectClass(String s, Integer i){
            number =i;
            name = s;
        }

        @Override
        public String className() {
            return CLASS_NUM;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public ObjectInstance copyWithName(String s) {
            return new NumberObjectClass(s, number);
        }

        @Override
        public List<Object> variableKeys() {
            return keys;
        }

        @Override
        public Object get(Object variableKey) {
            if(!(variableKey instanceof String)){
                throw new RuntimeException("RandomMDPState variable key must be a string");
            }

            String key = (String)variableKey;
            if(key.equals(NUM)){
                return number;
            }
            throw new RuntimeException("Unknown key for RandomMDPState: " + key);
        }

        @Override
        public NumberObjectClass copy() {
            return new NumberObjectClass(name, number);
        }
    }
}
