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


def run(suffix, start, num):
    for i in range(start, start + num):
        if i == 0:
            print('<item>%s</item>' % suffix)
        else:
            print('<item>%s_%d</item>' % (suffix, i))
    print()
    for i in range(start, start + num):
        if i == 0:
            print('<item drawable="%s" />' % suffix)
        else:
            print('<item drawable="%s_%d" />' % (suffix, i))


run(input("suffix: "), int(input("start: ")), int(input("num: ")))
