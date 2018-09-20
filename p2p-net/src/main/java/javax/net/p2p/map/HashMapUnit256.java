package javax.net.p2p.map;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//import javax.net.pool.PooledHashMapUnitFactory;

public class HashMapUnit256<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Charset UTF_8 = Charset.forName("utf-8");
    //private static final int (code.length-1) = 31;
    public int conflictCount;
    public int nodeCount;
    private final Object[] table = new Object[256];

    public int getConflictCount() {
        return conflictCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public Object[] getTable() {
        return table;
    }

    public static byte[] hashCode256(Byte key) {
        return ByteBuffer.allocate(1).put(key).array();
    }

    public static byte[] hashCode256(Short key) {
        return ByteBuffer.allocate(2).putShort(key).array();
    }

    public static byte[] hashCode256(Object key) {
        if (key.getClass() == String.class) {
            final byte[] array = new byte[32];
            byte[] bytes = ((String) key).getBytes(UTF_8);
            if (bytes.length >= 28) {
                System.arraycopy(bytes, 0, array, 0, 28);
                byte[] hashcode = ByteBuffer.allocate(4).putInt(key.hashCode()).array();
                for (int i = 28, j = 0; i < 32; i++, j++) {
                    array[i] = hashcode[j];
                }
            } else {
                //final byte[] sha256 = SecurityUtils.sha256((String) key);
                System.arraycopy(bytes, 0, array, 0, bytes.length);
                //System.arraycopy(sha256, bytes.length, array, bytes.length, 32 - bytes.length);
                //System.
            }
            return array;
        } else if (key.getClass() == Integer.class) {
            return ByteBuffer.allocate(4).putInt((int) key).array();
        } else if (key.getClass() == Long.class) {
            return ByteBuffer.allocate(8).putLong((long) key).array();
        } else if (key.getClass() == Date.class) {
            return ByteBuffer.allocate(8).putLong(((Date) key).getTime()).array();
        } else if (key.getClass() == Short.class) {
            return ByteBuffer.allocate(2).putShort((short) key).array();
        } else if (key.getClass() == Byte.class) {
            return ByteBuffer.allocate(1).put((byte) key).array();
        } else if (key.getClass() == Character.class) {
            return ByteBuffer.allocate(2).putChar((char) key).array();
        } else if (key instanceof Sha256) {
            return ((Sha256) key).hashCode256();
        }
        return ByteBuffer.allocate(4).putInt(key.hashCode()).array();
    }

    public V put(K key, V value) {
        KeyValue256<K, V> pair = new KeyValue256<>(key, value);
        byte[] code = hashCode256(key);
        if (code.length - 1 == 0) {
            V v = endPut(pair, code);
            if (v == null) {
                nodeCount++;
            }
            return v;
        }
        int hash = (int) (code[0] & 0xff);

        if (table[hash] == null) {
            table[hash] = pair;
            nodeCount++;
            return null;
        } else {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair1 = (KeyValue256<K, V>) table[hash];
                if (pair1.key.equals(pair.key)) {
                    V oldValue = pair1.value;
                    pair1.value = pair.value;
                    return oldValue;
                }
                //HashMapUnit256<K, V> mmu = PooledHashMapUnitFactory.borrowObject();
                HashMapUnit256<K, V> mmu = new HashMapUnit256<>();
                mmu.put(pair1, pair1.hashCode256(), 1);
                mmu.put(pair, code, 1);
                table[hash] = mmu;
                nodeCount++;
                return null;
            }

            HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[hash];
            V v = mmu.put(pair, code, 1);
            if (v == null) {
                nodeCount++;
            }
            return v;
        }
    }

    V put(KeyValue256<K, V> pair, byte[] code, int index) {
        if (index >= (code.length - 1)) {
            V v = endPut(pair, code);
            if (v == null) {
                nodeCount++;
            }
            return v;
        }
        int hash = (int) (code[index] & 0xff);
        if (table[hash] == null) {

            table[hash] = pair;
            nodeCount++;
            return null;
        } else {
            //index++;
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair1 = (KeyValue256<K, V>) table[hash];
                if (pair1.key.equals(pair.key)) {
                    V oldValue = pair1.value;
                    pair1.value = pair.value;
                    return oldValue;
                }
                //HashMapUnit256<K, V> mmu = PooledHashMapUnitFactory.borrowObject();
                HashMapUnit256<K, V> mmu = new HashMapUnit256<>();
                mmu.put(pair1, pair1.hashCode256(), index + 1);
                mmu.put(pair, code, index + 1);
                table[hash] = mmu;
                nodeCount++;
                return null;
            }
            V v = ((HashMapUnit256<K, V>) table[hash]).put(pair, code, index + 1);
            if (v == null) {
                nodeCount++;
            }
            return v;
        }

    }

    private V endPut(KeyValue256<K, V> pair, byte[] code) {
        int hash = (int) (code[(code.length - 1)] & 0xff);

        if (table[hash] == null) {
            table[hash] = pair;
            nodeCount++;
            return null;
        } else {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pairOld = (KeyValue256<K, V>) table[hash];
                if (pair.equals(pairOld)) {
                    V oldValue = pairOld.value;
                    table[hash] = pair;
                    return oldValue;// 已有同key元素，执行更新操作
                }
                //处理首次哈希冲突
                LinkedList<KeyValue256<K, V>> list = new LinkedList();
                list.add(pairOld);
                list.add(pair);
                table[hash] = list;
                nodeCount++;
                return null;

            }
            //处理多重哈希冲突
            conflictCount++;
            LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[hash]);
            Iterator<KeyValue256<K, V>> it = list.iterator();
            while (it.hasNext()) {
                KeyValue256<K, V> kv = it.next();
                if (pair.equals(kv)) {
                    V oldValue = kv.value;
                    table[hash] = pair;
                    return oldValue;// 已有同key元素，执行更新操作
                }
            }
            // 没找到同key元素，执行添加操作
            list.add(pair);
            nodeCount++;
            return null;
        }
    }

    public V get(Object key) {
        //int code = KeyValue.getCode(key);
        //System.out.println("get:"+code);
        byte[] code = hashCode256(key);
        int index = 0;

        int hash = (int) (code[0] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair = (KeyValue256<K, V>) table[hash];
                return (key.equals(pair.key)) ? pair.value : null;

            } else {
                //return (index >= (code.length-1))? endGet( key,code) : mmu.get(key,code,index+1);
                if (table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[hash]);
                    Iterator<KeyValue256<K, V>> it = list.iterator();
                    while (it.hasNext()) {
                        KeyValue256<K, V> kv = it.next();
                        if (key.equals(kv.key)) {
                            return kv.value;// 找到同key元素
                        }
                    }
                    return null;// 没找到同key元素
                    //return endGet( key,code);
                }
                return ((HashMapUnit256<K, V>) table[hash]).get(key, code, index + 1);
            }

        }
        return null;

    }

    private V get(Object key, byte[] code, int index) {
        int hash = (int) (code[index] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair = (KeyValue256<K, V>) table[hash];
                return (key.equals(pair.key)) ? pair.value : null;
            } else {

                //return (index >= (code.length-1))? endGet( key,code) : mmu.get(key,code,index+1);
                if (table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[hash]);
                    Iterator<KeyValue256<K, V>> it = list.iterator();
                    while (it.hasNext()) {
                        KeyValue256<K, V> kv = it.next();
                        if (key.equals(kv.key)) {
                            return kv.value;// 找到同key元素
                        }
                    }
                    return null;// 没找到同key元素
                    //return endGet( key,code);
                }
                HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[hash];
                return mmu.get(key, code, index + 1);
            }
        }
        return null;
    }

    public void listEntry(List<KeyValue256<K, V>> resultEntry, int layerLimit) {
        int index = 0;
        for (int i = 128; i < 256; i++) {//首先处理负整数集
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    resultEntry.add(pair);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.listEntry(resultEntry, index + 1);
                }
            }
        }
        for (int i = 0; i < 128; i++) {//其次处理0、正整数集
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    resultEntry.add(pair);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.listEntry(resultEntry, index + 1);
                }
            }
        }

    }

    private void listEntry(List<KeyValue256<K, V>> list, int index, int layerLimit) {
        if (index >= layerLimit) {
            endListEntry(list);
            return;
        }
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    list.add(pair);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.listEntry(list, index + 1);
                }
            }
        }

    }

    private void endListEntry(List<KeyValue256<K, V>> result) {
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    result.add(pair);
                } else {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[i]);
                    list.forEach((kv) -> {
                        result.add(kv);
                    });
                }
            }
        }

    }

    public Object nextEntry(int layer, int layerLimit) {
        if (layer >= layerLimit) {
            for (int i = 0; i < 256; i++) {
                if (table[i] != null) {
                    return table[i];
                }
            }
            return null;
        }
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                return table[i];
            }
        }
        return null;
    }

    public void keyList(List<K> list, int layerLimit) {
        int index = 0;
        for (int i = 128; i < 256; i++) {//首先处理负整数集
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    list.add(pair.key);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.keyList(list, index + 1);
                }
            }
        }
        for (int i = 0; i < 128; i++) {//其次处理0、正整数集
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    list.add(pair.key);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.keyList(list, index + 1);
                }
            }
        }
    }

    public void unsignedKeyList(List<K> list, int layerLimit) {
        int index = 0;
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    list.add(pair.key);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);
                }
            }
        }

    }

    private void keyList(List<K> list, int index, int layerLimit) {
        if (index >= layerLimit) {
            endKeyList(list);
            return;
        }
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    list.add(pair.key);
                } else {
                    HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);
                }
            }
        }

    }

    private void endKeyList(List<K> result) {
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == KeyValue256.class) {
                    KeyValue256<K, V> pair = (KeyValue256<K, V>) table[i];
                    result.add(pair.key);
                } else {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[i]);

                    list.forEach((kv) -> {
                        result.add(kv.key);
                    });
                }
            }
        }

    }

    V remove(Object key) {
        byte[] code = hashCode256(key);
        int index = 0;
        int hash = (int) (code[index] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair = (KeyValue256<K, V>) table[hash];
                if (key.equals(pair.key)) {
                    V oldValue = pair.value;
                    table[hash] = null;
                    nodeCount--;
                    return oldValue;
                }
                return null;

            } else {
                if (index >= (code.length - 1)) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[hash]);
                    Iterator<KeyValue256<K, V>> it = list.iterator();
                    while (it.hasNext()) {
                        KeyValue256<K, V> kv = it.next();
                        if (key.equals(kv.key)) {
                            it.remove();
                            nodeCount--;
                            return kv.value;// 找到同key元素
                        }
                    }
                    return null;// 没找到同key元素
                    //return endGet( key,code);
                }
                HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[hash];
                V v = mmu.remove(key, code, index + 1);
                if (v != null) {
                    nodeCount--;
                    if (mmu.nodeCount == 1) {
                        for (int i = 0; i < mmu.table.length; i++) {
                            if (mmu.table[i] != null) {
                                table[hash] = mmu.table[i];
                                break;
                            }
                        }
                    } else if (mmu.nodeCount <= 0) {
                        table[hash] = null;
                        //PooledHashMapUnitFactory.returnObject(mmu);
                    }
                }
                return v;
            }
        }
        return null;

    }

    private V remove(Object key, byte[] code, int index) {

        int hash = (int) (code[index] & 0xff);

        if (table[hash] != null) {
            if (table[hash].getClass() == KeyValue256.class) {
                KeyValue256<K, V> pair = (KeyValue256<K, V>) table[hash];
                if (key.equals(pair.key)) {
                    V oldValue = pair.value;
                    table[hash] = null;
                    nodeCount--;
                    return oldValue;
                }
                return null;

            } else {
                if (index >= (code.length - 1)) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<KeyValue256<K, V>> list = ((LinkedList<KeyValue256<K, V>>) table[hash]);
                    Iterator<KeyValue256<K, V>> it = list.iterator();
                    while (it.hasNext()) {
                        KeyValue256<K, V> kv = it.next();
                        if (key.equals(kv.key)) {
                            it.remove();
                            nodeCount--;
                            return kv.value;// 找到同key元素
                        }
                    }
                    return null;// 没找到同key元素
                    //return endGet( key,code);
                }
                HashMapUnit256<K, V> mmu = (HashMapUnit256<K, V>) table[hash];
                V v = mmu.remove(key, code, index + 1);
                if (v != null) {
                    nodeCount--;
                    if (mmu.nodeCount == 1) {
                        for (int i = 0; i < mmu.table.length; i++) {
                            if (mmu.table[i] != null) {
                                table[hash] = mmu.table[i];
                                break;
                            }
                        }
                    } else if (mmu.nodeCount <= 0) {
                        table[hash] = null;
                        //PooledHashMapUnitFactory.returnObject(mmu);
                    }
                }
                return v;
            }
        }
        return null;
    }

    void clear() {
        for (int i = 0; i < 256; i++) {
            table[i] = null;
//            if (null != table[i]) {
//                if (table[i].getClass() == HashMapUnit256.class) {
//                    PooledHashMapUnitFactory.returnObject((HashMapUnit256) table[i]);
//                } else {
//                    table[i] = null;
//                }
//            }
        }
    }

    //自定义序列化操作：
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();

    }

    public static void main(String[] args) {

    }

}
