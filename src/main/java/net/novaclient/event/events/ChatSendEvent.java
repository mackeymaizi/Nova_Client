package net.novaclient.event.events;

import net.novaclient.event.Event;

public final class ChatSendEvent extends Event
{
	private String message;

	public ChatSendEvent(String message)
	{
		this.message = message;
	}

	public String message()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
