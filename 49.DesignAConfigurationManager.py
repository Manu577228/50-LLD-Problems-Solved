# Design an in-memory Configuration Manager that supports CRUD operations on key–value settings, 
# maintains version history, and allows rollback to any previous version. 
# The system must be thread-safe, follow the Singleton pattern, and run fully in a 
# single Python file for demo purposes.

import threading
import copy
from typing import Any, Dict, List

class ConfigurationManager:
    _instance = None
    _instance_lock = threading.Lock()

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._config_store = {}
            cls._instance._versions = []
            cls._instance._store_lock = threading.Lock()
        return cls._instance
    
    def get(self, key: str) -> Any:
        return self._config_store.get(key)
    
    def set(self, key: str, value: Any) -> None:
        self._store_lock.acquire()
        try:
            self._config_store[key] = value
            self._save_version()
        finally:
            self._store_lock.release()

    def delete(self, key: str) -> None:
        self._store_lock.acquire()
        try:
            if key in self._config_store:
                del self._config_store[key]
                self._save_version()
        finally:
            self._store_lock.release()

    def _save_version(self) -> None:
        snapshot = copy.deepcopy(self._config_store)
        self._versions.append(snapshot)

    def rollback(self, version_index: int) -> bool:
        self._store_lock.acquire()
        try:
            if 0 <= version_index < len(self._versions):
                self._config_store = copy.deepcopy(self._versions[version_index])
                self._save_version()
                return True
            else:
                return False
        finally:
            self._store_lock.release()

    def export(self) -> Dict[str, Any]:
        return copy.deepcopy(self._config_store)
    
    def show_version(self) -> None:
        for i, v in enumerate(self._versions):
            print(f"Version {i}: {v}")

if __name__ == "__main__":
    mgr = ConfigurationManager()

    mgr.set("theme", "dark")
    mgr.set("timeout", 30)
    mgr.set("volume", 80)

    print("Version 0:", mgr._versions[0])
    print("Version 1:", mgr._versions[1])
    print("Version 2:", mgr._versions[2])

    mgr.set("theme", "light")
    print("Version 3:", mgr._versions[3])

    # Rollback to version 1
    mgr.rollback(1)
    print("After rollback:", mgr.export())

