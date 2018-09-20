package javax.net.p2p.set;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.codec.binary.Hex;
//import javax.net.pool.PooledHashMapUnitFactory;

/**
 * @author Karl Jinkai 2010-07-10
 * @param <K>
 * @info 一个专为大规模数据的快速存储和检索而设计的分布式哈希表类， 可提供基于32字节长哈希键值对的近似常数时间的寻位存储和定位检索。
 * 寻位和定位算法基于键对象之哈希值的每8个二进制位的状态所构造的多叉（256）分层（最多32层）树。 <br>
 *
 */
public class NewHashSet<K> extends AbstractSet<K> implements Set<K>, Serializable {

    private static final long serialVersionUID = 20100710L;
    private BigInteger size = BigInteger.ZERO;

    private final SetUnit<K> map = new SetUnit<>();

//    public HashMap256() {
//        this.map = PooledHashMapUnitFactory.borrowObject();
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        PooledHashMapUnitFactory.returnObject(map);
//    }


    //自定义序列化操作：
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();

    }

    @Override
    public void clear() {
        size = BigInteger.ZERO;
        map.clear();

    }

    @Override
    public boolean contains(Object key) {
        if (key == null) {
            return false;
        }
        return map.get(key);
    }

    @Override
    public final Iterator<K> iterator() {
        return new BaseIterator(map);
    }

    @Override
    public boolean isEmpty() {

        return size.equals(BigInteger.ZERO);
    }

//    @Override
//    public Set keySet() {
//        Set<K> set = new HashSet<K>(size);
//        set.addAll(this.listKeys());
//        return set;
//    }

//    public List<K> listKeys() {
//        ArrayList<K> resultKey = new ArrayList<K>(size);
//        map.keyList(resultKey);
//        return resultKey;
//    }

    @Override
    public boolean add(Object key) {
        if (key == null) {
            return false;
        }
        boolean v = map.add((K) key);
        if (v) {
            size = size.add(BigInteger.ONE);
        }
        return v;
    }

    @Override
    public boolean addAll(Collection<? extends K> m) {
        Iterator it = m.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (map.add((K) it.next())) {
                changed = true;
            }

        }
        return changed;
    }

    @Override
    public boolean remove(Object key) {
        if (key == null) {
            return false;
        }
        boolean v = map.remove((K) key);
        if (v) {
            size = size.subtract(BigInteger.ONE);
        }
        return v;

    }

    @Override
    public int size() {
        return size.intValue();
    }

    public BigInteger realSize() {
        return size;
    }

//    @Override
//    public Collection values() {
//        ArrayList<V> resultValue = new ArrayList<>(size);
//        for (K kv : listEntry()) {
//            resultValue.add(kv.value);
//        }
//        return resultValue;
//    }

    /**
     * Base of key, value, and entry Iterators. Adds fields to Traverser to
     * support iterator.remove.
     */
    static class BaseIterator<K> implements Iterator<K> {

        SetUnit[] layers = new SetUnit[32];
        int[] layerIndexs = new int[32];
        Object[] table;        // current table; updated if resized
        K next;         // the next entry to use
        Iterator<K> nextIterator;
        int layer = 0;              // index of bin to use next

        BaseIterator(SetUnit<K> map) {
            this.table = map.getTable();
            layerIndexs[layer] = -1;
        }

        public final boolean hasNext() {
            if (nextIterator != null) {
                if (nextIterator.hasNext()) {
                    next = nextIterator.next();
                } else {
                    nextIterator = null;
                }
            }
            int index = ++layerIndexs[layer];
            if (table[index] == null) {
                if (index == 255) {
                    if (layer == 0) {//嵌套终止
                        next = null;
                        return false;
                    }
                    layer--;//返回上一层
                    return this.hasNext();
                }
                //layerIndexs[layer]++;
                return this.hasNext();
            } else {
                if (table[index].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[index];
                    table = mmu.getTable();
                    layers[layer] = mmu;
                    //layerIndexs[layer]++;
                    layer++;
                    layerIndexs[layer] = -1;
                    return this.hasNext();
                } else {
                    if (table[index].getClass() == LinkedList.class) {//最底层特殊处理
                        //处理多重哈希冲突
                        LinkedList<K> list = ((LinkedList<K>) table[index]);
                        nextIterator = list.iterator();
                        if (nextIterator.hasNext()) {
                            next = nextIterator.next();
                        } else {
                            next = null;
                        }
                        if (index == 255) {//嵌套终止
                            //return next != null;
                            layer--;//返回上一层
                            return this.hasNext();
                        }
                    } else {
                        next = (K) table[index];
                    }
                }
            }
            return next != null;
        }

        public final boolean hasMoreElements() {
            return this.hasNext();
        }

        public final void remove() {
            if (nextIterator != null) {
                nextIterator.remove();
            } else {
                table[layerIndexs[layer]] = null;
            }
        }

        @Override
        public K next() {
            return next;
        }
    }

    //以下是扩展功能：
//    /**
//     * 获得Map容器自然排序的最小元素
//     *
//     * @return　最小元素
//     */
//    public K min() {
//        if (size == 0) {
//            return null;
//        }
//        loadCache();
//        return resultEntry.get(0);
//    }
//
//    /**
//     * 获得Map容器自然排序的最大元素
//     *
//     * @return　最大元素
//     */
//    public K max() {
//        if (size == 0) {
//            return null;
//        }
//        loadCache();
//        return resultEntry.get(resultEntry.size() - 1);
//    }
//
//    /**
//     * 获得Map容器自然排序的有序子集
//     *
//     * @param int fromIndex, int toIndex
//     * @return　子集列表
//     */
//    public List<K> subList(int begin, int end) {
//        if (size == 0) {
//            return null;
//        }
//        loadCache();
//        return resultEntry.subList(begin, end);
//    }
//
//    /**
//     * 获得Map容器自然排序的从指定位置到最末尾有序子集
//     *
//     * @param int fromIndex
//     * @return　子集列表
//     */
//    public List<K> subList(int begin) {
//        if (size == 0) {
//            return null;
//        }
//        loadCache();
//        return resultEntry.subList(begin, resultEntry.size() - 1);
//    }
//
//    /**
//     * 获得Map容器指定键的位置顺序号
//     *
//     * @param int fromIndex
//     * @return　子集列表
//     */
//    public int indexOf(K obj) {
//        if (size == 0) {
//            return -1;
//        }
//        loadCache();
//        return resultKey.indexOf(obj);
//    }
//
//    /**
//     * 获得Map容器指定键的位置倒序号
//     *
//     * @param int fromIndex
//     * @return　子集列表
//     */
//    public int lastIndexOf(K obj) {
//        if (size == 0) {
//            return -1;
//        }
//
//        return resultKey.lastIndexOf(obj);
//    }
    /**
     * 获得Map容器内哈希冲突计数
     *
     * @return　冲突计数
     */
    public int getConflictCount() {
        return map.conflictCount;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 255; i++) {
            System.out.println((i + 128) & 0xff);
        }
        Set<Integer> set = new NewHashSet();
        for (int i = 0; i < 10; i++) {
            set.add(new Random().nextInt());
        }
        System.out.println(set.size());
        for (Integer i : set) {
            System.out.println(i);
        }

    }
}
