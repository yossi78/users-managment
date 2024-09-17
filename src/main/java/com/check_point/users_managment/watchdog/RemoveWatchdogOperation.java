package com.check_point.users_managment.watchdog;


import com.check_point.users_managment.entity.User;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonSerialize
@AllArgsConstructor
public class RemoveWatchdogOperation implements WatchdogOperation{

    private User user;

    @Override
    public OperationType operationType() {
        return OperationType.DELETE;
    }

    @Override
    public User user() {
        return this.user;
    }

}
