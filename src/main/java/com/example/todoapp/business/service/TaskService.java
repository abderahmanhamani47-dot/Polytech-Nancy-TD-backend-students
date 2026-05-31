package com.example.todoapp.business.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.persitence.TaskDao;
import com.example.todoapp.dto.Outputdto;
import com.example.todoapp.dto.POSTdto;
import com.example.todoapp.dto.Outputdto;
import com.example.todoapp.dto.POSTdto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class TaskService {

    // accés au DAO pour manipuler les taches
    private final TaskDao taskDao = new TaskDao();

    // creer une nouvelle tache
    public Outputdto createTask(POSTdto input) {
        Task task = this.taskDao.save(input);

        // transformation vers DTO
        return new Outputdto(task.id(), task.title(), task.description(), task.done());
    }

    // recuperer une tache avec son id
    public Optional<Outputdto> getTaskById(int id) {
        Optional<Task> task = this.taskDao.findById(id);

        if (task.isEmpty()) {
            return Optional.empty(); // si la tache n'existe pas
        }

        Outputdto output = new Outputdto(
                task.get().id(),
                task.get().title(),
                task.get().description(),
                task.get().done()
        );

        return Optional.of(output);
    }

    // supprimer une tache par id
    public void deleteTaskById(int id) {
        this.taskDao.deleteTaskById(id);
    }

    // modifier une tache
    public void updateTaskById(int id, String newTitle, String newDesc, boolean newDone) {
        this.taskDao.updateTaskById(id, newTitle, newDesc, newDone);
    }

    // recuperer toutes les taches
    public Collection<Outputdto> getTasksList(boolean todoOnly) {
        Collection<Task> tasksList = this.taskDao.getTasksList(todoOnly);
        Collection<Outputdto> output = new ArrayList<>();

        // conversion des tasks vers DTO
        for (Task task : tasksList) {
            output.add(new Outputdto(
                    task.id(),
                    task.title(),
                    task.description(),
                    task.done()
            ));
        }

        return output;
    }
}