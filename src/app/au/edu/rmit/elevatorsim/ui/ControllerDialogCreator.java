package au.edu.rmit.elevatorsim.ui;

/**
 * Interface meant to enable model classes to create dialogs
 * without writing ui code
 * 
 * @author Joshua Richards
 */
public interface ControllerDialogCreator
{
	/**
	 * Display an error dialog with the given message.
	 * Will not return until user closes the dialog
	 * @param message
	 */
	public void showErrorDialog(String message);
}
