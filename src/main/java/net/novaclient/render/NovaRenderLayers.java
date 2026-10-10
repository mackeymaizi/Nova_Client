package net.novaclient.render;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public enum NovaRenderLayers
{
	;

	public static final RenderType LINES = RenderType.create("nova:lines",
		RenderSetup.builder(NovaRenderPipelines.DEPTH_TEST_LINES)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup());

	public static final RenderType ESP_LINES =
		RenderType.create("nova:esp_lines",
			RenderSetup.builder(NovaRenderPipelines.ESP_LINES)
				.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
				.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
				.createRenderSetup());

	public static final RenderType QUADS = RenderType.create("nova:quads",
		RenderSetup.builder(NovaRenderPipelines.QUADS).sortOnUpload()
			.createRenderSetup());

	public static final RenderType ESP_QUADS =
		RenderType.create("nova:esp_quads", RenderSetup
			.builder(NovaRenderPipelines.ESP_QUADS).sortOnUpload()
			.createRenderSetup());

	public static RenderType getQuads(boolean depthTest)
	{
		return depthTest ? QUADS : ESP_QUADS;
	}

	public static RenderType getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
}
