package me.kenzierocks.converse.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * A really big class for recording outgoing messages on a PrintStream.
 */
public class RecordingPrintStream extends PrintStream {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final PrintStream target;
    private final ByteArrayOutputStream recorder;
    private final Charset charset;

    public RecordingPrintStream(PrintStream target) {
        this(target, DEFAULT_CHARSET);
    }

    public RecordingPrintStream(PrintStream target, Charset charset) {
        super(new ByteArrayOutputStream());

        this.target = checkNotNull(target);
        this.charset = checkNotNull(charset);

        this.recorder = (ByteArrayOutputStream) this.out;
    }

    public byte[] toByteArray() {
        return this.recorder.toByteArray();
    }

    @Override
    public String toString() {
        ByteBuffer bytes = ByteBuffer.wrap(toByteArray());
        CharBuffer string = this.charset.decode(bytes);
        return string.toString();
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        this.target.write(b);
    }

    @Override
    public void flush() {
        super.flush();
        this.target.flush();
    }

    @Override
    public void close() {
        this.target.close();
        super.close();
    }

    @Override
    public boolean checkError() {
        return super.checkError() || this.target.checkError();
    }

    @Override
    public void write(int b) {
        super.write(b);
        this.target.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        this.target.write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        super.print(b);
        this.target.print(b);
    }

    @Override
    public void print(char c) {
        super.print(c);
        this.target.print(c);
    }

    @Override
    public void print(int i) {
        super.print(i);
        this.target.print(i);
    }

    @Override
    public void print(long l) {
        super.print(l);
        this.target.print(l);
    }

    @Override
    public void print(float f) {
        super.print(f);
        this.target.print(f);
    }

    @Override
    public void print(double d) {
        super.print(d);
        this.target.print(d);
    }

    @Override
    public void print(char[] s) {
        super.print(s);
        this.target.print(s);
    }

    @Override
    public void print(String s) {
        super.print(s);
        this.target.print(s);
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
        this.target.print(obj);
    }

    @Override
    public void println() {
        super.println();
        this.target.println();
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(char x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(int x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(long x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(float x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(double x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(String x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public void println(Object x) {
        super.println(x);
        this.target.println(x);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        this.target.printf(format, args);
        return super.printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        this.target.printf(l, format, args);
        return super.printf(l, format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        this.target.format(format, args);
        return super.format(format, args);
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        this.target.format(l, format, args);
        return super.format(l, format, args);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        this.target.append(csq);
        return super.append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        this.target.append(csq, start, end);
        return super.append(csq, start, end);
    }

    @Override
    public PrintStream append(char c) {
        this.target.append(c);
        return super.append(c);
    }

}
