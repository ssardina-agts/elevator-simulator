package au.edu.rmit.agtgrp.elevatorsim.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerCliMessageView implements ControllerDialogCreator {
    private static final Logger LOG = LoggerFactory.getLogger(ControllerCliMessageView.class.getSimpleName());
    @Override
    public void showErrorDialog(String message) {
        LOG.error(message);
    }
}
