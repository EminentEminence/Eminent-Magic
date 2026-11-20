import json
import sys
from pathlib import Path


version = sys.argv[1]

for filename in sys.argv[2:]:
	path = Path(filename)
	with path.open(encoding="utf-8") as file:
		project = json.load(file)

	project["workspaceSettings"]["version"] = version
	project["version"] = version
	with path.open("w", encoding="utf-8") as file:
		json.dump(project, file, ensure_ascii=False, indent=2)
		file.write("\n")