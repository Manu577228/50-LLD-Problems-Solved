/* ----------------------------------------------------------------------------  
 ___ _                      _              _  
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)  
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |  
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |  
                                        |__/  
-----------------------------------------------------------------------------

UML (simplified):

+----------------+            1..*         +---------------+
|  ParkingLot    |--------------------------|    Level      |
+----------------+                         +---------------+
| - levels:[]    |                         | - levelId     |
| - ticketMap    |                         | - slots:[]    |
| - plateMap     |                         +---------------+
| - lock         |                                  |
+----------------+                                  |
       | 1                                           | 1..*
       |                                             v
       |                                       +---------------+
       |                                       | ParkingSlot   |
       |                                       +---------------+
       |                                       | - slotId      |
       |                                       | - size        |
       |                                       | - occupiedBy  |
       |                                       +---------------+
       |
       v
+---------------+
|   Ticket      |
+---------------+
| - ticketId    |
| - plate       |
| - slotId      |
| - parkedAt    |
+---------------+

Vehicle (data structure): {plate, vtype}
*/

import java.util.*;
import java.util.concurrent.locks.*;
import java.time.*;

public class ParkingLotDemo {

    /* ----------------------------- ENUMS ----------------------------- */
    enum VehicleType {
        MOTORCYCLE(1), CAR(2), BUS(3);
        int minSlot;
        VehicleType(int s) { this.minSlot = s; }
    }

    /* --------------------------- VEHICLE ----------------------------- */
    static class Vehicle {
        String plate;
        VehicleType type;

        Vehicle(String plate, VehicleType type) {
            this.plate = plate;
            this.type = type;
        }

        public String toString() {
            return "Vehicle(" + plate + "," + type.name() + ")";
        }
    }

    /* --------------------------- PARKING SLOT ----------------------------- */
    static class ParkingSlot {
        String slotId;
        int size;
        int levelId;
        Vehicle occupiedBy;

        ParkingSlot(String slotId, int size, int levelId) {
            this.slotId = slotId;
            this.size = size;
            this.levelId = levelId;
            this.occupiedBy = null;
        }

        boolean isFree() {
            return occupiedBy == null;
        }

        boolean fits(Vehicle v) {
            return isFree() && this.size >= v.type.minSlot;
        }

        public String toString() {
            String occ = (occupiedBy == null) ? "Free" : occupiedBy.plate;
            return "Slot(" + slotId + ",size=" + size + ",occ=" + occ + ")";
        }
    }

    /* --------------------------- LEVEL ----------------------------- */
    static class Level {
        int levelId;
        List<ParkingSlot> slots = new ArrayList<>();

        Level(int levelId, List<ParkingSlot> slots) {
            this.levelId = levelId;
            this.slots = slots;
        }
    }

    /* --------------------------- TICKET ----------------------------- */
    static class Ticket {
        String ticketId;
        String plate;
        String slotId;
        long parkedAt;

        Ticket(String ticketId, String plate, String slotId, long parkedAt) {
            this.ticketId = ticketId;
            this.plate = plate;
            this.slotId = slotId;
            this.parkedAt = parkedAt;
        }

        public String toString() {
            return "Ticket(" + ticketId + ",plate=" + plate + ",slot=" + slotId + ",at=" + parkedAt + ")";
        }
    }

    /* --------------------------- PARKING LOT ----------------------------- */
    static class ParkingLot {
        List<Level> levels = new ArrayList<>();
        Map<String, Ticket> ticketMap = new HashMap<>();
        Map<String, String> plateMap = new HashMap<>();
        ReentrantLock lock = new ReentrantLock();

        ParkingLot(List<Level> levels) {
            this.levels = levels;
        }

        static ParkingLot createDemoLot(int levelCount, int slotsPerLevel) {
            List<Level> levels = new ArrayList<>();
            for (int li = 0; li < levelCount; li++) {
                List<ParkingSlot> slots = new ArrayList<>();
                for (int si = 0; si < slotsPerLevel; si++) {
                    int size;
                    if (si < 2) size = 1;
                    else if (si < slotsPerLevel - 2) size = 2;
                    else size = 3;
                    String slotId = "L" + (li + 1) + "-S" + (si + 1);
                    slots.add(new ParkingSlot(slotId, size, li + 1));
                }
                levels.add(new Level(li + 1, slots));
            }
            return new ParkingLot(levels);
        }

