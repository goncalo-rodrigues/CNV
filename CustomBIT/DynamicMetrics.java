public class DynamicMetrics {
    private long method_count = 0;
    private long bb_count = 0;
    private long instr_count = 0;

    public synchronized void incMethodCount(long i) { method_count+=i; }
    public synchronized void incBBCount(long i) { bb_count+=i; }
    public synchronized void incIntrCount(long i) { instr_count+=i; }

    public long getMethodCount() { return method_count; }
    public long getBBCount() { return bb_count; }
    public long getInstrCount() { return instr_count; }
}
