import os
import re

root_dir = r"d:\Data\Android\AgriDoctorAI"

pattern = re.compile(r'project\(":(.*?)"\)')

def replacer(match):
    module_path = match.group(1)
    # Convert hyphens to dots if any, but gradle does camelCase or dot?
    # Actually gradle accessor replaces ":" with "." and "-" with "."
    # Let's see if there are any hyphens in modules we found
    accessor = "projects." + module_path.replace(":", ".")
    return accessor

def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = pattern.sub(replacer, content)

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {filepath}")

for root, _, files in os.walk(root_dir):
    for file in files:
        if file == 'build.gradle.kts':
            filepath = os.path.join(root, file)
            # Exclude build-logic if needed, but build-logic convention plugin doesn't use project() except maybe in dependencies...
            # The pattern is specific enough
            replace_in_file(filepath)
