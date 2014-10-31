package cytomine.web

import be.cytomine.security.AuthWithToken
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.SecurityUtils
import grails.plugin.springsecurity.SpringSecurityUtils

import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class APIAuthentificationFilters implements javax.servlet.Filter {


    void init(FilterConfig filterConfig) {

    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        //log with token id
        boolean token = tryAPIAUhtentificationWithToken(request, response)
        if(!token) {
            //with signature (in header)
            tryAPIAuthentification(request, response)
        }

        chain.doFilter(request, response)
    }

    void destroy() {}


    /**
     * http://code.google.com/apis/storage/docs/reference/v1/developer-guidev1.html#authentication
     */
    private boolean tryAPIAuthentification(def request, def response) {
        String authorization = request.getHeader("authorization")
        if (request.getHeader("date") == null) {
            return false
        }
        if (request.getHeader("host") == null) {
            return false
        }
        if (authorization == null) {
            return false
        }
        if (!authorization.startsWith("CYTOMINE") || !authorization.indexOf(" ") == -1 || !authorization.indexOf(":") == -1) {
            return false
        }
        try {

            String content_md5 = (request.getHeader("content-MD5") != null) ? request.getHeader("content-MD5") : ""
            String content_type = (request.getHeader("content-type") != null) ? request.getHeader("content-type") : ""
            content_type = (request.getHeader("Content-Type") != null) ? request.getHeader("Content-Type") : content_type
            String date = (request.getHeader("date") != null) ? request.getHeader("date") : ""

            String queryString = (request.getQueryString() != null) ? "?" + request.getQueryString() : ""

            String path = request.forwardURI //original URI Request

            String accessKey = authorization.substring(authorization.indexOf(" ") + 1, authorization.indexOf(":"))
            String authorizationSign = authorization.substring(authorization.indexOf(":") + 1)
            SecUser user = SecUser.findByPublicKey(accessKey)
            if (!user) {
                return false
            }

            String signature = SecurityUtils.generateKeys(request.getMethod(),content_md5, content_type,date,queryString,path,user)
            if (authorizationSign == signature) {
                SpringSecurityUtils.reauthenticate user.getUsername(), null
                return true
            } else {
                return false
            }

        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
        return false
    }

    private boolean tryAPIAUhtentificationWithToken(ServletRequest request, ServletResponse response) {
        String tokenKey = request.getParameter("tokenKey");

        if(tokenKey!=null) {
            log.info "login with token: $tokenKey"
            String username = request.getParameter("username")
            User user = User.findByUsername(username) //we are not logged, we bypass the service

            log.info "user: $user"
            log.info "tokenKey: $tokenKey"
            AuthWithToken authToken = AuthWithToken.findByTokenKeyAndUser(tokenKey, user)
            log.info "authToken: $authToken"
            log.info "authToken: ${authToken?.isValid()}"
            //check first if a entry is made for this token
            if (authToken && authToken.isValid())  {
                SpringSecurityUtils.reauthenticate user.username, null
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }


    def filters = {
        all(uri:'/api/**') {
            before = {
            }
            after = {

            }
            afterView = {

            }
        }
    }

}
