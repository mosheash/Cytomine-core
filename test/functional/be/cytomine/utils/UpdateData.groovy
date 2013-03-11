package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.server.Storage
import be.cytomine.laboratory.Sample
import be.cytomine.ontology.AnnotationProperty
import be.cytomine.security.User
import be.cytomine.image.Mime
import grails.converters.JSON

import org.apache.commons.logging.LogFactory
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.security.UserJob
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Discipline
import be.cytomine.security.Group
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobParameter
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.test.BasicInstanceBuilder

/**
 * User: lrollus
 * Date: 8/01/13
 * GIGA-ULg
 *
 */
class UpdateData {

    private static final log = LogFactory.getLog(this)

    static def createUpdateSet(AbstractImage AbstractImage) {
        String oldFilename = "oldName"
        String newFilename = "newName"

        Instrument oldScanner = BasicInstanceBuilder.getScanner()
        Instrument newScanner = BasicInstanceBuilder.getNewScannerNotExist()
        newScanner.save(flush:true)

        Sample oldSlide = BasicInstanceBuilder.getSlide()
        Sample newSlide = BasicInstanceBuilder.getSlideNotExist()
        newSlide.save(flush:true)

        User user2 = BasicInstanceBuilder.getUser()
        User user1 = BasicInstanceBuilder.getUserNotExist()
        user1.save(flush:true)


        String oldPath = "oldPath"
        String newPath = "newPath"

        Mime oldMime = BasicInstanceBuilder.getMime() //TODO: replace by a mime different with image server
        Mime newMime = BasicInstanceBuilder.getMime()  //jp2

        Integer oldWidth = 1000
        Integer newWidth = 9000

        Integer oldHeight = 10000
        Integer newHeight = 900000


        def mapNew = ["filename":newFilename,"scanner":newScanner.id,"path":newPath,"mime":newMime.extension,"width":newWidth,"height":newHeight]
        def mapOld = ["filename":oldFilename,"scanner":oldScanner.id,"path":oldPath,"mime":oldMime.extension,"width":oldWidth,"height":oldHeight]

        /* Create a old AbstractImage with point 1111 1111 */
        /* Create a old image */
        log.info("create image")
        AbstractImage imageToAdd = BasicInstanceBuilder.getAbstractImage()
        imageToAdd.filename = oldFilename
        imageToAdd.scanner = oldScanner
        imageToAdd.sample = oldSlide
        imageToAdd.path = oldPath
        imageToAdd.mime = oldMime
        imageToAdd.width = oldWidth
        imageToAdd.height = oldHeight
        imageToAdd.save(flush:true)

        /* Encode a new image to modify */
        AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.filename = newFilename
        jsonUpdate.scanner = newScanner.id
        jsonUpdate.slide = newSlide.id
        jsonUpdate.path = newPath
        jsonUpdate.mime = newMime.extension
        jsonUpdate.width = newWidth
        jsonUpdate.height = newHeight
        jsonImage = jsonUpdate.encodeAsJSON()

        return ['oldData':imageToEdit,'newData':jsonImage,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AlgoAnnotation annotation) {
        log.info "update algoAnnotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        UserJob user2 = annotation.user
        UserJob user1 = annotation.user

        def mapNew = ["location":newGeom,"user":user1.id]
        def mapOld = ["location":oldGeom,"user":user2.id]

        /* Create a old annotation with point 1111 1111 */
        log.info("create algoAnnotation")
        AlgoAnnotation annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = user2
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = user1.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AnnotationDomain annotation) {
        log.info "update AnnotationDomain:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        def user2 = annotation.user
        def user1 = annotation.user

        def mapNew = ["location":newGeom,"user":user1.id]
        def mapOld = ["location":oldGeom,"user":user2.id]

        /* Create a old annotation with point 1111 1111 */
        log.info("create AnnotationDomain")

        def jsonAnnotation

        if(annotation instanceof UserAnnotation) {
            log.info("create userAnnotation")
            UserAnnotation annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = user2
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = user1.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else if(annotation instanceof AlgoAnnotation) {
            AlgoAnnotation annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = user2
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = user1.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else {
            throw new Exception("Type is not supported!")
        }
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Discipline discipline) {
        log.info "update discipline"
        String oldName = "NAME1"
        String newName = "NAME2"
        def mapNew = ["name":newName]
        def mapOld = ["name":oldName]
        /* Create a Name1 discipline */
        Discipline disciplineToAdd = BasicInstanceBuilder.getDiscipline()
        disciplineToAdd.name = oldName
        assert (disciplineToAdd.save(flush:true) != null)
        /* Encode a niew discipline Name2*/
        Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
        def jsonDiscipline = disciplineToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonDiscipline)
        jsonUpdate.name = newName
        jsonDiscipline = jsonUpdate.encodeAsJSON()
        return ['oldData':discipline,'newData':jsonDiscipline,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Storage storage) {
        log.info ("update storage")
        String oldName = "NAME1"
        String newName = "NAME2"
        def mapNew = ["name":newName]
        def mapOld = ["name":oldName]
        /* Create a Name1 sample */
        Storage storageToAdd = BasicInstanceBuilder.getStorage()
        storageToAdd.name = oldName
        assert (storageToAdd.save(flush:true) != null)
        /* Encode a new sample Name2*/
        Storage storageToEdit = Storage.get(storageToAdd.id)
        def jsonSample = storageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSample)
        jsonUpdate.name = newName
        jsonSample = jsonUpdate.encodeAsJSON()
        return ['oldData':storage,'newData':jsonSample,'mapOld':mapOld,'mapNew':mapNew]

    }

    static def createUpdateSet(Sample sample) {
        log.info "update sample"
        String oldName = "NAME1"
        String newName = "NAME2"
        def mapNew = ["name":newName]
        def mapOld = ["name":oldName]
        /* Create a Name1 sample */
        Sample sampleToAdd = BasicInstanceBuilder.getSample()
        sampleToAdd.name = oldName
        assert (sampleToAdd.save(flush:true) != null)
        /* Encode a niew sample Name2*/
        Sample sampleToEdit = Sample.get(sampleToAdd.id)
        def jsonSample = sampleToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSample)
        jsonUpdate.name = newName
        jsonSample = jsonUpdate.encodeAsJSON()
        return ['oldData':sample,'newData':jsonSample,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(Group group) {
        String oldName = "NAME1"
        String newName = "NAME2"
        def mapNew = ["name":newName]
        def mapOld = ["name":oldName]
        /* Create a Name1 group */
        Group groupToAdd = BasicInstanceBuilder.getGroup()
        groupToAdd.name = oldName
        assert (groupToAdd.save(flush:true) != null)
        /* Encode a niew group Name2*/
        Group groupToEdit = Group.get(groupToAdd.id)
        def jsonGroup = groupToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonGroup)
        jsonUpdate.name = newName
        jsonGroup = jsonUpdate.encodeAsJSON()
        return ['oldData':group,'newData':jsonGroup,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(ImageInstance image) {
        log.info "update ImageInstance"
        Project oldProject = BasicInstanceBuilder.getProject()
        Project newProject = BasicInstanceBuilder.getProjectNotExist()
        newProject.save(flush: true)

        AbstractImage oldImage = BasicInstanceBuilder.getAbstractImage()
        AbstractImage newImage = BasicInstanceBuilder.getAbstractImageNotExist()
        newImage.save(flush: true)

        User user2 = BasicInstanceBuilder.getUser()
        User user1 = BasicInstanceBuilder.getUserNotExist()
        user1.save(flush: true)

        def mapNew = ["project": newProject.id, "baseImage": newImage.id, "user": user1.id]
        def mapOld = ["project": oldProject.id, "baseImage": oldImage.id, "user": user2.id]


        /* Create a old image */
        ImageInstance imageToAdd = BasicInstanceBuilder.getImageInstance()
        imageToAdd.project = oldProject;
        imageToAdd.baseImage = oldImage;
        imageToAdd.user = user2;
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.project = newProject.id
        jsonUpdate.baseImage = newImage.id
        jsonUpdate.user = user1.id

        jsonImage = jsonUpdate.encodeAsJSON()
        return ['oldData':image,'newData':jsonUpdate,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Job job) {
        log.info "update job"
        Integer oldProgress = 0
        Integer newProgress = 100

        def mapNew = ["progress": newProgress]
        def mapOld = ["progress": oldProgress]

        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstanceBuilder.getJob()
        jobToAdd.progress = oldProgress
        assert (jobToAdd.save(flush: true) != null)

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.progress = newProgress
        jsonJob = jsonUpdate.encodeAsJSON()
        return ['oldData':job,'newData':jsonJob,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(JobData Jobdata) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Job oldJob = BasicInstanceBuilder.getJob()
        Job newJob = BasicInstanceBuilder.getJobNotExist()
        newJob.save(flush: true)

        def mapNew = ["key": newName, "job": newJob.id]
        def mapOld = ["key": oldName, "job": oldJob.id]

        def jsonJobData = Jobdata.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobData)
        jsonUpdate.key = newName
        jsonUpdate.job = newJob.id
        jsonJobData = jsonUpdate.encodeAsJSON()
        return ['oldData':Jobdata,'newData':jsonJobData,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(JobParameter jobparameter) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 jobparameter */
        log.info("create jobparameter")
        JobParameter jobparameterToAdd = BasicInstanceBuilder.getJobParameter()
        jobparameterToAdd.value = oldValue
        assert (jobparameterToAdd.save(flush: true) != null)

        /* Encode a niew jobparameter Name2*/
        JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
        def jsonJobparameter = jobparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobparameter)
        jsonUpdate.value = newValue
        jsonJobparameter = jsonUpdate.encodeAsJSON()
        return ['oldData':jobparameter,'newData':jsonJobparameter,'mapOld':mapOld,'mapNew':mapNew]
    }





    static def createUpdateSet(CytomineDomain domain,def maps) {
         def mapOld = [:]
         def mapNew = [:]

        maps.each {
            String key = it.key
            mapOld[key] = extractValue(it.value[0])
            domain[key] = it.value[0]
        }

        BasicInstanceBuilder.saveDomain(domain)



        def json = JSON.parse(domain.encodeAsJSON())

        maps.each {
            String key = it.key
            mapNew[key] = extractValue(it.value[1])
            json[key] = extractValue(it.value[1])
        }

        println domain.encodeAsJSON()
        println domain.encodeAsJSON()

        println "mapOld="+mapOld
        println "mapNew="+mapNew

        return ['postData':json.toString(),'mapOld':mapOld,'mapNew':mapNew]


    }

    static extractValue(def value) {
        println "extractValue=$value"
        println "extractValue.class="+value.class
        println "extractValue.class.isInstance="+value.class.isInstance(CytomineDomain)
        if (value.class.toString().contains("be.cytomine")) {
            //if cytomine domain, get its id
            return value.id
        } else {
            return value
        }
    }

    static def createUpdateSet(Project project) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Ontology oldOtology = BasicInstanceBuilder.getOntology()
        Ontology newOtology = BasicInstanceBuilder.getOntologyNotExist()
        newOtology.save(flush: true)

        String oldDescription = "DescriptionOld"
        String newDescription = "DescriptionNew"

        def mapNew = ["name": newName, "ontology": newOtology.id, "description": newDescription]
        def mapOld = ["name": oldName, "ontology": oldOtology.id, "description": oldDescription]

        def jsonProject = project.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = newName
        jsonUpdate.ontology = newOtology.id
        jsonUpdate.description = newDescription
        jsonProject = jsonUpdate.encodeAsJSON()
        return ['oldData':project,'newData':jsonProject,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(AnnotationFilter af) {
        String oldName = "Name1"
        String newName = Math.random()+""

        def mapNew = ["name": newName]
        def mapOld = ["name": oldName]

        def json = af.encodeAsJSON()
        def jsonUpdate = JSON.parse(json)
        jsonUpdate.name = newName
        json = jsonUpdate.encodeAsJSON()
        return ['oldData':af,'newData':json,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(ReviewedAnnotation annotation) {
        log.info "update reviewedannotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        User user2 = annotation.user
        User user1 = annotation.user

        Term oldTerm = BasicInstanceBuilder.getTerm()
        Term newTerm = BasicInstanceBuilder.getTermNotExist()
        newTerm.save(flush: true)

        def mapNew = ["location":newGeom,"user":user1.id,"terms":[newTerm.id]]
        def mapOld = ["location":oldGeom,"user":user2.id,"terms":[oldTerm.id]]

        /* Create a old annotation with point 1111 1111 */
        log.info("create reviewedannotation")
        ReviewedAnnotation annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = user2
        annotationToAdd.addToTerms(oldTerm)
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        ReviewedAnnotation annotationToEdit = ReviewedAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = user1.id
        jsonUpdate.terms = [newTerm.id]
        jsonAnnotation = jsonUpdate.encodeAsJSON()
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(Software software) {
        log.info "update software"
        String oldName = "Name1"
        String newName = "Name2"
        String oldNameService = "projectService"
        String newNameService = "userAnnotationService"

        def mapNew = ["name": newName,"serviceName" : newNameService]
        def mapOld = ["name": oldName,"serviceName" : oldNameService]
        /* Create a Name1 software */
        Software softwareToAdd = BasicInstanceBuilder.getSoftware()
        softwareToAdd.name = oldName
        softwareToAdd.serviceName = oldNameService
        assert (softwareToAdd.save(flush:true) != null)
        /* Encode a niew software Name2*/
        Software softwareToEdit = Software.get(softwareToAdd.id)
        def jsonSoftware = softwareToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.name = newName
        jsonUpdate.serviceName = newNameService
        jsonSoftware = jsonUpdate.encodeAsJSON()
        return ['oldData':software,'newData':jsonSoftware,'mapOld':mapOld,'mapNew':mapNew]
    }

    static def createUpdateSet(SoftwareParameter softwareparameter) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 softwareparameter */
        log.info("create softwareparameter")
        SoftwareParameter softwareparameterToAdd = BasicInstanceBuilder.getSoftwareParameter()
        softwareparameterToAdd.name = oldValue
        assert (softwareparameterToAdd.save(flush: true) != null)

        /* Encode a niew softwareparameter Name2*/
        SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
        def jsonSoftwareparameter = softwareparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftwareparameter)
        jsonUpdate.value = newValue
        jsonSoftwareparameter = jsonUpdate.encodeAsJSON()
        return ['oldData':softwareparameter,'newData':jsonSoftwareparameter,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(Term term) {
        String oldName = "Name1"
        String newName = "Name2"

        String oldComment = "Comment1"
        String newComment = "Comment2"

        String oldColor = "000000"
        String newColor = "FFFFFF"

        Ontology oldOntology = BasicInstanceBuilder.getOntology()
        Ontology newOntology = BasicInstanceBuilder.getOntologyNotExist()
        newOntology.save(flush:true)

        def mapOld = ["name":oldName,"comment":oldComment,"color":oldColor,"ontology":oldOntology.id]
        def mapNew = ["name":newName,"comment":newComment,"color":newColor,"ontology":newOntology.id]


        /* Create a Name1 term */
        log.info("create term")
        Term termToAdd = BasicInstanceBuilder.getTerm()
        termToAdd.name = oldName
        termToAdd.comment = oldComment
        termToAdd.color = oldColor
        termToAdd.ontology = oldOntology
        assert (termToAdd.save(flush:true) != null)

        /* Encode a niew term Name2*/
        Term termToEdit = Term.get(termToAdd.id)
        def jsonTerm = termToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonTerm)
        jsonUpdate.name = newName
        jsonUpdate.comment = newComment
        jsonUpdate.color = newColor
        jsonUpdate.ontology = newOntology.id
        jsonTerm = jsonUpdate.encodeAsJSON()
        return ['oldData':term,'newData':jsonTerm,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(UserAnnotation annotation) {

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        User user2 = annotation.user
        User user1 = annotation.user

        def mapNew = ["location":newGeom,"user":user1.id]
        def mapOld = ["location":oldGeom,"user":user2.id]

        /* Create a old annotation with point 1111 1111 */
        log.info("create userAnnotation")
        UserAnnotation annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = user2
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = user1.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()
        return ['oldData':annotation,'newData':jsonAnnotation,'mapOld':mapOld,'mapNew':mapNew]
    }


    static def createUpdateSet(User user) {
        log.info "update user"
        String oldFirstname = "Firstname1"
        String newFirstname = "Firstname2"

        String oldLastname = "Lastname1"
        String newLastname = "Lastname2"

        String oldEmail = "old@email.com"
        String newEmail = "new@email.com"

        String user2name = "Username1"
        String user1name = "Username2"


        def mapOld = ["firstname":oldFirstname,"lastname":oldLastname,"email":oldEmail,"username":user2name]
        def mapNew = ["firstname":newFirstname,"lastname":newLastname,"email":newEmail,"username":user1name]


        /* Create a Name1 user */
        log.info("create user")
        User userToAdd = BasicInstanceBuilder.getUser()
        userToAdd.firstname = oldFirstname
        userToAdd.lastname = oldLastname
        userToAdd.email = oldEmail
        userToAdd.username = user2name
        assert (userToAdd.save(flush:true) != null)

        /* Encode a niew user Name2*/
        User userToEdit = User.get(userToAdd.id)
        def jsonUser = userToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonUser)
        jsonUpdate.firstname = newFirstname
        jsonUpdate.lastname = newLastname
        jsonUpdate.email = newEmail
        jsonUpdate.username = user1name
        jsonUser = jsonUpdate.encodeAsJSON()
        return ['oldData':user,'newData':jsonUser,'mapOld':mapOld,'mapNew':mapNew]
    }

  static def createUpdateSet(AnnotationProperty annotationProperty) {
        log.info "update annotationProperty"

        String oldKey = "Key1"
        String newKey = "Key2"
        String oldValue = "Value1"
        String newValue = "Value2"

        def mapNew = ["value":newValue,"key":newKey]
        def mapOld = ["value":oldValue,"key":oldKey]

        AnnotationProperty annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationProperty()
        annotationPropertyToAdd.value = oldValue
        assert (annotationPropertyToAdd.save(flush: true) != null)

        AnnotationProperty annotationPropertyToEdit = AnnotationProperty.get(annotationPropertyToAdd.id)
        def json = annotationPropertyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(json)
        jsonUpdate.key = newKey
        jsonUpdate.value = newValue
        json = jsonUpdate.encodeAsJSON()

        return ['oldData' : annotationProperty, 'newData' : json, 'mapOld': mapOld, 'mapNew' : mapNew]
    }
}

