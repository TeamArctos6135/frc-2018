
package org.usfirst.frc.team6135.robot;

import org.usfirst.frc.team6135.robot.subsystems.*;
import org.usfirst.frc.team6135.robot.commands.autocommands.*;
import org.usfirst.frc.team6135.robot.commands.autoutils.AutoTurn;
import org.usfirst.frc.team6135.robot.commands.autoutils.DriveStraightDistance;
import org.usfirst.frc.team6135.robot.commands.autoutils.DriveStraightDistanceEx;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
//import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	//SUBSYSTEMS
	//public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;
	public static DriveTrain drive;
	public static IntakeSubsystem intakeSubsystem;
	public static GearShiftSubsystem gearShiftSubsystem;
	public static ElevatorSubsystem elevatorSubsystem;
	public static WristSubsystem wristSubsystem;
	public static VisionSubsystem visionSubsystem;
	
	public static Alliance color;
	public static int station; //Starting position of robot
	public static String gameData;
	
	//These commands are combined with the alliance colour and switch location and used later
	//They are the options that are shown in the auto menu
	public static PlaceCubeFromMiddle placeCubeFromMiddle;
	public static PlaceCubeSameSide placeCubeLeftSide;
	public static PlaceCubeFromSideOffset placeCubeLeftSideOffset;
	public static PlaceCubeFromSideOffset placeCubeRightSideOffset;
	public static PlaceCubeSameSide placeCubeRightSide;
	public static VisionAuto visionAuto;
	
	//Autonomous command chooser
	Command autonomousCommand;
	SendableChooser<Command> chooser = new SendableChooser<>();
	
	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		//Initialize our subsystems
		RobotMap.init();
		RobotMap.wristGyro.calibrate();
		drive = new DriveTrain();
		intakeSubsystem = new IntakeSubsystem();
		gearShiftSubsystem = new GearShiftSubsystem();
		elevatorSubsystem = new ElevatorSubsystem();
		wristSubsystem = new WristSubsystem();
		
		//Get the team's colour and station number
		station = DriverStation.getInstance().getLocation();
		color = DriverStation.getInstance().getAlliance();
		
		//Initialize camera stream and vision subsystem
        visionSubsystem = new VisionSubsystem(CameraServer.getInstance().startAutomaticCapture());
        //Set camera config
        visionSubsystem.setMode(VisionSubsystem.Mode.VISION);
        
        oi = new OI();
        (new Thread(new TestingThread())).start();

        //Add commands into the autonomous command chooser
        chooser.addDefault("Drive straight distance", new DriveStraightDistance(30.0, 0.5));
		chooser.addObject("Turn 90 degrees", new AutoTurn(90, 0.75));
		placeCubeFromMiddle = new PlaceCubeFromMiddle(PlaceCubeFromMiddle.DIRECTION_LEFT);
		placeCubeLeftSide = new PlaceCubeSameSide();
		placeCubeRightSide = new PlaceCubeSameSide();
		placeCubeLeftSideOffset = new PlaceCubeFromSideOffset(PlaceCubeFromSideOffset.DIRECTION_LEFT);
		placeCubeRightSideOffset = new PlaceCubeFromSideOffset(PlaceCubeFromSideOffset.DIRECTION_RIGHT);
		visionAuto = new VisionAuto(VisionAuto.DIRECTION_LEFT);
		chooser.addDefault("No Auto", null);
		chooser.addObject("Drive Past Baseline", new DrivePastBaseLine());
		chooser.addObject("Place Cube (Aligned with switch): Left", placeCubeLeftSide);
		chooser.addObject("Place Cube (Aligned with switch): Right", placeCubeRightSide);
		chooser.addObject("Place Cube (From side): Left", placeCubeLeftSideOffset);
		chooser.addObject("Place Cube (From side): Right", placeCubeRightSideOffset);
		chooser.addObject("Place Cube: Middle", placeCubeFromMiddle);
		chooser.addObject("Place Cube With Vision: Middle", visionAuto);
		//Display the chooser
		SmartDashboard.putData("Auto mode", chooser);
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
	 */
	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		
		//Retrieve the selected auto command
		autonomousCommand = chooser.getSelected();
		
		if(autonomousCommand != null) {
			gameData = DriverStation.getInstance().getGameSpecificMessage().toUpperCase();
			if(gameData.length() > 0){
				//Depending on which side the alliance switch is on, some commands need to change
				if(gameData.charAt(0) == 'L'){
					//If the alliance switch is on the left side
					if(autonomousCommand.equals(placeCubeFromMiddle)) {
						//If command is to place a cube from the middle
						(new PlaceCubeFromMiddle(PlaceCubeFromMiddle.DIRECTION_LEFT)).start();
					}
					//Use == to check if they're the exact same object
					else if(autonomousCommand == placeCubeLeftSide) {
						//If command is to place a cube from the left side, start the command
						autonomousCommand.start();
					}
					else if(autonomousCommand == placeCubeRightSide) {
						//If command is to place a cube from the right, give up placing the cube and
						//instead drive past the paseline
						(new DrivePastBaseLineOffset(DrivePastBaseLineOffset.DIRECTION_RIGHT)).start();
					}
					else if(autonomousCommand == placeCubeLeftSideOffset) {
						autonomousCommand.start();
					}
					else if(autonomousCommand == placeCubeRightSideOffset) {
						(new DrivePastBaseLine()).start();
					}
					else if(autonomousCommand == visionAuto) {
						(new VisionAuto(VisionAuto.DIRECTION_LEFT)).start();
					}
					else {
						autonomousCommand.start();
					}
				} 
				else {
					if(autonomousCommand.equals(placeCubeFromMiddle)) {
						(new PlaceCubeFromMiddle(PlaceCubeFromMiddle.DIRECTION_RIGHT)).start();
					}
					else if(autonomousCommand == placeCubeRightSide) {
						autonomousCommand.start();
					}
					else if(autonomousCommand == placeCubeLeftSide) {
						(new DrivePastBaseLineOffset(DrivePastBaseLineOffset.DIRECTION_LEFT)).start();
					}
					else if(autonomousCommand == placeCubeRightSideOffset) {
						autonomousCommand.start();
					}
					else if(autonomousCommand == placeCubeLeftSideOffset) {
						(new DrivePastBaseLine()).start();
					}
					else if(autonomousCommand == visionAuto) {
						(new VisionAuto(VisionAuto.DIRECTION_RIGHT)).start();
					}
					else {
						autonomousCommand.start();
					}
				}
			}
		}
		

		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		//Set camera config
		visionSubsystem.setMode(VisionSubsystem.Mode.VISION);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();
	}

	@Override
	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null)
			autonomousCommand.cancel();
		//Set camera config
		visionSubsystem.setMode(VisionSubsystem.Mode.VIDEO);
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		//According to documentation, this method is deprecated since it's no longer required
		//LiveWindow.run();
	}
}
