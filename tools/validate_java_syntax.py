#!/usr/bin/env python3
"""Parse every Java source with the JDK compiler frontend without needing Forge/Minecraft classpath jars."""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCES = sorted((ROOT / "src/main/java").rglob("*.java"))
HELPER = r'''
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import com.sun.source.util.JavacTask;

public final class ParseJavaSources {
    public static void main(String[] args) throws Exception {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            List<Path> paths = new ArrayList<>();
            for (String arg : args) paths.add(Path.of(arg));
            Iterable<? extends JavaFileObject> units = files.getJavaFileObjectsFromPaths(paths);
            JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                    List.of("-proc:none", "--release", "17"), null, units);
            task.parse();
        }
        boolean failed = false;
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                failed = true;
                String source = diagnostic.getSource() == null ? "<unknown>" : diagnostic.getSource().getName();
                System.err.printf(Locale.ROOT, "%s:%d:%d: %s%n", source, diagnostic.getLineNumber(),
                        diagnostic.getColumnNumber(), diagnostic.getMessage(Locale.ROOT));
            }
        }
        if (failed) System.exit(1);
        System.out.println("Java syntax parse OK: " + args.length + " source files");
    }
}
'''


def main() -> int:
    if not SOURCES:
        print("No Java sources found", file=sys.stderr)
        return 1
    javac = shutil.which("javac")
    java = shutil.which("java")
    if not javac or not java:
        print("JDK java/javac are required for Java syntax validation", file=sys.stderr)
        return 1
    with tempfile.TemporaryDirectory(prefix="jetsetcraft-java-parse-") as temp:
        work = Path(temp)
        helper = work / "ParseJavaSources.java"
        helper.write_text(HELPER, encoding="utf-8")
        subprocess.run([javac, "--release", "17", "-d", str(work), str(helper)], check=True)
        subprocess.run([java, "-cp", str(work), "ParseJavaSources", *(str(path) for path in SOURCES)], check=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
