# Design an in-memory Geo-Location Service that can store locations, update/delete 
# them, and efficiently find all nearby locations within a given radius 
# using the Haversine distance formula. The system must be thread-safe, 
# use no external database, and run fully in a single Python file.

""" --------------------------------------------------------
   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
/---------------------------------------------------------
   Youtube: https://youtube.com/@code-with-Bharadwaj
   Github : https://github.com/Manu577228
--------------------------------------------------------- """
import math
import threading
from typing import Dict, Tuple, List

class DistanceStrategy:
    def calculate(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        raise NotImplementedError("DistanceStrategy.calculate must be implemented by subclass")

class HaversineDistance(DistanceStrategy):
    def calculate(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        R = 6371.0
        dlat = math.radians(lat2 - lat1)
        dlon = math.radians(lon2 - lon1)
        a = (math.sin(dlat / 2) ** 2 +
             math.cos(math.radians(lat1)) *
             math.cos(math.radians(lat2)) *
             math.sin(dlon / 2) ** 2)
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        return R * c

class Location:
    def __init__(self, name: str, lat: float, lon: float):
        self.name = name
        self.lat = lat
        self.lon = lon

class GeoService:
    _instance = None
    _singleton_lock = threading.Lock()

    def __new__(cls):
        if cls._instance is None:
            with cls._singleton_lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
                    cls._instance._locations = {}
                    cls._instance._id_counter = 1
                    cls._instance._lock = threading.Lock()
                    cls._instance._distance_strategy = HaversineDistance()
        return cls._instance

    def add_location(self, name: str, lat: float, lon: float) -> int:
        with self._lock:
            uid = self._id_counter
            self._locations[uid] = Location(name, lat, lon)
            self._id_counter += 1
            return uid

    def update_location(self, uid: int, name: str = None, lat: float = None, lon: float = None) -> bool:
        with self._lock:
            if uid not in self._locations:
                return False
            loc = self._locations[uid]
            if name is not None:
                loc.name = name
            if lat is not None:
                loc.lat = lat
            if lon is not None:
                loc.lon = lon
            return True

    def delete_location(self, uid: int) -> bool:
        with self._lock:
            if uid in self._locations:
                del self._locations[uid]
                return True
            return False

    def get_nearby(self, lat: float, lon: float, radius_km: float) -> List[Tuple[float, str]]:
        results: List[Tuple[float, str]] = []
        for uid, loc in self._locations.items():
            dist = self._distance_strategy.calculate(lat, lon, loc.lat, loc.lon)
            if dist <= radius_km:
                results.append((dist, loc.name))
        results.sort(key=lambda x: x[0])
        return results

    def set_distance_strategy(self, strategy: DistanceStrategy) -> None:
        with self._lock:
            self._distance_strategy = strategy

if __name__ == "__main__":
    geo = GeoService()

    id1 = geo.add_location("Restaurant A", 28.6139, 77.2090)
    id2 = geo.add_location("Cafe B", 28.7041, 77.1025)

    nearby = geo.get_nearby(28.61, 77.20, 15.0)
    print("Nearby locations (distance_km, name):")
    for distance_km, name in nearby:
        print(f"{distance_km:.2f} km — {name}")

    geo.update_location(id1, name="Restaurant A (Updated)")
    geo.delete_location(id2)

    nearby_after = geo.get_nearby(28.61, 77.20, 15.0)
    
    print("\nAfter update/delete:")
    for distance_km, name in nearby_after:
        print(f"{distance_km:.2f} km — {name}")
