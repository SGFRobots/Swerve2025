package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.GenericHID;
import frc.robot.Constants;

import frc.robot.subsystems.SwerveSubsystem;

public class SwerveJoystick extends Command {
    private final SwerveSubsystem mSwerveSubsystem;
    private final SlewRateLimiter xLimiter, yLimiter, turningLimiter;
    private final GenericHID mController;

    // Constructor
    public SwerveJoystick(SwerveSubsystem pSwerveSubsystem, GenericHID pController) {
        // Subsystem and controller instances
        mSwerveSubsystem = pSwerveSubsystem;
        mController = pController;

        // Slew Rate Limiter smooths the robot's accelerations
        xLimiter = new SlewRateLimiter(Constants.Mechanical.kTeleDriveMaxAccelerationUnitsPerSecond);
        yLimiter = new SlewRateLimiter(Constants.Mechanical.kTeleDriveMaxAccelerationUnitsPerSecond);
        turningLimiter = new SlewRateLimiter(Constants.Mechanical.kTeleDriveMaxAngularAccelerationUnitsPerSecond);
        addRequirements(mSwerveSubsystem);
    }

    @Override
    public void initialize() {}

    @Override
    public void execute() {
        // Get joystick inputs
        double xSpeed = -mController.getRawAxis(Constants.Controllers.LeftYPort);
        double ySpeed = -mController.getRawAxis(Constants.Controllers.LeftXPort);
        double turningSpeed = -mController.getRawAxis(Constants.Controllers.RightXPort); 
        
        // Apply Deadzone
        xSpeed = Math.abs(xSpeed) > Constants.Mechanical.kDeadzone ? xSpeed : 0.0;
        ySpeed = Math.abs(ySpeed) > Constants.Mechanical.kDeadzone ? ySpeed : 0.0;
        turningSpeed = Math.abs(turningSpeed) > Constants.Mechanical.kDeadzone ? turningSpeed : 0.0;
        
        // Make Driving Smoother using Slew Rate Limiter - less jerky by accelerating slowly
        xSpeed = xLimiter.calculate(xSpeed);
        ySpeed = yLimiter.calculate(ySpeed);
        turningSpeed = turningLimiter.calculate(turningSpeed);

        // Calculate speed in m/s
        xSpeed *= Constants.Mechanical.kTeleDriveMaxSpeedMetersPerSecond;
        ySpeed *= Constants.Mechanical.kTeleDriveMaxSpeedMetersPerSecond;
        turningSpeed *= Constants.Mechanical.kTeleDriveMaxSpeedMetersPerSecond;

        // Set desire chassis speeds based on field or robot relative
        ChassisSpeeds chassisSpeed;
        // chassisSpeed = new ChassisSpeeds(xSpeed, ySpeed, turningSpeed);

        // // double rcw = pJoystick->GetTwist();
        // // double forwrd = pJoystick->GetY() * -1; /* Invert stick Y axis */
        // // double strafe = pJoystick->GetX();
        // // float pi = 3.1415926;
        // /* Adjust Joystick X/Y inputs by navX MXP yaw angle */
        // double gyro_degrees = mSwerveSubsystem.getHeading();
        // double gyro_radians = gyro_degrees * Math.PI/180; 
        // double temp = xSpeed * Math.cos(gyro_radians) + ySpeed * Math.sin(gyro_radians);
        // ySpeed = -xSpeed * Math.sin(gyro_radians) + ySpeed * Math.cos(gyro_radians);
        // xSpeed = temp;
        // /* At this point, Joystick X/Y (strafe/forwrd) vectors have been */
        // /* rotated by the gyro angle, and can be sent to drive system */


        chassisSpeed = ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, turningSpeed, mSwerveSubsystem.geRotation2d());
        // chassisSpeed = new ChassisSpeeds(xSpeed, ySpeed, turningSpeed);

        chassisSpeed = ChassisSpeeds.discretize(chassisSpeed, 0.02);
        // chassisSpeed.vxMetersPerSecond *= -1;
        // chassisSpeed.vyMetersPerSecond *= -1;

        // Drive
        mSwerveSubsystem.drive(chassisSpeed);

        // Test
        // mSwerveSubsystem.driveIndividualModule(xSpeed, turningSpeed);
    }

    @Override
    public void end(boolean interrupted) {
        // Stop all modules
        mSwerveSubsystem.stopModules();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
