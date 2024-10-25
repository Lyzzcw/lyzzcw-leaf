package lyzzcw.work.leaf.core;


import lyzzcw.work.component.domain.common.entity.Result;

public interface IDGen {
    long get();
    long get(String serverId);
    boolean init();
}
