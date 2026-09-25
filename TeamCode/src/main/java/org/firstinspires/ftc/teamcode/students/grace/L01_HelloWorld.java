// ============================================================
//  LESSON 01 — Hello World & OpMode Structure
//  Book: Chapter 1  |  No hardware needed
// ============================================================
//
//  INSTRUCTIONS: Fill in every section marked with // TODO
//  Run it on the Driver Station and verify each telemetry line appears.
//
//  File must be saved in:
//  TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
//  and the class name below MUST match the filename.
// ============================================================

package org.firstinspires.ftc.teamcode.students.grace;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// @TeleOp makes this program appear in the TeleOp list on the Driver Station.
// The name in the parentheses is optional — if left empty it uses the class name.
@TeleOp(name = "L01 Hello World - Grace", group = "Grace")
public class L01_HelloWorld extends OpMode {

    // ── CLASS MEMBERS ──────────────────────────────────────────────────────────
    // Variables declared here are accessible in ALL methods below.
    // They keep their values between calls to loop().

    // TODO 1: Declare an int variable named teamNumber and set it to 17651
    //         (or your actual team number)
    //
    //         Format:   int variableName = value;
    //
    //  int teamNumber = ???;
    int teamNumber = 16751;

    // TODO 2: Declare a String variable named studentName and set it to your name
    //
    //         Format:   String variableName = "text";
    //
    //  String studentName = ???;
    String studentName = "Elaine And Grace";


    // ── INIT ───────────────────────────────────────────────────────────────────
    // Called ONCE when the driver presses INIT on the Driver Station.
    // Great place for one-time setup messages.
    @Override
    public void init() {

        // TODO 3: Send a greeting to the Driver Station.
        //         Use telemetry.addData("label", value) to show two pieces of info.
        //         Example:  telemetry.addData("Hello", "Driver Station!");
        //
        //         Show:  "Hello" → your name
        //                "Team"  → your team number
        telemetry.addData("Hello ", studentName);
        telemetry.addData("My team number is: ", teamNumber);



        // This forces the telemetry to display right away (needed in init()).
        telemetry.update();
    }


    // ── LOOP ───────────────────────────────────────────────────────────────────
    // Called REPEATEDLY (~50 times/second) while the OpMode is running.
    // The Driver Station screen refreshes automatically each loop cycle.
    @Override
    public void loop() {

        // TODO 4: Show the runtime (how long the OpMode has been running).
        //         getRuntime() returns a double (number of seconds).
        //         Label it "Runtime (sec)"
        //
        //         telemetry.addData("Runtime (sec)", ???);
        telemetry.addData("i love opmodes every: ", getRuntime());



        // TODO 5 (CHALLENGE): Show the loop number.
        //         Hint: You'll need a class-level int variable that you increment
        //         each time loop() is called. Declare it above in the class members
        //         section (not inside this method).

    }


    // ── OPTIONAL METHODS ──────────────────────────────────────────────────────
    // These are already inherited from OpMode. You only need to @Override them
    // if you want custom behaviour.

    // init_loop() → runs repeatedly between INIT and PLAY
    // start()     → runs once when PLAY is pressed
    // stop()      → runs once when STOP is pressed

    // TODO 6 (CHALLENGE): Add a start() method that sends "OpMode Started!" to
    //                     telemetry and calls telemetry.update().
}
