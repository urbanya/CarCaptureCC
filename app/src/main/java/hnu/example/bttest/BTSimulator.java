package hnu.example.bttest;

import java.util.Random;

public class BTSimulator {
    public static int INTERVALL_MS = 1000; //simulate data every 1000ms

    public static String simulateValue() {
        Random rand = new Random();

        String prefix;
        String postfix=";";
        int value;

        if (rand.nextBoolean()==true) {
            value = rand.nextInt(210); // Enthält eine Geschwindigkeit zwischen [0 - 209].
            prefix= "V";
        } else {
            value = rand.nextInt(4000); // Enthält eine Drehzahl im Bereich von [0 - 3999].
            prefix= "R";
        }
        return prefix+value+postfix;
    }
}
