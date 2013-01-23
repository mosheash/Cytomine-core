package be.cytomine.security

import be.cytomine.processing.Software
import be.cytomine.test.Infos
import be.cytomine.test.http.SoftwareAPI
import be.cytomine.utils.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.SoftwareParameterAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SoftwareParameterSecurityTests extends SecurityTestsAbstract {
    
  void testSoftwareParameterSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //create software
      Software software = BasicInstance.getBasicSoftwareNotExist()
      BasicInstance.saveDomain(software)
      def softwareParameter = BasicInstance.getBasicSoftwareParameterNotExist()
      softwareParameter.software = software
      Infos.addUserRight(user1,software)

      //Create new software param (user1)
      def result = SoftwareParameterAPI.create(softwareParameter.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      softwareParameter = result.data

      //check if admin user can access/update/delete
      assertEquals(200, SoftwareParameterAPI.show(softwareParameter.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assertTrue(SoftwareParameterAPI.containsInJSONList(softwareParameter.id,JSON.parse(SoftwareParameterAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assertEquals(200, SoftwareParameterAPI.update(softwareParameter.id,softwareParameter.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assertEquals(200, SoftwareParameterAPI.delete(softwareParameter.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testSoftwareParameterSecurityForSoftwareCreator() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //create software
      Software software = BasicInstance.getBasicSoftwareNotExist()
      BasicInstance.saveDomain(software)
      Infos.addUserRight(user1,software)
      def softwareParameter = BasicInstance.getBasicSoftwareParameterNotExist()
      softwareParameter.software = software

      //Create new software param (user1)
      def result = SoftwareParameterAPI.create(softwareParameter.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      softwareParameter = result.data

      //check if user 1 can access/update/delete
      assertEquals(200, SoftwareParameterAPI.show(softwareParameter.id,USERNAME1,PASSWORD1).code)
      assertEquals(200, SoftwareParameterAPI.update(softwareParameter.id,softwareParameter.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assertEquals(200, SoftwareParameterAPI.delete(softwareParameter.id,USERNAME1,PASSWORD1).code)
  }

  void testSoftwareParameterSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //create software
      Software software = BasicInstance.getBasicSoftwareNotExist()
      BasicInstance.saveDomain(software)
      Infos.addUserRight(user1,software)
      def softwareParameter = BasicInstance.getBasicSoftwareParameterNotExist()
      softwareParameter.software = software

      //Create new software param (user1)
      def result = SoftwareParameterAPI.create(softwareParameter.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      softwareParameter = result.data
      //check if user 2 cannot access/update/delete
      assertEquals(200, SoftwareParameterAPI.show(softwareParameter.id,USERNAME2,PASSWORD2).code)
      assertEquals(403, SoftwareParameterAPI.update(softwareParameter.id,softwareParameter.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assertEquals(403, SoftwareParameterAPI.delete(softwareParameter.id,USERNAME2,PASSWORD2).code)

  }

  void testSoftwareParameterSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //create software
      Software software = BasicInstance.getBasicSoftwareNotExist()
      BasicInstance.saveDomain(software)
      Infos.addUserRight(user1,software)
      def softwareParameter = BasicInstance.getBasicSoftwareParameterNotExist()
      softwareParameter.software = software

      //Create new software param (user1)
      def result = SoftwareParameterAPI.create(softwareParameter.encodeAsJSON(),USERNAME1,PASSWORD1)
      assertEquals(200, result.code)
      softwareParameter = result.data
      //check if user 2 cannot access/update/delete
      assertEquals(401, SoftwareParameterAPI.show(softwareParameter.id,USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, SoftwareParameterAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, SoftwareParameterAPI.update(softwareParameter.id,software.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assertEquals(401, SoftwareParameterAPI.delete(softwareParameter.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}