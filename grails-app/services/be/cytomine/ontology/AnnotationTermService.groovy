package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser

class AnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def commandService
    def responseService
    def domainService

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("hasPermission(#annotation.project.id,'be.cytomine.project.Project',read) or hasPermission(#annotation.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def list(Annotation annotation) {
        annotation.annotationTerm
    }

    @PreAuthorize("hasPermission(#annotation.project.id,'be.cytomine.project.Project',read) or hasPermission(#annotation.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def listNotUser(Annotation annotation, User user) {
        AnnotationTerm.findAllByAnnotationAndUserNotEqual(annotation, user)
    }

    @PreAuthorize("hasPermission(#image.project.id,'be.cytomine.project.Project',read) or hasPermission(#image.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, Term term) {

        def annotations = []
        Annotation.findAllByImage(image).each { annotation ->
            annotation.annotationTerm.each { annotationTerm ->
                if (annotationTerm.getTerm() == term) annotations << annotation
            }
        }
        annotations
    }

    @PreAuthorize("hasPermission(#annotation.project.id,'be.cytomine.project.Project',read) or hasPermission(#annotation.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def read(Annotation annotation, Term term, User user) {
        if (user) AnnotationTerm.findWhere('annotation': annotation, 'term': term, 'user': user)
        else AnnotationTerm.findByAnnotationAndTerm(annotation, term)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        SecUser creator = SecUser.read(json.user)
        if(!creator)
            json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def addAnnotationTerm(def idAnnotation, def idTerm, def idExpectedTerm, def idUser, User currentUser,Transaction transaction) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, expectedTerm: $idExpectedTerm, user: $idUser}")
        return executeCommand(new AddCommand(user: currentUser,transaction:transaction), json)
    }

    @PreAuthorize("#domain.user.id == principal.id or hasRole('ROLE_ADMIN')")
    def delete(def domain,def json) {
        User currentUser = cytomineService.getCurrentUser()
        return deleteAnnotationTerm(json.annotation, json.term, json.expectedTerm, domain.user.id, currentUser,null)
    }

    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    def addWithDeletingOldTerm(def idAnnotation, def idterm) {
        User currentUser = cytomineService.getCurrentUser()
        Annotation annotation = Annotation.read(idAnnotation)
        if (!annotation) throw new ObjectNotFoundException("Annotation $idAnnotation not found")
        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete all annotation term
        deleteAnnotationTermFromUser(annotation, currentUser, currentUser,transaction)

        //Add annotation term
        def result = addAnnotationTerm(idAnnotation, idterm, null, currentUser.id, currentUser,transaction)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Delete an annotation term
     */
    def deleteAnnotationTerm(def idAnnotation, def idTerm, def idExpectedTerm, def idUser, User currentUser, Transaction transaction) {
        return deleteAnnotationTerm(idAnnotation, idTerm, idExpectedTerm, idUser, currentUser, true,transaction)
    }

    def deleteAnnotationTerm(def idAnnotation, def idTerm, def idExpectedTerm, def idUser, User currentUser, boolean printMessage, Transaction transaction) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, expectedTerm: $idExpectedTerm, user: $idUser}")
        def result = executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
        return result
    }

    /**
     * Delete all term map by user for annotation
     */
    def deleteAnnotationTermFromUser(Annotation annotation, User user, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByAnnotationAndUser(annotation, user)
        log.info "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.expectedTerm?.id,annotterm.user.id, currentUser, false,transaction)
        }
    }

    /**
     * Delete all term map for annotation
     */
    def deleteAnnotationTermFromAllUser(Annotation annotation, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByAnnotation(annotation)
        log.info "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.expectedTerm?.id,annotterm.user.id, currentUser, false,transaction)
        }
    }

    /**
     * Delete all term map by user for term
     */
    def deleteAnnotationTermFromAllUser(Term term, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByTerm(term)
        log.info "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.expectedTerm?.id, annotterm.user.id, currentUser, false,transaction)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(AnnotationTerm.createFromDataWithId(json), printMessage)
    }

    def create(AnnotationTerm domain, boolean printMessage) {
        //Build response message
        log.debug "domain=" + domain + " responseService=" + responseService
        //Save new object

        log.info '##### INSERT annotation': domain?.annotation?.id, 'term': domain?.term?.id, 'expectedTerm': domain?.expectedTerm?.id, 'user': domain?.user?.id
        //domainService.saveDomain(domain)
        domain = AnnotationTerm.link(domain.annotation, domain.term, domain.expectedTerm, domain.user,domain.rate,domain.algo)
        def response = responseService.createResponseMessage(domain, [domain.id, domain.annotation.id, domain.term.name, domain.user?.username], printMessage, "Add", domain.getCallBack())

        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, boolean printMessage) {
        destroy(AnnotationTerm.createFromData(json), printMessage)
    }

    def destroy(AnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.annotation.id, domain.term.name, domain.user?.username], printMessage, "Delete", domain.getCallBack())
        //Delete new object

        log.info "##############################################"
        log.info "DELETE DOMAIN:" + domain
        log.info "ANNOTATION LINK:" + domain?.annotation?.annotationTerm
        log.info "TERM LINK:" + domain?.term?.annotationTerm
        log.info "##############################################"
        AnnotationTerm.unlink(domain.annotation, domain.term, domain.expectedTerm, domain.user)
        //domainService.deleteDomain(domain)
        return response
    }

    AnnotationTerm createFromJSON(def json) {
        return AnnotationTerm.createFromData(json)
    }

    def retrieve(def json) {
        Annotation annotation = Annotation.get(json.annotation)
        Term term = Term.get(json.term)
        Term expectedTerm = (!json.expectedTerm.toString().equals("null")) ? Term.get(json.expectedTerm) : null
        User user = User.get(json.user)
        log.info '##### SELECT annotation': annotation?.id, 'term': term?.id, 'expectedTerm': expectedTerm?.id, 'user': user?.id
        AnnotationTerm relation = AnnotationTerm.findWhere('annotation': annotation, 'term': term, 'expectedTerm': expectedTerm, 'user': user)
        if (!relation) throw new ObjectNotFoundException("Annotation term not found ($annotation,$term,$expectedTerm,$user)")
        return relation
    }
}
