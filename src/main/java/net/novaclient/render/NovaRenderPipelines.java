package net.novaclient.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Render pipelines used by Nova Client to draw ESP boxes, tracers and other
 * world overlays.
 *
 * <p>MC 1.21.11 already has the pipeline/snippet based renderer that the 26.2
 * branch uses, but the builder API is slightly different: depth state is set
 * with {@code withDepthTestFunction} instead of {@code withDepthStencilState},
 * and the vertex format carries the primitive topology.
 */
public enum NovaRenderPipelines
{
	;

	/** Similar to the LINES snippet, but without fog. */
	public static final Snippet FOGLESS_LINES_SNIPPET = RenderPipeline
		.builder(RenderPipelines.LINES_SNIPPET)
		.withVertexShader(Identifier.parse("nova:core/fogless_lines"))
		.withFragmentShader(Identifier.parse("nova:core/fogless_lines"))
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH,
			VertexFormat.Mode.LINES)
		.buildSnippet();

	public static final RenderPipeline DEPTH_TEST_LINES =
		RenderPipelines.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(Identifier.parse("nova:pipeline/nova_lines"))
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).build());

	public static final RenderPipeline ESP_LINES = RenderPipelines
		.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(Identifier.parse("nova:pipeline/nova_esp_lines"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());

	public static final RenderPipeline QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.parse("nova:pipeline/nova_quads"))
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withCull(true).build());

	public static final RenderPipeline ESP_QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.parse("nova:pipeline/nova_esp_quads"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(true).build());
}
