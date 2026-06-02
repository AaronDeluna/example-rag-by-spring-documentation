package ru.mirent.skills.exeptions;

public class InvalidSkillNameException extends RuntimeException {

    public InvalidSkillNameException(String message) {
        super(message);
    }
}
