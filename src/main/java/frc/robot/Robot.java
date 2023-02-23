// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  Joystick Joystick = new Joystick(0); // 0 is the USB Port to be used as indicated on the Driver Station
XboxController controller = new XboxController(1);
  WPI_TalonFX m_frontLeft = new WPI_TalonFX(1);
  WPI_TalonFX m_rearLeft = new WPI_TalonFX(2);
  MotorControllerGroup m_left = new MotorControllerGroup(m_frontLeft, m_rearLeft);

  WPI_TalonFX m_frontRight = new WPI_TalonFX(3);
  WPI_TalonFX m_rearRight = new WPI_TalonFX(4);
  MotorControllerGroup m_right = new MotorControllerGroup(m_frontRight, m_rearRight);
  DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);
  WPI_TalonSRX graber = new WPI_TalonSRX(5);
  WPI_TalonSRX extender = new WPI_TalonSRX(6);
  Accelerometer accelerometer = new BuiltInAccelerometer();

  int state;
  Timer T = new Timer();

  private void PrintDebug()
  {
    SmartDashboard.putNumber("accelX", accelerometer.getX());
    SmartDashboard.putNumber("accely", accelerometer.getY());
    SmartDashboard.putNumber("accelz", accelerometer.getZ());
  }
  private void PickupCube(boolean on)
  {
    if (on){
       graber.set(-0.25);
    }
    else {
      graber.set(0);
    }
  }
  private void toss(boolean on)
  {
if (on)
{
  graber.set(0.25);
}
else
{
  graber.set(0);
}
  }
  private void balance(boolean on)
  {

  }

  private void out(boolean on)
  {
    if (on)
    {
      extender.set(-0.25);
    }
    else 
    {
      extender.set(0);
    }
  }
  private void in(boolean on)
  {
    if (on)
    {
      extender.set(0.25);
    }
    else
    {
      extender.set(0);
    }
  }

  private void backwards()
  {
    m_drive.arcadeDrive(-0.5,0);
  }
  private boolean iswall()
  {
    return true;
  }  
  private void forward()
  {
    m_drive.arcadeDrive(0.5, 0);
  }

  private boolean istossed(Timer t)
  {
    if (t.get()>6)
  {
    return true;
  }
  else
  {
    return false;
  }
  }
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    m_frontLeft.setNeutralMode(NeutralMode.Brake);
    m_rearLeft.setNeutralMode(NeutralMode.Brake);
    m_frontRight.setNeutralMode(NeutralMode.Brake);
    m_rearRight.setNeutralMode(NeutralMode.Brake);
    m_right.setInverted(true);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() 
  {
    PrintDebug();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    state = 100;
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (state) 
    {
      case 1:
        backwards();
        if (iswall())
        {
          state = 2;
          T.reset(); 
        }
        break;
      case 2:
        m_drive.arcadeDrive(0,0);
        toss(true);
        // if (isstoptoss())
        // {
        //   state = 3;
        // }
      break;
      case 3:
        forward();
         if (accelerometer.getZ()<0.5)
         {
           state = 4;
         }
        
      break;
      case 4:
      forward();
      if (accelerometer.getZ()>0.9 && accelerometer.getZ()<1.1)
      {
        state = 5;
      }

      break;
      case 5:
       m_drive.arcadeDrive(0,0);
       balance(true);
      break;
      default:
        // Put default auto code here
        break;
        
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {


    m_drive.arcadeDrive((Math.pow(-Joystick.getY(),3))*0.5, Math.pow(-Joystick.getTwist(),3)*0.5);

    if (controller.getYButton()){
    toss(false);
      PickupCube(true);
    }
    else if (controller.getAButton()){
      PickupCube(false);
      toss(true);
    }

    else {
    PickupCube(false);
    toss(false);
    }

    if (controller.getXButton()){
      balance(true);
    }

    else {
      balance(false);
    }

    if (controller.getLeftBumper()){
      in(false);
      out(true);
    }
    else if (controller.getRightBumper()){
      out(false);
      in(true);
    }

    else {
      out(false);
      in(false);
    }
  }
  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
  
}
