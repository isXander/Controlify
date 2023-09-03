package dev.isxander.controlify.utils;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public class TrackingBodySubscriber<T> implements HttpResponse.BodySubscriber<T> {
    private final HttpResponse.BodySubscriber<T> delegate;
    private final TrackingConsumer consumer;

    private long receivedBytes;
    private final long contentLengthIfKnown;

    public TrackingBodySubscriber(HttpResponse.BodySubscriber<T> delegate, TrackingConsumer consumer, long contentLengthIfKnown) {
        this.delegate = delegate;
        this.consumer = consumer;
        this.contentLengthIfKnown = contentLengthIfKnown;
    }

    @Override
    public CompletionStage<T> getBody() {
        return delegate.getBody();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        consumer.start().accept(contentLengthIfKnown);
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext(List<ByteBuffer> item) {
        receivedBytes += countBytes(item);
        delegate.onNext(item);
        consumer.progressConsumer().accept(receivedBytes, contentLengthIfKnown);
    }

    @Override
    public void onError(Throwable throwable) {
        consumer.onComplete().accept(Optional.of(throwable));
        delegate.onError(throwable);
    }

    @Override
    public void onComplete() {
        consumer.onComplete().accept(Optional.empty());
        delegate.onComplete();
    }

    private long countBytes(List<ByteBuffer> buffers) {
        return buffers.stream().mapToLong(ByteBuffer::remaining).sum();
    }

    public static <T> HttpResponse.BodyHandler<T> bodyHandler(HttpResponse.BodyHandler<T> delegate, TrackingConsumer consumer) {
        return (responseInfo) -> new TrackingBodySubscriber<>(delegate.apply(responseInfo), consumer, responseInfo.headers().firstValueAsLong("Content-Length").orElse(-1L));
    }
}
