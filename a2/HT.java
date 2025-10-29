import java.io.*;
import java.util.ArrayList;
import java.util.List;

class HT implements Serializable {
    static final class Node implements Serializable {
        Object key;
        Object value;
        Node next;
        int count;
        Node(Object k, Object v, Node n) {
            key = k;
            value = v;
            next = n;
            count = 1;
        }
        Node(Object k, Node n) {
            key = k;
            next = n;
            count = 1;
        }
    }

    Node[] table = new Node[8];
    int size = 0;

    Object get(Object key) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                return e.value;
            }
        }
        return null;
    }

    void add(Object key, Object value) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.value = value;
                e.count++;
                return;
            }
        }
        table[i] = new Node(key, value, table[i]);
        size++;
        if ((float)size / table.length >= 0.75f) {
            resize();
        }
    }

    void add(Object key) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.count++;
                return;
            }
        }
        table[i] = new Node(key, table[i]);
        size++;
        if ((float)size / table.length >= 0.75f) {
            resize();
        }
    }

    int getCount(Object key) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                return e.count;
            }
        }
        return 0;
    }

    void resize() { // avoids unnecessary creation
        Node[] oldTable = table;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1;
        Node[] newTable = new Node[newCapacity];
        for (Node node : oldTable) {
            Node e = node;
            while (e != null) {
                Node next = e.next;
                int h = e.key.hashCode();
                int j = h & (newTable.length - 1);
                e.next = newTable[j];
                newTable[j] = e;
                e = next;
            }
        }
        table = newTable;
    }

    public Iterable<Node> nodes() {
        List<Node> list = new ArrayList<>();
        for (Node bucket : table) {
            for (Node n = bucket; n != null; n = n.next) list.add(n);
        }
        return list;
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws Exception {
        s.defaultWriteObject();
        s.writeInt(size);
        for (Node node : table) {
            for (Node e = node; e != null; e = e.next) {
                s.writeObject(e.key);
            }
        }
    }
    @Serial
    private void readObject(ObjectInputStream s) throws Exception {
        s.defaultReadObject();
        int n = s.readInt();
        for (int i = 0; i < n; ++i)
            add(s.readObject());
    }
}
