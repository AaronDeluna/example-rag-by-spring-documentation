package org.mirent.skills.exeptions;

public class NotFoundSaveModelNameException extends RuntimeException {

    public NotFoundSaveModelNameException() {
        super("Нет сохранённого предыдущего имени модели. Сначала вызовите updateModelNameAndSave.");
    }
}
