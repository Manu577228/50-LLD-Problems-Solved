# Design a Bulk Data Importer that loads CSV/JSON files, validates each record,
# processes them concurrently, stores valid records in an in-memory datastore,
# and retries failed records once. The system must generate a summary report of
# total, success, and failed records. The entire solution should run as a single,
# self-contained file without any external database.

import json
import csv
from concurrent.futures import ThreadPoolExecutor
from threading import Lock

DATA_STORE = []
DB_LOCK = Lock()


class DataStore:
    @staticmethod
    def save(record):
        with DB_LOCK:
            DATA_STORE.append(record)

    @staticmethod
    def list_all():
        return DATA_STORE


class BaseImporter:
    def __init__(self, file_path):
        self.file_path = file_path
        self.failed_records = []

    def load(self):
        raise NotImplementedError

    def validate(self, record):
        return True, ""

    def process(self, record):
        DataStore.save(record)

    def run(self):
        records = self.load()

        with ThreadPoolExecutor(max_workers=4) as pool:
            results = list(pool.map(self._process_record, records))

        retry_results = []
        for r in self.failed_records:
            ok, msg = self.validate(r)
            if ok:
                self.process(r)
            else:
                retry_results.append((r, msg))

        return {
            "total": len(records),
            "success": len(records) - len(retry_results),
            "failed": len(retry_results),
            "data": DataStore.list_all()
        }

    def _process_record(self, record):
        ok, msg = self.validate(record)
        if ok:
            self.process(record)
        else:
            self.failed_records.append(record)


class CSVImporter(BaseImporter):
    def load(self):
        records = []

        with open(self.file_path, "r") as f:
            reader = csv.DictReader(f)
            for row in reader:
                records.append(row)

        return records

    def validate(self, record):
        if not record.get("id"):
            return False, "Missing Id"
        return True, ""


class JSONImporter(BaseImporter):
    def load(self):
        with open(self.file_path, "r") as f:
            return json.load(f)

    def validate(self, record):
        if "id" not in record:
            return False, "Missing Id"
        return True, ""


class ImporterFactory:
    @staticmethod
    def get_importer(file_path):
        if file_path.endswith(".csv"):
            return CSVImporter(file_path)
        if file_path.endswith(".json"):
            return JSONImporter(file_path)
        raise Exception("Unsupported file format")


if __name__ == "__main__":

    # Create sample CSV file for demo
    with open("sample.csv", "w") as f:
        f.write("id,name\n1,Alice\n,Invalid\n2,Bob\n")

    # Get correct importer from factory
    importer = ImporterFactory.get_importer("sample.csv")

    # Run importer (load → validate → process → retry → summary)
    output = importer.run()

    # Print final structured summary
    print("=== Import Summary ===")
    print(json.dumps(output, indent=2))        # Pretty-print final dictionary
