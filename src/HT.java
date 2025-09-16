class HT {
    static final class Node {
        Object key, value;
        Node next;
        Node(Object k, Object v, Node n) { key = k; value = v; next = n; }
    }

    Node[] table = new Node[8];
    int size = 0;

    public Object get(Object key) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) return e.value;
        }
        return null;
    }

    public void add(Object key, Object value) {
        int i = key.hashCode() & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        table[i] = new Node(key, value, table[i]);
        if (++size > table.length * 0.75)
            resize();
    }

    private void resize() {
        Node[] old = table;
        table = new Node[old.length * 2];
        size = 0;
        for (Node bucket : old) {
            for (Node e = bucket; e != null; e = e.next) {
                add(e.key, e.value);
            }
        }
    }
}