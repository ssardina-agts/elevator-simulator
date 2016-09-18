package io.sarl.wrapper.ui;

public interface ControllerDialogCreator
{
	public ControllerDialog createLongCancellableOperationDialog(
			String title, String message, Runnable onCancel
	);
}
