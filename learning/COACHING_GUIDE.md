# FTC Learning System: Coaching Guide

A guide for coaches, mentors, and team leads running the FTC Learning System.

---

## Quick Start for Coaches

1. **Understand the progression**: the 16 lessons in `README.md` build in order, in four phases
2. **Prepare hardware** using HARDWARE_SETUP_GUIDE.md
3. **Have students read the README** before coding
4. **Give students the Template code** to complete
5. **Circulate and help with TODOs**
6. **Compare solutions** together as a team
7. **Assign exercises** for extra challenge

---

## Lesson Structure Overview

| Phase | Lessons | Hardware | Time |
|-------|---------|----------|------|
| 1 Java fundamentals | L01–L05 | none (L03: gamepad + board) | 6–8 hrs |
| 2 Hardware basics | L06–L11 | programming board | 8–10 hrs |
| 3 Programming patterns | L12–L14 | programming board | 6–8 hrs |
| 4 Advanced FTC | L20, L24 | a drivable robot | 6–8 hrs |

**Total Time**: 26–38 hours per student. The full lesson table, with book chapters, is in
`README.md`; the board wiring is in `HARDWARE_SETUP_GUIDE.md`.

---

## Session Plan Template

### Pre-Session (15 minutes before)
- [ ] Hardware powered on and responsive
- [ ] Configuration active and verified
- [ ] Sample solution code deployed and tested
- [ ] Telemetry visible on Driver Station
- [ ] All gamepads charged (if needed)
- [ ] Print copies of lesson README

### Session Start (5-10 minutes)
- [ ] Have students read the lesson README (or read together)
- [ ] Discuss key concepts as a group
- [ ] Show a demo of the working solution
- [ ] Explain what the TODOs ask them to do

### Student Work Time (30-60 minutes)
- [ ] Students work on Template code
- [ ] Help students interpret TODOs
- [ ] Encourage them to try, fail, debug
- [ ] Provide hints, not solutions
- [ ] Pair students who need help

### Check Understanding (10-15 minutes)
- [ ] Have a few students show their code
- [ ] Discuss different approaches
- [ ] Show the Complete_Solution
- [ ] Highlight what they did well

### Challenges (10-20 minutes if time)
- [ ] Assign an Exercise from Exercises.txt
- [ ] Encourage creative modifications
- [ ] Have students present their enhancements

### Wrap-Up (5 minutes)
- [ ] Ask: "What was hard about this?"
- [ ] Remind about next lesson prerequisites
- [ ] Have them commit their code (it lives in their `students/<name>` package)

---

## Coaching Strategies by Lesson

One key point, one common struggle, and one question to ask, per lesson.

| Lesson | Emphasize | Watch for | Ask |
|--------|-----------|-----------|-----|
| L01 Hello World | `init()` runs once, `loop()` runs ~50×/s | a `while` loop inside `loop()` freezes the robot | "What happens if you put `telemetry.update()` only in `init()`?" |
| L02 Variables | `int` vs `double`, and what `/` does to each | `5 / 2` is `2`; class vs local scope | "Where must a variable live to survive between loops?" |
| L03 Gamepad | `left_stick_y` is inverted; read it inside the loop | forgetting the negation; button held = repeats every loop | "How would you make a button toggle instead of hold?" |
| L04 Decisions | `if / else if / else` chains; `&&` vs `\|\|` | `=` instead of `==`; a `while` that never ends | "What's the difference between `if/if` and `if/else if`?" |
| L05 Classes | a class bundles data + the methods that use it | `private` fields "not found" from another class | "Why hide a field behind a method?" |
| L06 First hardware | the config name must match the code string exactly | typo in `touch_sensor`; config not activated | "What does the Mechanism class buy us over doing it in the OpMode?" |
| L07 Motors | power is −1..+1, direction and speed together | `setPower(2.0)`; motor never stopped; encoder mode confusion | "What does `RUN_USING_ENCODER` change?" |
| L08 Servos | position 0..1 holds; it is not power | positions outside 0..1; expecting continuous spin | "What does 0.75 mean on this servo?" |
| L09 Analog | voltage → angle with `Range.scale()` | assuming 3.3 V = 5 V; wrong port type in config | "How would you know if the pot is wired backwards?" |
| L10 Color/Distance | one device, two interfaces, one config name | raw RGB depends on lighting; distance saturates | "Why read the same name as two types?" |
| L11 IMU | hub orientation must match the physical mount | yaw wraps at ±180°; wrong `RevHubOrientationOnRobot` | "What happens to yaw when you spin past 180°?" |
| L12 State machines | `enum` + `switch`, one state per loop pass | blocking with `sleep()`; forgetting to transition | "Which state are you in if the sensor never fires?" |
| L13 Arrays | index from 0; `ArrayList` grows, arrays don't | off-by-one; `length` vs `size()` | "What does `list.get(list.size())` do?" |
| L14 Inheritance | `abstract` says "you must implement this" | missing `super()`; forgetting `@Override` | "Why does `TestWiring` not care which `TestItem` it holds?" |
| L20 Driving | normalize so no wheel exceeds 1.0 | one motor reversed; mecanum wheel order | "Why divide by the max instead of clipping?" |
| L24 PID | start with P only; add I and D one at a time | integral windup; tuning all three at once | "What does the robot do with P too high?" |

