package it.thisone.iotter.ui.visualizers.controlpanel;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.thisone.iotter.ui.uitask.UIRunnable;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

public class QuickCommandUIRunnable implements UIRunnable {
    public static Logger logger = LoggerFactory.getLogger(QuickCommandUIRunnable.class);

    private static final int LOOP_COUNT = 6;
    private static final int LOOP_PAUSE = 5;
    private final QuickCommandButton button;
    private String errorMessage;

    public QuickCommandUIRunnable(QuickCommandButton button) {
        this.button = button;
    }

    @Override
    public void runInBackground() {
        BigDecimal value = new BigDecimal(button.getNextValue());
        if (button.getTopic() != null && !button.getTopic().isEmpty()) {
            try {
                if (button.getMqttOutboundService() == null) {
                    errorMessage = getI18nLabel("quick_command_not_issued");
                    return;
                }
                button.getCallback().beforeCommand();
                button.getMqttOutboundService().setValue(button.getTopic(), value);
                int i = 0;
                boolean done = false;
                while (i <= LOOP_COUNT) {
                    i++;
                    try {
                        Thread.sleep(LOOP_PAUSE * 1000L);
                    } catch (InterruptedException ignored) {
                    }
                    if (button.getCallback().checkResult(value.floatValue())) {
                        done = true;
                        button.getCallback().onSuccess();
                        break;
                    }
                }
                if (!done) {
                    errorMessage = getI18nLabel("quick_command_accepted_but_unsuccessfull");
                    button.getCallback().onError();
                }
            } catch (Exception e) {
                errorMessage = getI18nLabel("quick_command_not_accepted");
                logger.error(errorMessage, e);
            }
        } else {
            errorMessage = getI18nLabel("quick_command_not_issued");
        }
    }

    @Override
    public void runInUI(Throwable ex) {
        button.setEnabled(true);
        if (errorMessage != null) {
            PopupNotification.show(errorMessage, Type.ERROR);
            button.showIcon();
        } else {
            button.setEnabled(!button.isSingleState());
            int value = button.getNextValue();
            button.setIconValue(value);
            PopupNotification.show(getI18nLabel("quick_command_successfull"), Type.HUMANIZED);
        }
    }

    public String getI18nLabel(String key) {
        return button.getTranslation("mqtt.setvalue" + "." + key);
    }
}
