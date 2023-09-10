package isotropy.lmf.core.notification.impl;

import org.eclipse.emf.common.notify.Notification;

public class IntNotification implements Notification
{
	private final Object notifier;
	private final Enum<?> feature;
	private final int oldValue;
	private final int newValue;

	public IntNotification(Object Notifier, Enum<?> feature, int oldValue, int newValue)
	{
		notifier = Notifier;
		this.feature = feature;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public Object getNotifier()
	{
		return notifier;
	}

	@Override
	public int getEventType()
	{
		return Notification.SET;
	}

	@Override
	public int getFeatureID(Class<?> expectedClass)
	{
		return feature.ordinal();
	}

	@Override
	public Object getFeature()
	{
		return feature;
	}

	@Override
	public Object getOldValue()
	{
		return oldValue;
	}

	@Override
	public Object getNewValue()
	{
		return newValue;
	}

	@Override
	public boolean wasSet()
	{
		return false;
	}

	@Override
	public boolean isTouch()
	{
		return false;
	}

	@Override
	public boolean isReset()
	{
		return false;
	}

	@Override
	public int getPosition()
	{
		return 0;
	}

	@Override
	public boolean merge(Notification notification)
	{
		return false;
	}

	@Override
	public boolean getOldBooleanValue()
	{
		return false;
	}

	@Override
	public boolean getNewBooleanValue()
	{
		return false;
	}

	@Override
	public byte getOldByteValue()
	{
		return 0;
	}

	@Override
	public byte getNewByteValue()
	{
		return 0;
	}

	@Override
	public char getOldCharValue()
	{
		return 0;
	}

	@Override
	public char getNewCharValue()
	{
		return 0;
	}

	@Override
	public double getOldDoubleValue()
	{
		return 0;
	}

	@Override
	public double getNewDoubleValue()
	{
		return 0;
	}

	@Override
	public float getOldFloatValue()
	{
		return 0;
	}

	@Override
	public float getNewFloatValue()
	{
		return 0;
	}

	@Override
	public int getOldIntValue()
	{
		return oldValue;
	}

	@Override
	public int getNewIntValue()
	{
		return newValue;
	}

	@Override
	public long getOldLongValue()
	{
		return 0;
	}

	@Override
	public long getNewLongValue()
	{
		return 0;
	}

	@Override
	public short getOldShortValue()
	{
		return 0;
	}

	@Override
	public short getNewShortValue()
	{
		return 0;
	}

	@Override
	public String getOldStringValue()
	{
		return null;
	}

	@Override
	public String getNewStringValue()
	{
		return null;
	}

}
