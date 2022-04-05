package io.intelligence.ppmTool.services;

import io.intelligence.ppmTool.domain.Backlog;
import io.intelligence.ppmTool.domain.ProjectTask;
import io.intelligence.ppmTool.exceptions.ProjectNotFoundException;
import io.intelligence.ppmTool.repositories.BacklogRepository;
import io.intelligence.ppmTool.repositories.ProjectRepository;
import io.intelligence.ppmTool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectTaskService {

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){
//        try{
            //PTS to be added to a specific project, project != null, BL exists
//            Backlog backlog = backlogRepository.findByProjectIdentifier(projectIdentifier);
            Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier,username).getBacklog();
            //set backlog to project task
            projectTask.setBacklog(backlog);
            //we want our project sequence to be like this: IDPRO-1 IDPRO-2
            Integer BacklogSequence = backlog.getPTSequence();
            //Update the BL SEQUENCE
            BacklogSequence++;
            backlog.setPTSequence(BacklogSequence);

            //Add Sequence to projectTask
            projectTask.setProjectSequence(backlog.getProjectIdentifier()+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);

            //INITIAL priority when priority null
            if(projectTask.getPriority() == null || projectTask.getPriority() == 0){
                // In the future we need projectTask.getPriority() == 0 to handle the form
                projectTask.setPriority(3);
            }
            //INITIAL status when status is null
            if(projectTask.getStatus() ==""|| projectTask.getStatus() == null){
                projectTask.setStatus("TO_DO");
            }

            return projectTaskRepository.save(projectTask);

//        }catch(Exception e){
//            // Exceptions: Project Not Found
//            // {Project Not Found}
//            throw new ProjectNotFoundException("Project Not Found");
//        }

    }

    public Iterable<ProjectTask> findBacklogById(String id, String username){
//        Project project = projectRepository.findByProjectIdentifier(id);
//
//        if(project == null){
//            throw new ProjectNotFoundException("Project with ID: '"+id+"' does not exist!");
//        }
        projectService.findProjectByIdentifier(id,username);
        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username) {

        //make sure we are searching on an existing backlog
//        Backlog backlog = backlogRepository.findByProjectIdentifier((backlog_id));
//        if(backlog == null){
//            throw new ProjectNotFoundException("Project with ID: '"+backlog_id+"' does not exist!");
//        }
        projectService.findProjectByIdentifier(backlog_id,username);

        //make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask == null){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' not found!");
        }

        //make sure that the bakjlog/project id in the path corresponds to the right project
        if(!projectTask.getBacklog().getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' does not exist in project: "+backlog_id);
        }

//        return projectTaskRepository.findByProjectSequence(pt_id);
        return projectTask;
    }


    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id,pt_id,username);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id,String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id,pt_id,username);
        projectTaskRepository.delete(projectTask);
    }
}
