package com.broadcom.wbi.security;

import com.broadcom.wbi.security.model.CustomError;
import com.broadcom.wbi.security.model.CustomResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//no need subclass
public final class SecurityUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    //constructor
    private SecurityUtils() {
    }

//	public static Authentication getCurrentLogin() {
//		EmployeePermissionService emplPermissionServ = new EmployeePermissionServiceImpl();
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails user = (UserDetails) authentication.getPrincipal();
//		List<EmployeePermission> permissions = emplPermissionServ.findByEmployeeName(user.getUsername());
//		Set<GrantedAuthority> gas = new HashSet<GrantedAuthority>();
//		if(permissions!= null){
//			for(EmployeePermission permission : permissions){
//				gas.add(new SimpleGrantedAuthority(permission.getPermission().toString().toLowerCase()));
//			}
//		}
//		Authentication new_auth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), gas);
//		SecurityContextHolder.getContext().sCustomError.javaetAuthentication(new_auth);
//		return SecurityContextHolder.getContext().getAuthentication();
//	}

    public static void sendError(HttpServletResponse response, Exception exception, int status, String message) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(status);
        PrintWriter writer = response.getWriter();
//        exception.getMessage()
        CustomError error = new CustomError("authError", exception.getLocalizedMessage());
        writer.write(mapper.writeValueAsString(new CustomResponse(status, message, error)));
        writer.flush();
        writer.close();
    }

    public static void sendResponse(HttpServletResponse response, int status, Object object) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        PrintWriter writer = response.getWriter();
        writer.write(mapper.writeValueAsString(object));
        response.setStatus(status);
        writer.flush();
        writer.close();
    }

}
