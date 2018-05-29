package au.edu.rmit.agtgrp.elevatorsim.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class ControllerCliMessageView implements ControllerDialogCreator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    @Override
    public void showErrorDialog(String message) {
        LOG.error(message);
    }
}
