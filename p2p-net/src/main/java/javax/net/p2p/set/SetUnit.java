package javax.net.p2p.set;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.net.p2p.map.Sha256;
//import javax.net.pool.PooledHashMapUnitFactory;

public class SetUnit<K> implements Serializable {

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

    public boolean add(K key) {
        byte[] code = hashCode256(key);
        if (code.length - 1 == 0) {
            boolean v = endAdd(key, code);
            if (v) {
                nodeCount++;
            }
            return v;
        }
        int hash = (int) (code[0] & 0xff);

        if (table[hash] == null) {
            table[hash] = key;
            nodeCount++;
            return true;
        } else {
            if (table[hash].getClass() == SetUnit.class) {
                SetUnit<K> mmu = (SetUnit<K>) table[hash];
                boolean v = mmu.add(key, code, 1);
                if (v) {
                    nodeCount++;
                }
                return v;
            }
            K key1 = (K) table[hash];
            if (key1.equals(key)) {
                return false;
            }
            SetUnit<K> mmu = new SetUnit<>();
            mmu.add(key1, hashCode256(key1), 1);
            mmu.add(key, code, 1);
            table[hash] = mmu;
            nodeCount++;
            return true;

        }
    }

    boolean add(K key, byte[] code, int index) {
        if (index >= (code.length - 1)) {
            boolean v = endAdd(key, code);
            if (v) {
                nodeCount++;
            }
            return v;
        }
        int hash = (int) (code[index] & 0xff);
        if (table[hash] == null) {

            table[hash] = key;
            nodeCount++;
            return true;
        } else {
            //index++;
            if (table[hash].getClass() == SetUnit.class) {
                boolean v = ((SetUnit<K>) table[hash]).add(key, code, index + 1);
                if (v) {
                    nodeCount++;
                }
                return v;
            }

            K key1 = (K) table[hash];
            if (key1.equals(key)) {
                return false;
            }
            SetUnit<K> mmu = new SetUnit<>();
            mmu.add(key1, hashCode256(key1), index + 1);
            mmu.add(key, code, index + 1);
            table[hash] = mmu;
            nodeCount++;
            return true;
        }

    }

    private boolean endAdd(K key, byte[] code) {
        int hash = (int) (code[(code.length - 1)] & 0xff);

        if (table[hash] == null) {
            table[hash] = key;
            nodeCount++;
            return true;
        } else {
            if (table[hash].getClass() == LinkedList.class) {
                //处理多重哈希冲突
                conflictCount++;
                LinkedList<K> list = ((LinkedList<K>) table[hash]);
                Iterator<K> it = list.iterator();
                while (it.hasNext()) {
                    K kv = it.next();
                    if (key.equals(kv)) {
                        return false;// 已有同key元素
                    }
                }
                // 没找到同key元素，执行添加操作
                list.add(key);
                nodeCount++;
                return true;

            }

            K keyOld = (K) table[hash];
            if (key.equals(keyOld)) {
                return false;// 已有同key元素
            }
            //处理首次哈希冲突
            LinkedList<K> list = new LinkedList();
            list.add(keyOld);
            list.add(key);
            table[hash] = list;
            nodeCount++;
            return true;
        }
    }

