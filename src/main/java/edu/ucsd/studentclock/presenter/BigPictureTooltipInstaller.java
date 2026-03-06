package edu.ucsd.studentclock.presenter;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

final class BigPictureTooltipInstaller {

    private final BigPictureTooltipTextFormatter formatter = new BigPictureTooltipTextFormatter();

    void installTooltips(XYChart.Series<String, Number> workloadSeries) {
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> point : workloadSeries.getData()) {
                if (point.getNode() == null) {
                    continue;
                }

                Object extra = point.getExtraValue();
                if (!(extra instanceof BigPictureTooltipPayload)) {
                    continue;
                }

                BigPictureTooltipPayload payload = (BigPictureTooltipPayload) extra;

                String text = formatter.format(payload);
                if (text == null || text.trim().isEmpty()) {
                    continue;
                }

                Tooltip tooltip = new Tooltip(text);
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.seconds(60));

                point.getNode().setPickOnBounds(true);
                point.getNode().setMouseTransparent(false);
                point.getNode().setStyle("-fx-cursor: hand;");

                Tooltip.install(point.getNode(), tooltip);
            }
        });
    }
}