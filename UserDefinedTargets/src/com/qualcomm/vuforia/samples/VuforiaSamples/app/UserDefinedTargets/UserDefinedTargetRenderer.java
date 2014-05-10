/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.UserDefinedTargets;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.Area;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.ImageTargetResult;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Rectangle;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VirtualButton;
import com.qualcomm.vuforia.VirtualButtonResult;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LineShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleMath;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;

// The renderer class for the ImageTargetsBuilder sample. 
public class UserDefinedTargetRenderer implements GLSurfaceView.Renderer {
	private static final String LOGTAG = "UserDefinedTargetRenderer";

	SampleApplicationSession vuforiaAppSession;

	public boolean mIsActive = false;

	private Vector<Texture> mTextures = new Vector<Texture>();

	private int shaderProgramID;

	private int vertexHandle;

	private int normalHandle;

	private int textureCoordHandle;

	private int mvpMatrixHandle;

	private int texSampler2DHandle;

	// OpenGL ES 2.0 specific (Virtual Buttons):
	private int vbShaderProgramID = 0;
	private int vbVertexHandle = 0;

	private int lineOpacityHandle = 0;
	private int lineColorHandle = 0;
	private int mvpMatrixButtonsHandle = 0;

	// Constants:
	static final float kObjectScale = 20.0f;

	private CubeObject cubeObject;

	// Set the texture used for the cube model:
	int textureIndex = 0;

	// Reference to main activity
	private UserDefinedTargets mActivity;

	/**
	 * The model view matrix
	 */
	private Matrix44F modelViewMatrix;

	public UserDefinedTargetRenderer(UserDefinedTargets activity,
			SampleApplicationSession session) {
		mActivity = activity;
		vuforiaAppSession = session;
		setModelViewMatrix();
	}

	private void setModelViewMatrix() {
		modelViewMatrix = new Matrix44F();
		float[] m = new float[16];
		// Set the modelview as "identity" matrix
		for (int i = 0; i < 16; ++i) {
			m[i] = 0;
		}
		m[0] = 1.0f;
		m[5] = 1.0f;
		m[10] = 1.0f;
		m[15] = 1.0f;
		Matrix.translateM(m, 0, 0.0f, 0.0f, 200.0f);
		modelViewMatrix.setData(m);
	}

	// Called when the surface is created or recreated.
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

		// Call function to initialize rendering:
		initRendering();

