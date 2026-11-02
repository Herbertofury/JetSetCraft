#!/usr/bin/env python3
"""Download the exact optional MCDivers Forge provider, unmodified and outside the repository's source artifacts."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import os
import shutil
import tempfile
import urllib.request
import zipfile

NAME='mcdivers-forge-1.20.1-1.0.3.jar'
URL='https://edge.forgecdn.net/files/9307/472/'+NAME
PAGE='https://www.curseforge.com/minecraft/mc-mods/mcdiver/files/9307472'
BYTES=2160981
SHA256='49c4ab153c50dab8766fca15ab7affcc14b1c9a8e7e52d58a9683c06ac4ceac0'

def verify(path:Path)->None:
    if not path.is_file() or path.stat().st_size!=BYTES:
        raise ValueError('MCDivers release is absent or has the wrong byte count: '+str(path))
    with path.open('rb') as source:
        if hashlib.file_digest(source,'sha256').hexdigest()!=SHA256:raise ValueError('MCDivers SHA-256 does not match the pinned official release')
    with zipfile.ZipFile(path) as jar:
        metadata=jar.read('META-INF/mods.toml').decode()
        if 'modId="mcdivers"' not in metadata or 'version="1.0.3"' not in metadata:raise ValueError('Unexpected provider metadata')
        if 'com/shiroaki193/mcdivers/entity/StratagemBeaconEntity.class' not in jar.namelist():raise ValueError('Missing native beacon implementation')

def main()->None:
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--destination',type=Path,default=Path('run/provider-downloads'),help='Directory to receive the intact JAR; use an installed instance mods/ folder to install it. The default does not duplicate Gradle runtime dependencies.')
    p.add_argument('--source',type=Path,help='Reuse an existing official JAR after exact verification')
    p.add_argument('--verify-only',action='store_true')
    args=p.parse_args();target=args.destination.resolve()/NAME
    if target.exists() or args.verify_only:verify(target)
    else:
        args.destination.mkdir(parents=True,exist_ok=True)
        temporary=None
        try:
            with tempfile.NamedTemporaryFile(dir=args.destination,prefix='.mcdivers-',suffix='.part',delete=False) as f:
                temporary=Path(f.name)
                if args.source:
                    verify(args.source)
                    with args.source.open('rb') as src:shutil.copyfileobj(src,f)
                else:
                    request=urllib.request.Request(URL,headers={'User-Agent':'JetSetCraft-Provider-Verification/1.0'})
                    with urllib.request.urlopen(request,timeout=45) as src:
                        received=0
                        while chunk:=src.read(1<<20):
                            received+=len(chunk)
                            if received>BYTES:raise ValueError('Response exceeds the pinned artifact size')
                            f.write(chunk)
            verify(temporary);os.replace(temporary,target);temporary=None
        finally:
            if temporary is not None:temporary.unlink(missing_ok=True)
    print(f'JETSETCRAFT_HELLDIVERS_PROVIDER_READY file=9307472 bytes={BYTES} sha256={SHA256}')
    print('Source: '+PAGE)
    print('Prepared: '+str(target))

if __name__=='__main__':
    try:main()
    except (OSError,ValueError,zipfile.BadZipFile) as error:raise SystemExit('MCDivers preparation failed: '+str(error))
