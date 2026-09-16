// ============================================================
//  LESSON 14 — Inheritance  (FILE 1 of 4: Abstract Base Class)
//  Book: Chapter 14, Listing 14.4
// ============================================================
//  This is the CONTRACT that every TestItem must fulfill.
//  It cannot be instantiated directly — only its subclasses can.
// ============================================================

package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

// TODO 1: Make this class abstract by adding the 'abstract' keyword
//   abstract public class L14_TestItem {
public class L14_TestItem {

    // TODO 2: Declare a private String field named 'description'
    //   private String description;


    // TODO 3: Write a PROTECTED constructor that takes a String description
    //         and stores it in the field.
    //         (protected = visible to this class and its subclasses)
    //
    //   protected L14_TestItem(String description) {
    //       this.description = description;
    //   }


    // TODO 4: Write a public getter for description
    //   public String getDescription() { return description; }


    // TODO 5: Declare an ABSTRACT method named 'run' with these parameters:
    //         boolean on, Telemetry telemetry
    //         It returns void and has no body.
    //
    //   abstract public void run(boolean on, Telemetry telemetry);

}
