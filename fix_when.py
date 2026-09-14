with open('overlay.txt', 'r') as f:
    content = f.read()

start = content.find('// Expanded Studio Window')
print(content[start:])
