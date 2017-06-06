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
