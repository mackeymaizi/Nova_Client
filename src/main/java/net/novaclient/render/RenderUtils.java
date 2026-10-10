package net.novaclient.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.novaclient.NovaClient;
import net.novaclient.util.EntityUtils;

public enum RenderUtils
{
	;

	public record ColoredBox(AABB box, int color)
	{}

	public static Vec3 getCameraPos()
	{
		Camera camera = NovaClient.MC.gameRenderer.getMainCamera();
		if(camera == null)
			return Vec3.ZERO;

		return camera.position();
	}

	public static void applyRenderOffset(PoseStack matrices)
	{
		Vec3 camPos = getCameraPos();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
	}

	public static int toIntColor(float[] rgb, float opacity)
	{
		return (int)(Mth.clamp(opacity, 0, 1) * 255) << 24
			| (int)(Mth.clamp(rgb[0], 0, 1) * 255) << 16
			| (int)(Mth.clamp(rgb[1], 0, 1) * 255) << 8
			| (int)(Mth.clamp(rgb[2], 0, 1) * 255);
	}

	public static void drawLine(PoseStack matrices, Vec3 start, Vec3 end,
		int color, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		drawLine(matrices, buffer, start.add(offset), end.add(offset), color);

		bs.uploadAndDraw();
	}

	public static void drawLine(PoseStack matrices, VertexConsumer buffer,
		Vec3 start, Vec3 end, int color)
	{
		Pose entry = matrices.last();
		float x1 = (float)start.x;
		float y1 = (float)start.y;
		float z1 = (float)start.z;
		float x2 = (float)end.x;
		float y2 = (float)end.y;
		float z2 = (float)end.z;

		Vector3f normal = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, normal).setLineWidth(2);

		// If the line goes through the screen, add another vertex there.
		// Works around a bug in Minecraft's line shader.
		float t = new Vector3f(x1, y1, z1).negate().dot(normal);
		float length = new Vector3f(x2, y2, z2).sub(x1, y1, z1).length();
		if(t > 0 && t < length)
		{
			Vector3f closeToCam = new Vector3f(normal).mul(t).add(x1, y1, z1);
			buffer.addVertex(entry, closeToCam).setColor(color)
				.setNormal(entry, normal).setLineWidth(2);
			buffer.addVertex(entry, closeToCam).setColor(color)
				.setNormal(entry, normal).setLineWidth(2);
		}

		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, normal).setLineWidth(2);
	}

	public static void drawTracers(PoseStack matrices, float partialTicks,
		List<Vec3> ends, int color, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(Vec3 end : ends)
			drawLine(matrices, buffer, offset, end.add(offset), color);

		bs.uploadAndDraw();
	}

	public static void drawTracers(PoseStack matrices, float partialTicks,
		List<Vec3> ends, List<Integer> colors, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(int i = 0; i < ends.size(); i++)
			drawLine(matrices, buffer, offset, ends.get(i).add(offset),
				colors.get(i));

		bs.uploadAndDraw();
	}

	public static void drawOutlinedBoxes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawOutlinedBox(matrices, buffer, box.move(offset), color);

		bs.uploadAndDraw();
	}

	public static void drawOutlinedBoxes(PoseStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(ColoredBox box : boxes)
			drawOutlinedBox(matrices, buffer, box.box().move(offset),
				box.color());

		bs.uploadAndDraw();
	}

	public static void drawOutlinedBox(PoseStack matrices,
		VertexConsumer buffer, AABB box, int color)
	{
		Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;

		// bottom
		line(buffer, entry, x1, y1, z1, x2, y1, z1, 1, 0, 0, color);
		line(buffer, entry, x1, y1, z1, x1, y1, z2, 0, 0, 1, color);
		line(buffer, entry, x2, y1, z1, x2, y1, z2, 0, 0, 1, color);
		line(buffer, entry, x1, y1, z2, x2, y1, z2, 1, 0, 0, color);

		// top
		line(buffer, entry, x1, y2, z1, x2, y2, z1, 1, 0, 0, color);
		line(buffer, entry, x1, y2, z1, x1, y2, z2, 0, 0, 1, color);
		line(buffer, entry, x2, y2, z1, x2, y2, z2, 0, 0, 1, color);
		line(buffer, entry, x1, y2, z2, x2, y2, z2, 1, 0, 0, color);

		// sides
		line(buffer, entry, x1, y1, z1, x1, y2, z1, 0, 1, 0, color);
		line(buffer, entry, x2, y1, z1, x2, y2, z1, 0, 1, 0, color);
		line(buffer, entry, x1, y1, z2, x1, y2, z2, 0, 1, 0, color);
		line(buffer, entry, x2, y1, z2, x2, y2, z2, 0, 1, 0, color);
	}

	private static void line(VertexConsumer buffer, Pose entry, float x1,
		float y1, float z1, float x2, float y2, float z2, float nx, float ny,
		float nz, int color)
	{
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, nx, ny, nz).setLineWidth(2);
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, nx, ny, nz).setLineWidth(2);
	}

	public static void drawOutlinedBox(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		drawOutlinedBox(matrices, buffer, box.move(getCameraPos().reverse()),
			color);

		bs.uploadAndDraw();
	}

	public static void drawCurvedLine(PoseStack matrices, List<Vec3> points,
		int color, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getLines(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(int i = 0; i < points.size() - 1; i++)
			drawLine(matrices, buffer, points.get(i).add(offset),
				points.get(i + 1).add(offset), color);

		bs.uploadAndDraw();
	}

	public static void drawSolidBox(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getQuads(depthTest));

		drawSolidBox(matrices, buffer, box.move(getCameraPos().reverse()),
			color);

		bs.uploadAndDraw();
	}

	public static void drawSolidBoxes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		BufferSource bs = new BufferSource();
		VertexConsumer buffer = bs.getBuffer(NovaRenderLayers.getQuads(depthTest));

		Vec3 offset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawSolidBox(matrices, buffer, box.move(offset), color);

		bs.uploadAndDraw();
	}

	public static void drawSolidBox(PoseStack matrices, VertexConsumer buffer,
		AABB box, int color)
	{
		Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;

		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		float a = (color >> 24 & 255) / 255F;

		buffer.addVertex(entry, x1, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y1, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y1, z2).setColor(r, g, b, a);

		buffer.addVertex(entry, x1, y2, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y2, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z1).setColor(r, g, b, a);

		buffer.addVertex(entry, x1, y1, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y1, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y2, z2).setColor(r, g, b, a);

		buffer.addVertex(entry, x2, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y2, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z1).setColor(r, g, b, a);

		buffer.addVertex(entry, x1, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y1, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y2, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x1, y2, z1).setColor(r, g, b, a);

		buffer.addVertex(entry, x2, y1, z2).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y1, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z1).setColor(r, g, b, a);
		buffer.addVertex(entry, x2, y2, z2).setColor(r, g, b, a);
	}

	public static List<AABB> toBoxes(List<? extends net.minecraft.world.entity.Entity> entities,
		float partialTicks, double extraSize)
	{
		List<AABB> boxes = new ArrayList<>(entities.size());
		for(net.minecraft.world.entity.Entity e : entities)
			boxes.add(EntityUtils.getLerpedBox(e, partialTicks).move(0,
				extraSize, 0).inflate(extraSize));

		return boxes;
	}
}
