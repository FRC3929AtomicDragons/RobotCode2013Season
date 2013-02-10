/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package team3929.commands;

/**
 *
 * @author Carter
 */
public class ManualShooterControl extends CommandBase {

    double power;

    public ManualShooterControl() {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
        requires(shooter);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        power = .1;

    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        //rotate shooter based on the axes of the attack3 controller

        shooter.rotateTurretByJoy((oi.getAttackX()));
        shooter.spinUpToPowerLevel(-oi.getAttackY());

        if (oi.checkAttackButton(3)) {
            shooter.changeWithPower(.2);
        } else if (!oi.checkAttackButton(2) && !oi.checkAttackButton(3)) {
            shooter.changeWithPower(0);
        } else if (oi.checkAttackButton(2)) {
            shooter.changeWithPower(-.11);
        }

        System.out.println(power);

    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
