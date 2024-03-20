package cc.yezj.rpc.core.registry;

import cc.yezj.rpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    List<InstanceMeta> data;
}
