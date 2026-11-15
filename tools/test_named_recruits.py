#!/usr/bin/env python3
from pathlib import Path
import subprocess
import tempfile
root=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as out:
    subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','17','-target','17','-d',out,
        str(root/'src/main/java/com/herberto/jetsetcraft/crew/NamedCrewRecruits.java'),str(root/'tests/NamedCrewRecruitsTest.java')],check=True)
    subprocess.run(['java','-ea','-cp',out,'NamedCrewRecruitsTest'],check=True)
