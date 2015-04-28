package com.playonlinux.api;

import com.playonlinux.utils.messages.CancelerSynchroneousMessage;
import com.playonlinux.utils.messages.InterrupterAsynchroneousMessage;
import com.playonlinux.utils.messages.InterrupterSynchroneousMessage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

public interface SetupWindow {
    void setTopImage(File topImage) throws MalformedURLException;

    void setLeftImage(File leftImage) throws MalformedURLException;

    void showSimpleMessageStep(CancelerSynchroneousMessage message, String textToShow);

    void showYesNoQuestionStep();

    void showTextBoxStep(CancelerSynchroneousMessage message, String textToShow, String defaultValue);

    void showMenuStep(CancelerSynchroneousMessage message, String textToShow, List<String> menuItems);

    void showSpinnerStep(InterrupterAsynchroneousMessage message, String textToShow);

    ProgressStep showProgressBar(InterrupterSynchroneousMessage message, String textToShow);

    void showPresentationStep(CancelerSynchroneousMessage message, String textToShow);

    void close();
}
