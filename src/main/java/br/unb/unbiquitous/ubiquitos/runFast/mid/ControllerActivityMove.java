package br.unb.unbiquitous.ubiquitos.runFast.mid;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ControllerActivityMove extends Activity implements
		SensorEventListener {

	private static String TAG = "controller move";

	private SensorManager mSensorManager = null;

	// angular speeds from gyro
	private float[] gyro = new float[3];

	// rotation matrix from gyro data
	private float[] gyroMatrix = new float[9];

	// orientation angles from gyro matrix
	private float[] gyroOrientation = new float[3];

	// magnetic field vector
	private float[] magnet = new float[3];

	// accelerometer vector
	private float[] accel = new float[3];

	// orientation angles from accel and magnet
	private float[] accMagOrientation = new float[3];

	// final orientation angles from sensor fusion
	private float[] fusedOrientation = new float[3];

	// accelerometer and magnetometer based rotation matrix
	private float[] rotationMatrix = new float[9];

	public static final float EPSILON = 0.000000001f;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;

	public static final int TIME_CONSTANT = 30;
	public static final float FILTER_COEFFICIENT = 0.98f;
	private Timer fuseTimer = new Timer();

	private Button left, right, up, down, action1, action2, plusLeft,
			plusRight, plusSelect, quit;
	private ToggleButton enableMove;

	TextView txt;
	public Handler mHandler;
	Matrix matrix;
	int angle = 0;

	boolean cond = true;
	Map<String, Object> map = null;
	boolean inputPerformed;
	int actualValue = 0;

	private Gateway gateway = null;

	private String character;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");

		MidManager.setActivity(this);
		gateway = MidManager.getGateway();

		character = getIntent().getStringExtra("character");
		if (character.equals("pilot")) {
			setContentView(R.layout.pilot_controller);
			initPilotUI();
			initPilotListeners();
		} else if (character.equals("copilot")) {
			setContentView(R.layout.copilot_controller);
			initCopilotUI();
			initCopilotListeners();
		} else
			setContentView(R.layout.assistant_controller);
		quit = (Button) findViewById(R.id.controller_quit);
		initQuitListener();

		// setContentView(R.layout.controller);

	}

	@Override
	public void onBackPressed() {
		createQuitDialog().show();
	}

	private void initUI() {
		left = (Button) findViewById(R.id.controller_left);
		right = (Button) findViewById(R.id.controller_right);
		up = (Button) findViewById(R.id.controller_up);
		down = (Button) findViewById(R.id.controller_down);

		action1 = (Button) findViewById(R.id.controller_action1);

		txt = (TextView) findViewById(R.id.tv_angle);

		enableMove = (ToggleButton) findViewById(R.id.enable_move);
		enableMove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.enable_move)
					cond = !cond;
			}
		});

		// initialize sensor variables
		gyroOrientation[0] = 0.0f;
		gyroOrientation[1] = 0.0f;
		gyroOrientation[2] = 0.0f;

		// initialize gyroMatrix with identity matrix
		gyroMatrix[0] = 1.0f;
		gyroMatrix[1] = 0.0f;
		gyroMatrix[2] = 0.0f;
		gyroMatrix[3] = 0.0f;
		gyroMatrix[4] = 1.0f;
		gyroMatrix[5] = 0.0f;
		gyroMatrix[6] = 0.0f;
		gyroMatrix[7] = 0.0f;
		gyroMatrix[8] = 1.0f;

		// get sensorManager and initialize sensor listeners
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		initListeners();

		// wait for one second until gyroscope and magnetometer/accelerometer
		// data is initialized then schedule the complementary filter task
		fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
				1000, TIME_CONSTANT);

		// GUI stuff
		mHandler = new Handler();
	}

	private void initCopilotUI() {
		initUI();
		action2 = (Button) findViewById(R.id.controller_action2);
	}

	private void initPilotUI() {
		initUI();
		action2 = (Button) findViewById(R.id.controller_action2);
		plusLeft = (Button) findViewById(R.id.controller_plusLeft);
		plusRight = (Button) findViewById(R.id.controller_plusRight);
		plusSelect = (Button) findViewById(R.id.controller_plusSelect);
	}

	private void initQuitListener() {
		final ControllerActivityMove activity = this;
		quit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.createQuitDialog().show();
			}
		});
	}

	private AlertDialog createQuitDialog() {
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage(R.string.quitDialog_message)
				.setTitle(R.string.quitDialog_title)
				.setPositiveButton(R.string.quitDialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK button: call the quit service
								if (gateway.listDevices().contains(
										MidManager.getGameDevice()))
									try {
										Map<String, Object> map = new HashMap<String, Object>();
										map.put("deviceName", gateway
												.getCurrentDevice().getName());
										gateway.callService(
												MidManager.getGameDevice(),
												"playerQuit",
												MidManager.RFDEVICES_DRIVER,
												null, null, map);
									} catch (ServiceCallException e) {
										e.printStackTrace();
									}

								// Finishes this activity!
								finish();
							}
						})
				.setNegativeButton(R.string.quitDialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog: do nothing
							}
						});

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		return dialog;
	}

	private ControllerListener initListeners() {
		ControllerListener listener = new ControllerListener(
				MidManager.getGateway(), this, getIntent().getStringExtra(
						"character"));
		left.setOnTouchListener(listener);
		right.setOnTouchListener(listener);
		up.setOnTouchListener(listener);
		down.setOnTouchListener(listener);

		action1.setOnTouchListener(listener);

		return listener;
	}

	private void initCopilotListeners() {
		ControllerListener listener = initListeners();
		action2.setOnTouchListener(listener);
	}

	private void initPilotListeners() {
		ControllerListener listener = initListeners();
		action2.setOnTouchListener(listener);
		plusLeft.setOnTouchListener(listener);
		plusRight.setOnTouchListener(listener);
		plusSelect.setOnTouchListener(listener);

		// initialize sensor's listeners
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_FASTEST);

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (cond) {
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				// copy new accelerometer data into accel array and calculate
				// orientation
				System.arraycopy(event.values, 0, accel, 0, 3);
				calculateAccMagOrientation();
				break;

			case Sensor.TYPE_GYROSCOPE:
				// process gyro data
				gyroFunction(event);
				break;

			case Sensor.TYPE_MAGNETIC_FIELD:
				// copy new magnetometer data into magnet array
				System.arraycopy(event.values, 0, magnet, 0, 3);
				break;
			}
		}
	}

	// calculates orientation angles from accelerometer and magnetometer output
	public void calculateAccMagOrientation() {
		if (SensorManager
				.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}

	// This function is borrowed from the Android reference
	// at
	// http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
	private void getRotationVectorFromGyro(float[] gyroValues,
			float[] deltaRotationVector, float timeFactor) {
		float[] normValues = new float[3];

		// Calculate the angular speed of the sample
		float omegaMagnitude = (float) Math
				.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1]
						* gyroValues[1] + gyroValues[2] * gyroValues[2]);

		// Normalize the rotation vector if it's big enough to get the axis
		if (omegaMagnitude > EPSILON) {
			normValues[0] = gyroValues[0] / omegaMagnitude;
			normValues[1] = gyroValues[1] / omegaMagnitude;
			normValues[2] = gyroValues[2] / omegaMagnitude;
		}

		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
		float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

	// This function performs the integration of the gyroscope data.
	// It writes the gyroscope based orientation into gyroOrientation.
	public void gyroFunction(SensorEvent event) {
		// don't start until first accelerometer/magnetometer orientation has
		// been acquired
		if (accMagOrientation == null)
			return;

		// initialisation of the gyroscope based rotation matrix
		if (initState) {
			float[] initMatrix = new float[9];
			initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
			float[] test = new float[3];
			SensorManager.getOrientation(initMatrix, test);
			gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
			initState = false;
		}

		// copy the new gyro values into the gyro array
		// convert the raw gyro data into a rotation vector
		float[] deltaVector = new float[4];
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			System.arraycopy(event.values, 0, gyro, 0, 3);
			getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
		}

		// measurement done, save current time for next interval
		timestamp = event.timestamp;

		// convert rotation vector into rotation matrix
		float[] deltaMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

		// apply the new rotation interval on the gyroscope based rotation
		// matrix
		gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

		// get the gyroscope based orientation from the rotation matrix
		SensorManager.getOrientation(gyroMatrix, gyroOrientation);
	}

	private float[] getRotationMatrixFromOrientation(float[] o) {
		float[] xM = new float[9];
		float[] yM = new float[9];
		float[] zM = new float[9];

		float sinX = (float) Math.sin(o[1]);
		float cosX = (float) Math.cos(o[1]);
		float sinY = (float) Math.sin(o[2]);
		float cosY = (float) Math.cos(o[2]);
		float sinZ = (float) Math.sin(o[0]);
		float cosZ = (float) Math.cos(o[0]);

		// rotation about x-axis (pitch)
		xM[0] = 1.0f;
		xM[1] = 0.0f;
		xM[2] = 0.0f;
		xM[3] = 0.0f;
		xM[4] = cosX;
		xM[5] = sinX;
		xM[6] = 0.0f;
		xM[7] = -sinX;
		xM[8] = cosX;

		// rotation about y-axis (roll)
		yM[0] = cosY;
		yM[1] = 0.0f;
		yM[2] = sinY;
		yM[3] = 0.0f;
		yM[4] = 1.0f;
		yM[5] = 0.0f;
		yM[6] = -sinY;
		yM[7] = 0.0f;
		yM[8] = cosY;

		// rotation about z-axis (azimuth)
		zM[0] = cosZ;
		zM[1] = sinZ;
		zM[2] = 0.0f;
		zM[3] = -sinZ;
		zM[4] = cosZ;
		zM[5] = 0.0f;
		zM[6] = 0.0f;
		zM[7] = 0.0f;
		zM[8] = 1.0f;

		// rotation order is y, x, z (roll, pitch, azimuth)
		float[] resultMatrix = matrixMultiplication(xM, yM);
		resultMatrix = matrixMultiplication(zM, resultMatrix);
		return resultMatrix;
	}

	private float[] matrixMultiplication(float[] A, float[] B) {
		float[] result = new float[9];

		result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
		result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
		result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

		result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
		result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
		result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

		result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
		result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
		result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

		return result;
	}

	class calculateFusedOrientationTask extends TimerTask {
		public void run() {
			float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

			/*
			 * Fix for 179° <--> -179° transition problem: Check whether one of
			 * the two orientation angles (gyro or accMag) is negative while the
			 * other one is positive. If so, add 360° (2 * math.PI) to the
			 * negative value, perform the sensor fusion, and remove the 360°
			 * from the result if it is greater than 180°. This stabilizes the
			 * output in positive-to-negative-transition cases.
			 */

			// azimuth
			if (gyroOrientation[0] < -0.5 * Math.PI
					&& accMagOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (FILTER_COEFFICIENT
						* (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[0]);
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[0] < -0.5 * Math.PI
					&& gyroOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (FILTER_COEFFICIENT
						* gyroOrientation[0] + oneMinusCoeff
						* (accMagOrientation[0] + 2.0 * Math.PI));
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0]
						+ oneMinusCoeff * accMagOrientation[0];
			}

			// pitch
			if (gyroOrientation[1] < -0.5 * Math.PI
					&& accMagOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (FILTER_COEFFICIENT
						* (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[1]);
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[1] < -0.5 * Math.PI
					&& gyroOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (FILTER_COEFFICIENT
						* gyroOrientation[1] + oneMinusCoeff
						* (accMagOrientation[1] + 2.0 * Math.PI));
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1]
						+ oneMinusCoeff * accMagOrientation[1];
			}

			// roll
			if (gyroOrientation[2] < -0.5 * Math.PI
					&& accMagOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (FILTER_COEFFICIENT
						* (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[2]);
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[2] < -0.5 * Math.PI
					&& gyroOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (FILTER_COEFFICIENT
						* gyroOrientation[2] + oneMinusCoeff
						* (accMagOrientation[2] + 2.0 * Math.PI));
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2]
						+ oneMinusCoeff * accMagOrientation[2];
			}

			// overwrite gyro matrix and orientation with fused orientation
			// to comensate gyro drift
			gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
			System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

			// update sensor output in GUI
			mHandler.post(updateOreintationDisplayTask);
		}
	}

	// **************************** GUI FUNCTIONS
	// *********************************

	public void updateOreintationDisplay() {
		// inputPerformed = false;
		map = new HashMap<String, Object>();
		angle = (int) (gyroOrientation[1] * 180 / Math.PI);
		matrix = new Matrix();
		matrix.postRotate(-angle);
		txt.setText(String.valueOf(angle) + "°");

		//if(cond && (angle % 10 == 0) && angle != 0) {
		if(cond && angle < 45 && angle > -45) {
			if (angle > 10 && !inputPerformed) {
				map.put("inputCode", ""+0x03);//"LEFT");
				inputPerformed = true;
			}else if(angle <= 10 && angle > 0 && inputPerformed) {
				map.put("inputCode", ""+0x03);//"LEFT");
				inputPerformed = false;
			}else if (angle < -10 && !inputPerformed) {
				map.put("inputCode", ""+0x04);//"RIGHT");
				inputPerformed = true;
			}else if(angle >= -10 && angle < 0 && inputPerformed) {
				map.put("inputCode", ""+0x04);//"RIGHT");
				inputPerformed = false;
			}else
				return;

			//actualValue = angle;

			// Calls the middleware driver.
			try {
				if (inputPerformed)
					gateway.callService(null,
							"inputPerformed",
							"br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver",
							null, null, map);
				else
					gateway.callService(
							null,
							"inputReleased",
							"br.unb.unbiquitous.ubiquitos.runFast.mid.RFInputDriver",
							null, null, map);
			} catch (ServiceCallException e) {
				e.printStackTrace();
			}
		}

		/*
		 * try{ Thread.sleep(1000); }catch (InterruptedException e) { // TODO:
		 * handle exception e.printStackTrace(); }
		 */
	}

	private Runnable updateOreintationDisplayTask = new Runnable() {
		public void run() {
			updateOreintationDisplay();
		}
	};

	/**
	 * @return the character
	 */
	public String getCharacter() {
		return character;
	}

	@Override
	public void onStop() {
		super.onStop();
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// restore the sensor listeners when user resumes the application.
		initListeners();
	}

}