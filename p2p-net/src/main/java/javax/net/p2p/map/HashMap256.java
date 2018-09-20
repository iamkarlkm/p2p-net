package javax.net.p2p.map;

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
 * @param <V>
 * @info 一个专为大规模数据的快速存储和检索而设计的分布式哈希表类， 可提供基于32字节长哈希键值对的近似常数时间的寻位存储和定位检索。
 * 寻位和定位算法基于键对象之哈希值的每8个二进制位的状态所构造的多叉（256）分层（最多32层）树。 <br>
 *
 */
public class HashMap256<K, V> extends AbstractMap<K, V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 20100710L;
    private BigInteger size = BigInteger.ZERO;
    private transient EntrySet<K, V> entrySet;

    private final HashMapUnit256<K, V> map = new HashMapUnit256<>();

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
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        V v = get(key);
        return v == null ? false : true;
    }

//    @Override
//    public Set entrySet() {
//        Set<KeyValue256<K, V>> set = new HashSet<>(size);
//        set.addAll(this.listEntry());
//        return set;
//    }
    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set
     * is backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation, or
     * through the
     * <tt>setValue</tt> operation on a map entry returned by the iterator) the
     * results of the iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations. It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet<K, V> es;
        return (es = (EntrySet<K, V>) entrySet) != null ? es : (entrySet = new EntrySet<K, V>());
    }
//    public Set<Entry<K, V>> entrySet() {
//        return (Set<Entry<K, V>>)(entrySet == null ? (entrySet = new EntrySet()) : entrySet);
//    }

    final class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

//        public EntrySet(HashMap256 instance) {
//        }
        @Override
        public final int size() {
            return size.intValue();
        }

        @Override
        public final void clear() {
            size = BigInteger.ZERO;
            map.clear();
        }

        @Override
        public final Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator(map);
        }

        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof KeyValue256)) {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            K key = e.getKey();
            V val = (V) map.get(key);
            return val != null && val.equals(e.getValue());
        }

        @Override
        public final boolean remove(Object o) {
            if (o instanceof KeyValue256) {
                KeyValue256<K, V> e = (KeyValue256<K, V>) o;
                K key = e.key;
                return map.remove(key) != null;
            }
            return false;
        }

