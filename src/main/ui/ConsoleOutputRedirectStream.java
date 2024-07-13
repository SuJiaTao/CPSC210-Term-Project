package ui;

import java.io.*;

// Represents a simple stream that only saves the last thing that was printed to it. Use to 
// redirect stdout/err to be visible when using Lanterna
public class ConsoleOutputRedirectStream extends PrintStream {
    private String stringToDisplay;

    // EFFECTS: redirects a given outputstream to super, enable always flush
    public ConsoleOutputRedirectStream(OutputStream outputStream) {
        // NOTE:
        // i can't lie, I am not familiar enough with the JDK's stream related classes
        // and methods to completely understand what this constructor is doing, but
        // making any modification to it will cause the program to crash, so I will opt
        // to keep it like this >.<
        super(outputStream, true);
        stringToDisplay = "";
    }

    public String getStringToDisplay() {
        return stringToDisplay;
    }

    // EFFECTS: updates latest display string
    @Override
    public void print(String toPrint) {
        internalPrint(toPrint);
    }

    // EFFECTS: updates latest display string
    @Override
    public void println(String toPrint) {
        internalPrint(toPrint);
    }

    // EFFECTS: updates latest display string
    @Override
    public void print(Object toPrint) {
        internalPrint(toPrint.toString());
    }

    // EFFECTS: updates latest display string
    @Override
    public void println(Object toPrint) {
        internalPrint(toPrint.toString());
    }

    // EFFECTS: updates latest display string
    private void internalPrint(String toPrint) {
        stringToDisplay = toPrint;
    }
}
