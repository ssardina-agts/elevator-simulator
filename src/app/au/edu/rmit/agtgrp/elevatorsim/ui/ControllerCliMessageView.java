package au.edu.rmit.agtgrp.elevatorsim.ui;

public class ControllerCliMessageView implements ControllerDialogCreator {
    @Override
    public void showErrorDialog(String message) {
        System.err.println(message);
    }
}