        private String generateTicketId() {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        Ticket parkVehicle(Vehicle v) {
            lock.lock();
            try {
                if (plateMap.containsKey(v.plate)) {
                    return ticketMap.get(plateMap.get(v.plate));
                }
                for (Level lvl : levels) {
                    for (ParkingSlot s : lvl.slots) {
                        if (s.fits(v)) {
                            s.occupiedBy = v;
                            String tid = generateTicketId();
                            Ticket t = new Ticket(tid, v.plate, s.slotId, System.currentTimeMillis());
                            ticketMap.put(tid, t);
                            plateMap.put(v.plate, tid);
                            return t;
                        }
                    }
                }
                return null; // no space
            } finally {
                lock.unlock();
            }
        }

        Map<String, Object> leave(String ticketId) {
            lock.lock();
            try {
                Ticket t = ticketMap.get(ticketId);
                if (t == null) return null;

                for (Level lvl : levels) {
                    for (ParkingSlot s : lvl.slots) {
                        if (s.slotId.equals(t.slotId)) {
                            s.occupiedBy = null;
                            break;
                        }
                    }
                }

                long now = System.currentTimeMillis();
                long seconds = (now - t.parkedAt) / 1000;
                long hours = (seconds + 3599) / 3600;
                long fee = (hours > 0 ? hours * 10 : 10);

                ticketMap.remove(ticketId);
                plateMap.remove(t.plate);

                Map<String, Object> res = new HashMap<>();
                res.put("ticket", t);
                res.put("fee", fee);
                res.put("durationSeconds", seconds);
                return res;
            } finally {
                lock.unlock();
            }
        }

        Map<String, Object> status() {
            lock.lock();
            try {
                int total = 0, free = 0;
                Map<Integer, Map<String, Integer>> perLevel = new HashMap<>();
                for (Level lvl : levels) {
                    int lvlTotal = lvl.slots.size();
                    int lvlFree = 0;
                    for (ParkingSlot s : lvl.slots) {
                        if (s.isFree()) lvlFree++;
                    }
                    Map<String, Integer> info = new HashMap<>();
                    info.put("total", lvlTotal);
                    info.put("free", lvlFree);
                    perLevel.put(lvl.levelId, info);
                    total += lvlTotal;
                    free += lvlFree;
                }
                int occupied = total - free;
                Map<String, Object> result = new HashMap<>();
                result.put("totalSlots", total);
                result.put("freeSlots", free);
                result.put("occupied", occupied);
                result.put("perLevel", perLevel);
                return result;
            } finally {
                lock.unlock();
            }
        }

        String dumpSlots() {
            lock.lock();
            try {
                StringBuilder sb = new StringBuilder();
                for (Level lvl : levels) {
                    sb.append("Level ").append(lvl.levelId).append(":\n");
                    for (ParkingSlot s : lvl.slots) {
                        String occ = (s.occupiedBy == null) ? "Free" : s.occupiedBy.plate;
                        sb.append("  ").append(s.slotId)
                          .append(" size=").append(s.size)
                          .append(" -> ").append(occ).append("\n");
                    }
                }
                return sb.toString();
            } finally {
                lock.unlock();
            }
        }
    }

    /* --------------------------- DEMO MAIN ----------------------------- */
    public static void main(String[] args) throws InterruptedException {
        ParkingLot pl = ParkingLot.createDemoLot(2, 8);
        System.out.println("=== Parking Lot Created ===");
        System.out.println(pl.status());
        System.out.println();

        List<Vehicle> vehicles = Arrays.asList(
                new Vehicle("KA01AA1111", VehicleType.CAR),
                new Vehicle("KA01BB2222", VehicleType.CAR),
                new Vehicle("KA01CC3333", VehicleType.MOTORCYCLE),
                new Vehicle("KA01DD4444", VehicleType.BUS)
        );

        List<Ticket> tickets = new ArrayList<>();
        for (Vehicle v : vehicles) {
            Ticket t = pl.parkVehicle(v);
            if (t != null) {
                System.out.println("Parked " + v.plate + " as " + v.type.name() + " -> ticket " + t.ticketId + " slot " + t.slotId);
                tickets.add(t);
            } else {
                System.out.println("No space for " + v.plate + " (" + v.type.name() + ")");
            }
        }

        System.out.println("\nStatus after parking: " + pl.status());
        System.out.println("\nSlot dump:\n" + pl.dumpSlots());

        Thread.sleep(1200);
        if (!tickets.isEmpty()) {
            Ticket t0 = tickets.get(0);
            Map<String, Object> res = pl.leave(t0.ticketId);
            if (res != null) {
                System.out.println("\nVehicle " + t0.plate + " left. Fee: " + res.get("fee") +
                        " Duration(s): " + res.get("durationSeconds"));
            }
        }

        System.out.println("\nFinal status: " + pl.status());
        System.out.println("\nFinal slot dump:\n" + pl.dumpSlots());
    }
}
