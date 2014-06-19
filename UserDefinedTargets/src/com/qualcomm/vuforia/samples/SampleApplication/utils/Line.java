package com.qualcomm.vuforia.samples.SampleApplication.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.UserDefinedTargets.UserDefinedTargetRenderer;

public class Line {

	private FloatBuffer VertexBuffer;

	private final String VertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 uMVPMatrix;" +

	"attribute vec4 vPosition;" + "void main() {" +
	// the matrix must be included as a modifier of gl_Position
			"  gl_Position = uMVPMatrix * vPosition;" + "}";

	private final String FragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	protected int GlProgram;
	protected int vertexHandle;
	protected int ColorHandle;
	protected int MVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	private float lineCoords[] = { 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f };

	private final int VertexCount = lineCoords.length / COORDS_PER_VERTEX;
	private final int VertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per
	private GLSurfaceView.Renderer renderer; // vertex

	// Set color with red, green, blue and alpha (opacity) values
	private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
	private int textureIndex =0;
	private boolean end1Selected;
	private boolean end2Selected;

	public Line(UserDefinedTargetRenderer renderer) {
		this.renderer = renderer;
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
				lineCoords.length * 4);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		VertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		VertexBuffer.put(lineCoords);
		// set the buffer to read the first coordinate
		VertexBuffer.position(0);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				FragmentShaderCode);

		GlProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
		GLES20.glAttachShader(GlProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(GlProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(GlProgram); // creates OpenGL ES program
											// executables

	}

	public static int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	public void SetVerts(float v0, float v1, float v2, float v3, float v4,
			float v5) {
		lineCoords[0] = v0;
		lineCoords[1] = v1;
		lineCoords[2] = v2;
		lineCoords[3] = v3;
		lineCoords[4] = v4;
		lineCoords[5] = v5;

		VertexBuffer.put(lineCoords);
		// set the buffer to read the first coordinate
		VertexBuffer.position(0);

	}

	public void SetColor(float red, float green, float blue, float alpha) {
		color[0] = red;
		color[1] = green;
		color[2] = blue;
		color[3] = alpha;		
	}

	public void draw(float[] modelViewProjection, int shaderProgramID,
			int mvpMatrixHandle,Vector<Texture> textures) {

		GLES20.glUseProgram(shaderProgramID);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		Texture thisTexture = textures.get(textureIndex);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, thisTexture.mTextureID[0]);
		// get handle to vertex shader's vPosition member
		// PositionHandle = GLES20.glGetAttribLocation(GlProgram, "vPosition");
		GLES20.glLineWidth(20.0f);
		GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
				0, VertexBuffer);
		GLES20.glEnableVertexAttribArray(vertexHandle);
		// Set color for drawing the triangle
		GLES20.glUniform4fv(ColorHandle, 1, color, 0);
		GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
				modelViewProjection, 0);
		checkGlError("glUniformMatrix4fv");
		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, VertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(vertexHandle);
		SampleUtils.checkGLError("UserDefinedTargets renderFrame");

	}

	public void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("SEVERE", op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	public float[] getLineEnd1() {
		float[] pointCoordinates = { lineCoords[0], lineCoords[1],
				lineCoords[2] };
		return pointCoordinates;
	}

	public float[] getLineEnd2() {
		float[] pointCoordinates = { lineCoords[3], lineCoords[4],
				lineCoords[5] };
		return pointCoordinates;
	}

	public void setLineEnd1(float[] p1) {
		SetVerts(p1[0], p1[1], p1[2], lineCoords[3], lineCoords[4],
				lineCoords[5]);
	}

	public void setLineEnd2(float[] p2) {
		SetVerts(lineCoords[0], lineCoords[1], lineCoords[2], p2[0], p2[1],
				p2[2]);
	}

	public boolean isEnd1Selected() {
		return end1Selected;
	}

	public void setEnd1Selected(boolean end1Selected) {
		this.end1Selected = end1Selected;
		SetColor(1,0,0,1);
		textureIndex=1;
	}

	public boolean isEnd2Selected() {
		return end2Selected;
	}

	public void setEnd2Selected(boolean end2Selected) {
		this.end2Selected = end2Selected;
		SetColor(1,0,0,1);
		textureIndex=1;
	}
	

}