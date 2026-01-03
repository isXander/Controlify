package dev.isxander.controlify.haptics.hd;

public record PcmFormat(
        int sampleRate,
        int channels
) {
    public int bytesPerSample() {
        return 4;
    }

    public int bytesPerFrame() {
        return bytesPerSample() * channels();
    }
}
