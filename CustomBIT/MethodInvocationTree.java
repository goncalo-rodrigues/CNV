import java.io.PrintWriter;
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

    public void print(PrintWriter writer) {
        main.print(writer, 0);
    }

    public static class Node {
        private String name;
        private Node parent;
        private List<Node> invokes;

        public void print(PrintWriter writer, int identation) {
            String spaces = "";

            if(identation > 0)
                spaces = String.format("%" + identation + "s", "");

            String current = spaces + name;
            writer.println(current);

            if(invokes.size() == 0)
                return;

            for (Node n : invokes)
                n.print(writer, identation + 2);

            writer.println(current);
        }
    }
}
