package io.sarl.wrapper.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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

	@Override
	public void showErrorDialog(String message)
	{
		JOptionPane.showMessageDialog(parent,  message, "Error", JOptionPane.ERROR_MESSAGE);
	}

}
