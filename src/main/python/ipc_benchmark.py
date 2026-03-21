import multiprocessing
import multiprocessing.shared_memory
import random
import struct
import sys
import time


def _queue_worker(q_in, q_out):
    value = q_in.get()
    q_out.put(value)


def bench_queue(value, rounds):
    latencies = []
    for _ in range(rounds):
        q_in = multiprocessing.Queue()
        q_out = multiprocessing.Queue()
        p = multiprocessing.Process(target=_queue_worker, args=(q_in, q_out))
        p.start()
        t0 = time.perf_counter_ns()
        q_in.put(value)
        received = q_out.get()
        elapsed = time.perf_counter_ns() - t0
        p.join()
        latencies.append(elapsed)
        assert received == value
    return sum(latencies) // len(latencies)


def _pipe_worker(conn):
    value = conn.recv()
    conn.send(value)
    conn.close()


def bench_pipe(value, rounds):
    latencies = []
    for _ in range(rounds):
        parent_conn, child_conn = multiprocessing.Pipe()
        p = multiprocessing.Process(target=_pipe_worker, args=(child_conn,))
        p.start()
        child_conn.close()
        t0 = time.perf_counter_ns()
        parent_conn.send(value)
        received = parent_conn.recv()
        elapsed = time.perf_counter_ns() - t0
        parent_conn.close()
        p.join()
        latencies.append(elapsed)
        assert received == value
    return sum(latencies) // len(latencies)


def _shm_worker(shm_name, ready_flag, done_flag):
    ready_flag.wait()
    shm = multiprocessing.shared_memory.SharedMemory(name=shm_name)
    value = struct.unpack('q', bytes(shm.buf[:8]))[0]
    shm.close()
    done_flag.set()


def bench_shared_memory(value, rounds):
    latencies = []
    for _ in range(rounds):
        shm = multiprocessing.shared_memory.SharedMemory(create=True, size=8)
        ready_flag = multiprocessing.Event()
        done_flag = multiprocessing.Event()
        p = multiprocessing.Process(target=_shm_worker, args=(shm.name, ready_flag, done_flag))
        p.start()
        shm.buf[:8] = struct.pack('q', value)
        t0 = time.perf_counter_ns()
        ready_flag.set()
        done_flag.wait()
        elapsed = time.perf_counter_ns() - t0
        p.join()
        shm.close()
        shm.unlink()
        latencies.append(elapsed)
    return sum(latencies) // len(latencies)


if __name__ == '__main__':
    rounds = int(sys.argv[1]) if len(sys.argv) > 1 else 5
    rnd = random.Random(99)
    value = rnd.randint(0, 1_000_000)

    avg_queue = bench_queue(value, rounds)
    avg_pipe = bench_pipe(value, rounds)
    avg_shm = bench_shared_memory(value, rounds)

    print(f"Queue:         {avg_queue / 1_000_000:.3f}")
    print(f"Pipe:          {avg_pipe / 1_000_000:.3f}")
    print(f"SharedMemory:  {avg_shm / 1_000_000:.3f}")
