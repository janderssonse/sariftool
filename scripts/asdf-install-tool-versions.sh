#!/usr/bin/env bash

# A script to easily set up basing dev tooling with asdf using versions in .tool-versions

is_command_available() {
  local COMMAND="${1}"
  local INFO="${2}"

  if ! [ -x "$(command -v "${COMMAND}")" ]; then
    echo "Error: ${COMMAND} is not availble in path/installed." >&2
    echo "See ${INFO} for more info about the command." >&2
    exit 1
  fi
}
is_command_available 'asdf' 'https://asdf-vm.com/'

asdf install