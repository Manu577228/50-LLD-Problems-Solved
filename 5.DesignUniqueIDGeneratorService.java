/* -------------------------------------------------------- */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* ---------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj        */
/*    Github :  https://github.com/Manu577228                  */
/* ----------------------------------------------------------- */

import java.util.concurrent.locks.ReentrantLock;

class UniqueIDGenerator {
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private final ReentrantLock lock = new ReentrantLock();

    public UniqueIDGenerator(long machineId) {
        this.machineId = machineId & 0x3FF; // 10 bits for machine ID
    }

    private long currentMillis() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long lastTs) {
        long ts = currentMillis();
        while (ts <= lastTs) {
            ts = currentMillis();
        }
        return ts;
    }

    public long getId() {
        lock.lock();
        try {
            long ts = currentMillis();
            if (ts == lastTimestamp) {
                sequence = (sequence + 1) & 0xFFF; // 12-bit sequence
                if (sequence == 0) {
                    ts = waitNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }
            lastTimestamp = ts;
            return (ts << 22) | (machineId << 12) | sequence;
        } finally {
            lock.unlock();
        }
    }
}

public class IDGeneratorDemo {
    public static void main(String[] args) {
        UniqueIDGenerator generator = new UniqueIDGenerator(42);
        System.out.println("Generating 5 unique IDs:");
        for (int i = 0; i < 5; i++) {
            long uid = generator.getId();
            System.out.println(uid);
        }
    }
}

