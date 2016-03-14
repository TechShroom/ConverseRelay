package me.kenzierocks.converse.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

public class SplittingOutputStream extends OutputStream {

    @FunctionalInterface
    private static interface IOConsumer<T> {

        void consume(T io) throws IOException;

    }

    public static interface OutputStreamLike extends Closeable, Flushable {

        static OutputStreamLike from(PrintStream stream) {
            return new OutputStreamLike() {

                @Override
                public void write(byte[] b, int off, int len)
                        throws IOException {
                    stream.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    stream.write(b);
                }

                @Override
                public void write(int b) throws IOException {
                    stream.write(b);
                }

                @Override
                public void flush() throws IOException {
                    stream.flush();
                }

                @Override
                public void close() throws IOException {
                    stream.close();
                }

            };
        }

        static OutputStreamLike from(OutputStream stream) {
            return new OutputStreamLike() {

                @Override
                public void write(byte[] b, int off, int len)
                        throws IOException {
                    stream.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    stream.write(b);
                }

                @Override
                public void write(int b) throws IOException {
                    stream.write(b);
                }

                @Override
                public void flush() throws IOException {
                    stream.flush();
                }

                @Override
                public void close() throws IOException {
                    stream.close();
                }

            };
        }

        static OutputStreamLike resolve(Object o) {
            checkNotNull(o);
            if (o instanceof PrintStream) {
                return from((PrintStream) o);
            }
            if (o instanceof OutputStream) {
                return from((OutputStream) o);
            }
            throw new IllegalArgumentException(
                    "Don't know how to resolve " + o.getClass().getName()
                            + " to an OutputStreamLike object");
        }

        void write(int b) throws IOException;

        void write(byte[] b) throws IOException;

        void write(byte[] b, int off, int len) throws IOException;

    }

    public static PrintStream splittingPrintStream(Object... printStreams) {
        OutputStreamLike[] outStreams =
                Stream.of(printStreams).map(OutputStreamLike::resolve)
                        .toArray(OutputStreamLike[]::new);
        return new PrintStream(new SplittingOutputStream(outStreams));
    }

    public static PrintStream
            splittingPrintStream(OutputStreamLike... printStreams) {
        return new PrintStream(new SplittingOutputStream(printStreams));
    }

    private final List<OutputStreamLike> streams;

    public SplittingOutputStream(OutputStreamLike... outputStreams) {
        this.streams = ImmutableList.copyOf(outputStreams);
    }

    private void runOverStreams(IOConsumer<OutputStreamLike> func)
            throws IOException {
        IOException first = null;
        for (OutputStreamLike outputStream : this.streams) {
            try {
                func.consume(outputStream);
            } catch (IOException e) {
                if (first == null) {
                    first = e;
                } else {
                    first.addSuppressed(e);
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }

    @Override
    public void write(int b) throws IOException {
        runOverStreams(s -> s.write(b));
    }

    @Override
    public void write(byte[] b) throws IOException {
        runOverStreams(s -> s.write(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        runOverStreams(s -> s.write(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        runOverStreams(OutputStreamLike::flush);
    }

    @Override
    public void close() throws IOException {
        runOverStreams(OutputStreamLike::close);
    }

}
