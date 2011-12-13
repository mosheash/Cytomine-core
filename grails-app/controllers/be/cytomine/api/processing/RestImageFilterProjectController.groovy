package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter
import be.cytomine.project.Project

class RestImageFilterProjectController extends RestController {

    def imageFilterService
    def imageFilterProjectService
    def projectService


    def list = {
		def project = Project.read(params.project)
		if (!project) responseNotFound("Project", "Project", params.project)
        def imagesFiltersProject = imageFilterProjectService.list(project)
 		responseSuccess(imagesFiltersProject.collect { it.imageFilter })        
    }

    def add = {
		println request.JSON
        Project project = projectService.read(request.JSON.project)
		if (!project) responseNotFound("Project", "Project", request.JSON.project)
        ImageFilter imageFilter = imageFilterService.read(request.JSON.imageFilter)
		if (!imageFilter) responseNotFound("ImageFilter", "ImageFilter", request.JSON.imageFilter)
        imageFilterProjectService.add(project, imageFilter)
        responseSuccess(imageFilterProjectService.get(project, imageFilter).imageFilter)
    }

    def delete = {
        Project project = projectService.read(params.long('project'))
        ImageFilter imageFilter = imageFilterService.read(params.long('imageFilter'))
        imageFilterProjectService.delete(project, imageFilter)
        responseSuccess([])
    }

}