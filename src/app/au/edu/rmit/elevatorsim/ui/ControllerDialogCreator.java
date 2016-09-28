package au.edu.rmit.elevatorsim.ui;

public interface ControllerDialogCreator
{
	public ControllerDialog createLongCancellableOperationDialog(
			String title, String message, Runnable onCancel
	);
	
	public void showErrorDialog(String message);
}
