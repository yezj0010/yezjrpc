package cc.yezj.rpc.core.cluster;

import cc.yezj.rpc.core.api.Router;
import cc.yezj.rpc.core.meta.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    private int grayRatio;

    private static Random random = new Random();

    public GrayRouter(int grayRatio){
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if(CollectionUtils.isEmpty(providers) || providers.size() == 1){
            return providers;
        }

        List<InstanceMeta> gray = providers.stream().filter(i -> "true".equals(i.getParameters().get("gray"))).collect(Collectors.toList());
        List<InstanceMeta> normal = providers.stream().filter(i -> !"true".equals(i.getParameters().get("gray"))).collect(Collectors.toList());

        //保证肯定可以返回 有灰度节点才判断灰度，没有就全部返回。
        if(CollectionUtils.isEmpty(gray) || CollectionUtils.isEmpty(normal)){
            return providers;
        }

        //恢复节点筛选
        if(grayRatio <= 0){
            return normal;
        }else if(grayRatio >= 100){
            return gray;
        }else{
            int randomInt = random.nextInt(100) + 1;
            log.debug("randomInt ={}", randomInt);
            if(randomInt <= grayRatio){
                return gray;
            }else{
                return normal;
            }
        }
    }
}
