package lyzzcw.work.leaf.core;


import lyzzcw.work.leaf.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
