import java.util.HashMap;
import java.math.BigInteger;
public class DynamicMetrics {
    private long method_count = 0;
    private HashMap<String, BigInteger> method_map = new HashMap<>();
    private HashMap<String, BigInteger> method_bb_map = new HashMap<>();
    private HashMap<String, BigInteger> method_call_map = new HashMap<>();
    private long bb_count = 0;
    private long instr_count = 0;

    public synchronized void incMethodCount(long i) { method_count+=i; }
    public synchronized void incMethodCount(long i, String routine_name) { method_count+=i;
    BigInteger count = BigInteger.ZERO;
    if (method_map.containsKey(routine_name)) count = method_map.get(routine_name);
        method_map.put(routine_name, count.add(BigInteger.valueOf(i)));}
    public synchronized void incBBCount(long i) { bb_count+=i; }
    public synchronized void incBBCount(long i, String routine_name) { bb_count+=i;
        BigInteger count = BigInteger.ZERO;
        if (method_bb_map.containsKey(routine_name)) count = method_bb_map.get(routine_name);
        method_bb_map.put(routine_name, count.add(BigInteger.valueOf(i)));; }
    public synchronized void incIntrCount(long i) { instr_count+=i; }

    public long getMethodCount() { return method_count; }
    public long getBBCount() { return bb_count; }
    public long getInstrCount() { return instr_count; }
    public HashMap<String, BigInteger> getMethodMap() { return method_map; }
    public HashMap<String, BigInteger> getMethodBBMap() { return method_bb_map; }
    public HashMap<String, BigInteger> getMethodCallMap() { return method_call_map; }

    public void incCallCount(String callercallee) {
        BigInteger count = BigInteger.ZERO;
        if (method_call_map.containsKey(callercallee)) {
            count = method_call_map.get(callercallee);
        }

        method_call_map.put(callercallee, count.add(BigInteger.ONE));
    }
}
