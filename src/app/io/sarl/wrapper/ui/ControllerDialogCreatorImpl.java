package io.sarl.wrapper.ui;

import javax.swing.JFrame;

public class ControllerDialogCreatorImpl implements ControllerDialogCreator
{
	private JFrame parent;
	
	public ControllerDialogCreatorImpl(JFrame parent)
	{
		this.parent = parent;
	}

	@Override
	public ControllerDialog createLongCancellableOperationDialog(String title, String message, Runnable onCancel)
	{
		return new LongCancellableOperationDialog(parent, title, message, onCancel);
	}

}
