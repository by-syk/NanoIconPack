# Copyright 2017 By_syk
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os


def generate_whats_new(dir_path):
    count = 0
    for parent, dir_names, file_names in os.walk(dir_path):
        for file_name in file_names:
            if '.png' not in file_name:
                continue
            count += 1
            print('<item>%s</item>' % file_name.replace('.png', ''))
    print()
    print('%d in total.' % count)


if __name__ == '__main__':
    path = input("path: ")
    if path.startswith('"') and path.endswith('"'):
        path = path[1:len(path) - 1]
    generate_whats_new(path)
