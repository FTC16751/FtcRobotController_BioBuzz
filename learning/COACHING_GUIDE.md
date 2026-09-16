# FTC Learning System: Coaching Guide

A guide for coaches, mentors, and team leads running the FTC Learning System.

---

## Quick Start for Coaches

1. **Understand the progression**: Lessons 1→2→3→4 build sequentially
2. **Prepare hardware** using HARDWARE_SETUP_GUIDE.md
3. **Have students read the README** before coding
4. **Give students the Template code** to complete
5. **Circulate and help with TODOs**
6. **Compare solutions** together as a team
7. **Assign exercises** for extra challenge

---

## Lesson Structure Overview

| Lesson | Duration | Focus | Prerequisites |
|--------|----------|-------|---------------|
| 1: Motors | 2-3 hrs | Basic motor control | None |
| 2: Servos | 2-3 hrs | Precise positioning | Lesson 1 |
| 3: Sensors | 3-4 hrs | Reading input data | Lesson 1 |
| 4: Gamepad | 3-4 hrs | Driver interaction | Lessons 1-3 |

**Total Time**: ~10-14 hours per student

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
- [ ] Save their code to Student_Code folder

---

## Coaching Strategies by Lesson

### Lesson 1: Motor Basics

**Key Points to Emphasize**:
- Motor power is the key control variable
- Range -1.0 to +1.0 includes direction AND speed
- OpMode structure: initialize → wait → loop → repeat

**Common Struggles**:
- Confusion about negative values (reverse)
- Forgetting to initialize before using
- Not understanding why `opModeIsActive()` matters

**Pro Tips**:
- Have students manually write out the power sequence (1.0 → -1.0 → 0.0)
- Show what happens if they forget to initialize
- Demo the difference between FWD/REV by listening/feeling motor

**Questions to Ask**:
1. "What happens if you set power to 2.0?"
2. "Why do we call `waitForStart()`?"
3. "What's the difference between 0.5 and -0.5?"

---

### Lesson 2: Servo Control

**Key Points to Emphasize**:
- Servos are different from motors (positional vs. continuous)
- Position range 0.0-1.0 maps to angles 0-180°
- Servo moves immediately, not gradually

**Common Struggles**:
- Confusing servo position with motor power
- Setting position outside 0.0-1.0 range
- Not understanding 0.5 = 90°

**Pro Tips**:
- Have students manually position servo (0, 0.25, 0.5, 0.75, 1.0)
- Draw a diagram: 0.0 ← 0.5 → 1.0 with angles labeled
- Show servo movement by hand, then with code

**Questions to Ask**:
1. "If 0.0 = 0° and 1.0 = 180°, what does 0.75 = ?"
2. "Can a servo spin continuously like a motor?"
3. "What would happen if you set position to 1.5?"

---

### Lesson 3: Sensor Reading

**Key Points to Emphasize**:
- Sensors provide feedback about the world
- Different sensors have different ranges and units
- Sensors enable decision-making

**Common Struggles**:
- Not understanding sensor units (cm, RGB values, etc.)
- Noisy/flickering readings
- Confusion about analog vs. digital

**Pro Tips**:
- Have students map the range physically (5cm, 10cm, 50cm, etc.)
- Show both raw and averaged readings
- Use telemetry to visualize readings changing

**Questions to Ask**:
1. "What's the purpose of a sensor?"
2. "Why might sensor readings be noisy?"
3. "How would you know if a sensor is broken?"

---

### Lesson 4: Gamepad Control

**Key Points to Emphasize**:
- Gamepad is the interface between driver and robot
- Different input types: buttons, sticks, triggers
- **Stick Y-axis is INVERTED** (critical!)

**Common Struggles**:
- Forgetting the Y-axis inversion
- Confusing button logic (if/else-if chains)
- Not reading gamepad inside the loop

**Pro Tips**:
- Draw diagram showing stick inversion
- Have students test stick input with telemetry first
- Demo what happens when they forget negation

**Questions to Ask**:
1. "Why is left stick Y-axis inverted?"
2. "What's the difference between button and trigger input?"
3. "How would you make a toggle button?"

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
```
[ ] Lesson 1 - Motor Basics
    [ ] Template completed
    [ ] Tested on robot
    [ ] Solution reviewed
    [ ] 1+ Exercise attempted
    
[ ] Lesson 2 - Servo Control
    [ ] Template completed
    [ ] Tested on robot
    [ ] Solution reviewed
    [ ] Challenge attempted
    
[ ] Lesson 3 - Sensor Reading
    [ ] Template completed
    [ ] Tested on robot
    [ ] Solution reviewed
    [ ] Multi-sensor exercise
    
[ ] Lesson 4 - Gamepad Control
    [ ] Template completed
    [ ] Tested on robot
    [ ] Solution reviewed
    [ ] Complex control implemented
```

### What to Look For
- ✅ Code compiles and runs
- ✅ Student can explain their code
- ✅ Student debugs when code doesn't work
- ✅ Student attempts exercises
- ✅ Student can modify code and predict results

---

## Next Steps After Lesson 4

### Students Ready for More:
- Multi-motor drive systems (tank/mecanum)
- Encoder-based position control
- State machines for complex behavior
- Autonomous programming
- Vision processing (AprilTags)

### Start Competitive Programming:
- TeleOp with real robot from competition
- Build real mechanisms
- Practice drive control with complex input
- Learn team roles (driver, operator, programmer)

---

## Resources for Coaches

**In This Folder**:
- README.md - Student learning guide
- FTC_PATTERNS_REFERENCE.md - Code patterns
- HARDWARE_SETUP_GUIDE.md - Hardware config
- QUICK_REFERENCE.md - Student cheat sheet
- LearnJavaForFTC - Comprehensive learning (team library)

**External Resources**:
- FTC SDK Examples: `RobotController/TeamCode`
- REV Documentation: rev-robotics.com/ftc
- FIRST Resources: firstinspires.org

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
- [ ] I collected student code into Student_Code folder
- [ ] I noted which students are struggling
- [ ] I noted which students are ready for challenges
- [ ] I gave positive feedback
- [ ] I planned next session's approach
- [ ] I updated progress tracking

---

**Remember**: You're not just teaching programming. You're developing future engineers, problem-solvers, and roboticists. That takes patience, encouragement, and genuine interest in their growth. 🤖

---

Good luck coaching!

Last Updated: May 2026
