package net.novaclient.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Thin wrapper around {@link MultiBufferSource.BufferSource} that lets Nova
 * queue several batches and flush them in one go.
 */
public final class BufferSource
{
	private final MultiBufferSource.BufferSource delegate =
		MultiBufferSource
			.immediate(new ByteBufferBuilder(RenderType.BIG_BUFFER_SIZE));

	public VertexConsumer getBuffer(RenderType renderType)
	{
		return delegate.getBuffer(renderType);
	}

	/** Uploads and draws everything that was queued. */
	public void uploadAndDraw()
	{
		delegate.endBatch();
	}
}
