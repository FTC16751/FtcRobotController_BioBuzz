# Test Dashboard: where it stands and what comes next

A local HTML dashboard for the unit tests plus a robot checklist, built to be shown to the student
coders. Written 2026-10-10. This file is the handoff for a later, dedicated chat: read it first, then
open `tools/test_dashboard.py`.

## Run it

```
python3 tools/test_dashboard.py --serve     # live page at http://localhost:8765, Re-run button, checklist saving
python3 tools/test_dashboard.py --run       # run the tests, then write build/test-dashboard/index.html and open it
python3 tools/test_dashboard.py             # just read the latest results
```

Needs only Python 3 (stdlib). `--serve` must run in a real terminal tab: it is killed if started from
a one-shot command. Ctrl+C stops it. The server listens on 127.0.0.1 only.

**The page is built from Python code loaded at start, so after editing `tools/test_dashboard.py`,
restart `--serve`.** Editing `tools/robot_checklist.json` needs no restart (read on every page load).

## What it does today

| Piece | How it works |
|---|---|
| Results | Reads the JUnit XML Gradle already writes to `TeamCode/build/test-results/testDebugUnitTest/`. |
| Re-run button | `POST /rerun` runs `./gradlew :TeamCode:cleanTestDebugUnitTest :TeamCode:testDebugUnitTest --continue`, then the page reloads. The clean step is needed: otherwise Gradle skips the tests as up to date and the page shows stale results. Only exists in `--serve`. |
| Hero | Pass-rate ring, counters, "Hive level" bar (one level per 25 tests), streak of consecutive all-green runs, confetti when everything passes. Respects reduced-motion. |
| Stale warning | Banner when any file under `TeamCode/src` is newer than the newest result file. File times only, so saving without changing counts as changed. |
| Test cards | Grouped by package, one dot per test, click to expand, failures open on their own with the message. Search box and All/Failing filter. |
| Tooltips | Hover a dot or row: the comment directly above `@Test` if there is one, else the method name as a sentence. Only 2 of 189 tests have real comments, so most tooltips are the name sentence. Section dividers like `// ---- feeding` are ignored. |
| Classes with no test | Every class under `common/` (not `test/`, `examples/`, `legacy/`) that no test file mentions by name. A name mention, not line coverage. |
| Robot checklist | Items come from `tools/robot_checklist.json` (12 starter items, written by guesswork, edit them). A student ticks an item, the name they entered once (kept in the browser's localStorage) and the time are saved to `build/test-dashboard/checklist.json`. Click again to undo, "Start a new checklist" clears it. Saving needs `--serve`; a plain file open shows it read-only. |
| History | `build/test-dashboard/history.json`, last 30 runs, drives the trend line and the streak. |

Everything under `build/` is git-ignored, so a clean build erases sign-offs and history.

## Files

- `tools/test_dashboard.py`: the whole thing. Python builds data, `PAGE` is one big HTML/CSS/JS string with `__DATA__`, `__HIST__`, `__FRESH__`, `__COV__`, `__CHK__`, `__GEN__` placeholders filled by `build()`.
- `tools/robot_checklist.json`: `[{"id","group","text"}]`. Ids are stable keys for saved sign-offs; renaming an id orphans its old sign-off.
- `build/test-dashboard/`: generated, git-ignored.

## Decisions and why

- **Static page plus a tiny local server**, not a framework. A plain HTML file cannot run Gradle or save sign-offs, so only those two things need `--serve`.
- **Python stdlib only** so nobody installs anything on a team laptop.
- **Checklist saved server-side**, not just in localStorage, so every browser and student on the same laptop sees the same sign-offs.
- **Ask for a name once** per browser (it was asking on every tick and was tedious).
- Considered and left out: a leaderboard of who added the most tests (rewards many tiny tests); a pre-push git hook (opt-in later); watch mode that re-runs on save.

## Known limits and gotchas

- Coverage is a name match, not JaCoCo. A class mentioned in a comment counts as covered.
- The checklist is a record of what a student says they did. The robot does not report back to the page.
- The browser pane inside the Claude desktop app shows `file://` pages as static snapshots (scripts do not run). Test over `http://localhost`, not by opening the file there.
- A JS `const top` collides with `window.top` and kills the whole script. Avoid top-level names that shadow `window` properties; `node --check` will not catch it, only loading the page will.
- Only unit tests are shown. Nothing here runs on the robot or the virtual_robot simulator (decided against; see below).

## Next ideas, in suggested order

1. **TeleOp per checklist item.** Optional `"opmode": "MechanismBenchTest"` on an item shows "Run this on the Driver Station: ...". Find OpMode names by scanning `@TeleOp(name=...)` / `@Autonomous(name=...)` in `TeamCode/src/main`, and warn on the page when an item names an OpMode that does not exist. Existing candidates in `common/test/`: `MechanismBenchTest`, `StandardBotEncoderMoveCheck`, `PrismLedTestOpMode`, `GBPinpointDriveToPoint`. Items with no matching OpMode become a "needs a test OpMode" list, another student job.
2. **Per-robot checklists.** `tools/checklists/<robot>.json` and a picker. Robots in the repo: `teams/p3`, `teams/testteam2027`, `teams/starterbot2027`, `teams/demobots`. Save sign-offs per robot.
3. **Richer answers:** a number ("flywheel rpm at ready") or a note, not just a tick.
4. **Fold in `doc/ROBOT_TEST_PLAN.md`.** That 295-line markdown file is already a manual robot test checklist with commit references and expected values. Decide whether the dashboard should read it (or be generated from it) instead of keeping a second list in JSON.
5. Optional: pre-push hook, watch mode, per-student view.

## virtual_robot (Beta8397) was evaluated and parked

It simulates drive, Pedro 3 and sensors only. No cameras, Limelight or AprilTags; no mechanism physics (a launcher is just numbers); no game elements. Its bundled Pedro jar is a post-3.0.1 snapshot (our Follower.class differs in size from 3.0.1) with the same package layout, so our autos would probably compile but this was never tried. For vision and logic, JUnit plus replayed logs is the better fit. Revisit only if you want Pedro autos tested away from the robot.
