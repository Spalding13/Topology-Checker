package com.company.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleRedirect {
    public static void redirectSystemStreams(TextArea textArea) {
        OutputStream out = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                synchronized (buffer) {
                    buffer.append((char) b);
                    if (b == '\n') {
                        flushBuffer();
                    }
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                synchronized (buffer) {
                    buffer.append(new String(b, off, len));
                    // if there's a newline in the appended text, flush the buffer so the TextArea stays responsive
                    if (buffer.indexOf("\n") >= 0) {
                        flushBuffer();
                    }
                }
            }

            @Override
            public void flush() {
                flushBuffer();
            }

            private void flushBuffer() {
                final String text;
                synchronized (buffer) {
                    if (buffer.length() == 0) return;
                    text = buffer.toString();
                    buffer.setLength(0);
                }
                Platform.runLater(() -> textArea.appendText(text));
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
