from pathlib import Path
import subprocess
from tempfile import TemporaryDirectory
import unittest

from wiki_receipt import source_pages, verify


class ReceiptTests(unittest.TestCase):
    def test_receipt_is_bound_to_publish_source_even_when_main_advances(self):
        with TemporaryDirectory() as temp:
            root = Path(temp)

            def git(*args):
                return subprocess.check_output(["git", *args], cwd=root).decode().strip()

            git("init", "-q", "--initial-branch=main")
            git("config", "user.name", "Wiki Fixture")
            git("config", "user.email", "fixture@example.invalid")
            wiki = root / "wiki"
            wiki.mkdir()
            (wiki / "Home.md").write_text("old home\n")
            (wiki / "Removed Later.md").write_text("published page\n")
            (wiki / "diagram.svg").write_text("not a page\n")
            git("add", "wiki")
            git("commit", "-qm", "validated and published source")
            source = git("rev-parse", "HEAD")

            (wiki / "Removed Later.md").unlink()
            (wiki / "NewMain.md").write_text("not published in first run\n")
            git("add", "-A")
            git("commit", "-qm", "main advances while wiki is publishing")
            main = git("rev-parse", "HEAD")
            self.assertNotEqual(source, main)
            older = {"source_commit": source, "pages": ["Home.md", "Removed Later.md"], "page_count": 2}
            self.assertEqual(verify(older, cwd=root), 2)
            self.assertEqual(source_pages(main, cwd=root), ["Home.md", "NewMain.md"])
            mixed = dict(older, pages=["Home.md", "NewMain.md"])
            with self.assertRaisesRegex(ValueError, "does not match"):
                verify(mixed, cwd=root)
            with self.assertRaisesRegex(ValueError, "does not match"):
                verify(dict(older, page_count=1), cwd=root)

    def test_reject_branch_names_instead_of_source_commit(self):
        with self.assertRaisesRegex(ValueError, "exact Git source commit ID"):
            source_pages("main")


if __name__ == "__main__":
    unittest.main(verbosity=2)
