package cc.yezj.rpc.core.api;

import cc.yezj.rpc.core.registry.Event;

public interface ChangedListener {
    void fire(Event event);
}
