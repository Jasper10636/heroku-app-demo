package io.intelligence.ppmTool.services;

import io.intelligence.ppmTool.domain.Backlog;
import io.intelligence.ppmTool.domain.Project;
import io.intelligence.ppmTool.domain.User;
import io.intelligence.ppmTool.exceptions.ProjectIdException;
import io.intelligence.ppmTool.exceptions.ProjectNotFoundException;
import io.intelligence.ppmTool.repositories.BacklogRepository;
import io.intelligence.ppmTool.repositories.ProjectRepository;
import io.intelligence.ppmTool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private UserRepository userRepository;

    public Project saveOrUpdateProject(Project project, String username){
        //Logic

        //project.getId != null
        //find by db id -> null

        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier());

            if(existingProject != null && (!existingProject.getProjectLeader().equals(username))){
                throw new ProjectNotFoundException("Project not found in your account ");
            }else if(existingProject == null){
                throw new ProjectNotFoundException("Project with ID: '" + project.getProjectIdentifier()+"' cannot be updated because it doesn't exist");
            }
        }

        try{
            User user = userRepository.findByUsername(username);
            project.setUser(user);
            project.setProjectLeader(user.getUsername());

            String toIDUpper = project.getProjectIdentifier().toUpperCase();
            project.setProjectIdentifier(toIDUpper);

            if(project.getId() == null){
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(toIDUpper);
            }

            if(project.getId() != null){
                project.setBacklog(backlogRepository.findByProjectIdentifier(toIDUpper));
            }


            return projectRepository.save(project);
        }catch (Exception e){
            throw new ProjectIdException("Project ID : '" + project.getProjectIdentifier().toUpperCase()+"' already exists");
        }
    }

    public Project findProjectByIdentifier(String projectId, String username){
        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());

        if(project == null){
            throw new ProjectIdException("Project ID : '" + projectId+"' does not exists");
        }

        if(!project.getProjectLeader().equals(username)){
            throw new ProjectNotFoundException("Project not found in your account");
        }

        return project;
    }

    public Iterable<Project> findAllProject(String username){
//        return projectRepository.findAll();
        return projectRepository.findAllByProjectLeader(username);
    }

    public void deleteProjectByIdentifier(String projectId, String username){
//        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());
//
//        if(project == null){
//            throw new ProjectIdException("Cannot delete Project with ID : '" + projectId+"'. This project does not exists");
//        }
//        projectRepository.delete(project);
        projectRepository.delete(findProjectByIdentifier(projectId,username));
    }


}
