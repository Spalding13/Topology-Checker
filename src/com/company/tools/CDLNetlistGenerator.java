package com.company.tools;

import java.io.FileWriter;
import java.io.IOException;

public class CDLNetlistGenerator {

    public static void main(String[] args) throws IOException {

        // ----------- CONFIGURE HERE -------------
        int diodeCount = 10000;     // total esddiode devices
        int groupSize  = 50;        // parallel devices per net
        int pnpCount   = 1000;      // esdvertpnp count
        String outputPath = "E:\\ESD Checks\\input\\netlist_ultra_large.cdl";
        // ----------------------------------------

        generateLargeNetlist(diodeCount, groupSize, pnpCount, outputPath);
        System.out.println("✔ LARGE CDL generated at: " + outputPath);
    }


    public static void generateLargeNetlist(int diodeCount, int groupSize, int pnpCount, String path)
            throws IOException {

        FileWriter fw = new FileWriter(path);

        fw.write("******************************************************\n");
        fw.write("* Synthetic Large-Scale CDL Netlist for Benchmarking\n");
        fw.write("******************************************************\n\n");

        fw.write(".INCLUDE dummy_subckt.cdl\n");
        fw.write(".PARAM wireopt=1111111\n\n");

        fw.write(".SUBCKT parallel_reduce GND PAD VDD\n");
        fw.write("*.PININFO GND:I PAD:I VDD:I\n\n");

        fw.write("* ---- AUTO-GENERATED ESD DIODES ----\n");

        int netIndex = 1;
        int countInGroup = 0;

        for (int i = 1; i <= diodeCount; i++) {

            if (countInGroup == 0)
                fw.write(String.format("* ---- Group %d (parallel) ----\n", netIndex));

            fw.write(String.format(
                    "XD%d GND PAD net%d sub! esddiode areapd=2.925e-11 perimpd=150u nf=1\n",
                    i, netIndex
            ));

            countInGroup++;

            // new net group every groupSize diodes
            if (countInGroup >= groupSize) {
                netIndex++;
                countInGroup = 0;
            }
        }

        fw.write("\n* ---- AUTO-GENERATED ESD PNP DEVICES ----\n");
        for (int j = 1; j <= pnpCount; j++) {
            fw.write(String.format(
                    "XP%d PAD VDD netX%d sub! esdvertpnp areapd=5.85e-11 perimpd=300u nf=2\n",
                    j, j
            ));
        }

        fw.write(".ENDS\n");
        fw.close();
    }
}
