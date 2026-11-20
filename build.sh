#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
VERSION_FILE="$ROOT_DIR/VERSION"
CORE_WORKSPACE="$ROOT_DIR/Eminent Magic: Core/Workspace"
CORE_BUILD_DIR="$ROOT_DIR/.build/core-workspace"
WORLD_WORKSPACE="$ROOT_DIR/Eminent Magic: World of Magic/workspace"
PLUGIN_PROJECT="$ROOT_DIR/Eminent Magic: Development Plugin"

if [[ ! -f "$VERSION_FILE" ]]; then
	printf 'Canonical version file was not found: %s\n' "$VERSION_FILE" >&2
	exit 1
fi
VERSION="$(< "$VERSION_FILE")"
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]]; then
	printf 'Invalid project version in %s: %s\n' "$VERSION_FILE" "$VERSION" >&2
	exit 1
fi

MCREATOR_HOME="${MCREATOR_HOME:-$HOME/Coding/Software/Mcreator/MCreator20253}"
MCREATOR_PLUGIN_DIR="${MCREATOR_PLUGIN_DIR:-$HOME/.mcreator/plugins}"
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

if [[ ! -f "$CORE_WORKSPACE/eminent_magic.mcreator" ]]; then
	printf 'Core workspace was not found at %s.\n' "$CORE_WORKSPACE" >&2
	exit 1
fi

if [[ ! -f "$WORLD_WORKSPACE/eminent_magic_world.mcreator" ]]; then
	printf 'World of Magic workspace was not found at %s.\n' "$WORLD_WORKSPACE" >&2
	exit 1
fi

export MCREATOR_HOME JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

printf '\n==> Building Core mod\n'
"$GRADLE" --no-daemon --console=plain -p "$CORE_WORKSPACE" build -PprojectVersion="$VERSION"

mapfile -d '' CORE_JARS < <(find "$CORE_BUILD_DIR/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' -print0 2>/dev/null)
if [[ "${#CORE_JARS[@]}" -ne 1 ]]; then
	printf 'Expected exactly one Core mod JAR in %s/libs; found %s.\n' \
		"$CORE_BUILD_DIR" "${#CORE_JARS[@]}" >&2
	exit 1
fi

WORLD_MODS_DIR="$WORLD_WORKSPACE/run/mods"
mkdir -p "$WORLD_MODS_DIR"
printf '\n==> Copying Core mod to World runtime: %s\n' "$WORLD_MODS_DIR"
find "$WORLD_MODS_DIR" -maxdepth 1 -type f -name 'eminent_magic_core*.jar' -delete
install -m 0644 "${CORE_JARS[0]}" "$WORLD_MODS_DIR/eminent_magic_core-${VERSION}-neoforge-1.21.8.jar"

printf '\n==> Building and installing development plugin %s\n' "$VERSION"
"$GRADLE" --no-daemon --console=plain -p "$PLUGIN_PROJECT" build \
	-PmcreatorHome="$MCREATOR_HOME" -PprojectVersion="$VERSION"
PLUGIN_JAR="$ROOT_DIR/.build/development-plugin/libs/eminent-magic-development-plugin-${VERSION}.jar"
if [[ ! -f "$PLUGIN_JAR" ]]; then
	printf 'Development plugin JAR was not produced: %s\n' "$PLUGIN_JAR" >&2
	exit 1
fi
mkdir -p "$MCREATOR_PLUGIN_DIR"
install -m 0644 "$PLUGIN_JAR" "$MCREATOR_PLUGIN_DIR/eminent-magic-development-plugin.jar"
printf 'Installed development plugin: %s\n' "$MCREATOR_PLUGIN_DIR/eminent-magic-development-plugin.jar"

printf '\nCore build, World runtime copy, and plugin installation completed successfully.\n'