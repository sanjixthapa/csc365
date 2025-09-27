import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;

public class HT implements Serializable {
    public static final class Node {
        Object key, value;
        Node next;
        Node(Object k, Object v, Node n) { key = k; value = v; next = n; }
    }
    Node[] table = new Node[8];
    int size = 0;

    Object get(Object key) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key))
                return e.value;
        }
        return null;
    }

    void add(Object key, Object value) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        table[i] = new Node(key, value, table[i]);
        ++size;
        if ((float)size/table.length >= 0.75f)
            resizeV2();
    }

    void resizeV2() {
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
    @Serial
    private void writeObject(ObjectOutputStream s) throws Exception {
        s.defaultWriteObject();
        s.writeInt(size);
        for (Node node : table) {
            for (Node e = node; e != null; e = e.next) {
                s.writeObject(e.key);
                s.writeObject(e.value);
            }
        }
    }
    @Serial
    private void readObject(ObjectInputStream s) throws Exception {
        s.defaultReadObject();
        table = new Node[8];
        int n = s.readInt();
        for (int i = 0; i < n; ++i) {
            Object key = s.readObject();
            Object value = s.readObject();
            add(key, value);
        }
    }
}