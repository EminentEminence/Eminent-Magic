#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CORE_WORKSPACE="$ROOT_DIR/Eminent Magic: Core/Workspace"
CORE_BUILD_DIR="$CORE_WORKSPACE/build"
WORLD_WORKSPACE="$ROOT_DIR/Eminent Magic: World of Magic/Workspace"
PLUGIN_PROJECT="$ROOT_DIR/Eminent Magic: Development Plugin"

MCREATOR_HOME="${MCREATOR_HOME:-$HOME/Coding/Software/Mcreator/MCreator20253}"
JAVA_HOME="${JAVA_HOME:-$MCREATOR_HOME/jdk}"
if [[ -z "${GRADLE:-}" ]]; then
	if command -v gradle >/dev/null 2>&1; then
		GRADLE="$(command -v gradle)"
	else
		for candidate in \
			"$HOME"/.mcreator/gradle/wrapper/dists/gradle-8.14.2-bin/*/gradle-8.14.2/bin/gradle \
			"$HOME"/.gradle/wrapper/dists/gradle-8.14.2-bin/*/gradle-8.14.2/bin/gradle
		do
			if [[ -x "$candidate" ]]; then
				GRADLE="$candidate"
				break
			fi
		done
	fi
fi
GRADLE="${GRADLE:-gradle}"

if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
	printf 'Java 21 was not found at %s. Set JAVA_HOME or MCREATOR_HOME.\n' "$JAVA_HOME" >&2
	exit 1
fi

if ! command -v "$GRADLE" >/dev/null 2>&1; then
	printf 'Gradle command not found: %s. Install Gradle or set GRADLE to its executable.\n' "$GRADLE" >&2
	exit 1
fi

if [[ ! -d "$MCREATOR_HOME/lib" ]]; then
	printf 'MCreator installation was not found at %s. Set MCREATOR_HOME.\n' "$MCREATOR_HOME" >&2
	exit 1
fi

export MCREATOR_HOME JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

printf '\n==> Building Core mod\n'
"$GRADLE" --no-daemon --console=plain -p "$CORE_WORKSPACE" build

mapfile -d '' CORE_JARS < <(find "$CORE_BUILD_DIR/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' -print0 2>/dev/null)
if [[ "${#CORE_JARS[@]}" -ne 1 ]]; then
	printf 'Expected exactly one Core mod JAR in %s/libs; found %s.\n' \
		"$CORE_BUILD_DIR" "${#CORE_JARS[@]}" >&2
	exit 1
fi

WORLD_MODS_DIR="$WORLD_WORKSPACE/run/mods"
WORLD_DEPENDENCY_DIR="$ROOT_DIR/.build/world-dependencies"
mkdir -p "$WORLD_MODS_DIR"
mkdir -p "$WORLD_DEPENDENCY_DIR"
printf '\n==> Copying Core mod to World runtime: %s\n' "$WORLD_MODS_DIR"
find "$WORLD_MODS_DIR" -maxdepth 1 -type f -name 'eminent_magic_core*.jar' -delete
install -m 0644 "${CORE_JARS[0]}" "$WORLD_MODS_DIR/eminent_magic_core-1.0.0-neoforge-1.21.8.jar"
install -m 0644 "${CORE_JARS[0]}" "$WORLD_DEPENDENCY_DIR/eminent_magic_core.jar"

TESTING_WORKSPACE="$PLUGIN_PROJECT/TestingWorkspace"
TESTING_MODS_DIR="$TESTING_WORKSPACE/run/mods"
TESTING_DEPENDENCY_DIR="$ROOT_DIR/.build/plugin-testing-dependencies"
MCREATOR_TESTING_WORKSPACE="${MCREATOR_TESTING_WORKSPACE:-$HOME/MCreatorWorkspaces/plugin_testing}"
mkdir -p "$TESTING_MODS_DIR"
mkdir -p "$TESTING_DEPENDENCY_DIR"
printf '\n==> Copying Core mod to plugin TestingWorkspace runtime: %s\n' "$TESTING_MODS_DIR"
find "$TESTING_MODS_DIR" -maxdepth 1 -type f -name 'eminent_magic_core*.jar' -delete
install -m 0644 "${CORE_JARS[0]}" "$TESTING_MODS_DIR/eminent_magic_core-1.0.0-neoforge-1.21.8.jar"
install -m 0644 "${CORE_JARS[0]}" "$TESTING_DEPENDENCY_DIR/eminent_magic_core.jar"

if [[ ! -d "$MCREATOR_TESTING_WORKSPACE" ]]; then
	printf 'MCreator plugin_testing workspace was not found at %s. Set MCREATOR_TESTING_WORKSPACE.\n' \
		"$MCREATOR_TESTING_WORKSPACE" >&2
	exit 1
fi

MCREATOR_TESTING_MODS_DIR="$MCREATOR_TESTING_WORKSPACE/run/mods"
mkdir -p "$MCREATOR_TESTING_MODS_DIR"
printf '\n==> Copying Core mod to MCreator plugin_testing runtime: %s\n' "$MCREATOR_TESTING_MODS_DIR"
find "$MCREATOR_TESTING_MODS_DIR" -maxdepth 1 -type f -name 'eminent_magic_core*.jar' -delete
install -m 0644 "${CORE_JARS[0]}" "$MCREATOR_TESTING_MODS_DIR/eminent_magic_core-1.0.0-neoforge-1.21.8.jar"

printf '\n==> Building and installing development plugin\n'
"$GRADLE" --no-daemon --console=plain -p "$PLUGIN_PROJECT" installPlugin \
	-Pmcreator_path="$MCREATOR_HOME"

printf '\n==> Building World of Magic\n'
"$GRADLE" --no-daemon --console=plain -p "$WORLD_WORKSPACE" build

printf '\nBuild pipeline completed successfully.\n'