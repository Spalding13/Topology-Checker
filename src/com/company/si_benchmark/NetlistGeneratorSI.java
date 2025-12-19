package com.company.si_benchmark;

public class NetlistGeneratorSI {

    // ------------------------------------------------------------
    // Generate POSITIVE netlist: 1 real match + N near misses
    // ------------------------------------------------------------
    public static String generatePositiveNetlist(int copies) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                ******************************************************
                * AUTO-GENERATED SI BENCHMARK NETLIST (POSITIVE CASE)
                ******************************************************

                .SUBCKT si_benchmark GND PAD VDD

                """);

        // ------------------------------------------------------------
        // REAL MATCH — exactly what DeviceFactory + ESDA pattern expect
        // ------------------------------------------------------------
        sb.append("""
                * --- REAL MATCH ---
                XD_REAL_D   GND PAD netD_real sub! esddiode   areapd=1e-11 perimpd=1u nf=1
                XP_REAL_P   PAD VDD netP_real sub! esdvertpnp areapd=1e-11 perimpd=1u nf=1

                """);

        // ------------------------------------------------------------
        // NEAR MISS DEVICES
        // ------------------------------------------------------------
        for (int i = 0; i < copies; i++) {
            sb.append(nearMiss(i));
        }

        sb.append(".ENDS\n");
        return sb.toString();
    }

    // ------------------------------------------------------------
    // Generate NEGATIVE netlist: only near misses (no real match)
    // ------------------------------------------------------------
    public static String generateNegativeNetlist(int copies) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                ******************************************************
                * AUTO-GENERATED SI BENCHMARK NETLIST (NEGATIVE CASE)
                ******************************************************

                .SUBCKT si_benchmark GND PAD VDD

                """);

        for (int i = 0; i < copies; i++) {
            sb.append(nearMiss(i));
        }

        sb.append(".ENDS\n");
        return sb.toString();
    }

    // ------------------------------------------------------------
    // NEAR MISS generator
    // Must produce VALID CDL devices but NOT match the real pattern.
    // ------------------------------------------------------------
    private static String nearMiss(int i) {

        int type = i % 3;

        return switch (type) {

            // 1) Wrong diode nets
            case 0 -> String.format("""
                    * Near Miss %d — wrong diode nets
                    XD_FD_%d   PAD VDD netD_%d  sub! esddiode   areapd=1e-11 perimpd=1u nf=1
                    XP_FP_%d   PAD VDD netP_%d  sub! esdvertpnp areapd=1e-11 perimpd=1u nf=1

                    """, i, i, i, i, i);

            // 2) Wrong PNP nets
            case 1 -> String.format("""
                    * Near Miss %d — wrong PNP nets
                    XD_FD_%d   GND PAD netD_%d  sub! esddiode   areapd=1e-11 perimpd=1u nf=1
                    XP_FP_%d   GND PAD netP_%d  sub! esdvertpnp areapd=1e-11 perimpd=1u nf=1

                    """, i, i, i, i, i);

            // 3) Valid nets but wrong MODELS
            default -> String.format("""
                    * Near Miss %d — wrong models
                    XD_FD_%d   GND PAD netD_%d  sub! dummy_diode areapd=1e-11 perimpd=1u nf=1
                    XP_FP_%d   PAD VDD netP_%d  sub! dummy_pnp   areapd=1e-11 perimpd=1u nf=1

                    """, i, i, i, i, i);
        };
    }
}