---

## Differentiation Strategies

### For Advanced Students
- Skip the template, have them write from scratch
- Assign multiple exercises at once
- Have them create documentation for less experienced students
- Challenge them to combine lessons (multi-motor control, etc.)

### For Struggling Students
- Work through template with them step-by-step
- Provide more hints in TODOs
- Have them focus on ONE exercise deeply
- Pair with a mentor/advanced student
- Create a simplified version of the lesson

### For Visual Learners
- Create diagrams (power ranges, servo angles, etc.)
- Have them demo hardware working
- Use telemetry displays prominently

### For Kinesthetic Learners
- Have them physically test ranges (move hand for sensors)
- Manually position servos before coding
- Play with gamepads to feel the input

---

## Troubleshooting During Lessons

### "My code won't compile!"
**Response**:
1. Check for typos in hardware names (case-sensitive)
2. Check imports at top of file
3. Look for missing braces or semicolons
4. Compare with Complete_Solution.java

### "Hardware doesn't respond!"
**Response**:
1. Check configuration is active (Control Hub)
2. Verify hardware name matches code exactly
3. Power cycle robot
4. Try the solution code - if that works, focus on their code differences

### "Motor does something weird"
**Response**:
1. Check the power value being sent (telemetry)
2. Verify motor direction isn't reversed
3. Check battery power (might be low)
4. Test with known-good solution code

### "I don't understand the TODO"
**Response**:
1. Read the TODO comment together
2. Point to similar code in the README
3. Ask: "What do we need to do?" (discovery, not telling)
4. If stuck, show them the hint in the comment

### "Is my answer right?"
**Response**:
1. "Does it compile?"
2. "Does it run?"
3. "Does it do what you expected?"
4. "Compare with the solution - what's different?"

---

## Assessment Ideas

### Quick Checks (After each lesson)
- [ ] Code compiles without errors
- [ ] Hardware responds correctly
- [ ] Student can explain what each line does
- [ ] Telemetry displays expected values
- [ ] Student can modify the code and predict behavior

### Skill Demonstrations
- "Show me how to reverse motor direction in code"
- "Set this servo to exactly 45°"
- "Make this motor stop when I press a button"
- "Read a sensor and display the value"

### Performance Tasks
- Combine motors + gamepad: "Drive forward on left stick"
- Combine servo + button: "Toggle claw on A/B buttons"
- Combine sensor + motor: "Stop when distance < 10cm"

---

## Group Activity Ideas

### Code Review Circles (15 min)
- Small group reads 1-2 students' code
- Discuss what's good, what could improve
- No criticism, just learning

### Demo Show-Off (10 min)
- Each student/pair demos their working lesson
- Others guess what will happen before seeing it
- Celebrate success!

