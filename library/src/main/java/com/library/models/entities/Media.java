package com.library.models.entities;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Media {

    private String mediaUrl;

    private int position;

    public Media() {
    }

    public Media(String mediaUrl, int position) {
        this.mediaUrl = mediaUrl;
        this.position = position;
    }
}
