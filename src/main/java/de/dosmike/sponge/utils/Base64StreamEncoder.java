package de.dosmike.sponge.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** This implementation will write the padding when flushed, instead of when closed like Base64.encoder().wrap(). The
 * Alternative Base64.encoder().encode(byte[]) would require a lot of memory and I don't want that.
 * <br><b>This is a utility implementation, not a full-blown generic class. Use is limited!</b>
 * */
public class Base64StreamEncoder extends OutputStream {

    final byte[] dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);
    int buffer=0;
    int buffered=0;
    int modulo3=0;
    OutputStream wrapped;

    public Base64StreamEncoder(OutputStream target) {
        wrapped = target;
    }
    @Override
    public void write(int b) throws IOException {
        buffer = (buffer << 8) | (b & 0xff);
        buffered += 8;
        while (buffered >= 6) {
            buffered -= 6;
            int b64 = 0b00111111 & (buffer >> buffered);
            wrapped.write(dictionary[b64]);
            if (--modulo3<0) modulo3=2;
        }
    }

    /** Writes padding, resets padding counter and flushes the stream */
    @Override
    public void flush() throws IOException {
        if (modulo3>1) wrapped.write('=');
        if (modulo3>0) wrapped.write('=');
        modulo3 = 0;
        wrapped.flush();
    }

}
