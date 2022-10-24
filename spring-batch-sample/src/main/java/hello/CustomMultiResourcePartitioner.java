package hello;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class CustomMultiResourcePartitioner implements Partitioner {

    private static final String PARTITION_KEY = "partition";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
//        int batchSize = 1000/gridSize + 1;
//        int offset = 0, i = 0;
//        while (offset < 1000){
//            ExecutionContext context = new ExecutionContext();
//            context.putInt("offset", offset );
//            context.putInt("size", batchSize );
//            map.put(PARTITION_KEY + i, context);
//            offset += batchSize;
//            i++;
//        }
        int batchSize = 40;
        int offset = 0, i = 0;
        while (offset < 1000){
            ExecutionContext context = new ExecutionContext();
            context.putInt("offset", offset );
            context.putInt("size", batchSize );
            map.put(PARTITION_KEY + i, context);
            offset += batchSize;
            i++;
        }
        return map;
    }

}