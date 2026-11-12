"""Synthetic fixture tests only: never substitutes fixture audio for a selected song."""
import json
from pathlib import Path
import tempfile
import unittest
import wave
import import_music


class MusicImportTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.source = self.root / 'synthetic-silence.wav'
        with wave.open(str(self.source), 'wb') as wav:
            wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(44100)
            wav.writeframes(b'\0\0' * 88200)
        self.metadata = {'title': 'Fixture', 'artist': 'Fixture artist',
                         'sound_event': 'jetsetcraft:test', 'audio_resource': 'jetsetcraft:sounds/test.ogg',
                         'contexts': ['home']}

    def build(self):
        (self.root / 'fixture.json').write_text(json.dumps(self.metadata))
        return import_music.build('fixture', self.source, self.root / 'pack', self.root)

    def test_legacy_metadata_and_no_overwrite(self):
        receipt = self.build()
        self.assertEqual(receipt['album'], '')
        self.assertEqual(receipt['featured_artists'], [])
        original = (self.root / 'pack/CREDITS.txt').read_bytes()
        with self.assertRaises(ValueError): self.build()
        self.assertEqual((self.root / 'pack/CREDITS.txt').read_bytes(), original)

    def test_full_credit_parity(self):
        self.metadata.update(album='Album\nName', featured_artists=['Guest\tName'])
        receipt = self.build()
        imported = json.loads((self.root / 'pack/assets/jetsetcraft/jetsetcraft_music/fixture.json').read_text())
        credits = (self.root / 'pack/CREDITS.txt').read_text()
        for key in ('title', 'artist', 'album', 'featured_artists'):
            self.assertEqual(receipt[key], imported[key])
        self.assertIn('Release: AlbumName', credits)
        self.assertIn('Featured artists: GuestName', credits)
        self.assertIn('/wiki/Music-Library', credits)
        self.assertEqual(imported['recording_status'], 'owner_audio_imported')

    def test_malformed_optional_credit_rejected_before_output(self):
        for extra in ({'album': 3}, {'album': ''}, {'featured_artists': 'Guest'},
                      {'featured_artists': [None]}, {'featured_artists': ['a'] * 17}):
            with self.subTest(extra=extra):
                self.metadata.pop('album', None); self.metadata.pop('featured_artists', None)
                self.metadata.update(extra)
                with self.assertRaises(ValueError): self.build()
                self.assertFalse((self.root / 'pack').exists())

    def test_label_bounds_and_formatting(self):
        self.assertEqual(import_music.credit_label(' A\n§B\x7f '), 'AB')
        for value in (None, '', '\n§', 'a' * 161, '😀' * 81):
            with self.assertRaises(ValueError): import_music.credit_label(value)


if __name__ == '__main__': unittest.main()
