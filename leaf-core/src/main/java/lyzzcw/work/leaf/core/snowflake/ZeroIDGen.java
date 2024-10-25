package lyzzcw.work.leaf.core.snowflake;


import lyzzcw.work.component.domain.common.entity.Result;
import lyzzcw.work.leaf.core.IDGen;

public class ZeroIDGen implements IDGen {
    @Override
    public Result get() {
        return Result.ok(0L);
    }

    @Override
    public Result get(String key) {
        return Result.ok(0L);
    }

    @Override
    public boolean init() {
        return true;
    }
}
