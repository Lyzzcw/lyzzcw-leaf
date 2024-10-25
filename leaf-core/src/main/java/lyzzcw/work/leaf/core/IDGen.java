package lyzzcw.work.leaf.core;


import lyzzcw.work.component.domain.common.entity.Result;

public interface IDGen {
    Result get();
    Result get(String serverId);
    boolean init();
}
