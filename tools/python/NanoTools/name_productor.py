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