    public boolean get(Object key) {
        //int code = KeyValue.getCode(key);
        //System.out.println("get:"+code);
        byte[] code = hashCode256(key);
        int index = 0;

        int hash = (int) (code[0] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == SetUnit.class) {
                return ((SetUnit<K>) table[hash]).get(key, code, index + 1);
            } else {
                //return (index >= (code.length-1))? endGet( key,code) : mmu.get(key,code,index+1);
                if (table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[hash]);
                    Iterator<K> it = list.iterator();
                    while (it.hasNext()) {
                        K kv = it.next();
                        if (key.equals(kv)) {
                            return false;// 找到同key元素
                        }
                    }
                    return true;// 没找到同key元素
                    //return endGet( key,code);
                }
                K k = (K) table[hash];
                return k.equals(key);
            }

        }
        return true;

    }

    private boolean get(Object key, byte[] code, int index) {
        int hash = (int) (code[index] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == SetUnit.class) {
                SetUnit<K> mmu = (SetUnit<K>) table[hash];
                return mmu.get(key, code, index + 1);
            } else {
                //return (index >= (code.length-1))? endGet( key,code) : mmu.get(key,code,index+1);
                if (table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[hash]);
                    Iterator<K> it = list.iterator();
                    while (it.hasNext()) {
                        K kv = it.next();
                        if (key.equals(kv)) {
                            return false;// 找到同key元素
                        }
                    }
                    return true;// 没找到同key元素
                    //return endGet( key,code);
                }

                K k = (K) table[hash];
                return k.equals(key);

            }
        }
        return true;
    }

    public void listEntry(List<K> list, int layerLimit) {
        int index = 0;
        for (int i = 128; i < 256; i++) {//首先处理负整数集
            if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }
        for (int i = 0; i < 128; i++) {//其次处理0、正整数集
            if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }

    }

    private void listEntry(List<K> list, int index, int layerLimit) {
        if (index >= layerLimit) {
            endListEntry(list);
            return;
        }
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.listEntry(list, index + 1);
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }

    }

    private void endListEntry(List<K> result) {
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[i]);
                    list.forEach((kv) -> {
                        result.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    result.add(key);
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
            return true;
        }
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                return table[i];
            }
        }
        return true;
    }

    public void keyList(List<K> list, int layerLimit) {
        int index = 0;
        for (int i = 128; i < 256; i++) {//首先处理负整数集
           if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }
        for (int i = 0; i < 128; i++) {//其次处理0、正整数集
            if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }
    }

    public void unsignedKeyList(List<K> list, int layerLimit) {
        int index = 0;
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
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
                if (table[i].getClass() == SetUnit.class) {
                    SetUnit<K> mmu = (SetUnit<K>) table[i];
                    mmu.keyList(list, index + 1, layerLimit);

                } else if (table[i].getClass() == LinkedList.class) {
                    //处理多重哈希冲突
                    LinkedList<K> l = ((LinkedList<K>) table[i]);
                    l.forEach((kv) -> {
                        list.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    list.add(key);
                }
            }
        }

    }

    private void endKeyList(List<K> result) {
        for (int i = 0; i < 256; i++) {
            if (table[i] != null) {
                if (table[i].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[i]);

                    list.forEach((kv) -> {
                        result.add(kv);
                    });
                } else {
                    K key = (K) table[i];
                    result.add(key);
                }
            }
        }

    }

    boolean remove(Object key) {
        byte[] code = hashCode256(key);
        int index = 0;
        int hash = (int) (code[index] & 0xff);
        if (table[hash] != null) {
            if (table[hash].getClass() == SetUnit.class) {
                SetUnit<K> mmu = (SetUnit<K>) table[hash];
                boolean v = mmu.remove(key, code, index + 1);
                if (v) {
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
            } else {
                if (index >= (code.length - 1)
                        && table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[hash]);
                    Iterator<K> it = list.iterator();
                    while (it.hasNext()) {
                        K kv = it.next();
                        if (key.equals(kv)) {
                            it.remove();
                            nodeCount--;
                            return true;// 找到同key元素
                        }
                    }
                    return false;// 没找到同key元素
                    //return endGet( key,code);
                }
                K k = (K) table[hash];
                if (k.equals(key)) {
                    table[hash] = null;
                    nodeCount--;
                    return true;
                }
                return false;
            }
        }
        return false;

    }

    private boolean remove(Object key, byte[] code, int index) {

        int hash = (int) (code[index] & 0xff);

        if (table[hash] != null) {
            if (table[hash].getClass() == SetUnit.class) {
                SetUnit<K> mmu = (SetUnit<K>) table[hash];
                boolean v = mmu.remove(key, code, index + 1);
                if (v) {
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
            } else {
                if (index >= (code.length - 1)
                        && table[hash].getClass() == LinkedList.class) {//最底层特殊处理
                    //处理多重哈希冲突
                    LinkedList<K> list = ((LinkedList<K>) table[hash]);
                    Iterator<K> it = list.iterator();
                    while (it.hasNext()) {
                        K kv = it.next();
                        if (key.equals(kv)) {
                            it.remove();
                            nodeCount--;
                            return true;// 找到同key元素
                        }
                    }
                    return false;// 没找到同key元素
                    //return endGet( key,code);
                }
                K k = (K) table[hash];
                if (k.equals(key)) {
                    table[hash] = null;
                    nodeCount--;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    void clear() {
        for (int i = 0; i < 256; i++) {
            table[i] = null;
//            if (null != table[i]) {
//                if (table[i].getClass() == SetUnit.class) {
//                    PooledHashMapUnitFactory.returnObject((SetUnit) table[i]);
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
