package ui;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulationManager manager = new SimulationManager();
        NewSimulationGraphics nsg = new NewSimulationGraphics(manager);
        manager.mainLoop();
    }
}
