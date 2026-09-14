with open('overlay.txt', 'r') as f:
    content = f.read()

expanded_result_box_start = content.find('                        Surface(\n                            modifier = Modifier.fillMaxWidth(),\n                            shape = RoundedCornerShape(14.dp),')
print("Expanded Result Box Start:", expanded_result_box_start)

# let's try a different substring
idx = content.find('Surface(\n')
while idx != -1:
    print(content[idx:idx+100])
    idx = content.find('Surface(\n', idx + 1)
