#!/bin/bash
#
# Copyright 2026 Aiven Oy and project contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#        SPDX-License-Identifier: Apache-2
#

git fetch origin
if [ -z $2 ]
then
  echo "Must provide source and final version"
  exit 1
fi

startTag=${1}
endTag=${2}

start=`git rev-parse ${startTag}`
end=`git rev-parse HEAD`
commits=${start}...${end}
printf "## %s\n### What's changed\n" ${endTag}
git log --format=' - %s'  ${commits}

printf "\n\n### Co-authored by\n"
git log --format=' - %an'  ${commits} | sort -u
printf "\n\n### Full Changelog\nhttps://github.com/Aiven-Open/aiven-commons/compare/${startTag}...${endTag}\n\n"
