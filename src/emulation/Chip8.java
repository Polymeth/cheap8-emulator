package emulation;

import java.io.*;
import java.util.Stack;

public final class Chip8 {
    private final static int MEMORY_OFFSET = 0x200;

    private final byte[] memory;
    private final byte[] register;
    private final Stack<Character> stack;

    private char pc; // program counter
    private char I;
    //private char sp;

    public Chip8() {
        memory = new byte[4096];
        register = new byte[16];
        stack = new Stack<>();
        pc = 0x200;
        I = 0;
    }

    public void run() {
        char opcode = (char)((memory[pc] << 8) | (memory[pc+1] & 0x00FF));

        byte x = (byte)((opcode & 0x0F00) >> 8);
        byte y = (byte)((opcode & 0x00F0) >> 4);
        char nnn = (char)(opcode & 0x0FFF);
        byte kk = (byte)(opcode & 0x00FF);

       // System.out.println("opcode actuel: " + Integer.toHexString(opcode));

        switch ((opcode & 0xF000)) {
            case 0x0000:
                switch (opcode) {
                    case 0x00E0 -> System.err.println(Integer.toHexString(opcode) + " is not supported so far"); // CLS
                    case 0x00EE -> pc = stack.pop(); // RET
                    default -> System.err.println(Integer.toHexString(opcode) + " - might be an ignored instruction");
                }
                break;
            // JP addr
            case 0x1000:
                System.out.println(Integer.toHexString(opcode) + " - set the PC to " + Integer.toHexString(nnn));
                pc = nnn;
                break;
            // CALL addr
            case 0x2000:
                System.out.println(Integer.toHexString(opcode) + " - set the PC to " + Integer.toHexString(nnn) + " and pushed the current PC (" + Integer.toHexString(pc) + ")");
                stack.push(pc);
                pc = nnn;
                break;
            // SE Vx
            case 0x3000:
                System.out.println(Integer.toHexString(opcode) + " - compared equality between Vx=" + Integer.toHexString(register[x]) + " and kk=" + Integer.toHexString(kk));
                if (register[x] == kk) {
                    pc+=2;
                }
                break;
            // SNE Vx
            case 0x4000:
                System.out.println(Integer.toHexString(opcode) + " - compared difference between Vx=" + Integer.toHexString(register[x]) + " and kk=" + Integer.toHexString(kk));
                if (register[x] != kk) pc+=2;
                break;
            // SE Vx, Vy
            case 0x5000:
                System.out.println(Integer.toHexString(opcode) + " - compared difference between Vx=" + Integer.toHexString(register[x]) + " and Vy=" + Integer.toHexString(register[y]));
                if (register[x] == register[y]) pc+=2;
                break;
            // LD Vx
            case 0x6000:
                System.out.println(Integer.toHexString(opcode) + " - set register V" + Integer.toHexString(x) + " to value " + Integer.toHexString(kk));
                register[x] = kk;
                break;
            // ADD Vx
            case 0x7000:
                System.out.println(Integer.toHexString(opcode) + " - added " + Integer.toHexString(kk) + " to " + Integer.toHexString(register[x]) + " in register V" + Integer.toHexString(x));
                register[x] += kk;
                break;
            // Math operations
            case 0x8000:
                switch (opcode & 0x000F) {
                    // LD Vx, Vy
                    case 0x0000 -> {
                        System.out.println(Integer.toHexString(opcode) + " - stored value " + Integer.toHexString(register[x]) + " in register V" + Integer.toHexString(y));
                        register[x] = register[y];
                    }
                    // OR Vx, Vy
                    case 0x0001 -> {
                        System.out.println(Integer.toHexString(opcode) + " - " + Integer.toHexString(register[x]) + " OR " + Integer.toHexString(register[y]));
                        register[x] = (byte) ((register[x] | register[y]) & 0xFF);
                    }
                    // AND Vx, Vy
                    case 0x0002 -> {
                        System.out.println(Integer.toHexString(opcode) + " - " + Integer.toHexString(register[x]) + " AND " + Integer.toHexString(register[y]));
                        register[x] = (byte) ((register[x] & register[y]) & 0xFF);
                    }
                    // XOR Vx, Vy
                    case 0x0003 -> {
                        System.out.println(Integer.toHexString(opcode) + " - " + Integer.toHexString(register[x]) + " XOR " + Integer.toHexString(register[y]));
                        register[x] = (byte) ((register[x] ^ register[y]) & 0xFF);
                    }
                    // ADD Vx, Vy
                    case 0x0004 -> {
                        System.out.println(Integer.toHexString(opcode) + " - Added " + Integer.toHexString(register[x]) + " and " + Integer.toHexString(register[y]));
                        if (register[x] > ((byte) 0xFF - register[y])) {
                            register[0xF] = 1;
                        } else {
                            register[0xF] = 0;
                        }
                        register[x] = (byte) ((register[x] + register[y]) & 0xFF);
                    }
                    // SUB Vx, By
                    case 0x0005 -> {
                        System.out.println(Integer.toHexString(opcode) + " - Substracted " + Integer.toHexString(register[x]) + " and " + Integer.toHexString(register[y]));
                        if (register[x] > register[y]) {
                            register[0xF] = 1;
                        } else {
                            register[0xF] = 0;
                        }
                        register[x] = (byte) ((register[x] - register[y]) & 0xFF);
                    }
                    // SHR Vx
                    case 0x0006 -> {
                        System.out.println(Integer.toHexString(opcode) + " - checked if " + Integer.toBinaryString(opcode) + " has 1 as LSB");
                        if ((register[x] & 0x1) == 1) {
                            register[0xF] = 1;
                        } else {
                            register[0xF] = 0;
                        }
                        register[x] = (register[x] >>= 1);
                    }
                    case 0x0007 -> {
                        System.out.println(Integer.toHexString(opcode) + " - Substracted " + Integer.toHexString(register[y]) + " and " + Integer.toHexString(register[x]));
                        if (register[y] > register[x]) {
                            register[0xF] = 1;
                        } else {
                            register[0xF] = 0;
                        }
                        register[x] = (byte) ((register[y] - register[x]) & 0xFF);
                    }
                    case 0x000E -> {
                        System.out.println(Integer.toHexString(opcode) + " - checked if " + Integer.toBinaryString(opcode) + " has 1 as MSB");
                        if (((register[x] >> 7) & 0x1) == 1) { // todo make this a bit more swag by saying register = condition
                            register[0xF] = 1;
                        } else {
                            register[0xF] = 0;
                        }
                        register[x] = (register[x] <<= 1);
                    }
                }
                break;
            // SNE Vx, Vy
            case 0x9000:
                System.out.println(Integer.toHexString(opcode) + " - compared Vx=" + Integer.toHexString(register[x]) + " and Vy=" + Integer.toHexString(register[y]) + " and +2 PC if different");
                if (register[x] != register[y]) pc+=2;
                break;
            case 0xA000:
                System.out.println(Integer.toHexString(opcode) + " - set I to " + Integer.toHexString(nnn));
                I = nnn;
                break;
            case 0xB000:
                System.out.println(Integer.toHexString(opcode) + " - set PC to " + Integer.toHexString(nnn+register[0]));
                pc = (char)(nnn + register[0]);
                break;
            case 0xC000:
                // todo
                System.err.println(Integer.toHexString(opcode) + " - unsupported instruction (TODO)");
                break;
            case 0xD000:
                // todo
                System.err.println(Integer.toHexString(opcode) + " - unsupported graphical instruction (TODO)");
                break;
            case 0xE000:
                switch (opcode & 0x00FF) {
                    case 0x009E -> {
                        break;
                    }
                }
                break;
        }
        pc += 2;
    }


    public void loadProgram(String path) throws IOException {
        try (InputStream stream = new FileInputStream(path)) {
            byte[] bs = new byte[1];
            int count = 0, i = 0;
            while((count = stream.readNBytes(bs, 0, bs.length)) != 0) {
                for (byte b : bs) {
                    memory[MEMORY_OFFSET + i++] = b;
                }
            }
        }
    }
}
