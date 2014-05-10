/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.SampleApplication.utils;

public class CubeShaders {

	public static final String CUBE_MESH_VERTEX_SHADER = " \n" + "\n"
			+ "attribute vec4 vertexPosition; \n"
			+ "attribute vec4 vertexNormal; \n"
			+ "attribute vec2 vertexTexCoord; \n" + "\n"
			+ "varying vec2 texCoord; \n" + "varying vec4 normal; \n" + "\n"
			+ "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
			+ "void main() \n" + "{ \n"
			+ "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
			+ "   normal = vertexNormal; \n"
			+ "   texCoord = vertexTexCoord; \n" + "} \n";

	public static final String CUBE_MESH_FRAGMENT_SHADER = " \n" + "\n"
			+ "precision mediump float; \n" + " \n"
			+ "varying vec2 texCoord; \n" + "varying vec4 normal; \n" + " \n"
			+ "uniform sampler2D texSampler2D; \n" + " \n" + "void main() \n"
			+ "{ \n"
			+ "   gl_FragColor = texture2D(texSampler2D, texCoord); \n"+
			" gl_FragColor.a *= 1.0f;"
			+ "} \n";
	//
	// public static final String CUBE_MESH_VERTEX_SHADER = " \n"
	// + "attribute vec4 vertexPosition; \n"
	// + "attribute vec4 vertexNormal; \n"
	// + "attribute vec2 vertexTexCoord; \n" + "varying vec2 texCoord; \n"
	// + "varying vec4 normal; \n" + "uniform mat4 modelViewMatrix; \n"
	// + "uniform mat4 modelViewProjectionMatrix; \n"
	// + "void main()  { \n"
	// + "gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
	// + "normal = modelViewMatrix * vec4(vertexNormal.xyz, 0.0); \n"
	// + "normal = normalize(normal); \n"
	// + "texCoord = vertexTexCoord; \n" + "}";
	//
	// public static final String CUBE_MESH_FRAGMENT_SHADER = " \n"
	// + "precision mediump float; \n"
	// + "varying vec2 texCoord; \n"
	// + "varying vec4 normal; \n"
	// + "uniform vec3 diffuseMaterial; \n"
	// + "uniform sampler2D texSampler2D; \n"
	// + "void main()  { \n"
	// + "vec3 n = normalize(normal.xyz); \n"
	// + "vec3 lightDir = vec3(0.0, 0.0, -1.0); \n"
	// + "vec3 ambient = vec3(0.2, 0.2, 0.2); \n"
	// + "vec3 diffuseLight = vec3(0.9, 0.9, 0.9); \n"
	// + "float diffuseFactor = max(0.0, dot(n, lightDir)); \n"
	// + "vec3 diffuse = diffuseFactor * diffuseMaterial * diffuseLight; \n"
	// + "vec3 shadedColor = ambient + diffuse; \n"
	// +
	// "gl_FragColor = vec4(shadedColor, 0.4) * texture2D(texSampler2D, texCoord); \n"
	// + "}";

}
