package ui;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulatorState simState = SimulatorState.getInstance();
        SimulatorGUI simGUI = SimulatorGUI.getInstance();

        while (true) {
            simState.tick();
            simGUI.tick();
        }
    }
}
