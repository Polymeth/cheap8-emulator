import emulation.Chip8;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Thread {
    private static Chip8 chip;


    public static void main(String[] args) throws IOException {
        chip = new Chip8();
        chip.loadProgram("pong2.c8");
        //chip.loadProgram("test_opcode.ch8");
        Main main = new Main();
        main.run();
    }

    public void run() {
        while(true) {
            chip.run();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
