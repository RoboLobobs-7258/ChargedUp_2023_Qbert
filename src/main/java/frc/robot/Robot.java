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
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;

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
SlewRateLimiter forwardfilter = new SlewRateLimiter(2);


  int state;
  Timer T = new Timer();

  private void PrintDebug()
  {
    SmartDashboard.putNumber("accelX", accelerometer.getX());
    SmartDashboard.putNumber("accely", accelerometer.getY());
    SmartDashboard.putNumber("accelz", accelerometer.getZ());
    SmartDashboard.putNumber("position", m_rearLeft.getSelectedSensorPosition());
    SmartDashboard.putNumber("ur timer", T.get());
    SmartDashboard.putNumber("state", state);
    SmartDashboard.putNumber( "Extender distince",extender.getSelectedSensorPosition());
    
  }
  private void PickupCube(boolean on)
  {
    if (on){
      graber.set(-0.6);
      extender.set(ControlMode.MotionMagic,distiance_to_edge( 9));
    }
    else {
      graber.set(0);
      extender.set(ControlMode.MotionMagic,distiance_to_edge( 5));
    }
  }
  private void toss(boolean on)
  {
if (on)
{
  graber.set(0.35);
  extender.set(ControlMode.MotionMagic,distiance_to_edge( 9));
}
else
{
graber.set(0);
extender.set(ControlMode.MotionMagic,distiance_to_edge( 5));
}
  }
  private void balance(boolean on)
  {

  }

  private void out(boolean on)
  {
    if (on)
    {
      extender.set(ControlMode.MotionMagic,distiance_to_edge( 9));
    }
    else 
    {
    //  extender.set(0);
    }
  }
  private void in(boolean on)
  {
    if (on)
    {
    //  extender.set(ControlMode.MotionMagic,distiance_to_edge(1));
      extender.set(ControlMode.MotionMagic,distiance_to_edge(5)
      );
    }
    

    else
    {
   //   extender.set(0);
    }
  }

  private void backwards()
  {
    m_drive.arcadeDrive(-0.2,0);
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
  private double cs_extender_rpm = 6000;
  private double cs_extender_motor = (cs_extender_rpm / 600) * 4096;
  private double ac_extender_seconds = .25;
  private double ac_extender = cs_extender_motor / ac_extender_seconds;
  private double distiance_per_rev = 1;
  private double gear_ratio = 3;
  private double spintime;
  private double overdistance;
  private double overspeed;
  private double balancedistance;
  private double balancespeed;
private double distiance_to_edge(double distiance_inches)
{
  return distiance_inches * distiance_per_rev * gear_ratio * 4096;
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
    CameraServer.startAutomaticCapture();
    extender.config_kP(0, .08, 0);
    extender.config_kI(0, 0, 0);
    extender.config_kD(0, 1, 0);
    extender.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
    extender.setSensorPhase(false);
    extender.setSelectedSensorPosition(0);
// TODO change start position code
extender.configMotionCruiseVelocity(cs_extender_motor,30);
extender.configMotionAcceleration(ac_extender,30);

double value = SmartDashboard.getNumber("spintime", 2);
SmartDashboard.putNumber("spintime", value);

 value = SmartDashboard.getNumber("overdistance", -160000);
SmartDashboard.putNumber("overdistance", value);

 value = SmartDashboard.getNumber("overspeed", -.4);
SmartDashboard.putNumber("overspeed", value);

 value = SmartDashboard.getNumber("balancedistance", -88000);
SmartDashboard.putNumber("balancedistance", value);

 value = SmartDashboard.getNumber("balancespeed", .4);
SmartDashboard.putNumber("balancespeed", value);
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
    state = 1;
    T.reset();
    T.start();
    m_rearLeft.getSelectedSensorPosition();
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
 spintime = SmartDashboard.getNumber("spintime", 2);

 overdistance = SmartDashboard.getNumber("overdistance", -160000);

 overspeed = SmartDashboard.getNumber("overspeed", -.4);

 balancedistance = SmartDashboard.getNumber("balancedistance", -88000);

 balancespeed = SmartDashboard.getNumber("balancespeed", .4);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (state) 
    {
      case 1:
        m_drive.arcadeDrive(0,0);
        graber.set(.35);
         if (T.get()>spintime)
         {
           m_rearLeft.setSelectedSensorPosition(0);
          state = 2;
         }
      break;
      case 2:
        m_drive.arcadeDrive(overspeed, 0);
        
        graber.set(0);
        
        if (m_rearLeft.getSelectedSensorPosition()<overdistance)
         {
           state = 4;
         }
        
      break;
      case 4:
      
      m_drive.arcadeDrive(balancespeed, 0);
        
      graber.set(0);
      
      if (m_rearLeft.getSelectedSensorPosition()>balancedistance)
       {

         state = 500;
       }

      break;
      case 5:
       m_drive.arcadeDrive(0,0);
       balance(true);
      break;
      default:
        graber.set(0);
       
        m_drive.arcadeDrive(0, 0);
        break;
        
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    m_rearLeft.setSelectedSensorPosition(0);

  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    m_drive.arcadeDrive(
      (Math.pow(
        forwardfilter.calculate(
          -Joystick.getY()),1)), 
      Math.pow(
        -Joystick.getTwist(),1)*0.5);

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
      extender.set(-.5);
    }
    else if (controller.getRightBumper()){
      extender.set(.5);
      
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






