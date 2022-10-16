#!/usr/bin/env bash

# Run a code quality check

declare -A EXITCODES

# Colour
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

#Terminal chars
CHECKMARK="\xE2\x9C\x94"
MISSING="\xE2\x9D\x8C"

is_command_available() {
  local COMMAND="${1}"
  local INFO="${2}"

  if ! [ -x "$(command -v "${COMMAND}")" ]; then
    printf '%b Error:%b %s is not availble in path/installed.\n' "${RED}" "${NC}" "${COMMAND}" >&2
    printf 'See %s for more info about the command.\n' "${INFO}" >&2
    exit 1
  fi
}

check() {
  local HEADER="$1"
  local COMMAND="$2"
  printf '%b\n************ %s ***********%b\n\n' "${YELLOW}" "$HEADER" "${NC}"
  eval "$COMMAND"
  return "$?"
}

store_exit_code() {
  declare -i STATUS="$1"
  local KEY="$2"
  local MESSAGE="$3"

  if [[ "${STATUS}" -ne 0 ]]; then
    EXITCODES["${KEY}"]="${MESSAGE}"
  fi
}

lint() {
  check 'LINTER HEALTH (MEGALINTER)' \
    "podman run --volume $(pwd):/repo -e MEGALINTER_CONFIG='configs/mega-linter.yml' -e DEFAULT_WORKSPACE='/repo' -e LOG_LEVEL=INFO oxsecurity/megalinter:v6.11.1"
  store_exit_code "$?" "Lint" "${MISSING} ${RED}Lint check failed, see logs and fix problems.${NC}\n"
  printf '\n\n'
}

license() {
  check 'LICENSE HEALTH (REUSE)' \
    "podman run --volume $(pwd):/data fsfe/reuse lint"
  store_exit_code "$?" "License" "${MISSING} ${RED}License check failed, see logs and fix problems.${NC}\n"
  printf '\n\n'
}

commits() {
  check 'COMMIT HEALTH (commitlint)' "commitlint --config configs/commitlint.config.js --from 'HEAD~1' --to 'HEAD' --verbose"
  store_exit_code "$?" "Commit" "${MISSING} ${RED}Commit check failed, see logs and fix problems.${NC}\n"
  printf '\n\n'
}

repository() {
  check 'REPOSITORY HEALTH (repolinter)' \
    "repolinter -r configs/repolinter.config.yml"
  store_exit_code "$?" "Repolint" "${MISSING} ${RED}Repository health check failed, see logs and fix problems/adjust rules.${NC}\n"
  printf '\n\n'
}

check_exit_codes() {
  printf '%b********* QUALITY RUN SUMMARY ******%b\n\n' "${YELLOW}" "${NC}"

  if [[ "${#EXITCODES[@]}" -gt 0 ]]; then
    for key in "${!EXITCODES[@]}"; do
      printf '%b' "${EXITCODES[$key]}"
    done
    printf "\n"
  else
    printf '%b%b All checks passed, congratulations!!\n\n%b' "${GREEN}" "${CHECKMARK}" "${NC}"
  fi

}

is_command_available 'podman' 'https://podman.io/'
is_command_available 'commitlint' 'https://commitlint.js.org/#/'
is_command_available 'repolinter' 'https://github.com/todogroup/repolinter'

lint
license
commits
repository

check_exit_codes
