package io.sarl.wrapper.ui;

import java.util.Map;

public interface ControllerDialogCreator
{
	public ControllerDialog createDialog(String message, Map<String, Runnable> options);
}
