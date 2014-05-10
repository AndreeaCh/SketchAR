/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.SampleApplication.utils;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;

import android.app.Activity;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.util.Log;


public class SampleUtils
{
    
    private static final String LOGTAG = "Vuforia_Sample_Applications";
    
    
    static int initShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0)
        {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            
            int[] glStatusVar = { GLES20.GL_FALSE };
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, glStatusVar,
                0);
            if (glStatusVar[0] == GLES20.GL_FALSE)
            {
                Log.e(LOGTAG, "Could NOT compile shader " + shaderType + " : "
                    + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            
        }
        
        return shader;
    }
    
    
    public static int createProgramFromShaderSrc(String vertexShaderSrc,
        String fragmentShaderSrc)
    {
        int vertShader = initShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
        int fragShader = initShader(GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderSrc);
        
        if (vertShader == 0 || fragShader == 0)
            return 0;
        
        int program = GLES20.glCreateProgram();
        if (program != 0)
        {
            GLES20.glAttachShader(program, vertShader);
            checkGLError("glAttchShader(vert)");
            
            GLES20.glAttachShader(program, fragShader);
            checkGLError("glAttchShader(frag)");
            
            GLES20.glLinkProgram(program);
            int[] glStatusVar = { GLES20.GL_FALSE };
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, glStatusVar,
                0);
            if (glStatusVar[0] == GLES20.GL_FALSE)
            {
                Log.e(
                    LOGTAG,
                    "Could NOT link program : "
                        + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        
        return program;
    }
    
    
    public static void checkGLError(String op)
    {
        for (int error = GLES20.glGetError(); error != 0; error = GLES20
            .glGetError())
            Log.e(
                LOGTAG,
                "After operation " + op + " got glError 0x"
                    + Integer.toHexString(error));
    }
    
    
    // Transforms a screen pixel to a pixel onto the camera image,
    // taking into account e.g. cropping of camera image to fit different aspect
    // ratio screen.
    // for the camera dimensions, the width is always bigger than the height
    // (always landscape orientation)
    // Top left of screen/camera is origin
    public static void screenCoordToCameraCoord(int screenX, int screenY,
        int screenDX, int screenDY, int screenWidth, int screenHeight,
        int cameraWidth, int cameraHeight, int[] cameraX, int[] cameraY,
        int[] cameraDX, int[] cameraDY)
    {
        boolean isPortraitMode = (screenWidth < screenHeight);
        float videoWidth, videoHeight;
        videoWidth = (float) cameraWidth;
        videoHeight = (float) cameraHeight;
        
        if (isPortraitMode)
        {
            // the width and height of the camera are always
            // based on a landscape orientation
            
            // as the camera coordinates are always in landscape
            // we convert the inputs into a landscape based coordinate system
            int tmp = screenX;
            screenX = screenY;
            screenY = screenWidth - tmp;
            
            tmp = screenDX;
            screenDX = screenDY;
            screenDY = tmp;
            
            tmp = screenWidth;
            screenWidth = screenHeight;
            screenHeight = tmp;
        } else
        {
            videoWidth = (float) cameraWidth;
            videoHeight = (float) cameraHeight;
        }
        
        float videoAspectRatio = videoHeight / videoWidth;
        float screenAspectRatio = (float) screenHeight / (float) screenWidth;
        
        float scaledUpX;
        float scaledUpY;
        float scaledUpVideoWidth;
        float scaledUpVideoHeight;
        
        if (videoAspectRatio < screenAspectRatio)
        {
            // the video height will fit in the screen height
            scaledUpVideoWidth = (float) screenHeight / videoAspectRatio;
            scaledUpVideoHeight = screenHeight;
            scaledUpX = (float) screenX
                + ((scaledUpVideoWidth - (float) screenWidth) / 2.0f);
            scaledUpY = (float) screenY;
        } else
        {
            // the video width will fit in the screen width
            scaledUpVideoHeight = (float) screenWidth * videoAspectRatio;
            scaledUpVideoWidth = screenWidth;
            scaledUpY = (float) screenY
                + ((scaledUpVideoHeight - (float) screenHeight) / 2.0f);
            scaledUpX = (float) screenX;
        }
        
        if (cameraX != null && cameraX.length > 0)
        {
            cameraX[0] = (int) ((scaledUpX / (float) scaledUpVideoWidth) * videoWidth);
        }
        
        if (cameraY != null && cameraY.length > 0)
        {
            cameraY[0] = (int) ((scaledUpY / (float) scaledUpVideoHeight) * videoHeight);
        }
        
        if (cameraDX != null && cameraDX.length > 0)
        {
            cameraDX[0] = (int) (((float) screenDX / (float) scaledUpVideoWidth) * videoWidth);
        }
        
        if (cameraDY != null && cameraDY.length > 0)
        {
            cameraDY[0] = (int) (((float) screenDY / (float) scaledUpVideoHeight) * videoHeight);
        }
    }
    
    
    public static float[] getOrthoMatrix(float nLeft, float nRight,
        float nBottom, float nTop, float nNear, float nFar)
    {
        float[] nProjMatrix = new float[16];
        
        int i;
        for (i = 0; i < 16; i++)
            nProjMatrix[i] = 0.0f;
        
        nProjMatrix[0] = 2.0f / (nRight - nLeft);
        nProjMatrix[5] = 2.0f / (nTop - nBottom);
        nProjMatrix[10] = 2.0f / (nNear - nFar);
        nProjMatrix[12] = -(nRight + nLeft) / (nRight - nLeft);
        nProjMatrix[13] = -(nTop + nBottom) / (nTop - nBottom);
        nProjMatrix[14] = (nFar + nNear) / (nFar - nNear);
        nProjMatrix[15] = 1.0f;
        
        return nProjMatrix;
    }
    
    public static float[] translatePoseMatrix(float x, float y, float z)
    {
    	float[] matrix= new float[16];

        // matrix * translate_matrix
        matrix[12] += 
            (matrix[0] * x + matrix[4] * y + matrix[8]  * z);
            
        matrix[13] += 
            (matrix[1] * x + matrix[5] * y + matrix[9]  * z);
            
        matrix[14] += 
            (matrix[2] * x + matrix[6] * y + matrix[10] * z);
            
        matrix[15] += 
            (matrix[3] * x + matrix[7] * y + matrix[11] * z);
        return matrix;
    }
   

	// ----------------------------------------------------------------------------
	// Touch projection
	// ----------------------------------------------------------------------------

	private Vec3F projectScreenPointToPlane(Vec3F point, Vec3F planeCenter,
			Vec3F planeNormal,Matrix44F modelViewMatrix,int screenWidth,int screenHeight) {
		// Cache the projection matrix:
		CameraCalibration cameraCalibration = CameraDevice.getInstance()
				.getCameraCalibration();
		Matrix44F projectionMatrix = Tool.getProjectionGL(cameraCalibration,
				2.0f, 2500.0f);
		// Invert the projection matrix
		Matrix44F inverseProjMatrix = SampleMath
				.Matrix44FInverse(projectionMatrix);

		// Window Coordinates to Normalized Device Coordinates
		VideoBackgroundConfig config = Renderer.getInstance()
				.getVideoBackgroundConfig();
		float halfScreenWidth = screenWidth / 2.0f;
		float halfScreenHeight = screenHeight / 2.0f;

		float halfViewportWidth = config.getSize().getData()[0] / 2.0f;
		float halfViewportHeight = config.getSize().getData()[1] / 2.0f;

		float x = (point.getData()[0] - halfScreenWidth) / halfViewportWidth;
		float y = (point.getData()[1] - halfScreenHeight) / halfViewportHeight
				* -1;

		Vec4F ndcNear = new Vec4F(x, y, -1, 1);
		Vec4F ndcFar = new Vec4F(x, y, 1, 1);

		// Normalized Device Coordinates to Eye Coordinates
		Vec4F pointOnNearPlane = SampleMath.Vec4FTransform(ndcNear,
				inverseProjMatrix);
		Vec4F pointOnFarPlane = SampleMath.Vec4FTransform(ndcFar,
				inverseProjMatrix);
		pointOnNearPlane = SampleMath.Vec4FDiv(pointOnNearPlane,
				pointOnNearPlane.getData()[3]);
		pointOnFarPlane = SampleMath.Vec4FDiv(pointOnFarPlane,
				pointOnFarPlane.getData()[3]);

		// Eye Coordinates to Object Coordinates
		Matrix44F inverseModelViewMatrix = SampleMath
				.Matrix44FInverse(modelViewMatrix);

		Vec4F nearWorld = SampleMath.Vec4FTransform(pointOnNearPlane,
				inverseModelViewMatrix);
		Vec4F farWorld = SampleMath.Vec4FTransform(pointOnFarPlane,
				inverseModelViewMatrix);

		Vec3F lineStart = new Vec3F(nearWorld.getData()[0],
				nearWorld.getData()[1], nearWorld.getData()[2]);
		Vec3F lineEnd = new Vec3F(farWorld.getData()[0], farWorld.getData()[1],
				farWorld.getData()[2]);
		Vec3F intersection = linePlaneIntersection(lineStart, lineEnd,
				planeCenter, planeNormal);
		return intersection;
	}

	private Vec3F linePlaneIntersection(Vec3F lineStart, Vec3F lineEnd,
			Vec3F pointOnPlane, Vec3F planeNormal) {
		Vec3F intersection = new Vec3F();
		Vec3F lineDir = SampleMath.Vec3FSub(lineEnd, lineStart);
		lineDir = SampleMath.Vec3FNormalize(lineDir);

		Vec3F planeDir = SampleMath.Vec3FSub(pointOnPlane, lineStart);

		float n = SampleMath.Vec3FDot(planeNormal, planeDir);
		float d = SampleMath.Vec3FDot(planeNormal, lineDir);

		if (Math.floor(d) < 0.00001) {
			// Line is parallel to plane
			return null;
		}
		float dist = n / d;
		Vec3F offset = SampleMath.Vec3FScale(lineDir, dist);
		intersection = SampleMath.Vec3FAdd(lineStart, offset);
		return intersection;
	}


}
