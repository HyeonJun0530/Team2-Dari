package com.wootecam.festivals.global.queue;

import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.queue.exception.QueueOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryQueue<T> implements CustomQueue<T> {

    // 백오프 관련 상수
    private static final long INITIAL_BACKOFF_MS = 2;
    private static final long MAX_BACKOFF_MS = 1000;
    private static final int MAX_RETRIES = 5;
    private static final int DEFAULT_QUEUE_SIZE = 1000;

    // ConcurrentLinkedQueue: 락-프리 알고리즘을 사용하여 높은 동시성을 제공
    private final ConcurrentLinkedQueue<T> queue;
    // AtomicInteger: 스레드 안전한 정수 카운터
    private final AtomicInteger size;
    private final int capacity;

    public InMemoryQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new ConcurrentLinkedQueue<>();
        this.size = new AtomicInteger(0);
    }

    public InMemoryQueue() {
        this(DEFAULT_QUEUE_SIZE);
    }

    @Override
    public void offer(T item) throws QueueFullException, QueueOperationException {
        if (item == null) {
            throw new IllegalArgumentException("큐에 null을 넣을 수 없습니다.");
        }

        long backoffMs = INITIAL_BACKOFF_MS;
        int retries = 0;

        while (true) {
            int currentSize = size.get();
            if (currentSize < capacity) {
                // CAS(Compare-And-Swap)를 사용하여 안전하게 크기를 증가
                if (size.compareAndSet(currentSize, currentSize + 1)) {
                    queue.offer(item);
                    return;
                }
            } else {
                // 큐가 가득 찼을 때 지수 백오프 적용
                if (retries >= MAX_RETRIES) {
                    throw new QueueFullException("큐가 가득 찼습니다. 최대 재시도 횟수를 초과했습니다.");
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new QueueOperationException("큐 작업 중 인터럽트가 발생했습니다.");
                }
                backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                retries++;
            }
        }
    }

    @Override
    public T poll() {
        T item = queue.poll();
        if (item != null) {
            // 항목을 성공적으로 꺼냈을 때만 크기 감소
            size.decrementAndGet();
        }
        return item;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public List<T> pollBatch(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("배치 사이즈는 0보다 커야합니다.");
        }

        List<T> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            T item = poll();
            if (item == null) {
                break;
            }
            batch.add(item);
        }
        return batch;
    }

    @Override
    public void clear() {
        queue.clear();
        size.set(0);
    }
}