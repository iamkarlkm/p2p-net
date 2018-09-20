package javax.net.pool;
import java.util.Arrays;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class PooledObjectArrayFactory extends BasePooledObjectFactory<Object[]> {

    @Override
    public Object[] create() throws Exception {
        return new Object[256];
    }

    @Override
    public PooledObject<Object[]> wrap(Object[] tab) {
        return new DefaultPooledObject<>(tab);
    }

    @Override
    public void passivateObject(PooledObject<Object[]> p) throws Exception {
        Arrays.fill(p.getObject(), null);
    }

    @Override
    public void destroyObject(PooledObject<Object[]> p) throws Exception {

    }


    public static void main(String[] args) throws Exception {
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        conf.setMinIdle(1000);

        GenericObjectPool<Object[]> pool = new GenericObjectPool<>(new PooledObjectArrayFactory(), conf);
        pool.preparePool();
        System.out.println(pool.borrowObject()[0]);
    }
}
