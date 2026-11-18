#!/usr/bin/env python3
"""Writes Markdown patch notes for the commits between two refs.

Most changes land on main as direct commits rather than pull requests, so GitHub's own
generated notes (which only list merged pull requests) come out empty. This builds the notes from
the commit history instead:

- Commits are grouped by their first word: "Add ..." under New, "Fix ..." under Fixes, and
  everything else under Improvements.
- Commits that only touch tests, CI, or documentation are left out, as they don't change the app.
- A change and its revert in the same release cancel out.
- A merged pull request is listed once, under its title, instead of its individual commits.
- Any commit whose message contains "[skip notes]" is left out.

Usage: release_notes.py --to <ref> [--from <ref>] [--repo <owner/name>] [--tag <new tag>]
"""
import argparse
import re
import subprocess
import sys

NEW_WORDS = {"add", "adds", "added", "introduce", "introduces", "support", "supports"}
FIX_WORDS = {"fix", "fixes", "fixed", "resolve", "resolves", "resolved", "correct", "corrects"}

# Paths whose changes never reach people using the app.
NON_USER_FACING = (
    re.compile(r"^app/src/(test|androidTest)/"),
    re.compile(r"^baselineprofile/src/"),
    re.compile(r"^\.github/"),
    re.compile(r"^resources/"),
    re.compile(r"\.md$"),
    re.compile(r"^LICENSE$"),
    re.compile(r"^gradle/wrapper/"),
)

SKIP_MARKER = "[skip notes]"
PR_MERGE = re.compile(r"^Merge pull request #(\d+) from ")


def git(*args: str) -> str:
    return subprocess.run(["git", *args], capture_output=True, text=True, check=True).stdout


def commits(range_spec: str):
    """Yields (sha, subject, body) for first-parent commits, oldest first."""
    out = git("log", "--first-parent", "--reverse", "--format=%H%x1f%s%x1f%b%x1e", range_spec)
    for record in out.split("\x1e"):
        record = record.strip("\n")
        if not record:
            continue
        sha, subject, body = (record.split("\x1f") + ["", ""])[:3]
        yield sha, subject.strip(), body.strip()


def changed_files(sha: str, is_merge: bool) -> list[str]:
    if is_merge:
        # Against the first parent, so the whole pull request counts.
        out = git("diff", "--name-only", f"{sha}^1", sha)
    else:
        # --root also lists the files of the repository's first commit, which has no parent.
        out = git("diff-tree", "--root", "--no-commit-id", "--name-only", "-r", sha)
    return [f for f in out.splitlines() if f]


def user_facing(files: list[str]) -> bool:
    return any(not any(p.search(f) for p in NON_USER_FACING) for f in files)


def tidy(subject: str) -> str:
    subject = subject.strip().rstrip(".")
    return subject[:1].upper() + subject[1:]


def category(subject: str) -> str:
    first = re.split(r"\W+", subject.strip().lower(), maxsplit=1)[0]
    if first in NEW_WORDS:
        return "New"
    if first in FIX_WORDS:
        return "Fixes"
    return "Improvements"


def build_entries(range_spec: str) -> list[tuple[str, str]]:
    entries = []  # (subject, suffix)
    for sha, subject, body in commits(range_spec):
        if SKIP_MARKER in subject or SKIP_MARKER in body:
            continue
        is_merge = len(git("rev-list", "--parents", "-n", "1", sha).split()) > 2
        pr = PR_MERGE.match(subject)
        if pr:
            title = body.splitlines()[0].strip() if body else f"Pull request #{pr.group(1)}"
            subject, suffix = title, f" (#{pr.group(1)})"
        elif is_merge:
            continue  # Other merges (e.g. syncing a branch) carry no change of their own.
        else:
            suffix = ""
        if not user_facing(changed_files(sha, is_merge)):
            continue
        entries.append((subject, suffix))

    # A change and its revert within the same release cancel out. A revert of something from an
    # earlier release stays, since it changes what users had.
    subjects = {s for s, _ in entries}
    cancelled = set()
    for subject, _ in entries:
        reverts = re.match(r'^Revert "(.+)"$', subject)
        if reverts and reverts.group(1) in subjects:
            cancelled |= {subject, reverts.group(1)}
    return [(s, x) for s, x in entries if s not in cancelled]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--from", dest="since", default="", help="previous release tag")
    parser.add_argument("--to", dest="until", required=True, help="ref being released")
    parser.add_argument("--repo", default="", help="owner/name, for the full changelog link")
    parser.add_argument("--tag", default="", help="tag of the new release, for the link")
    args = parser.parse_args()

    range_spec = f"{args.since}..{args.until}" if args.since else args.until
    groups: dict[str, list[str]] = {"New": [], "Improvements": [], "Fixes": []}
    for subject, suffix in build_entries(range_spec):
        groups[category(subject)].append(f"- {tidy(subject)}{suffix}")

    lines = ["## What's changed", ""]
    if not any(groups.values()):
        lines += ["Maintenance release with no changes to the app.", ""]
    for heading, items in groups.items():
        if items:
            lines += [f"### {heading}", "", *items, ""]
    if args.repo and args.since and args.tag:
        lines.append(
            f"**Full changelog**: https://github.com/{args.repo}/compare/{args.since}...{args.tag}"
        )
    sys.stdout.write("\n".join(lines).rstrip("\n") + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
