package org.starodubov;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            out.println("no args. Exit 0");
            return;
        }
        out.printf("args = %s%n", Arrays.toString(args));
        final var originFileName = args[0];
        String cur;
        ByteBuffer upd;
        final var lineBuffers = new ArrayList<ByteBuffer>(300);
        try (final var sc = new Scanner(new File(originFileName))) {
            while (sc.hasNextLine()) {
                cur = sc.nextLine();
                upd = snake2camel(cur);
                lineBuffers.add(upd);
            }
        }

        try (var fch = FileChannel.open(Path.of(originFileName), StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            fch.truncate(fch.size());
            for (ByteBuffer buf : lineBuffers) {
                while (buf.hasRemaining()) {
                    int write = fch.write(buf);
                    if (write <= 0) {
                        break;
                    }
                }
            }

            fch.force(true);
        }
    }

    static ByteBuffer snake2camel(final String s) {
        if (s.length() > 250) {
            throw new IllegalArgumentException("size of the line > 250");
        }

        if (s.isEmpty() || s.length() == 1) {
            return ByteBuffer.wrap((s + '\n').getBytes(StandardCharsets.UTF_8));
        }

        final var buf = ByteBuffer.allocate(250);
        int i = 1;

        buf.put((byte) s.charAt(0));
        while (i < s.length() - 1) {
            if (s.charAt(i) == '_' && Character.isLetterOrDigit(s.charAt(i - 1)) && Character.isLetterOrDigit(s.charAt(i + 1))) {
                buf.put((byte) Character.toUpperCase(s.charAt(i + 1)));
                i += 2;
            } else {
                buf.put((byte) s.charAt(i));
                i++;
            }
        }

        buf.put((byte) s.charAt(s.length() - 1));
        buf.put((byte) '\n');
        buf.flip();
        return buf;
    }
}