package com.promisetracker.promise.dto;

import com.promisetracker.audit.PromiseActivity;
import com.promisetracker.audit.PromiseActivityAction;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PromiseActivityResponse {

    private UUID id;
    private UUID promiseId;
    private UUID userId;
    private String userName;
    private PromiseActivityAction action;
    private String oldStatus;
    private String newStatus;
    private Instant createdAt;

    public static PromiseActivityResponse fromEntity(PromiseActivity activity) {
        return PromiseActivityResponse.builder()
                .id(activity.getId())
                .promiseId(activity.getPromise().getId())
                .userId(activity.getUser().getId())
                .userName(activity.getUser().getFirstName() + " " + activity.getUser().getLastName())
                .action(activity.getAction())
                .oldStatus(activity.getOldStatus())
                .newStatus(activity.getNewStatus())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
