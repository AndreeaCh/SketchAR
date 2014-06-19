package com.qualcomm.vuforia.samples.SampleApplication.utils;

import java.util.ArrayList;
import java.util.Vector;

import android.widget.TabHost;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.UserDefinedTargets.UserDefinedTargetRenderer;

public class Scene {
	private ArrayList<Line> lines = new ArrayList<Line>();
	private UserDefinedTargetRenderer renderer;
	private float z = 200f;

	public Scene(UserDefinedTargetRenderer renderer) {
		this.renderer = renderer;
		buildCubeSketch(renderer);
	}

	private void buildCubeSketch(UserDefinedTargetRenderer renderer) {
		Line eastHorz = new Line(renderer);
		eastHorz.SetVerts(80f, 80f, z, 80f, -80f, z);
		// eastHorz.SetColor(.8f, .8f, 0f, 1.0f);
		Line northHorz = new Line(renderer);
		northHorz.SetVerts(-80f, 80f, z, 80f, 80f, z);
		// northHorz.SetColor(0.8f, 0.8f, 0f, 1.0f);
		Line westHorz = new Line(renderer);
		westHorz.SetVerts(-80f, -80f, z, -80f, 80f, z);
		// westHorz.SetColor(0.8f, 0.8f, 0f, 1.0f);
		Line southHorz = new Line(renderer);
		southHorz.SetVerts(-80f, -80f, z, 80f, -80f, z);
		// southHorz.SetColor(0.8f, 0.8f, 0f, 1.0f);

		Line eastHorz2 = new Line(renderer);
		eastHorz2.SetVerts(100f, 100f, z, 100f, -60f, z);
		// eastHorz2.SetColor(.8f, .8f, 0f, 1.0f);
		Line northHorz2 = new Line(renderer);
		northHorz2.SetVerts(-60f, 100f, z, 100f, 100f, z);
		// northHorz2.SetColor(0.8f, 0.8f,0f , 1.0f);
		Line westHorz2 = new Line(renderer);
		westHorz2.SetVerts(-60f, -60f, z, -60f, 100f, z);
		// westHorz2.SetColor(0.8f, 0.8f, 0f, 1.0f);
		Line southHorz2 = new Line(renderer);
		southHorz2.SetVerts(-60f, -60f, z, 100f, -60f, z);
		// southHorz2.SetColor(0.8f, 0.8f, 0f, 1.0f);

		Line eastHorz3 = new Line(renderer);
		eastHorz3.SetVerts(80f, 80f, z, 100f, 100f, z);
		// eastHorz3.SetColor(.8f, .8f, 0f, 1.0f);
		Line northHorz3 = new Line(renderer);
		northHorz3.SetVerts(-80f, 80f, z, -60f, 100f, z);
		// northHorz3.SetColor(0.8f, 0.8f, 0f, 1.0f);
		Line westHorz3 = new Line(renderer);
		westHorz3.SetVerts(-80f, -80f, z, -60f, -60f, z);
		// westHorz3.SetColor(0.8f, 0.8f, 0f, 1.0f);
		Line southHorz3 = new Line(renderer);
		southHorz3.SetVerts(80f, -80f, z, 100f, -60f, z);
		// southHorz3.SetColor(0.8f, 0.8f, 0f, 1.0f);

		lines.add(eastHorz);
		lines.add(northHorz);
		lines.add(westHorz);
		lines.add(southHorz);

		lines.add(eastHorz2);
		lines.add(northHorz2);
		lines.add(westHorz2);
		lines.add(southHorz2);

		lines.add(eastHorz3);
		lines.add(northHorz3);
		lines.add(westHorz3);
		lines.add(southHorz3);
	}

	public void draw(float[] modelViewProjection, int shaderProgramID,
			int mvpMatrixHandle, Vector<Texture> textures) {
		for (Line l : lines) {
			l.draw(modelViewProjection, shaderProgramID, mvpMatrixHandle,
					textures);
		}
	}

	private static final float THRESHOLD = 30f;

	public void setSelectedPoints(float[] markerLocation) {
		float minDistance = getMinDistance(markerLocation);
		for (Line l : lines) {
			float distance1 = SampleUtils.calculateDistance(markerLocation,
					l.getLineEnd1());
			float distance2 = SampleUtils.calculateDistance(markerLocation,
					l.getLineEnd2());
			if ((distance1 <= minDistance) && (distance2 <= minDistance)) {
				if (distance1 < distance2) {
					l.setEnd1Selected(true);
				} else {
					l.setEnd2Selected(true);
				}
			}
			if (distance1 <= minDistance) {
				l.setEnd1Selected(true);
			}
			if (distance2 <= minDistance) {
				l.setEnd2Selected(true);
			}
		}

	}

	private float getMinDistance(float[] markerLocation) {
		float minDistance = THRESHOLD;
		for (Line l : lines) {
			float distance1 = SampleUtils.calculateDistance(markerLocation,
					l.getLineEnd1());
			if (distance1 < minDistance) {
				minDistance = distance1;
			}
			float distance2 = SampleUtils.calculateDistance(markerLocation,
					l.getLineEnd2());
			if (distance2 < minDistance) {
				minDistance = distance1;
			}
		}
		return minDistance;
	}

	public void translateSelected(float[] newPosition) {
		for (Line l : lines) {
			if (l.isEnd1Selected()) {
				l.setLineEnd1(newPosition);
			}
			if (l.isEnd2Selected()) {
				l.setLineEnd2(newPosition);
			}
		}
	}
}
