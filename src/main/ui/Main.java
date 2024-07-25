package ui;

public class Main {
    public static void main(String[] args) throws Exception {
        ensureUsingJavaw();
        SimulationManager manager = new SimulationManager();
        manager.mainLoop();
    }

    private static void ensureUsingJavaw() {
        if (System.console() == null) {
            // goodo
            return;
        }
        System.err.println("Please run this program with javaw.exe!");
    }
}
