package main.controllers;

import main.model.dto.UserDto;

public abstract class BaseController {
    protected UserDto baseUser;

    BaseController(UserDto user){
        baseUser = user;
    }
}
