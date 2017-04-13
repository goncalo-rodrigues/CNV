import java.util.ArrayList;
import java.util.List;

public class MethodInvocationTree {
    private Node main;
    private Node current;

    public MethodInvocationTree(String mainName) {
        main = new Node();
        main.name = mainName;
        main.invokes = new ArrayList<>();
        current = main;
    }

    public void ret() { current = current.parent; }

    public void call(String method) {
        Node n = new Node();
        n.name = method;
        n.parent = current;
        n.invokes = new ArrayList<>();

        current.invokes.add(n);
        current = n;
    }

    @Override
    public String toString() {
        return main.output(0);
    }

    public void print() {
        main.print();
    }

    public static class Node {
        private String name;
        private Node parent;
        private List<Node> invokes;

        public String output(int identation) {
            String spaces = "";

            if(identation > 0)
                spaces = String.format("%" + identation + "s", "");

            String current = spaces + name + "\n";

            if(invokes.size() == 0)
                return current;

            StringBuilder children = new StringBuilder("");
            for (Node n : invokes)
                children.append(n.output(identation + 2));

            return current + children + current;
        }

        public void print() {
            System.out.println(output(0));
        }
    }
}