//        public final Spliterator<Map.Entry<K, V>> spliterator() {
//            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
//        }

        @Override
        public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Iterator<Map.Entry<K, V>> it = iterator();
            while (it.hasNext()) {
                action.accept(it.next());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns a set that subclasses
     * {@link AbstractSet}. The subclass's iterator method returns a "wrapper
     * object" over this map's <tt>entrySet()</tt> iterator. The <tt>size</tt>
     * method delegates to this map's <tt>size</tt> method and the
     * <tt>contains</tt> method delegates to this map's
     * <tt>containsKey</tt> method.
     *
     * <p>
     * The set is created the first time this method is called, and returned in
     * response to all subsequent calls. No synchronization is performed, so
     * there is a slight chance that multiple calls to this method will not all
     * return the same set.
     */
    transient Set<K> keySet;
    transient Collection<V> values;

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new AbstractSet<K>() {
                @Override
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        private Iterator<Entry<K, V>> i = entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public K next() {
                            return i.next().getKey();
                        }

                        @Override
                        public void remove() {
                            i.remove();
                        }
                    };
                }

                @Override
                public int size() {
                    return size.intValue();
                }

                @Override
                public boolean isEmpty() {
                    return size.equals(BigInteger.ZERO);
                }

                @Override
                public void clear() {
                    size = BigInteger.ZERO;
                    map.clear();
                }

                @Override
                public boolean contains(Object key) {
                    return map.get(key) != null;
                }
            };
            keySet = ks;
        }
        return ks;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns a collection that subclasses {@link
     * AbstractCollection}. The subclass's iterator method returns a "wrapper
     * object" over this map's <tt>entrySet()</tt> iterator. The <tt>size</tt>
     * method delegates to this map's <tt>size</tt>
     * method and the <tt>contains</tt> method delegates to this map's
     * <tt>containsValue</tt> method.
     *
     * <p>
     * The collection is created the first time this method is called, and
     * returned in response to all subsequent calls. No synchronization is
     * performed, so there is a slight chance that multiple calls to this method
     * will not all return the same collection.
     */
    @Override
    public Collection<V> values() {
        Collection<V> vals = values;
        if (vals == null) {
            vals = new AbstractCollection<V>() {
                @Override
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        private final Iterator<Entry<K, V>> i = entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public V next() {
                            return i.next().getValue();
                        }

                        @Override
                        public void remove() {
                            i.remove();
                        }
                    };
                }

                @Override
                public int size() {
                    return size.intValue();
                }

                @Override
                public boolean isEmpty() {
                    return size.equals(BigInteger.ZERO);
                }

                @Override
                public void clear() {
                    size = BigInteger.ZERO;
                    map.clear();
                }

                @Override
                public boolean contains(Object value) {
                    if (null == value) {
                        throw new NullPointerException();
                    }
                    for (Entry<K, V> entry : entrySet()) {
                        if (value.equals(entry.getValue())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            values = vals;
        }
        return vals;
    }

//    public List<KeyValue256<K, V>> listEntry() {
//        ArrayList<KeyValue256<K, V>> resultEntry = new ArrayList<KeyValue256<K, V>>(size.intValue());
//        map.listEntry(resultEntry);
//        //map.entryList(resultEntry,0);
//        return resultEntry;
//
//    }

    @Override
    public V get(Object key) {
        if (key == null) {
            return null;
        }
        return map.get((K) key);
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
    public Object put(Object key, Object value) {
        if (key == null) {
            return null;
        }
        V v = map.put((K) key, (V) value);
        if (v == null) {
            size = size.add(BigInteger.ONE);
        }
        return v;
    }

    @Override
    public void putAll(Map m) {
        Set<Map.Entry<K, V>> entry = m.entrySet();

        for (Map.Entry<K, V> e : entry) {
            put(e.getKey(), e.getValue());

        }

    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            return null;
        }
        V v = map.remove((K) key);
        if (v != null) {
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
//        for (KeyValue256<K, V> kv : listEntry()) {
//            resultValue.add(kv.value);
//        }
//        return resultValue;
//    }

    /**
     * Base of key, value, and entry Iterators. Adds fields to Traverser to
     * support iterator.remove.
     */
    static class BaseIterator<K, V> {

        HashMapUnit256[] layers = new HashMapUnit256[32];
        int[] layerIndexs = new int[32];
        Object[] table;        // current table; updated if resized
        KeyValue256<K, V> next;         // the next entry to use
        Iterator<KeyValue256<K, V>> nextIterator;
        int layer = 0;              // index of bin to use next

        BaseIterator(HashMapUnit256<K, V> map) {
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
                if (table[index].getClass() == KeyValue256.class) {
                    next = (KeyValue256<K, V>) table[index];
                } else {
                    if (table[index].getClass() == LinkedList.class) {//最底层特殊处理
                        //处理多重哈希冲突
                        LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[index]);
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
                        HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[index];
                        table = mmu.getTable();
                        layers[layer] = mmu;
                        //layerIndexs[layer]++;
                        layer++;
                        layerIndexs[layer] = -1;
                        return this.hasNext();
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
    }

    static final class KeyIterator<K, V> extends BaseIterator<K, V>
            implements Iterator<K>, Enumeration<K> {

        KeyIterator(HashMapUnit256<K, V> map) {
            super(map);
        }

        @Override
        public final K next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next.key;
        }

        @Override
        public final K nextElement() {
            return next();
        }
    }

    static final class ValueIterator<K, V> extends BaseIterator<K, V>
            implements Iterator<V>, Enumeration<V> {

        ValueIterator(HashMapUnit256<K, V> map) {
            super(map);
        }

        @Override
        public final V next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next.value;
        }

        @Override
        public final V nextElement() {
            return next();
        }
    }

    static final class EntryIterator<K, V> extends BaseIterator<K, V>
            implements Iterator<Map.Entry<K, V>> {

        EntryIterator(HashMapUnit256<K, V> map) {
            super(map);
        }

        @Override
        public final Map.Entry<K, V> next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next;
        }
    }

    //以下是扩展功能：
//    /**
//     * 获得Map容器自然排序的最小元素
//     *
//     * @return　最小元素
//     */
//    public KeyValue256<K, V> min() {
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
//    public KeyValue256<K, V> max() {
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
//    public List<KeyValue256<K, V>> subList(int begin, int end) {
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
//    public List<KeyValue256<K, V>> subList(int begin) {
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
        int a = 0x6ad0f5;
        System.out.println("a=" + a);
        System.out.println(Hex.encodeHexString(ByteBuffer.allocate(4).putInt(0x6ad0f5).array()));
        HashMap256<Integer, String> mapTest = new HashMap256<>();
        mapTest.put('a', "test");
        mapTest.put('c', "test");
        String key = "hjk";
        String value = "lyf";
        Random r = new Random();
        long endTime = 0;
        long times = 0;
        long timeStart = 0;
        int testCount = 700;
        timeStart = System.currentTimeMillis();
        Map<String, String> map2 = new HashMap<String, String>();

        for (int i = 0; i < testCount; i++) {
            map2.put(i + key, value);
        }
        endTime = System.currentTimeMillis();
        times = endTime - timeStart;
        System.out.println("HashMap add:" + times);
        //Display the amount of free memory in the Java Virtual Machine.

        timeStart = System.currentTimeMillis();
        map2.put("2010hjk", "2010lyf");
        map2.get("2010hjk");
        map2.remove("2010hjk");
        endTime = System.currentTimeMillis();
        times = endTime - timeStart;
        System.out.println("HashMap get:" + times);
        long freeMem = Runtime.getRuntime().maxMemory();
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println(df.format(freeMem) + " MB");
        timeStart = System.currentTimeMillis();
        HashMap256<String, String> map = new HashMap256<String, String>();

        for (int i = 0; i < testCount; i++) {
            map.put(i + key, value);
        }

        //System.out.println(map.size());
        endTime = System.currentTimeMillis();
        times = endTime - timeStart;
        //System.out.println(map.size());
        System.out.println("MyHashMap add:" + times);
        long freeMemEnd = Runtime.getRuntime().maxMemory();
        df = new DecimalFormat("0.00");
        System.out.println(df.format(freeMem - freeMemEnd) + " MB");
//
//        map.put("hjk2010", "lyf2010");
//        map.put("2010hjk", "2010lyf");
//        map.put("中国hjk", "中国");
//        String s1 = map.get("2010hjk");
//        System.out.println("size=" + map.size);
//        System.out.println("2010hjk=" + s1);
//        map.remove("中国hjk");
//        s1 = map.get("中国hjk");
//        System.out.println(s1);
//
//        timeStart = System.currentTimeMillis();
////		List<Integer> li= new ArrayList<Integer>();
////		
////		for(int i=0;i<750000;i++){
////			li.add(r.nextInt());
////		}
////		//System.out.println(map.size());
////		Collections.sort(li);
//        endTime = System.currentTimeMillis();
//        times = endTime - timeStart;
//        //System.out.println(map.size());
//        System.out.println("list add:" + times);
//        System.out.println("哈希冲突计数：" + map.getConflictCount());

//		List<String> list = map.keyList();
//		timeStart = System.currentTimeMillis();
//		
//		
//		for(String s:list){
//			String val = map.get(s);
//			if(val==null){
//				System.out.println("TODO:debug... "+s+":");
//				//val = map.get(kv.key);
//				//System.out.println(val);
//			}
//		}
//
//        timeStart = System.currentTimeMillis();
//        map.put("2010hjk", "2010lyf");
//        map.get("2010hjk");
//        map.remove("2010hjk");
//        endTime = System.currentTimeMillis();
//        times = endTime - timeStart;
//        System.out.println("MyHashMap get:" + times);
//		long endTime = System.currentTimeMillis();
//		long times = endTime - timeStart;
//		//System.out.println(map.size());
//		//System.out.println("MyHashMap find:"+times);
//		//List<KeyValue256<String, String>> list = map.entryList();
        Set<String> keys2 = map2.keySet();
        timeStart = System.currentTimeMillis();
        //timeStart = System.nanoTime();
        for (String s : keys2) {
            map2.get(s);
        }
        //endTime = System.nanoTime();

        endTime = System.currentTimeMillis();
        times = endTime - timeStart;

//		System.out.println(map2.size())
        //System.out.println("HashMap find:" + (double) times / testCount);
        System.out.println("HashMap find:" + times);
        int cnt = 0;
        Set<String> keys = map.keySet();
        //List<String> list = new ArrayList(keys);
        System.out.println("keys:" + keys.size());
        //timeStart = System.nanoTime();
        timeStart = System.currentTimeMillis();
        for (String s : keys) {
            //System.out.println(map.get(s));
            map.get(s);
            //map.remove(s);
        }
//        for (String s : list) {
//            System.out.println(s + ":" + map.get(s));
//            if (map.get(s) != null) {
//                cnt++;
//            }
//
//        }
        //endTime = System.nanoTime();

        endTime = System.currentTimeMillis();
        times = endTime - timeStart;

//		System.out.println(map2.size());
       // System.out.println("MyHashMap find:" + (double) times / testCount);
        System.out.println("MyHashMap find:" + times);
        System.out.println("remove after keys:" + cnt);
//		
//		timeStart = System.currentTimeMillis();
//		MyHashMap<Integer, String> map3 = new MyHashMap<Integer, String>();
//		
//		for(int i=0;i<400000;i++){
//			map3.put(i, value);
//		}
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("MyHashMap3 add:"+times);
//		timeStart = System.currentTimeMillis();
//		//List<KeyValue256<String, String>> list = map.entryList();
//		List<String> list3 = map.listKeys();
//		
//		
//		for(String s:list3){
//			String val = map.get(s);
//			if(val==null){
//				System.out.println("TODO:debug... "+s+":");
//				//val = map.get(kv.key);
//				//System.out.println(val);
//			}
//		}
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("MyHashMap3 find:"+times);
//		System.out.println(map3.size());
//		
//		timeStart = System.currentTimeMillis();
//		Set<Integer> keys3 = map2.keySet();
//		for(Integer s:keys2){
//			String val = map2.get(s);
//			if(val==null){
//				System.out.println("TODO:debug...");
//			}
//		}
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("HashMap find2:"+times);
//		
//		timeStart = System.currentTimeMillis();
//		//List<KeyValue256<String, String>> list = map.entryList();
//		List<Integer> list5 = map3.keyList();
//		
//		
//		for(Integer s:list3){
//			String val = map3.get(s);
//			if(val==null){
//				System.out.println("TODO:debug... "+s+":");
//				//val = map.get(kv.key);
//				//System.out.println(val);
//			}
//		}
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("MyHashMap3 find2:"+times);
//		
//		timeStart = System.currentTimeMillis();
//		Set<Entry<Integer, String>> set = map2.entrySet();
//		for(Entry<Integer, String> e:set){
//			Integer i = e.getKey();
//			//System.out.println(i);
//		}
//
//		
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("HashMap entrySet():"+times);
        //System.out.println(map.size());
//		timeStart = System.currentTimeMillis();
//		List<Integer> list2 = map.keyList();
//		//System.out.println(list.size());
////		
////		
//		for(Integer s:list){
//			String val = map.get(s);
//			if(val==null){
//				System.out.println("TODO:debug... "+s+":");
//				//val = map.get(kv.key);
//				//System.out.println(val);
//			}
//		}
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("MyHashMap find:"+times);
//		timeStart = System.currentTimeMillis();
//		List<KeyValue256<Integer, String>> list3 = map.entryList();
//		
//		for(KeyValue256<Integer, String> kv:list3) {
//			Integer i = kv.key;
//			//System.out.println(i);
//		}
//		
//		endTime = System.currentTimeMillis();
//		times = endTime - timeStart;
//		System.out.println("MyHashMap map.entryList():"+times);
//		//System.out.println(list==list2);
    }
}
