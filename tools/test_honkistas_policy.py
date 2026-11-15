#!/usr/bin/env python3
"""Compile and execute the real dependency-free policy, without downloading Forge."""
from pathlib import Path
import json
import subprocess
import tempfile
root = Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as output:
    subprocess.run(['java', '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-source', '17', '-target', '17', '-d', output,
                    str(root/'src/main/java/com/herberto/jetsetcraft/gang/HonkistasVariantPolicy.java'),
                    str(root/'tests/HonkistasVariantPolicyTest.java')], check=True)
    subprocess.run(['java', '-cp', output, 'HonkistasVariantPolicyTest'], check=True)
for name in ('honkistas', 'grinions', 'amygdala'):
    data=json.loads((root/f'src/main/resources/data/jetsetcraft/jetsetcraft_gangs/{name}.json').read_text())
    assert data['gang_id'] == 'jetsetcraft:'+name
    assert not data['entities'] and not data['boombox_eligible']
    assert data['music'] == 'jetsetcraft:music/gangs/generic'
print('3 safe registry definitions passed; no entity remapping or unlicensed audio')
