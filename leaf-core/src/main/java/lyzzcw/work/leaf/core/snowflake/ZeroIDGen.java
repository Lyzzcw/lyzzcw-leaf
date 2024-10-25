package lyzzcw.work.leaf.core.snowflake;


import lyzzcw.work.leaf.core.IDGen;

public class ZeroIDGen implements IDGen {
    @Override
    public long get() {
        return 0L;
    }

    @Override
    public long get(String key) {
        return 0L;
    }

    @Override
    public boolean init() {
        return true;
    }
}
