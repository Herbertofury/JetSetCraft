"""Verify an existing Wiki receipt against its pinned Git source."""
import argparse
import json
import re
import subprocess


def source_pages(source_commit, *, cwd=None):
    if not isinstance(source_commit, str) or not re.fullmatch(r"[0-9a-f]{40}", source_commit):
        raise ValueError("Use an exact Git source commit ID")
    # Never read page names from a working tree that a newer main may replace.
    listing = subprocess.check_output(
        ["git", "ls-tree", "-z", source_commit + ":wiki"], cwd=cwd
    )
    pages = []
    for entry in listing.split(b"\0"):
        if not entry:
            continue
        meta, name = entry.split(b"\t", 1)
        if meta.split(b" ")[1] == b"blob" and name.endswith(b".md"):
            pages.append(name.decode("utf-8"))
    if not pages:
        raise ValueError("Cannot record an empty Wiki")
    return sorted(pages)


def verify(receipt, *, cwd=None):
    pages = source_pages(receipt["source_commit"], cwd=cwd)
    if receipt["pages"] != pages or type(receipt["page_count"]) is not int or receipt["page_count"] != len(pages):
        raise ValueError("Receipt page list or count does not match its stated source commit")
    return len(pages)


def main():
    parser = argparse.ArgumentParser(description="Verify an existing Wiki receipt; no writes or network.")
    parser.add_argument("receipt", type=argparse.FileType("r", encoding="utf-8"))
    args = parser.parse_args()
    try:
        with args.receipt as source:
            existing = json.load(source)
        count = verify(existing)
    except (KeyError, TypeError, ValueError, subprocess.CalledProcessError) as error:
        parser.error(str(error))
    print(f'Verified {count} receipt page names against {existing["source_commit"]}.')
    print("Live Wiki bytes and deployment are not checked.")


if __name__ == "__main__":
    main()
