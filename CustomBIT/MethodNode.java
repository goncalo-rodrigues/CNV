import sun.security.util.BigInt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

/**
 * Created by goncalo on 13-04-2017.
 */
public class MethodNode {
    public HashMap<MethodNode, BigInteger> parents = new HashMap<>();
    public List<MethodNode> children = new ArrayList<>();
    public double value = 0;
    public String name;
    public boolean visited = false;
    public BigInteger timesExecuted = BigInteger.ZERO;
    public BigInteger executedOthers = BigInteger.ZERO;
    public MethodNode(String name) {
        this.name = name;
    }
    public void visit(double val, MethodNode child) {

        if (child != null && !children.contains(child)) return;
        if (child != null) {
            children.remove(child);
        }
        this.value += val;

        this.visited = true;
        System.out.println(name + " distributing " + this.value + " timesexec: " + timesExecuted);
        BigInteger tempTimesExec = timesExecuted;
        // prevent recursion
//        for(Map.Entry<MethodNode, BigInteger> entry: parents.entrySet()) {
//            if (entry.getKey().visited) {
//                tempTimesExec = tempTimesExec.subtract(entry.getValue());
//            }
//
//        }
        // redistribute percentage for every parent
        for(Map.Entry<MethodNode, BigInteger> entry: parents.entrySet()) {
//            if (!entry.getKey().visited) {
                entry.getKey().visit((new BigDecimal(entry.getValue()).divide(new BigDecimal(tempTimesExec), MathContext.DECIMAL32)).doubleValue() * value, this);
//            }
        }
    }
    public void addChild(MethodNode child, BigInteger numExecs) {
        children.add(child);
        executedOthers = executedOthers.add(numExecs);
    }

    public void addParent(MethodNode parent, BigInteger numExecs) {
        parents.put(parent, numExecs);
    }

    public void incrementTimesExecuted(BigInteger cnt) {
        timesExecuted = timesExecuted.add(cnt);
    }

    public List<MethodNode> getTopChildren(double percentile) {
        if (children.size() == 0) return new ArrayList<>();
        Collections.sort(children, new Comparator<MethodNode>() {
            @Override
            public int compare(MethodNode o1, MethodNode o2) {
                return - o1.parents.get(MethodNode.this).compareTo(o2.parents.get(MethodNode.this));
            }
        });
        double currentP = 0;
        BigInteger currentV = BigInteger.ZERO;
        int i = 0;
        List<MethodNode> result = new ArrayList<>();
        while (currentP < percentile) {
            result.add(children.get(i));
            currentV = currentV.add(children.get(i).parents.get(this));
            currentP = new BigDecimal(currentV).divide(new BigDecimal(executedOthers), MathContext.DECIMAL32).doubleValue();
            i+=1;
        }
        return result;
    }

}