### Reverse Teaching (20 min)
- Pair advanced with struggling student
- Advanced student explains their code
- Struggles to ask specific questions

### Modification Challenge (30 min)
- Give all students same modification task
- See how many different ways they solve it
- Discuss trade-offs

---

## Pacing Guide

### If You Have 1 Hour Per Lesson
- 5 min: Read + discuss concepts
- 5 min: Show solution demo
- 40 min: Students work on template
- 10 min: Check understanding

### If You Have 2 Hours Per Lesson
- 10 min: Concepts + discussion
- 5 min: Demo solution
- 60 min: Students work + coach circulates
- 15 min: Check understanding + review solutions
- 10 min: Quick exercise or homework assigned

### If You Have 30 Minutes (Lightning Round)
- 5 min: Brief concept
- 15 min: Live code-along (coach types, students follow)
- 10 min: Students modify example for homework

---

## Communication with Students

### Effective Feedback
✅ "I see you used a for loop - how does that work?"  
❌ "That's wrong, use a while loop"

✅ "Your sensor reads are changing - why might that be?"  
❌ "You need to average your readings"

✅ "What do you think will happen if you run this?"  
❌ "Your code won't work because..."

### Encouraging Growth Mindset
- "You didn't get it yet" (instead of "You got it wrong")
- "Let's debug together"
- "What would you try next?"
- "That's a good question to ask!"

---

## Tracking Progress

### Student Progress Checklist

Each student keeps `PROGRESS.md` in their package (copied from `learning/STUDENT_PROGRESS.md`):
one row per lesson, one per exercise, and a space for coach notes.

### What to Look For
- ✅ Code compiles and runs
- ✅ Student can explain their code
- ✅ Student debugs when code doesn't work
- ✅ Student attempts exercises
- ✅ Student can modify code and predict results

---

## Next Steps After L24

- Read the team code in `TeamCode/.../teamcode/common/` and `teams/`; `teams/testteam2027`
  is a complete minimal team and its README walks through it
- Give them a real mechanism on the competition robot, with a driver to please
- Vision (AprilTags), odometry and the launcher are covered by `doc/` and the team code, not
  by these lessons

---

## Resources for Coaches

**In This Folder**:
- README.md - Student learning guide, lesson map, per-student package setup
- STUDENT_PROGRESS.md - Progress tracker to copy into each student's package
- FTC_PATTERNS_REFERENCE.md - Code patterns
- HARDWARE_SETUP_GUIDE.md - Programming board wiring and config
- QUICK_REFERENCE.md - Student cheat sheet
- book/ - *Learn Java for FTC* (download it there; not committed)

**External Resources**:
- FTC SDK samples: `FtcRobotController/src/main/java/.../external/samples`
- FTC docs: ftc-docs.firstinspires.org
- REV documentation: docs.revrobotics.com

---

## Final Tips

1. **Patience**: Programming is hard. Some students will struggle.
2. **Celebration**: Celebrate small wins! First motor spin = big deal.
3. **Consistency**: Regular practice matters more than duration.
4. **Customization**: Modify lessons to match your team's hardware.
5. **Documentation**: Save working student code as future reference.
6. **Feedback**: Ask students what helped, what didn't - improve lessons!

---

## Coach Self-Checklist

Before each lesson session:
- [ ] I understand the concepts of today's lesson
- [ ] I can run and explain the solution code
- [ ] I know the common mistakes students make
- [ ] I have a demo ready to show
- [ ] I know my students' experience levels
- [ ] I have a plan for differentiation
- [ ] Hardware is set up and tested
- [ ] I have printed materials ready

After each lesson session:
- [ ] Student code is committed in their package
- [ ] I noted which students are struggling
- [ ] I noted which students are ready for challenges
- [ ] I gave positive feedback
- [ ] I planned next session's approach
- [ ] I updated progress tracking

---

**Remember**: You're not just teaching programming. You're developing future engineers, problem-solvers, and roboticists. That takes patience, encouragement, and genuine interest in their growth. 🤖

---

Good luck coaching!

Last Updated: September 2026
