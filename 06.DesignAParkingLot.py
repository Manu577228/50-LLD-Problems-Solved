# Design and implement an object-oriented Parking Lot system that supports multiple levels 
# and slot sizes. The system should allow vehicles to park, unpark, and display the parking status 
# — all within a single-file.

import threading
import time
import uuid
from enum import Enum
from typing import Optional, Dict, List

class VehicleType(Enum):
    MOTORCYCLE = 1
    CAR = 2
    BUS = 3

VEHICLE_TO_MIN_SLOT = {
    VehicleType.MOTORCYCLE: 1,
    VehicleType.CAR: 2,
    VehicleType.BUS: 3,
}

class Vehicle:
    def __init__(self, plate:str, vtype: VehicleType):
        self.plate = plate
        self.vtype = vtype

    def __repr__(self):
        return f"Vehicle({self.plate}, {self.vtype.name})"
    
class ParkingSlot:
    def __init__(self, slot_id:str, size: int, level_id: int):
        self.slot_id = slot_id
        self.size = size
        self.level_id = level_id
        self.occupied_by: Optional[Vehicle] = None

    def is_free(self):
        return self.occupied_by is None
    
    def fits(self, vehicle: Vehicle) -> bool:
        return self.is_free() and self.size >= VEHICLE_TO_MIN_SLOT[vehicle.vtype]
    
    def __repr__(self):
        occ = self.occupied_by.plate if self.occupied_by else "Free"
        return f"Slot({self.slot_id}, size = {self.size}, occ = {occ})"
    
class Level:
    def __init__(self, level_id: int, slots: List[ParkingSlot]):
        self.level_id = level_id
        self.slots = slots

class Ticket:
    def __init__(self, ticket_id: str, plate: str, slot_id: str, parked_at: float):
        self.ticket_id = ticket_id
        self.plate = plate
        self.slot_id = slot_id
        self.parked_at = parked_at

    def __repr__(self):
        return f"Ticket({self.ticket_id}, plate = {self.plate}, slot = {self.slot_id}, at = {self.parked_at})"
    
class ParkingLot:
    def __init__(self, levels: List[Level]):
        self.levels = levels
        self.ticket_map: Dict[str, Ticket] = {}
        self.plate_map: Dict[str, str] = {}
        self.lock = threading.Lock()

    @classmethod
    def create_demo_lot(cls, level_count = 2, slots_per_level = 10):
        levels = []
        for li in range(level_count):
            slots = []
            for si in range(slots_per_level):
                if si < 2:
                    size = 1
                elif si < slots_per_level - 2:
                    size = 2
                else:
                    size = 3
                slot_id = f"L{li + 1}-S{si + 1}"
                slots.append(ParkingSlot(slot_id, size, li + 1))
            levels.append(Level(li + 1, slots))
        return cls(levels)
    
    def _generate_ticket_id(self) -> str:
        return str(uuid.uuid4())[:8]
    
    def park_vehicle(self, vehicle: Vehicle) -> Optional[Ticket]:
        with self.lock:
            if vehicle.plate in self.plate_map:
                tid = self.plate_map[vehicle.plate]
                return self.ticket_map.get(tid)
            
            for lvl in self.levels:
                for slot in lvl.slots:
                    if slot.fits(vehicle):
                        slot.occupied_by = vehicle
                        ticket_id = self._generate_ticket_id()
                        t = Ticket(ticket_id, vehicle.plate, slot.slot_id, time.time())
                        self.ticket_map[ticket_id] = t
                        self.plate_map[vehicle.plate] = ticket_id
                        return t
            return None
                    
    def leave(self, ticket_id: str) -> Optional[dict]:
        with self.lock:
            t = self.ticket_map.get(ticket_id)
            if not t:
                return None
            for lvl in self.levels:
                for slot in lvl.slots:
                    if slot.slot_id == t.slot_id:
                        slot.occupied_by = None
                        break

            parked_seconds = time.time() - t.parked_at
            hours = int((parked_seconds + 3599) // 3600)
            fee = hours * 10 if hours > 0 else 10
            del self.ticket_map[ticket_id]
            if t.plate in self.plate_map:
                del self.plate_map[t.plate]
            return {"ticket": t, "fee": fee, "duration_seconds": int(parked_seconds)}
        
    def status(self) -> dict:
        with self.lock:
            total = 0
            free = 0
            per_level = {}

            for lvl in self.levels:
                lvl_total = len(lvl.slots)
                lvl_free = sum(1 for s in lvl.slots if s.is_free())
                per_level[lvl.level_id] = {"total": lvl_total, "free": lvl_free}
                total += lvl_total
                free += lvl_free
            occupied = total - free
            return {"total_slots": total, "free_slots": free, "occupied": occupied, "per_level": per_level}
        
    def find_by_plate(self, plate: str) -> Optional[Ticket]:
        with self.lock:
            tid = self.plate_map.get(plate)
            if tid:
                return self.ticket_map.get(tid)
            return None
        
    def dump_slots(self) -> str:
        with self.lock:
            lines = []
            for lvl in self.levels:
                lines.append(f"Level {lvl.level_id}:")
                for s in lvl.slots:
                    occ = s.occupied_by.plate if s.occupied_by else "Free"
                    lines.append(f" {s.slot_id} size = {s.size} -> {occ}")
            return "\n".join(lines)
        
def demo_scenario():
    pl = ParkingLot.create_demo_lot(level_count=2, slots_per_level=8)
    print("=== Parking Lot Created ===")
    print(pl.status())
    print()

    # Park vehicles
    cars = [
        Vehicle("KA01AA1111", VehicleType.CAR),
        Vehicle("KA01BB2222", VehicleType.CAR),
        Vehicle("KA01CC3333", VehicleType.MOTORCYCLE),
        Vehicle("KA01DD4444", VehicleType.BUS),
    ]
    tickets = []
    for v in cars:
        t = pl.park_vehicle(v)
        if t:
            print(f"Parked {v.plate} as {v.vtype.name} -> ticket {t.ticket_id} slot {t.slot_id}")
            tickets.append(t)
        else:
            print(f"No space for {v.plate} ({v.vtype.name})")
    print()
    print("Status after parking:", pl.status())
    print()
    print("Slot dump:\n", pl.dump_slots())
    print()

    # Leave one car (simulate some time)
    time.sleep(1.2)  # small pause to create non-zero duration
    if tickets:
        t0 = tickets[0]
        res = pl.leave(t0.ticket_id)
        if res:
            print(f"Vehicle {res['ticket'].plate} left. Fee: {res['fee']} Duration(s): {res['duration_seconds']}")
        else:
            print("Invalid ticket for leaving.")
    print()
    print("Final status:", pl.status())
    print()
    print("Final slot dump:\n", pl.dump_slots())


if __name__ == "__main__":
    demo_scenario()


        
        
        

