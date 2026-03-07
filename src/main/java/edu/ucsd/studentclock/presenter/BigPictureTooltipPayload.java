package edu.ucsd.studentclock.presenter;

import java.util.List;

public final class BigPictureTooltipPayload {

    private final List<BigPictureTooltipItem> items;

    public BigPictureTooltipPayload(List<BigPictureTooltipItem> items) {
        this.items = items;
    }

    public List<BigPictureTooltipItem> getItems() {
        return items;
    }
}