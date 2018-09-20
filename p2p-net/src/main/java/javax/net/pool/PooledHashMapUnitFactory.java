package javax.net.pool;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.p2p.map.HashMapUnit256;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class PooledHashMapUnitFactory<K, V> extends BasePooledObjectFactory<HashMapUnit256<K, V>> {

    private static GenericObjectPool<HashMapUnit256> UNIT_POOL;

    static {
        try {
            GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
            //conf.setMinIdle(1000);
            //conf.setMaxIdle(10000);
            //conf.setMaxTotal(-1);
            UNIT_POOL = new GenericObjectPool<>(new PooledHashMapUnitFactory(), conf);
        } catch (Exception ex) {
            Logger.getLogger(PooledHashMapUnitFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public HashMapUnit256<K, V> create() throws Exception {
        return new HashMapUnit256<>();
    }

    @Override
    public PooledObject<HashMapUnit256<K, V>> wrap(HashMapUnit256<K, V> tab) {
        return new DefaultPooledObject<>(tab);
    }

    @Override
    public void passivateObject(PooledObject<HashMapUnit256<K, V>> p) throws Exception {
        //Arrays.fill(p.getObject(), null);
        HashMapUnit256<K, V> unit = p.getObject();
        Object[] tab = unit.getTable();
        for (int i = 0; i < tab.length; i++) {
            if (null != tab[i]) {
                if (tab[i].getClass() == HashMapUnit256.class) {
                    UNIT_POOL.returnObject((HashMapUnit256) tab[i]);
                } else {
                    tab[i] = null;
                }
            }
        }
    }

    @Override
    public void destroyObject(PooledObject<HashMapUnit256<K, V>> p) throws Exception {

    }

    public static HashMapUnit256 borrowObject() {
        try {
            return UNIT_POOL.borrowObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static void returnObject(HashMapUnit256<?, ?> unit) {
        UNIT_POOL.returnObject(unit);
    }

    public static void main(String[] args) throws Exception {
        UNIT_POOL.preparePool();
        System.out.println(UNIT_POOL.getCreatedCount());
    }
}