		// Call Vuforia function to (re)initialize rendering after first use
		// or after OpenGL ES context was lost (e.g. after onPause/onResume):
		vuforiaAppSession.onSurfaceCreated();
	}

	// Called when the surface changed size.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

		// Call function to update rendering when render surface
		// parameters have changed:
		mActivity.updateRendering();

		// Call Vuforia function to handle render surface size changes:
		vuforiaAppSession.onSurfaceChanged(width, height);
	}

	// Called to draw the current frame.
	@Override
	public void onDrawFrame(GL10 gl) {
		if (!mIsActive)
			return;

		// Call our function to render content
		renderFrame();
	}

	private int getScreenWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		return screenWidth;
	}

	private int getScreenHeight() {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenHeight = metrics.widthPixels;
		return screenHeight;
	}

	private void renderFrame() {
		// Clear color and depth buffer
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Get the state from Vuforia and mark the beginning of a rendering
		// section
		State state = Renderer.getInstance().begin();

		// Explicitly render the Video Background
		Renderer.getInstance().drawVideoBackground();

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		// GLES20.glEnable(GLES20.GL_BLEND);
		// GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
		// GLES20.GL_ONE_MINUS_SRC_ALPHA);
		// Draw the cube:

		// We must detect if background reflection is active and adjust the
		// culling direction.
		// If the reflection is active, this means the post matrix has been
		// reflected as well, therefore standard counter clockwise face
		// culling will result in "inside out" models.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) {
			GLES20.glFrontFace(GLES20.GL_CW);
		} // Front camera
		else {
			GLES20.glFrontFace(GLES20.GL_CCW);
		} // Back camera

		// Render the RefFree UI elements depending on the current state
		mActivity.refFreeFrame.render();

		// Did we find any trackables this frame?
		for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
			// Get the trackable:
			TrackableResult trackableResult = state.getTrackableResult(tIdx);
			// Get the model view matrix
			/*
			 * modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult
			 * .getPose());
			 */
			// Check the type of the trackable:
			assert (trackableResult.getType() == MarkerTracker.getClassType());
			MarkerResult markerResult = (MarkerResult) (trackableResult);
			Marker marker = (Marker) markerResult.getTrackable();

			switch (marker.getMarkerId()) {
			case 0:
				renderButtons(trackableResult);
			}

			// Assumptions:
			assert (textureIndex < mTextures.size());
			Texture thisTexture = mTextures.get(textureIndex);

			/*
			  Matrix44F modelViewMatrix_Vuforia = Tool
			 .convertPose2GLMatrix(trackableResult.getPose());
			 */
			float[] modelViewMatrix = this.modelViewMatrix.getData();

			float[] modelViewProjection = new float[16];
			Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, kObjectScale);
			Matrix.scaleM(modelViewMatrix, 0, kObjectScale, kObjectScale,
					kObjectScale);
			Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
					.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

			GLES20.glUseProgram(shaderProgramID);

			GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
					false, 0, cubeObject.getVertices());
			GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
					false, 0, cubeObject.getNormals());
			GLES20.glVertexAttribPointer(textureCoordHandle, 2,
					GLES20.GL_FLOAT, false, 0, cubeObject.getTexCoords());
			GLES20.glEnableVertexAttribArray(vertexHandle);
			GLES20.glEnableVertexAttribArray(normalHandle);
			GLES20.glEnableVertexAttribArray(textureCoordHandle);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
					thisTexture.mTextureID[0]);
			GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
					modelViewProjection, 0);
			GLES20.glUniform1i(texSampler2DHandle, 0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES,
					cubeObject.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
					cubeObject.getIndices());

			GLES20.glDisableVertexAttribArray(vertexHandle);
			GLES20.glDisableVertexAttribArray(normalHandle);
			GLES20.glDisableVertexAttribArray(textureCoordHandle);

			SampleUtils.checkGLError("UserDefinedTargets renderFrame");
		}

		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		// GLES20.glDisable(GLES20.GL_BLEND);
		Renderer.getInstance().end();
	}

	private void renderButtons(TrackableResult trackableResult) {

		// Get the trackable:
		float[] modelViewMatrix = Tool.convertPose2GLMatrix(
				trackableResult.getPose()).getData();// {0.99998903f,
														// 0.0043661683f,
														// -0.0016890828f,
														// 0.45908383f,
														// 0.004304928f,
														// -0.9993884f,
														// -0.03470344f,
														// -0.3460904f,
														// -0.0018395709f,
														// 0.03469579f,
														// -0.9993963f,
														// 273.47565f};

		// The image target specific result:
		assert (trackableResult.getType() == ImageTargetResult.getClassType());
		ImageTargetResult imageTargetResult = (ImageTargetResult) trackableResult;

		// Set transformations:
		float[] modelViewProjection = new float[16];
		Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
				.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

		float vbVertices[] = new float[imageTargetResult.getNumVirtualButtons() * 24];
		short vbCounter = 0;

		// Iterate through this targets virtual buttons:
		for (int i = 0; i < imageTargetResult.getNumVirtualButtons(); ++i) {
			VirtualButtonResult buttonResult = imageTargetResult
					.getVirtualButtonResult(i);
			VirtualButton button = buttonResult.getVirtualButton();

			int buttonIndex = 0;
			// Run through button name array to find button index
			for (int j = 0; j < UserDefinedTargets.NUM_BUTTONS; ++j) {
				if (button.getName()
						.compareTo(mActivity.virtualButtonColors[j]) == 0) {
					buttonIndex = j;
					break;
				}
			}

			// If the button is pressed, than use this texture:
			if (buttonResult.isPressed()) {
				textureIndex = buttonIndex + 1;
			}

			Area vbArea = button.getArea();
			assert (vbArea.getType() == Area.TYPE.RECTANGLE);
			Rectangle vbRectangle[] = new Rectangle[4];
			vbRectangle[0] = new Rectangle(-108.68f, -53.52f, -75.75f, -65.87f);
			vbRectangle[1] = new Rectangle(-45.28f, -53.52f, -12.35f, -65.87f);
			vbRectangle[2] = new Rectangle(14.82f, -53.52f, 47.75f, -65.87f);
			vbRectangle[3] = new Rectangle(76.57f, -53.52f, 109.50f, -65.87f);

			// We add the vertices to a common array in order to have one
			// single
			// draw call. This is more efficient than having multiple
			// glDrawArray calls
			vbVertices[vbCounter] = vbRectangle[buttonIndex].getLeftTopX();
			vbVertices[vbCounter + 1] = vbRectangle[buttonIndex].getLeftTopY();
			vbVertices[vbCounter + 2] = 0.0f;
			vbVertices[vbCounter + 3] = vbRectangle[buttonIndex]
					.getRightBottomX();
			vbVertices[vbCounter + 4] = vbRectangle[buttonIndex].getLeftTopY();
			vbVertices[vbCounter + 5] = 0.0f;
			vbVertices[vbCounter + 6] = vbRectangle[buttonIndex]
					.getRightBottomX();
			vbVertices[vbCounter + 7] = vbRectangle[buttonIndex].getLeftTopY();
			vbVertices[vbCounter + 8] = 0.0f;
			vbVertices[vbCounter + 9] = vbRectangle[buttonIndex]
					.getRightBottomX();
			vbVertices[vbCounter + 10] = vbRectangle[buttonIndex]
					.getRightBottomY();
			vbVertices[vbCounter + 11] = 0.0f;
			vbVertices[vbCounter + 12] = vbRectangle[buttonIndex]
					.getRightBottomX();
			vbVertices[vbCounter + 13] = vbRectangle[buttonIndex]
					.getRightBottomY();
			vbVertices[vbCounter + 14] = 0.0f;
			vbVertices[vbCounter + 15] = vbRectangle[buttonIndex].getLeftTopX();
			vbVertices[vbCounter + 16] = vbRectangle[buttonIndex]
					.getRightBottomY();
			vbVertices[vbCounter + 17] = 0.0f;
			vbVertices[vbCounter + 18] = vbRectangle[buttonIndex].getLeftTopX();
			vbVertices[vbCounter + 19] = vbRectangle[buttonIndex]
					.getRightBottomY();
			vbVertices[vbCounter + 20] = 0.0f;
			vbVertices[vbCounter + 21] = vbRectangle[buttonIndex].getLeftTopX();
			vbVertices[vbCounter + 22] = vbRectangle[buttonIndex].getLeftTopY();
			vbVertices[vbCounter + 23] = 0.0f;
			vbCounter += 24;

		}

		// We only render if there is something on the array
		if (vbCounter > 0) {
			// Render frame around button
			GLES20.glUseProgram(vbShaderProgramID);

			GLES20.glVertexAttribPointer(vbVertexHandle, 3, GLES20.GL_FLOAT,
					false, 0, fillBuffer(vbVertices));

			GLES20.glEnableVertexAttribArray(vbVertexHandle);

			GLES20.glUniform1f(lineOpacityHandle, 1.0f);
			GLES20.glUniform3f(lineColorHandle, 1.0f, 1.0f, 1.0f);

			GLES20.glUniformMatrix4fv(mvpMatrixButtonsHandle, 1, false,
					modelViewProjection, 0);

			// We multiply by 8 because that's the number of vertices per
			// button
			// The reason is that GL_LINES considers only pairs. So some
			// vertices
			// must be repeated.
			GLES20.glLineWidth(10.0f);
			GLES20.glDrawArrays(GLES20.GL_LINES, 0,
					imageTargetResult.getNumVirtualButtons() * 8);

			SampleUtils.checkGLError("VirtualButtons drawButton");

			GLES20.glDisableVertexAttribArray(vbVertexHandle);
		}
	}

	private Buffer fillBuffer(float[] array) {
		// Convert to floats because OpenGL doesnt work on doubles, and manually
		// casting each input value would take too much time.
		ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); // each
																		// float
																		// takes
																		// 4
																		// bytes
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (float d : array)
			bb.putFloat(d);
		bb.rewind();
		return bb;
	}

	private void initRendering() {
		Log.d(LOGTAG, "initRendering");

		cubeObject = new CubeObject();

		// Define clear color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Now generate the OpenGL texture objects and add settings
		for (Texture t : mTextures) {
			GLES20.glGenTextures(1, t.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_BYTE, t.mData);
		}

		shaderProgramID = SampleUtils.createProgramFromShaderSrc(
				CubeShaders.CUBE_MESH_VERTEX_SHADER,
				CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

		vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
				"vertexPosition");
		normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
				"vertexNormal");
		textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
				"vertexTexCoord");
		mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
				"modelViewProjectionMatrix");
		texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
				"texSampler2D");

		// OpenGL setup for Virtual Buttons
		vbShaderProgramID = SampleUtils.createProgramFromShaderSrc(
				LineShaders.LINE_VERTEX_SHADER,
				LineShaders.LINE_FRAGMENT_SHADER);

		mvpMatrixButtonsHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
				"modelViewProjectionMatrix");
		vbVertexHandle = GLES20.glGetAttribLocation(vbShaderProgramID,
				"vertexPosition");
		lineOpacityHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
				"opacity");
		lineColorHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
				"color");
	}

	public void setTextures(Vector<Texture> textures) {
		mTextures = textures;
	}

}
