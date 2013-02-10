/*
 * The class represents the shooter, hood and the turret subsystems.
 */
package team3929.subsystems;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.Timer;
import team3929.commands.ManualShooterControl;
import team3929.templates.RobotMap;

/**
 *
 * @author Carter Henderson
 * @author Brent Gray
 * @version 2012-03-03-1308
 */
public class Shooter extends Subsystem {

    private static final double SHOOTER_MOTOR_STARTUP_POWER = 0.2;
    private static final double SHOOTER_MOTOR_SPINDOWN_POWER = 0.0;
    /***********************  SHOOTER SUBSYSTEM *********************** */
    // the shooter has two Jags being controlled off one PWM
    // therefore this Jaguar acutally represents both
    Jaguar shooterMotors;
    // RPM encoder for the shooter -- NOT INCLUDED ON THE BOT CURRENTLY
    //Encoder encoder;
    /***********************  HOOD SUBSYSTEM *********************** */
    // safety for hood.
    // TODO:  is this necessary?  there might be mechanical stops at this point.
    //DigitalInput hoodSafetyLimitSwitch;
    Victor hoodAngleMotor;
    AnalogChannel hoodAnglePot;
    PIDController hoodAngleController;
    /***********************  TURRET SUBSYSTEM *********************** */
    AnalogChannel turretRotationPot;
    Victor turretRotationMotor;
    PIDController turretRotationController;
    // mechanical zero on the pot will be set to 90 degrees left
    // driving coordinate system will be 0 straight ahead, -90 for LEFT;
    // +90 for RIGHT
    int potMechanicalOffset = 90;
    public boolean hoodIsDown;

    public Shooter() {
        hoodIsDown = true;
        turretRotationMotor = new Victor(RobotMap.DPWM_shooterVic1);
        hoodAngleMotor = new Victor(RobotMap.DPWM_shooterVic2);
        shooterMotors = new Jaguar(RobotMap.DPWM_shooterJag3);
        // hoodSafetyLimitSwitch = new DigitalInput(RobotMap.DIO_shooterLimSwitch);
        //encoder = new Encoder(RobotMap.DIO_shooterEncoderChannel1, RobotMap.DIO_shooterEncoderChannel2, false);
        hoodAnglePot = new AnalogChannel(RobotMap.A_Potential1);
        hoodAngleController = new PIDController(0.1, 0.001, 0.0, hoodAnglePot, hoodAngleMotor);
        turretRotationPot = new AnalogChannel(RobotMap.A_Potential2);
        turretRotationController = new PIDController(0.1, 0.001, 0.0, turretRotationPot, turretRotationMotor);
    }

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        setDefaultCommand(new ManualShooterControl());
    }

    /*********************** TURRET SUBSYSTEM METHODS *********************** */
    // mechanical angle on the pot is 0 (90 degrees LEFT) to 180 (90 degrees RIGHT)
    // input is in the drivetrain coordinate system (0 is straight ahead)
    public void rotateTurretToAngle(int angle) {
        turretRotationController.setSetpoint(convertDriverAngleToPotAngle(angle));
    }

    public void rotateTurretByJoy(double speed) {
        turretRotationMotor.set(speed);
    }

    public int convertDriverAngleToPotAngle(int driverAngle) {
        if (driverAngle >= -potMechanicalOffset && driverAngle <= 0) {
            return Math.abs(potMechanicalOffset - Math.abs(driverAngle));
        } else if (driverAngle > 0 && driverAngle <= 90) {
            return potMechanicalOffset + Math.abs(driverAngle);
        }
        return 0;
    }

    public int convertPotAngleToDriverAngle(int potAngle) {
        if (potAngle >= 0 && potAngle <= 90) {
            return (-1) * potAngle;
        } else if (potAngle >= 90 && potAngle <= 180) {
            return potAngle - potMechanicalOffset;
        }
        return 0;
    }

    // Joystick is [-1..1]
    // TODO:  calibrate turret to joystick on manual
    public void rotateTurretToAngleByJoystick(double d) {
        this.rotateTurretToAngle((int) (d + 1 * 2.5));
    }

    public long checkAnglePotentiometer() {//checks potentiometer of the hood which can get the angle of the hood
        long value = 0;
        // value = hoodAnglePot.getAccumulatorValue();
        //getAccumulatorValue(); returns how far the potentiometer is turned at the given call to it.
        //getAccumulatorCount(); returns a total count since the robot was turned on.
        return value;
    }

    public long checkRotaterPotentiometer() {//checks potentiometer of the turret which gets angle of rotation
        long value = 0;
        value = turretRotationPot.getAccumulatorValue();
        return value;
    }

    public void setTurretToZero() {
        this.rotateTurretToAngle(0);
    }

    /*********************** SHOOTER SUBSYSTEM METHODS *********************** */

    /* The shooter doesn't have an encoder on it currently, so all RPM control
     * is done using an estimate of power level.
     */
    // spins up the shooter to a minimum level
    public void spinUpToMinimum() {
        shooterMotors.set(SHOOTER_MOTOR_STARTUP_POWER);
    }

    public void spinUpToPowerLevel(double power) {
        shooterMotors.set(power);
    }

    // spins down the shooter
    public void spinDown() {//stops shooting wheels
        shooterMotors.set(SHOOTER_MOTOR_SPINDOWN_POWER);
    }

//    public int checkEncoder() {//gets encoder value at given moment
//        int value = 0;
//        //value = encoder.get();
//        return value;
//    }
    /*********************** HOOD SUBSYSTEM METHODS *********************** */
    //changes hood angle
    public void changeAngle(boolean thing) {
        if (thing) {
            hoodAngleMotor.set(.2);
            Timer.delay(5.3);
            hoodAngleMotor.set(0);
            hoodIsDown = false;
        } else if (!thing) {
            hoodAngleMotor.set(-.11);
            Timer.delay(1.25);
            hoodAngleMotor.set(0);
            hoodIsDown = true;
        }
    }
    public void changeWithPower(double power){
        hoodAngleMotor.set(power);
    }
//    //checks if limit switch is pressed
//    public boolean checkLimit() {
//        return hoodSafetyLimitSwitch.get();
//    }
}
